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

import java.io.IOException;
import java.nio.ByteBuffer;

public interface CBSerializationContextOutputType
{
  void flush()
    throws IOException;

  void writeSequenceLength(
    int size)
    throws IOException;

  void writeVariantIndex(
    int x)
    throws IOException;

  void writeS64(
    long x)
    throws IOException;

  void writeS32(
    long x)
    throws IOException;

  void writeS16(
    long x)
    throws IOException;

  void writeS8(
    long x)
    throws IOException;

  void writeU64(
    long x)
    throws IOException;

  void writeU32(
    long x)
    throws IOException;

  void writeU16(
    long x)
    throws IOException;

  void writeU8(
    long x)
    throws IOException;

  void writeF64(
    double x)
    throws IOException;

  void writeF32(
    double x)
    throws IOException;

  void writeF16(
    double x)
    throws IOException;

  void writeByteArray(
    ByteBuffer x)
    throws IOException;

  void writeUTF8(
    String x)
    throws IOException;
}
