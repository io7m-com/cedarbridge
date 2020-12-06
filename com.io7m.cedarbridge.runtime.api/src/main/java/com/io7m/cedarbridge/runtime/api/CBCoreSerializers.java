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

package com.io7m.cedarbridge.runtime.api;

/**
 * Serializers for the core Cedarbridge package types.
 */

public final class CBCoreSerializers
{
  private static final CBSerializerCollection COLLECTION;

  static {
    COLLECTION =
      CBSerializerCollection.builder()
        .setPackageName("com.io7m.cedarbridge")
        .addSerializers(new CBByteArraySerializers())
        .addSerializers(new CBFloat16Serializers())
        .addSerializers(new CBFloat32Serializers())
        .addSerializers(new CBFloat64Serializers())
        .addSerializers(new CBIntegerSigned16Serializers())
        .addSerializers(new CBIntegerSigned32Serializers())
        .addSerializers(new CBIntegerSigned64Serializers())
        .addSerializers(new CBIntegerSigned8Serializers())
        .addSerializers(new CBIntegerUnsigned16Serializers())
        .addSerializers(new CBIntegerUnsigned32Serializers())
        .addSerializers(new CBIntegerUnsigned64Serializers())
        .addSerializers(new CBIntegerUnsigned8Serializers())
        .addSerializers(new CBListSerializers<>())
        .addSerializers(new CBMapEntrySerializers<>())
        .addSerializers(new CBMapSerializers<>())
        .addSerializers(new CBOptionSerializers<>())
        .addSerializers(new CBStringSerializers())
        .build();
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
