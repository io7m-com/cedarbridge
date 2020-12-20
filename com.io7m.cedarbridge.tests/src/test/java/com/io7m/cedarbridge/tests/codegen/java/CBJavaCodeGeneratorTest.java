/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.cedarbridge.tests.codegen.java;

import com.io7m.cedarbridge.codegen.java.CBCGJavaFactory;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.CBExpressionSources;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceType;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.binder.CBBinderFactory;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.core_types.CBCore;
import com.io7m.cedarbridge.schema.parser.CBParserFactory;
import com.io7m.cedarbridge.schema.typer.CBTypeCheckerFactory;
import com.io7m.cedarbridge.tests.CBFakeLoader;
import com.io7m.cedarbridge.tests.CBFakePackage;
import com.io7m.cedarbridge.tests.CBTestDirectories;
import com.io7m.cedarbridge.tests.CBZip;
import com.sun.source.util.JavacTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CBJavaCodeGeneratorTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBJavaCodeGeneratorTest.class);

  private CBExpressionSourceType source;
  private CBBinderFactory binders;
  private CBParserFactory parsers;
  private CBTypeCheckerFactory typeCheckers;
  private CBExpressionSources sources;
  private ArrayList<CBError> errors;
  private Path directory;
  private Path moduleDirectory;
  private CBFakeLoader loader;
  private HashMap<BigInteger, CBBindingLocalType> bindings;
  private CBCGJavaFactory codeGen;

  private void compileJava(
    final List<Path> createdFiles)
    throws IOException
  {
    final var listener =
      new Diagnostics();
    final var tool =
      ToolProvider.getSystemJavaCompiler();

    try (var fileManager = tool.getStandardFileManager(listener, ROOT, UTF_8)) {
      final var compileFiles =
        new ArrayList<SimpleJavaFileObject>(createdFiles.size());
      for (final var created : createdFiles) {
        LOG.info("compile {}", created.toUri());
        compileFiles.add(new SourceFile(created));
      }

      final var compileArguments =
        List.of(
          "-Werror",
          "-Xdiags:verbose",
          "-Xlint:unchecked",
          "-d",
          this.directory.toAbsolutePath().toString()
        );

      final var task =
        (JavacTask) tool.getTask(
          null,
          fileManager,
          listener,
          compileArguments,
          null,
          compileFiles
        );

      assertTrue(
        task.call().booleanValue(),
        "Compilation of all files must succeed");
    }

    this.createModule();
  }

  private void createModule()
    throws IOException
  {
    final var moduleFile = this.moduleDirectory.resolve("module.jar");
    LOG.debug("creating module {}", moduleFile);

    final var metaInf = this.directory.resolve("META-INF");
    Files.createDirectories(metaInf);
    final var manifest = metaInf.resolve("MANIFEST.MF");

    try (var out = Files.newBufferedWriter(manifest)) {
      out.append("Manifest-Version: 1.0");
      out.newLine();
      out.append("Automatic-Module-Name: com.io7m.cedarbridge.tests.generated");
      out.newLine();
    }

    CBZip.create(moduleFile, this.directory);
  }

  private CBASTPackage parse(
    final String name)
    throws Exception
  {
    final var path =
      CBTestDirectories.resourceOf(
        CBJavaCodeGeneratorTest.class,
        this.directory,
        name);
    this.source =
      this.sources.create(path.toUri(), Files.newInputStream(path));
    try (var parser =
           this.parsers.createParser(this::addError, this.source)) {
      return parser.execute();
    }
  }

  private CBPackageType check(
    final String name)
    throws Exception
  {
    final var parsedPackage = this.parse(name);
    try (var binder =
           this.binders.createBinder(
             this.loader, this::addError, this.source, parsedPackage)) {
      binder.execute();
      try (var checker =
             this.typeCheckers.createTypeChecker(
               this::addError, this.source, parsedPackage)) {
        checker.execute();
        return parsedPackage.userData()
          .get(CBPackageType.class);
      }
    }
  }

  private void addError(
    final CBError error)
  {
    LOG.error("{}", error.message());
    this.errors.add(error);
  }

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.errors = new ArrayList<>();
    this.bindings = new HashMap<>();
    this.directory = CBTestDirectories.createTempDirectory();
    this.moduleDirectory = CBTestDirectories.createTempDirectory();
    this.sources = new CBExpressionSources();
    this.parsers = new CBParserFactory();
    this.binders = new CBBinderFactory();
    this.loader = new CBFakeLoader();
    this.typeCheckers = new CBTypeCheckerFactory();
    this.codeGen = new CBCGJavaFactory();
  }

  private ClassLoader loadClasses()
  {
    final var finder =
      ModuleFinder.of(this.moduleDirectory);
    final var parent =
      ModuleLayer.boot();

    final var configuration =
      parent.configuration()
        .resolve(
          finder,
          ModuleFinder.of(),
          Set.of("com.io7m.cedarbridge.tests.generated")
        );

    final var systemClassLoader =
      ClassLoader.getSystemClassLoader();
    final var layer =
      parent.defineModulesWithOneLoader(configuration, systemClassLoader);
    return layer.findLoader("com.io7m.cedarbridge.tests.generated");
  }

  @Test
  public void testRecordOk0()
    throws Exception
  {
    final var pack = this.check("typeRecordOk0.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.T");
    loader.loadClass("x.y.z.TSerializer");
    loader.loadClass("x.y.z.TSerializerFactory");
  }

  @Test
  public void testRecordOk1()
    throws Exception
  {
    final var pack = this.check("typeRecordOk1.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.T");
    loader.loadClass("x.y.z.TSerializer");
    loader.loadClass("x.y.z.TSerializerFactory");
    loader.loadClass("x.y.z.U");
    loader.loadClass("x.y.z.USerializer");
    loader.loadClass("x.y.z.USerializerFactory");
  }

  @Test
  public void testRecordOk2()
    throws Exception
  {
    final var pack = this.check("typeRecordOk2.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.U");
    loader.loadClass("x.y.z.USerializer");
    loader.loadClass("x.y.z.USerializerFactory");
  }

  @Test
  public void testRecordOk3()
    throws Exception
  {
    final var pack = this.check("typeRecordOk3.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.T");
    loader.loadClass("x.y.z.TSerializer");
    loader.loadClass("x.y.z.TSerializerFactory");
    loader.loadClass("x.y.z.U");
    loader.loadClass("x.y.z.USerializer");
    loader.loadClass("x.y.z.USerializerFactory");
    loader.loadClass("x.y.z.V");
    loader.loadClass("x.y.z.VSerializer");
    loader.loadClass("x.y.z.VSerializerFactory");
  }

  @Test
  public void testRecordOk4()
    throws Exception
  {
    final var pack = this.check("typeRecordOk4.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.T");
    loader.loadClass("x.y.z.TSerializer");
    loader.loadClass("x.y.z.TSerializerFactory");
    loader.loadClass("x.y.z.U");
    loader.loadClass("x.y.z.USerializer");
    loader.loadClass("x.y.z.USerializerFactory");
    loader.loadClass("x.y.z.V");
    loader.loadClass("x.y.z.VSerializer");
    loader.loadClass("x.y.z.VSerializerFactory");
  }

  @Test
  public void testVariantOk0()
    throws Exception
  {
    final var pack = this.check("typeVariantOk0.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.T");
    loader.loadClass("x.y.z.TSerializer");
    loader.loadClass("x.y.z.TSerializerFactory");
  }

  @Test
  public void testVariantOk1()
    throws Exception
  {
    final var pack = this.check("typeVariantOk1.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.T");
    loader.loadClass("x.y.z.TSerializer");
    loader.loadClass("x.y.z.TSerializerFactory");
    loader.loadClass("x.y.z.U");
    loader.loadClass("x.y.z.USerializer");
    loader.loadClass("x.y.z.USerializerFactory");
  }

  @Test
  public void testVariantOk2()
    throws Exception
  {
    final var pack = this.check("typeVariantOk2.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.U");
    loader.loadClass("x.y.z.USerializer");
    loader.loadClass("x.y.z.USerializerFactory");
  }

  @Test
  public void testVariantOk3()
    throws Exception
  {
    final var pack = this.check("typeVariantOk3.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.T");
    loader.loadClass("x.y.z.TSerializer");
    loader.loadClass("x.y.z.TSerializerFactory");
    loader.loadClass("x.y.z.U");
    loader.loadClass("x.y.z.USerializer");
    loader.loadClass("x.y.z.USerializerFactory");
    loader.loadClass("x.y.z.V");
    loader.loadClass("x.y.z.VSerializer");
    loader.loadClass("x.y.z.VSerializerFactory");
  }

  @Test
  public void testVariantOk4()
    throws Exception
  {
    final var pack = this.check("typeVariantOk4.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.T");
    loader.loadClass("x.y.z.TSerializer");
    loader.loadClass("x.y.z.TSerializerFactory");
    loader.loadClass("x.y.z.U");
    loader.loadClass("x.y.z.USerializer");
    loader.loadClass("x.y.z.USerializerFactory");
    loader.loadClass("x.y.z.V");
    loader.loadClass("x.y.z.VSerializer");
    loader.loadClass("x.y.z.VSerializerFactory");
  }
  
  @Test
  public void testBasic0()
    throws Exception
  {
    final var pack = this.check("basicNoImport.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("com.io7m.cedarbridge.Option");
    loader.loadClass("com.io7m.cedarbridge.OptionSerializer");
    loader.loadClass("com.io7m.cedarbridge.OptionSerializerFactory");
    loader.loadClass("com.io7m.cedarbridge.List");
    loader.loadClass("com.io7m.cedarbridge.ListSerializer");
    loader.loadClass("com.io7m.cedarbridge.ListSerializerFactory");
    loader.loadClass("com.io7m.cedarbridge.Unit");
    loader.loadClass("com.io7m.cedarbridge.UnitSerializer");
    loader.loadClass("com.io7m.cedarbridge.UnitSerializerFactory");
    loader.loadClass("com.io7m.cedarbridge.Map");
    loader.loadClass("com.io7m.cedarbridge.MapSerializer");
    loader.loadClass("com.io7m.cedarbridge.MapSerializerFactory");
    loader.loadClass("com.io7m.cedarbridge.Pair");
    loader.loadClass("com.io7m.cedarbridge.PairSerializer");
    loader.loadClass("com.io7m.cedarbridge.PairSerializerFactory");
  }

  @Test
  public void testBasic()
    throws Exception
  {
    this.loader.register(new CBFakePackage("x.y.z"));
    this.loader.register(CBCore.get());

    final var pack = this.check("basic.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("com.io7m.cedarbridge.ProtocolZType");
    loader.loadClass("com.io7m.cedarbridge.ProtocolZv0Type");
    loader.loadClass("com.io7m.cedarbridge.Option");
    loader.loadClass("com.io7m.cedarbridge.OptionSerializer");
    loader.loadClass("com.io7m.cedarbridge.OptionSerializerFactory");
    loader.loadClass("com.io7m.cedarbridge.List");
    loader.loadClass("com.io7m.cedarbridge.ListSerializer");
    loader.loadClass("com.io7m.cedarbridge.ListSerializerFactory");
    loader.loadClass("com.io7m.cedarbridge.UnitType");
    loader.loadClass("com.io7m.cedarbridge.UnitTypeSerializer");
    loader.loadClass("com.io7m.cedarbridge.UnitTypeSerializerFactory");
    loader.loadClass("com.io7m.cedarbridge.Map");
    loader.loadClass("com.io7m.cedarbridge.MapSerializer");
    loader.loadClass("com.io7m.cedarbridge.MapSerializerFactory");
    loader.loadClass("com.io7m.cedarbridge.Pair");
    loader.loadClass("com.io7m.cedarbridge.PairSerializer");
    loader.loadClass("com.io7m.cedarbridge.PairSerializerFactory");
  }

  @Test
  public void testBasicWithCore()
    throws Exception
  {
    this.loader.register(CBCore.get());

    final var pack = this.check("basicWithCore.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.Mix");
    loader.loadClass("x.y.z.MixSerializer");
    loader.loadClass("x.y.z.MixSerializerFactory");
  }

  @Test
  public void testBasicType3()
    throws Exception
  {
    this.loader.register(CBCore.get());

    final var pack = this.check("basicType3.cbs");
    assertEquals(0, this.errors.size());

    final var result =
      this.codeGen.createGenerator(
        CBSPICodeGeneratorConfiguration.builder()
          .setOutputDirectory(this.directory)
          .build()
      ).execute(pack);

    this.compileJava(result.createdFiles());

    final var loader = this.loadClasses();
    loader.loadClass("x.y.z.Q");
    loader.loadClass("x.y.z.QSerializer");
    loader.loadClass("x.y.z.QSerializerFactory");
    loader.loadClass("x.y.z.List");
    loader.loadClass("x.y.z.ListSerializer");
    loader.loadClass("x.y.z.ListSerializerFactory");
    loader.loadClass("x.y.z.Z");
    loader.loadClass("x.y.z.ZSerializer");
    loader.loadClass("x.y.z.ZSerializerFactory");
  }
}
