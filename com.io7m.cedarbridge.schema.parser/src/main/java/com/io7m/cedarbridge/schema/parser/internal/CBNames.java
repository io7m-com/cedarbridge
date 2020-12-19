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
import com.io7m.cedarbridge.schema.ast.CBASTFieldName;
import com.io7m.cedarbridge.schema.ast.CBASTNames;
import com.io7m.cedarbridge.schema.ast.CBASTTypeName;
import com.io7m.cedarbridge.schema.ast.CBASTVariantCaseName;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.List;

import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;

public final class CBNames
{
  private CBNames()
  {

  }

  static CBASTFieldName parseFieldName(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectFieldName";
    final var expectingForms =
      List.of("<field-name>");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingForms)) {
      final var symbol =
        subContext.checkExpressionIs(expression, SExpressionSymbolType.class);
      try {
        return CBASTNames.fieldName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          "errorFieldNameInvalid",
          e);
      }
    }
  }

  static CBASTElementType parseTypeParameterName(
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
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          "errorTypeParameterNameInvalid",
          e);
      }
    }
  }

  static CBASTTypeName parseTypeName(
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
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          "errorTypeNameInvalid",
          e);
      }
    }
  }

  static CBASTVariantCaseName parseVariantCaseName(
    final CBParseContextType context,
    final SExpressionSymbolType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectVariantCaseName";
    final var expectingForms =
      List.of("<variant-case-name>");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingForms)) {
      final var symbol =
        subContext.checkExpressionIs(expression, SExpressionSymbolType.class);
      try {
        return CBASTNames.variantCaseName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          "errorVariantCaseNameInvalid",
          e);
      }
    }
  }
}
