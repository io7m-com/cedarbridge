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

package com.io7m.cedarbridge.tests.runtime.api;

import org.junit.jupiter.api.Test;

import static com.io7m.cedarbridge.runtime.api.CBCore.float16;
import static com.io7m.cedarbridge.runtime.api.CBCore.float32;
import static com.io7m.cedarbridge.runtime.api.CBCore.float64;
import static com.io7m.cedarbridge.runtime.api.CBCore.signed16;
import static com.io7m.cedarbridge.runtime.api.CBCore.signed32;
import static com.io7m.cedarbridge.runtime.api.CBCore.signed64;
import static com.io7m.cedarbridge.runtime.api.CBCore.signed8;
import static com.io7m.cedarbridge.runtime.api.CBCore.string;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned16;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned32;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned64;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CBCoreTest
{
  @Test
  public void testConvenience()
  {
    assertEquals(23, unsigned16(23).value());
    assertEquals(23, unsigned16(23L).value());
    assertEquals(23, unsigned8(23).value());
    assertEquals(23, unsigned8(23L).value());
    assertEquals(23L, unsigned32(23).value());
    assertEquals(23L, unsigned32(23L).value());
    assertEquals(23L, unsigned64(23L).value());

    assertEquals(23, signed16(23).value());
    assertEquals(23, signed16(23L).value());
    assertEquals(23, signed8(23).value());
    assertEquals(23, signed8(23L).value());
    assertEquals(23, signed32(23).value());
    assertEquals(23, signed32(23L).value());
    assertEquals(23L, signed64(23L).value());

    assertEquals("23", string("23").value());
    assertEquals(23.0, float16(23.0).value());
    assertEquals(23.0, float32(23.0).value());
    assertEquals(23.0, float64(23.0).value());
  }
}
