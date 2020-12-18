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

package com.io7m.cedarbridge.schema.compiled;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A compiled package.
 */

public interface CBPackageType
{
  /**
   * @return The full name of the package, such as {@code com.io7m.cedarbridge}
   */

  String name();

  /**
   * @return The packages imported by this package
   */

  List<CBPackageType> imports();

  /**
   * @return The types declared within this package
   */

  Map<String, CBTypeDeclarationType> types();

  /**
   * @return The protocols declared within this package
   */

  Map<String, CBProtocolDeclarationType> protocols();

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
    return this.protocols()
      .values()
      .stream()
      .flatMap(proto -> proto.versions().values().stream())
      .filter(version -> version.types().contains(type))
      .collect(Collectors.toList());
  }
}
