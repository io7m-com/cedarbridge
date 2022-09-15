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

import java.util.List;

/**
 * Serializers for the core Cedarbridge package types.
 */

public final class CBCoreSerializers
{
  private static final CBSerializerCollection COLLECTION;

  static {
    COLLECTION =
      new CBSerializerCollection(
        "com.io7m.cedarbridge",
        List.of(
          new CBByteArraySerializers(),
          new CBFloat16Serializers(),
          new CBFloat32Serializers(),
          new CBFloat64Serializers(),
          new CBIntegerSigned16Serializers(),
          new CBIntegerSigned32Serializers(),
          new CBIntegerSigned64Serializers(),
          new CBIntegerSigned8Serializers(),
          new CBIntegerUnsigned16Serializers(),
          new CBIntegerUnsigned32Serializers(),
          new CBIntegerUnsigned64Serializers(),
          new CBIntegerUnsigned8Serializers(),
          new CBListSerializers<>(),
          new CBMapEntrySerializers<>(),
          new CBMapSerializers<>(),
          new CBOptionSerializers<>(),
          new CBStringSerializers()
        ));
  }

  private CBCoreSerializers()
  {

  }

  /**
   * @return The serializer collection for the core package
   */

  public static CBSerializerCollection get()
  {
    return COLLECTION;
  }
}
