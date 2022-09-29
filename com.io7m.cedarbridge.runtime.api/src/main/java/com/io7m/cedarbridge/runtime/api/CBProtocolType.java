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

import java.math.BigInteger;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;

/**
 * The base type of protocols.
 *
 * @param <T> The type of messages
 */

public interface CBProtocolType<T extends CBProtocolMessageType>
{
  /**
   * @return The type of messages in this protocol
   */

  Class<T> messageClass();

  /**
   * @return The unique protocol identifier
   */

  UUID protocolId();

  /**
   * Retrieve a serializer for the given message class.
   *
   * @param messageClass The message class
   *
   * @return A serializer, if one is available
   */

  Optional<CBProtocolMessageVersionedSerializerType<T>>
  serializerForMessageClass(
    Class<T> messageClass);

  /**
   * Retrieve a serializer for the given protocol version.
   *
   * @param version The protocol version
   *
   * @return A serializer, if one is available
   */

  Optional<CBProtocolMessageVersionedSerializerType<T>>
  serializerForProtocolVersion(
    BigInteger version);

  /**
   * Retrieve a serializer for the given protocol version.
   *
   * @param version The protocol version
   *
   * @return A serializer, if one is available
   */

  default Optional<CBProtocolMessageVersionedSerializerType<T>>
  serializerForProtocolVersion(
    final long version)
  {
    return this.serializerForProtocolVersion(BigInteger.valueOf(version));
  }

  /**
   * @return The set of protocol coordinates for this protocol
   */

  SortedSet<CBProtocolCoordinates> protocols();

  /**
   * @return The set of protocol versions for this protocol
   */

  SortedSet<BigInteger> protocolVersions();
}
