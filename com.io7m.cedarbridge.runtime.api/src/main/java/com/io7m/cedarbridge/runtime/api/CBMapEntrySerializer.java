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

import java.util.Objects;

public final class CBMapEntrySerializer<K extends CBSerializableType, V extends CBSerializableType>
  extends CBAbstractSerializer<CBMapEntry<K, V>>
{
  private final CBSerializerType<K> keySerializer;
  private final CBSerializerType<V> valueSerializer;

  public CBMapEntrySerializer(
    final CBSerializerType<K> inKeySerializer,
    final CBSerializerType<V> inValueSerializer)
  {
    this.keySerializer =
      Objects.requireNonNull(inKeySerializer, "keySerializer");
    this.valueSerializer =
      Objects.requireNonNull(inValueSerializer, "valueSerializer");
  }

  @Override
  public void serialize(
    final CBSerializationContextType context,
    final CBMapEntry<K, V> value)
  {
    this.keySerializer.serialize(context, value.key());
    this.valueSerializer.serialize(context, value.value());
  }

  @Override
  public CBMapEntry<K, V> deserialize(
    final CBSerializationContextType context)
  {
    final var key = this.keySerializer.deserialize(context);
    final var val = this.valueSerializer.deserialize(context);
    return CBMapEntry.of(key, val);
  }
}
