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

package com.io7m.cedarbridge.examples.generic;

import com.io7m.cedarbridge.runtime.api.CBProtocolMessageType;

import java.util.HashMap;

public final class CBExMessageTranslatorDirectory<M, P extends CBProtocolMessageType>
{
  private final HashMap<Long, CBExMessageTranslatorType<M, P>> translators;

  public CBExMessageTranslatorDirectory()
  {
    this.translators = new HashMap<>();
  }

  public <Q extends P> CBExMessageTranslatorDirectory<M, P> addTranslator(
    final long version,
    final CBExMessageTranslatorType<M, Q> translator)
  {
    this.translators.put(
      Long.valueOf(version),
      (CBExMessageTranslatorType<M, P>) translator
    );
    return this;
  }

  public CBExMessageTranslatorType<M, P> get(
    final long version)
  {
    final var translator =
      this.translators.get(Long.valueOf(version));
    if (translator == null) {
      throw new IllegalStateException(String.format(
        "No translator for version: %d",
        Long.valueOf(version))
      );
    }
    return translator;
  }
}
