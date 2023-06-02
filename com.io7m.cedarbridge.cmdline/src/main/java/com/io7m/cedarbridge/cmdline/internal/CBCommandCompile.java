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

package com.io7m.cedarbridge.cmdline.internal;

import com.io7m.cedarbridge.codegen.api.CBCodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.api.CBCodeGenerators;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilation;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerException;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerFactoryType;
import com.io7m.cedarbridge.schema.core_types.CBCore;
import com.io7m.cedarbridge.schema.time.CBTime;
import com.io7m.quarrel.core.QCommandContextType;
import com.io7m.quarrel.core.QCommandMetadata;
import com.io7m.quarrel.core.QCommandStatus;
import com.io7m.quarrel.core.QCommandType;
import com.io7m.quarrel.core.QParameterNamed0N;
import com.io7m.quarrel.core.QParameterNamed1;
import com.io7m.quarrel.core.QParameterNamedType;
import com.io7m.quarrel.core.QParametersPositionalNone;
import com.io7m.quarrel.core.QParametersPositionalType;
import com.io7m.quarrel.core.QStringType.QConstant;
import com.io7m.quarrel.ext.logback.QLogback;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The "compile" command.
 */

public final class CBCommandCompile implements QCommandType
{
  private static final QParameterNamed0N<Path> FILES =
    new QParameterNamed0N<>(
      "--file",
      List.of(),
      new QConstant("The file(s) to type-check."),
      List.of(),
      Path.class
    );

  private static final QParameterNamed0N<Path> INCLUDES =
    new QParameterNamed0N<>(
      "--include",
      List.of(),
      new QConstant("The directories containing source files."),
      List.of(),
      Path.class
    );

  private static final QParameterNamed1<Boolean> NO_CORE =
    new QParameterNamed1<>(
      "--no-core",
      List.of(),
      new QConstant(
        "Disable registration of the core com.io7m.cedarbridge packages."),
      Optional.of(Boolean.FALSE),
      Boolean.class
    );

  private static final QParameterNamed1<Path> OUTPUT_DIRECTORY =
    new QParameterNamed1<>(
      "--output-directory",
      List.of(),
      new QConstant("The output directory containing generated files."),
      Optional.empty(),
      Path.class
    );

  private static final QParameterNamed1<String> LANGUAGE =
    new QParameterNamed1<>(
      "--language",
      List.of(),
      new QConstant("The language name used to select a code generator."),
      Optional.empty(),
      String.class
    );

  /**
   * Construct a command.
   */

  public CBCommandCompile()
  {

  }

  @Override
  public List<QParameterNamedType<?>> onListNamedParameters()
  {
    return Stream.concat(
      Stream.of(FILES, INCLUDES, NO_CORE, OUTPUT_DIRECTORY, LANGUAGE),
      QLogback.parameters().stream()
    ).toList();
  }

  @Override
  public QParametersPositionalType onListPositionalParameters()
  {
    return new QParametersPositionalNone();
  }

  @Override
  public QCommandStatus onExecute(
    final QCommandContextType context)
    throws Exception
  {
    QLogback.configure(context);

    final var codeGenerators =
      new CBCodeGenerators();
    final var compilers =
      CBServices.findService(CBSchemaCompilerFactoryType.class);

    final var languageName =
      context.parameterValue(LANGUAGE);

    final var codeGeneratorFactory =
      codeGenerators.findByLanguageName(languageName)
        .orElseThrow(() -> new IllegalArgumentException(String.format(
          "No code generator available for the language '%s'",
          languageName)
        ));

    final var compileFiles =
      context.parameterValues(FILES)
        .stream()
        .map(Path::toAbsolutePath)
        .collect(Collectors.toList());

    final var includeDirectories =
      context.parameterValues(INCLUDES)
        .stream()
        .map(Path::toAbsolutePath)
        .collect(Collectors.toList());

    final var configuration =
      new CBSchemaCompilerConfiguration(
        includeDirectories,
        compileFiles
      );

    final var compiler =
      compilers.createCompiler(configuration);

    if (!context.<Boolean>parameterValue(NO_CORE).booleanValue()) {
      final var loader = compiler.loader();
      loader.register(CBCore.get());
      loader.register(CBTime.get());
    }

    final CBSchemaCompilation compilation;
    try {
      compilation = compiler.execute();
    } catch (final CBSchemaCompilerException e) {
      return QCommandStatus.FAILURE;
    }

    final var codeGeneratorConfiguration =
      new CBCodeGeneratorConfiguration(context.parameterValue(OUTPUT_DIRECTORY));

    final var codeGenerator =
      codeGeneratorFactory.createGenerator(codeGeneratorConfiguration);

    for (final var packV : compilation.compiledPackages()) {
      codeGenerator.execute(packV);
    }

    return QCommandStatus.SUCCESS;
  }

  @Override
  public QCommandMetadata metadata()
  {
    return new QCommandMetadata(
      "compile",
      new QConstant("Compile a schema file and generate code."),
      Optional.empty()
    );
  }
}
