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
import com.io7m.cedarbridge.runtime.api.CBSerializerDirectoryMutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import static com.io7m.cedarbridge.examples.generic.CBExServer.State.STATE_DONE;
import static com.io7m.cedarbridge.examples.generic.CBExServer.State.STATE_INITIAL;
import static com.io7m.cedarbridge.examples.generic.CBExServer.State.STATE_RUNNING;

public final class CBExServer<M, P extends CBProtocolMessageType> implements
  Closeable
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBExServer.class);

  private final ExecutorService threadPool;
  private final CBSerializerDirectoryMutable serializers;
  private final CBProtocolSerializerCollectionType<P> protocols;
  private final CBExMessageTranslatorDirectory<M, P> translators;
  private final Supplier<CBExServerClientCoreType<M>> core;
  private volatile State state;

  public CBExServer(
    final CBSerializerDirectoryMutable inSerializers,
    final CBProtocolSerializerCollectionType<P> inProtocols,
    final CBExMessageTranslatorDirectory<M, P> inTranslators,
    final Supplier<CBExServerClientCoreType<M>> inCore)
  {
    this.serializers =
      Objects.requireNonNull(inSerializers, "serializers");
    this.protocols =
      Objects.requireNonNull(inProtocols, "protocols");
    this.translators =
      Objects.requireNonNull(inTranslators, "translators");
    this.core =
      Objects.requireNonNull(inCore, "core");

    this.threadPool = Executors.newCachedThreadPool();
    this.state = STATE_INITIAL;
  }

  public void start()
  {
    if (this.state == STATE_INITIAL) {
      this.threadPool.execute(() -> {
        if (this.state == STATE_INITIAL) {
          this.state = STATE_RUNNING;
          try {
            this.execute();
          } catch (final IOException e) {
            LOG.error("server failed: ", e);
            this.state = STATE_DONE;
          }
        }
      });
    }
  }

  private void execute()
    throws IOException
  {
    final var port = 32000;
    final var localhost = new InetSocketAddress("localhost", port);

    try (var socket = new ServerSocket(port, 10, localhost.getAddress())) {
      socket.setPerformancePreferences(0, 1, 0);
      socket.setSoTimeout(1_000);

      LOG.info("[{}] listen", socket.getLocalSocketAddress());
      while (true) {
        try {
          final var clientSocket = socket.accept();
          clientSocket.setKeepAlive(true);
          clientSocket.setPerformancePreferences(0, 1, 0);
          clientSocket.setTcpNoDelay(true);
          clientSocket.setTrafficClass(0x02 | 0x10);

          final var clientAddress =
            clientSocket.getRemoteSocketAddress();

          LOG.info("[{}] client connect", clientAddress);
          this.threadPool.execute(() -> {

            final var serverClient =
              new CBExServerClient<>(
                this,
                LOG,
                clientSocket,
                clientAddress,
                this.serializers,
                this.protocols,
                this.translators,
                this.core.get()
              );

            try (var client = serverClient) {
              client.execute();
            } catch (final EOFException e) {
              // Fine!
            } catch (final IOException e) {
              LOG.error("[{}] client crashed: ", clientAddress, e);
            }
          });
        } catch (final SocketTimeoutException e) {
          // Continue!
        }
      }
    }
  }

  public boolean isDone()
  {
    return this.state == STATE_DONE;
  }

  @Override
  public void close()
  {
    this.threadPool.shutdown();
  }

  enum State
  {
    STATE_INITIAL,
    STATE_RUNNING,
    STATE_DONE
  }
}
