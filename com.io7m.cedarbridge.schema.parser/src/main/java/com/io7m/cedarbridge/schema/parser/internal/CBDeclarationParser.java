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

package com.io7m.cedarbridge.schema.parser.internal;

import com.io7m.cedarbridge.schema.ast.CBASTDeclarationType;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.List;

/**
 * A parser for declarations.
 */

public final class CBDeclarationParser
  implements CBElementParserType<CBASTDeclarationType>
{
  public CBDeclarationParser()
  {

  }

  private static CBASTDeclarationType parseDeclaration(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    if (expression.size() < 1) {
      throw context.failed(expression, "errorDeclarationInvalid");
    }

    final var keyword =
      context.checkExpressionIs(expression.get(0), SExpressionSymbolType.class);

    switch (keyword.text()) {
      case "package": {
        return new CBPackageDeclarationParser()
          .parse(context, expression);
      }
      case "import": {
        return new CBImportParser()
          .parse(context, expression);
      }
      case "record": {
        return new CBRecordParser()
          .parse(context, expression);
      }
      case "variant": {
        return new CBVariantParser()
          .parse(context, expression);
      }
      default: {
        throw context.failed(expression, "errorDeclarationUnrecognized");
      }
    }
  }

  @Override
  public CBASTDeclarationType parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind = "objectDeclaration";
    final var expectingShapes =
      List.of(
        "<package-decl>",
        "<import-decl>",
        "<record-decl>",
        "<variant-decl>"
      );

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseDeclaration(
        subContext,
        subContext.checkExpressionIs(expression, SExpressionListType.class));
    }
  }
}
