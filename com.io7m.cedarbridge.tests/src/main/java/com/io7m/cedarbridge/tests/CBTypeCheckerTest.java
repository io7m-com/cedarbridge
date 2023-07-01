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
import com.io7m.cedarbridge.schema.binder.api.CBBindingExternal;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalType;
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
import com.io7m.cedarbridge.schema.typer.api.CBTypesForProtocolVersion;
import com.io7m.junreachable.UnreachableCodeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import java.util.Objects;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprApplicationType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprParameterType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

      if (srcType instanceof CBASTTypeVariant srcVar) {
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
      } else if (srcType instanceof CBASTTypeRecord srcRec) {
        final var tarRec = (CBRecordType) tarType;
        final var srcFields = srcRec.fields();
        final var tarFields = tarRec.fields();
        checkFieldsMatch(srcFields, tarFields);
      } else {
        throw new UnreachableCodeException();
      }
    }

    final var srcProtos = srcPack.protocols();
    final var tarProtos = tarPack.protocols();
    for (var index = 0; index < srcProtos.size(); ++index) {
      final var srcProto = srcProtos.get(index);
      final var tarProto = tarProtos.get(srcProto.name().text());
      assertEquals(srcProto.name().text(), tarProto.name());

      for (var vIndex = 0; vIndex < srcProto.versions().size(); ++vIndex) {
        final var srcV =
          srcProto.versions().get(vIndex);
        final var tarV =
          tarProto.versions().get(srcV.version());
        assertEquals(srcV.version(), tarV.version());

        final var evalT =
          srcV.userData()
            .get(CBTypesForProtocolVersion.class);

        final var srcVT = evalT.types();
        assertEquals(srcVT.size(), tarV.typesInOrder().size());

        final var tarVT = tarV.typesInOrder();
        for (final var srcT : srcVT) {
          assertTrue(
            tarVT.stream()
              .anyMatch(t -> Objects.equals(t.declaration().name(), srcT.text()))
          );
        }
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
        final var bindExt = (CBBindingExternal) binding;
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

  @AfterEach
  public void tearDown()
    throws IOException
  {
    CBTestDirectories.deleteDirectory(this.directory);
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

    final var cpack = pack.userData().get(CBPackageType.class);
    checkPackagesMatch(pack, cpack);

    assertEquals(
      "8cb88865-c48b-3e51-8b32-1614308ce64d",
      cpack.types().get("V").id().toString()
    );
  }

  @Test
  public void testProtoOk0()
    throws Exception
  {
    final var trec = new CBFakeRecord("T", 1);

    final var pack = this.check("typeProtoOK0.cbs");
    assertEquals(0, this.errors.size());

    final var p = pack.protocols().get(0);
    final var pv = p.versions();

    {
      final var v = pv.get(0);
      final var ts = v.userData().get(CBTypesForProtocolVersion.class);
      final var tt = ts.types();
      assertEquals(1, tt.size());
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "A")));
    }

    {
      final var v = pv.get(1);
      final var ts = v.userData().get(CBTypesForProtocolVersion.class);
      final var tt = ts.types();
      assertEquals(2, tt.size());
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "A")));
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "B")));
    }

    {
      final var v = pv.get(2);
      final var ts = v.userData().get(CBTypesForProtocolVersion.class);
      final var tt = ts.types();
      assertEquals(2, tt.size());
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "A")));
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "C")));
    }

    {
      final var v = pv.get(3);
      final var ts = v.userData().get(CBTypesForProtocolVersion.class);
      final var tt = ts.types();
      assertEquals(1, tt.size());
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "C")));
    }
  }

  @Test
  public void testProtoOk1()
    throws Exception
  {
    final var trec = new CBFakeRecord("T", 1);

    final var pack = this.check("typeProtoOK1.cbs");
    assertEquals(0, this.errors.size());

    final var p = pack.protocols().get(0);
    final var pv = p.versions();

    {
      final var v = pv.get(0);
      final var ts = v.userData().get(CBTypesForProtocolVersion.class);
      final var tt = ts.types();
      assertEquals(1, tt.size());
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "A")));
    }

    {
      final var v = pv.get(1);
      final var ts = v.userData().get(CBTypesForProtocolVersion.class);
      final var tt = ts.types();
      assertEquals(1, tt.size());
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "B")));
    }

    {
      final var v = pv.get(2);
      final var ts = v.userData().get(CBTypesForProtocolVersion.class);
      final var tt = ts.types();
      assertEquals(1, tt.size());
      assertTrue(tt.stream().anyMatch(t -> Objects.equals(t.text(), "C")));
    }
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
  public void testError5()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorType5.cbs");
    });

    assertEquals("errorTypeApplicationEmpty", this.takeError().errorCode());
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

    final var cpack = pack.userData().get(CBPackageType.class);
    checkPackagesMatch(pack, cpack);

    assertEquals(
      "55c7e9c2-a861-3812-a89f-3ca50fad4247",
      cpack.types().get("V").id().toString()
    );
  }

  @Test
  public void testOk0()
    throws Exception
  {
    this.loader.register(new CBFakePackage("x.y.z"));

    final var pack = this.check("basic.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(5, types.size());
    final var protos = pack.protocols();
    assertEquals(1, protos.size());

    final var compiled = pack.userData().get(CBPackageType.class);
    checkPackagesMatch(pack, compiled);

    final var protoBelongings =
      compiled.protocolVersionsForType(compiled.types().get("UnitType"));
    assertEquals(1, protoBelongings.size());
    assertEquals("Z", protoBelongings.get(0).owner().name());
  }

  @Test
  public void testErrorProto0()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorProtoType0.cbs");
    });

    assertEquals("errorTypeProtocolKind0", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testErrorProtoBecomesEmpty()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorProtoBecomesEmpty.cbs");
    });

    assertEquals("errorTypeProtocolBecameEmpty", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testErrorProtoFirstRemoval()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorProtoFirstRemoval.cbs");
    });

    assertEquals("errorTypeProtocolFirstNoRemovals", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testErrorProtoTypeWasNotPresent()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorProtoWasNotPresent.cbs");
    });

    assertEquals("errorTypeProtocolWasNotPresent", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testErrorProtoTypeAlreadyPresent()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("errorProtoAlreadyPresent.cbs");
    });

    assertEquals("errorTypeProtocolAlreadyPresent", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  @Disabled("Not yet implemented.")
  public void testErrorRecordTypeTooLarge()
    throws Exception
  {
    assertThrows(CBTypeCheckFailedException.class, () -> {
      this.check("bigRecord0.cbs");
    });

    assertEquals("errorTypeRecordTooManyTypeReferences", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testDocumented0()
    throws Exception
  {
    this.loader.register(new CBFakePackage("x.y.z"));

    final var pack = this.check("documented0.cbs");
    assertEquals(0, this.errors.size());

    final var types = pack.types();
    assertEquals(3, types.size());
    final var protos = pack.protocols();
    assertEquals(1, protos.size());

    final var p = pack.userData().get(CBPackageType.class);

    {
      final var d = pack.documentation();
      assertEquals(3, d.size());
      assertEquals("Option", d.get(0).target());
      assertEquals("An Option", d.get(0).text());
      assertEquals("Pair", d.get(1).target());
      assertEquals("A Pair", d.get(1).text());
      assertEquals("Z", d.get(2).target());
      assertEquals("A Z", d.get(2).text());
    }

    {
      final var t = (CBASTTypeVariant) types.get(0);
      assertEquals(t.name().text(), "UnitType");

      final var pt = p.types().get(t.name().text());
      assertEquals(List.of(), pt.documentation());
    }

    {
      final var t = (CBASTTypeVariant) types.get(1);
      assertEquals(t.name().text(), "Option");

      {
        final var d = t.documentations();
        assertEquals(3, d.size());
        assertEquals("T", d.get(0).target());
        assertEquals("A T", d.get(0).text());
        assertEquals("None", d.get(1).target());
        assertEquals("A None", d.get(1).text());
        assertEquals("Some", d.get(2).target());
        assertEquals("A Some", d.get(2).text());
      }

      final var pt = (CBVariantType) p.types().get(t.name().text());
      assertEquals(List.of("An Option"), pt.documentation());

      {
        final var c = t.cases().get(0);
        final var pc = pt.cases().get(0);

        {
          final var d = c.documentations();
          assertEquals(0, c.documentations().size());
        }

        assertEquals(List.of("A None"), pc.documentation());
      }

      {
        final var c = t.cases().get(1);
        final var pc = pt.cases().get(1);

        {
          final var d = c.documentations();
          assertEquals(1, d.size());
          assertEquals("value", d.get(0).target());
          assertEquals("A value", d.get(0).text());

          final var pf = pc.fields().get(0);
          assertEquals(List.of("A value"), pf.documentation());
        }

        assertEquals(List.of("A Some"), pc.documentation());
      }
    }

    {
      final var t = (CBASTTypeRecord) types.get(2);
      assertEquals(t.name().text(), "Pair");

      final var pt = (CBRecordType) p.types().get(t.name().text());
      assertEquals(List.of("A Pair"), pt.documentation());

      {
        final var d = t.documentations();
        assertEquals(4, d.size());
        assertEquals("A", d.get(0).target());
        assertEquals("A A", d.get(0).text());
        assertEquals("B", d.get(1).target());
        assertEquals("A B", d.get(1).text());
        assertEquals("f0", d.get(2).target());
        assertEquals("A f0", d.get(2).text());
        assertEquals("f1", d.get(3).target());
        assertEquals("A f1", d.get(3).text());
      }

      {
        final var f = pt.parameters().get(0);
        assertEquals(List.of("A A"), f.documentation());
      }

      {
        final var f = pt.parameters().get(1);
        assertEquals(List.of("A B"), f.documentation());
      }

      {
        final var f = pt.fields().get(0);
        assertEquals(List.of("A f0"), f.documentation());
      }

      {
        final var f = pt.fields().get(1);
        assertEquals(List.of("A f1"), f.documentation());
      }
    }
  }
}
