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
import java.util.ArrayList;
import java.util.Formattable;
import java.util.Formatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type of lists.
 *
 * @param <T>    The type of list elements
 * @param values The values
 */

public record CBList<T extends CBSerializableType>(List<T> values)
  implements Formattable, CBSerializableType
{
  /**
   * The type of lists.
   *
   * @param values The values
   */

  public CBList
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
          .stream()
          .map(x -> String.format("%s", x))
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
   * @param ft      A serializer for {@code T}
   * @param <T>     The type of list values
   *
   * @throws IOException On errors
   */

  @CBSerializerMethod
  public static <T extends CBSerializableType> void serialize(
    final CBSerializationContextType context,
    final CBList<T> x,
    final CBSerializeType<T> ft)
    throws IOException
  {
    context.begin("items");

    try {
      final var items = x.values();
      context.writeSequenceLength(items.size());

      for (int index = 0; index < items.size(); ++index) {
        context.begin("item", index);
        try {
          final var item = items.get(index);
          ft.execute(context, item);
        } finally {
          context.end("item", index);
        }
      }
    } finally {
      context.end("items");
    }
  }

  /**
   * Deserialize a list value.
   *
   * @param context The serialization context
   * @param ft      A deserializer for {@code T}
   * @param <T>     The type of list values
   *
   * @return A list of values of type {@code T}
   *
   * @throws IOException On errors
   */

  @CBDeserializerMethod
  public static <T extends CBSerializableType> CBList<T> deserialize(
    final CBSerializationContextType context,
    final CBDeserializeType<T> ft)
    throws IOException
  {
    context.begin("items");

    try {
      final var items = new ArrayList<T>();
      final var count = context.readSequenceLength();
      for (int index = 0; index < count; ++index) {
        context.begin("item", index);
        try {
          items.add(ft.execute(context));
        } finally {
          context.end("item", index);
        }
      }
      return new CBList<>(items);
    } finally {
      context.end("items");
    }
  }
}
