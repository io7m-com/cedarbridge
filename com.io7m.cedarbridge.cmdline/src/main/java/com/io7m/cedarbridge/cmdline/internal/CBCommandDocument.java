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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.io7m.cedarbridge.bridgedoc.api.CBDocGeneratorConfiguration;
import com.io7m.cedarbridge.bridgedoc.api.CBDocGenerators;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilation;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerException;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerFactoryType;
import com.io7m.cedarbridge.schema.core_types.CBCore;
import com.io7m.claypot.core.CLPAbstractCommand;
import com.io7m.claypot.core.CLPCommandContextType;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.io7m.claypot.core.CLPCommandType.Status.FAILURE;
import static com.io7m.claypot.core.CLPCommandType.Status.SUCCESS;

/**
 * The "document" command.
 */

@Parameters(commandDescription = "Compile a schema file and generate documentation.")
public final class CBCommandDocument extends CLPAbstractCommand
{
  @Parameter(
    names = "--file",
    description = "The file(s) to type-check"
  )
  private List<Path> files = List.of();

  @Parameter(
    names = "--include",
    description = "The directories containing source files"
  )
  private List<Path> includes = List.of();

  @Parameter(
    names = "--output-directory",
    required = true,
    description = "The output directory containing generated files"
  )
  private Path output;

  @Parameter(
    names = "--no-core",
    arity = 1,
    description = "Disable registration of the core com.io7m.cedarbridge package"
  )
  private boolean noCore;

  @Parameter(
    names = "--language",
    required = true,
    description = "The language name used to select a documentation generator"
  )
  private String languageName;

  @Parameter(
    names = "--custom-style",
    required = false,
    description = "The name of the custom style used for documentation"
  )
  private String customStyle;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CBCommandDocument(
    final CLPCommandContextType inContext)
  {
    super(inContext);
  }

  @Override
  protected Status executeActual()
    throws Exception
  {
    final var docGenerators =
      new CBDocGenerators();
    final var compilers =
      CBServices.findService(CBSchemaCompilerFactoryType.class);

    final var docGeneratorFactory =
      docGenerators.findByLanguageName(this.languageName)
        .orElseThrow(() -> new IllegalArgumentException(String.format(
          "No documentation generator available for the language '%s'",
          this.languageName)
        ));

    final var compileFiles =
      this.files.stream()
        .map(Path::toAbsolutePath)
        .collect(Collectors.toList());

    final var includeDirectories =
      this.includes.stream()
        .map(Path::toAbsolutePath)
        .collect(Collectors.toList());

    final var configuration =
      new CBSchemaCompilerConfiguration(
        includeDirectories,
        compileFiles
      );

    final var compiler =
      compilers.createCompiler(configuration);

    if (!this.noCore) {
      compiler.loader().register(CBCore.get());
    }

    final CBSchemaCompilation compilation;
    try {
      compilation = compiler.execute();
    } catch (final CBSchemaCompilerException e) {
      return FAILURE;
    }

    final var docGeneratorConfiguration =
      new CBDocGeneratorConfiguration(
        this.output,
        Optional.ofNullable(this.customStyle)
      );

    final var docGenerator =
      docGeneratorFactory.createGenerator(docGeneratorConfiguration);

    for (final var packV : compilation.compiledPackages()) {
      docGenerator.execute(packV);
    }

    return SUCCESS;
  }

  @Override
  public String name()
  {
    return "document";
  }
}
