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

import com.io7m.cedarbridge.schema.ast.CBASTElementType;
import com.io7m.cedarbridge.schema.ast.CBASTMutableUserData;
import com.io7m.cedarbridge.schema.ast.CBASTTypeParameterName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariantCase;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;
import static com.io7m.cedarbridge.schema.parser.internal.CBNames.parseTypeName;
import static com.io7m.cedarbridge.schema.parser.internal.CBNames.parseTypeParameterName;

/**
 * A parser for variant declarations.
 */

public final class CBVariantParser
  implements CBElementParserType<CBASTTypeVariant>
{
  /**
   * The syntax section for variants.
   */

  public static final Optional<UUID> SPEC_SECTION =
    uuid("9c9c589e-4cc0-457f-8f3d-4d475b2763a3");

  /**
   * A parser for variant declarations.
   */

  public CBVariantParser()
  {

  }

  private static CBASTElementType parseVariantMemberParameter(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectTypeParameter";
    final var expectingShapes =
      List.of("(parameter <type-parameter-name>)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      if (expression.size() != 2) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorVariantInvalidTypeParameter"
        );
      }

      return parseTypeParameterName(subContext, expression.get(1));
    }
  }

  private static CBASTTypeVariant parseVariant(
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
        "errorVariantInvalidDeclaration"
      );
    }

    context.checkExpressionIsKeyword(
      expression.get(0),
      SPEC_SECTION,
      "variant",
      "errorVariantKeyword"
    );

    final var typeName =
      context.checkExpressionIs(
        expression.get(1),
        SPEC_SECTION,
        SExpressionSymbolType.class
      );

    final var name =
      parseTypeName(context, typeName);

    final var errorsThen = context.errorCount();
    for (var index = 2; index < expression.size(); ++index) {
      try {
        items.add(parseVariantMember(context, expression.get(index)));
      } catch (final CBParseFailedException e) {
        // Ignore this particular exception
      }
    }

    if (context.errorCount() > errorsThen) {
      throw new CBParseFailedException(IS_NOT_FATAL);
    }

    final var parameters =
      CBFilters.filter(items, CBASTTypeParameterName.class);
    final var cases =
      CBFilters.filter(items, CBASTTypeVariantCase.class);

    return new CBASTTypeVariant(
      new CBASTMutableUserData(),
      expression.lexical(),
      name,
      parameters,
      cases
    );
  }

  private static CBASTElementType parseVariantMember(
    final CBParseContextType context,
    final SExpressionType subExpression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectVariantMember";
    final var expectingShapes =
      List.of(
        "(parameter <type-parameter-name>)",
        "(case <type-name> <field-decl> ...)"
      );

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseVariantMemberActual(
        subContext,
        subContext.checkExpressionIs(
          subExpression,
          SPEC_SECTION,
          SExpressionListType.class)
      );
    }
  }

  private static CBASTElementType parseVariantMemberActual(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    if (expression.size() < 1) {
      throw context.failed(
        expression,
        IS_NOT_FATAL,
        SPEC_SECTION,
        "errorVariantUnrecognizedMember"
      );
    }

    final var start =
      context.checkExpressionIs(
        expression.get(0),
        SPEC_SECTION,
        SExpressionSymbolType.class
      );

    switch (start.text()) {
      case "parameter":
        return parseVariantMemberParameter(context, expression);
      case "case":
        return new CBVariantCaseParser().parse(context, expression);
      default:
        throw context.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorVariantUnrecognizedMember"
        );
    }
  }

  @Override
  public CBASTTypeVariant parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectVariantDeclaration";
    final var expectingShapes =
      List.of("(variant <type-name-decl> <parameter-decl> <record-decl>...)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseVariant(
        subContext,
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION,
          SExpressionListType.class)
      );
    }
  }
}
