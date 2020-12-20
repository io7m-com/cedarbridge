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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Map;

@ImmutablesStyleType
@Value.Immutable(builder = false, copy = false)
public interface CBMapType<K extends CBSerializableType, V extends CBSerializableType>
  extends Formattable, CBSerializableType
{
  @Value.Parameter
  Map<K, V> values();

  @Override
  default void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    try {
      formatter.out().append('[');
      for (final var entry : this.values().entrySet()) {
        formatter.format("(%s, %s)", entry.getKey(), entry.getValue());
      }
      formatter.out().append(']');
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}