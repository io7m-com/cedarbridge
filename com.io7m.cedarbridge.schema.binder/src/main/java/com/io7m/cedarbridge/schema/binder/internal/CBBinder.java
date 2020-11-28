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

package com.io7m.cedarbridge.schema.binder.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionLineLogType;
import com.io7m.cedarbridge.schema.ast.CBASTImport;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBinderType;
import com.io7m.cedarbridge.schema.loader.api.CBLoaderType;
import com.io7m.cedarbridge.strings.api.CBStringsType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class CBBinder implements CBBinderType
{
  private final CBLoaderType loader;
  private final CBExpressionLineLogType lineLog;
  private final CBASTPackage parsedPackage;
  private final CBStringsType strings;
  private final Consumer<CBError> errors;

  public CBBinder(
    final CBStringsType inStrings,
    final CBLoaderType inLoader,
    final Consumer<CBError> inErrors,
    final CBExpressionLineLogType inLineLog,
    final CBASTPackage inParsedPackage)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.loader =
      Objects.requireNonNull(inLoader, "loader");
    this.errors =
      Objects.requireNonNull(inErrors, "errors");
    this.lineLog =
      Objects.requireNonNull(inLineLog, "inLineLog");
    this.parsedPackage =
      Objects.requireNonNull(inParsedPackage, "parsedPackage");
  }

  @Override
  public CBASTPackage execute()
    throws CBBindFailedException
  {
    final var context =
      new CBBinderContext(this.strings, this.loader, this.lineLog, this.errors);
    final var contextMain =
      context.current();

    final var imports =
      this.processImports(contextMain);

    throw new UnsupportedOperationException();
  }

  private List<CBASTImport> processImports(
    final CBBinderContextType context)
    throws CBBindFailedException
  {
    var failed = false;

    final var newImports =
      new ArrayList<CBASTImport>(this.parsedPackage.imports().size());
    for (final var importV : this.parsedPackage.imports()) {
      try {
        newImports.add(new CBImportBinder().bind(context, importV));
      } catch (final CBBindFailedException e) {
        failed = true;
      }
    }

    if (failed) {
      throw new CBBindFailedException();
    }

    return List.copyOf(newImports);
  }

  @Override
  public void close()
  {

  }
}
