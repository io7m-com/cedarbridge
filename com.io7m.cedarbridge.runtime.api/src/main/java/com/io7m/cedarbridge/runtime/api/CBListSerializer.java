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
import java.util.ArrayList;
import java.util.Objects;

/**
 * A serializer of lists.
 *
 * @param <T> The type of elements
 */

public final class CBListSerializer<T extends CBSerializableType>
  extends CBAbstractSerializer<CBList<T>>
{
  private final CBSerializerType<T> itemSerializer;

  /**
   * A serializer of lists.
   *
   * @param inItemSerializer The list element serializer
   */

  public CBListSerializer(
    final CBSerializerType<T> inItemSerializer)
  {
    this.itemSerializer =
      Objects.requireNonNull(inItemSerializer, "itemSerializer");
  }

  @Override
  public void serialize(
    final CBSerializationContextType context,
    final CBList<T> value)
    throws IOException
  {
    context.begin("items");

    final var items = value.values();
    context.writeSequenceLength(items.size());

    for (int index = 0; index < items.size(); ++index) {
      context.begin("item", index);
      final var item = items.get(index);
      this.itemSerializer.serialize(context, item);
      context.end("item", index);
    }

    context.end("items");
  }

  @Override
  public CBList<T> deserialize(
    final CBSerializationContextType context)
    throws IOException
  {
    context.begin("items");

    final var length = context.readSequenceLength();
    final var items = new ArrayList<T>();
    for (int index = 0; index < length; ++index) {
      context.begin("item", index);
      items.add(this.itemSerializer.deserialize(context));
      context.end("item", index);
    }

    context.end("items");
    return new CBList(items);
  }
}
