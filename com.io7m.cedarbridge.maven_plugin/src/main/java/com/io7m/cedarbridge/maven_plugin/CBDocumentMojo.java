/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.cedarbridge.maven_plugin;

import com.io7m.cedarbridge.bridgedoc.api.CBDocGeneratorConfiguration;
import com.io7m.cedarbridge.bridgedoc.api.CBDocGeneratorException;
import com.io7m.cedarbridge.bridgedoc.api.CBDocGenerators;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerException;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerFactoryType;
import com.io7m.cedarbridge.schema.compiler.internal.CBServices;
import com.io7m.cedarbridge.schema.core_types.CBCore;
import com.io7m.jaffirm.core.Postconditions;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_SOURCES;

/**
 * The "document" mojo.
 */

@Mojo(name = "document", defaultPhase = GENERATE_SOURCES)
public final class CBDocumentMojo extends AbstractMojo
{
  @Parameter(
    name = "files"
  )
  private List<String> files = List.of();

  @Parameter(
    name = "includes"
  )
  private List<String> includes = List.of();

  @Parameter(
    name = "outputDirectory",
    required = true
  )
  private String outputDirectory;

  @Parameter(
    name = "noCore"
  )
  private boolean noCore;

  @Parameter(
    name = "languageName",
    required = true
  )
  private String languageName;

  @Parameter(
    name = "customStyle",
    required = false
  )
  private String customStyle;

  @Parameter(
    required = false,
    name = "skip",
    property = "cedarbridge.skip",
    defaultValue = "false"
  )
  private boolean skip;

  /**
   * The "document" mojo.
   */

  public CBDocumentMojo()
  {

  }

  @Override
  public void execute()
    throws MojoExecutionException
  {
    if (this.skip) {
      return;
    }

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
        .map(Path::of)
        .map(Path::toAbsolutePath)
        .collect(Collectors.toList());

    final var includeDirectories =
      this.includes.stream()
        .map(Path::of)
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

    try {
      final var compilation =
        compiler.execute();

      final var outputPath =
        Path.of(this.outputDirectory).toAbsolutePath();

      Files.createDirectories(outputPath);

      final var docGeneratorConfiguration =
        new CBDocGeneratorConfiguration(
          outputPath,
          Optional.ofNullable(this.customStyle)
        );

      final var docGenerator =
        docGeneratorFactory.createGenerator(docGeneratorConfiguration);

      final var created = new HashSet<Path>();
      for (final var packV : compilation.compiledPackages()) {
        final var result = docGenerator.execute(packV);
        created.addAll(result.createdFiles());
      }

      created.forEach(path -> {
        this.getLog().info("create %s".formatted(path));
      });
      Postconditions.checkPostconditionV(
        !created.isEmpty(),
        "Must have created at least one file."
      );
    } catch (final CBSchemaCompilerException
                   | CBDocGeneratorException
                   | IOException e) {
      throw new MojoExecutionException(e);
    }
  }
}
