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

import com.io7m.cedarbridge.schema.ast.CBASTImport;
import com.io7m.cedarbridge.schema.ast.CBASTPackageName;
import com.io7m.cedarbridge.schema.ast.CBASTPackageNames;
import com.io7m.cedarbridge.schema.ast.CBASTPackageShortName;
import com.io7m.cedarbridge.schema.ast.CBASTPackageShortNames;
import com.io7m.cedarbridge.schema.parser.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.CBParsed;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.util.List;

import static com.io7m.cedarbridge.schema.parser.CBParsed.PARSED;

/**
 * A parser for import declarations.
 */

public final class CBImportParser
  implements CBElementParserType<CBParsed, CBASTImport<CBParsed>>
{
  private static final String EXPECTING_KIND =
    "objectImport";
  private static final List<String> EXPECTING_SHAPES =
    List.of("(import <package-name> <package-short-name>)");

  public CBImportParser()
  {

  }

  private static CBASTImport<CBParsed> parseImport(
    final CBParseContextType context,
    final SExpressionListType list)
    throws CBParseFailedException
  {
    if (list.size() != 3) {
      throw context.failed(list, "errorImportInvalid");
    }

    context.checkExpressionIsKeyword(
      list.get(0), "import", "errorImportKeyword");

    final var packName =
      context.checkExpressionIs(list.get(1), SExpressionSymbolType.class);
    final var packShort =
      context.checkExpressionIs(list.get(2), SExpressionSymbolType.class);

    return CBASTImport.<CBParsed>builder()
      .setData(PARSED)
      .setLexical(list.lexical())
      .setTarget(parsePackageName(context, packName))
      .setShortName(parseShortName(context, packShort))
      .build();
  }

  private static CBASTPackageShortName<CBParsed> parseShortName(
    final CBParseContextType context,
    final SExpressionSymbolType packShort)
    throws CBParseFailedException
  {
    try (var subContext =
           context.openExpectingOneOf(
             "objectPackageShortName", List.of("<package-short-name>"))) {
      try {
        return CBASTPackageShortNames.of(packShort, PARSED, packShort.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(packShort, "errorPackageShortNameInvalid", e);
      }
    }
  }

  private static CBASTPackageName<CBParsed> parsePackageName(
    final CBParseContextType context,
    final SExpressionSymbolType packName)
    throws CBParseFailedException
  {
    try (var subContext =
           context.openExpectingOneOf(
             "objectPackageName", List.of("<package-name>"))) {
      try {
        return CBASTPackageNames.of(packName, PARSED, packName.text());
      } catch (final IllegalArgumentException e) {
        throw subContext.failed(packName, "errorPackageNameInvalid", e);
      }
    }
  }

  @Override
  public CBASTImport<CBParsed> parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {

    try (var subContext =
           context.openExpectingOneOf(EXPECTING_KIND, EXPECTING_SHAPES)) {
      final var list =
        subContext.checkExpressionIs(expression, SExpressionListType.class);
      return parseImport(subContext, list);
    }
  }
}
