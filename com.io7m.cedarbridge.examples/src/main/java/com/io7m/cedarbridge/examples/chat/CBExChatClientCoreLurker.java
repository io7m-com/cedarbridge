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

import com.io7m.cedarbridge.examples.generic.CBExClientCoreType;
import com.io7m.cedarbridge.examples.generic.CBExSocket;
import com.io7m.junreachable.UnreachableCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * A chat client that connects and says nothing.
 */

public final class CBExChatClientCoreLurker
  implements CBExClientCoreType<CBExChatMessageType>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBExChatClientCoreLurker.class);

  /**
   * A chat client that connects and says nothing.
   */

  public CBExChatClientCoreLurker()
  {

  }

  @Override
  public void execute(
    final CBExSocket<CBExChatMessageType, ?> socket)
    throws IOException
  {
    Objects.requireNonNull(socket, "socket");

    socket.write(
      CBChatCommandJoin.builder()
        .setName("lurker")
        .build()
    );

    final var joinResponse = socket.readBlocking();
    if (joinResponse instanceof CBChatEventError) {
      final var error = (CBChatEventError) joinResponse;
      LOG.error("failed to join: {}", error.message());
      return;
    } else if (joinResponse instanceof CBChatEventJoined) {
      LOG.info("joined");
    } else {
      throw new UnreachableCodeException();
    }

    while (true) {
      final var message = socket.readBlocking();
      if (message instanceof CBChatEventSpoke) {
        final var spoke = (CBChatEventSpoke) message;
        LOG.info("[spoke] {}: {}", spoke.user(), spoke.message());
      } else if (message instanceof CBChatEventJoined) {
        final var joined = (CBChatEventJoined) message;
        LOG.info("[joined] {}", joined.user());
      } else if (message instanceof CBChatEventLeft) {
        final var left = (CBChatEventLeft) message;
        LOG.info("[left] {}", left.user());
      }
    }
  }
}
