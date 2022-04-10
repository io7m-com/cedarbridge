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

package com.io7m.cedarbridge.tests.codegen.java;

import com.io7m.cedarbridge.runtime.api.CBCoreSerializers;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializerCollection;
import com.io7m.cedarbridge.runtime.api.CBSerializerDirectoryMutable;
import com.io7m.cedarbridge.runtime.api.CBSerializerFactoryType;
import com.io7m.cedarbridge.runtime.api.CBSerializerType;
import com.io7m.cedarbridge.schema.core_types.CBCore;
import com.io7m.cedarbridge.tests.CBFakeLoader;
import com.io7m.cedarbridge.tests.CBFakePackage;
import com.io7m.cedarbridge.tests.CBTestDirectories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.Times;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public final class CBJavaCodeGeneratorTest
{
  private Path directory;
  private Path moduleDirectory;
  private CBFakeLoader loader;
  private CBSerializationContextType context;
  private CBSerializerDirectoryMutable serializers;
  private CBJavaCompilation compiled;

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
    this.serializers =
      new CBSerializerDirectoryMutable();
  }

  private void compile(
    final String name)
    throws Exception
  {
    this.compiled =
      CBJavaCompilation.compile(
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
      "x.y.z.T",
      "x.y.z.TSerializer",
      "x.y.z.TSerializerFactory"
    );

    final var tsfc = loader.loadClass("x.y.z.TSerializerFactory");
    final var tsf = instantiateFactory(tsfc);
    final var ts = tsf.create(this.serializers, List.of());
    ts.deserialize(this.context);
    verifyNoInteractions(this.context);
  }

  private ClassLoader loadClasses(
    final String... classes)
    throws Exception
  {
    final ClassLoader loader = this.compiled.classLoader();
    for (final var clazz : classes) {
      loader.loadClass(clazz);
    }
    return loader;
  }

  private static CBSerializerFactoryType<?> instantiateFactory(
    final Class<?> tsfc)
    throws Exception
  {
    return (CBSerializerFactoryType<?>) tsfc.getConstructor().newInstance();
  }

  @Test
  public void testRecordOk1()
    throws Exception
  {
    this.compile("typeRecordOk1.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.TSerializer",
      "x.y.z.TSerializerFactory",
      "x.y.z.U",
      "x.y.z.USerializer",
      "x.y.z.USerializerFactory"
    );
  }

  @Test
  public void testRecordOk2()
    throws Exception
  {
    this.compile("typeRecordOk2.cbs");

    final var loader = this.loadClasses(
      "x.y.z.U",
      "x.y.z.USerializer",
      "x.y.z.USerializerFactory"
    );
  }

  @Test
  public void testRecordOk3()
    throws Exception
  {
    this.compile("typeRecordOk3.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.TSerializer",
      "x.y.z.TSerializerFactory",
      "x.y.z.U",
      "x.y.z.USerializer",
      "x.y.z.USerializerFactory",
      "x.y.z.V",
      "x.y.z.VSerializer",
      "x.y.z.VSerializerFactory"
    );
  }

  @Test
  public void testRecordOk4()
    throws Exception
  {
    this.compile("typeRecordOk4.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.TSerializer",
      "x.y.z.TSerializerFactory",
      "x.y.z.U",
      "x.y.z.USerializer",
      "x.y.z.USerializerFactory",
      "x.y.z.V",
      "x.y.z.VSerializer",
      "x.y.z.VSerializerFactory"
    );
  }

  @Test
  public void testVariantOk0()
    throws Exception
  {
    this.compile("typeVariantOk0.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.TSerializer",
      "x.y.z.TSerializerFactory"
    );
  }

  @Test
  public void testVariantOk1()
    throws Exception
  {
    this.compile("typeVariantOk1.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.TSerializer",
      "x.y.z.TSerializerFactory",
      "x.y.z.U",
      "x.y.z.USerializer",
      "x.y.z.USerializerFactory"
    );
  }

  @Test
  public void testVariantOk2()
    throws Exception
  {
    this.compile("typeVariantOk2.cbs");

    final var loader = this.loadClasses(
      "x.y.z.U",
      "x.y.z.USerializer",
      "x.y.z.USerializerFactory"
    );
  }

  @Test
  public void testVariantOk3()
    throws Exception
  {
    this.compile("typeVariantOk3.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.TSerializer",
      "x.y.z.TSerializerFactory",
      "x.y.z.U",
      "x.y.z.USerializer",
      "x.y.z.USerializerFactory",
      "x.y.z.V",
      "x.y.z.VSerializer",
      "x.y.z.VSerializerFactory"
    );
  }

  @Test
  public void testVariantOk4()
    throws Exception
  {
    this.compile("typeVariantOk4.cbs");

    final var loader = this.loadClasses(
      "x.y.z.T",
      "x.y.z.TSerializer",
      "x.y.z.TSerializerFactory",
      "x.y.z.U",
      "x.y.z.USerializer",
      "x.y.z.USerializerFactory",
      "x.y.z.V",
      "x.y.z.VSerializer",
      "x.y.z.VSerializerFactory"
    );
  }

  @Test
  public void testBasic0()
    throws Exception
  {
    this.compile("basicNoImport.cbs");

    final var loader = this.loadClasses(
      "com.io7m.cedarbridge.Option",
      "com.io7m.cedarbridge.OptionSerializer",
      "com.io7m.cedarbridge.OptionSerializerFactory",
      "com.io7m.cedarbridge.List",
      "com.io7m.cedarbridge.ListSerializer",
      "com.io7m.cedarbridge.ListSerializerFactory",
      "com.io7m.cedarbridge.Unit",
      "com.io7m.cedarbridge.UnitSerializer",
      "com.io7m.cedarbridge.UnitSerializerFactory",
      "com.io7m.cedarbridge.Map",
      "com.io7m.cedarbridge.MapSerializer",
      "com.io7m.cedarbridge.MapSerializerFactory",
      "com.io7m.cedarbridge.Pair",
      "com.io7m.cedarbridge.PairSerializer",
      "com.io7m.cedarbridge.PairSerializerFactory"
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
      "com.io7m.cedarbridge.ListSerializer",
      "com.io7m.cedarbridge.ListSerializerFactory",
      "com.io7m.cedarbridge.Map",
      "com.io7m.cedarbridge.MapSerializer",
      "com.io7m.cedarbridge.MapSerializerFactory",
      "com.io7m.cedarbridge.Option",
      "com.io7m.cedarbridge.OptionSerializer",
      "com.io7m.cedarbridge.OptionSerializerFactory",
      "com.io7m.cedarbridge.Pair",
      "com.io7m.cedarbridge.PairSerializer",
      "com.io7m.cedarbridge.PairSerializerFactory",
      "com.io7m.cedarbridge.ProtocolZ",
      "com.io7m.cedarbridge.ProtocolZType",
      "com.io7m.cedarbridge.ProtocolZv0Serializer",
      "com.io7m.cedarbridge.ProtocolZv0SerializerFactory",
      "com.io7m.cedarbridge.ProtocolZv0Type",
      "com.io7m.cedarbridge.UnitType",
      "com.io7m.cedarbridge.UnitTypeSerializer",
      "com.io7m.cedarbridge.UnitTypeSerializerFactory"
    );
  }

  @Test
  public void testBasicWithCore()
    throws Exception
  {
    this.loader.register(CBCore.get());

    this.compile("basicWithCore.cbs");

    final var loader = this.loadClasses(
      "x.y.z.Mix",
      "x.y.z.MixSerializer",
      "x.y.z.MixSerializerFactory"
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
      "x.y.z.QSerializer",
      "x.y.z.QSerializerFactory",
      "x.y.z.List",
      "x.y.z.ListSerializer",
      "x.y.z.ListSerializerFactory",
      "x.y.z.Z",
      "x.y.z.ZSerializer",
      "x.y.z.ZSerializerFactory"
    );
  }

  @Test
  public void testCodegenUnit()
    throws Exception
  {
    this.compile("codegenUnit.cbs");

    final var loader = this.loadClasses(
      "x.Unit",
      "x.UnitSerializer",
      "x.UnitSerializerFactory"
    );

    final var f =
      this.createFactory(loader, "x.UnitSerializerFactory");

    assertThrows(IllegalArgumentException.class, () -> {
      f.deserialize(this.context);
    });

    verify(this.context, new Times(1)).readVariantIndex();
  }

  @Test
  public void testCodegenIntVec0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenIntVec0.cbs");

    final var loader = this.loadClasses(
      "x.IntVec",
      "x.IntVecSerializer",
      "x.IntVecSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.IntVecSerializerFactory");

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

    final var x = f.deserialize(this.context);
    f.serialize(this.context, x);

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
      "x.FloatVec",
      "x.FloatVecSerializer",
      "x.FloatVecSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.FloatVecSerializerFactory");

    when(Double.valueOf(this.context.readF16()))
      .thenReturn(Double.valueOf(16.0));
    when(Double.valueOf(this.context.readF32()))
      .thenReturn(Double.valueOf(32.0));
    when(Double.valueOf(this.context.readF64()))
      .thenReturn(Double.valueOf(64.0));

    final var x = f.deserialize(this.context);
    f.serialize(this.context, x);

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
      "x.Data",
      "x.DataSerializer",
      "x.DataSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.DataSerializerFactory");

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

    final var x = f.deserialize(this.context);
    f.serialize(this.context, x);

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
      "x.Data",
      "x.DataSerializer",
      "x.DataSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.DataSerializerFactory");

    when(Integer.valueOf(this.context.readSequenceLength()))
      .thenReturn(Integer.valueOf(5));

    when(Integer.valueOf(this.context.readS32()))
      .thenReturn(Integer.valueOf(100_0))
      .thenReturn(Integer.valueOf(100_1))
      .thenReturn(Integer.valueOf(100_2))
      .thenReturn(Integer.valueOf(100_3))
      .thenReturn(Integer.valueOf(100_4));

    final var x = f.deserialize(this.context);
    f.serialize(this.context, x);

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
      "x.Data",
      "x.DataSerializer",
      "x.DataSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.DataSerializerFactory");

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(0));

    final var x = f.deserialize(this.context);
    f.serialize(this.context, x);

    verify(this.context).writeVariantIndex(0);
  }

  @Test
  public void testCodegenOption1()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenOption0.cbs");

    final var loader = this.loadClasses(
      "x.Data",
      "x.DataSerializer",
      "x.DataSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.DataSerializerFactory");

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(1));
    when(Integer.valueOf(this.context.readS32()))
      .thenReturn(Integer.valueOf(1000));

    final var x = f.deserialize(this.context);
    f.serialize(this.context, x);

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
      "x.Data",
      "x.DataSerializer",
      "x.DataSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.DataSerializerFactory");

    when(Integer.valueOf(this.context.readVariantIndex()))
      .thenReturn(Integer.valueOf(2));

    {
      final var ex =
        assertThrows(IllegalStateException.class, () -> {
          f.deserialize(this.context);
        });
      assertTrue(ex.getMessage().contains("Unrecognized variant index: 2"));
    }
  }

  @Test
  public void testCodegenByteArray0()
    throws Exception
  {
    this.loader.register(CBCore.get());
    this.compile("codegenByteArray0.cbs");

    final var loader = this.loadClasses(
      "x.Data",
      "x.DataSerializer",
      "x.DataSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.DataSerializerFactory");

    when(this.context.readByteArray())
      .thenReturn(ByteBuffer.wrap("A ByteArray".getBytes(UTF_8)));

    final var x = f.deserialize(this.context);
    f.serialize(this.context, x);

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
      "x.Data",
      "x.DataSerializer",
      "x.DataSerializerFactory"
    );

    this.registerSerializers(CBCoreSerializers.get());

    final var f =
      this.createFactory(loader, "x.DataSerializerFactory");

    when(this.context.readUTF8()).thenReturn("A String");

    final var x = f.deserialize(this.context);
    f.serialize(this.context, x);

    verify(this.context).writeUTF8("A String");
  }

  private void registerSerializers(
    final CBSerializerCollection serializers)
  {
    for (final var s : serializers.serializers()) {
      this.serializers.addSerializer(s);
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends CBSerializableType> CBSerializerType<T> createFactory(
    final ClassLoader loader,
    final String name)
    throws Exception
  {
    final var ffc = loader.loadClass(name);
    final var ff = instantiateFactory(ffc);
    assertEquals(0, ff.typeParameters().size());
    return (CBSerializerType<T>) ff.create(this.serializers, List.of());
  }
}
