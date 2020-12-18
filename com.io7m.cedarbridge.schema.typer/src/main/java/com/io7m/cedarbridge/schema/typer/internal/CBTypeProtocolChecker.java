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

import com.io7m.cedarbridge.errors.CBExceptionTracker;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolVersion;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeAssignment;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckFailedException;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.Objects;

public final class CBTypeProtocolChecker
  implements CBElementCheckerType<CBASTProtocolDeclaration>
{
  public CBTypeProtocolChecker()
  {

  }

  private static void checkDeclaration(
    final CBTyperContextType context,
    final CBASTProtocolDeclaration decl)
    throws CBTypeCheckFailedException
  {
    final var tracker = new CBExceptionTracker<CBTypeCheckFailedException>();
    for (final var version : decl.versions()) {
      try {
        checkVersion(context, version);
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }
    tracker.throwIfNecessary();
  }

  private static void checkVersion(
    final CBTyperContextType context,
    final CBASTProtocolVersion version)
    throws CBTypeCheckFailedException
  {
    final var tracker = new CBExceptionTracker<CBTypeCheckFailedException>();
    for (final var type : version.types()) {
      final var binding =
        type.userData().get(CBBindingType.class);

      if (binding instanceof CBBindingLocalTypeDeclaration) {
        final var targetType =
          ((CBBindingLocalTypeDeclaration) binding).type();
        final var typeAssignment =
          targetType.userData().get(CBTypeAssignment.class);

        if (typeAssignment.arity() != 0) {
          throw context.failed(
            type.lexical(),
            "errorTypeProtocolKind0",
            targetType.name().text(),
            Integer.valueOf(typeAssignment.arity())
          );
        }
      } else {
        throw new UnreachableCodeException();
      }
    }
    tracker.throwIfNecessary();
  }

  @Override
  public void check(
    final CBTyperContextType context,
    final CBASTProtocolDeclaration item)
    throws CBTypeCheckFailedException
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(item, "item");

    checkDeclaration(context, item);
  }
}
