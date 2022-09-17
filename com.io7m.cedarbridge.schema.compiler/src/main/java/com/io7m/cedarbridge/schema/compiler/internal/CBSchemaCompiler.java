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

package com.io7m.cedarbridge.schema.compiler.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.errors.CBExceptionTracker;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceFactoryType;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBinderFactoryType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilation;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerException;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerType;
import com.io7m.cedarbridge.schema.loader.api.CBLoaderType;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.api.CBParserFactoryType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckFailedException;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckerFactoryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A schema compiler.
 */

public final class CBSchemaCompiler implements CBSchemaCompilerType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBSchemaCompiler.class);

  private final CBBinderFactoryType binders;
  private final CBExpressionSourceFactoryType sources;
  private final CBLoaderType loader;
  private final CBParserFactoryType parsers;
  private final CBSchemaCompilerConfiguration configuration;
  private final CBTypeCheckerFactoryType typers;
  private final Consumer<CBError> errorConsumer;

  /**
   * A schema compiler.
   *
   * @param inConfiguration The compiler configuration
   * @param inErrorConsumer An error consumer
   * @param inLoader        The package loader
   * @param inBinders       A source of binders
   * @param inParsers       A source of parsers
   * @param inSources       A source of expression sources
   * @param inTypers        A source of type checkers
   */

  public CBSchemaCompiler(
    final Consumer<CBError> inErrorConsumer,
    final CBExpressionSourceFactoryType inSources,
    final CBParserFactoryType inParsers,
    final CBBinderFactoryType inBinders,
    final CBTypeCheckerFactoryType inTypers,
    final CBLoaderType inLoader,
    final CBSchemaCompilerConfiguration inConfiguration)
  {
    this.errorConsumer =
      Objects.requireNonNull(inErrorConsumer, "errorConsumer");
    this.sources =
      Objects.requireNonNull(inSources, "sources");
    this.parsers =
      Objects.requireNonNull(inParsers, "parsers");
    this.binders =
      Objects.requireNonNull(inBinders, "binders");
    this.typers =
      Objects.requireNonNull(inTypers, "typers");
    this.loader =
      Objects.requireNonNull(inLoader, "loader");
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public CBLoaderType loader()
  {
    return this.loader;
  }

  @Override
  public CBSchemaCompilation execute()
    throws CBSchemaCompilerException
  {
    final var paths =
      this.configuration.filesToCompile();
    final var exceptions =
      new CBExceptionTracker<CBSchemaCompilerException>();

    final var packages = new ArrayList<CBPackageType>();
    for (final var path : paths) {
      try {
        packages.add(this.compileOne(path));
      } catch (final CBSchemaCompilerException e) {
        exceptions.addException(e);
      }
    }

    exceptions.throwIfNecessary();
    return new CBSchemaCompilation(packages);
  }

  private CBPackageType compileOne(
    final Path path)
    throws CBSchemaCompilerException
  {
    LOG.debug("compile: {}", path);

    try (var source = this.sources.create(path)) {
      try (var parser = this.parsers.createParser(
        this.errorConsumer,
        source)) {
        final var pack = parser.execute();
        try (var binder =
               this.binders.createBinder(
                 this.loader, this.errorConsumer, source, pack)) {
          binder.execute();
          try (var typeChecker =
                 this.typers.createTypeChecker(
                   this.errorConsumer,
                   source,
                   pack)) {
            typeChecker.execute();
            return pack.userData().get(CBPackageType.class);
          }
        }
      } catch (final CBBindFailedException
        | CBParseFailedException
        | CBTypeCheckFailedException e) {
        throw new CBSchemaCompilerException();
      }
    } catch (final IOException exception) {
      LOG.error("{}: ", path, exception);
      throw new CBSchemaCompilerException();
    }
  }
}

