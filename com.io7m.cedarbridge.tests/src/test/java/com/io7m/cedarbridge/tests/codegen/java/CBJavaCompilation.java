/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
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
import com.io7m.cedarbridge.schema.parser.CBParserFactory;
import com.io7m.cedarbridge.schema.typer.CBTypeCheckerFactory;
import com.io7m.cedarbridge.tests.CBFakeLoader;
import com.io7m.cedarbridge.tests.CBTestDirectories;
import com.io7m.cedarbridge.tests.CBZip;
import com.sun.source.util.JavacTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.module.ModuleFinder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Locale.ROOT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CBJavaCompilation
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBJavaCompilation.class);

  private final CBBinderFactory binders;
  private final CBParserFactory parsers;
  private final CBTypeCheckerFactory typeCheckers;
  private final CBExpressionSources sources;
  private final Path directory;
  private final Path moduleDirectory;
  private final CBCGJavaFactory codeGen;
  private final CBFakeLoader loader;
  private CBExpressionSourceType source;
  private ArrayList<CBError> errors;
  private ClassLoader classLoader;

  private CBJavaCompilation(
    final CBFakeLoader inLoader,
    final Path inOutputDirectory,
    final Path inModuleDirectory)
  {
    this.loader =
      Objects.requireNonNull(inLoader, "loader");
    this.directory =
      Objects.requireNonNull(inOutputDirectory, "outputDirectory");
    this.moduleDirectory =
      Objects.requireNonNull(inModuleDirectory, "moduleDirectory");

    this.errors =
      new ArrayList<>();
    this.binders =
      new CBBinderFactory();
    this.parsers =
      new CBParserFactory();
    this.typeCheckers =
      new CBTypeCheckerFactory();
    this.sources =
      new CBExpressionSources();
    this.codeGen =
      new CBCGJavaFactory();
  }

  public static CBJavaCompilation compile(
    final CBFakeLoader loader,
    final Path outputDirectory,
    final Path moduleDirectory,
    final String name)
    throws Exception
  {
    final var compilation =
      new CBJavaCompilation(loader, outputDirectory, moduleDirectory);

    final var pack = compilation.check(name);
    assertEquals(0, compilation.errors.size());

    final var result =
      compilation.codeGen.createGenerator(
        new CBSPICodeGeneratorConfiguration(outputDirectory)
      ).execute(pack);

    compilation.compileJava(result.createdFiles());
    return compilation;
  }

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
          "-g",
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

      final var result =
        task.call();

      assertTrue(
        result.booleanValue(),
        "Compilation of all files must succeed"
      );
    }

    this.createModule();
    this.classLoader = this.loadClasses();
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

  public ClassLoader classLoader()
  {
    return this.classLoader;
  }
}
