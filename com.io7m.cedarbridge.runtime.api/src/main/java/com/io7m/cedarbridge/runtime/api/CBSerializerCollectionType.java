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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;
import org.osgi.annotation.versioning.ProviderType;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@ProviderType
@ImmutablesStyleType
@Value.Immutable
public interface CBSerializerCollectionType
{
  private static void check(
    final boolean condition,
    final String format,
    final Object... arguments)
  {
    if (!condition) {
      throw new IllegalArgumentException(String.format(format, arguments));
    }
  }

  String packageName();

  List<CBSerializerFactoryType<?>> serializers();

  @Value.Check
  default void checkPreconditions()
  {
    final var names =
      new HashMap<String, CBSerializerFactoryType<?>>(
        this.serializers().size()
      );

    for (final var serializer : this.serializers()) {
      final var typeName = serializer.typeName();

      check(
        Objects.equals(typeName.packageName(), this.packageName()),
        "Serializer %s must have package name %s (was %s)",
        serializer.getClass(),
        typeName,
        this.packageName()
      );
      final var existing = names.get(typeName.typeName());
      if (existing != null) {
        check(
          false,
          "Serializer type name %s appears in classes %s and %s",
          serializer.typeName(),
          serializer.getClass(),
          existing.getClass()
        );
      }
      names.put(typeName.typeName(), serializer);
    }
  }
}
