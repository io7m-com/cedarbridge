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

package com.io7m.cedarbridge.tests.runtime.bssio;

import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned32;
import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned8;
import com.io7m.cedarbridge.runtime.api.CBOptionType;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializationException;
import com.io7m.cedarbridge.runtime.bssio.CBSerializationContextBSSIO;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import org.apache.commons.io.output.BrokenOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBSerializationContextBSSIOTest
{
  private static final String READ_ERROR_MESSAGE =
    "@0x0: Attempting to read bytes would exceed the reader size limit.";

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

  @Test
  public void testReadErrors()
    throws IOException
  {
    final var bs =
      new byte[0];
    final var c =
      CBSerializationContextBSSIO.createFromByteArray(this.readers, bs);

    assertAll(
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readS8);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readS16);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readS32);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readS64);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readU8);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readU16);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readU32);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readU64);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readF16);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readF32);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readF64);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readByteArray);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readUTF8);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readVariantIndex);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      },
      () -> {
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, c::readSequenceLength);
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(0L, ex.byteOffset());
        assertEquals(READ_ERROR_MESSAGE, ex.getMessage());
      }
    );
  }

  @Test
  public void testWriteErrors()
    throws IOException
  {
    assertAll(
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeS8(0L);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(1L, ex.byteOffset());
        assertEquals("@0x1: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeS16(0L);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(2L, ex.byteOffset());
        assertEquals("@0x2: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeS32(0L);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(4L, ex.byteOffset());
        assertEquals("@0x4: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeS64(0L);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(8L, ex.byteOffset());
        assertEquals("@0x8: Broken output stream", ex.getMessage());
      },

      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeU8(0L);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(1L, ex.byteOffset());
        assertEquals("@0x1: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeU16(0L);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(2L, ex.byteOffset());
        assertEquals("@0x2: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeU32(0L);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(4L, ex.byteOffset());
        assertEquals("@0x4: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeU64(0L);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(8L, ex.byteOffset());
        assertEquals("@0x8: Broken output stream", ex.getMessage());
      },

      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeF16(0.0);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(2L, ex.byteOffset());
        assertEquals("@0x2: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeF32(0.0);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(4L, ex.byteOffset());
        assertEquals("@0x4: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeF64(0.0);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(8L, ex.byteOffset());
        assertEquals("@0x8: Broken output stream", ex.getMessage());
      },

      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeByteArray(ByteBuffer.wrap("x".getBytes(UTF_8)));
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(4L, ex.byteOffset());
        assertEquals("@0x4: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeUTF8("x");
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(4L, ex.byteOffset());
        assertEquals("@0x4: Broken output stream", ex.getMessage());
      },

      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeVariantIndex(23);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(4L, ex.byteOffset());
        assertEquals("@0x4: Broken output stream", ex.getMessage());
      },
      () -> {
        final var c = this.brokenWriteContext();
        final var ex =
          assertThrows(CBSerializationException.class, () -> {
            executeIO(c, () -> {
              c.writeSequenceLength(23);
            });
          });
        assertEquals("x.y.z", ex.path());
        assertEquals(4L, ex.byteOffset());
        assertEquals("@0x4: Broken output stream", ex.getMessage());
      }
    );
  }

  @Test
  public void testUnrecognizedVariantIndex()
    throws IOException
  {
    final var bs = new byte[8];
    final var c =
      CBSerializationContextBSSIO.createFromByteArray(this.readers, bs);

    bs[0] = (byte) 0xff;
    bs[1] = (byte) 0x00;
    bs[2] = (byte) 0x00;
    bs[3] = (byte) 0x00;
    bs[4] = (byte) 0xff;
    bs[5] = (byte) 0x00;
    bs[6] = (byte) 0x00;
    bs[7] = (byte) 0x00;

    c.begin("x");
    c.begin("y");
    c.begin("z");

    final var ex =
      assertThrows(CBSerializationException.class, () -> {
        CBOptionType.deserialize(c, CBIntegerUnsigned32::deserialize);
      });

    assertEquals(4L, ex.byteOffset());
    assertEquals(
      "@0x4: CBOptionType: Unrecognized variant index: -16777216",
      ex.getMessage());
    assertEquals("x.y.z", ex.path());
  }

  @Test
  public void testUnrecognizedVariantCaseClass()
    throws IOException
  {
    final var c =
      CBSerializationContextBSSIO.createFromOutputStream(
        this.writers,
        new ByteArrayOutputStream());

    c.begin("x");
    c.begin("y");
    c.begin("z");

    final var ex =
      assertThrows(CBSerializationException.class, () -> {
        throw c.errorUnrecognizedVariantCaseClass(
          CBIntegerUnsigned32.class,
          CBIntegerUnsigned8.class);
      });

    assertEquals(0L, ex.byteOffset());
    assertEquals(
      "@0x0: CBIntegerUnsigned32: Unrecognized variant case class: class com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned8",
      ex.getMessage());
    assertEquals("x.y.z", ex.path());
  }

  private CBSerializationContextType brokenWriteContext()
  {
    return CBSerializationContextBSSIO.createFromOutputStream(
      this.writers,
      new BrokenOutputStream()
    );
  }

  private interface IOOperationType
  {
    void execute()
      throws IOException;
  }

  private static void executeIO(
    final CBSerializationContextType c,
    final IOOperationType op)
    throws IOException
  {
    c.begin("x");
    try {
      c.begin("y");
      try {
        c.begin("z");
        try {
          op.execute();
        } finally {
          c.end("z");
        }
      } finally {
        c.end("y");
      }
    } finally {
      c.end("x");
    }
  }
}
