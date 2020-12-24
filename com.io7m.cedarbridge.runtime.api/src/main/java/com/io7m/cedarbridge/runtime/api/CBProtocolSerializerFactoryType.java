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

import org.osgi.annotation.versioning.ProviderType;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A serializer factory for protocols.
 *
 * @param <T> The type of serialized values
 */

@ProviderType
public interface CBProtocolSerializerFactoryType<T extends CBProtocolMessageType>
{
  /**
   * @return The unique ID of the protocol
   */

  UUID id();

  /**
   * @return The version of the protocol implemented by this serializer
   */

  long version();

  /**
   * @return The base class of serialized messages
   */

  Class<T> serializes();

  /**
   * Create a new serializer.
   *
   * @param directory The directory of available serializers
   *
   * @return A new serializer
   */

  CBProtocolSerializerType<T> create(
    CBSerializerDirectoryType directory);

  /**
   * If this serializer factory has version {@code version} then return it.
   * Otherwise, return nothing.
   *
   * @param version The protocol version
   * @param <U>     The type of protocol messages
   *
   * @return This, or nothing
   */

  @SuppressWarnings("unchecked")
  default <U extends T> Optional<CBProtocolSerializerFactoryType<U>> matches(
    final long version)
  {
    if (this.version() == version) {
      return Optional.of((CBProtocolSerializerFactoryType<U>) this);
    }
    return Optional.empty();
  }

  /**
   * If this serializer factory has version {@code version} and serializes
   * messages of type {@code clazz}, then return it. Otherwise, return nothing.
   *
   * @param version The protocol version
   * @param clazz   The type of protocol messages
   * @param <U>     The type of protocol messages
   *
   * @return This, or nothing
   */

  @SuppressWarnings("unchecked")
  default <U extends T> Optional<CBProtocolSerializerFactoryType<U>> matches(
    final long version,
    final Class<U> clazz)
  {
    if (Objects.equals(clazz, this.serializes()) && this.version() == version) {
      return Optional.of((CBProtocolSerializerFactoryType<U>) this);
    }
    return Optional.empty();
  }

  /**
   * If this serializer factory serializes messages of type {@code clazz}, then
   * return it. Otherwise, return nothing.
   *
   * @param clazz The type of protocol messages
   * @param <U>   The type of protocol messages
   *
   * @return This, or nothing
   */

  @SuppressWarnings("unchecked")
  default <U extends T> Optional<CBProtocolSerializerFactoryType<U>> matches(
    final Class<U> clazz)
  {
    if (Objects.equals(clazz, this.serializes())) {
      return Optional.of((CBProtocolSerializerFactoryType<U>) this);
    }
    return Optional.empty();
  }
}
