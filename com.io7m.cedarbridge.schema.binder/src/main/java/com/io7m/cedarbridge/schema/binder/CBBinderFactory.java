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

package com.io7m.cedarbridge.schema.binder;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionLineLogType;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.binder.api.CBBinderFactoryType;
import com.io7m.cedarbridge.schema.binder.api.CBBinderType;
import com.io7m.cedarbridge.schema.binder.internal.CBBinder;
import com.io7m.cedarbridge.schema.binder.internal.CBBinderStrings;
import com.io7m.cedarbridge.schema.loader.api.CBLoaderType;
import com.io7m.cedarbridge.strings.api.CBStringsType;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The binder factory implementation.
 */

public final class CBBinderFactory implements CBBinderFactoryType
{
  private final CBStringsType strings;

  /**
   * Construct a binder factory.
   *
   * @param inStrings String resources
   */

  public CBBinderFactory(
    final CBStringsType inStrings)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
  }

  /**
   * Construct a binder factory
   */

  public CBBinderFactory()
  {
    this(CBBinderStrings.create());
  }

  @Override
  public CBBinderType createBinder(
    final CBLoaderType loader,
    final Consumer<CBError> errors,
    final CBExpressionLineLogType lineLog,
    final CBASTPackage parsedPackage)
  {
    Objects.requireNonNull(loader, "loader");
    Objects.requireNonNull(errors, "errors");
    Objects.requireNonNull(lineLog, "lineLog");
    Objects.requireNonNull(parsedPackage, "parsedPackage");

    return new CBBinder(this.strings, loader, errors, lineLog, parsedPackage);
  }
}
