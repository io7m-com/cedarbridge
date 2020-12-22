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

package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.schema.compiled.CBExternalName;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeParameterType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CBFakeRecord implements CBRecordType
{
  private final String name;
  private final int arity;
  private CBPackageType owner;

  public CBFakeRecord(
    final String inName,
    final int arity)
  {
    this.name = Objects.requireNonNull(inName, "name");
    this.arity = arity;
  }

  public void setOwner(
    final CBPackageType inOwner)
  {
    this.owner = Objects.requireNonNull(inOwner, "owner");
  }

  @Override
  public CBPackageType owner()
  {
    return this.owner;
  }

  @Override
  public String name()
  {
    return this.name;
  }

  @Override
  public int arity()
  {
    return this.arity;
  }

  @Override
  public List<CBTypeParameterType> parameters()
  {
    return List.of();
  }

  @Override
  public Optional<CBExternalName> external()
  {
    return Optional.empty();
  }

  @Override
  public List<CBFieldType> fields()
  {
    return List.of();
  }
}
