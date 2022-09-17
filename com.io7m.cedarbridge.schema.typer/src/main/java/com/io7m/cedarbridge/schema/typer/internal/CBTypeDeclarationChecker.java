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

package com.io7m.cedarbridge.schema.typer.internal;

import com.io7m.cedarbridge.errors.CBExceptionTracker;
import com.io7m.cedarbridge.schema.ast.CBASTField;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.typer.api.CBTypeAssignment;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckFailedException;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;

/**
 * Type checking of type declarations.
 */

public final class CBTypeDeclarationChecker
  implements CBElementCheckerType<CBASTTypeDeclarationType>
{
  private static final Optional<UUID> SPEC_SECTION_RECORD_FIELD_KIND_0 =
    uuid("06c63d66-019b-420a-809c-98ed41c3cfb2");
  private static final Optional<UUID> SPEC_SECTION_VARIANT_FIELD_KIND_0 =
    uuid("9d1d4d2e-bee3-43eb-90db-0e75e40ce882");

  /**
   * Type checking of type declarations.
   */

  public CBTypeDeclarationChecker()
  {

  }

  private static void checkDeclaration(
    final CBTyperContextType context,
    final CBASTTypeDeclarationType decl)
    throws CBTypeCheckFailedException
  {
    if (decl instanceof CBASTTypeRecord) {
      checkDeclarationRecord(context, (CBASTTypeRecord) decl);
      return;
    }
    if (decl instanceof CBASTTypeVariant) {
      checkDeclarationVariant(context, (CBASTTypeVariant) decl);
      return;
    }
    throw new UnreachableCodeException();
  }

  private static void checkDeclarationRecord(
    final CBTyperContextType context,
    final CBASTTypeRecord decl)
    throws CBTypeCheckFailedException
  {
    checkFields(context, SPEC_SECTION_RECORD_FIELD_KIND_0, decl.fields());
  }

  private static void checkFields(
    final CBTyperContextType context,
    final Optional<UUID> specSection,
    final List<CBASTField> fields)
    throws CBTypeCheckFailedException
  {
    final var tracker = new CBExceptionTracker<CBTypeCheckFailedException>();
    for (final var field : fields) {
      try {
        new CBTypeExpressionChecker().check(context, field.type());

        final var typeAssignment =
          field.type().userData().get(CBTypeAssignment.class);

        if (typeAssignment.arity() != 0) {
          throw context.failed(
            specSection,
            field.type().lexical(),
            "errorTypeArgumentsIncorrect",
            String.format("%s", field.type()),
            Integer.valueOf(typeAssignment.arity()),
            Integer.valueOf(0)
          );
        }
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }
    tracker.throwIfNecessary();
  }

  private static void checkDeclarationVariant(
    final CBTyperContextType context,
    final CBASTTypeVariant decl)
    throws CBTypeCheckFailedException
  {
    final var tracker = new CBExceptionTracker<CBTypeCheckFailedException>();
    for (final var caseV : decl.cases()) {
      try {
        checkFields(context, SPEC_SECTION_VARIANT_FIELD_KIND_0, caseV.fields());
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }
    tracker.throwIfNecessary();
  }

  @Override
  public void check(
    final CBTyperContextType context,
    final CBASTTypeDeclarationType item)
    throws CBTypeCheckFailedException
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(item, "item");

    checkDeclaration(context, item);
  }
}
