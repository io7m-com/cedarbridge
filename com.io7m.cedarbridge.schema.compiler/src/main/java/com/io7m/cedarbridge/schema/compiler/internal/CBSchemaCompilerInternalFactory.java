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

package com.io7m.cedarbridge.schema.compiler.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceFactoryType;
import com.io7m.cedarbridge.schema.binder.api.CBBinderFactoryType;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerType;
import com.io7m.cedarbridge.schema.parser.api.CBParserFactoryType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckerFactoryType;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * An internal compiler factory.
 */

public final class CBSchemaCompilerInternalFactory
{
  private final CBParserFactoryType parsers;
  private final CBExpressionSourceFactoryType sources;
  private final CBTypeCheckerFactoryType typers;
  private final CBBinderFactoryType binders;

  /**
   * An internal compiler factory.
   *
   * @param inBinders A source of binders
   * @param inParsers A source of parsers
   * @param inSources A source of expression sources
   * @param inTypers  A source of type checkers
   */

  public CBSchemaCompilerInternalFactory(
    final CBExpressionSourceFactoryType inSources,
    final CBParserFactoryType inParsers,
    final CBBinderFactoryType inBinders,
    final CBTypeCheckerFactoryType inTypers)
  {
    this.parsers =
      Objects.requireNonNull(inParsers, "inParsers");
    this.sources =
      Objects.requireNonNull(inSources, "inSources");
    this.typers =
      Objects.requireNonNull(inTypers, "inTypers");
    this.binders =
      Objects.requireNonNull(inBinders, "inBinders");
  }

  /**
   * Create a new compiler.
   *
   * @param errorConsumer The error consumer
   * @param configuration The compiler configuration
   * @param loader        The package loader
   *
   * @return A new compiler
   */

  public CBSchemaCompilerType createNewCompiler(
    final Consumer<CBError> errorConsumer,
    final CBSchemaCompilerConfiguration configuration,
    final CBLoader loader)
  {
    return new CBSchemaCompiler(
      errorConsumer,
      this.sources,
      this.parsers,
      this.binders,
      this.typers,
      loader,
      configuration
    );
  }
}
