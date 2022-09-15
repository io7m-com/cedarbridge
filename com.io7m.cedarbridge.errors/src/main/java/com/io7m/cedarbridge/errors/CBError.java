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

package com.io7m.cedarbridge.errors;

import com.io7m.jlexing.core.LexicalPosition;

import java.net.URI;
import java.util.Objects;

/**
 * @param lexical   The lexical information
 * @param severity  The error severity
 * @param exception The exception associated with the error
 * @param errorCode The error code
 * @param message   The error message
 */

public record CBError(
  LexicalPosition<URI> lexical,
  CBError.Severity severity,
  Exception exception,
  String errorCode,
  String message)
{
  /**
   * @param lexical   The lexical information
   * @param severity  The error severity
   * @param exception The exception associated with the error
   * @param errorCode The error code
   * @param message   The error message
   */

  public CBError
  {
    Objects.requireNonNull(lexical, "lexical");
    Objects.requireNonNull(severity, "severity");
    Objects.requireNonNull(exception, "exception");
    Objects.requireNonNull(errorCode, "errorCode");
    Objects.requireNonNull(message, "message");
  }

  /**
   * The error severity.
   */

  public enum Severity
  {
    /**
     * The error is a fatal error.
     */

    ERROR,

    /**
     * The error is a warning.
     */

    WARNING
  }
}
