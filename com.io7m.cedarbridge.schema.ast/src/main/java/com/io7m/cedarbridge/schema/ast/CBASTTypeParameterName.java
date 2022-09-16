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

package com.io7m.cedarbridge.schema.ast;

import com.io7m.cedarbridge.schema.names.CBTypeParameterNames;
import com.io7m.jlexing.core.LexicalPosition;

import java.net.URI;
import java.util.Objects;

/**
 * The type of type parameter names.
 *
 * @param userData User data
 * @param lexical  Lexical info
 * @param text     The name text
 */

public record CBASTTypeParameterName(
  CBASTMutableUserData userData,
  LexicalPosition<URI> lexical,
  String text)
  implements CBASTNameType, CBASTRecordMemberType, CBASTVariantMemberType
{
  /**
   * The type of type parameter names.
   *
   * @param userData User data
   * @param lexical  Lexical info
   * @param text     The name text
   */

  public CBASTTypeParameterName
  {
    Objects.requireNonNull(userData, "userData");
    Objects.requireNonNull(lexical, "lexical");
    Objects.requireNonNull(text, "text");

    CBTypeParameterNames.INSTANCE.checkValid(text);
  }
}
