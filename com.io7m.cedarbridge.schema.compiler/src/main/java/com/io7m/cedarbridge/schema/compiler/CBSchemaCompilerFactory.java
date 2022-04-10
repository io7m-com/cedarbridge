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

package com.io7m.cedarbridge.schema.compiler;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceFactoryType;
import com.io7m.cedarbridge.schema.binder.api.CBBinderFactoryType;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerFactoryType;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerType;
import com.io7m.cedarbridge.schema.compiler.internal.CBLoader;
import com.io7m.cedarbridge.schema.compiler.internal.CBSchemaCompiler;
import com.io7m.cedarbridge.schema.compiler.internal.CBSchemaCompilerInternalFactory;
import com.io7m.cedarbridge.schema.compiler.internal.CBServices;
import com.io7m.cedarbridge.schema.parser.api.CBParserFactoryType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckerFactoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A factory of schema compilers.
 */

public final class CBSchemaCompilerFactory
  implements CBSchemaCompilerFactoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBSchemaCompiler.class);

  private final CBSchemaCompilerInternalFactory factory;

  /**
   * A factory of schema compilers. Dependencies are loaded from ServiceLoader.
   */

  public CBSchemaCompilerFactory()
  {
    this(
      CBServices.findService(CBExpressionSourceFactoryType.class),
      CBServices.findService(CBParserFactoryType.class),
      CBServices.findService(CBBinderFactoryType.class),
      CBServices.findService(CBTypeCheckerFactoryType.class)
    );
  }

  /**
   * A factory of schema compilers.
   *
   * @param inBinders A source of binders
   * @param inParsers A source of parsers
   * @param inSources A source of expression sources
   * @param inTypers  A source of type checkers
   */

  public CBSchemaCompilerFactory(
    final CBExpressionSourceFactoryType inSources,
    final CBParserFactoryType inParsers,
    final CBBinderFactoryType inBinders,
    final CBTypeCheckerFactoryType inTypers)
  {
    this.factory =
      new CBSchemaCompilerInternalFactory(
        inSources,
        inParsers,
        inBinders,
        inTypers
      );
  }

  @Override
  public CBSchemaCompilerType createCompiler(
    final CBSchemaCompilerConfiguration configuration)
  {
    Objects.requireNonNull(configuration, "configuration");

    final Consumer<CBError> errorConsumer = error -> {
      switch (error.severity()) {
        case ERROR:
          LOG.error("{}", error.message());
          break;
        case WARNING:
          LOG.warn("{}", error.message());
          break;
      }
    };

    return this.factory.createNewCompiler(
      errorConsumer,
      configuration,
      new CBLoader(
        this.factory,
        configuration.includeDirectories(),
        errorConsumer)
    );
  }
}
