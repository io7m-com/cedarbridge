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
import java.util.Formattable;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type of maps.
 *
 * @param <K>    The type of keys
 * @param <V>    The type of values
 * @param values The values
 */

public record CBMap<K extends CBSerializableType, V extends CBSerializableType>(
  Map<K, V> values)
  implements Formattable, CBSerializableType
{
  /**
   * The type of maps.
   *
   * @param values The values
   */
  public CBMap
  {
    Objects.requireNonNull(values, "values");
  }

  @Override
  public void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    try {
      final var out = formatter.out();
      out.append('[');
      out.append(
        this.values()
          .entrySet()
          .stream()
          .map(x -> String.format("(%s %s)", x.getKey(), x.getValue()))
          .collect(Collectors.joining(" "))
      );
      out.append(']');
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Serialize the given value.
   *
   * @param context The serialization context
   * @param x       The value
   * @param fk      A serializer for {@code K}
   * @param fv      A serializer of {@code V}
   * @param <K>     The type of keys
   * @param <V>     The type of values
   *
   * @throws IOException On errors
   */

  @CBSerializerMethod
  public static <K extends CBSerializableType, V extends CBSerializableType> void serialize(
    final CBSerializationContextType context,
    final CBMap<K, V> x,
    final CBSerializeType<K> fk,
    final CBSerializeType<V> fv)
    throws IOException
  {
    final Map<K, V> map = x.values();
    context.begin("entries");
    context.writeSequenceLength(map.size());
    for (final var entry : map.entrySet()) {
      CBMapEntry.serialize(
        context,
        new CBMapEntry<>(entry.getKey(), entry.getValue()),
        fk,
        fv
      );
    }
    context.end("entries");
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
  public static <K extends CBSerializableType, V extends CBSerializableType> CBMap<K, V> deserialize(
    final CBSerializationContextType context,
    final CBDeserializeType<K> fk,
    final CBDeserializeType<V> fv)
    throws IOException
  {
    context.begin("entries");
    try {
      final var count = context.readSequenceLength();
      final var results = new HashMap<K, V>();
      for (int index = 0; index < count; ++index) {
        final var k = fk.execute(context);
        final var v = fv.execute(context);
        results.put(k, v);
      }
      return new CBMap<>(results);
    } finally {
      context.end("entries");
    }
  }
}
