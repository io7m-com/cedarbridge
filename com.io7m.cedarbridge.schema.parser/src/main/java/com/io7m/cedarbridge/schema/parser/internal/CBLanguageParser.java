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

import com.io7m.cedarbridge.schema.ast.CBASTLanguage;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_FATAL;

/**
 * A parser for language declarations.
 */

public final class CBLanguageParser
  implements CBElementParserType<CBASTLanguage>
{
  private static final String EXPECTING_KIND =
    "objectLanguage";

  private static final List<String> EXPECTING_SHAPES =
    List.of("(language <language-name> <language-major> <language-minor>)");

  private static final Optional<UUID> SPEC_SECTION_LANGUAGE_NAME =
    uuid("9835ba13-ffc3-44ac-90df-1e3e5cc120fa");
  private static final Optional<UUID> SPEC_SECTION_VERSION_MAJOR =
    uuid("a028f13d-dc6f-4fd5-872b-dfa41318ebe4");
  private static final Optional<UUID> SPEC_SECTION_VERSION_MINOR =
    uuid("34c52233-ee42-4dec-99ac-9997d7cd4bbe");
  private static final Optional<UUID> SPEC_SECTION =
    uuid("5ce707c0-0cfe-4085-86db-1e5968049b92");

  /**
   * A parser for language declarations.
   */

  public CBLanguageParser()
  {

  }

  private static CBASTLanguage parseLanguage(
    final CBParseContextType context,
    final SExpressionListType list)
    throws CBParseFailedException
  {
    if (list.size() != 4) {
      throw context.failed(
        list,
        IS_FATAL,
        SPEC_SECTION,
        "errorDeclarationInvalid"
      );
    }

    context.checkExpressionIsKeyword(
      list.get(0), SPEC_SECTION, "language", "errorLanguageKeyword");

    final var langName =
      context.checkExpressionIs(
        list.get(1),
        SPEC_SECTION_LANGUAGE_NAME,
        SExpressionSymbolType.class
      ).text();

    final var langMajorText =
      context.checkExpressionIs(
        list.get(2),
        SPEC_SECTION_VERSION_MAJOR,
        SExpressionSymbolType.class
      ).text();

    final var langMinorText =
      context.checkExpressionIs(
        list.get(3),
        SPEC_SECTION_VERSION_MINOR,
        SExpressionSymbolType.class
      ).text();

    if (!Objects.equals(langName, "cedarbridge")) {
      throw context.failed(
        list.get(1),
        IS_FATAL,
        SPEC_SECTION_LANGUAGE_NAME,
        "errorLanguageBadName"
      );
    }

    final BigInteger langMajor;
    try {
      langMajor = new BigInteger(langMajorText);
    } catch (final Exception e) {
      throw context.failed(
        list.get(2),
        IS_FATAL,
        SPEC_SECTION_VERSION_MAJOR,
        "errorLanguageBadVersion",
        e
      );
    }

    final BigInteger langMinor;
    try {
      langMinor = new BigInteger(langMinorText);
    } catch (final Exception e) {
      throw context.failed(
        list.get(3),
        IS_FATAL,
        SPEC_SECTION_VERSION_MINOR,
        "errorLanguageBadVersion",
        e
      );
    }

    if (!Objects.equals(langMajor, BigInteger.ONE)) {
      throw context.failed(
        list.get(2),
        IS_FATAL,
        SPEC_SECTION_VERSION_MAJOR,
        "errorLanguageBadVersion"
      );
    }

    if (!Objects.equals(langMinor, BigInteger.ZERO)) {
      throw context.failed(
        list.get(3),
        IS_FATAL,
        SPEC_SECTION_VERSION_MINOR,
        "errorLanguageBadVersion"
      );
    }

    return CBASTLanguage.builder()
      .setLexical(list.lexical())
      .setLanguage(langName)
      .setMajor(langMajor)
      .setMinor(langMinor)
      .build();
  }

  @Override
  public CBASTLanguage parse(
    final CBParseContextType context,
    final SExpressionType expression)
    throws CBParseFailedException
  {
    try (var subContext =
           context.openExpectingOneOf(EXPECTING_KIND, EXPECTING_SHAPES)) {
      return parseLanguage(
        subContext,
        subContext.checkExpressionIs(
          expression,
          SPEC_SECTION,
          SExpressionListType.class
        ));
    }
  }
}
