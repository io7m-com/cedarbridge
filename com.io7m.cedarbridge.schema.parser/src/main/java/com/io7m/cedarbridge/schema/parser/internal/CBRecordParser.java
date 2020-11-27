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
import com.io7m.cedarbridge.schema.ast.CBASTField;
import com.io7m.cedarbridge.schema.ast.CBASTFieldName;
import com.io7m.cedarbridge.schema.ast.CBASTFieldNames;
import com.io7m.cedarbridge.schema.ast.CBASTTypeName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeNames;
import com.io7m.cedarbridge.schema.ast.CBASTTypeParameterName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeParameterNames;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.parser.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.CBParsed;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.io7m.cedarbridge.schema.parser.CBParsed.PARSED;

/**
 * A parser for record declarations.
 */

public final class CBRecordParser
  implements CBElementParserType<CBParsed, CBASTTypeRecord<CBParsed>>
{
  public CBRecordParser()
  {

  }

  private static List<CBASTField<CBParsed>> getFields(
    final Collection<Object> items)
  {
    return items.stream()
      .filter(i -> i instanceof CBASTField)
      .map(x -> (CBASTField<CBParsed>) x)
      .collect(Collectors.toList());
  }

  private static CBASTFieldName<CBParsed> parseFieldName(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectRecordFieldName";
    final var expectingForms =
      List.of("<field-name>");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingForms)) {
      final var symbol =
        subContext.checkExpressionIs(expression, SExpressionSymbolType.class);
      try {
        return CBASTFieldNames.of(expression, PARSED, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(expression, "errorRecordFieldNameInvalid", e);
      }
    }
  }

  private static CBASTElementType<CBParsed> parseRecordMemberParameter(
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
        throw subContext.failed(expression, "errorRecordInvalidTypeParameter");
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
        return CBASTTypeParameterNames.of(expression, PARSED, symbol.text());
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
        return CBASTTypeNames.of(expression, PARSED, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(expression, "errorTypeNameInvalid", e);
      }
    }
  }

  private static List<CBASTTypeParameterName<CBParsed>> getParameters(
    final Collection<Object> items)
  {
    return items.stream()
      .filter(i -> i instanceof CBASTTypeParameterName)
      .map(x -> (CBASTTypeParameterName<CBParsed>) x)
      .collect(Collectors.toList());
  }

  private static CBASTTypeRecord<CBParsed> parseRecord(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    final var items = new ArrayList<>();

    if (expression.size() < 2) {
      throw context.failed(expression, "errorRecordInvalidDeclaration");
    }

    context.checkExpressionIsKeyword(
      expression.get(0), "record", "errorRecordKeyword");
    final var typeName =
      context.checkExpressionIs(expression.get(1), SExpressionSymbolType.class);
    final var name =
      parseTypeName(context, typeName);

    final var errorsThen = context.errorCount();
    for (var index = 2; index < expression.size(); ++index) {
      try {
        items.add(parseRecordMember(context, expression.get(index)));
      } catch (final CBParseFailedException e) {
        // Ignore this particular exception
      }
    }

    if (context.errorCount() > errorsThen) {
      throw new CBParseFailedException();
    }

    final var fields =
      getFields(items);
    final var parameters =
      getParameters(items);

    return CBASTTypeRecord.<CBParsed>builder()
      .setLexical(expression.lexical())
      .setFields(fields)
      .setParameters(parameters)
      .setData(PARSED)
      .setName(name)
      .build();
  }

  private static CBASTElementType<CBParsed> parseRecordMember(
    final CBParseContextType context,
    final SExpressionType subExpression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectRecordMember";
    final var expectingShapes =
      List.of(
        "(parameter <type-parameter-name>)",
        "(field <field-name> <type-expression>)"
      );

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseRecordMemberActual(
        subContext,
        subContext.checkExpressionIs(subExpression, SExpressionListType.class)
      );
    }
  }

  private static CBASTElementType<CBParsed> parseRecordMemberActual(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    if (expression.size() < 1) {
      throw context.failed(expression, "errorRecordUnrecognizedMember");
    }

    final var start =
      context.checkExpressionIs(expression.get(0), SExpressionSymbolType.class);

    switch (start.text()) {
      case "parameter":
        return parseRecordMemberParameter(context, expression);
      case "field":
        return parseRecordMemberField(context, expression);
      default:
        throw context.failed(expression, "errorRecordUnrecognizedMember");
    }
  }

  private static CBASTElementType<CBParsed> parseRecordMemberField(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectRecordField";
    final var expectingShapes =
      List.of("(field <field-name> <type-expression>)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      if (expression.size() != 3) {
        throw subContext.failed(expression, "errorRecordInvalidField");
      }

      final var name =
        parseFieldName(subContext, expression.get(1));
      final var expr =
        new CBTypeExpressionParser().parse(subContext, expression.get(2));

      return CBASTField.<CBParsed>builder()
        .setLexical(expression.lexical())
        .setData(PARSED)
        .setName(name)
        .setType(expr)
        .build();
    }
  }

  @Override
  public CBASTTypeRecord<CBParsed> parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectRecordDeclaration";
    final var expectingShapes =
      List.of("(record <type-name-decl> <parameter-decl>... <field-decl>...)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseRecord(
        subContext,
        subContext.checkExpressionIs(expression, SExpressionListType.class)
      );
    }
  }
}
