/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.runtime.bssio.CBSerializationContextBSSIO;
import com.io7m.cedarbridge.tests.identity.ProtocolIdentity;
import com.io7m.cedarbridge.tests.identity.ProtocolIdentityv1Type;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class CBIdentityTest
{
  @Property(tries = 5000)
  public void testSerialization(
    @ForAll final ProtocolIdentityv1Type m)
    throws Exception
  {
    final var messages =
      new ProtocolIdentity();
    final var serializer =
      messages.serializerForProtocolVersion(1L).orElseThrow();

    final var byteArrayOut =
      new ByteArrayOutputStream();

    final var writerContext =
      CBSerializationContextBSSIO.createFromOutputStream(
        new BSSWriters(),
        byteArrayOut
      );

    serializer.serialize(writerContext, m);

    final var byteArrayIn = byteArrayOut.toByteArray();
    assertNotEquals(0, byteArrayIn.length);

    final var readerContext =
      CBSerializationContextBSSIO.createFromByteArray(
        new BSSReaders(),
        byteArrayIn
      );

    final var r = serializer.deserialize(readerContext);
    assertEquals(m, r);
  }
}
