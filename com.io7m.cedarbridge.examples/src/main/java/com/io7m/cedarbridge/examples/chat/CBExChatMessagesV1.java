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

import com.io7m.cedarbridge.examples.generic.CBExMessageTranslatorType;
import com.io7m.junreachable.UnreachableCodeException;

import static com.io7m.cedarbridge.runtime.api.CBCore.string;

public final class CBExChatMessagesV1
  implements CBExMessageTranslatorType<CBExChatMessageType, ProtocolChatv1Type>
{
  public CBExChatMessagesV1()
  {

  }

  @Override
  public ProtocolChatv1Type toWireNullable(
    final CBExChatMessageType x)
  {
    if (x instanceof CBChatCommandJoin) {
      final var xm = (CBChatCommandJoin) x;
      return new ChatCommandJoin(string(xm.name()));
    }
    if (x instanceof CBChatCommandSpeak) {
      final var xm = (CBChatCommandSpeak) x;
      return new ChatCommandSpeak(string(xm.message()));
    }
    if (x instanceof CBChatEventError) {
      final var xm = (CBChatEventError) x;
      return new ChatEventError(string(xm.message()));
    }
    if (x instanceof CBChatEventJoined) {
      final var xm = (CBChatEventJoined) x;
      return new ChatEventJoined(string(xm.user()));
    }
    if (x instanceof CBChatEventLeft) {
      final var xm = (CBChatEventLeft) x;
      return new ChatEventLeft(string(xm.user()));
    }
    if (x instanceof CBChatEventSpoke) {
      final var xm = (CBChatEventSpoke) x;
      return new ChatEventSpoke(string(xm.user()), string(xm.message()));
    }
    throw new UnreachableCodeException();
  }

  @Override
  public CBExChatMessageType fromWire(
    final ProtocolChatv1Type x)
  {
    if (x instanceof ChatCommandJoin) {
      final var xm = (ChatCommandJoin) x;
      return CBChatCommandJoin.builder()
        .setName(xm.fieldName().value())
        .build();
    }
    if (x instanceof ChatCommandSpeak) {
      final var xm = (ChatCommandSpeak) x;
      return CBChatCommandSpeak.builder()
        .setMessage(xm.fieldMessage().value())
        .build();
    }
    if (x instanceof ChatEventError) {
      final var xm = (ChatEventError) x;
      return CBChatEventError.builder()
        .setMessage(xm.fieldMessage().value())
        .build();
    }
    if (x instanceof ChatEventJoined) {
      final var xm = (ChatEventJoined) x;
      return CBChatEventJoined.builder()
        .setUser(xm.fieldUser().value())
        .build();
    }
    if (x instanceof ChatEventLeft) {
      final var xm = (ChatEventLeft) x;
      return CBChatEventLeft.builder()
        .setUser(xm.fieldUser().value())
        .build();
    }
    if (x instanceof ChatEventSpoke) {
      final var xm = (ChatEventSpoke) x;
      return CBChatEventSpoke.builder()
        .setUser(xm.fieldUser().value())
        .setMessage(xm.fieldMessage().value())
        .build();
    }
    throw new UnreachableCodeException();
  }
}
