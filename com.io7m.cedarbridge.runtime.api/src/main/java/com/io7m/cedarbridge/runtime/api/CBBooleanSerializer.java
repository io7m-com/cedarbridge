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
 * A serializer of boolean values.
 */

public final class CBBooleanSerializer
  extends CBAbstractSerializer<CBBooleanType>
{
  /**
   * A serializer of boolean values.
   */

  public CBBooleanSerializer()
  {

  }

  @Override
  public void serialize(
    final CBSerializationContextType context,
    final CBBooleanType value)
    throws IOException
  {
    if (value instanceof CBTrue t) {
      CBTrue.serialize(context, t);
      return;
    }
    if (value instanceof CBFalse f) {
      CBFalse.serialize(context, f);
      return;
    }
    throw new IllegalStateException(
      String.format("Unrecognized variant case: %s", value.getClass())
    );
  }

  @Override
  public CBBooleanType deserialize(
    final CBSerializationContextType context)
    throws IOException
  {
    final var index = context.readVariantIndex();
    return switch (index) {
      case CBFalse.VARIANT_INDEX -> CBFalse.deserialize(context);
      case CBTrue.VARIANT_INDEX -> CBTrue.deserialize(context);
      default -> throw new IllegalStateException(
        String.format(
          "Unrecognized variant index: %d", Integer.valueOf(index))
      );
    };
  }
}
