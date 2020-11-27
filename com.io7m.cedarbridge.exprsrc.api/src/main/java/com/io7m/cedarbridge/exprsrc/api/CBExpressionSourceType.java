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

package com.io7m.cedarbridge.exprsrc.api;

import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.api.parser.JSXParserException;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * A source of expressions.
 *
 * The source provides access to previously encountered lines of text in order
 * to provide good diagnostic messages.
 */

public interface CBExpressionSourceType extends Closeable,
  CBExpressionLineLogType
{
  /**
   * @return The URI of the source
   */

  URI source();

  /**
   * @return The next expression, or {@link Optional#empty()} if EOF is reached
   *
   * @throws JSXParserException On parse errors
   * @throws IOException        On I/O errors
   */

  Optional<SExpressionType> parseExpressionOrEOF()
    throws JSXParserException, IOException;
}
