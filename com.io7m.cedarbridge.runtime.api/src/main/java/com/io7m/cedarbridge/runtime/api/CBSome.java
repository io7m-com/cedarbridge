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
import java.util.Objects;
import java.util.Optional;

/**
 * The Some case.
 *
 * @param <T>   The type of values
 * @param value The present value
 */

public record CBSome<T extends CBSerializableType>(T value)
  implements CBOptionType<T>
{
  /**
   * The variant index.
   */

  static final int VARIANT_INDEX = 1;

  /**
   * The Some case.
   *
   * @param value The present value
   */

  public CBSome
  {
    Objects.requireNonNull(value, "value");
  }

  @Override
  public void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    formatter.format("(CBSome %s)", this.value());
  }

  @Override
  public Optional<T> asOptional()
  {
    return Optional.of(this.value());
  }

  /**
   * Serialize the given value.
   *
   * @param context The serialization context
   * @param x       The value
   * @param ft      A serializer for {@code T}
   * @param <T>     The type of list values
   *
   * @throws IOException On errors
   */

  public static <T extends CBSerializableType> void serialize(
    final CBSerializationContextType context,
    final CBSome<T> x,
    final CBSerializeType<T> ft)
    throws IOException
  {
    context.writeVariantIndex(VARIANT_INDEX);
    ft.execute(context, x.value);
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
  public static <T extends CBSerializableType> CBSome<T> deserialize(
    final CBSerializationContextType context,
    final CBDeserializeType<T> ft)
    throws IOException
  {
    return new CBSome<>(ft.execute(context));
  }
}
