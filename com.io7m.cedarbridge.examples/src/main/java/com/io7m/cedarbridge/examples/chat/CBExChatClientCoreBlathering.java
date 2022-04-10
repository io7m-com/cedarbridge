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

package com.io7m.cedarbridge.examples.chat;

import com.io7m.cedarbridge.examples.generic.CBExClientCoreType;
import com.io7m.cedarbridge.examples.generic.CBExSocket;
import com.io7m.junreachable.UnreachableCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * A chat client that connects and blathers nonsense endlessly.
 */

public final class CBExChatClientCoreBlathering
  implements CBExClientCoreType<CBExChatMessageType>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBExChatClientCoreBlathering.class);

  private static final DateTimeFormatter FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  /**
   * A chat client that connects and blathers nonsense endlessly.
   */

  public CBExChatClientCoreBlathering()
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
        .setName("blathering")
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

    var timeThen = LocalDateTime.now();
    final var waitDuration = Duration.ofSeconds(3L);

    while (true) {
      final var timeNow = LocalDateTime.now();
      final var elapsed = Duration.between(timeThen, timeNow);

      if (elapsed.compareTo(waitDuration) > 0) {
        LOG.debug("[send]");
        timeThen = timeNow;
        socket.write(
          CBChatCommandSpeak.builder()
            .setMessage(String.format(
              "The time is now %s",
              FORMATTER.format(timeNow)))
            .build()
        );
      }

      final var messageOpt = socket.read();
      if (messageOpt.isPresent()) {
        final var message = messageOpt.get();
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

      try {
        Thread.sleep(1_000L);
      } catch (final InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
