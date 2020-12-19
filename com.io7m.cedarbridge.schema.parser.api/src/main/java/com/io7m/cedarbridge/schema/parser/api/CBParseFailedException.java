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

package com.io7m.cedarbridge.schema.parser.api;

import java.util.Objects;

/**
 * Parsing failed.
 */

public final class CBParseFailedException extends Exception
{
  /**
   * An indication of whether or not the exception is fatal.
   */

  public enum Fatal
  {
    /**
     * The exception is fatal.
     */

    IS_FATAL,

    /**
     * The exception is not fatal.
     */

    IS_NOT_FATAL
  }

  private final Fatal fatality;

  /**
   * Construct an exception.
   *
   * @param inFatality The degree of fatality
   */

  public CBParseFailedException(
    final Fatal inFatality)
  {
    this.fatality = Objects.requireNonNull(inFatality, "fatality");
  }

  /**
   * @return The fatality of the exception
   */

  public Fatal fatality()
  {
    return this.fatality;
  }
}
