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

package com.io7m.cedarbridge.schema.ast;

import com.io7m.jlexing.core.LexicalType;

import java.net.URI;

/**
 * Functions over record field names.
 */

public final class CBASTFieldNames extends CBASTNameChecker
{
  /**
   * The checker instance.
   */

  public static final CBASTFieldNames INSTANCE = new CBASTFieldNames();

  private CBASTFieldNames()
  {
    super("[a-z][a-zA-Z0-9]*");
  }

  /**
   * Construct a name.
   *
   * @param lexical Lexical data
   * @param data    Pass-specific data
   * @param text    The text
   * @param <T>     The type of pass-specific data
   *
   * @return A name
   */

  public static <T> CBASTFieldName<T> of(
    final LexicalType<URI> lexical,
    final T data,
    final String text)
  {
    return CBASTFieldName.<T>builder()
      .setLexical(lexical.lexical())
      .setData(data)
      .setText(text)
      .build();
  }
}
