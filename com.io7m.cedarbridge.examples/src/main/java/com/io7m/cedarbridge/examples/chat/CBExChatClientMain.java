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

import com.io7m.cedarbridge.examples.generic.CBExClient;
import com.io7m.cedarbridge.examples.generic.CBExClientCoreType;
import com.io7m.cedarbridge.examples.generic.CBExMessageTranslatorDirectory;

/**
 * The main client.
 */

public final class CBExChatClientMain
{
  private CBExChatClientMain()
  {

  }

  /**
   * The main command-line entry point.
   *
   * @param args The command-line arguments
   */

  public static void main(
    final String[] args)
  {
    CBExClientCoreType<CBExChatMessageType> core =
      new CBExChatClientCoreLurker();

    if (args.length > 0) {
      switch (args[0]) {
        case "lurker" -> {
          core = new CBExChatClientCoreLurker();
        }
        case "blathering" -> {
          core = new CBExChatClientCoreBlathering();
        }
        default -> {
          throw new IllegalArgumentException(
            String.format("Unrecognized core: %s", args[0])
          );
        }
      }
    }

    final var protocols =
      new ProtocolChat();
    final var translators =
      new CBExMessageTranslatorDirectory<CBExChatMessageType, ProtocolChatType>();
    translators.addTranslator(1L, new CBExChatMessagesV1());

    try (var client = new CBExClient<>(protocols, translators, core)) {
      client.start();
      while (!client.isDone()) {
        try {
          Thread.sleep(1_000L);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
