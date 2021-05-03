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
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;

/**
 * Functions over names.
 */

public final class CBNames
{
  private static final Optional<UUID> SPEC_SECTION_FIELD_NAME =
    uuid("b43940c3-038f-4330-971f-ac76d56d5fad");
  private static final Optional<UUID> SPEC_SECTION_TYPE_PARAMETER_NAME =
    uuid("b43940c3-038f-4330-971f-ac76d56d5fad");
  private static final Optional<UUID> SPEC_SECTION_TYPE_NAME =
    uuid("6f6e50ac-83e9-4454-9329-149e8ca97cb8");
  private static final Optional<UUID> SPEC_SECTION_VARIANT_CASE_NAME =
    uuid("9c9c589e-4cc0-457f-8f3d-4d475b2763a3");

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
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION_FIELD_NAME,
          SExpressionSymbolType.class
        );

      try {
        return CBASTNames.fieldName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION_FIELD_NAME,
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
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION_TYPE_PARAMETER_NAME,
          SExpressionSymbolType.class
        );
      try {
        return CBASTNames.typeParameterName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION_TYPE_PARAMETER_NAME,
          "errorTypeParameterNameInvalid",
          e
        );
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
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION_TYPE_NAME,
          SExpressionSymbolType.class
        );

      try {
        return CBASTNames.typeName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION_TYPE_NAME,
          "errorTypeNameInvalid",
          e
        );
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
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION_VARIANT_CASE_NAME,
          SExpressionSymbolType.class
        );
      try {
        return CBASTNames.variantCaseName(expression, symbol.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          expression,
          IS_NOT_FATAL,
          SPEC_SECTION_VARIANT_CASE_NAME,
          "errorVariantCaseNameInvalid",
          e
        );
      }
    }
  }
}
