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


import com.io7m.cedarbridge.examples.generic.CBExMessageTranslatorType;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.UUID;

import static com.io7m.cedarbridge.runtime.api.CBCore.string;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned64;

/**
 * A translator between the core application messages and the protocol v1 messages.
 */

public final class CBExPasteMessagesV1
  implements CBExMessageTranslatorType<CBExPasteMessageType, ProtocolPastev1Type>
{
  /**
   * A translator between the core application messages and the protocol v1 messages.
   */

  public CBExPasteMessagesV1()
  {

  }

  private static PasteID pasteId(
    final UUID id)
  {
    return new PasteID(
      unsigned64(id.getMostSignificantBits()),
      unsigned64(id.getLeastSignificantBits())
    );
  }

  @Override
  public ProtocolPastev1Type toWireNullable(
    final CBExPasteMessageType x)
  {
    if (x instanceof CBError xm) {
      return new Error(string(xm.message()));
    }
    if (x instanceof CBPasteCreate xm) {
      return new PasteCreate(string(xm.key()), string(xm.text()));
    }
    if (x instanceof CBPasteCreated xm) {
      return new PasteCreated(pasteId(xm.id()));
    }
    if (x instanceof CBPasteDelete xm) {
      return new PasteDelete(pasteId(xm.id()), string(xm.key()));
    }
    if (x instanceof CBPasteDeleted xm) {
      return new PasteDeleted(pasteId(xm.id()));
    }
    if (x instanceof CBPasteGet xm) {
      return new PasteGet(pasteId(xm.id()));
    }
    if (x instanceof CBPasteGetResult xm) {
      return new PasteGetResult(pasteId(xm.id()), string(xm.text()));
    }
    throw new UnreachableCodeException();
  }

  @Override
  public CBExPasteMessageType fromWire(
    final ProtocolPastev1Type x)
  {
    if (x instanceof Error xm) {
      return CBError.builder()
        .setMessage(xm.fieldMessage().value())
        .build();
    }
    if (x instanceof PasteCreate xm) {
      return CBPasteCreate.builder()
        .setKey(xm.fieldKey().value())
        .setText(xm.fieldText().value())
        .build();
    }
    if (x instanceof PasteCreated xm) {
      final var idMsb = xm.fieldId().fieldMsb().value();
      final var idLsb = xm.fieldId().fieldLsb().value();
      return CBPasteCreated.builder()
        .setId(new UUID(idMsb, idLsb))
        .build();
    }
    if (x instanceof PasteDelete xm) {
      final var idMsb = xm.fieldPasteId().fieldMsb().value();
      final var idLsb = xm.fieldPasteId().fieldLsb().value();
      return CBPasteDelete.builder()
        .setId(new UUID(idMsb, idLsb))
        .setKey(xm.fieldKey().value())
        .build();
    }
    if (x instanceof PasteDeleted xm) {
      final var idMsb = xm.fieldId().fieldMsb().value();
      final var idLsb = xm.fieldId().fieldLsb().value();
      return CBPasteDeleted.builder()
        .setId(new UUID(idMsb, idLsb))
        .build();
    }
    if (x instanceof PasteGet xm) {
      final var idMsb = xm.fieldId().fieldMsb().value();
      final var idLsb = xm.fieldId().fieldLsb().value();
      return CBPasteGet.builder()
        .setId(new UUID(idMsb, idLsb))
        .build();
    }
    if (x instanceof PasteGetResult xm) {
      final var idMsb = xm.fieldId().fieldMsb().value();
      final var idLsb = xm.fieldId().fieldLsb().value();
      return CBPasteGetResult.builder()
        .setId(new UUID(idMsb, idLsb))
        .setText(xm.fieldText().value())
        .build();
    }
    throw new UnreachableCodeException();
  }
}
