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

import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned8;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBIntegerUnsigned8Test
{
  @Test
  public void testRange0()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      CBIntegerUnsigned8.of(-1);
    });
  }

  @Test
  public void testRange1()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      CBIntegerUnsigned8.of(256);
    });
  }

  @Test
  public void testCompare()
  {
    assertEquals(
      0,
      CBIntegerUnsigned8.of(0).compareTo(CBIntegerUnsigned8.of(0))
    );
    assertEquals(
      1,
      CBIntegerUnsigned8.of(1).compareTo(CBIntegerUnsigned8.of(0))
    );
    assertEquals(
      -1,
      CBIntegerUnsigned8.of(0).compareTo(CBIntegerUnsigned8.of(1))
    );
  }

  @Test
  public void testFormat()
  {
    assertEquals(
      "23",
      String.format("%s", CBIntegerUnsigned8.of(23))
    );
  }
}
