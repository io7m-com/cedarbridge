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

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;

/**
 * The type of package builders.
 */

public interface CBPackageBuilderType
{
  /**
   * Create a new external type declaration.
   *
   * @param externalPackageName The external package name
   * @param externalName        The external type name
   * @param name                The name within this package
   *
   * @return A type declaration builder
   */

  CBTypeDeclarationBuilderType createExternalType(
    String externalPackageName,
    String externalName,
    String name);

  /**
   * Create a new record.
   *
   * @param name The name within this package
   *
   * @return A record builder
   */

  CBRecordBuilderType createRecord(
    String name);

  /**
   * Find an existing record.
   *
   * @param name The name within this package
   *
   * @return A record builder
   */

  CBRecordBuilderType findRecord(
    String name);

  /**
   * Create a new variant.
   *
   * @param name The name within this package
   *
   * @return A variant builder
   */

  CBVariantBuilderType createVariant(
    String name);

  /**
   * Find an existing variant.
   *
   * @param name The name within this package
   *
   * @return A record builder
   */

  CBVariantBuilderType findVariant(
    String name);

  /**
   * Find an existing type.
   *
   * @param name The name within this package
   *
   * @return A type builder
   */

  CBTypeDeclarationBuilderType findType(
    String name);

  /**
   * Reference an existing type.
   *
   * @param name The name within this package
   *
   * @return A named type expression
   */

  CBTypeExprNamedType referenceType(
    String name);

  /**
   * Reference an external type.
   *
   * @param typeDeclaration The type declaration
   *
   * @return A named type expression
   */

  CBTypeExprNamedType referenceExternalType(
    CBTypeDeclarationType typeDeclaration);

  /**
   * Build a package based on all of the given values so far.
   *
   * @return A package
   */

  CBPackageType build();

  /**
   * Import a package.
   *
   * @param imported The package
   *
   * @return this
   */

  CBPackageBuilderType addImport(
    CBPackageType imported);

  /**
   * Create a new protocol.
   *
   * @param name The name within this package
   *
   * @return A protocol builder
   */

  CBProtocolBuilderType createProtocol(
    String name);
}
