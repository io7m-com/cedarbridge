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

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A response to a {@link CBContainerProtocolUse} message. If {@code ok} is
 * {@code false}, the client should expect to be disconnected immediately.
 *
 * @param ok      The "use" message was/was not acceptable
 * @param message The error message text, if any
 */

public record CBContainerProtocolResponse(
  boolean ok,
  String message)
{
  /**
   * A response to a {@link CBContainerProtocolUse} message. If {@code ok} is
   * {@code false}, the client should expect to be disconnected immediately.
   *
   * @param ok      The "use" message was/was not acceptable
   * @param message The error message text, if any
   */

  public CBContainerProtocolResponse
  {
    Objects.requireNonNull(message, "message");

    final var encoded = message.getBytes(UTF_8);
    if (encoded.length > 244) {
      throw new IllegalArgumentException(
        "Message too long; must be <= 244 bytes");
    }
  }
}
