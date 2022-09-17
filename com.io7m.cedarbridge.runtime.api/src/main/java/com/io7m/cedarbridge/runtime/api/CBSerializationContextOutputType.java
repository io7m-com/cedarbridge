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
 * The serialization context related to writing values.
 */

public interface CBSerializationContextOutputType
{
  /**
   * Flush the underlying stream, ensuring any buffered output is written.
   *
   * @throws IOException On I/O errors
   */

  void flush()
    throws IOException;

  /**
   * Write a sequence length
   *
   * @param size The size
   *
   * @throws IOException On I/O errors
   */

  void writeSequenceLength(
    int size)
    throws IOException;

  /**
   * Write a variant index
   *
   * @param x The index
   *
   * @throws IOException On I/O errors
   */

  void writeVariantIndex(
    int x)
    throws IOException;

  /**
   * Write a signed 64-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeS64(
    long x)
    throws IOException;

  /**
   * Write a signed 32-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeS32(
    long x)
    throws IOException;

  /**
   * Write a signed 16-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeS16(
    long x)
    throws IOException;

  /**
   * Write a signed 8-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeS8(
    long x)
    throws IOException;

  /**
   * Write an unsigned 64-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeU64(
    long x)
    throws IOException;

  /**
   * Write an unsigned 32-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeU32(
    long x)
    throws IOException;

  /**
   * Write an unsigned 16-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeU16(
    long x)
    throws IOException;

  /**
   * Write an unsigned 8-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeU8(
    long x)
    throws IOException;

  /**
   * Write a floating point 64-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeF64(
    double x)
    throws IOException;

  /**
   * Write a floating point 32-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeF32(
    double x)
    throws IOException;

  /**
   * Write a floating point 16-bit value
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeF16(
    double x)
    throws IOException;

  /**
   * Write a byte array value.
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeByteArray(
    ByteBuffer x)
    throws IOException;

  /**
   * Write a UTF-8 string value.
   *
   * @param x The value
   *
   * @throws IOException On I/O errors
   */

  void writeUTF8(
    String x)
    throws IOException;
}
