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

package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.runtime.api.CBQualifiedTypeName;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.runtime.api.CBSerializerDirectoryType;
import com.io7m.cedarbridge.runtime.api.CBSerializerFactoryType;
import com.io7m.cedarbridge.runtime.api.CBSerializerType;
import com.io7m.cedarbridge.runtime.api.CBTypeArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CBFakeSerializerDirectory
  implements CBSerializerDirectoryType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBFakeSerializerDirectory.class);

  private final HashMap<CBQualifiedTypeName, CBSerializerFactoryType<?>> serializerFactories;
  private final HashMap<Instantiation, CBSerializerType<?>> serializers;

  public CBFakeSerializerDirectory()
  {
    this.serializerFactories = new HashMap<>();
    this.serializers = new HashMap<>();
  }

  public void addSerializer(
    final CBSerializerFactoryType<?> serializer)
  {
    final var typeName = serializer.typeName();
    if (this.serializerFactories.containsKey(typeName)) {
      throw new IllegalStateException(String.format(
        "Serializer %s already registered",
        typeName)
      );
    }

    this.serializerFactories.put(typeName, serializer);
  }

  @Override
  public <T extends CBSerializableType> CBSerializerType<T> serializerFor(
    final CBQualifiedTypeName typeName,
    final List<CBTypeArgument> arguments)
  {
    Objects.requireNonNull(typeName, "typeName");
    Objects.requireNonNull(arguments, "arguments");

    LOG.debug(
      "serializerFor: {} {}",
      String.format("%s", typeName),
      arguments.stream()
        .map(x -> String.format("%s", x))
        .collect(Collectors.toList())
    );

    final var instantiation = new Instantiation(typeName, arguments);
    final var result = this.serializers.get(instantiation);
    if (result == null) {
      final var factory =
        this.serializerFactories.get(typeName);

      if (factory == null) {
        throw new IllegalArgumentException(
          String.format("No such serializer: %s", typeName)
        );
      }

      final var serializer = factory.create(this, arguments);
      this.serializers.put(instantiation, serializer);
      return (CBSerializerType<T>) serializer;
    }
    return (CBSerializerType<T>) result;
  }

  private final class Instantiation
  {
    private final CBQualifiedTypeName typeName;
    private final List<CBTypeArgument> arguments;

    Instantiation(
      final CBQualifiedTypeName inTypeName,
      final List<CBTypeArgument> inArguments)
    {
      this.typeName =
        Objects.requireNonNull(inTypeName, "typeName");
      this.arguments =
        Objects.requireNonNull(inArguments, "arguments");
    }

    @Override
    public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o == null || !Objects.equals(this.getClass(), o.getClass())) {
        return false;
      }
      final Instantiation that = (Instantiation) o;
      return this.typeName.equals(that.typeName)
        && this.arguments.equals(that.arguments);
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(this.typeName, this.arguments);
    }
  }
}
