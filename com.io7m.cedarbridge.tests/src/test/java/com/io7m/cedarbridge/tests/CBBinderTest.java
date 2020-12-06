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
import com.io7m.cedarbridge.schema.ast.CBASTElementType;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.ast.CBASTTypeApplication;
import com.io7m.cedarbridge.schema.ast.CBASTTypeNamed;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.binder.CBBinderFactory;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBindingExternal;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeParameter;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.parser.CBParserFactory;
import org.junit.jupiter.api.Assertions;
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

import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBBinderTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBBinderTest.class);

  private static final CBExpressionSources SOURCES =
    new CBExpressionSources();

  private CBExpressionSourceType source;
  private CBBinderFactory binders;
  private CBParserFactory parsers;
  private CBExpressionSources sources;
  private ArrayList<CBError> errors;
  private Path directory;
  private CBFakeLoader loader;
  private HashMap<BigInteger, CBBindingLocalType> bindings;

  private CBASTPackage parse(
    final String name)
    throws Exception
  {
    final var path =
      CBTestDirectories.resourceOf(CBBinderTest.class, this.directory, name);
    this.source =
      this.sources.create(path.toUri(), Files.newInputStream(path));
    final var parser =
      this.parsers.createParser(this::addError, this.source);
    return parser.execute();
  }

  private CBASTPackage bind(
    final String name)
    throws Exception
  {
    final var parsedPackage = this.parse(name);
    try (var binder =
           this.binders.createBinder(
             this.loader, this::addError, this.source, parsedPackage)) {
      binder.execute();
      return parsedPackage;
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
  }

  @Test
  public void testUnresolvablePackage()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("basic.cbs");
    });

    assertEquals("errorPackageUnavailable", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testImportConflict()
    throws Exception
  {
    this.loader.register(new CBFakePackage("x.y.z"));
    this.loader.register(new CBFakePackage("a.b.c"));

    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindPackageTwice.cbs");
    });

    assertEquals("errorPackageShortNameUsed", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testDuplicateType0()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindDuplicateType0.cbs");
    });

    assertEquals("errorBindingConflict", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testDuplicateField0()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindDuplicateField0.cbs");
    });

    assertEquals("errorBindingConflict", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testDuplicateVariantCase0()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindDuplicateVariantCase0.cbs");
    });

    assertEquals("errorBindingConflict", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testDuplicateVariantCaseField0()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindDuplicateVariantCaseField0.cbs");
    });

    assertEquals("errorBindingConflict", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testDuplicateTypeParameter0()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindDuplicateTypeParameter0.cbs");
    });

    assertEquals("errorBindingConflict", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testMissingType0()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindTypeReference0.cbs");
    });

    assertEquals("errorBindingMissing", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testMissingType1()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindTypeReference1.cbs");
    });

    assertEquals("errorBindingMissing", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testMissingType2()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindTypeReference2.cbs");
    });

    assertEquals("errorBindingMissing", this.takeError().errorCode());
    assertEquals("errorBindingMissing", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testMissingType3()
    throws Exception
  {
    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindTypeReference3.cbs");
    });

    assertEquals("errorPackageUnavailable", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testMissingType4()
    throws Exception
  {
    this.loader.register(new CBFakePackage("x.y.z"));

    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorBindTypeReference4.cbs");
    });

    assertEquals("errorTypeMissing", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testOK0()
    throws Exception
  {
    final var pack = this.bind("bindOk0.cbs");

    assertEquals(0, pack.imports().size());

    final var types = pack.types();
    assertEquals(3, types.size());

    {
      final var t = (CBASTTypeRecord) types.get(0);
      this.checkHaveNotSeenBefore(t.name());
      assertEquals(0, t.parameters().size());
      assertEquals(0, t.fields().size());
    }

    {
      final var t = (CBASTTypeRecord) types.get(1);
      this.checkHaveNotSeenBefore(t.name());
      assertEquals(1, t.parameters().size());
      for (final var p : t.parameters()) {
        this.checkHaveNotSeenBefore(p);
      }
      final var tf = t.fields();
      assertEquals(1, tf.size());
      final var f0 = tf.get(0);
      this.checkHaveNotSeenBefore(f0.name());

      final var f0t = (CBASTTypeNamed) f0.type();
      final var f0tb =
        (CBBindingLocalTypeParameter) this.checkHaveSeenBefore(f0t);
    }

    {
      final var t = (CBASTTypeRecord) types.get(2);
      this.checkHaveNotSeenBefore(t.name());

      assertEquals(0, t.parameters().size());
      final var tf = t.fields();
      assertEquals(2, tf.size());

      final var f0 = tf.get(0);
      this.checkHaveNotSeenBefore(f0.name());

      final var ta = (CBASTTypeApplication) f0.type();
      final var tab0 =
        (CBBindingLocalTypeDeclaration) this.checkHaveSeenBefore(ta.target());

      final var arguments = ta.arguments();
      assertEquals(1, arguments.size());
      final var tab1 =
        (CBBindingLocalTypeDeclaration) this.checkHaveSeenBefore(arguments.get(0));

      final var f1 = tf.get(1);
      this.checkHaveNotSeenBefore(f1.name());
    }

    assertEquals(0, this.errors.size());
  }

  @Test
  public void testOK1()
    throws Exception
  {
    final var otherPack = new CBFakePackage("x.y.z");
    final var rec = new CBFakeRecord("T", 0);
    otherPack.addType(rec);
    this.loader.register(otherPack);

    final var pack = this.bind("bindOk1.cbs");

    assertEquals(1, pack.imports().size());

    final var types = pack.types();
    assertEquals(1, types.size());

    {
      final var t = (CBASTTypeRecord) types.get(0);
      this.checkHaveNotSeenBefore(t.name());
      final var tParams = t.parameters();
      assertEquals(0, tParams.size());
      final var tFields = t.fields();
      assertEquals(1, tFields.size());
      final var f = tFields.get(0);
      assertEquals("x", f.name().text());

      final var fType =
        (CBASTTypeNamed) f.type();
      final var fBind =
        (CBBindingExternal) fType.userData().get(CBBindingType.class);

      assertSame(rec, fBind.type());
    }

    assertEquals(0, this.errors.size());
  }

  private CBBindingLocalType checkHaveSeenBefore(
    final CBASTElementType element)
  {
    return this.checkHaveSeenBefore(
      (CBBindingLocalType) element.userData().get(CBBindingType.class)
    );
  }

  private void checkHaveNotSeenBefore(
    final CBASTElementType element)
  {
    this.checkHaveNotSeenBefore(
      (CBBindingLocalType) element.userData().get(CBBindingType.class)
    );
  }

  private CBBindingLocalType checkHaveSeenBefore(
    final CBBindingLocalType bind)
  {
    final var existing = this.bindings.get(bind.id());
    if (existing == null) {
      Assertions.fail(
        String.format("Binding %s points to nothing", bind)
      );
    }
    return existing;
  }

  private void checkHaveNotSeenBefore(
    final CBBindingLocalType bind)
  {
    final var existing = this.bindings.get(bind.id());
    if (existing != null) {
      Assertions.fail(
        String.format("Binding %s has been seen before (%s)", bind, existing)
      );
    }
    this.bindings.put(bind.id(), bind);
  }
}
