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

import com.io7m.cedarbridge.schema.ast.CBASTField;
import com.io7m.cedarbridge.schema.ast.CBASTFieldName;
import com.io7m.cedarbridge.schema.ast.CBASTImport;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.ast.CBASTPackageDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTPackageName;
import com.io7m.cedarbridge.schema.ast.CBASTPackageShortName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeApplication;
import com.io7m.cedarbridge.schema.ast.CBASTTypeName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeNamed;
import com.io7m.cedarbridge.schema.ast.CBASTTypeParameterName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.binder.api.CBBindingExternal;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalFieldName;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeParameter;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalVariantCase;
import com.io7m.cedarbridge.schema.typer.api.CBTypeAssignment;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

public final class CBEqualsTest
{
  private static DynamicTest toTest(
    final Class<?> clazz)
  {
    return DynamicTest.dynamicTest(
      String.format("testEquals_%s", clazz.getCanonicalName()),
      () -> {
        EqualsVerifier.forClass(clazz)
          .suppress(Warning.NULL_FIELDS)
          .verify();
      }
    );
  }

  @TestFactory
  public Stream<DynamicTest> testEquals()
  {
    return Stream.of(
      CBASTField.class,
      CBASTFieldName.class,
      CBASTImport.class,
      CBASTPackage.class,
      CBASTPackageDeclaration.class,
      CBASTPackageName.class,
      CBASTPackageShortName.class,
      CBASTTypeApplication.class,
      CBASTTypeName.class,
      CBASTTypeNamed.class,
      CBASTTypeParameterName.class,
      CBASTTypeRecord.class,
      CBASTTypeVariant.class,
      CBBindingExternal.class,
      CBBindingLocalFieldName.class,
      CBBindingLocalTypeDeclaration.class,
      CBBindingLocalTypeParameter.class,
      CBBindingLocalVariantCase.class,
      CBTypeAssignment.class
    ).map(CBEqualsTest::toTest);
  }
}
