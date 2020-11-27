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

import com.io7m.cedarbridge.schema.ast.CBASTElementType;
import com.io7m.cedarbridge.schema.ast.CBASTNames;
import com.io7m.cedarbridge.schema.ast.CBASTTypeName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeParameterName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.api.CBParsed;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.io7m.cedarbridge.schema.parser.api.CBParsed.PARSED;

/**
 * A parser for variant declarations.
 */

public final class CBVariantParser
  implements CBElementParserType<CBParsed, CBASTTypeVariant<CBParsed>>
{
  public CBVariantParser()
  {

  }

  private static List<CBASTTypeRecord<CBParsed>> getCases(
    final Collection<?> items)
  {
    return items.stream()
      .filter(x -> x instanceof CBASTTypeRecord)
      .map(x -> (CBASTTypeRecord<CBParsed>) x)
      .collect(Collectors.toList());
  }

  private static List<CBASTTypeParameterName<CBParsed>> getParameters(
    final Collection<Object> items)
  {
    return items.stream()
      .filter(i -> i instanceof CBASTTypeParameterName)
      .map(x -> (CBASTTypeParameterName<CBParsed>) x)
      .collect(Collectors.toList());
  }

  private static CBASTElementType<CBParsed> parseVariantMemberParameter(
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
        throw subContext.failed(expression, "errorVariantInvalidTypeParameter");
      }

      return parseTypeParameterName(subContext, expression.get(1));
    }
  }

  private static CBASTElementType<CBParsed> parseTypeParameterName(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectTypeParameterName";
    final var expectingForms =
      List.of("<type-parameter-name>");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingForms)) {
      final var symbol =
        subContext.checkExpressionIs(expression, SExpressionSymbolType.class);
      try {
        return CBASTNames.typeParameterName(expression, PARSED, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(expression, "errorTypeParameterNameInvalid", e);
      }
    }
  }

  private static CBASTTypeName<CBParsed> parseTypeName(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectTypeName";
    final var expectingForms =
      List.of("<type-name>");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingForms)) {
      final var symbol =
        subContext.checkExpressionIs(expression, SExpressionSymbolType.class);
      try {
        return CBASTNames.typeName(expression, PARSED, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(expression, "errorTypeNameInvalid", e);
      }
    }
  }

  private static CBASTTypeVariant<CBParsed> parseVariant(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    final var items = new ArrayList<>();

    if (expression.size() < 2) {
      throw context.failed(expression, "errorVariantInvalidDeclaration");
    }

    context.checkExpressionIsKeyword(
      expression.get(0), "variant", "errorVariantKeyword");
    final var typeName =
      context.checkExpressionIs(expression.get(1), SExpressionSymbolType.class);
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
      throw new CBParseFailedException();
    }

    final var parameters =
      getParameters(items);
    final var cases =
      getCases(items);

    return CBASTTypeVariant.<CBParsed>builder()
      .setLexical(expression.lexical())
      .setCases(cases)
      .setParameters(parameters)
      .setData(PARSED)
      .setName(name)
      .build();
  }

  private static CBASTElementType<CBParsed> parseVariantMember(
    final CBParseContextType context,
    final SExpressionType subExpression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectVariantMember";
    final var expectingShapes =
      List.of(
        "(parameter <type-parameter-name>)",
        "<record-decl>"
      );

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseVariantMemberActual(
        subContext,
        subContext.checkExpressionIs(subExpression, SExpressionListType.class)
      );
    }
  }

  private static CBASTElementType<CBParsed> parseVariantMemberActual(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    if (expression.size() < 1) {
      throw context.failed(expression, "errorVariantUnrecognizedMember");
    }

    final var start =
      context.checkExpressionIs(expression.get(0), SExpressionSymbolType.class);

    switch (start.text()) {
      case "parameter":
        return parseVariantMemberParameter(context, expression);
      case "record":
        return parseVariantMemberRecord(context, expression);
      default:
        throw context.failed(expression, "errorVariantUnrecognizedMember");
    }
  }

  private static CBASTElementType<CBParsed> parseVariantMemberRecord(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    return new CBRecordParser().parse(context, expression);
  }

  @Override
  public CBASTTypeVariant<CBParsed> parse(
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
        subContext.checkExpressionIs(expression, SExpressionListType.class)
      );
    }
  }
}
