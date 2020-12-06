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

import org.osgi.annotation.versioning.ProviderType;

import java.util.List;

/**
 * A serializer factory.
 *
 * @param <T> The type of serialized values
 */

@ProviderType
public interface CBSerializerFactoryType<T extends CBSerializableType>
{
  /**
   * Declare the type name of the type. This must be a valid Cedarbridge
   * qualified type name, and is the name used to refer to the type from other
   * Cedarbridge type declarations.
   *
   * @return The type name of the type
   */

  CBQualifiedTypeName typeName();

  /**
   * Declare the parameters of the type. The names must be valid Cedarbridge
   * type parameter names.
   *
   * @return The parameters of the type, in declaration order
   */

  default List<String> typeParameters()
  {
    return List.of();
  }

  /**
   * Create a new serializer, instantiating it with the given type arguments.
   *
   * @param directory The directory of available serializers
   * @param arguments The arguments
   *
   * @return A new serializer
   */

  CBSerializerType<T> create(
    CBSerializerDirectoryType directory,
    List<CBTypeArgument> arguments);
}
