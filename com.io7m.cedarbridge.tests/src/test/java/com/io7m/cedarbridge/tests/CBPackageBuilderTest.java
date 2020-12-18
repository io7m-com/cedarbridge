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

import com.io7m.cedarbridge.schema.compiled.CBPackages;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBPackageBuilderTest
{
  @Test
  public void testClosed()
  {
    final var packageBuilder = CBPackages.createPackage("x.y");
    packageBuilder.build();

    assertThrows(IllegalStateException.class, () -> {
      packageBuilder.createVariant("A");
    });
  }

  @Test
  public void testInvalidPackageName()
  {
    assertThrows(IllegalArgumentException.class, () -> {
      CBPackages.createPackage("23");
    });
  }

  @Test
  public void testInvalidTypeName0()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createRecord("23");
    });
  }

  @Test
  public void testDuplicateTypeName0()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    packageBuilder.createRecord("A");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createRecord("A");
    });
  }

  @Test
  public void testInvalidTypeName1()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createVariant("23");
    });
  }

  @Test
  public void testDuplicateTypeName1()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    packageBuilder.createVariant("A");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createVariant("A");
    });
  }

  @Test
  public void testInvalidTypeName2()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createProtocol("23");
    });
  }

  @Test
  public void testDuplicateTypeName2()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    packageBuilder.createProtocol("A");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createProtocol("A");
    });
  }

  @Test
  public void testInvalidTypeName3()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createExternalType("x", "23", "x");
    });
  }

  @Test
  public void testDuplicateTypeName3()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    packageBuilder.createExternalType("x.y", "A", "A");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createExternalType("x.y", "A", "A");
    });
  }

  @Test
  public void testInvalidProtocolName3()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createProtocol("23");
    });
  }

  @Test
  public void testDuplicateProtocolName3()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    packageBuilder.createProtocol("A");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.createProtocol("A");
    });
  }

  @Test
  public void testNotVariant()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    packageBuilder.createRecord("A");
    assertThrows(IllegalStateException.class, () -> {
      packageBuilder.findVariant("A");
    });
  }

  @Test
  public void testNotRecord()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    packageBuilder.createVariant("A");
    assertThrows(IllegalStateException.class, () -> {
      packageBuilder.findRecord("A");
    });
  }

  @Test
  public void testNotFoundType0()
  {
    final var packageBuilder = CBPackages.createPackage("x.y.z");
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.findType("A");
    });
    assertThrows(IllegalStateException.class, () -> {
      packageBuilder.findVariant("A");
    });
    assertThrows(IllegalStateException.class, () -> {
      packageBuilder.findRecord("A");
    });
    assertThrows(IllegalArgumentException.class, () -> {
      packageBuilder.referenceType("A");
    });
  }

  @Test
  public void testRecordFieldUsed()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var recordBuilder =
      packageBuilder.createRecord("A");

    recordBuilder.createField("x", recordBuilder.reference());
    assertThrows(IllegalArgumentException.class, () -> {
      recordBuilder.createField("x", recordBuilder.reference());
    });
  }

  @Test
  public void testRecordParameterUsed()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var recordBuilder =
      packageBuilder.createRecord("A");

    recordBuilder.addTypeParameter("A");
    assertThrows(IllegalArgumentException.class, () -> {
      recordBuilder.addTypeParameter("A");
    });
  }

  @Test
  public void testRecordFieldBadParameter()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var recordBuilder =
      packageBuilder.createRecord("A");

    assertThrows(IllegalArgumentException.class, () -> {
      recordBuilder.createField("x", recordBuilder.referenceParameter("Z"));
    });
  }

  @Test
  public void testVariantCaseUsed()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var variantBuilder =
      packageBuilder.createVariant("A");

    variantBuilder.createCase("C");
    assertThrows(IllegalArgumentException.class, () -> {
      variantBuilder.createCase("C");
    });
  }

  @Test
  public void testVariantCaseBadParameter()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var variantBuilder =
      packageBuilder.createVariant("A");

    final var caseBuilder = variantBuilder.createCase("C");
    assertThrows(IllegalArgumentException.class, () -> {
      caseBuilder.createField("x", variantBuilder.referenceParameter("Z"));
    });
  }

  @Test
  public void testVariantCaseFieldUsed()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var variantBuilder =
      packageBuilder.createVariant("A");

    final var caseBuilder = variantBuilder.createCase("C");
    caseBuilder.createField("x", variantBuilder.reference());

    assertThrows(IllegalArgumentException.class, () -> {
      caseBuilder.createField("x", variantBuilder.reference());
    });
  }

  @Test
  public void testVariantParameterUsed()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var recordBuilder =
      packageBuilder.createVariant("A");

    recordBuilder.addTypeParameter("A");
    assertThrows(IllegalArgumentException.class, () -> {
      recordBuilder.addTypeParameter("A");
    });
  }

  @Test
  public void testExternalMissingParameter0()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var externalBuilder =
      packageBuilder.createExternalType("x.y", "A", "T");

    assertThrows(IllegalStateException.class, () -> {
      externalBuilder.referenceParameter("Z");
    });
  }

  @Test
  public void testExternalMissingParameter1()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var externalBuilder =
      packageBuilder.createExternalType("x.y", "A", "T");

    assertThrows(IllegalStateException.class, () -> {
      externalBuilder.findParameter("Z");
    });
  }

  @Test
  public void testExternalDuplicateParameter()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var externalBuilder =
      packageBuilder.createExternalType("x.y", "A", "T");

    externalBuilder.addTypeParameter("A");
    assertThrows(IllegalArgumentException.class, () -> {
      externalBuilder.addTypeParameter("A");
    });
  }

  @Test
  public void testProtocolVersionUsed()
  {
    final var packageBuilder =
      CBPackages.createPackage("x.y.z");
    final var protoBuilder =
      packageBuilder.createProtocol("A");

    protoBuilder.createVersion(BigInteger.ONE);
    assertThrows(IllegalArgumentException.class, () -> {
      protoBuilder.createVersion(BigInteger.ONE);
    });
  }
}
