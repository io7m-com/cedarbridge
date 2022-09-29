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

package com.io7m.cedarbridge.runtime.api;

import java.io.IOException;

/**
 * The serialization context related to errors during serialization.
 */

public interface CBSerializationContextErrorType
{
  /**
   * An unrecognized variant index was encountered.
   *
   * @param reader The class attempting the read
   * @param index  The index
   *
   * @return An exception detailing the errors
   */

  IOException errorUnrecognizedVariantIndex(
    Class<?> reader,
    int index);

  /**
   * An unrecognized variant case class was encountered.
   *
   * @param writer The class attempting the write
   * @param clazz  The class
   *
   * @return An exception detailing the errors
   */

  IOException errorUnrecognizedVariantCaseClass(
    Class<?> writer,
    Class<?> clazz);
}
