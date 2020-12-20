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

package com.io7m.cedarbridge.schema.compiled.internal;

import com.io7m.cedarbridge.schema.compiled.CBExternalType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBTypeParameterType;
import com.io7m.cedarbridge.schema.names.CBPackageNames;
import com.io7m.cedarbridge.schema.names.CBTypeNames;
import com.io7m.jaffirm.core.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CBTypeDeclarationExternal implements CBExternalType
{
  private final String name;
  private final ArrayList<CBTypeParameter> parameters;
  private final List<CBTypeParameterType> parametersRead;
  private final String externalPackage;
  private final String externalType;
  private CBPackageType owner;

  public CBTypeDeclarationExternal(
    final String inName,
    final String inExternalPackage,
    final String inExternalType)
  {
    this.name =
      Objects.requireNonNull(inName, "name");
    this.externalPackage =
      Objects.requireNonNull(inExternalPackage, "externalPackage");
    this.externalType =
      Objects.requireNonNull(inExternalType, "externalType");
    this.parameters =
      new ArrayList<>();

    Preconditions.checkPreconditionV(
      inName,
      CBPackageNames.INSTANCE.isValid(inExternalPackage),
      "Package name must be valid"
    );

    Preconditions.checkPreconditionV(
      inName,
      CBTypeNames.INSTANCE.isValid(inExternalType),
      "External type name must be valid"
    );

    Preconditions.checkPreconditionV(
      inName,
      CBTypeNames.INSTANCE.isValid(inName),
      "Type name must be valid"
    );

    this.parametersRead =
      Collections.unmodifiableList(this.parameters);
  }

  public void addTypeParameter(
    final CBTypeParameter parameter)
  {
    Objects.requireNonNull(parameter, "parameter");

    final var pName = parameter.name();
    Preconditions.checkPreconditionV(
      this.parameters.stream()
        .filter(f -> Objects.equals(f.name(), pName))
        .findAny()
        .isEmpty(),
      "A parameter named %s cannot already exist!",
      pName
    );

    final int pIndex = parameter.index();
    Preconditions.checkPreconditionV(
      this.parameters.stream()
        .filter(f -> f.index() == pIndex)
        .findAny()
        .isEmpty(),
      "A parameter with index %s cannot already exist!",
      Integer.valueOf(pIndex)
    );

    this.parameters.add(parameter);
    parameter.setOwner(this);
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
  public List<CBTypeParameterType> parameters()
  {
    return this.parametersRead;
  }

  public void setOwner(
    final CBPackage newOwner)
  {
    this.owner = Objects.requireNonNull(newOwner, "cbPackage");
  }

  @Override
  public String externalPackage()
  {
    return this.externalPackage;
  }

  @Override
  public String externalType()
  {
    return this.externalType;
  }
}