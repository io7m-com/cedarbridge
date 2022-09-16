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

package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.codegen.api.CBCodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.api.CBCodeGeneratorDescription;
import com.io7m.cedarbridge.codegen.api.CBCodeGeneratorResult;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorDescription;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorResult;
import com.io7m.cedarbridge.runtime.api.CBQualifiedTypeName;
import com.io7m.cedarbridge.runtime.api.CBTypeArgument;
import com.io7m.cedarbridge.schema.ast.CBASTFieldName;
import com.io7m.cedarbridge.schema.ast.CBASTImport;
import com.io7m.cedarbridge.schema.ast.CBASTPackageDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTPackageName;
import com.io7m.cedarbridge.schema.ast.CBASTPackageShortName;
import com.io7m.cedarbridge.schema.binder.api.CBBindingExternal;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalFieldName;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalProtocolVersionDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeParameter;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalVariantCase;
import com.io7m.cedarbridge.schema.typer.api.CBTypeAssignment;
import com.io7m.cedarbridge.version.CBVersion;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

public final class CBEqualsTest
{
  private static final CBTypeArgument TYPE_ARGUMENT_0 =
    new CBTypeArgument(
      new CBQualifiedTypeName("x.y", "T"),
      List.of()
    );

  private static final CBTypeArgument TYPE_ARGUMENT_1 =
    new CBTypeArgument(
      new CBQualifiedTypeName("x.y", "U"),
      List.of()
    );

  private static DynamicTest toTest(
    final Class<?> clazz)
  {
    return DynamicTest.dynamicTest(
      String.format("testEquals_%s", clazz.getCanonicalName()),
      () -> {
        EqualsVerifier.forClass(clazz)
          .suppress(Warning.NULL_FIELDS)
          .withPrefabValues(
            CBTypeArgument.class,
            TYPE_ARGUMENT_0,
            TYPE_ARGUMENT_1)
          .verify();
      }
    );
  }

  @TestFactory
  public Stream<DynamicTest> testEquals()
  {
    return Stream.of(
      CBASTFieldName.class,
      CBASTImport.class,
      CBASTPackageDeclaration.class,
      CBASTPackageName.class,
      CBASTPackageShortName.class,
      CBBindingExternal.class,
      CBBindingLocalFieldName.class,
      CBBindingLocalProtocolVersionDeclaration.class,
      CBBindingLocalTypeParameter.class,
      CBBindingLocalVariantCase.class,
      CBCodeGeneratorConfiguration.class,
      CBCodeGeneratorDescription.class,
      CBCodeGeneratorResult.class,
      CBSPICodeGeneratorConfiguration.class,
      CBSPICodeGeneratorDescription.class,
      CBSPICodeGeneratorResult.class,
      CBTypeAssignment.class,
      CBVersion.class
    ).map(CBEqualsTest::toTest);
  }
}
