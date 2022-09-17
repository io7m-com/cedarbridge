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

package com.io7m.cedarbridge.schema.compiled;

import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type of type expressions.
 */

public sealed interface CBTypeExpressionType extends Formattable
{
  /**
   * Determine if the given type parameter occurs in the type expression.
   *
   * @param parameter The type parameter
   *
   * @return {@code true} if {@code parameter} occurs in this expression
   */

  boolean contains(CBTypeParameterType parameter);

  /**
   * A type parameter expression.
   */

  non-sealed interface CBTypeExprParameterType extends CBTypeExpressionType
  {
    /**
     * @return The type parameter
     */

    CBTypeParameterType parameter();

    @Override
    default boolean contains(
      final CBTypeParameterType parameter)
    {
      return Objects.equals(parameter, this.parameter());
    }

    @Override
    default void formatTo(
      final Formatter formatter,
      final int flags,
      final int width,
      final int precision)
    {
      formatter.format("%s", this.parameter().name());
    }
  }

  /**
   * A named type.
   */

  non-sealed interface CBTypeExprNamedType extends CBTypeExpressionType
  {
    /**
     * @return The target type declaration
     */

    CBTypeDeclarationType declaration();

    @Override
    default boolean contains(
      final CBTypeParameterType parameter)
    {
      return false;
    }

    @Override
    default void formatTo(
      final Formatter formatter,
      final int flags,
      final int width,
      final int precision)
    {
      final var decl = this.declaration();
      formatter.format("%s:%s", decl.owner().name(), decl.name());
    }
  }

  /**
   * A type application.
   */

  non-sealed interface CBTypeExprApplicationType extends CBTypeExpressionType
  {
    /**
     * @return The target type
     */

    CBTypeExprNamedType target();

    /**
     * @return The type expression arguments
     */

    List<CBTypeExpressionType> arguments();

    @Override
    default boolean contains(
      final CBTypeParameterType parameter)
    {
      return this.arguments()
        .stream()
        .anyMatch(p -> p.contains(parameter));
    }

    @Override
    default void formatTo(
      final Formatter formatter,
      final int flags,
      final int width,
      final int precision)
    {
      formatter.format(
        "(%s %s)",
        this.target(),
        this.arguments()
          .stream()
          .map(o -> String.format("%s", o))
          .collect(Collectors.joining(" "))
      );
    }
  }
}
