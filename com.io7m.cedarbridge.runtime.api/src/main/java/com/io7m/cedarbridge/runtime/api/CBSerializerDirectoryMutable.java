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

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * A mutable serializer directory.
 */

public final class CBSerializerDirectoryMutable
  implements CBSerializerDirectoryType
{
  private final HashMap<CBQualifiedTypeName, CBSerializerFactoryType<?>> serializerFactories;
  private final HashMap<Instantiation, CBSerializerType<?>> serializers;

  /**
   * Construct a serializer directory.
   */

  public CBSerializerDirectoryMutable()
  {
    this.serializerFactories = new HashMap<>();
    this.serializers = new HashMap<>();
  }

  /**
   * Add all serializers in the collection to the directory. This is equivalent
   * to calling {@link #addSerializer(CBSerializerFactoryType)} to every member
   * of the collection in order, accumulating the exceptions and then raising an
   * exception at the end if any exceptions were raised.
   *
   * @param collection The collection
   */

  public void addCollection(
    final CBSerializerCollection collection)
  {
    final var exceptions = new ExceptionTracker<IllegalStateException>();
    for (final var serializer : collection.serializers()) {
      try {
        this.addSerializer(serializer);
      } catch (final IllegalStateException e) {
        exceptions.addException(e);
      }
    }
    exceptions.throwIfNecessary();
  }

  /**
   * Add the serializer to the directory.
   *
   * @param serializer The serializer
   */

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

  private static final class ExceptionTracker<T extends Exception>
  {
    private T exception;

    ExceptionTracker()
    {

    }

    public void addException(
      final T nextException)
    {
      Objects.requireNonNull(nextException, "exception");

      if (this.exception == null) {
        this.exception = nextException;
      } else {
        this.exception.addSuppressed(nextException);
      }
    }

    public void throwIfNecessary()
      throws T
    {
      if (this.exception != null) {
        throw this.exception;
      }
    }
  }

  private static final class Instantiation
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
