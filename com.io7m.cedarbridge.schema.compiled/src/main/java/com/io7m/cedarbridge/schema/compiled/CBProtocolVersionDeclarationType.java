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

package com.io7m.cedarbridge.schema.compiled;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;

/**
 * The type of protocol version declarations.
 */

public interface CBProtocolVersionDeclarationType
{
  /**
   * @return The declaration that owns this declaration
   */

  CBProtocolDeclarationType owner();

  /**
   * @return The version number
   */

  BigInteger version();

  /**
   * @return The types in this version
   */

  List<CBTypeExprNamedType> types();

  /**
   * @param type The type
   *
   * @return {@code true} if the version contains {@code type}
   */

  default boolean containsType(
    final CBTypeDeclarationType type)
  {
    return this.types()
      .stream()
      .anyMatch(name -> Objects.equals(name.declaration(), type));
  }
}
