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

package com.io7m.cedarbridge.schema.typer.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.errors.CBExceptionTracker;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionLineLogType;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeAssignment;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckFailedException;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckerType;
import com.io7m.cedarbridge.strings.api.CBStringsType;

import java.util.Objects;
import java.util.function.Consumer;

public final class CBTypeChecker implements CBTypeCheckerType
{
  private final Consumer<CBError> errors;
  private final CBExpressionLineLogType lineLog;
  private final CBASTPackage packageV;
  private final CBStringsType strings;

  public CBTypeChecker(
    final Consumer<CBError> inErrors,
    final CBExpressionLineLogType inLineLog,
    final CBASTPackage inPackage,
    final CBStringsType inStrings)
  {
    this.errors =
      Objects.requireNonNull(inErrors, "errors");
    this.lineLog =
      Objects.requireNonNull(inLineLog, "lineLog");
    this.packageV =
      Objects.requireNonNull(inPackage, "parsedPackage");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");
  }

  private static void processTypeDeclaration(
    final CBASTTypeDeclarationType decl)
  {
    final var assignment =
      CBTypeAssignment.builder()
        .setArity(decl.parameters().size())
        .build();

    decl.userData().put(CBTypeAssignment.class, assignment);
  }

  @Override
  public void execute()
    throws CBTypeCheckFailedException
  {
    final var context =
      new CBTyperContext(this.strings, this.lineLog, this.errors);
    final var current =
      context.current();

    this.processTopLevelDeclarations();
    this.processDeclarations(current);

    this.packageV.userData()
      .put(
        CBPackageType.class,
        new CBTypePackageConverter().build(this.packageV));
  }

  private void processDeclarations(
    final CBTyperContextType current)
    throws CBTypeCheckFailedException
  {
    final var tracker = new CBExceptionTracker<CBTypeCheckFailedException>();
    for (final var type : this.packageV.types()) {
      try {
        new CBTypeDeclarationChecker().check(current, type);
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }
    for (final var proto : this.packageV.protocols()) {
      try {
        new CBTypeProtocolChecker().check(current, proto);
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }
    tracker.throwIfNecessary();
  }

  private void processTopLevelDeclarations()
  {
    for (final var type : this.packageV.types()) {
      processTypeDeclaration(type);
    }
  }

  @Override
  public void close()
  {

  }
}
