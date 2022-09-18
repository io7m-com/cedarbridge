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

package com.io7m.cedarbridge.schema.parser.internal;

import com.io7m.cedarbridge.schema.ast.CBASTField;
import com.io7m.cedarbridge.schema.ast.CBASTMutableUserData;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.SExpressionType.SList;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;

/**
 * A field parser.
 */

public final class CBFieldParser implements CBElementParserType<CBASTField>
{
  private static final Optional<UUID> SPEC_SECTION =
    uuid("b43940c3-038f-4330-971f-ac76d56d5fad");

  /**
   * A field parser.
   */

  public CBFieldParser()
  {

  }

  private static CBASTField parseField(
    final CBParseContextType context,
    final SList expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectField";
    final var expectingShapes =
      List.of("(field <field-name> <type-expression>)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      if (expression.size() != 3) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorFieldInvalid"
        );
      }

      final var name =
        CBNames.parseFieldName(subContext, expression.get(1));
      final var expr =
        new CBTypeExpressionParser().parse(subContext, expression.get(2));

      return new CBASTField(
        new CBASTMutableUserData(),
        expression.lexical(),
        name,
        expr
      );
    }
  }

  @Override
  public CBASTField parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    return parseField(
      context,
      context.checkExpressionIs(
        expression,
        SPEC_SECTION,
        SList.class)
    );
  }
}
