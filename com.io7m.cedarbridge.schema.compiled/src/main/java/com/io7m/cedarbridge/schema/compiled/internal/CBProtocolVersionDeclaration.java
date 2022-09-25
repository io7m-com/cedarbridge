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

package com.io7m.cedarbridge.schema.compiled.internal;

import com.io7m.cedarbridge.schema.compiled.CBProtocolDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolVersionDeclarationType;
import com.io7m.jaffirm.core.Preconditions;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;

/**
 * A protocol version declaration.
 */

public final class CBProtocolVersionDeclaration
  implements CBProtocolVersionDeclarationType
{
  private final BigInteger version;
  private final CBProtocolDeclarationType owner;
  private final Set<CBTypeExprNamedType> types;

  /**
   * Construct a version declaration.
   *
   * @param inOwner   The owner
   * @param inVersion The version
   */

  public CBProtocolVersionDeclaration(
    final CBProtocolDeclarationType inOwner,
    final BigInteger inVersion)
  {
    this.owner =
      Objects.requireNonNull(inOwner, "owner");
    this.version =
      Objects.requireNonNull(inVersion, "version");
    this.types =
      new HashSet<>();
  }

  @Override
  public CBProtocolDeclarationType owner()
  {
    return this.owner;
  }

  @Override
  public BigInteger version()
  {
    return this.version;
  }

  @Override
  public List<CBTypeExprNamedType> typesInOrder()
  {
    return this.types.stream()
      .sorted(Comparator.comparing(o -> o.declaration().name()))
      .toList();
  }

  /**
   * Add a type to the declaration.
   *
   * @param type The type
   */

  public void addType(
    final CBTypeExprNamedType type)
  {
    Preconditions.checkPreconditionV(
      this.types.size() < 255,
      "Number of types must be <= 255"
    );

    this.types.add(type);
  }
}
