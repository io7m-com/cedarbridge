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
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariantCase;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.ArrayList;
import java.util.List;

import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;
import static com.io7m.cedarbridge.schema.parser.internal.CBVariantParser.SPEC_SECTION;

/**
 * A parser for variant case declarations.
 */

public final class CBVariantCaseParser
  implements CBElementParserType<CBASTTypeVariantCase>
{
  /**
   * A parser for variant case declarations.
   */

  public CBVariantCaseParser()
  {

  }

  private static CBASTTypeVariantCase parseVariantCase(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    final var items = new ArrayList<>();

    if (expression.size() < 2) {
      throw context.failed(
        expression,
        IS_NOT_FATAL,
        SPEC_SECTION,
        "errorVariantCaseInvalidDeclaration"
      );
    }

    context.checkExpressionIsKeyword(
      expression.get(0),
      SPEC_SECTION,
      "case",
      "errorVariantCaseKeyword"
    );

    final var typeName =
      context.checkExpressionIs(
        expression.get(1),
        SPEC_SECTION,
        SExpressionSymbolType.class
      );

    final var name =
      CBNames.parseVariantCaseName(context, typeName);

    final var errorsThen = context.errorCount();
    for (var index = 2; index < expression.size(); ++index) {
      try {
        final var subExpr = expression.get(index);
        items.add(
          new CBFieldParser().parse(
            context,
            context.checkExpressionIs(
              subExpr,
              SPEC_SECTION,
              SExpressionListType.class)
          )
        );
      } catch (final CBParseFailedException e) {
        // Ignore this particular exception
      }
    }

    if (context.errorCount() > errorsThen) {
      throw new CBParseFailedException(IS_NOT_FATAL);
    }

    return CBASTTypeVariantCase.builder()
      .setLexical(expression.lexical())
      .setFields(CBFilters.filter(items, CBASTField.class))
      .setName(name)
      .build();
  }


  @Override
  public CBASTTypeVariantCase parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectVariantCase";
    final var expectingShapes =
      List.of("(case <type-name-decl> <field-decl>...)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseVariantCase(
        subContext,
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION,
          SExpressionListType.class)
      );
    }
  }
}
