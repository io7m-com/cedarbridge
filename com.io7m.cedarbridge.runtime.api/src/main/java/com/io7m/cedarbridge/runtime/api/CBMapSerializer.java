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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CBMapSerializer<K extends CBSerializableType, V extends CBSerializableType>
  extends CBAbstractSerializer<CBMap<K, V>>
{
  private final CBSerializerType<CBMapEntry<K, V>> entrySerializer;

  public CBMapSerializer(
    final CBSerializerType<CBMapEntry<K, V>> inEntryDeserializer)
  {
    this.entrySerializer =
      Objects.requireNonNull(inEntryDeserializer, "entrySerializer");
  }

  @Override
  public void serialize(
    final CBSerializationContextType context,
    final CBMap<K, V> value)
  {
    final Map<K, V> map = value.values();
    context.writeSequenceLength(map.size());

    for (final var entry : map.entrySet()) {
      this.entrySerializer.serialize(
        context,
        CBMapEntry.of(entry.getKey(), entry.getValue())
      );
    }
  }

  @Override
  public CBMap<K, V> deserialize(
    final CBSerializationContextType context)
  {
    final var count = context.readSequenceLength();

    final var results = new HashMap<K, V>();
    for (int index = 0; index < count; ++index) {
      final var entry = this.entrySerializer.deserialize(context);
      results.put(entry.key(), entry.value());
    }
    return CBMap.of(results);
  }
}
