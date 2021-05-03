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

import com.io7m.cedarbridge.schema.ast.CBASTNames;
import com.io7m.cedarbridge.schema.ast.CBASTTypeApplication;
import com.io7m.cedarbridge.schema.ast.CBASTTypeExpressionType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeNamed;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;

/**
 * A parser for type expressions.
 */

public final class CBTypeExpressionParser
  implements CBElementParserType<CBASTTypeExpressionType>
{
  private static final Optional<UUID> SPEC_SECTION =
    uuid("b77ecee0-9add-4182-b3c5-f1d0a75ecfd9");

  /**
   * A parser for type expressions.
   */

  public CBTypeExpressionParser()
  {

  }

  private static CBASTTypeExpressionType.CBASTTypeNamedType parseTypePath(
    final CBParseContextType context,
    final SExpressionSymbolType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectTypePath";
    final var expectingForms =
      List.of(
        "<package-name> ':' <type-name>",
        "<type-name>"
      );

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingForms)) {
      try {
        final var builder =
          CBASTTypeNamed.builder()
            .setLexical(expression.lexical());

        final var text = expression.text();
        final var segments = List.of(text.split(":"));

        if (segments.size() == 1) {
          builder.setName(
            CBASTNames.typeName(expression, segments.get(0))
          );
          return builder.build();
        }

        if (segments.size() == 2) {
          builder.setPackageName(
            CBASTNames.packageName(expression, segments.get(0))
          );
          builder.setName(
            CBASTNames.typeName(expression, segments.get(1))
          );
          return builder.build();
        }

        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorTypePathInvalid"
        );
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorTypePathInvalid",
          e);
      }
    }
  }

  @Override
  public CBASTTypeExpressionType parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectTypeExpression";
    final var expectingShapes =
      List.of("<type-path>", "(<type-expression> ...)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      if (expression instanceof SExpressionSymbolType) {
        return parseTypePath(context, (SExpressionSymbolType) expression);
      }

      if (expression instanceof SExpressionListType) {
        return this.parseTypeApplication(
          context, (SExpressionListType) expression);
      }

      throw subContext.failed(
        expression,
        IS_NOT_FATAL,
        SPEC_SECTION,
        "errorUnexpectedExpressionForm");
    }
  }

  private CBASTTypeExpressionType parseTypeApplication(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectTypeApplication";
    final var expectingForms =
      List.of("(<type-path> <type-expression>...)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingForms)) {

      final var builder =
        CBASTTypeApplication.builder()
          .setLexical(expression.lexical());

      if (expression.size() == 0) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorEmptyTypeApplication");
      }

      builder.setTarget(
        parseTypePath(
          subContext,
          subContext.checkExpressionIs(
            expression.get(0),
            SPEC_SECTION,
            SExpressionSymbolType.class))
      );

      for (int index = 1; index < expression.size(); ++index) {
        builder.addArguments(this.parse(subContext, expression.get(index)));
      }
      return builder.build();
    }
  }
}
