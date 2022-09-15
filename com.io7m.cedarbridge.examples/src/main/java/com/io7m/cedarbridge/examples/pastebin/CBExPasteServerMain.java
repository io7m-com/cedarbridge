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

import com.io7m.cedarbridge.examples.generic.CBExMessageTranslatorDirectory;
import com.io7m.cedarbridge.examples.generic.CBExServer;
import com.io7m.cedarbridge.runtime.api.CBCoreSerializers;
import com.io7m.cedarbridge.runtime.api.CBSerializerDirectoryMutable;

/**
 * The main pastebin server.
 */

public final class CBExPasteServerMain
{
  private CBExPasteServerMain()
  {

  }

  /**
   * The main pastebin server.
   *
   * @param args The command-line arguments
   */

  public static void main(
    final String[] args)
  {
    final var serializers = new CBSerializerDirectoryMutable();
    serializers.addCollection(CBCoreSerializers.get());
    serializers.addCollection(Serializers.get());

    final var protocols =
      ProtocolPaste.factories();

    final var translators =
      new CBExMessageTranslatorDirectory<CBExPasteMessageType, ProtocolPasteType>();
    translators.addTranslator(1L, new CBExPasteMessagesV1());

    try (var server = new CBExServer<>(
      serializers, protocols, translators, CBExPasteServerClientCore::new)) {
      server.start();
      while (!server.isDone()) {
        try {
          Thread.sleep(1_000L);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
