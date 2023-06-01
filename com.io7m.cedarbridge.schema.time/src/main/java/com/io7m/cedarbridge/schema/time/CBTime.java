/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.cedarbridge.schema.time;

import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBPackages;

import java.util.List;

/**
 * The time package.
 */

public final class CBTime
{
  private static final CBPackageType PACKAGE_VALUE;

  static {
    final var externalPackageName =
      "com.io7m.cedarbridge.runtime.time";
    final var packageName =
      "com.io7m.cedarbridge.time";

    final var builder =
      CBPackages.createPackage(packageName);

    {
      final var t =
        builder.createExternalType(
          externalPackageName,
          "CBDuration",
          "Duration"
        );
      t.setDocumentation(List.of("An ISO duration value."));
    }

    {
      final var t =
        builder.createExternalType(
          externalPackageName,
          "CBLocalDate",
          "LocalDate"
        );
      t.setDocumentation(List.of("A local date value."));
    }

    {
      final var t =
        builder.createExternalType(
          externalPackageName,
          "CBLocalDateTime",
          "LocalDateTime"
        );
      t.setDocumentation(List.of("A local date/time value."));
    }

    {
      final var t =
        builder.createExternalType(
          externalPackageName,
          "CBLocalTime",
          "LocalTime"
        );
      t.setDocumentation(List.of("A local time value."));
    }

    {
      final var t =
        builder.createExternalType(
          externalPackageName,
          "CBOffsetDateTime",
          "OffsetDateTime"
        );
      t.setDocumentation(List.of(
        "A date/time value with an explicit timezone offset."));
    }

    {
      final var t =
        builder.createExternalType(
          externalPackageName,
          "CBZoneOffset",
          "ZoneOffset"
        );
      t.setDocumentation(List.of("A timezone offset."));
    }

    PACKAGE_VALUE = builder.build();
  }

  /**
   * @return The time package
   */

  public static CBPackageType get()
  {
    return PACKAGE_VALUE;
  }

  private CBTime()
  {

  }
}
