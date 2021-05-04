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
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerCollectionType;
import com.io7m.cedarbridge.runtime.api.CBSerializerDirectoryType;
import com.io7m.cedarbridge.runtime.container_protocol.CBContainerProtocolMessages;
import com.io7m.cedarbridge.runtime.container_protocol.CBContainerProtocolUse;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.io7m.cedarbridge.examples.generic.CBExClient.State.STATE_DONE;
import static com.io7m.cedarbridge.examples.generic.CBExClient.State.STATE_INITIAL;
import static com.io7m.cedarbridge.examples.generic.CBExClient.State.STATE_RUNNING;

/**
 * A basic TCP client. The client read and writes
 * messages of type {@code M}, converting them to messages of type {@code P}
 * for transfer on the wire. This client is generic enough to provide the
 * base implementation for all of the example code.
 *
 * @param <M> The application-level message types
 * @param <P> The wire-level message types
 */

public final class CBExClient<M, P extends CBProtocolMessageType>
  implements Closeable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBExClient.class);

  private final ExecutorService threadPool;
  private final CBSerializerDirectoryType serializers;
  private final CBProtocolSerializerCollectionType<P> protocols;
  private final CBExMessageTranslatorDirectory<M, P> translators;
  private final CBExClientCoreType<M> core;
  private volatile CBExClient.State state;

  /**
   * Construct a client.
   *
   * @param inCore        A supplier of client cores
   * @param inProtocols   The protocol serializer collection
   * @param inSerializers A serializer directory
   * @param inTranslators A translator directory
   */

  public CBExClient(
    final CBSerializerDirectoryType inSerializers,
    final CBProtocolSerializerCollectionType<P> inProtocols,
    final CBExMessageTranslatorDirectory<M, P> inTranslators,
    final CBExClientCoreType<M> inCore)
  {
    this.serializers =
      Objects.requireNonNull(inSerializers, "serializers");
    this.protocols =
      Objects.requireNonNull(inProtocols, "protocols");
    this.translators =
      Objects.requireNonNull(inTranslators, "inTranslators");
    this.core =
      Objects.requireNonNull(inCore, "core");

    this.threadPool = Executors.newSingleThreadExecutor();
    this.state = STATE_INITIAL;
  }

  @Override
  public void close()
  {
    this.threadPool.shutdown();
  }

  /**
   * Start the client.
   */

  public void start()
  {
    if (this.state == STATE_INITIAL) {
      this.threadPool.execute(() -> {
        if (this.state == STATE_INITIAL) {
          this.state = STATE_RUNNING;
          try {
            this.execute();
          } catch (final IOException e) {
            LOG.error("client failed: ", e);
          } finally {
            this.state = STATE_DONE;
          }
        }
      });
    }
  }

  private void execute()
    throws IOException
  {
    final var localhost =
      new InetSocketAddress("localhost", 32000);

    try (var socket = new Socket()) {
      socket.setKeepAlive(true);
      socket.setPerformancePreferences(0, 1, 0);
      socket.setTcpNoDelay(true);
      socket.setTrafficClass(0x02 | 0x10);
      socket.connect(localhost);

      LOG.info("[{}] connected", socket.getRemoteSocketAddress());

      final var input =
        socket.getInputStream();
      final var output =
        socket.getOutputStream();

      final var availableBuffer =
        input.readNBytes(CBContainerProtocolMessages.sizeAvailable());
      final var available =
        CBContainerProtocolMessages.parseAvailable(availableBuffer);

      final var highestVersion =
        this.protocols.highestSupportedVersion(
          available.applicationProtocolId(),
          available.applicationProtocolMinimumVersion(),
          available.applicationProtocolMaximumVersion()
        );

      LOG.debug(
        "requesting protocol {} version {}",
        available.applicationProtocolId(),
        Long.valueOf(highestVersion)
      );

      output.write(
        CBContainerProtocolMessages.serializeUseAsBytes(
          CBContainerProtocolUse.builder()
            .setContainerProtocolVersion(1L)
            .setApplicationProtocolId(available.applicationProtocolId())
            .setApplicationProtocolVersion(highestVersion)
            .build()
        )
      );

      final var responseBuffer =
        input.readNBytes(CBContainerProtocolMessages.sizeResponse());
      final var response =
        CBContainerProtocolMessages.parseResponse(responseBuffer);

      if (!response.ok()) {
        LOG.error("protocol cannot be used: {}", response.message());
        return;
      }

      socket.setSoTimeout(10);

      final var protocolFactory =
        this.protocols.findOrThrow(highestVersion);
      final var protocol =
        protocolFactory.create(this.serializers);

      final var exSocket =
        new CBExSocket<>(
          new BSSReaders(),
          new BSSWriters(),
          socket,
          this.translators.get(highestVersion),
          protocol
        );

      this.core.execute(exSocket);
    } catch (final EOFException e) {
      // Fine!
    } finally {
      LOG.info("disconnected");
    }
  }

  /**
   * @return {@code true} if the client has finished executing
   */

  public boolean isDone()
  {
    return this.state == STATE_DONE;
  }

  enum State
  {
    STATE_INITIAL,
    STATE_RUNNING,
    STATE_DONE
  }
}
