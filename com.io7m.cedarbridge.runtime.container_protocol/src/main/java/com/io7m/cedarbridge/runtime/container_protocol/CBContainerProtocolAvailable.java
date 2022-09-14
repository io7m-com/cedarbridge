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

package com.io7m.cedarbridge.runtime.container_protocol;

import java.util.Objects;
import java.util.UUID;

/**
 * An availability message, typically presented by a server, that announces the
 * available application protocol ID and version, and the version of the
 * container protocol used to serve it.
 *
 * @param containerProtocolMinimumVersion   The minimum supported container
 *                                          protocol version
 * @param containerProtocolMaximumVersion   The maximum supported container
 *                                          protocol version
 * @param applicationProtocolId             The application protocol ID
 * @param applicationProtocolMinimumVersion The minimum supported application
 *                                          protocol version
 * @param applicationProtocolMaximumVersion The maximum supported application
 *                                          protocol version
 */

public record CBContainerProtocolAvailable(
  long containerProtocolMinimumVersion,
  long containerProtocolMaximumVersion,
  UUID applicationProtocolId,
  long applicationProtocolMinimumVersion,
  long applicationProtocolMaximumVersion)
{
  /**
   * An availability message, typically presented by a server, that announces
   * the available application protocol ID and version, and the version of the
   * container protocol used to serve it.
   *
   * @param containerProtocolMinimumVersion   The minimum supported container
   *                                          protocol version
   * @param containerProtocolMaximumVersion   The maximum supported container
   *                                          protocol version
   * @param applicationProtocolId             The application protocol ID
   * @param applicationProtocolMinimumVersion The minimum supported application
   *                                          protocol version
   * @param applicationProtocolMaximumVersion The maximum supported application
   *                                          protocol version
   */

  public CBContainerProtocolAvailable
  {
    Objects.requireNonNull(applicationProtocolId, "applicationProtocolId");

    final var cpvMin = containerProtocolMinimumVersion;
    final var cpvMax = containerProtocolMaximumVersion;
    if (Long.compareUnsigned(cpvMin, cpvMax) > 0) {
      throw new IllegalArgumentException(String.format(
        "Container protocol version %s must be <= container protocol maximum version %s",
        Long.toUnsignedString(cpvMin),
        Long.toUnsignedString(cpvMax)
      ));
    }

    final var apvMin = applicationProtocolMinimumVersion;
    final var apvMax = applicationProtocolMaximumVersion;
    if (Long.compareUnsigned(apvMin, apvMax) > 0) {
      throw new IllegalArgumentException(String.format(
        "Container protocol version %s must be <= application protocol maximum version %s",
        Long.toUnsignedString(apvMin),
        Long.toUnsignedString(apvMax)
      ));
    }
  }
}
