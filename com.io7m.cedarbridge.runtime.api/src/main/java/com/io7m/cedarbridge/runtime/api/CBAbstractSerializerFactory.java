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
import java.util.Objects;

import static com.io7m.cedarbridge.runtime.api.CBTypeArguments.checkArgumentCount;

/**
 * A factory of serializers.
 *
 * @param <T> The type of serialized values
 */

@ProviderType
public abstract class CBAbstractSerializerFactory<T extends CBSerializableType>
  implements CBSerializerFactoryType<T>
{
  private final CBQualifiedTypeName typeName;

  protected CBAbstractSerializerFactory(
    final String inPackageName,
    final String inTypeName)
  {
    this.typeName = CBQualifiedTypeName.of(inPackageName, inTypeName);
  }

  @Override
  public final CBSerializerType<T> create(
    final CBSerializerDirectoryType directory,
    final List<CBTypeArgument> arguments)
  {
    Objects.requireNonNull(directory, "directory");
    Objects.requireNonNull(arguments, "arguments");

    final var qName = this.typeName();
    final var packageName = qName.packageName();
    final var qTypeName = qName.typeName();
    final var expectedCount = this.typeParameters().size();
    final var receivedCount = arguments.size();

    checkArgumentCount(packageName, qTypeName, expectedCount, receivedCount);
    return this.createActual(directory, arguments);
  }

  @Override
  public final CBQualifiedTypeName typeName()
  {
    return this.typeName;
  }

  protected abstract CBSerializerType<T> createActual(
    CBSerializerDirectoryType directory,
    List<CBTypeArgument> arguments);
}
