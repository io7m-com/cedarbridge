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

import java.util.List;

/**
 * A factory of map entry serializers.
 *
 * @param <K> The type of keys
 * @param <V> The type of values
 */

public final class CBMapEntrySerializers<K extends CBSerializableType, V extends CBSerializableType>
  extends CBAbstractSerializerFactory<CBMapEntry<K, V>>
{
  /**
   * A factory of map entry serializers.
   */

  public CBMapEntrySerializers()
  {
    super("com.io7m.cedarbridge", "MapEntry");
  }

  @Override
  protected CBSerializerType<CBMapEntry<K, V>> createActual(
    final CBSerializerDirectoryType directory,
    final List<CBTypeArgument> arguments)
  {
    final var keyType = arguments.get(0);
    final var valType = arguments.get(1);

    final CBSerializerType<K> keySerializer =
      directory.serializerFor(keyType.target(), keyType.arguments());
    final CBSerializerType<V> valSerializer =
      directory.serializerFor(valType.target(), valType.arguments());

    return new CBMapEntrySerializer<>(keySerializer, valSerializer);
  }

  @Override
  public List<String> typeParameters()
  {
    return List.of("K", "V");
  }
}
