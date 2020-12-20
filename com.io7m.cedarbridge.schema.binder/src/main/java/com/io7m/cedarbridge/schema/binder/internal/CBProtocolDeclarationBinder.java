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

import com.io7m.cedarbridge.errors.CBExceptionTracker;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolVersion;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;

import java.math.BigInteger;
import java.util.Objects;
import java.util.TreeSet;

public final class CBProtocolDeclarationBinder
  implements CBElementBinderType<CBASTProtocolDeclaration>
{
  public CBProtocolDeclarationBinder()
  {

  }

  private static void bindDeclaration(
    final CBBinderContextType context,
    final CBASTProtocolDeclaration item)
    throws CBBindFailedException
  {
    final var exceptions = new CBExceptionTracker<CBBindFailedException>();

    try (var subContext = context.openBindingScope()) {
      final var numbers = new TreeSet<BigInteger>();
      for (final var version : item.versions()) {
        try {
          bindVersionDeclaration(subContext, version);
        } catch (final CBBindFailedException e) {
          exceptions.addException(e);
        }
        numbers.add(version.version());
      }

      final var min =
        numbers.stream().min(BigInteger::compareTo).orElseThrow();
      final var max =
        numbers.stream().max(BigInteger::compareTo).orElseThrow();

      for (var current = min;
           current.compareTo(max) <= 0;
           current = current.add(BigInteger.ONE)) {
        if (!numbers.contains(current)) {
          exceptions.addException(
            context.failed(
              item.lexical(),
              "errorProtocolVersionMissing",
              min,
              max,
              current
            ));
          break;
        }
      }

      exceptions.throwIfNecessary();
    }
  }

  private static void bindVersionDeclaration(
    final CBBinderContextType context,
    final CBASTProtocolVersion version)
    throws CBBindFailedException
  {
    version.userData()
      .put(
        CBBindingType.class,
        context.bindProtocolVersion(version.version(), version.lexical()));

    for (final var type : version.types()) {
      final var binding =
        context.checkTypeBinding(type.text(), type.lexical());
      type.userData().put(CBBindingType.class, binding);
    }
  }

  @Override
  public void bind(
    final CBBinderContextType context,
    final CBASTProtocolDeclaration item)
    throws CBBindFailedException
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(item, "item");

    bindDeclaration(context, item);
  }
}
