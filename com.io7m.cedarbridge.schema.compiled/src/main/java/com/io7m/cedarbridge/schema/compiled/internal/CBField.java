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

import com.io7m.cedarbridge.schema.compiled.CBFieldOwnerType;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.cedarbridge.schema.names.CBFieldNames;
import com.io7m.jaffirm.core.Preconditions;

import java.util.List;
import java.util.Objects;

/**
 * A field.
 */

public final class CBField implements CBFieldType
{
  private final String name;
  private final CBTypeExpressionType type;
  private CBFieldOwnerType owner;
  private List<String> documentation;

  /**
   * Construct a field.
   *
   * @param inName          The name
   * @param inDocumentation The documentation
   * @param inType          The type
   */

  public CBField(
    final String inName,
    final List<String> inDocumentation,
    final CBTypeExpressionType inType)
  {
    this.name =
      Objects.requireNonNull(inName, "name");
    this.type =
      Objects.requireNonNull(inType, "type");
    this.documentation =
      Objects.requireNonNull(inDocumentation, "documentation");

    Preconditions.checkPreconditionV(
      inName,
      CBFieldNames.INSTANCE.isValid(inName),
      "Field name must be valid"
    );
  }

  /**
   * Set the field owner.
   *
   * @param newOwner The owner
   */

  public void setOwner(
    final CBFieldOwnerType newOwner)
  {
    this.owner = Objects.requireNonNull(newOwner, "newOwner");
  }

  @Override
  public CBFieldOwnerType fieldOwner()
  {
    return this.owner;
  }

  @Override
  public String name()
  {
    return this.name;
  }

  @Override
  public CBTypeExpressionType type()
  {
    return this.type;
  }

  @Override
  public List<String> documentation()
  {
    return this.documentation;
  }
}
