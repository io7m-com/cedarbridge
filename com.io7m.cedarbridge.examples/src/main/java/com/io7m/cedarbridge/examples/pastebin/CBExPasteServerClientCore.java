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

package com.io7m.cedarbridge.examples.pastebin;

import com.io7m.cedarbridge.examples.generic.CBExServerClientCoreType;
import com.io7m.cedarbridge.examples.generic.CBExSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * A client on a pastebin server.
 */

public final class CBExPasteServerClientCore
  implements CBExServerClientCoreType<CBExPasteMessageType>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBExPasteServerClientCore.class);

  /**
   * A client on a pastebin server.
   */

  public CBExPasteServerClientCore()
  {

  }

  private static void executeCommandPasteDelete(
    final CBExSocket<CBExPasteMessageType, ?> socket,
    final CBPasteDelete command)
    throws IOException
  {
    final var pasteBin =
      CBExPastebin.get();
    final var pasteOpt =
      pasteBin.find(command.id());

    if (pasteOpt.isPresent()) {
      final var paste = pasteOpt.get();
      if (Objects.equals(command.key(), paste.key())) {
        pasteBin.delete(command.id());
        socket.write(
          CBPasteDeleted.builder()
            .setId(paste.id())
            .build()
        );
        return;
      }
    }

    socket.write(
      CBError.builder()
        .setMessage("No such paste")
        .build()
    );
  }

  private static void executeCommandPasteGet(
    final CBExSocket<CBExPasteMessageType, ?> socket,
    final CBPasteGet command)
    throws IOException
  {
    final var pasteBin =
      CBExPastebin.get();
    final var pasteOpt =
      pasteBin.find(command.id());

    if (pasteOpt.isPresent()) {
      final var paste = pasteOpt.get();
      socket.write(
        CBPasteGetResult.builder()
          .setId(paste.id())
          .setText(paste.text())
          .build()
      );
      return;
    }

    socket.write(
      CBError.builder()
        .setMessage("No such paste")
        .build()
    );
  }

  private static void executeCommandPasteCreate(
    final CBExSocket<CBExPasteMessageType, ?> socket,
    final CBPasteCreate command)
    throws IOException
  {
    final var pasteBin =
      CBExPastebin.get();
    final var paste =
      pasteBin.create(command.key(), command.text());

    socket.write(
      CBPasteCreated.builder()
        .setId(paste.id())
        .build()
    );
  }

  @Override
  public void execute(
    final CBExSocket<CBExPasteMessageType, ?> socket)
    throws IOException
  {
    Objects.requireNonNull(socket, "socket");

    while (true) {
      LOG.info("pastes {}", Integer.valueOf(CBExPastebin.get().size()));

      final var command = socket.readBlocking();
      if (command instanceof CBPasteCreate) {
        executeCommandPasteCreate(socket, (CBPasteCreate) command);
      } else if (command instanceof CBPasteGet) {
        executeCommandPasteGet(socket, (CBPasteGet) command);
      } else if (command instanceof CBPasteDelete) {
        executeCommandPasteDelete(socket, (CBPasteDelete) command);
      }
    }
  }
}
