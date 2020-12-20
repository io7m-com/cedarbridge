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
import java.util.Optional;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_FATAL;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;

/**
 * A parser for declarations.
 */

public final class CBDeclarationParser
  implements CBElementParserType<CBASTDeclarationType>
{
  private final boolean tooLateForLanguage;

  public CBDeclarationParser(
    final boolean inTooLateForLanguage)
  {
    this.tooLateForLanguage = inTooLateForLanguage;
  }

  private CBASTDeclarationType parseDeclaration(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    if (expression.size() < 1) {
      throw context.failed(
        expression,
        IS_NOT_FATAL,
        Optional.empty(),
        "errorDeclarationInvalid"
      );
    }

    final var keyword =
      context.checkExpressionIs(
        expression.get(0),
        Optional.empty(),
        SExpressionSymbolType.class
      );

    switch (keyword.text()) {
      case "language": {
        if (this.tooLateForLanguage) {
          throw context.failed(
            expression,
            IS_FATAL,
            uuid("e03347f6-a042-452c-b34b-eeb6514ee26e"),
            "errorLanguageFirst"
          );
        }

        final var language =
          new CBLanguageParser().parse(context, expression);

        context.setLanguageVersion(
          language.major().intValueExact(),
          language.minor().intValueExact()
        );
        return language;
      }
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
      case "protocol": {
        return new CBProtocolParser()
          .parse(context, expression);
      }
      default: {
        throw context.failed(
          expression,
          IS_NOT_FATAL,
          Optional.empty(),
          "errorDeclarationUnrecognized");
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

    final List<String> expectingShapes;
    if (this.tooLateForLanguage) {
      expectingShapes = List.of(
        "<package-decl>",
        "<import-decl>",
        "<record-decl>",
        "<variant-decl>",
        "<protocol-decl>"
      );
    } else {
      expectingShapes = List.of(
        "<package-decl>",
        "<import-decl>",
        "<record-decl>",
        "<variant-decl>",
        "<protocol-decl>",
        "<language-decl>"
      );
    }

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return this.parseDeclaration(
        subContext,
        subContext.checkExpressionIs(
          expression,
          Optional.empty(),
          SExpressionListType.class)
      );
    }
  }
}
