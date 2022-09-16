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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The base type of type declarations.
 */

public interface CBTypeDeclarationType
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
   * @return The number of type parameters
   */

  default int arity()
  {
    return this.parameters().size();
  }

  /**
   * @return The type parameters, if any, in declaration order
   */

  List<CBTypeParameterType> parameters();

  /**
   * @return The type parameters of the type by name
   */

  default Map<String, CBTypeParameterType> parametersByName()
  {
    return this.parameters()
      .stream()
      .collect(Collectors.toMap(
        CBTypeParameterType::name,
        Function.identity()));
  }

  /**
   * @return The external name, if any
   */

  Optional<CBExternalName> external();

  /**
   * @return The unique ID of the type
   */

  UUID id();

  /**
   * @return Documentation for the type declaration
   */

  List<String> documentation();

  /**
   * @param type The "type" of type declaration (record, variant, etc)
   *
   * @return The unique ID of the type
   */

  default UUID idForType(
    final String type)
  {
    try (var stream = new ByteArrayOutputStream()) {
      stream.writeBytes(type.getBytes(UTF_8));
      stream.write(':');
      stream.writeBytes(this.owner().name().getBytes(UTF_8));
      stream.write(':');
      stream.write(this.name().getBytes(UTF_8));
      return UUID.nameUUIDFromBytes(stream.toByteArray());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
