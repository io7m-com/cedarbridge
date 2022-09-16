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

import com.io7m.jlexing.core.LexicalPosition;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * A record declaration.
 *
 * @param documentations The documentation elements
 * @param fields         The record fields
 * @param lexical        The lexical info
 * @param name           The type name
 * @param parameters     The type parameters
 * @param userData       The user data
 */

public record CBASTTypeRecord(
  CBASTMutableUserData userData,
  LexicalPosition<URI> lexical,
  CBASTTypeName name,
  List<CBASTDocumentation> documentations,
  List<CBASTTypeParameterName> parameters,
  List<CBASTField> fields)
  implements CBASTTypeDeclarationType
{
  /**
   * A record declaration.
   *
   * @param documentations The documentation elements
   * @param fields         The record fields
   * @param lexical        The lexical info
   * @param name           The type name
   * @param parameters     The type parameters
   * @param userData       The user data
   */

  public CBASTTypeRecord
  {
    Objects.requireNonNull(documentations, "documentations");
    Objects.requireNonNull(fields, "fields");
    Objects.requireNonNull(lexical, "lexical");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(parameters, "parameters");
    Objects.requireNonNull(userData, "userData");
  }
}
