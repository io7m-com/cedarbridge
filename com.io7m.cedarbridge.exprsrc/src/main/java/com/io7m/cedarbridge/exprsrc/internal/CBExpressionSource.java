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

package com.io7m.cedarbridge.exprsrc.internal;

import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceType;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.api.parser.JSXParserException;
import com.io7m.jsx.api.parser.JSXParserType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

public final class CBExpressionSource implements CBExpressionSourceType
{
  private final CBRecordingUnicodeCharacterReader reader;
  private final URI uri;
  private final InputStream stream;
  private final JSXParserType parser;

  public CBExpressionSource(
    final CBRecordingUnicodeCharacterReader inReader,
    final URI inUri,
    final InputStream inStream,
    final JSXParserType inParser)
  {
    this.reader =
      Objects.requireNonNull(inReader, "reader");
    this.uri =
      Objects.requireNonNull(inUri, "uri");
    this.stream =
      Objects.requireNonNull(inStream, "stream");
    this.parser =
      Objects.requireNonNull(inParser, "parser");
  }

  @Override
  public URI source()
  {
    return this.uri;
  }

  @Override
  public Optional<SExpressionType> parseExpressionOrEOF()
    throws JSXParserException, IOException
  {
    return this.parser.parseExpressionOrEOF();
  }

  @Override
  public Optional<String> showLineFor(
    final int lineNumber)
  {
    return this.reader.showLineFor(lineNumber);
  }

  @Override
  public Optional<String> contextualize(
    final LexicalPosition<?> position)
  {
    Objects.requireNonNull(position, "position");

    final int lineNumber = position.line();
    final var current = this.showLineFor(lineNumber);
    if (current.isPresent()) {
      final var builder = new StringBuilder(current.get());
      builder.append(System.lineSeparator());
      final var end = Math.max(0, position.column() - 1);
      for (int index = 0; index < end; ++index) {
        builder.append(' ');
      }
      builder.append('^');
      return Optional.of(builder.toString());
    }
    return Optional.empty();
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }
}
