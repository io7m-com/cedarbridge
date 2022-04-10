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

package com.io7m.cedarbridge.schema.ast;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The  type of type expressions.
 */

public interface CBASTTypeExpressionType extends CBASTElementType, Formattable
{
  /**
   * A named type.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBASTTypeNamedType extends CBASTTypeExpressionType
  {
    /**
     * @return The package name qualification, if any
     */

    Optional<CBASTPackageName> packageName();

    /**
     * @return The type name
     */

    CBASTTypeName name();

    @Override
    default void formatTo(
      final Formatter formatter,
      final int flags,
      final int width,
      final int precision)
    {
      try {
        final var out = formatter.out();
        final var pName = this.packageName();
        if (pName.isPresent()) {
          out.append(pName.get().text());
          out.append(':');
        }
        final var nameText = this.name().text();
        out.append(nameText);
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  /**
   * A type application.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBASTTypeApplicationType extends CBASTTypeExpressionType
  {
    /**
     * @return The target type
     */

    CBASTTypeNamedType target();

    /**
     * @return The type arguments
     */

    List<CBASTTypeExpressionType> arguments();

    @Override
    default void formatTo(
      final Formatter formatter,
      final int flags,
      final int width,
      final int precision)
    {
      try {
        final var out = formatter.out();
        out.append('[');
        this.target().formatTo(formatter, flags, width, precision);
        if (this.arguments().size() > 0) {
          out.append(' ');
          out.append(
            this.arguments()
              .stream()
              .map(x -> String.format("%s", x))
              .collect(Collectors.joining(" "))
          );
        }
        out.append(']');
      } catch (final IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }
}
