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

package com.io7m.cedarbridge.runtime.bssio;

import com.io7m.cedarbridge.runtime.api.CBSerializationContextFlushOperationType;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializationException;
import com.io7m.jbssio.api.BSSReaderProviderType;
import com.io7m.jbssio.api.BSSReaderSequentialType;
import com.io7m.jbssio.api.BSSReaderSequentialUnsupported;
import com.io7m.jbssio.api.BSSWriterProviderType;
import com.io7m.jbssio.api.BSSWriterSequentialType;
import com.io7m.jbssio.api.BSSWriterSequentialUnsupported;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Objects;
import java.util.OptionalLong;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A serialization context based on {@code jbssio}.
 */

public final class CBSerializationContextBSSIO
  implements CBSerializationContextType
{
  private final ArrayList<String> nameStack;
  private final BSSWriterSequentialType writerRoot;
  private final BSSReaderSequentialType readerRoot;
  private final CBSerializationContextFlushOperationType flushOp;
  private BSSReaderSequentialType reader;
  private BSSWriterSequentialType writer;

  private CBSerializationContextBSSIO(
    final BSSReaderSequentialType inReader,
    final BSSWriterSequentialType inWriter,
    final CBSerializationContextFlushOperationType inFlushOp)
  {
    this.writerRoot =
      Objects.requireNonNull(inWriter, "writer");
    this.readerRoot =
      Objects.requireNonNull(inReader, "reader");
    this.flushOp =
      Objects.requireNonNull(inFlushOp, "flushOp");
    this.writer =
      this.writerRoot;
    this.reader =
      this.readerRoot;
    this.nameStack =
      new ArrayList<>(32);
  }

  /**
   * Create a context based on the reader and writer.
   *
   * @param reader  The reader
   * @param writer  The writer
   * @param flushOp An operation executed upon flushing the context
   *
   * @return A context
   */

  public static CBSerializationContextType create(
    final BSSReaderSequentialType reader,
    final BSSWriterSequentialType writer,
    final CBSerializationContextFlushOperationType flushOp)
  {
    return new CBSerializationContextBSSIO(reader, writer, flushOp);
  }

  /**
   * Create a context based on the output stream.
   *
   * @param writers The writers
   * @param stream  The output stream
   *
   * @return A context
   */

  public static CBSerializationContextType createFromOutputStream(
    final BSSWriterProviderType writers,
    final OutputStream stream)
  {
    try {
      return new CBSerializationContextBSSIO(
        new BSSReaderSequentialUnsupported(),
        writers.createWriterFromStream(
          URI.create("urn:stream"),
          stream,
          "output"
        ),
        stream::flush
      );
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Create a context based on the input stream.
   *
   * @param readers The readers
   * @param stream  The input stream
   *
   * @return A context
   */

  public static CBSerializationContextType createFromInputStream(
    final BSSReaderProviderType readers,
    final InputStream stream)
  {
    try {
      return new CBSerializationContextBSSIO(
        readers.createReaderFromStream(
          URI.create("urn:stream"),
          stream,
          "input"
        ),
        new BSSWriterSequentialUnsupported(),
        () -> {

        }
      );
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * Create a context based on the readers and byte array.
   *
   * @param readers The reader provider
   * @param bytes   The byte array
   *
   * @return A context
   */

  public static CBSerializationContextType createFromByteArray(
    final BSSReaderProviderType readers,
    final byte[] bytes)
  {
    try {
      return new CBSerializationContextBSSIO(
        readers.createReaderFromStream(
          URI.create("urn:stream"),
          new ByteArrayInputStream(bytes),
          "input",
          OptionalLong.of(Integer.toUnsignedLong(bytes.length))
        ),
        new BSSWriterSequentialUnsupported(),
        () -> {

        }
      );
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private CBSerializationException errorReadIO(
    final IOException e)
  {
    final var offset = this.reader.offsetCurrentAbsolute();
    return new CBSerializationException(
      "@0x%s: %s".formatted(
        Long.toUnsignedString(offset, 16),
        e.getMessage()
          .lines()
          .findFirst()
          .orElse("Read I/O error")
      ),
      offset,
      this.accessPath()
    );
  }

  private CBSerializationException errorWriteIO(
    final IOException e)
  {
    final var offset = this.writer.offsetCurrentAbsolute();
    return new CBSerializationException(
      "@0x%s: %s".formatted(
        Long.toUnsignedString(offset, 16),
        e.getMessage()
          .lines()
          .findFirst()
          .orElse("Write I/O error")
      ),
      offset,
      this.accessPath()
    );
  }

  private String accessPath()
  {
    return String.join(".", this.nameStack);
  }

  @Override
  public int readSequenceLength()
    throws IOException
  {
    try {
      return (int) this.reader.readU32BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public int readVariantIndex()
    throws IOException
  {
    try {
      return (int) this.reader.readU32BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public long readS64()
    throws IOException
  {
    try {
      return this.reader.readS64BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public int readS32()
    throws IOException
  {
    try {
      return (int) this.reader.readS32BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public int readS16()
    throws IOException
  {
    try {
      return this.reader.readS16BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public int readS8()
    throws IOException
  {
    try {
      return this.reader.readS8();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public long readU64()
    throws IOException
  {
    try {
      return this.reader.readU64BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public long readU32()
    throws IOException
  {
    try {
      return this.reader.readU32BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public int readU16()
    throws IOException
  {
    try {
      return this.reader.readU16BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public int readU8()
    throws IOException
  {
    try {
      return this.reader.readU8();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public double readF64()
    throws IOException
  {
    try {
      return this.reader.readD64BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public double readF32()
    throws IOException
  {
    try {
      return this.reader.readF32BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public double readF16()
    throws IOException
  {
    try {
      return this.reader.readF16BE();
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public ByteBuffer readByteArray()
    throws IOException
  {
    try {
      final var length = this.reader.readU32BE();
      try (var output = new ByteArrayOutputStream()) {
        for (long index = 0L; Long.compareUnsigned(index, length) < 0; ++index) {
          output.write(this.reader.readS8());
        }
        return ByteBuffer.wrap(output.toByteArray());
      }
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public String readUTF8()
    throws IOException
  {
    try {
      final var length = this.reader.readU32BE();
      try (var output = new ByteArrayOutputStream()) {
        for (long index = 0L; Long.compareUnsigned(index, length) < 0; ++index) {
          output.write(this.reader.readS8());
        }
        return output.toString(UTF_8);
      }
    } catch (final IOException e) {
      throw this.errorReadIO(e);
    }
  }

  @Override
  public void flush()
    throws IOException
  {
    this.flushOp.flush();
  }

  @Override
  public void writeSequenceLength(final int size)
    throws IOException
  {
    try {
      this.writer.writeU32BE(Integer.toUnsignedLong(size));
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeVariantIndex(final int x)
    throws IOException
  {
    try {
      this.writer.writeU32BE(Integer.toUnsignedLong(x));
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeS64(final long x)
    throws IOException
  {
    try {
      this.writer.writeS64BE(x);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeS32(final long x)
    throws IOException
  {
    try {
      this.writer.writeS32BE(x);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeS16(final long x)
    throws IOException
  {
    try {
      this.writer.writeS16BE(Math.toIntExact(x));
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeS8(final long x)
    throws IOException
  {
    try {
      this.writer.writeS8(Math.toIntExact(x));
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeU64(final long x)
    throws IOException
  {
    try {
      this.writer.writeU64BE(x);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeU32(final long x)
    throws IOException
  {
    try {
      this.writer.writeU32BE(x);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeU16(final long x)
    throws IOException
  {
    try {
      this.writer.writeU16BE(Math.toIntExact(x));
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeU8(final long x)
    throws IOException
  {
    try {
      this.writer.writeU8(Math.toIntExact(x));
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeF64(final double x)
    throws IOException
  {
    try {
      this.writer.writeF64BE(x);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeF32(final double x)
    throws IOException
  {
    try {
      this.writer.writeF32BE(x);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeF16(final double x)
    throws IOException
  {
    try {
      this.writer.writeF16BE(x);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeByteArray(
    final ByteBuffer x)
    throws IOException
  {
    try {
      final var bytes = x.array();
      this.writer.writeU32BE(Integer.toUnsignedLong(bytes.length));
      this.writer.writeBytes(bytes);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void writeUTF8(
    final String x)
    throws IOException
  {
    try {
      final var bytes = x.getBytes(UTF_8);
      this.writer.writeU32BE(Integer.toUnsignedLong(bytes.length));
      this.writer.writeBytes(bytes);
    } catch (final IOException e) {
      throw this.errorWriteIO(e);
    }
  }

  @Override
  public void begin(
    final String item)
  {
    this.nameStack.add(item);
  }

  @Override
  public void begin(
    final String item,
    final int index)
  {
    this.nameStack.add(item);
  }

  @Override
  public void end(
    final String item)
  {
    this.nameStack.remove(this.nameStack.size() - 1);
  }

  @Override
  public void end(
    final String item,
    final int index)
  {
    this.nameStack.remove(this.nameStack.size() - 1);
  }

  @Override
  public IOException errorUnrecognizedVariantIndex(
    final Class<?> readerClass,
    final int index)
  {
    final var offset = this.reader.offsetCurrentAbsolute();
    return new CBSerializationException(
      "@0x%s: %s: Unrecognized variant index: %d"
        .formatted(
          Long.toUnsignedString(offset, 16),
          readerClass.getSimpleName(),
          index),
      offset,
      this.accessPath()
    );
  }

  @Override
  public IOException errorUnrecognizedVariantCaseClass(
    final Class<?> writerClass,
    final Class<?> clazz)
  {
    final var offset = this.writer.offsetCurrentAbsolute();
    return new CBSerializationException(
      "@0x%s: %s: Unrecognized variant case class: %s"
        .formatted(
          Long.toUnsignedString(offset, 16),
          writerClass.getSimpleName(),
          clazz),
      offset,
      this.accessPath()
    );
  }
}
