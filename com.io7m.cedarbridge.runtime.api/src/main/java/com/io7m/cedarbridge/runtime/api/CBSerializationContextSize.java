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

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A serialization context that writes no data, but calculates the size of all
 * data passing through it.
 */

public final class CBSerializationContextSize
  implements CBSerializationContextType
{
  private long currentSize;

  /**
   * Construct a context.
   */

  public CBSerializationContextSize()
  {

  }

  /**
   * @return The accumulated size of all data encountered so far
   */

  public long size()
  {
    return this.currentSize;
  }

  /**
   * Reset the current size to zero.
   */

  public void reset()
  {
    this.currentSize = 0L;
  }

  @Override
  public int readSequenceLength()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int readVariantIndex()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public long readS64()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int readS32()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int readS16()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int readS8()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public long readU64()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public long readU32()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int readU16()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int readU8()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public double readF64()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public double readF32()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public double readF16()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public ByteBuffer readByteArray()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public String readUTF8()
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void flush()
  {

  }

  @Override
  public void writeSequenceLength(
    final int size)
  {
    this.currentSize = this.currentSize + 4L;
  }

  @Override
  public void writeVariantIndex(
    final int x)
  {
    this.currentSize = this.currentSize + 4L;
  }

  @Override
  public void writeS64(
    final long x)
  {
    this.currentSize = this.currentSize + 8L;
  }

  @Override
  public void writeS32(
    final long x)
  {
    this.currentSize = this.currentSize + 4L;
  }

  @Override
  public void writeS16(
    final long x)
  {
    this.currentSize = this.currentSize + 2L;
  }

  @Override
  public void writeS8(
    final long x)
  {
    this.currentSize = this.currentSize + 1L;
  }

  @Override
  public void writeU64(
    final long x)
  {
    this.currentSize = this.currentSize + 8L;
  }

  @Override
  public void writeU32(
    final long x)
  {
    this.currentSize = this.currentSize + 4L;
  }

  @Override
  public void writeU16(
    final long x)
  {
    this.currentSize = this.currentSize + 2L;
  }

  @Override
  public void writeU8(
    final long x)
  {
    this.currentSize = this.currentSize + 1L;
  }

  @Override
  public void writeF64(
    final double x)
  {
    this.currentSize = this.currentSize + 8L;
  }

  @Override
  public void writeF32(
    final double x)
  {
    this.currentSize = this.currentSize + 4L;
  }

  @Override
  public void writeF16(
    final double x)
  {
    this.currentSize = this.currentSize + 2L;
  }

  @Override
  public void writeByteArray(
    final ByteBuffer x)
  {
    this.currentSize = this.currentSize + 4L;
    this.currentSize = this.currentSize + Integer.toUnsignedLong(x.remaining());
  }

  @Override
  public void writeUTF8(
    final String x)
  {
    this.currentSize = this.currentSize + 4L;
    this.currentSize =
      this.currentSize + Integer.toUnsignedLong(x.getBytes(UTF_8).length);
  }

  @Override
  public void begin(
    final String item)
  {

  }

  @Override
  public void begin(
    final String item,
    final int index)
  {

  }

  @Override
  public void end(
    final String item)
  {

  }

  @Override
  public void end(
    final String item,
    final int index)
  {

  }

  @Override
  public IOException errorUnrecognizedVariantIndex(
    final int index)
  {
    return new IOException(
      "Unrecognized variant index: %d".formatted(index)
    );
  }

  @Override
  public IOException errorUnrecognizedVariantCaseClass(
    final Class<?> clazz)
  {
    return new IOException(
      "Unrecognized variant case class: %s".formatted(clazz)
    );
  }
}
