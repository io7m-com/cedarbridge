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

package com.io7m.cedarbridge.examples.generic;

import com.io7m.cedarbridge.runtime.api.CBProtocolMessageType;
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerType;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextSize;
import com.io7m.cedarbridge.runtime.bssio.CBSerializationContextBSSIO;
import com.io7m.jbssio.api.BSSReaderProviderType;
import com.io7m.jbssio.api.BSSWriterProviderType;
import com.io7m.junreachable.UnreachableCodeException;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.Optional;

public final class CBExSocket<M, P extends CBProtocolMessageType>
{
  private final BSSReaderProviderType readers;
  private final BSSWriterProviderType writers;
  private final Socket socket;
  private final CBExMessageTranslatorType<M, P> translator;
  private final InputStream inputStream;
  private final OutputStream outputStream;
  private final CBProtocolSerializerType<P> serializer;
  private final ByteBuffer writeSizeBuffer;
  private final byte[] writeSizeData;
  private volatile ReadStateType<M> readState;

  public CBExSocket(
    final BSSReaderProviderType inReaders,
    final BSSWriterProviderType inWriters,
    final Socket inSocket,
    final CBExMessageTranslatorType<M, P> inTranslator,
    final CBProtocolSerializerType<P> inSerializer)
    throws IOException
  {
    this.readers =
      Objects.requireNonNull(inReaders, "readers");
    this.writers =
      Objects.requireNonNull(inWriters, "writers");
    this.socket =
      Objects.requireNonNull(inSocket, "socket");
    this.translator =
      Objects.requireNonNull(inTranslator, "translator");
    this.serializer =
      Objects.requireNonNull(inSerializer, "serializer");

    this.writeSizeData =
      new byte[4];
    this.writeSizeBuffer =
      ByteBuffer.wrap(this.writeSizeData)
        .order(ByteOrder.BIG_ENDIAN);

    this.inputStream =
      this.socket.getInputStream();
    this.outputStream =
      this.socket.getOutputStream();
    this.readState =
      new ReadStateGettingSize<>(this);
  }

  public Optional<M> read()
    throws IOException
  {
    return this.readState.read();
  }

  public <N extends M> Optional<N> read(
    final Class<N> clazz)
    throws IOException
  {
    return this.readState.read().map(m -> {
      if (clazz.isInstance(m)) {
        return clazz.cast(m);
      }
      final var lineSeparator = System.lineSeparator();
      throw new IllegalStateException(
        new StringBuilder(128)
          .append("Unexpected message.")
          .append(lineSeparator)
          .append("  Expected: ")
          .append(clazz)
          .append(lineSeparator)
          .append("  Received: ")
          .append(m.getClass())
          .append(lineSeparator)
          .toString()
      );
    });
  }

  public M readBlocking()
    throws IOException
  {
    while (true) {
      final var result = this.read();
      if (result.isPresent()) {
        return result.get();
      }
    }
  }

  public <N extends M> N readBlocking(
    final Class<N> clazz)
    throws IOException
  {
    while (true) {
      final var result = this.read(clazz);
      if (result.isPresent()) {
        return result.get();
      }
    }
  }

  public void write(
    final M message)
    throws IOException
  {
    final var serializedOpt = this.translator.toWire(message);
    if (serializedOpt.isPresent()) {
      final var serialized = serializedOpt.get();

      final var sizeContext = new CBSerializationContextSize();
      try {
        this.serializer.serialize(sizeContext, serialized);
      } catch (final IOException e) {
        throw new UnreachableCodeException(e);
      }

      this.writeSizeBuffer.putInt(0, (int) sizeContext.size());

      final var buffered = new BufferedOutputStream(this.outputStream);
      buffered.write(this.writeSizeData);
      final var writeContext =
        CBSerializationContextBSSIO.createFromOutputStream(
          this.writers, buffered
        );

      this.serializer.serialize(writeContext, serialized);
      buffered.flush();
    }
  }

  private interface ReadStateType<M>
  {
    Optional<M> read()
      throws IOException;
  }

  private static final class ReadStateGettingSize<M, P extends CBProtocolMessageType>
    implements ReadStateType<M>
  {
    private final CBExSocket<M, P> socket;
    private final ByteArrayOutputStream sizeBuffer;

    ReadStateGettingSize(
      final CBExSocket<M, P> inSocket)
    {
      this.socket = inSocket;
      this.sizeBuffer = new ByteArrayOutputStream(4);
    }

    @Override
    public Optional<M> read()
      throws IOException
    {
      final var bytesRemaining = 4 - this.sizeBuffer.size();
      final var bytes = new byte[bytesRemaining];

      final int bytesRead;
      try {
        bytesRead = this.socket.inputStream.read(bytes);
      } catch (final SocketTimeoutException e) {
        return Optional.empty();
      }

      if (bytesRead > 0) {
        this.sizeBuffer.write(bytes, 0, bytesRead);
      }

      if (this.sizeBuffer.size() == 4) {
        final var sizeData =
          ByteBuffer.wrap(this.sizeBuffer.toByteArray())
            .order(ByteOrder.BIG_ENDIAN);
        final var size =
          sizeData.getInt();
        this.socket.readState =
          new ReadStateGettingData<>(this.socket, size);
        return this.socket.readState.read();
      }

      if (this.sizeBuffer.size() > 4) {
        throw new IllegalStateException("Consumed too much data");
      }
      return Optional.empty();
    }
  }

  private static final class ReadStateGettingData<M, P extends CBProtocolMessageType>
    implements ReadStateType<M>
  {
    private final CBExSocket<M, P> socket;
    private final int expectedSize;
    private final ByteArrayOutputStream dataBuffer;
    private final byte[] dataChunk;

    ReadStateGettingData(
      final CBExSocket<M, P> inSocket,
      final int inExpectedSize)
    {
      this.socket = inSocket;
      this.expectedSize = inExpectedSize;
      this.dataBuffer = new ByteArrayOutputStream();
      this.dataChunk = new byte[1024];
    }

    @Override
    public Optional<M> read()
      throws IOException
    {
      final var bytesRemaining =
        this.expectedSize - this.dataBuffer.size();
      final var toRead =
        Math.min(bytesRemaining, this.dataChunk.length);
      final var bytesRead =
        this.socket.inputStream.read(this.dataChunk, 0, toRead);

      if (bytesRead == -1) {
        throw new EOFException();
      }

      this.dataBuffer.write(this.dataChunk, 0, bytesRead);
      if (this.dataBuffer.size() == this.expectedSize) {
        this.socket.readState = new ReadStateGettingSize<>(this.socket);
        return this.deserialize();
      }
      if (this.dataBuffer.size() > this.expectedSize) {
        throw new IllegalStateException("Consumed too much data");
      }
      return Optional.empty();
    }

    private Optional<M> deserialize()
      throws IOException
    {
      final var context =
        CBSerializationContextBSSIO.createFromByteArray(
          this.socket.readers,
          this.dataBuffer.toByteArray()
        );

      final var serialized = this.socket.serializer.deserialize(context);
      return Optional.of(this.socket.translator.fromWire(serialized));
    }
  }
}
