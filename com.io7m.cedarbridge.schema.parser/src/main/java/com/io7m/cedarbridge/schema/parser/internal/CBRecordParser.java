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
import com.io7m.cedarbridge.schema.ast.CBASTTypeParameterName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
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
 * A parser for record declarations.
 */

public final class CBRecordParser
  implements CBElementParserType<CBASTTypeRecord>
{
  private static final Optional<UUID> SPEC_SECTION =
    uuid("b43940c3-038f-4330-971f-ac76d56d5fad");

  public CBRecordParser()
  {

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
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorRecordInvalidTypeParameter"
        );
      }
      return parseTypeParameterName(subContext, expression.get(1));
    }
  }

  private static CBASTTypeRecord parseRecord(
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
        "errorRecordInvalidDeclaration"
      );
    }

    context.checkExpressionIsKeyword(
      expression.get(0),
      SPEC_SECTION,
      "record",
      "errorRecordKeyword"
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
        items.add(parseRecordMember(context, expression.get(index)));
      } catch (final CBParseFailedException e) {
        // Ignore this particular exception
      }
    }

    if (context.errorCount() > errorsThen) {
      throw new CBParseFailedException(IS_NOT_FATAL);
    }

    final var fields =
      CBFilters.filter(items, CBASTField.class);
    final var parameters =
      CBFilters.filter(items, CBASTTypeParameterName.class);

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
        subContext.checkExpressionIs(
          subExpression,
          SPEC_SECTION,
          SExpressionListType.class)
      );
    }
  }

  private static CBASTElementType parseRecordMemberActual(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    if (expression.size() < 1) {
      throw context.failed(
        expression,
        IS_NOT_FATAL,
        SPEC_SECTION,
        "errorRecordUnrecognizedMember"
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
        return parseRecordMemberParameter(context, expression);
      case "field":
        return new CBFieldParser().parse(context, expression);
      default:
        throw context.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorRecordUnrecognizedMember"
        );
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
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION,
          SExpressionListType.class)
      );
    }
  }
}
