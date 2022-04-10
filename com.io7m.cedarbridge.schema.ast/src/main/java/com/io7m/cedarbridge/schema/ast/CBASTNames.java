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

package com.io7m.cedarbridge.schema.ast;

import com.io7m.jlexing.core.LexicalType;

import java.net.URI;

/**
 * Functions over AST names.
 */

public final class CBASTNames
{
  private CBASTNames()
  {

  }

  /**
   * Construct a name.
   *
   * @param lexical Lexical data
   * @param text    The text
   *
   * @return A name
   */

  public static CBASTFieldName fieldName(
    final LexicalType<URI> lexical,
    final String text)
  {
    return CBASTFieldName.builder()
      .setLexical(lexical.lexical())
      .setText(text)
      .build();
  }

  /**
   * Construct a name.
   *
   * @param lexical Lexical data
   * @param text    The text
   *
   * @return A name
   */

  public static CBASTPackageName packageName(
    final LexicalType<URI> lexical,
    final String text)
  {
    return CBASTPackageName.builder()
      .setLexical(lexical.lexical())
      .setText(text)
      .build();
  }

  /**
   * Construct a name.
   *
   * @param lexical Lexical data
   * @param text    The text
   *
   * @return A name
   */

  public static CBASTPackageShortName shortPackageName(
    final LexicalType<URI> lexical,
    final String text)
  {
    return CBASTPackageShortName.builder()
      .setLexical(lexical.lexical())
      .setText(text)
      .build();
  }

  /**
   * Construct a name.
   *
   * @param lexical Lexical data
   * @param text    The text
   *
   * @return A name
   */

  public static CBASTTypeName typeName(
    final LexicalType<URI> lexical,
    final String text)
  {
    return CBASTTypeName.builder()
      .setLexical(lexical.lexical())
      .setText(text)
      .build();
  }

  /**
   * Construct a name.
   *
   * @param lexical Lexical data
   * @param text    The text
   *
   * @return A name
   */

  public static CBASTTypeParameterName typeParameterName(
    final LexicalType<URI> lexical,
    final String text)
  {
    return CBASTTypeParameterName.builder()
      .setLexical(lexical.lexical())
      .setText(text)
      .build();
  }

  /**
   * Construct a name.
   *
   * @param lexical Lexical data
   * @param text    The text
   *
   * @return A name
   */

  public static CBASTVariantCaseName variantCaseName(
    final LexicalType<URI> lexical,
    final String text)
  {
    return CBASTVariantCaseName.builder()
      .setLexical(lexical.lexical())
      .setText(text)
      .build();
  }
}
