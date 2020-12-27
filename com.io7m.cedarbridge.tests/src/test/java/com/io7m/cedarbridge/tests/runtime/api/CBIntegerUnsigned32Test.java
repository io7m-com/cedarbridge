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

import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned32;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBIntegerUnsigned32Test
{
  @Test
  public void testRange0()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      CBIntegerUnsigned32.of(-1L);
    });
  }

  @Test
  public void testRange1()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      CBIntegerUnsigned32.of(4294967296L);
    });
  }

  @Test
  public void testCompare()
  {
    assertEquals(
      0,
      CBIntegerUnsigned32.of(0L).compareTo(CBIntegerUnsigned32.of(0L))
    );
    assertEquals(
      1,
      CBIntegerUnsigned32.of(1L).compareTo(CBIntegerUnsigned32.of(0L))
    );
    assertEquals(
      -1,
      CBIntegerUnsigned32.of(0L).compareTo(CBIntegerUnsigned32.of(1L))
    );
  }

  @Test
  public void testFormat()
  {
    assertEquals(
      "23",
      String.format("%s", CBIntegerUnsigned32.of(23L))
    );
  }
}