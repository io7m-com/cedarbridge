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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The serialization context related to reading values.
 */

public interface CBSerializationContextInputType
{
  /**
   * Read a sequence length
   *
   * @return The length
   *
   * @throws IOException On I/O errors
   */

  int readSequenceLength()
    throws IOException;

  /**
   * Read a variant index
   *
   * @return The index
   *
   * @throws IOException On I/O errors
   */

  int readVariantIndex()
    throws IOException;

  /**
   * Read a signed 64-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  long readS64()
    throws IOException;

  /**
   * Read a signed 32-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  int readS32()
    throws IOException;

  /**
   * Read a signed 16-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  int readS16()
    throws IOException;

  /**
   * Read a signed 8-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  int readS8()
    throws IOException;

  /**
   * Read an unsigned 64-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  long readU64()
    throws IOException;

  /**
   * Read an unsigned 32-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  long readU32()
    throws IOException;

  /**
   * Read an unsigned 16-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  int readU16()
    throws IOException;

  /**
   * Read an unsigned 8-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  int readU8()
    throws IOException;

  /**
   * Read a floating point 64-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  double readF64()
    throws IOException;

  /**
   * Read a floating point 32-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  double readF32()
    throws IOException;

  /**
   * Read a floating point 16-bit value
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  double readF16()
    throws IOException;

  /**
   * Read a byte array.
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  ByteBuffer readByteArray()
    throws IOException;

  /**
   * Read a UTF-8 string.
   *
   * @return The value
   *
   * @throws IOException On I/O errors
   */

  String readUTF8()
    throws IOException;
}
