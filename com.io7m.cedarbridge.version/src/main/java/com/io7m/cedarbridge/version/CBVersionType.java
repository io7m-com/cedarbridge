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

package com.io7m.cedarbridge.version;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;
import java.util.Formattable;
import java.util.Formatter;

/**
 * A semantic version number.
 */

@ImmutablesStyleType
@Value.Immutable
public interface CBVersionType extends Comparable<CBVersionType>, Formattable
{
  /**
   * @return The major version
   */

  int major();

  /**
   * @return The minor version
   */

  int minor();

  /**
   * @return The patch version
   */

  int patch();

  /**
   * @return A version qualifier
   */

  String qualifier();

  @Override
  default int compareTo(
    final CBVersionType other)
  {
    return Comparator.comparingInt(CBVersionType::major)
      .thenComparingInt(CBVersionType::minor)
      .thenComparingInt(CBVersionType::patch)
      .thenComparing(CBVersionType::qualifier)
      .compare(this, other);
  }

  @Override
  default void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    try {
      final var out = formatter.out();
      out.append(Integer.toUnsignedString(this.major()));
      out.append('.');
      out.append(Integer.toUnsignedString(this.minor()));
      out.append('.');
      out.append(Integer.toUnsignedString(this.patch()));

      if (!this.qualifier().isEmpty()) {
        out.append('-');
        out.append(this.qualifier());
      }
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
