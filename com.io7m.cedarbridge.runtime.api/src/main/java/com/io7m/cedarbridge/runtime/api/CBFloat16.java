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
import java.util.Formatter;

/**
 * A 16-bit floating point value.
 *
 * @param value The value
 */

public record CBFloat16(double value)
  implements Comparable<CBFloat16>, CBFloatType
{
  @Override
  public int compareTo(
    final CBFloat16 other)
  {
    return Double.compare(this.value(), other.value());
  }

  @Override
  public void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    formatter.format("%f", Double.valueOf(this.value()));
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
    final CBFloat16 x)
    throws IOException
  {
    context.writeF16(x.value);
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
  public static CBFloat16 deserialize(
    final CBSerializationContextType context)
    throws IOException
  {
    return new CBFloat16(context.readF16());
  }
}
