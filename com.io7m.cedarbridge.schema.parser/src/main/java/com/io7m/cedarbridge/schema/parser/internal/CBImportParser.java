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

import com.io7m.cedarbridge.schema.ast.CBASTImport;
import com.io7m.cedarbridge.schema.ast.CBASTMutableUserData;
import com.io7m.cedarbridge.schema.ast.CBASTNames;
import com.io7m.cedarbridge.schema.ast.CBASTPackageName;
import com.io7m.cedarbridge.schema.ast.CBASTPackageShortName;
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
 * A parser for import declarations.
 */

public final class CBImportParser
  implements CBElementParserType<CBASTImport>
{
  private static final String EXPECTING_KIND =
    "objectImport";
  private static final List<String> EXPECTING_SHAPES =
    List.of("(import <package-name> <package-short-name>)");

  private static final Optional<UUID> SPEC_SECTION =
    uuid("5740d88d-b9c3-4046-ab74-34d350ff4903");

  /**
   * A parser for import declarations.
   */

  public CBImportParser()
  {

  }

  private static CBASTImport parseImport(
    final CBParseContextType context,
    final SExpressionListType list)
    throws CBParseFailedException
  {
    if (list.size() != 3) {
      throw context.failed(
        list,
        IS_NOT_FATAL,
        SPEC_SECTION,
        "errorImportInvalid"
      );
    }

    context.checkExpressionIsKeyword(
      list.get(0), SPEC_SECTION, "import", "errorImportKeyword");

    final var packName =
      context.checkExpressionIs(
        list.get(1),
        SPEC_SECTION,
        SExpressionSymbolType.class
      );
    final var packShort =
      context.checkExpressionIs(
        list.get(2),
        SPEC_SECTION,
        SExpressionSymbolType.class
      );

    return new CBASTImport(
      new CBASTMutableUserData(),
      list.lexical(),
      parsePackageName(context, packName),
      parseShortName(context, packShort)
    );
  }

  private static CBASTPackageShortName parseShortName(
    final CBParseContextType context,
    final SExpressionSymbolType packShort)
    throws CBParseFailedException
  {
    try (var subContext =
           context.openExpectingOneOf(
             "objectPackageShortName", List.of("<package-short-name>"))) {
      try {
        return CBASTNames.shortPackageName(packShort, packShort.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          packShort,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorPackageShortNameInvalid",
          e);
      }
    }
  }

  private static CBASTPackageName parsePackageName(
    final CBParseContextType context,
    final SExpressionSymbolType packName)
    throws CBParseFailedException
  {
    try (var subContext =
           context.openExpectingOneOf(
             "objectPackageName", List.of("<package-name>"))) {
      try {
        return CBASTNames.packageName(packName, packName.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(
          packName,
          IS_NOT_FATAL,
          SPEC_SECTION,
          "errorPackageNameInvalid",
          e);
      }
    }
  }

  @Override
  public CBASTImport parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {

    try (var subContext =
           context.openExpectingOneOf(EXPECTING_KIND, EXPECTING_SHAPES)) {
      final var list =
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION,
          SExpressionListType.class
        );
      return parseImport(subContext, list);
    }
  }
}
