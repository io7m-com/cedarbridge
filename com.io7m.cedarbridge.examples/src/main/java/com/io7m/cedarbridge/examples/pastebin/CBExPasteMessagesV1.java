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

package com.io7m.cedarbridge.examples.pastebin;


import com.io7m.cedarbridge.examples.generic.CBExMessageTranslatorType;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.UUID;

import static com.io7m.cedarbridge.runtime.api.CBCore.string;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned64;

public final class CBExPasteMessagesV1
  implements CBExMessageTranslatorType<CBExPasteMessageType, ProtocolPastev1Type>
{
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
    if (x instanceof CBError) {
      final var xm = (CBError) x;
      return new Error(string(xm.message()));
    }
    if (x instanceof CBPasteCreate) {
      final var xm = (CBPasteCreate) x;
      return new PasteCreate(string(xm.key()), string(xm.text()));
    }
    if (x instanceof CBPasteCreated) {
      final var xm = (CBPasteCreated) x;
      return new PasteCreated(pasteId(xm.id()));
    }
    if (x instanceof CBPasteDelete) {
      final var xm = (CBPasteDelete) x;
      return new PasteDelete(pasteId(xm.id()), string(xm.key()));
    }
    if (x instanceof CBPasteDeleted) {
      final var xm = (CBPasteDeleted) x;
      return new PasteDeleted(pasteId(xm.id()));
    }
    if (x instanceof CBPasteGet) {
      final var xm = (CBPasteGet) x;
      return new PasteGet(pasteId(xm.id()));
    }
    if (x instanceof CBPasteGetResult) {
      final var xm = (CBPasteGetResult) x;
      return new PasteGetResult(pasteId(xm.id()), string(xm.text()));
    }
    throw new UnreachableCodeException();
  }

  @Override
  public CBExPasteMessageType fromWire(
    final ProtocolPastev1Type x)
  {
    if (x instanceof Error) {
      final var xm = (Error) x;
      return CBError.builder()
        .setMessage(xm.fieldMessage().value())
        .build();
    }
    if (x instanceof PasteCreate) {
      final var xm = (PasteCreate) x;
      return CBPasteCreate.builder()
        .setKey(xm.fieldKey().value())
        .setText(xm.fieldText().value())
        .build();
    }
    if (x instanceof PasteCreated) {
      final var xm = (PasteCreated) x;
      final var idMsb = xm.fieldId().fieldMsb().value();
      final var idLsb = xm.fieldId().fieldLsb().value();
      return CBPasteCreated.builder()
        .setId(new UUID(idMsb, idLsb))
        .build();
    }
    if (x instanceof PasteDelete) {
      final var xm = (PasteDelete) x;
      final var idMsb = xm.fieldPasteId().fieldMsb().value();
      final var idLsb = xm.fieldPasteId().fieldLsb().value();
      return CBPasteDelete.builder()
        .setId(new UUID(idMsb, idLsb))
        .setKey(xm.fieldKey().value())
        .build();
    }
    if (x instanceof PasteDeleted) {
      final var xm = (PasteDeleted) x;
      final var idMsb = xm.fieldId().fieldMsb().value();
      final var idLsb = xm.fieldId().fieldLsb().value();
      return CBPasteDeleted.builder()
        .setId(new UUID(idMsb, idLsb))
        .build();
    }
    if (x instanceof PasteGet) {
      final var xm = (PasteGet) x;
      final var idMsb = xm.fieldId().fieldMsb().value();
      final var idLsb = xm.fieldId().fieldLsb().value();
      return CBPasteGet.builder()
        .setId(new UUID(idMsb, idLsb))
        .build();
    }
    if (x instanceof PasteGetResult) {
      final var xm = (PasteGetResult) x;
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
