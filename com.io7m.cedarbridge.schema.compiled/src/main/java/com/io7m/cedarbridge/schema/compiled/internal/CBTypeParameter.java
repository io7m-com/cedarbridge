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

import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBTypeParameterType;
import com.io7m.cedarbridge.schema.names.CBTypeParameterNames;
import com.io7m.jaffirm.core.Preconditions;

import java.util.Objects;

/**
 * A type parameter.
 */

public final class CBTypeParameter implements CBTypeParameterType
{
  private final String name;
  private final int index;
  private CBTypeDeclarationType owner;

  /**
   * Construct a type parameter.
   *
   * @param inName  The parameter name
   * @param inIndex The parameter index
   */

  public CBTypeParameter(
    final String inName,
    final int inIndex)
  {
    this.name = Objects.requireNonNull(inName, "name");
    this.index = inIndex;

    Preconditions.checkPreconditionV(
      inName,
      CBTypeParameterNames.INSTANCE.isValid(inName),
      "Type parameter name must be valid"
    );
  }

  /**
   * Set the owner of the type.
   *
   * @param type The owner declaration
   */

  public void setOwner(
    final CBTypeDeclarationType type)
  {
    this.owner = Objects.requireNonNull(type, "type");
  }

  @Override
  public int index()
  {
    return this.index;
  }

  @Override
  public String name()
  {
    return this.name;
  }

  @Override
  public CBTypeDeclarationType owner()
  {
    return this.owner;
  }
}
