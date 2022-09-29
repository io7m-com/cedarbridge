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

package com.io7m.cedarbridge.runtime.api;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/**
 * The unique coordinates for a protocol version.
 *
 * @param id      The protocol ID
 * @param version The protocol version
 */

public record CBProtocolCoordinates(
  UUID id,
  BigInteger version)
  implements Comparable<CBProtocolCoordinates>
{
  /**
   * The unique coordinates for a protocol version.
   *
   * @param id      The protocol ID
   * @param version The protocol version
   */

  public CBProtocolCoordinates
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(version, "version");
  }

  @Override
  public int compareTo(
    final CBProtocolCoordinates other)
  {
    return Comparator.comparing(CBProtocolCoordinates::id)
      .thenComparing(CBProtocolCoordinates::version)
      .compare(this, other);
  }
}
