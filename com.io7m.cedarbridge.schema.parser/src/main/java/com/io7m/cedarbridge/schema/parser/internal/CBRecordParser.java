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
import com.io7m.cedarbridge.schema.ast.CBASTNames;
import com.io7m.cedarbridge.schema.ast.CBASTTypeName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeParameterName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A parser for record declarations.
 */

public final class CBRecordParser
  implements CBElementParserType<CBASTTypeRecord>
{
  public CBRecordParser()
  {

  }

  private static List<CBASTField> getFields(
    final Collection<Object> items)
  {
    return items.stream()
      .filter(i -> i instanceof CBASTField)
      .map(x -> (CBASTField) x)
      .collect(Collectors.toList());
  }

  private static CBASTFieldName parseFieldName(
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
        return CBASTNames.fieldName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(expression, "errorRecordFieldNameInvalid", e);
      }
    }
  }

  private static CBASTElementType parseRecordMemberParameter(
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

  private static CBASTElementType parseTypeParameterName(
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
        return CBASTNames.typeParameterName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(expression, "errorTypeParameterNameInvalid", e);
      }
    }
  }

  private static CBASTTypeName parseTypeName(
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
        return CBASTNames.typeName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(expression, "errorTypeNameInvalid", e);
      }
    }
  }

  private static List<CBASTTypeParameterName> getParameters(
    final Collection<Object> items)
  {
    return items.stream()
      .filter(i -> i instanceof CBASTTypeParameterName)
      .map(x -> (CBASTTypeParameterName) x)
      .collect(Collectors.toList());
  }

  private static CBASTTypeRecord parseRecord(
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

    return CBASTTypeRecord.builder()
      .setLexical(expression.lexical())
      .setFields(fields)
      .setParameters(parameters)
      .setName(name)
      .build();
  }

  private static CBASTElementType parseRecordMember(
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

  private static CBASTElementType parseRecordMemberActual(
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

  private static CBASTElementType parseRecordMemberField(
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

      return CBASTField.builder()
        .setLexical(expression.lexical())
        .setName(name)
        .setType(expr)
        .build();
    }
  }

  @Override
  public CBASTTypeRecord parse(
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
