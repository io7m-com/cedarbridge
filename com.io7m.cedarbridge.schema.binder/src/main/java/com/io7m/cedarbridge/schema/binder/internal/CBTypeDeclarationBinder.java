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

import com.io7m.cedarbridge.schema.ast.CBASTField;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeParameterName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariantCase;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.binder.internal.CBBinder.SPEC_SEMANTICS_RECORD;
import static com.io7m.cedarbridge.schema.binder.internal.CBBinder.SPEC_SEMANTICS_VARIANT;

/**
 * Binding analysis for type declarations.
 */

public final class CBTypeDeclarationBinder
  implements CBElementBinderType<CBASTTypeDeclarationType>
{
  /**
   * Binding analysis for type declarations.
   */

  public CBTypeDeclarationBinder()
  {

  }

  private static void bindDeclaration(
    final CBBinderContextType context,
    final CBASTTypeDeclarationType item)
    throws CBBindFailedException
  {
    if (item instanceof CBASTTypeVariant) {
      bindDeclarationVariant(context, (CBASTTypeVariant) item);
    } else if (item instanceof CBASTTypeRecord) {
      bindDeclarationRecord(context, (CBASTTypeRecord) item);
    } else {
      throw new UnreachableCodeException();
    }
  }

  private static void bindTypeParameters(
    final CBBinderContextType context,
    final Optional<UUID> specSection,
    final List<CBASTTypeParameterName> parameters)
    throws CBBindFailedException
  {
    var failed = false;
    for (final var parameter : parameters) {
      try {
        final var binding =
          context.bindTypeParameter(
            specSection,
            parameter.text(),
            parameter.lexical()
          );

        parameter.userData().put(CBBindingType.class, binding);
      } catch (final CBBindFailedException e) {
        failed = true;
      }
    }
    if (failed) {
      throw new CBBindFailedException();
    }
  }

  private static void bindDeclarationRecord(
    final CBBinderContextType context,
    final CBASTTypeRecord item)
    throws CBBindFailedException
  {
    bindTypeParameters(context, SPEC_SEMANTICS_RECORD, item.parameters());
    bindFields(context, item.fields());
  }

  private static void bindFields(
    final CBBinderContextType context,
    final List<CBASTField> fields)
    throws CBBindFailedException
  {
    var failed = false;
    for (final var field : fields) {
      try {
        final var name = field.name();
        final var binding =
          context.bindField(SPEC_SEMANTICS_RECORD, name.text(), name.lexical());
        name.userData().put(CBBindingType.class, binding);
      } catch (final CBBindFailedException e) {
        failed = true;
      }

      try {
        new CBTypeExpressionBinder().bind(context, field.type());
      } catch (final CBBindFailedException e) {
        failed = true;
      }
    }

    if (failed) {
      throw new CBBindFailedException();
    }
  }

  private static void bindDeclarationVariant(
    final CBBinderContextType context,
    final CBASTTypeVariant item)
    throws CBBindFailedException
  {
    bindTypeParameters(context, SPEC_SEMANTICS_VARIANT, item.parameters());
    bindVariantCases(context, item.cases());
  }

  private static void bindVariantCases(
    final CBBinderContextType context,
    final List<CBASTTypeVariantCase> cases)
    throws CBBindFailedException
  {
    var failed = false;
    for (final var caseV : cases) {
      try {
        final var name = caseV.name();
        final var binding =
          context.bindVariantCase(
            SPEC_SEMANTICS_VARIANT,
            name.text(),
            name.lexical()
          );
        caseV.userData().put(CBBindingType.class, binding);
      } catch (final CBBindFailedException e) {
        failed = true;
      }

      try {
        bindFields(context, caseV.fields());
      } catch (final CBBindFailedException e) {
        failed = true;
      }
    }

    if (failed) {
      throw new CBBindFailedException();
    }
  }

  @Override
  public void bind(
    final CBBinderContextType context,
    final CBASTTypeDeclarationType item)
    throws CBBindFailedException
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(item, "item");

    bindDeclaration(context, item);
  }
}
