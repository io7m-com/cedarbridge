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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A collection of protocol serializers.
 *
 * @param <T> The type of protocol messages
 */

public interface CBProtocolSerializerCollectionType<T extends CBProtocolMessageType>
{
  private static long minUnsigned(
    final long x,
    final long y)
  {
    if (Long.compareUnsigned(x, y) < 0) {
      return x;
    }
    return y;
  }

  /**
   * @return The protocol ID
   */

  UUID id();

  /**
   * @return The lowest supported protocol version
   */

  long versionLower();

  /**
   * @return The highest supported protocol version
   */

  long versionUpper();

  /**
   * @return The list of protocol serializers
   */

  List<CBProtocolSerializerFactoryType<T>> factories();

  /**
   * Find the serializer with the given type.
   *
   * @param clazz The type of protocol messages
   * @param <U>   The type of protocol messages
   *
   * @return The serializer, if any
   */

  default <U extends T> Optional<CBProtocolSerializerFactoryType<U>> find(
    final Class<U> clazz)
  {
    return this.factories()
      .stream()
      .filter(s -> s.matches(clazz).isPresent())
      .findFirst()
      .flatMap(s -> s.matches(clazz));
  }

  /**
   * Find the serializer with the given type.
   *
   * @param clazz The type of protocol messages
   * @param <U>   The type of protocol messages
   *
   * @return The serializer, if any
   */

  default <U extends T> CBProtocolSerializerFactoryType<U> findOrThrow(
    final Class<U> clazz)
  {
    return this.find(clazz)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("No handler for type: %s", clazz))
      );
  }

  /**
   * Find the serializer with the given version.
   *
   * @param version The version of protocol messages
   *
   * @return The serializer, if any
   */

  default Optional<CBProtocolSerializerFactoryType<T>> find(
    final long version)
  {
    return this.factories()
      .stream()
      .filter(s -> s.matches(version).isPresent())
      .findFirst();
  }

  /**
   * Find the serializer with the given type.
   *
   * @param version The type of protocol messages
   *
   * @return The serializer, if any
   */

  default CBProtocolSerializerFactoryType<T> findOrThrow(
    final long version)
  {
    return this.find(version)
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("No handler for version: %d", version))
      );
  }

  /**
   * Retrieve the highest supported version of protocol {@code requestedId} in
   * the range {@code [requestedLow, requestedHigh]}.
   *
   * @param requestedId   The protocol ID
   * @param requestedLow  The lowest allowed version
   * @param requestedHigh The highest allowed version
   *
   * @return The version
   *
   * @throws IllegalArgumentException If the range is malformed, or if no
   *                                  version is available in the given range
   */

  default long highestSupportedVersion(
    final UUID requestedId,
    final long requestedLow,
    final long requestedHigh)
    throws IllegalArgumentException
  {
    if (Long.compareUnsigned(requestedLow, requestedHigh) > 0) {
      throw new IllegalArgumentException(
        String.format(
          "Requested low %s must be >= requested high %s",
          Long.toUnsignedString(requestedLow),
          Long.toUnsignedString(requestedHigh)
        )
      );
    }

    if (Objects.equals(requestedId, this.id())) {
      final var max = minUnsigned(requestedHigh, this.versionUpper());
      if (Long.compareUnsigned(max, requestedLow) >= 0) {
        return max;
      }
    }

    final var lineSeparator = System.lineSeparator();
    throw new IllegalArgumentException(
      new StringBuilder(244)
        .append("Unsupported protocol.")
        .append(lineSeparator)
        .append("  Requested: ")
        .append(requestedId)
        .append(lineSeparator)
        .append("  Requested Versions: [")
        .append(Long.toUnsignedString(requestedLow))
        .append(' ')
        .append(Long.toUnsignedString(requestedHigh))
        .append(']')
        .append(lineSeparator)
        .append("  Supported: ")
        .append(this.id())
        .append(lineSeparator)
        .append("  Supported Versions: [")
        .append(Long.toUnsignedString(this.versionLower()))
        .append(' ')
        .append(Long.toUnsignedString(this.versionUpper()))
        .append(']')
        .append(lineSeparator)
        .toString()
    );
  }

  /**
   * Check that the version {@code requestedVersion} falls within the supported
   * range of versions, and that the protocol is {@code requestedId}.
   *
   * @param requestedId      The protocol ID
   * @param requestedVersion The version
   *
   * @return The version
   *
   * @throws IllegalArgumentException If no version is available in the given
   *                                  range
   */

  default long checkSupportedVersion(
    final UUID requestedId,
    final long requestedVersion)
  {
    if (Objects.equals(requestedId, this.id())) {
      if (Long.compareUnsigned(requestedVersion, this.versionLower()) >= 0) {
        if (Long.compareUnsigned(requestedVersion, this.versionUpper()) <= 0) {
          return requestedVersion;
        }
      }
    }

    final var lineSeparator = System.lineSeparator();
    throw new IllegalArgumentException(
      new StringBuilder(244)
        .append("Unsupported protocol.")
        .append(lineSeparator)
        .append("  Requested: ")
        .append(requestedId)
        .append(" version ")
        .append(Long.toUnsignedString(requestedVersion))
        .append(lineSeparator)
        .append("  Supported: ")
        .append(this.id())
        .append(" versions [")
        .append(Long.toUnsignedString(this.versionLower()))
        .append(", ")
        .append(Long.toUnsignedString(this.versionUpper()))
        .append(']')
        .append(lineSeparator)
        .toString()
    );
  }
}
