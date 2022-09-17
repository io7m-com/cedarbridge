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

import com.io7m.cedarbridge.examples.generic.CBExMessageTranslatorType;
import com.io7m.junreachable.UnreachableCodeException;

import static com.io7m.cedarbridge.runtime.api.CBCore.string;

/**
 * A mapping from protocol v1 messages to the core message protocol.
 */

public final class CBExChatMessagesV1
  implements CBExMessageTranslatorType<CBExChatMessageType, ProtocolChatv1Type>
{
  /**
   * A mapping from protocol v1 messages to the core message protocol.
   */

  public CBExChatMessagesV1()
  {

  }

  @Override
  public ProtocolChatv1Type toWireNullable(
    final CBExChatMessageType x)
  {
    if (x instanceof CBChatCommandJoin xm) {
      return new ChatCommandJoin(string(xm.name()));
    }
    if (x instanceof CBChatCommandSpeak xm) {
      return new ChatCommandSpeak(string(xm.message()));
    }
    if (x instanceof CBChatEventError xm) {
      return new ChatEventError(string(xm.message()));
    }
    if (x instanceof CBChatEventJoined xm) {
      return new ChatEventJoined(string(xm.user()));
    }
    if (x instanceof CBChatEventLeft xm) {
      return new ChatEventLeft(string(xm.user()));
    }
    if (x instanceof CBChatEventSpoke xm) {
      return new ChatEventSpoke(string(xm.user()), string(xm.message()));
    }
    throw new UnreachableCodeException();
  }

  @Override
  public CBExChatMessageType fromWire(
    final ProtocolChatv1Type x)
  {
    if (x instanceof ChatCommandJoin xm) {
      return CBChatCommandJoin.builder()
        .setName(xm.fieldName().value())
        .build();
    }
    if (x instanceof ChatCommandSpeak xm) {
      return CBChatCommandSpeak.builder()
        .setMessage(xm.fieldMessage().value())
        .build();
    }
    if (x instanceof ChatEventError xm) {
      return CBChatEventError.builder()
        .setMessage(xm.fieldMessage().value())
        .build();
    }
    if (x instanceof ChatEventJoined xm) {
      return CBChatEventJoined.builder()
        .setUser(xm.fieldUser().value())
        .build();
    }
    if (x instanceof ChatEventLeft xm) {
      return CBChatEventLeft.builder()
        .setUser(xm.fieldUser().value())
        .build();
    }
    if (x instanceof ChatEventSpoke xm) {
      return CBChatEventSpoke.builder()
        .setUser(xm.fieldUser().value())
        .setMessage(xm.fieldMessage().value())
        .build();
    }
    throw new UnreachableCodeException();
  }
}
