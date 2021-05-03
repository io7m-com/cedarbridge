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

package com.io7m.cedarbridge.runtime.api;

import java.io.IOException;
import java.util.Objects;

/**
 * A serializer of optional values.
 *
 * @param <T> The type of value
 */

public final class CBOptionSerializer<T extends CBSerializableType>
  extends CBAbstractSerializer<CBOptionType<T>>
{
  private final CBSerializerType<T> itemSerializer;

  /**
   * A serializer of optional values.
   *
   * @param inItemSerializer The serializer for the held values
   */

  public CBOptionSerializer(
    final CBSerializerType<T> inItemSerializer)
  {
    this.itemSerializer =
      Objects.requireNonNull(inItemSerializer, "itemSerializer");
  }

  @Override
  public void serialize(
    final CBSerializationContextType context,
    final CBOptionType<T> value)
    throws IOException
  {
    if (value instanceof CBSome) {
      this.serializeSome(context, (CBSome<T>) value);
      return;
    }
    if (value instanceof CBNone) {
      this.serializeNone(context, (CBNone<T>) value);
      return;
    }
    throw new IllegalStateException(
      String.format("Unrecognized variant case: %s", value.getClass())
    );
  }

  private void serializeNone(
    final CBSerializationContextType context,
    final CBNone<T> value)
    throws IOException
  {
    context.writeVariantIndex(CBNone.VARIANT_INDEX);
  }

  private void serializeSome(
    final CBSerializationContextType context,
    final CBSome<T> value)
    throws IOException
  {
    context.writeVariantIndex(CBSome.VARIANT_INDEX);
    this.itemSerializer.serialize(context, value.value());
  }

  @Override
  public CBOptionType<T> deserialize(
    final CBSerializationContextType context)
    throws IOException
  {
    final var index = context.readVariantIndex();
    switch (index) {
      case CBNone.VARIANT_INDEX:
        return this.deserializeNone(context);
      case CBSome.VARIANT_INDEX:
        return this.deserializeSome(context);

      default:
        throw new IllegalStateException(
          String.format(
            "Unrecognized variant index: %d", Integer.valueOf(index))
        );
    }
  }

  private CBOptionType<T> deserializeSome(
    final CBSerializationContextType context)
    throws IOException
  {
    final var v0000 = this.itemSerializer.deserialize(context);
    return CBSome.of(v0000);
  }

  private CBOptionType<T> deserializeNone(
    final CBSerializationContextType context)
  {
    return new CBNone<>();
  }
}
