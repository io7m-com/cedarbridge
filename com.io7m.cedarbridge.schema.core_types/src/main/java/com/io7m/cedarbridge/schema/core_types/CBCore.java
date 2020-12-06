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

package com.io7m.cedarbridge.schema.core_types;

import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBPackages;

import java.util.List;

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
      builder.createExternalType(
        externalPackageName,
        String.format("CB%s", name),
        name
      );
    }

    {
      final var option = builder.createVariant("Option");
      option.addTypeParameter("T");
      option.createCase("None");
      final var some = option.createCase("Some");
      some.createField("value", option.referenceParameter("T"));
    }

    {
      final var mapEntry =
        builder.createExternalType(
          externalPackageName,
          "CBMapEntry",
          "MapEntry");
      mapEntry.addTypeParameter("K");
      mapEntry.addTypeParameter("V");
    }

    {
      final var list =
        builder.createExternalType(externalPackageName, "CBList", "List");
      list.addTypeParameter("A");
    }

    {
      final var map =
        builder.createExternalType(externalPackageName, "CBMap", "Map");
      map.addTypeParameter("K");
      map.addTypeParameter("V");
    }

    PACKAGE_VALUE = builder.build();
  }

  public static CBPackageType get()
  {
    return PACKAGE_VALUE;
  }
}
