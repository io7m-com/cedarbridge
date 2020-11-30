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

package com.io7m.cedarbridge.schema.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable user data associated with AST elements. This structure
 * is effectively a type-indexed heterogeneous map.
 */

public final class CBASTMutableUserData
{
  private final Map<Class<?>, Object> items;

  /**
   * Construct an empty metadata map.
   */

  public CBASTMutableUserData()
  {
    this.items = new HashMap<>();
  }

  /**
   * Put a value into the user data, replacing any existing data of the
   * same type.
   *
   * @param clazz The user data type
   * @param item  The data
   * @param <T>   The precise type of data
   */

  public <T> void put(
    final Class<T> clazz,
    final T item)
  {
    Objects.requireNonNull(clazz, "clazz");
    Objects.requireNonNull(item, "item");
    this.items.put(clazz, item);
  }

  /**
   * Get data from the map with the given type.
   *
   * @param clazz The user data type
   * @param <T>   The precise type of data
   *
   * @return The data, if any
   *
   * @throws IllegalArgumentException If no data exists with the given type
   */

  public <T> T get(
    final Class<T> clazz)
    throws IllegalArgumentException
  {
    Objects.requireNonNull(clazz, "clazz");

    return this.find(clazz)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("No user data registered of type %s", clazz)
      ));
  }

  /**
   * Get data from the map with the given type.
   *
   * @param clazz The user data type
   * @param <T>   The precise type of data
   *
   * @return The data, if any
   *
   * @throws IllegalArgumentException If no data exists with the given type
   */

  private <T> Optional<T> find(
    final Class<T> clazz)
  {
    Objects.requireNonNull(clazz, "clazz");

    return Optional.ofNullable(this.items.get(clazz))
      .map(clazz::cast);
  }
}
