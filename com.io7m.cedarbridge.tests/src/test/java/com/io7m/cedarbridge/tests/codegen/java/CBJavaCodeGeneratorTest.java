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
import com.sun.source.util.JavacTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static javax.tools.JavaFileObject.Kind.SOURCE;
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

      final JavacTask task =
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
    this.sources = new CBExpressionSources();
    this.parsers = new CBParserFactory();
    this.binders = new CBBinderFactory();
    this.loader = new CBFakeLoader();
    this.typeCheckers = new CBTypeCheckerFactory();
    this.codeGen = new CBCGJavaFactory();
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
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

    compileJava(result.createdFiles());
  }

  private static final class SourceFile
    extends SimpleJavaFileObject
  {
    private final Path path;

    SourceFile(final Path inPath)
    {
      super(inPath.toUri(), SOURCE);
      this.path = Objects.requireNonNull(inPath, "path");
    }

    @Override
    public CharSequence getCharContent(
      final boolean ignoreEncodingErrors)
      throws IOException
    {
      return Files.readString(this.path, UTF_8);
    }
  }

  private static final class Diagnostics
    implements DiagnosticListener<JavaFileObject>
  {
    Diagnostics()
    {

    }

    @Override
    public void report(
      final Diagnostic<? extends JavaFileObject> diagnostic)
    {
      final URI sourceURI;
      if (diagnostic.getSource() != null) {
        sourceURI = diagnostic.getSource().toUri();
      } else {
        sourceURI = URI.create("urn:unavailable");
      }

      LOG.error(
        "{}: {}:{}:{}: {}",
        diagnostic.getCode(),
        sourceURI,
        Long.valueOf(diagnostic.getLineNumber()),
        Long.valueOf(diagnostic.getColumnNumber()),
        diagnostic.getMessage(ROOT)
      );
    }
  }
}
