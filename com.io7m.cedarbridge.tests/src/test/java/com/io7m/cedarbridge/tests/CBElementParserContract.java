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

package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.exprsrc.CBExpressionSources;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceType;
import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.api.parser.JSXParserException;
import org.apache.commons.io.input.CharSequenceInputStream;

import java.io.IOException;
import java.net.URI;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class CBElementParserContract
{
  private static final CBExpressionSources SOURCES =
    new CBExpressionSources();

  protected CBExpressionSourceType source;

  public final SExpressionType expression(
    final String text)
  {
    this.source =
      SOURCES.create(
        URI.create("urn:stdin"),
        new CharSequenceInputStream(text, UTF_8)
      );

    try {
      return this.source.parseExpressionOrEOF().orElseThrow();
    } catch (JSXParserException | IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
