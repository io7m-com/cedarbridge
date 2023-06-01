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
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerException;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerFactoryType;
import com.io7m.cedarbridge.schema.core_types.CBCore;
import com.io7m.cedarbridge.schema.time.CBTime;
import com.io7m.claypot.core.CLPAbstractCommand;
import com.io7m.claypot.core.CLPCommandContextType;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.io7m.claypot.core.CLPCommandType.Status.FAILURE;
import static com.io7m.claypot.core.CLPCommandType.Status.SUCCESS;

/**
 * The "check" command.
 */

@Parameters(commandDescription = "Type-check a schema file.")
public final class CBCommandCheck extends CLPAbstractCommand
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
    names = "--no-core",
    arity = 1,
    description = "Disable registration of the core com.io7m.cedarbridge package"
  )
  private boolean noCore;

  /**
   * Construct a command.
   *
   * @param inContext The command context
   */

  public CBCommandCheck(
    final CLPCommandContextType inContext)
  {
    super(inContext);
  }

  @Override
  protected Status executeActual()
    throws Exception
  {
    final var compilers =
      CBServices.findService(CBSchemaCompilerFactoryType.class);

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
      final var loader = compiler.loader();
      loader.register(CBCore.get());
      loader.register(CBTime.get());
    }

    try {
      compiler.execute();
    } catch (final CBSchemaCompilerException e) {
      return FAILURE;
    }

    return SUCCESS;
  }

  @Override
  public String name()
  {
    return "check";
  }
}
