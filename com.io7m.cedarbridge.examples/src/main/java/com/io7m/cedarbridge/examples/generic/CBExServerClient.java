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

package com.io7m.cedarbridge.examples.generic;

import com.io7m.cedarbridge.runtime.api.CBProtocolMessageType;
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerCollectionType;
import com.io7m.cedarbridge.runtime.api.CBSerializerDirectoryType;
import com.io7m.cedarbridge.runtime.container_protocol.CBContainerProtocolAvailable;
import com.io7m.cedarbridge.runtime.container_protocol.CBContainerProtocolMessages;
import com.io7m.cedarbridge.runtime.container_protocol.CBContainerProtocolResponse;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Objects;

/**
 * A client on the server. The clients read and writes messages of type
 * {@code M}, converting them to messages of type {@code P} for transfer on
 * the wire.
 *
 * @param <M> The application-level message types
 * @param <P> The wire-level message types
 */

public final class CBExServerClient<M, P extends CBProtocolMessageType>
  implements Closeable
{
  private final CBExServer<M, P> server;
  private final Logger logger;
  private final Socket socket;
  private final SocketAddress clientAddress;
  private final CBSerializerDirectoryType serializers;
  private final CBProtocolSerializerCollectionType<P> protocols;
  private final CBExMessageTranslatorDirectory<M, P> translators;
  private final CBExServerClientCoreType<M> core;

  /**
   * Create a client.
   *
   * @param inServer        The server
   * @param inLogger        The logger
   * @param inSocket        The client socket
   * @param inClientAddress The client address
   * @param inSerializers   The serializer directory
   * @param inProtocols     The protocol directory
   * @param inTranslators   The translator directory
   * @param inCore          The client core
   */

  public CBExServerClient(
    final CBExServer<M, P> inServer,
    final Logger inLogger,
    final Socket inSocket,
    final SocketAddress inClientAddress,
    final CBSerializerDirectoryType inSerializers,
    final CBProtocolSerializerCollectionType<P> inProtocols,
    final CBExMessageTranslatorDirectory<M, P> inTranslators,
    final CBExServerClientCoreType<M> inCore)
  {
    this.server =
      Objects.requireNonNull(inServer, "server");
    this.logger =
      Objects.requireNonNull(inLogger, "log");
    this.socket =
      Objects.requireNonNull(inSocket, "socket");
    this.clientAddress =
      Objects.requireNonNull(inClientAddress, "clientAddress");
    this.serializers =
      Objects.requireNonNull(inSerializers, "inSerializers");
    this.protocols =
      Objects.requireNonNull(inProtocols, "inProtocols");
    this.translators =
      Objects.requireNonNull(inTranslators, "inHandlers");
    this.core =
      Objects.requireNonNull(inCore, "inCore");
  }

  @Override
  public void close()
    throws IOException
  {
    this.socket.close();
  }

  /**
   * Execute the client.
   *
   * @throws IOException On I/O errors
   */

  public void execute()
    throws IOException
  {
    try {
      final var input =
        this.socket.getInputStream();
      final var output =
        this.socket.getOutputStream();

      output.write(
        CBContainerProtocolMessages.serializeAvailableAsBytes(
          new CBContainerProtocolAvailable(
            1L,
            1L,
            this.protocols.id(),
            this.protocols.versionLower(),
            this.protocols.versionUpper()
          )
        )
      );

      final var useBuffer =
        input.readNBytes(CBContainerProtocolMessages.sizeUse());
      final var useMessage =
        CBContainerProtocolMessages.parseUse(useBuffer);

      try {
        this.protocols.checkSupportedVersion(
          useMessage.applicationProtocolId(),
          useMessage.applicationProtocolVersion()
        );
      } catch (final IllegalArgumentException e) {
        output.write(
          CBContainerProtocolMessages.serializeResponseAsBytes(
            new CBContainerProtocolResponse(false, e.getMessage())
          )
        );
        this.socket.close();
        return;
      }

      this.logger.info(
        "[{}] client protocol {} version {}",
        this.clientAddress,
        useMessage.applicationProtocolId(),
        Long.valueOf(useMessage.applicationProtocolVersion())
      );

      output.write(
        CBContainerProtocolMessages.serializeResponseAsBytes(
          new CBContainerProtocolResponse(true, "")
        )
      );

      final var protocolFactory =
        this.protocols.findOrThrow(useMessage.applicationProtocolVersion());
      final var protocol =
        protocolFactory.create(this.serializers);

      this.socket.setSoTimeout(10);

      final var exSocket =
        new CBExSocket<>(
          new BSSReaders(),
          new BSSWriters(),
          this.socket,
          this.translators.get(useMessage.applicationProtocolVersion()),
          protocol
        );

      this.core.execute(exSocket);
    } finally {
      this.logger.info("[{}] client disconnect", this.clientAddress);
    }
  }
}
