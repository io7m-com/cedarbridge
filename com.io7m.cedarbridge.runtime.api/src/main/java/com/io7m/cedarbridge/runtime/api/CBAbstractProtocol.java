/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A convenient base implementation for protocols.
 *
 * @param <T> The type of messages in the protocol
 */

public abstract class CBAbstractProtocol<T extends CBProtocolMessageType>
  implements CBProtocolType<T>
{
  private final Class<T> msgClazz;
  private final String packageName;
  private final String protoName;
  private final List<CBProtocolMessageVersionedSerializerType<T>> serializers;
  private final UUID identifier;
  private final Map<Class<T>, CBProtocolMessageVersionedSerializerType<T>> serializersByMessageClass;
  private final Map<BigInteger, CBProtocolMessageVersionedSerializerType<T>> serializersByVersion;
  private final SortedSet<CBProtocolCoordinates> identifiers;
  private final SortedSet<BigInteger> supportedVersions;

  protected static <T extends CBProtocolMessageType> CBProtocolMessageVersionedSerializerType<T>
  widen(
    final CBProtocolMessageVersionedSerializerType<? extends T> p)
  {
    return (CBProtocolMessageVersionedSerializerType<T>) p;
  }

  protected CBAbstractProtocol(
    final Class<T> inMsgClazz,
    final String inPackageName,
    final String inProtoName,
    final List<CBProtocolMessageVersionedSerializerType<T>> inSerializers)
  {
    this.msgClazz =
      Objects.requireNonNull(inMsgClazz, "msgClazz");
    this.packageName =
      Objects.requireNonNull(inPackageName, "packageName");
    this.protoName =
      Objects.requireNonNull(inProtoName, "protoName");
    this.serializers =
      Objects.requireNonNull(inSerializers, "serializers");
    this.identifier =
      UUID.nameUUIDFromBytes(
        ("%s:%s".formatted(this.packageName, this.protoName))
          .getBytes(UTF_8)
      );

    this.serializersByMessageClass =
      this.serializers.stream()
        .collect(Collectors.toUnmodifiableMap(
          CBProtocolMessageSerializerType::messageClass,
          Function.identity()
        ));
    this.serializersByVersion =
      this.serializers.stream()
        .collect(Collectors.toUnmodifiableMap(
          CBProtocolMessageVersionedSerializerType::version,
          Function.identity()
        ));
    this.identifiers =
      Collections.unmodifiableSortedSet(
        new TreeSet<>(
          this.serializers.stream()
            .map(v -> new CBProtocolCoordinates(this.identifier, v.version()))
            .collect(Collectors.toUnmodifiableSet())
        ));

    this.supportedVersions =
      Collections.unmodifiableSortedSet(
        new TreeSet<>(this.serializersByVersion.keySet())
      );
  }

  @Override
  public final SortedSet<CBProtocolCoordinates> protocols()
  {
    return this.identifiers;
  }

  @Override
  public final SortedSet<BigInteger> protocolVersions()
  {
    return this.supportedVersions;
  }

  @Override
  public final Optional<CBProtocolMessageVersionedSerializerType<T>>
  serializerForMessageClass(
    final Class<T> messageClass)
  {
    Objects.requireNonNull(messageClass, "messageClass");
    return Optional.ofNullable(this.serializersByMessageClass.get(messageClass));
  }

  @Override
  public final Optional<CBProtocolMessageVersionedSerializerType<T>>
  serializerForProtocolVersion(
    final BigInteger version)
  {
    Objects.requireNonNull(version, "version");
    return Optional.ofNullable(this.serializersByVersion.get(version));
  }

  @Override
  public final Class<T> messageClass()
  {
    return this.msgClazz;
  }

  @Override
  public final UUID protocolId()
  {
    return this.identifier;
  }
}
