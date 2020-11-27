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

package com.io7m.cedarbridge.exprsrc;

import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceFactoryType;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceType;
import com.io7m.cedarbridge.exprsrc.internal.CBExpressionSource;
import com.io7m.cedarbridge.exprsrc.internal.CBExprsrcServices;
import com.io7m.cedarbridge.exprsrc.internal.CBRecordingUnicodeCharacterReader;
import com.io7m.jeucreader.UnicodeCharacterReader;
import com.io7m.jsx.api.lexer.JSXLexerComment;
import com.io7m.jsx.api.lexer.JSXLexerConfiguration;
import com.io7m.jsx.api.lexer.JSXLexerSupplierType;
import com.io7m.jsx.api.parser.JSXParserConfiguration;
import com.io7m.jsx.api.parser.JSXParserSupplierType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The default expression source factory.
 */

public final class CBExpressionSources implements CBExpressionSourceFactoryType
{
  private final JSXLexerSupplierType lexers;
  private final JSXParserSupplierType parsers;

  /**
   * Construct a parser factory.
   *
   * @param inLexers  S-expression lexers
   * @param inParsers S-expression parsers
   */

  public CBExpressionSources(
    final JSXLexerSupplierType inLexers,
    final JSXParserSupplierType inParsers)
  {
    this.lexers =
      Objects.requireNonNull(inLexers, "inLexers");
    this.parsers =
      Objects.requireNonNull(inParsers, "parsers");
  }

  /**
   * Construct an expression source factory. Dependencies are
   * fetched using {@link java.util.ServiceLoader}.
   */

  public CBExpressionSources()
  {
    this(
      CBExprsrcServices.find(JSXLexerSupplierType.class),
      CBExprsrcServices.find(JSXParserSupplierType.class)
    );
  }

  @Override
  public CBExpressionSourceType create(
    final URI uri,
    final InputStream stream)
  {
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(stream, "stream");

    final var reader =
      UnicodeCharacterReader.newReader(new InputStreamReader(stream, UTF_8));
    final var loggingReader =
      new CBRecordingUnicodeCharacterReader(reader);

    final var lexerConfiguration =
      JSXLexerConfiguration.builder()
        .setComments(EnumSet.of(JSXLexerComment.COMMENT_SEMICOLON))
        .setFile(uri)
        .setNewlinesInQuotedStrings(true)
        .setSquareBrackets(true)
        .build();

    final var lexer =
      this.lexers.create(lexerConfiguration, loggingReader);

    final var parserConfiguration =
      JSXParserConfiguration.builder()
        .setPreserveLexical(true)
        .build();

    final var parser =
      this.parsers.create(parserConfiguration, lexer);

    return new CBExpressionSource(loggingReader, uri, stream, parser);
  }
}
