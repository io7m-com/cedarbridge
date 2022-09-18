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

package com.io7m.cedarbridge.schema.core_types;

import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBPackages;

import java.util.List;

/**
 * The core package.
 */

public final class CBCore
{
  private CBCore()
  {

  }

  private static final CBPackageType PACKAGE_VALUE;

  static {
    final var externalPackageName =
      "com.io7m.cedarbridge.runtime.api";
    final var packageName =
      "com.io7m.cedarbridge";
    final var builder =
      CBPackages.createPackage(packageName);

    for (final var name : List.of(
      "IntegerUnsigned8",
      "IntegerUnsigned16",
      "IntegerUnsigned32",
      "IntegerUnsigned64",
      "IntegerSigned8",
      "IntegerSigned16",
      "IntegerSigned32",
      "IntegerSigned64",
      "Float16",
      "Float32",
      "Float64",
      "String",
      "ByteArray"
    )) {
      final var t =
        builder.createExternalType(
          externalPackageName,
          String.format("CB%s", name),
          name
        );
      t.setDocumentation(
        List.of(
          switch (name) {
            case "IntegerUnsigned8" -> "An 8-bit unsigned integer.";
            case "IntegerUnsigned16" -> "A 16-bit unsigned integer";
            case "IntegerUnsigned32" -> "A 32-bit unsigned integer.";
            case "IntegerUnsigned64" -> "A 64-bit unsigned integer.";
            case "IntegerSigned8" -> "An 8-bit signed integer.";
            case "IntegerSigned16" -> "A 16-bit signed integer";
            case "IntegerSigned32" -> "A 32-bit signed integer.";
            case "IntegerSigned64" -> "A 64-bit signed integer.";
            case "Float16" -> "A 16-bit IEEE binary16 floating-point value.";
            case "Float32" -> "A 32-bit IEEE binary32 floating-point value.";
            case "Float64" -> "A 64-bit IEEE binary64 floating-point value.";
            case "String" -> "A UTF-8 string.";
            case "ByteArray" -> "An array of bytes.";
            default -> throw new IllegalStateException(name);
          }
        )
      );
    }

    {
      final var bool = builder.createVariant("Boolean");
      bool.setExternalName(externalPackageName, "CBBooleanType");
      bool.setDocumentation(List.of("A boolean value."));

      final var falseV = bool.createCase("False");
      falseV.setDocumentation(List.of("A false value."));
      final var trueV = bool.createCase("True");
      trueV.setDocumentation(List.of("A true value."));
    }

    {
      final var option = builder.createVariant("Option");
      option.setExternalName(externalPackageName, "CBOptionType");
      option.addTypeParameter("T", List.of("The type of optional values."));
      option.setDocumentation(List.of("An optional value."));

      final var none = option.createCase("None");
      none.setDocumentation(List.of("No value is present."));

      final var some = option.createCase("Some");
      some.createField("value", option.referenceParameter("T"), List.of("The current value."));
      some.setDocumentation(List.of("A value is present."));
    }

    {
      final var mapEntry =
        builder.createExternalType(
          externalPackageName,
          "CBMapEntry",
          "MapEntry");
      mapEntry.addTypeParameter("K", List.of("The type of keys."));
      mapEntry.addTypeParameter("V", List.of("The type of values."));
      mapEntry.setDocumentation(List.of("An entry within a map."));
    }

    {
      final var list =
        builder.createExternalType(externalPackageName, "CBList", "List");
      list.addTypeParameter("A", List.of("The type of values within the list."));
      list.setDocumentation(List.of("A sequence of values."));
    }

    {
      final var map =
        builder.createExternalType(externalPackageName, "CBMap", "Map");
      map.addTypeParameter("K", List.of());
      map.addTypeParameter("V", List.of());
      map.setDocumentation(List.of("A key/value map data structure."));
    }

    PACKAGE_VALUE = builder.build();
  }

  /**
   * @return The core package.
   */

  public static CBPackageType get()
  {
    return PACKAGE_VALUE;
  }
}
