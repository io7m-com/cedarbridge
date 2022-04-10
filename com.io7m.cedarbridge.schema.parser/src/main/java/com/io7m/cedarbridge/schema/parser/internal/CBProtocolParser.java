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

import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolVersion;
import com.io7m.cedarbridge.schema.ast.CBASTTypeName;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;
import static com.io7m.cedarbridge.schema.parser.internal.CBNames.parseTypeName;

/**
 * A parser for protocol declarations.
 */

public final class CBProtocolParser
  implements CBElementParserType<CBASTProtocolDeclaration>
{
  private static final Optional<UUID> SPEC_SECTION =
    uuid("1f9b69fc-7b95-4007-8bd1-6715b07fd69a");

  /**
   * A parser for protocol declarations.
   */

  public CBProtocolParser()
  {

  }

  private static CBASTProtocolDeclaration parseProtocol(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    final var items = new ArrayList<CBASTProtocolVersion>();

    if (expression.size() < 2) {
      throw context.failed(
        expression,
        IS_NOT_FATAL,
        SPEC_SECTION,
        "errorProtocolInvalidDeclaration"
      );
    }

    context.checkExpressionIsKeyword(
      expression.get(0),
      SPEC_SECTION,
      "protocol",
      "errorProtocolKeyword"
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
        items.add(parseVersion(context, expression.get(index)));
      } catch (final CBParseFailedException e) {
        // Ignore this particular exception
      }
    }

    if (context.errorCount() > errorsThen) {
      throw new CBParseFailedException(IS_NOT_FATAL);
    }

    return CBASTProtocolDeclaration.builder()
      .setLexical(expression.lexical())
      .setName(name)
      .setVersions(items)
      .build();
  }

  private static CBASTProtocolVersion parseVersion(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectProtocolVersion";
    final var expectingShapes =
      List.of(
        "(version <version-number> <type-name> ...)"
      );

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseVersionActual(
        subContext,
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION,
          SExpressionListType.class)
      );
    }
  }

  private static CBASTProtocolVersion parseVersionActual(
    final CBParseContextType context,
    final SExpressionListType expression)
    throws CBParseFailedException
  {
    if (expression.size() < 2) {
      throw context.failed(
        expression,
        IS_NOT_FATAL,
        SPEC_SECTION,
        "errorProtocolVersionInvalidDeclaration"
      );
    }

    final var number =
      context.checkExpressionIs(
        expression.get(1),
        SPEC_SECTION,
        SExpressionSymbolType.class
      );

    final var items = new ArrayList<CBASTTypeName>();
    final var errorsThen = context.errorCount();
    for (var index = 2; index < expression.size(); ++index) {
      try {
        items.add(parseTypeName(context, expression.get(index)));
      } catch (final CBParseFailedException e) {
        // Ignore this particular exception
      }
    }

    final BigInteger versionNumber;
    try {
      versionNumber = new BigInteger(number.text());
    } catch (final NumberFormatException e) {
      throw context.failed(
        expression,
        IS_NOT_FATAL,
        SPEC_SECTION,
        "errorProtocolVersionInvalidDeclaration",
        e
      );
    }

    if (context.errorCount() > errorsThen) {
      throw new CBParseFailedException(IS_NOT_FATAL);
    }

    return CBASTProtocolVersion.builder()
      .setVersion(versionNumber)
      .setTypes(items)
      .setLexical(expression.get(1).lexical())
      .build();
  }

  @Override
  public CBASTProtocolDeclaration parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    final var expectingKind =
      "objectProtocolDeclaration";
    final var expectingShapes =
      List.of("(protocol <type-name-decl> <version-decl>...)");

    try (var subContext =
           context.openExpectingOneOf(expectingKind, expectingShapes)) {
      return parseProtocol(
        subContext,
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION,
          SExpressionListType.class)
      );
    }
  }
}
