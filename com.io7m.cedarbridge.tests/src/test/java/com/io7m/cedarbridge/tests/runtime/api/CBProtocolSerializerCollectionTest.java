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

package com.io7m.cedarbridge.tests.runtime.api;

import com.io7m.cedarbridge.runtime.api.CBProtocolMessageType;
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerCollection;
import com.io7m.cedarbridge.tests.runtime.FakeProtocolSerializerFactory;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CBProtocolSerializerCollectionTest
{
  @Test
  public void testEmpty()
  {
    final var ex =
      assertThrows(IllegalArgumentException.class, () -> {
        CBProtocolSerializerCollection.builder(UUID.randomUUID())
          .build();
      });

    assertTrue(ex.getMessage().contains("empty"));
  }

  @Test
  public void testBadId()
  {
    final var id = UUID.randomUUID();

    final var ex =
      assertThrows(IllegalArgumentException.class, () -> {
        CBProtocolSerializerCollection.builder(id)
          .addFactory(new FakeProtocolSerializerFactory(
            UUID.randomUUID(), 1L, CBProtocolMessageType.class))
          .build();
      });

    assertTrue(ex.getMessage().contains("incorrect ID"));
  }

  @Test
  public void testVersionConflict()
  {
    final var id = UUID.randomUUID();
    final var fake0 =
      new FakeProtocolSerializerFactory(id, 1L, CBProtocolMessageType.class);
    final var fake1 =
      new FakeProtocolSerializerFactory(id, 1L, CBProtocolMessageType.class);

    final var ex =
      assertThrows(IllegalArgumentException.class, () -> {
        CBProtocolSerializerCollection.builder(id)
          .addFactory(fake0)
          .addFactory(fake1)
          .build();
      });

    assertTrue(ex.getMessage().contains("conflict"));
  }

  @Test
  public void testRangeMalformed()
  {
    final var id = UUID.randomUUID();
    final var fake0 =
      new FakeProtocolSerializerFactory(id, 1L, CBProtocolMessageType.class);
    final var fake1 =
      new FakeProtocolSerializerFactory(id, 2L, CBProtocolMessageType.class);
    final var fake2 =
      new FakeProtocolSerializerFactory(id, 3L, CBProtocolMessageType.class);

    final var collection =
      CBProtocolSerializerCollection.builder(id)
        .addFactory(fake0)
        .addFactory(fake1)
        .addFactory(fake2)
        .build();

    final var ex =
      assertThrows(IllegalArgumentException.class, () -> {
        collection.highestSupportedVersion(id, 1L, 0L);
      });

    assertTrue(ex.getMessage().contains("must be >= requested"));
  }

  @Test
  public void testSupported()
  {
    final var id = UUID.randomUUID();
    final var fake0 =
      new FakeProtocolSerializerFactory(id, 1L, CBProtocolMessageType.class);
    final var fake1 =
      new FakeProtocolSerializerFactory(id, 2L, CBProtocolMessageType.class);
    final var fake2 =
      new FakeProtocolSerializerFactory(id, 3L, CBProtocolMessageType.class);

    final var collection =
      CBProtocolSerializerCollection.builder(id)
        .addFactory(fake0)
        .addFactory(fake1)
        .addFactory(fake2)
        .build();

    assertEquals(1L, collection.versionLower());
    assertEquals(3L, collection.versionUpper());
    assertEquals(id, collection.id());
    assertTrue(collection.factories().contains(fake0));
    assertTrue(collection.factories().contains(fake1));
    assertTrue(collection.factories().contains(fake2));
    assertEquals(3, collection.factories().size());

    assertEquals(1L, collection.highestSupportedVersion(id, 0L, 1L));
    assertEquals(2L, collection.highestSupportedVersion(id, 0L, 2L));
    assertEquals(3L, collection.highestSupportedVersion(id, 0L, 3L));
    assertEquals(3L, collection.highestSupportedVersion(id, 3L, 4L));
    assertEquals(3L, collection.checkSupportedVersion(id, 3L));

    assertEquals(fake0, collection.findOrThrow(CBProtocolMessageType.class));
  }

  @Test
  public void testNotSupported0()
  {
    final var id = UUID.randomUUID();
    final var fake0 =
      new FakeProtocolSerializerFactory(id, 1L, CBProtocolMessageType.class);
    final var fake1 =
      new FakeProtocolSerializerFactory(id, 2L, CBProtocolMessageType.class);
    final var fake2 =
      new FakeProtocolSerializerFactory(id, 3L, CBProtocolMessageType.class);

    final var collection =
      CBProtocolSerializerCollection.builder(id)
        .addFactory(fake0)
        .addFactory(fake1)
        .addFactory(fake2)
        .build();

    assertThrows(IllegalArgumentException.class, () -> {
      collection.checkSupportedVersion(UUID.randomUUID(), 1L);
    });
    assertThrows(IllegalArgumentException.class, () -> {
      collection.checkSupportedVersion(id, 0L);
    });
    assertThrows(IllegalArgumentException.class, () -> {
      collection.highestSupportedVersion(id, 4L, 5L);
    });
  }
}
