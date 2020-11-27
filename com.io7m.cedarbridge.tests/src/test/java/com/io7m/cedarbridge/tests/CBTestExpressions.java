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

package com.io7m.cedarbridge.tests;

import com.io7m.jeucreader.UnicodeCharacterReader;
import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.api.lexer.JSXLexerComment;
import com.io7m.jsx.api.lexer.JSXLexerConfiguration;
import com.io7m.jsx.api.parser.JSXParserConfiguration;
import com.io7m.jsx.api.parser.JSXParserException;
import com.io7m.jsx.lexer.JSXLexerSupplier;
import com.io7m.jsx.parser.JSXParserSupplier;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.EnumSet;

public final class CBTestExpressions
{
  private CBTestExpressions()
  {

  }

  public static SExpressionType expression(
    final String text)
  {
    try {
      final var parsers =
        new JSXParserSupplier();
      final var lexers =
        new JSXLexerSupplier();

      final var reader =
        UnicodeCharacterReader.newReader(
          new StringReader(text)
        );

      final var lexerConfiguration =
        JSXLexerConfiguration.builder()
          .setComments(EnumSet.of(JSXLexerComment.COMMENT_HASH))
          .setFile(URI.create("urn:stdin"))
          .setNewlinesInQuotedStrings(true)
          .setSquareBrackets(true)
          .build();

      final var lexer =
        lexers.create(lexerConfiguration, reader);

      final var parserConfiguration =
        JSXParserConfiguration.builder()
          .setPreserveLexical(true)
          .build();

      final var parser =
        parsers.create(parserConfiguration, lexer);

      return parser.parseExpression();
    } catch (JSXParserException | IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
