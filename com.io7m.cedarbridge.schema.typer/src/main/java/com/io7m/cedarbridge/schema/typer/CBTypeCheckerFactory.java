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

package com.io7m.cedarbridge.schema.typer;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionLineLogType;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckerFactoryType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckerType;
import com.io7m.cedarbridge.schema.typer.internal.CBTypeChecker;
import com.io7m.cedarbridge.schema.typer.internal.CBTyperStrings;
import com.io7m.cedarbridge.strings.api.CBStringsType;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A factory of type checkers.
 */

public final class CBTypeCheckerFactory implements CBTypeCheckerFactoryType
{
  private final CBStringsType strings;

  /**
   * A factory of type checkers.
   *
   * @param inStrings String resources
   */

  public CBTypeCheckerFactory(
    final CBStringsType inStrings)
  {
    this.strings = Objects.requireNonNull(inStrings, "strings");
  }

  /**
   * A factory of type checkers.
   */

  public CBTypeCheckerFactory()
  {
    this(CBTyperStrings.create());
  }

  @Override
  public CBTypeCheckerType createTypeChecker(
    final Consumer<CBError> errors,
    final CBExpressionLineLogType lineLog,
    final CBASTPackage parsedPackage)
  {
    Objects.requireNonNull(errors, "errors");
    Objects.requireNonNull(lineLog, "lineLog");
    Objects.requireNonNull(parsedPackage, "parsedPackage");

    return new CBTypeChecker(errors, lineLog, parsedPackage, this.strings);
  }
}
