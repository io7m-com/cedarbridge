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

package com.io7m.cedarbridge.examples.chat;

import com.io7m.cedarbridge.examples.generic.CBExServerClientCoreType;
import com.io7m.cedarbridge.examples.generic.CBExSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * A single server client.
 */

public final class CBExChatServerClientCore
  implements CBExServerClientCoreType<CBExChatMessageType>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBExChatServerClientCore.class);

  /**
   * A single server client.
   */

  public CBExChatServerClientCore()
  {

  }

  @Override
  public void execute(
    final CBExSocket<CBExChatMessageType, ?> socket)
    throws IOException
  {
    Objects.requireNonNull(socket, "socket");

    final var chat = CBExChat.get();

    /*
     * Read a JOIN command. Any other command results in an error.
     */

    final var joinCommand = socket.readBlocking(CBChatCommandJoin.class);
    final CBExChat.Session session;
    try {
      session = chat.sessionCreateNew(joinCommand.name());
    } catch (final CBExChat.ChatException e) {
      socket.write(
        CBChatEventError.builder()
          .setMessage(e.getMessage())
          .build()
      );
      return;
    }

    LOG.info("{} joined", joinCommand.name());

    try {

      /*
       * Read commands and execute them.
       */

      while (true) {
        final var command = socket.read();
        if (command.isPresent()) {
          this.executeCommand(socket, session, command.get());
        }

        while (true) {
          final var eventOpt = session.takeEvent();
          if (eventOpt.isPresent()) {
            final var event = eventOpt.get();
            LOG.debug("sending: {}", event);
            socket.write(event);
          } else {
            break;
          }
        }
      }
    } finally {
      session.close();
    }
  }

  private void executeCommand(
    final CBExSocket<CBExChatMessageType, ?> socket,
    final CBExChat.Session session,
    final CBExChatMessageType command)
    throws IOException
  {
    LOG.debug("received: {}", command);

    if (command instanceof CBChatCommandJoin) {
      socket.write(
        CBChatEventError.builder()
          .setMessage("Already joined!")
          .build()
      );
      return;
    }

    if (command instanceof CBChatCommandSpeak) {
      final var speakCommand = (CBChatCommandSpeak) command;
      session.speak(speakCommand.message());
      return;
    }
  }
}
