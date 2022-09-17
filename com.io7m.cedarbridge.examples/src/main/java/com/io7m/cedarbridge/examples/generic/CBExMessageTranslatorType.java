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

package com.io7m.cedarbridge.examples.generic;

import com.io7m.cedarbridge.runtime.api.CBProtocolMessageType;

import java.util.Optional;

/**
 * A translator interface to translate between the application domain's
 * canonical message format between the versioned, serialized message formats.
 *
 * @param <M> The canonical message type
 * @param <P> The serialized message formats
 */

public interface CBExMessageTranslatorType<M, P extends CBProtocolMessageType>
{
  /**
   * Convert a canonical message to the equivalent serialized format.
   *
   * @param x The input message
   *
   * @return The serialized format, or {@code null} if no corresponding message exists
   */

  P toWireNullable(M x);

  /**
   * Convert a canonical message to the equivalent serialized format.
   *
   * @param x The input message
   *
   * @return The serialized format, or empty if no corresponding message exists
   */

  default Optional<P> toWire(
    final M x)
  {
    return Optional.ofNullable(this.toWireNullable(x));
  }

  /**
   * Convert a serialized format message to the equivalent canonical message format.
   *
   * @param x The input message
   *
   * @return The canonical message format
   */

  M fromWire(P x);
}
