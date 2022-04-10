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

package com.io7m.cedarbridge.exprsrc.internal;

import com.io7m.jeucreader.InvalidSurrogatePair;
import com.io7m.jeucreader.MissingLowSurrogate;
import com.io7m.jeucreader.OrphanLowSurrogate;
import com.io7m.jeucreader.UnicodeCharacterReaderPushBackType;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import static com.io7m.cedarbridge.exprsrc.internal.CBRecordingUnicodeCharacterReader.State.IN_CR;
import static com.io7m.cedarbridge.exprsrc.internal.CBRecordingUnicodeCharacterReader.State.IN_TEXT;

/**
 * A character reader that records characters.
 */

public final class CBRecordingUnicodeCharacterReader
  implements UnicodeCharacterReaderPushBackType
{
  private final UnicodeCharacterReaderPushBackType delegate;
  private final StringBuilder lineBuffer;
  private final Map<Integer, String> lines;
  private State state;
  private int lineNumber;

  /**
   * A character reader that records characters.
   *
   * @param inDelegate The underlying character reader
   */

  public CBRecordingUnicodeCharacterReader(
    final UnicodeCharacterReaderPushBackType inDelegate)
  {
    this.delegate =
      Objects.requireNonNull(inDelegate, "delegate");
    this.lineBuffer =
      new StringBuilder(128);

    this.lines = new TreeMap<>();
    this.lineNumber = 0;
    this.state = IN_TEXT;
  }

  @Override
  public void pushCodePoint(
    final int c)
  {
    this.delegate.pushCodePoint(c);
  }

  /**
   * Show the line with the given number.
   *
   * @param target The number
   *
   * @return The line, if any
   */

  public Optional<String> showLineFor(
    final int target)
  {
    return Optional.ofNullable(
      this.lines.get(Integer.valueOf(target))
    );
  }

  @Override
  public int readCodePoint()
    throws
    IOException,
    InvalidSurrogatePair,
    MissingLowSurrogate,
    OrphanLowSurrogate
  {
    final var codePoint = this.delegate.readCodePoint();

    switch (this.state) {
      case IN_TEXT: {
        switch (codePoint) {
          case '\r': {
            this.state = IN_CR;
            break;
          }
          case '\n':
          case -1: {
            this.finishLine();
            break;
          }
          default: {
            this.lineBuffer.appendCodePoint(codePoint);
            this.lines.put(
              Integer.valueOf(this.lineNumber),
              this.lineBuffer.toString()
            );
            break;
          }
        }
        break;
      }
      case IN_CR: {
        switch (codePoint) {
          case '\r': {
            this.state = IN_CR;
            this.finishLine();
            break;
          }
          case -1:
          case '\n': {
            this.finishLine();
            break;
          }
          default: {
            this.finishLine();
            this.lineBuffer.appendCodePoint(codePoint);
            this.lines.put(
              Integer.valueOf(this.lineNumber),
              this.lineBuffer.toString()
            );
            break;
          }
        }
        break;
      }
    }

    return codePoint;
  }

  private void finishLine()
  {
    this.state = IN_TEXT;
    this.lines.put(
      Integer.valueOf(this.lineNumber),
      this.lineBuffer.toString()
    );
    this.lineBuffer.setLength(0);
    ++this.lineNumber;
  }

  enum State
  {
    IN_TEXT,
    IN_CR
  }
}
