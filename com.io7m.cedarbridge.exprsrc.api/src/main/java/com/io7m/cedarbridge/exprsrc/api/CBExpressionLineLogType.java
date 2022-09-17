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

package com.io7m.cedarbridge.exprsrc.api;

import com.io7m.jlexing.core.LexicalPosition;

import java.util.Optional;

/**
 * A log of the lines of text encountered so far.
 */

public interface CBExpressionLineLogType
{
  /**
   * Show the previously-encountered line with the given line number.
   *
   * @param lineNumber The line number
   *
   * @return The line, if one has been encountered
   */

  Optional<String> showLineFor(
    int lineNumber);

  /**
   * Show a contextually highlighted line for the given lexical position.
   *
   * @param position The position
   *
   * @return The highlighted line, if one has been encountered
   */

  Optional<String> contextualize(
    LexicalPosition<?> position);
}
