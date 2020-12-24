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

import java.io.IOException;

/**
 * A serializer for protocols.
 *
 * @param <T> The type of serialized values
 */

@ProviderType
public interface CBProtocolSerializerType<T extends CBProtocolMessageType>
{
  /**
   * Serialize a value of the given type.
   *
   * @param context The serialization context
   * @param value   The value to be serialized
   *
   * @throws IOException On I/O errors
   */

  void serialize(
    CBSerializationContextType context,
    T value)
    throws IOException;

  /**
   * Deserialize a value of the given type.
   *
   * @param context The serialization context
   *
   * @return A deserialized value
   *
   * @throws IOException On I/O errors
   */

  T deserialize(
    CBSerializationContextType context)
    throws IOException;
}
