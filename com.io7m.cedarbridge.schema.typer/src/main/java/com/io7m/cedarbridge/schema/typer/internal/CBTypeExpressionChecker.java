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
import com.io7m.cedarbridge.schema.ast.CBASTTypeApplication;
import com.io7m.cedarbridge.schema.ast.CBASTTypeExpressionType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeNamed;
import com.io7m.cedarbridge.schema.binder.api.CBBindingExternal;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeAssignment;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckFailedException;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType;
import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType.CBBindingLocalTypeDeclarationType;
import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType.CBBindingLocalTypeParameterType;
import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;

/**
 * Type checking of type expressions.
 */

public final class CBTypeExpressionChecker
  implements CBElementCheckerType<CBASTTypeExpressionType>
{
  private static final Optional<UUID> SPEC_SECTION_APPLICATION =
    uuid("2168e18e-88d5-4434-9a9e-07f489c836c7");

  /**
   * Type checking of type expressions.
   */

  public CBTypeExpressionChecker()
  {

  }

  private static void checkExpression(
    final CBTyperContextType context,
    final CBASTTypeExpressionType expression)
    throws CBTypeCheckFailedException
  {
    if (expression instanceof CBASTTypeApplication) {
      checkExpressionApplication(context, (CBASTTypeApplication) expression);
      return;
    }
    if (expression instanceof CBASTTypeNamed) {
      checkExpressionNamed(context, (CBASTTypeNamed) expression);
      return;
    }
    throw new UnreachableCodeException();
  }

  private static void checkExpressionNamed(
    final CBTyperContextType context,
    final CBASTTypeNamed expression)
  {
    final var binding =
      expression.userData().get(CBBindingType.class);

    if (binding instanceof CBBindingLocalType) {
      checkExpressionNamedLocal(
        context,
        expression,
        (CBBindingLocalType) binding);
      return;
    }
    if (binding instanceof CBBindingExternal) {
      checkExpressionNamedExternal(
        context,
        expression,
        (CBBindingExternal) binding);
      return;
    }
    throw new UnreachableCodeException();
  }

  private static void checkExpressionNamedExternal(
    final CBTyperContextType context,
    final CBASTTypeNamed expression,
    final CBBindingExternal binding)
  {
    final var targetType = binding.type();
    expression.userData()
      .put(
        CBTypeAssignment.class,
        CBTypeAssignment.builder()
          .setArity(targetType.arity())
          .build()
      );
  }

  private static void checkExpressionNamedLocal(
    final CBTyperContextType context,
    final CBASTTypeNamed expression,
    final CBBindingLocalType binding)
  {
    if (binding instanceof CBBindingLocalTypeDeclarationType) {
      final var parameters =
        ((CBBindingLocalTypeDeclarationType) binding).type().parameters();
      expression.userData()
        .put(
          CBTypeAssignment.class,
          CBTypeAssignment.builder()
            .setArity(parameters.size())
            .build()
        );
      return;
    }

    if (binding instanceof CBBindingLocalTypeParameterType) {
      expression.userData()
        .put(
          CBTypeAssignment.class,
          CBTypeAssignment.builder()
            .setArity(0)
            .build()
        );
      return;
    }

    throw new UnreachableCodeException();
  }

  private static void checkExpressionApplication(
    final CBTyperContextType context,
    final CBASTTypeApplication expression)
    throws CBTypeCheckFailedException
  {
    final var targetExpression = expression.target();
    checkExpression(context, targetExpression);

    if (expression.arguments().isEmpty()) {
      throw context.failed(
        SPEC_SECTION_APPLICATION,
        expression.lexical(),
        "errorTypeApplicationEmpty"
      );
    }

    final var targetArity =
      targetExpression.userData().get(CBTypeAssignment.class);

    final var tracker = new CBExceptionTracker<CBTypeCheckFailedException>();
    final var arguments = expression.arguments();
    for (final var subExpression : arguments) {
      try {
        checkExpression(context, subExpression);

        final var subExpressionAssignment =
          subExpression.userData().get(CBTypeAssignment.class);

        if (subExpressionAssignment.arity() != 0) {
          throw context.failed(
            SPEC_SECTION_APPLICATION,
            subExpression.lexical(),
            "errorTypeArgumentsIncorrect",
            String.format("%s", subExpression),
            Integer.valueOf(subExpressionAssignment.arity()),
            Integer.valueOf(0)
          );
        }
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }
    tracker.throwIfNecessary();

    final int argumentCount = arguments.size();
    if (targetArity.arity() < argumentCount) {
      throw context.failed(
        SPEC_SECTION_APPLICATION,
        targetExpression.lexical(),
        "errorTypeArgumentsIncorrect",
        String.format("%s", targetExpression),
        Integer.valueOf(targetArity.arity()),
        Integer.valueOf(argumentCount)
      );
    }

    final var remaining = targetArity.arity() - argumentCount;
    expression.userData()
      .put(
        CBTypeAssignment.class,
        CBTypeAssignment.builder()
          .setArity(remaining)
          .build()
      );
  }

  @Override
  public void check(
    final CBTyperContextType context,
    final CBASTTypeExpressionType item)
    throws CBTypeCheckFailedException
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(item, "item");

    checkExpression(context, item);
  }
}
