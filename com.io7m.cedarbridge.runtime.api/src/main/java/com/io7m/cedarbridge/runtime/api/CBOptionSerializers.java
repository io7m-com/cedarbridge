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

import java.util.List;

/**
 * A factory of serializers of optional values.
 *
 * @param <T> The type of value
 */

public final class CBOptionSerializers<T extends CBSerializableType>
  extends CBAbstractSerializerFactory<CBOptionType<T>>
{
  /**
   * A factory of serializers of optional values.
   */

  public CBOptionSerializers()
  {
    super("com.io7m.cedarbridge", "Option");
  }

  @Override
  protected CBSerializerType<CBOptionType<T>> createActual(
    final CBSerializerDirectoryType directory,
    final List<CBTypeArgument> arguments)
  {
    final var elementType = arguments.get(0);
    return new CBOptionSerializer<>(
      directory.serializerFor(
        elementType.target(),
        elementType.arguments()
      ));
  }

  @Override
  public List<String> typeParameters()
  {
    return List.of("T");
  }
}
