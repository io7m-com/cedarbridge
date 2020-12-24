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

package com.io7m.cedarbridge.tests.runtime.bssio;

import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.bssio.CBSerializationContextBSSIO;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CBSerializationContextBSSIOTest
{
  private BSSReaders readers;
  private BSSWriters writers;
  private ByteArrayOutputStream writerBuffer;
  private CBSerializationContextType writeContext;

  private CBSerializationContextType readContextStream()
  {
    return CBSerializationContextBSSIO.createFromInputStream(
      this.readers,
      new ByteArrayInputStream(this.writerBuffer.toByteArray())
    );
  }

  private CBSerializationContextType readContextArray()
  {
    return CBSerializationContextBSSIO.createFromByteArray(
      this.readers,
      this.writerBuffer.toByteArray()
    );
  }

  @BeforeEach
  public void setup()
  {
    this.readers = new BSSReaders();
    this.writers = new BSSWriters();

    this.writerBuffer = new ByteArrayOutputStream();
    this.writeContext =
      CBSerializationContextBSSIO.createFromOutputStream(
        this.writers,
        this.writerBuffer);
  }

  @Test
  public void testExhaustive()
    throws IOException
  {
    this.writeContext.begin("Hello!");
    this.writeContext.begin("Data", 0);
    this.writeContext.writeS8(1L);
    this.writeContext.writeS16(2L);
    this.writeContext.writeS32(3L);
    this.writeContext.writeS64(4L);
    this.writeContext.writeU8(5L);
    this.writeContext.writeU16(6L);
    this.writeContext.writeU32(7L);
    this.writeContext.writeU64(8L);
    this.writeContext.writeF16(9.0);
    this.writeContext.writeF32(10.0);
    this.writeContext.writeF64(11.0);
    this.writeContext.writeUTF8("Hello");
    this.writeContext.writeByteArray(ByteBuffer.wrap(new byte[]{(byte) 0x30, (byte) 0x40, (byte) 0x50}));
    this.writeContext.writeSequenceLength(12);
    this.writeContext.writeVariantIndex(13);
    this.writeContext.end("Data", 0);
    this.writeContext.end("Hello!");
    this.writeContext.flush();

    {
      final var readContext = this.readContextStream();
      assertEquals(1L, readContext.readS8());
      assertEquals(2L, readContext.readS16());
      assertEquals(3L, readContext.readS32());
      assertEquals(4L, readContext.readS64());
      assertEquals(5L, readContext.readU8());
      assertEquals(6L, readContext.readU16());
      assertEquals(7L, readContext.readU32());
      assertEquals(8L, readContext.readU64());
      assertEquals(9.0, readContext.readF16());
      assertEquals(10.0, readContext.readF32());
      assertEquals(11.0, readContext.readF64());
      assertEquals("Hello", readContext.readUTF8());
      final var data = readContext.readByteArray();
      assertEquals(0x30, data.get(0));
      assertEquals(0x40, data.get(1));
      assertEquals(0x50, data.get(2));
      assertEquals(12, readContext.readSequenceLength());
      assertEquals(13, readContext.readVariantIndex());
    }

    {
      final var readContext = this.readContextArray();
      assertEquals(1L, readContext.readS8());
      assertEquals(2L, readContext.readS16());
      assertEquals(3L, readContext.readS32());
      assertEquals(4L, readContext.readS64());
      assertEquals(5L, readContext.readU8());
      assertEquals(6L, readContext.readU16());
      assertEquals(7L, readContext.readU32());
      assertEquals(8L, readContext.readU64());
      assertEquals(9.0, readContext.readF16());
      assertEquals(10.0, readContext.readF32());
      assertEquals(11.0, readContext.readF64());
      assertEquals("Hello", readContext.readUTF8());
      final var data = readContext.readByteArray();
      assertEquals(0x30, data.get(0));
      assertEquals(0x40, data.get(1));
      assertEquals(0x50, data.get(2));
      assertEquals(12, readContext.readSequenceLength());
      assertEquals(13, readContext.readVariantIndex());
    }
  }
}
