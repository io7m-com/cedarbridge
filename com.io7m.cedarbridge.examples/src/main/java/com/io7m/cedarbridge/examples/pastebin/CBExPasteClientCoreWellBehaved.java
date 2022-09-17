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

import com.io7m.cedarbridge.examples.generic.CBExClientCoreType;
import com.io7m.cedarbridge.examples.generic.CBExSocket;
import com.io7m.junreachable.UnreachableCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * A client that creates a paste, retrieves it, and then deletes it.
 */

public final class CBExPasteClientCoreWellBehaved
  implements CBExClientCoreType<CBExPasteMessageType>
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBExPasteClientCoreWellBehaved.class);

  /**
   * A client that creates a paste, retrieves it, and then deletes it.
   */

  public CBExPasteClientCoreWellBehaved()
  {

  }

  @Override
  public void execute(
    final CBExSocket<CBExPasteMessageType, ?> socket)
    throws IOException
  {
    Objects.requireNonNull(socket, "socket");

    socket.write(
      CBPasteCreate.builder()
        .setKey("Strong Password")
        .setText(
          "Congratulations can wait until we are at minimum safe distance.")
        .build()
    );

    final UUID pasteId;
    final var pasteCreateResponse = socket.readBlocking();
    if (pasteCreateResponse instanceof CBError) {
      LOG.error("paste failed: {}", ((CBError) pasteCreateResponse).message());
      return;
    } else if (pasteCreateResponse instanceof CBPasteCreated) {
      pasteId = ((CBPasteCreated) pasteCreateResponse).id();
    } else {
      throw new UnreachableCodeException();
    }

    socket.write(
      CBPasteGet.builder()
        .setId(pasteId)
        .build()
    );

    final var pasteGetResponse = socket.readBlocking();
    if (pasteGetResponse instanceof CBError) {
      LOG.error(
        "paste retrieval failed: {}",
        ((CBError) pasteGetResponse).message());
      return;
    } else if (pasteGetResponse instanceof CBPasteGetResult) {
      LOG.debug("paste text: {}", ((CBPasteGetResult) pasteGetResponse).text());
    } else {
      throw new UnreachableCodeException();
    }

    socket.write(
      CBPasteDelete.builder()
        .setId(pasteId)
        .setKey("Strong Password")
        .build()
    );

    final var pasteDeleteResponse = socket.readBlocking();
    if (pasteDeleteResponse instanceof CBError) {
      LOG.error(
        "paste deletion failed: {}",
        ((CBError) pasteGetResponse).message());
      return;
    } else if (pasteDeleteResponse instanceof CBPasteDeleted) {
      LOG.debug("paste deleted");
    } else {
      throw new UnreachableCodeException();
    }

    LOG.debug("finished");
  }
}
