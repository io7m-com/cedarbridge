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

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.CBExpressionSources;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceType;
import com.io7m.cedarbridge.schema.ast.CBASTField;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.ast.CBASTTypeApplication;
import com.io7m.cedarbridge.schema.ast.CBASTTypeExpressionType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeNamed;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.binder.CBBinderFactory;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeParameter;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.cedarbridge.schema.parser.CBParserFactory;
import com.io7m.cedarbridge.schema.typer.CBTypeCheckerFactory;
import com.io7m.cedarbridge.schema.typer.api.CBTypeAssignment;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckFailedException;
import com.io7m.junreachable.UnreachableCodeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType.CBASTTypeRecordType;
import static com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType.CBASTTypeVariantType;
import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingExternalType;
import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprApplicationType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprParameterType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBTypeCheckerTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBTypeCheckerTest.class);

  private CBExpressionSourceType source;
  private CBBinderFactory binders;
  private CBParserFactory parsers;
  private CBTypeCheckerFactory typeCheckers;
  private CBExpressionSources sources;
  private ArrayList<CBError> errors;
  private Path directory;
  private CBFakeLoader loader;
  private HashMap<BigInteger, CBBindingLocalType> bindings;

  private static void checkPackagesMatch(
    final CBASTPackage srcPack,
    final CBPackageType tarPack)
  {
    assertEquals(srcPack.name().text(), tarPack.name());
    final var srcImports = srcPack.imports();
    final var tarImports = tarPack.imports();
    assertEquals(srcImports.size(), tarImports.size());

    for (var index = 0; index < srcImports.size(); ++index) {
      final var srcI = srcImports.get(index);
      final var tarI = tarImports.get(index);
      assertEquals(srcI.target().text(), tarI.name());
    }

    final var srcTypes = srcPack.types();
    final var tarTypes = tarPack.types();
    assertEquals(srcTypes.size(), tarTypes.size());

    for (var index = 0; index < srcTypes.size(); ++index) {
      final var srcType = srcTypes.get(index);
      final var tarType = tarTypes.get(srcType.name().text());

      final var srcParams = srcType.parameters();
      final var tarParams = tarType.parameters();
      assertEquals(srcParams.size(), tarParams.size());
      assertEquals(tarType.owner(), tarPack);

      for (var pIndex = 0; pIndex < srcParams.size(); ++pIndex) {
        final var srcParam = srcParams.get(pIndex);
        final var tarParam = tarParams.get(pIndex);
        assertEquals(srcParam.text(), tarParam.name());
        assertEquals(tarType, tarParam.owner());
      }

      if (srcType instanceof CBASTTypeVariantType) {
        final var srcVar = (CBASTTypeVariantType) srcType;
        final var tarVar = (CBVariantType) tarType;
        final var srcCases = srcVar.cases();
        final var tarCases = tarVar.cases();
        assertEquals(srcCases.size(), tarCases.size());

        for (var cIndex = 0; cIndex < srcCases.size(); ++cIndex) {
          final var srcCase = srcCases.get(cIndex);
          final var tarCase = tarCases.get(cIndex);
          assertEquals(srcCase.name().text(), tarCase.name());
          checkFieldsMatch(srcCase.fields(), tarCase.fields());
        }
      } else if (srcType instanceof CBASTTypeRecordType) {
        final var srcRec = (CBASTTypeRecordType) srcType;
        final var tarRec = (CBRecordType) tarType;
        final var srcFields = srcRec.fields();
        final var tarFields = tarRec.fields();
        checkFieldsMatch(srcFields, tarFields);
      } else {
        throw new UnreachableCodeException();
      }
    }
  }

  private static void checkFieldsMatch(
    final List<CBASTField> srcFields,
    final List<CBFieldType> tarFields)
  {
    assertEquals(srcFields.size(), tarFields.size());

    for (var index = 0; index < srcFields.size(); ++index) {
      final var srcField = srcFields.get(index);
      final var tarField = tarFields.get(index);
      assertEquals(srcField.name().text(), tarField.name());
      checkTypeExpressionsMatch(srcField.type(), tarField.type());
    }
  }

  private static void checkTypeExpressionsMatch(
    final CBASTTypeExpressionType srcType,
    final CBTypeExpressionType tarType)
  {
    if (srcType instanceof CBASTTypeApplication) {
      final var tarApp = (CBTypeExprApplicationType) tarType;
      final var srcApp = (CBASTTypeApplication) srcType;
      checkTypeExpressionsMatch(srcApp.target(), tarApp.target());
      for (var index = 0; index < srcApp.arguments().size(); ++index) {
        checkTypeExpressionsMatch(
          srcApp.arguments().get(index),
          tarApp.arguments().get(index)
        );
      }
    } else if (srcType instanceof CBASTTypeNamed) {
      final var binding = srcType.userData().get(CBBindingType.class);
      if (binding instanceof CBBindingLocalType) {
        if (binding instanceof CBBindingLocalTypeDeclaration) {
          final var tarNam = (CBTypeExprNamedType) tarType;
          final var bindNam = (CBBindingLocalTypeDeclaration) binding;
          assertEquals(bindNam.name(), tarNam.declaration().name());
        } else if (binding instanceof CBBindingLocalTypeParameter) {
          final var tarPar = (CBTypeExprParameterType) tarType;
          final var bindNam = (CBBindingLocalTypeParameter) binding;
          assertEquals(bindNam.name(), tarPar.parameter().name());
        } else {
          throw new UnreachableCodeException();
        }
      } else {
        final var tarNam = (CBTypeExprNamedType) tarType;
        final var bindExt = (CBBindingExternalType) binding;
        assertEquals(bindExt.type(), tarNam.declaration());
      }
    } else {
      throw new UnreachableCodeException();
    }
  }

  private CBASTPackage parse(
    final String name)
    throws Exception
  {
    final var path =
      CBTestDirectories.resourceOf(
        CBTypeCheckerTest.class,
        this.directory,
        name);
    this.source =
      this.sources.create(path.toUri(), Files.newInputStream(path));
    try (var parser =
           this.parsers.createParser(this::addError, this.source)) {
      return parser.execute();
    }
  }

  private CBASTPackage check(
    final String name)
    throws Exception
  {
    final var parsedPackage = this.parse(name);
    try (var binder =
           this.binders.createBinder(
             this.loader, this::addError, this.source, parsedPackage)) {
      binder.execute();
      try (var checker =
             this.typeCheckers.createTypeChecker(
               this::addError, this.source, parsedPackage)) {
        checker.execute();
        return parsedPackage;
      }
    }
  }

  private CBError takeError()
  {
    return this.errors.remove(0);
  }

  private void addError(
    final CBError error)
  {
    LOG.error("{}", error.message());
    this.errors.add(error);
  }

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.errors = new ArrayList<>();
    this.bindings = new HashMap<>();
    this.directory = CBTestDirectories.createTempDirectory();
    this.sources = new CBExpressionSources();
    this.parsers = new CBParserFactory();
    this.binders = new CBBinderFactory();
    this.loader = new CBFakeLoader();
    this.typeCheckers = new CBTypeCheckerFactory();
  }

  @Test
  public void testRecordOk0()
    throws Exception
  {
    final var pack = this.check("typeRecordOk0.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(1, types.size());

    final var t0 = types.get(0);
    final var t0a = t0.userData().get(CBTypeAssignment.class);
    assertEquals(0, t0a.arity());

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testRecordOk1()
    throws Exception
  {
    final var pack = this.check("typeRecordOk1.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(2, types.size());

    final var t0 = types.get(0);
    final var t0a = t0.userData().get(CBTypeAssignment.class);
    assertEquals(0, t0a.arity());

    final var t1 = types.get(0);
    final var t1a = t1.userData().get(CBTypeAssignment.class);
    assertEquals(0, t1a.arity());

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
    {
      final var vpack = pack.userData().get(CBPackageType.class);
      assertEquals("x.y.z", vpack.name());
      assertEquals(2, vpack.types().size());

      final var vt0 = (CBRecordType) vpack.types().get("T");
      assertEquals("T", vt0.name());
      assertEquals(0, vt0.fields().size());
      assertEquals(0, vt0.parameters().size());

      final var vt1 = (CBRecordType) vpack.types().get("U");
      assertEquals("U", vt1.name());
      assertEquals(1, vt1.fields().size());
      final var vt1f0 = vt1.fields().get(0);
      assertEquals("x", vt1f0.name());
      assertSame(vt1, vt1f0.fieldOwner());
      assertSame(vt0, ((CBTypeExprNamedType) vt1f0.type()).declaration());
      assertEquals(0, vt1.parameters().size());
    }
  }

  @Test
  public void testRecordOk2()
    throws Exception
  {
    final var pack = this.check("typeRecordOk2.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(1, types.size());

    final var t0 = types.get(0);
    final var t0a = t0.userData().get(CBTypeAssignment.class);
    assertEquals(1, t0a.arity());

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
    {
      final var vpack = pack.userData().get(CBPackageType.class);
      assertEquals("x.y.z", vpack.name());
      assertEquals(1, vpack.types().size());

      final var vt0 = (CBRecordType) vpack.types().get("U");
      assertEquals("U", vt0.name());
      assertEquals(1, vt0.fields().size());
      final var vt1f0 = vt0.fields().get(0);
      assertEquals("x", vt1f0.name());
      assertSame(vt0, vt1f0.fieldOwner());
      assertSame(
        vt0.parameters().get(0),
        ((CBTypeExprParameterType) vt1f0.type()).parameter());
      assertEquals(1, vt0.parameters().size());
    }
  }

  @Test
  public void testRecordOk3()
    throws Exception
  {
    final var pack = this.check("typeRecordOk3.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(3, types.size());

    {
      final var t = types.get(0);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    {
      final var t = types.get(1);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(1, ta.arity());
    }

    {
      final var t = types.get(2);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testRecordOk4()
    throws Exception
  {
    final var pack = this.check("typeRecordOk4.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(3, types.size());

    {
      final var t = types.get(0);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    {
      final var t = types.get(1);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(2, ta.arity());
    }

    {
      final var t = (CBASTTypeRecord) types.get(2);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());

      final var f0 = t.fields().get(0);
      assertEquals("r", f0.name().text());
    }

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testRecordOk5()
    throws Exception
  {
    final var trec = new CBFakeRecord("T", 0);
    final var tpack = new CBFakePackage("a.b.c");
    tpack.addType(trec);
    this.loader.register(tpack);

    final var pack = this.check("typeRecordOk5.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(1, types.size());

    {
      final var t = types.get(0);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testRecordOk6()
    throws Exception
  {
    final var trec = new CBFakeRecord("T", 1);
    final var tpack = new CBFakePackage("a.b.c");
    tpack.addType(trec);
    this.loader.register(tpack);

    final var pack = this.check("typeRecordOk6.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(2, types.size());

    {
      final var t = types.get(0);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    {
      final var t = types.get(1);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testError0()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorType0.cbs");
    });

    assertEquals("errorTypeArgumentsIncorrect", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testError1()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorType1.cbs");
    });

    assertEquals("errorTypeArgumentsIncorrect", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testError2()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorType2.cbs");
    });

    assertEquals("errorTypeArgumentsIncorrect", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testError3()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorType3.cbs");
    });

    assertEquals("errorTypeArgumentsIncorrect", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testError4()
    throws Exception
  {
    final var trec = new CBFakeRecord("T", 0);
    final var tpack = new CBFakePackage("a.b.c");
    tpack.addType(trec);
    this.loader.register(tpack);

    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorType4.cbs");
    });

    assertEquals("errorTypeArgumentsIncorrect", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testVariantOk0()
    throws Exception
  {
    final var pack = this.check("typeVariantOk0.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(1, types.size());

    final var t0 = types.get(0);
    final var t0a = t0.userData().get(CBTypeAssignment.class);
    assertEquals(0, t0a.arity());

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testVariantOk1()
    throws Exception
  {
    final var pack = this.check("typeVariantOk1.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(2, types.size());

    final var t0 = types.get(0);
    final var t0a = t0.userData().get(CBTypeAssignment.class);
    assertEquals(0, t0a.arity());

    final var t1 = types.get(0);
    final var t1a = t1.userData().get(CBTypeAssignment.class);
    assertEquals(0, t1a.arity());

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testVariantOk2()
    throws Exception
  {
    final var pack = this.check("typeVariantOk2.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(1, types.size());

    final var t0 = types.get(0);
    final var t0a = t0.userData().get(CBTypeAssignment.class);
    assertEquals(1, t0a.arity());

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testVariantOk3()
    throws Exception
  {
    final var pack = this.check("typeVariantOk3.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(3, types.size());

    {
      final var t = types.get(0);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    {
      final var t = types.get(1);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(1, ta.arity());
    }

    {
      final var t = types.get(2);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testVariantOk4()
    throws Exception
  {
    final var pack = this.check("typeVariantOk4.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(3, types.size());

    {
      final var t = types.get(0);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    {
      final var t = types.get(1);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(2, ta.arity());
    }

    {
      final var t = (CBASTTypeVariant) types.get(2);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testVariantOk5()
    throws Exception
  {
    final var trec = new CBFakeRecord("T", 0);
    final var tpack = new CBFakePackage("a.b.c");
    tpack.addType(trec);
    this.loader.register(tpack);

    final var pack = this.check("typeVariantOk5.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(1, types.size());

    {
      final var t = types.get(0);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }

  @Test
  public void testVariantOk6()
    throws Exception
  {
    final var trec = new CBFakeRecord("T", 1);
    final var tpack = new CBFakePackage("a.b.c");
    tpack.addType(trec);
    this.loader.register(tpack);

    final var pack = this.check("typeVariantOk6.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(2, types.size());

    {
      final var t = types.get(0);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    {
      final var t = types.get(1);
      final var ta = t.userData().get(CBTypeAssignment.class);
      assertEquals(0, ta.arity());
    }

    checkPackagesMatch(pack, pack.userData().get(CBPackageType.class));
  }
}
