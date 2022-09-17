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

package com.io7m.cedarbridge.schema.compiled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The base type of protocol declarations.
 */

public interface CBProtocolDeclarationType
{
  /**
   * @return The package that owns this declaration
   */

  CBPackageType owner();

  /**
   * @return The package-unique declaration name
   */

  String name();

  /**
   * @return The protocols by version
   */

  Map<BigInteger, CBProtocolVersionDeclarationType> versions();

  /**
   * Find all protocol versions to which the given type belongs.
   *
   * @param type The type
   *
   * @return The protocol versions
   */

  default List<CBProtocolVersionDeclarationType> protocolVersionsForType(
    final CBTypeDeclarationType type)
  {
    return this.versions()
      .values()
      .stream()
      .filter(version -> version.containsType(type))
      .collect(Collectors.toList());
  }

  /**
   * @return The unique ID of the protocol
   */

  default UUID id()
  {
    try (var stream = new ByteArrayOutputStream()) {
      stream.writeBytes("protocol:".getBytes(UTF_8));
      stream.writeBytes(this.owner().name().getBytes(UTF_8));
      stream.write(':');
      stream.write(this.name().getBytes(UTF_8));
      return UUID.nameUUIDFromBytes(stream.toByteArray());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /**
   * @return Documentation for the protocol declaration
   */

  List<String> documentation();
}
