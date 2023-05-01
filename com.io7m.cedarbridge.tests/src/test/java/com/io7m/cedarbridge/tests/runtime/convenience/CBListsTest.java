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


package com.io7m.cedarbridge.tests.runtime.convenience;

import com.io7m.cedarbridge.runtime.convenience.CBLists;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CBListsTest
{
  @Provide
  private static Arbitrary<List<ByteBuffer>> byteBufferLists()
  {
    return Arbitraries.defaultFor(byte[].class)
      .map(ByteBuffer::wrap)
      .list();
  }

  @Provide
  private static Arbitrary<List<UUID>> uuidLists()
  {
    return Arbitraries.create(UUID::randomUUID)
      .list();
  }

  @Property
  public void testByteArrayIdentity(
    final @ForAll("byteBufferLists") List<ByteBuffer> data)
  {
    assertEquals(
      data,
      CBLists.toListByteArray(CBLists.ofCollectionByteArray(data))
    );
  }

  @Property
  public void testStringIdentity(
    final @ForAll List<String> data)
  {
    assertEquals(
      data,
      CBLists.toListString(CBLists.ofCollectionString(data))
    );
  }

  @Property
  public void testUUIDIdentity(
    final @ForAll("uuidLists") List<UUID> data)
  {
    assertEquals(
      data,
      CBLists.toListUUID(CBLists.ofCollectionUUID(data))
    );
  }

  @Property
  public void testBooleanIdentity(
    final @ForAll List<Boolean> data)
  {
    assertEquals(
      data,
      CBLists.toListBoolean(CBLists.ofCollectionBoolean(data))
    );
  }

  @Provide
  private static Arbitrary<List<Integer>> signed8()
  {
    return Arbitraries.integers()
      .between(-128, 127)
      .list();
  }

  @Property
  public void testIntegerSigned8Identity(
    final @ForAll("signed8") List<Integer> data)
  {
    assertEquals(
      data,
      CBLists.toListSigned8(CBLists.ofCollectionSigned8(data))
    );
  }

  @Provide
  private static Arbitrary<List<Integer>> signed16()
  {
    return Arbitraries.integers()
      .between(-32768, 32767)
      .list();
  }

  @Property
  public void testIntegerSigned16Identity(
    final @ForAll("signed16") List<Integer> data)
  {
    assertEquals(
      data,
      CBLists.toListSigned16(CBLists.ofCollectionSigned16(data))
    );
  }

  @Provide
  private static Arbitrary<List<Long>> signed32()
  {
    return Arbitraries.integers()
      .map(x -> Long.valueOf(x.longValue()))
      .list();
  }

  @Property
  public void testIntegerSigned32Identity(
    final @ForAll("signed32") List<Long> data)
  {
    assertEquals(
      data,
      CBLists.toListSigned32(CBLists.ofCollectionSigned32(data))
    );
  }

  @Property
  public void testIntegerSigned64Identity(
    final @ForAll List<Long> data)
  {
    assertEquals(
      data,
      CBLists.toListSigned64(CBLists.ofCollectionSigned64(data))
    );
  }

  @Provide
  private static Arbitrary<List<Integer>> unsigned8()
  {
    return Arbitraries.integers()
      .between(0, 255)
      .list();
  }

  @Property
  public void testIntegerUnsigned8Identity(
    final @ForAll("unsigned8") List<Integer> data)
  {
    assertEquals(
      data,
      CBLists.toListUnsigned8(CBLists.ofCollectionUnsigned8(data))
    );
  }

  @Provide
  private static Arbitrary<List<Integer>> unsigned16()
  {
    return Arbitraries.integers()
      .between(0, 65535)
      .list();
  }

  @Property
  public void testIntegerUnsigned16Identity(
    final @ForAll("unsigned16") List<Integer> data)
  {
    assertEquals(
      data,
      CBLists.toListUnsigned16(CBLists.ofCollectionUnsigned16(data))
    );
  }

  @Provide
  private static Arbitrary<List<Long>> unsigned32()
  {
    return Arbitraries.longs()
      .between(0L, 0xffffffffL)
      .list();
  }

  @Property
  public void testIntegerUnsigned32Identity(
    final @ForAll("unsigned32") List<Long> data)
  {
    assertEquals(
      data,
      CBLists.toListUnsigned32(CBLists.ofCollectionUnsigned32(data))
    );
  }

  @Property
  public void testIntegerUnsigned64Identity(
    final @ForAll List<Long> data)
  {
    assertEquals(
      data,
      CBLists.toListUnsigned64(CBLists.ofCollectionUnsigned64(data))
    );
  }

  @Provide
  private static Arbitrary<List<Double>> normalDoubles()
  {
    return Arbitraries.doubles()
      .between(-1.0, 1.0)
      .list();
  }

  @Property
  public void testFloat16Identity(
    final @ForAll("normalDoubles") List<Double> data)
  {
    assertEquals(
      data,
      CBLists.toListFloat16(CBLists.ofCollectionFloat16(data))
    );
  }

  @Property
  public void testFloat32Identity(
    final @ForAll("normalDoubles") List<Double> data)
  {
    assertEquals(
      data,
      CBLists.toListFloat32(CBLists.ofCollectionFloat32(data))
    );
  }

  @Property
  public void testFloat64Identity(
    final @ForAll("normalDoubles") List<Double> data)
  {
    assertEquals(
      data,
      CBLists.toListFloat64(CBLists.ofCollectionFloat64(data))
    );
  }
}
