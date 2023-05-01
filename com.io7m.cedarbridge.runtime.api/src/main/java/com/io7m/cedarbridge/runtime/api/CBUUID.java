/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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
import java.util.Formattable;
import java.util.Formatter;
import java.util.Objects;
import java.util.UUID;

/**
 * The type of UUID values.
 *
 * @param value The UUID value
 */

public record CBUUID(UUID value)
  implements Comparable<CBUUID>, Formattable, CBSerializableType
{
  /**
   * The type of UUID values.
   *
   * @param value The UUID value
   */

  public CBUUID
  {
    Objects.requireNonNull(value, "value");
  }

  @Override
  public int compareTo(
    final CBUUID other)
  {
    return this.value().compareTo(other.value());
  }

  @Override
  public void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    try {
      formatter.out().append(this.value().toString());
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
    final CBUUID x)
    throws IOException
  {
    context.writeU64(x.value.getMostSignificantBits());
    context.writeU64(x.value.getLeastSignificantBits());
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
  public static CBUUID deserialize(
    final CBSerializationContextType context)
    throws IOException
  {
    final var msb = context.readU64();
    final var lsb = context.readU64();
    return new CBUUID(new UUID(msb, lsb));
  }
}
