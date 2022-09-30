/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.cedarbridge.runtime.api;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Formatter;

/**
 * The type of unsigned 8-bit integers.
 *
 * @param value The value
 */

public record CBIntegerUnsigned8(int value)
  implements Comparable<CBIntegerUnsigned8>, CBIntegerType
{
  /**
   * The type of unsigned 8-bit integers.
   *
   * @param value The value
   */

  public CBIntegerUnsigned8
  {
    if (value < 0) {
      throw new IllegalArgumentException(
        String.format(
          "Value %d must be in the range [0, 255]",
          Integer.valueOf(value))
      );
    }

    if (value > 255) {
      throw new IllegalArgumentException(
        String.format(
          "Value %d must be in the range [0, 255]",
          Integer.valueOf(value))
      );
    }
  }

  @Override
  public int compareTo(
    final CBIntegerUnsigned8 other)
  {
    return Integer.compareUnsigned(this.value(), other.value());
  }

  @Override
  public void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    try {
      formatter.out().append(Integer.toUnsignedString(this.value()));
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  /**
   * Serialize the given value.
   *
   * @param context The serialization context
   * @param x       The value
   *
   * @throws IOException On errors
   */

  @CBSerializerMethod
  public static void serialize(
    final CBSerializationContextType context,
    final CBIntegerUnsigned8 x)
    throws IOException
  {
    context.writeU8(x.value);
  }

  /**
   * Deserialize the given value.
   *
   * @param context The serialization context
   *
   * @return The deserialized value
   *
   * @throws IOException On errors
   */

  @CBDeserializerMethod
  public static CBIntegerUnsigned8 deserialize(
    final CBSerializationContextType context)
    throws IOException
  {
    return new CBIntegerUnsigned8(context.readU8());
  }
}
