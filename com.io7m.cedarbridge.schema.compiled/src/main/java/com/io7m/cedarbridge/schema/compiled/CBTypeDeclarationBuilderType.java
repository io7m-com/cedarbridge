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

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprParameterType;

/**
 * The base type of type declaration builders.
 */

public interface CBTypeDeclarationBuilderType
{
  /**
   * @return The package that owns the type
   */

  CBPackageBuilderType ownerPackage();

  /**
   * @return A reference to this type
   */

  CBTypeExprNamedType reference();

  /**
   * Find an existing parameter with the given name.
   *
   * @param name The parameter name
   *
   * @return A parameter
   */

  CBTypeParameterType findParameter(String name);

  /**
   * Reference an existing parameter with the given name.
   *
   * @param name The parameter name
   *
   * @return A parameter expression
   */

  CBTypeExprParameterType referenceParameter(String name);

  /**
   * Add a type parameter.
   *
   * @param name The parameter name
   *
   * @return A type parameter
   */

  CBTypeParameterType addTypeParameter(
    String name);

  /**
   * Set the external type of this name
   *
   * @param externalPackageName The package name
   * @param name                The type name
   */

  void setExternalName(
    String externalPackageName,
    String name);
}
