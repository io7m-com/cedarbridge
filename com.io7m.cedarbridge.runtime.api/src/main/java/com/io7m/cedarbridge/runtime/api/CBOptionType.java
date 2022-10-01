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

package com.io7m.cedarbridge.runtime.api;

import java.io.IOException;
import java.util.Formattable;
import java.util.Optional;

/**
 * The option type.
 *
 * @param <T> The type of values
 */

public sealed interface CBOptionType<T extends CBSerializableType>
  extends Formattable, CBSerializableType
  permits CBNone, CBSome
{
  /**
   * @return This value as a Java Optional
   */

  Optional<T> asOptional();

  /**
   * Convert the given Java optional value to an option value.
   *
   * @param opt The Java optional
   * @param <T> The type of contained values
   *
   * @return The option value
   */

  static <T extends CBSerializableType> CBOptionType<T> fromOptional(
    final Optional<T> opt)
  {
    return opt.isPresent() ? new CBSome<>(opt.get()) : new CBNone<>();
  }

  /**
   * Serialize the given value.
   *
   * @param context The serialization context
   * @param x       The value
   * @param ft      A serializer for {@code T}
   * @param <T>     The type of optional values
   *
   * @throws IOException On errors
   */

  @CBSerializerMethod
  static <T extends CBSerializableType> void serialize(
    final CBSerializationContextType context,
    final CBOptionType<T> x,
    final CBSerializeType<T> ft)
    throws IOException
  {
    if (x instanceof CBSome<T> s) {
      CBSome.serialize(context, s, ft);
      return;
    }
    if (x instanceof CBNone<T> n) {
      CBNone.serialize(context, n, ft);
      return;
    }
  }

  /**
   * Deserialize the given value.
   *
   * @param context The serialization context
   * @param <T>     The type of optional values
   * @param fv      A deserializer for {@code T}
   *
   * @return The deserialized value
   *
   * @throws IOException On errors
   */

  @CBDeserializerMethod
  static <T extends CBSerializableType> CBOptionType<T> deserialize(
    final CBSerializationContextType context,
    final CBDeserializeType<T> fv)
    throws IOException
  {
    final var index = context.readVariantIndex();
    return switch (index) {
      case CBSome.VARIANT_INDEX -> CBSome.deserialize(context, fv);
      case CBNone.VARIANT_INDEX -> CBNone.deserialize(context, fv);
      default -> throw context.errorUnrecognizedVariantIndex(CBOptionType.class, index);
    };
  }
}
