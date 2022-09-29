/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.cedarbridge.runtime.api;

import java.io.IOException;
import java.util.Objects;

/**
 * The type of serialization exceptions.
 */

public class CBSerializationException extends IOException
{
  private final long byteOffset;
  private final String path;

  /**
   * Create an exception.
   *
   * @param message      The error message
   * @param inByteOffset The byte offset where the error occurred
   * @param inPath       The access path where the error occurred
   */

  public CBSerializationException(
    final String message,
    final long inByteOffset,
    final String inPath)
  {
    super(Objects.requireNonNull(message, "message"));
    this.byteOffset = inByteOffset;
    this.path = Objects.requireNonNull(inPath, "path");
  }

  /**
   * @return The byte offset
   */

  public final long byteOffset()
  {
    return this.byteOffset;
  }

  /**
   * @return The access path
   */

  public final String path()
  {
    return this.path;
  }

  /**
   * Create an exception.
   *
   * @param message      The error message
   * @param cause        The cause
   * @param inByteOffset The byte offset where the error occurred
   * @param inPath       The access path where the error occurred
   */

  public CBSerializationException(
    final String message,
    final Throwable cause,
    final long inByteOffset,
    final String inPath)
  {
    super(
      Objects.requireNonNull(message, "message"),
      Objects.requireNonNull(cause, "cause")
    );
    this.byteOffset = inByteOffset;
    this.path = Objects.requireNonNull(inPath, "path");
  }

  /**
   * Create an exception.
   *
   * @param cause        The cause
   * @param inByteOffset The byte offset where the error occurred
   * @param inPath       The access path where the error occurred
   */

  public CBSerializationException(
    final Throwable cause,
    final long inByteOffset,
    final String inPath)
  {
    super(Objects.requireNonNull(cause, "cause"));
    this.byteOffset = inByteOffset;
    this.path = Objects.requireNonNull(inPath, "path");
  }
}
