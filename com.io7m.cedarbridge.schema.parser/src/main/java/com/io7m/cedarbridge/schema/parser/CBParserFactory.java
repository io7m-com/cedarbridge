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

package com.io7m.cedarbridge.schema.parser;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.schema.parser.internal.CBParser;
import com.io7m.cedarbridge.schema.parser.internal.CBParserServices;
import com.io7m.cedarbridge.schema.parser.internal.CBParserStrings;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jeucreader.UnicodeCharacterReader;
import com.io7m.jsx.api.lexer.JSXLexerComment;
import com.io7m.jsx.api.lexer.JSXLexerConfiguration;
import com.io7m.jsx.api.lexer.JSXLexerSupplierType;
import com.io7m.jsx.api.parser.JSXParserConfiguration;
import com.io7m.jsx.api.parser.JSXParserSupplierType;
import com.io7m.jsx.api.serializer.JSXSerializerSupplierType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class CBParserFactory implements CBParserFactoryType
{
  private final CBStringsType strings;
  private final JSXLexerSupplierType lexers;
  private final JSXParserSupplierType parsers;
  private final JSXSerializerSupplierType serializers;

  public CBParserFactory(
    final CBStringsType inStrings,
    final JSXLexerSupplierType inLexers,
    final JSXParserSupplierType inParsers,
    final JSXSerializerSupplierType inSerializers)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.lexers =
      Objects.requireNonNull(inLexers, "inLexers");
    this.parsers =
      Objects.requireNonNull(inParsers, "parsers");
    this.serializers =
      Objects.requireNonNull(inSerializers, "serializers");
  }

  public CBParserFactory()
  {
    this(
      CBParserStrings.create(),
      CBParserServices.find(JSXLexerSupplierType.class),
      CBParserServices.find(JSXParserSupplierType.class),
      CBParserServices.find(JSXSerializerSupplierType.class)
    );
  }

  @Override
  public CBParserType createParser(
    final Consumer<CBError> errors,
    final URI uri,
    final InputStream stream)
  {
    Objects.requireNonNull(errors, "errors");
    Objects.requireNonNull(uri, "uri");
    Objects.requireNonNull(stream, "stream");

    final var reader =
      UnicodeCharacterReader.newReader(new InputStreamReader(stream, UTF_8));

    final var lexerConfiguration =
      JSXLexerConfiguration.builder()
        .setComments(EnumSet.of(JSXLexerComment.COMMENT_HASH))
        .setFile(URI.create("urn:stdin"))
        .setNewlinesInQuotedStrings(true)
        .setSquareBrackets(true)
        .build();

    final var lexer =
      this.lexers.create(lexerConfiguration, reader);

    final var parserConfiguration =
      JSXParserConfiguration.builder()
        .setPreserveLexical(true)
        .build();

    final var parser =
      this.parsers.create(parserConfiguration, lexer);

    final var serializer =
      this.serializers.create();

    return new CBParser(errors, uri, stream, parser, this.strings, serializer);
  }
}
