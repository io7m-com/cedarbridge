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
import java.util.Formattable;

/**
 * The boolean type.
 */

public sealed interface CBBooleanType
  extends Formattable, CBSerializableType
  permits CBFalse, CBTrue
{
  /**
   * @return This value as a Java boolean
   */

  boolean asBoolean();

  /**
   * A boolean from the given boolean value.
   *
   * @param b The Java boolean
   *
   * @return The boolean
   */

  static CBBooleanType fromBoolean(
    final boolean b)
  {
    return b ? new CBTrue() : new CBFalse();
  }

  /**
   * Serialize the given value.
   *
   * @param context The serialization context
   * @param x       The value
   *
   * @throws IOException On errors
   */

  @CBSerializerMethod
  static void serialize(
    final CBSerializationContextType context,
    final CBBooleanType x)
    throws IOException
  {
    if (x instanceof CBTrue t) {
      CBTrue.serialize(context, t);
      return;
    }
    if (x instanceof CBFalse f) {
      CBFalse.serialize(context, f);
      return;
    }
    throw new IllegalStateException(
      String.format("Unrecognized variant case: %s", x.getClass())
    );
  }

  /**
   * Deserialize the given value.
   *
   * @param context The serialization context
   *
   * @return The deserialized value
   *
   * @throws IOException On errors
   */

  @CBDeserializerMethod
  static CBBooleanType deserialize(
    final CBSerializationContextType context)
    throws IOException
  {
    final var index = context.readVariantIndex();
    return switch (index) {
      case CBTrue.VARIANT_INDEX -> CBTrue.deserialize(context);
      case CBFalse.VARIANT_INDEX -> CBFalse.deserialize(context);
      default -> throw context.errorUnrecognizedVariantIndex(CBBooleanType.class, index);
    };
  }
}
