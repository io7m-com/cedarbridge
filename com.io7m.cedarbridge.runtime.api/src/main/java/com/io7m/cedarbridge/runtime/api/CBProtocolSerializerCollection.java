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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The default implementation of the {@link CBProtocolSerializerCollectionType} interface.
 *
 * @param <T> The type of serialized protocol messages
 */

public final class CBProtocolSerializerCollection<T extends CBProtocolMessageType>
  implements CBProtocolSerializerCollectionType<T>
{
  private final UUID id;
  private final List<CBProtocolSerializerFactoryType<T>> serializers;
  private final long lowestVersion;
  private final long highestVersion;

  private CBProtocolSerializerCollection(
    final UUID inId,
    final List<CBProtocolSerializerFactoryType<T>> inSerializers,
    final long inLowestVersion,
    final long inHighestVersion)
  {
    this.id =
      Objects.requireNonNull(inId, "id");
    this.serializers =
      List.copyOf(Objects.requireNonNull(inSerializers, "serializers"));
    this.lowestVersion =
      inLowestVersion;
    this.highestVersion =
      inHighestVersion;

    if (this.serializers.isEmpty()) {
      throw new IllegalArgumentException(
        "Serializer collection cannot be empty.");
    }
  }

  /**
   * @param id  The protocol ID
   * @param <T> The type of serialized protocol messages
   *
   * @return A new collection builder
   */

  public static <T extends CBProtocolMessageType> CBProtocolSerializerCollectionBuilderType<T> builder(
    final UUID id)
  {
    return new Builder<>(id);
  }

  @Override
  public UUID id()
  {
    return this.id;
  }

  @Override
  public long versionLower()
  {
    return this.lowestVersion;
  }

  @Override
  public long versionUpper()
  {
    return this.highestVersion;
  }

  @Override
  public List<CBProtocolSerializerFactoryType<T>> factories()
  {
    return this.serializers;
  }

  private static final class Builder<T extends CBProtocolMessageType>
    implements CBProtocolSerializerCollectionBuilderType<T>
  {
    private final ArrayList<CBProtocolSerializerFactoryType<T>> serializers;
    private final UUID id;
    private long lowestVersion;
    private long highestVersion;

    Builder(
      final UUID inId)
    {
      this.id =
        Objects.requireNonNull(inId, "id");
      this.serializers =
        new ArrayList<>();

      this.lowestVersion = 0xffff_ffff_ffff_ffffL;
      this.highestVersion = 0L;
    }

    @Override
    public CBProtocolSerializerCollectionBuilderType<T> addFactory(
      final CBProtocolSerializerFactoryType<? extends T> factory)
    {
      Objects.requireNonNull(factory, "factory");

      if (!Objects.equals(factory.id(), this.id)) {
        final var lineSeparator = System.lineSeparator();
        throw new IllegalArgumentException(
          new StringBuilder(128)
            .append("Serializer factory has incorrect ID.")
            .append(lineSeparator)
            .append("  Expected: ")
            .append(this.id)
            .append(lineSeparator)
            .append("  Received: ")
            .append(factory.id())
            .append(lineSeparator)
            .toString()
        );
      }

      final var existing =
        this.serializers.stream()
          .filter(f -> f.version() == factory.version())
          .findFirst();

      if (existing.isPresent()) {
        final var lineSeparator = System.lineSeparator();
        throw new IllegalArgumentException(
          new StringBuilder(128)
            .append("Serializer factory version conflict.")
            .append(lineSeparator)
            .append("  Version: ")
            .append(factory.version())
            .append(lineSeparator)
            .append("  Existing: ")
            .append(existing.get().getClass())
            .append(lineSeparator)
            .toString()
        );
      }

      if (Long.compareUnsigned(factory.version(), this.lowestVersion) < 0) {
        this.lowestVersion = factory.version();
      }
      if (Long.compareUnsigned(factory.version(), this.highestVersion) > 0) {
        this.highestVersion = factory.version();
      }

      @SuppressWarnings("unchecked") final var softened =
        (CBProtocolSerializerFactoryType<T>) factory;

      this.serializers.add(softened);
      return this;
    }

    @Override
    public CBProtocolSerializerCollectionType<T> build()
    {
      return new CBProtocolSerializerCollection<T>(
        this.id,
        this.serializers,
        this.lowestVersion,
        this.highestVersion
      );
    }
  }
}
