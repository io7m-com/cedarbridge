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

package com.io7m.cedarbridge.tests.codegen.javastatic;

import com.io7m.cedarbridge.runtime.api.CBDeserializeType;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializeType;
import com.io7m.cedarbridge.schema.core_types.CBCore;
import com.io7m.cedarbridge.tests.CBFakeLoader;
import com.io7m.cedarbridge.tests.CBFakePackage;
import com.io7m.cedarbridge.tests.CBTestDirectories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class CBJavaStaticCodeGeneratorTest
{
  private Path directory;
  private Path moduleDirectory;
  private CBFakeLoader loader;
  private CBSerializationContextType context;
  private CBJavaStaticCompilation compiled;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory =
      CBTestDirectories.createTempDirectory();
    this.moduleDirectory =
      CBTestDirectories.createTempDirectory();
    this.loader =
      new CBFakeLoader();
    this.context =
      mock(CBSerializationContextType.class);

    when(this.context.errorUnrecognizedVariantIndex(Mockito.any(), Mockito.anyInt()))
      .thenReturn(new IOException("Unrecognized variant index"));
    when(this.context.errorUnrecognizedVariantCaseClass(Mockito.any(), Mockito.any()))
      .thenReturn(new IOException("Unrecognized variant class"));
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    CBTestDirectories.deleteDirectory(this.directory);
    CBTestDirectories.deleteDirectory(this.moduleDirectory);
  }

  private void compile(
    final String name)
    throws Exception
  {
    this.compiled =
      CBJavaStaticCompilation.compile(
        this.loader,
        this.directory,
        this.moduleDirectory,
        name
      );
  }

  @Test
  public void testRecordOk0()
    throws Exception
  {
    this.compile("typeRecordOk0.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T"
    );
  }

  private ClassLoader loadClasses(
    final String... classes)
    throws Exception
  {
    final ClassLoader loader = this.compiled.classLoader();
    for (final var clazz : classes) {
      loader.loadClass(Objects.requireNonNull(clazz, "clazz"));
    }
    return loader;
  }

  @Test
  public void testRecordOk1()
    throws Exception
  {
    this.compile("typeRecordOk1.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.U"
    );
  }

  @Test
  public void testRecordOk2()
    throws Exception
  {
    this.compile("typeRecordOk2.cbs");

    final var loader = this.loadClasses(
      "x.y.z.U"
    );
  }

  @Test
  public void testRecordOk3()
    throws Exception
  {
    this.compile("typeRecordOk3.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.U",
      "x.y.z.V"
    );
  }

  @Test
  public void testRecordOk4()
    throws Exception
  {
    this.compile("typeRecordOk4.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.U",
      "x.y.z.V"
    );
  }

  @Test
  public void testRecordOk7()
    throws Exception
  {
    this.compile("typeRecordOk7.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.U",
      "x.y.z.N"
    );

    {
      final var c = loader.loadClass("x.y.z.T");
      checkSerializeMethod(
        c,
        CBSerializationContextType.class,
        c,
        CBSerializeType.class
      );
      checkDeserializeMethod(
        c,
        CBSerializationContextType.class,
        CBDeserializeType.class
      );
    }

    {
      final var c = loader.loadClass("x.y.z.U");
      checkSerializeMethod(
        c,
        CBSerializationContextType.class,
        c
      );
      checkDeserializeMethod(
        c,
        CBSerializationContextType.class
      );
    }

    {
      final var c = loader.loadClass("x.y.z.N");
      checkSerializeMethod(
        c,
        CBSerializationContextType.class,
        c
      );
      checkDeserializeMethod(
        c,
        CBSerializationContextType.class
      );
    }
  }

  @Test
  public void testRecordOk8()
    throws Exception
  {
    this.compile("typeRecordOk8.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.U",
      "x.y.z.M",
      "x.y.z.N"
    );

    {
      final var c = loader.loadClass("x.y.z.N");
      checkSerializeMethod(
        c,
        CBSerializationContextType.class,
        c
      );
      checkDeserializeMethod(
        c,
        CBSerializationContextType.class
      );
    }

    {
      final var c = loader.loadClass("x.y.z.M");
      checkSerializeMethod(
        c,
        CBSerializationContextType.class,
        c,
        CBSerializeType.class
      );
      checkDeserializeMethod(
        c,
        CBSerializationContextType.class,
        CBDeserializeType.class
      );
    }

    {
      final var c = loader.loadClass("x.y.z.U");
      checkSerializeMethod(
        c,
        CBSerializationContextType.class,
        c
      );
      checkDeserializeMethod(
        c,
        CBSerializationContextType.class
      );
    }

    {
      final var c = loader.loadClass("x.y.z.T");
      checkSerializeMethod(
        c,
        CBSerializationContextType.class,
        c,
        CBSerializeType.class,
        CBSerializeType.class,
        CBSerializeType.class
      );
      checkDeserializeMethod(
        c,
        CBSerializationContextType.class,
        CBDeserializeType.class,
        CBDeserializeType.class,
        CBDeserializeType.class
      );
    }
  }

  private static void checkDeserializeMethod(
    final Class<?> c,
    final Class<?>... arguments)
    throws Exception
  {
    final var m = c.getMethod("deserialize", arguments);
    assertEquals(c, m.getReturnType());
    assertEquals(PUBLIC, m.getModifiers() & PUBLIC);
    assertEquals(STATIC, m.getModifiers() & STATIC);
  }

  private static void checkSerializeMethod(
    final Class<?> c,
    final Class<?>... arguments)
    throws Exception
  {
    final var m = c.getMethod("serialize", arguments);
    assertEquals(void.class, m.getReturnType());
    assertEquals(PUBLIC, m.getModifiers() & PUBLIC);
    assertEquals(STATIC, m.getModifiers() & STATIC);
  }

  @Test
  public void testVariantOk0()
    throws Exception
  {
    this.compile("typeVariantOk0.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T"
    );
  }

  @Test
  public void testVariantOk1()
    throws Exception
  {
    this.compile("typeVariantOk1.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.U"
    );
  }

  @Test
  public void testVariantOk2()
    throws Exception
  {
    this.compile("typeVariantOk2.cbs");

    final var loader = this.loadClasses(
      "x.y.z.U"
    );
  }

  @Test
  public void testVariantOk3()
    throws Exception
  {
    this.compile("typeVariantOk3.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.U",
      "x.y.z.V"
    );
  }

  @Test
  public void testVariantOk4()
    throws Exception
  {
    this.compile("typeVariantOk4.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.U",
      "x.y.z.V"
    );
  }

  @Test
  public void testVariantOk7()
    throws Exception
  {
    this.compile("typeVariantOk7.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.T$C2",
      "x.y.z.T$C1",
      "x.y.z.T$C0"
    );
  }

  @Test
  public void testBasic0()
    throws Exception
  {
    this.compile("basicNoImport.cbs");

    final var loader = this.loadClasses(
      "com.io7m.cedarbridge.Option",
      "com.io7m.cedarbridge.List",
      "com.io7m.cedarbridge.Unit",
      "com.io7m.cedarbridge.Map",
      "com.io7m.cedarbridge.Pair"
    );
  }

  @Test
  public void testBasic()
    throws Exception
  {
    this.loader.register(new CBFakePackage("x.y.z"));
    this.loader.register(CBCore.get());

    this.compile("basic.cbs");

    final var loader = this.loadClasses(
      "com.io7m.cedarbridge.List",
      "com.io7m.cedarbridge.Map",
      "com.io7m.cedarbridge.Option",
      "com.io7m.cedarbridge.Pair",
      "com.io7m.cedarbridge.ProtocolZType",
      "com.io7m.cedarbridge.ProtocolZv0Type",
      "com.io7m.cedarbridge.UnitType"
    );
  }

  @Test
  public void testBasicWithCore()
    throws Exception
  {
    this.loader.register(CBCore.get());

    this.compile("basicWithCore.cbs");

    final var loader = this.loadClasses(
      "x.y.z.Mix"
    );
  }

  @Test
  public void testBasicType3()
    throws Exception
  {
    this.loader.register(CBCore.get());

    this.compile("basicType3.cbs");

    final var loader = this.loadClasses(
      "x.y.z.Q",
      "x.y.z.List",
      "x.y.z.Z"
    );
  }

  @Test
  public void testCodegenUnit()
    throws Exception
  {
    this.compile("codegenUnit.cbs");

    final var loader = this.loadClasses(
      "x.Unit"
    );

    final var c =
      loader.loadClass("x.Unit");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    final var ex =
      assertThrows(InvocationTargetException.class, () -> {
        d.invoke(c, this.context);
      });
    assertInstanceOf(IOException.class, ex.getCause());

    verify(this.context, new Times(1)).readVariantIndex();
  }

  @Test
  public void testCodegenIntVec0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenIntVec0.cbs");

    final var loader = this.loadClasses(
      "x.IntVec"
    );

    final var c =
      loader.loadClass("x.IntVec");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(Integer.valueOf(this.context.readS8()))
      .thenReturn(Integer.valueOf(8));
    when(Integer.valueOf(this.context.readS16()))
      .thenReturn(Integer.valueOf(16));
    when(Integer.valueOf(this.context.readS32()))
      .thenReturn(Integer.valueOf(32));
    when(Long.valueOf(this.context.readS64()))
      .thenReturn(Long.valueOf(64L));
    when(Integer.valueOf(this.context.readU8()))
      .thenReturn(Integer.valueOf(80));
    when(Integer.valueOf(this.context.readU16()))
      .thenReturn(Integer.valueOf(160));
    when(Long.valueOf(this.context.readU32()))
      .thenReturn(Long.valueOf(320));
    when(Long.valueOf(this.context.readU64()))
      .thenReturn(Long.valueOf(640L));

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeS8(8L);
    verify(this.context).writeS16(16L);
    verify(this.context).writeS32(32L);
    verify(this.context).writeS64(64L);
    verify(this.context).writeU8(80L);
    verify(this.context).writeU16(160L);
    verify(this.context).writeU32(320L);
    verify(this.context).writeU64(640L);
    verify(this.context, new Times(1)).readS8();
    verify(this.context, new Times(1)).readS16();
    verify(this.context, new Times(1)).readS32();
    verify(this.context, new Times(1)).readS64();
    verify(this.context, new Times(1)).readU8();
    verify(this.context, new Times(1)).readU16();
    verify(this.context, new Times(1)).readU32();
    verify(this.context, new Times(1)).readU64();
  }

  @Test
  public void testCodegenFloatVec0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenFloatVec0.cbs");

    final var loader = this.loadClasses(
      "x.FloatVec"
    );

    final var c =
      loader.loadClass("x.FloatVec");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(Double.valueOf(this.context.readF16()))
      .thenReturn(Double.valueOf(16.0));
    when(Double.valueOf(this.context.readF32()))
      .thenReturn(Double.valueOf(32.0));
    when(Double.valueOf(this.context.readF64()))
      .thenReturn(Double.valueOf(64.0));

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeF16(16.0);
    verify(this.context).writeF32(32.0);
    verify(this.context).writeF64(64.0);
    verify(this.context, new Times(1)).readF16();
    verify(this.context, new Times(1)).readF32();
    verify(this.context, new Times(1)).readF64();
  }

  @Test
  public void testCodegenData0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenMap0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c =
      loader.loadClass("x.Data");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(Integer.valueOf(this.context.readSequenceLength()))
      .thenReturn(Integer.valueOf(5));

    when(Integer.valueOf(this.context.readS32()))
      .thenReturn(Integer.valueOf(10_0))
      .thenReturn(Integer.valueOf(20_0))
      .thenReturn(Integer.valueOf(10_1))
      .thenReturn(Integer.valueOf(20_1))
      .thenReturn(Integer.valueOf(10_2))
      .thenReturn(Integer.valueOf(20_2))
      .thenReturn(Integer.valueOf(10_3))
      .thenReturn(Integer.valueOf(20_3))
      .thenReturn(Integer.valueOf(10_4))
      .thenReturn(Integer.valueOf(20_4));

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeSequenceLength(5);
    verify(this.context).writeS32(10_0L);
    verify(this.context).writeS32(20_0L);
    verify(this.context).writeS32(10_1L);
    verify(this.context).writeS32(20_1L);
    verify(this.context).writeS32(10_2L);
    verify(this.context).writeS32(20_2L);
    verify(this.context).writeS32(10_3L);
    verify(this.context).writeS32(20_3L);
    verify(this.context).writeS32(10_4L);
    verify(this.context).writeS32(20_4L);
  }

  @Test
  public void testCodegenList0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenList0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c =
      loader.loadClass("x.Data");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(Integer.valueOf(this.context.readSequenceLength()))
      .thenReturn(Integer.valueOf(5));

    when(Integer.valueOf(this.context.readS32()))
      .thenReturn(Integer.valueOf(100_0))
      .thenReturn(Integer.valueOf(100_1))
      .thenReturn(Integer.valueOf(100_2))
      .thenReturn(Integer.valueOf(100_3))
      .thenReturn(Integer.valueOf(100_4));

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeSequenceLength(5);
    verify(this.context).writeS32(100_0L);
    verify(this.context).writeS32(100_1L);
    verify(this.context).writeS32(100_2L);
    verify(this.context).writeS32(100_3L);
    verify(this.context).writeS32(100_4L);
  }

  @Test
  public void testCodegenOption0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenOption0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c =
      loader.loadClass("x.Data");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(0));

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeVariantIndex(0);
  }

  @Test
  public void testCodegenOption1()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenOption0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c =
      loader.loadClass("x.Data");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(1));
    when(Integer.valueOf(this.context.readS32()))
      .thenReturn(Integer.valueOf(1000));

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeVariantIndex(1);
    verify(this.context).writeS32(1000L);
  }

  @Test
  public void testCodegenOption2()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenOption0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c =
      loader.loadClass("x.Data");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(2));

    {
      final var ex =
        assertThrows(InvocationTargetException.class, () -> {
          d.invoke(c, this.context);
        });
      assertInstanceOf(IOException.class, ex.getCause());
    }
  }

  @Test
  public void testCodegenByteArray0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenByteArray0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c =
      loader.loadClass("x.Data");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(this.context.readByteArray())
      .thenReturn(ByteBuffer.wrap("A ByteArray".getBytes(UTF_8)));

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context)
      .writeByteArray(ByteBuffer.wrap("A ByteArray".getBytes(UTF_8)));
  }

  @Test
  public void testCodegenString0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenString0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c =
      loader.loadClass("x.Data");
    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    when(this.context.readUTF8()).thenReturn("A String");

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeUTF8("A String");
  }

  @Test
  public void testCodegenBoolean0_0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenBoolean0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c = loader.loadClass("x.Data");

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(0));

    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeVariantIndex(0);
  }

  @Test
  public void testCodegenBoolean0_1()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenBoolean0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c = loader.loadClass("x.Data");

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(1));

    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);
    final var s =
      c.getMethod("serialize", CBSerializationContextType.class, c);

    final var x = d.invoke(c, this.context);
    s.invoke(c, this.context, x);

    verify(this.context).writeVariantIndex(1);
  }

  @Test
  public void testCodegenBoolean0_2()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenBoolean0.cbs");

    final var loader = this.loadClasses(
      "x.Data"
    );

    final var c = loader.loadClass("x.Data");

    final var d =
      c.getMethod("deserialize", CBSerializationContextType.class);

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(2));

    final var ex =
      assertThrows(InvocationTargetException.class, () -> {
        d.invoke(c, this.context);
      });
    assertInstanceOf(IOException.class, ex.getCause());
  }

  @Test
  public void testBug14()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("bug14.cbs");

    final var loader = this.loadClasses(
      "x.IdA1Page"
    );

    final var c = loader.loadClass("x.IdA1Page");
    assertEquals(1, c.getTypeParameters().length);
  }

  @Test
  public void testBug15()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("bug15.cbs");

    final var loader = this.loadClasses(
      "x.IdA1AdminPermission",
      "x.IdA1AdminPermission$UserBan",
      "x.IdA1AdminPermission$UserRead",
      "x.IdA1AdminPermission$UserWrite",
      "x.IdA1AdminPermission$UserCreate",
      "x.IdA1AdminPermission$UserDelete",
      "x.IdA1AdminPermission$AuditRead",
      "x.IdA1AdminPermission$AdminRead",
      "x.IdA1AdminPermission$AdminWriteSelf",
      "x.IdA1AdminPermission$AdminWrite",
      "x.IdA1AdminPermission$AdminDelete",
      "x.IdA1AdminPermission$AdminCreate",
      "x.IdA1AdminPermission$AdminBan"
    );
  }

  @Test
  @Disabled("Not valid for large variants.")
  public void testBigVariant0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("bigVariant0.cbs");

    final var loader = this.loadClasses(
      "x.TSerializerFactory",
      "x.TSerializer",
      "x.Serializers"
    );
  }

  @Test
  public void testBigVariant1()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("bigVariant1.cbs");

    final var classNames =
      Stream.concat(
        Stream.of("x.T"),
        IntStream.range(0, 254).mapToObj("x.T$C%d"::formatted)
      ).toList();

    final var classNameArray = new String[classNames.size()];
    classNames.toArray(classNameArray);

    final var loader = this.loadClasses(classNameArray);
    classNames.forEach(c -> {
      try {
        loader.loadClass(c);
      } catch (final ClassNotFoundException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  @Test
  @Disabled("Not yet allowed.")
  public void testBigProto0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("bigProto0.cbs");

    final var classNames =
      Stream.concat(
        Stream.of(
          "x.ProtocolPType",
          "x.ProtocolPv1Type"),
        IntStream.range(0, 255).mapToObj("x.T%d"::formatted)
      ).toList();

    final var classNameArray = new String[classNames.size()];
    classNames.toArray(classNameArray);

    final var loader = this.loadClasses(classNameArray);
    classNames.forEach(c -> {
      try {
        loader.loadClass(c);
      } catch (final ClassNotFoundException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  @Test
  public void testBigProto1()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("bigProto1.cbs");

    final var classNames =
      Stream.concat(
        Stream.of(
          "x.ProtocolPType",
          "x.ProtocolPv1Type"),
        IntStream.range(0, 254).mapToObj("x.T%d"::formatted)
      ).toList();

    final var classNameArray = new String[classNames.size()];
    classNames.toArray(classNameArray);

    final var loader = this.loadClasses(classNameArray);
    classNames.forEach(c -> {
      try {
        loader.loadClass(c);
      } catch (final ClassNotFoundException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  @Test
  @Disabled("Not yet implemented.")
  public void testBigRecord0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("bigRecord0.cbs");

    final var loader = this.loadClasses(
      "x.TSerializerFactory",
      "x.TSerializer",
      "x.Serializers"
    );
  }

  @Test
  public void testBigRecord1()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("bigRecord1.cbs");

    final var classNames =
      IntStream.range(0, 254)
        .mapToObj("x.T%d"::formatted)
        .toList();

    final var classNameArray = new String[classNames.size()];
    classNames.toArray(classNameArray);

    final var loader = this.loadClasses(classNameArray);
    classNames.forEach(c -> {
      try {
        loader.loadClass(c);
      } catch (final ClassNotFoundException e) {
        throw new IllegalStateException(e);
      }
    });
  }
}
