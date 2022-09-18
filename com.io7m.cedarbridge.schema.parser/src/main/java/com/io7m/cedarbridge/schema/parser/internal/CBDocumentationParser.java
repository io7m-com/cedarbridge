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

import com.io7m.cedarbridge.schema.ast.CBASTDocumentation;
import com.io7m.cedarbridge.schema.ast.CBASTMutableUserData;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.SExpressionType.SList;
import com.io7m.jsx.SExpressionType.SQuotedString;
import com.io7m.jsx.SExpressionType.SSymbol;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_FATAL;

/**
 * A parser for documentation declarations.
 */

public final class CBDocumentationParser
  implements CBElementParserType<CBASTDocumentation>
{
  private static final String EXPECTING_KIND =
    "objectDocumentation";

  private static final List<String> EXPECTING_SHAPES =
    List.of("(documentation <name> <string> ...)");

  private static final Optional<UUID> SPEC_SECTION =
    uuid("a07ef108-697e-4d79-bb17-3ff419babd68");

  /**
   * A parser for documentation declarations.
   */

  public CBDocumentationParser()
  {

  }

  private static CBASTDocumentation parseDocumentation(
    final CBParseContextType context,
    final SList list)
    throws CBParseFailedException
  {
    final var expressionCount = list.size();
    if (expressionCount != 3) {
      throw context.failed(
        list,
        IS_FATAL,
        SPEC_SECTION,
        "errorDeclarationInvalid"
      );
    }

    context.checkExpressionIsKeyword(
      list.get(0),
      SPEC_SECTION,
      "documentation",
      "errorDocumentationKeyword"
    );

    final var docName =
      context.checkExpressionIs(
        list.get(1),
        SPEC_SECTION,
        SSymbol.class
      );

    final var text =
      context.checkExpressionIs(
        list.get(2),
        SPEC_SECTION,
        SQuotedString.class
      ).text();

    return new CBASTDocumentation(
      new CBASTMutableUserData(),
      list.lexical(),
      docName.text(),
      text
    );
  }

  @Override
  public CBASTDocumentation parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    try (var subContext =
           context.openExpectingOneOf(EXPECTING_KIND, EXPECTING_SHAPES)) {
      return parseDocumentation(
        subContext,
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION,
          SList.class
        ));
    }
  }
}
