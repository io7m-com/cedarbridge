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
import com.io7m.cedarbridge.runtime.convenience.CBSets;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CBSetsTest
{
  @Provide
  private static Arbitrary<Set<ByteBuffer>> byteBufferLists()
  {
    return Arbitraries.defaultFor(byte[].class)
      .map(ByteBuffer::wrap)
      .set();
  }

  @Provide
  private static Arbitrary<Set<UUID>> uuidLists()
  {
    return Arbitraries.create(UUID::randomUUID)
      .set();
  }

  @Property
  public void testByteArrayIdentity(
    final @ForAll("byteBufferLists") Set<ByteBuffer> data)
  {
    assertEquals(
      data,
      CBSets.toSetByteArray(CBLists.ofCollectionByteArray(data))
    );
  }

  @Property
  public void testStringIdentity(
    final @ForAll Set<String> data)
  {
    assertEquals(
      data,
      CBSets.toSetString(CBLists.ofCollectionString(data))
    );
  }

  @Property
  public void testUUIDIdentity(
    final @ForAll("uuidLists") Set<UUID> data)
  {
    assertEquals(
      data,
      CBSets.toSetUUID(CBLists.ofCollectionUUID(data))
    );
  }

  @Property
  public void testBooleanIdentity(
    final @ForAll Set<Boolean> data)
  {
    assertEquals(
      data,
      CBSets.toSetBoolean(CBLists.ofCollectionBoolean(data))
    );
  }

  @Provide
  private static Arbitrary<Set<Integer>> signed8()
  {
    return Arbitraries.integers()
      .between(-128, 127)
      .set();
  }

  @Property
  public void testIntegerSigned8Identity(
    final @ForAll("signed8") Set<Integer> data)
  {
    assertEquals(
      data,
      CBSets.toSetSigned8(CBLists.ofCollectionSigned8(data))
    );
  }

  @Provide
  private static Arbitrary<Set<Integer>> signed16()
  {
    return Arbitraries.integers()
      .between(-32768, 32767)
      .set();
  }

  @Property
  public void testIntegerSigned16Identity(
    final @ForAll("signed16") Set<Integer> data)
  {
    assertEquals(
      data,
      CBSets.toSetSigned16(CBLists.ofCollectionSigned16(data))
    );
  }

  @Provide
  private static Arbitrary<Set<Long>> signed32()
  {
    return Arbitraries.integers()
      .map(x -> Long.valueOf(x.longValue()))
      .set();
  }

  @Property
  public void testIntegerSigned32Identity(
    final @ForAll("signed32") Set<Long> data)
  {
    assertEquals(
      data,
      CBSets.toSetSigned32(CBLists.ofCollectionSigned32(data))
    );
  }

  @Property
  public void testIntegerSigned64Identity(
    final @ForAll Set<Long> data)
  {
    assertEquals(
      data,
      CBSets.toSetSigned64(CBLists.ofCollectionSigned64(data))
    );
  }

  @Provide
  private static Arbitrary<Set<Integer>> unsigned8()
  {
    return Arbitraries.integers()
      .between(0, 255)
      .set();
  }

  @Property
  public void testIntegerUnsigned8Identity(
    final @ForAll("unsigned8") Set<Integer> data)
  {
    assertEquals(
      data,
      CBSets.toSetUnsigned8(CBLists.ofCollectionUnsigned8(data))
    );
  }

  @Provide
  private static Arbitrary<Set<Integer>> unsigned16()
  {
    return Arbitraries.integers()
      .between(0, 65535)
      .set();
  }

  @Property
  public void testIntegerUnsigned16Identity(
    final @ForAll("unsigned16") Set<Integer> data)
  {
    assertEquals(
      data,
      CBSets.toSetUnsigned16(CBLists.ofCollectionUnsigned16(data))
    );
  }

  @Provide
  private static Arbitrary<Set<Long>> unsigned32()
  {
    return Arbitraries.longs()
      .between(0L, 0xffffffffL)
      .set();
  }

  @Property
  public void testIntegerUnsigned32Identity(
    final @ForAll("unsigned32") Set<Long> data)
  {
    assertEquals(
      data,
      CBSets.toSetUnsigned32(CBLists.ofCollectionUnsigned32(data))
    );
  }

  @Property
  public void testIntegerUnsigned64Identity(
    final @ForAll Set<Long> data)
  {
    assertEquals(
      data,
      CBSets.toSetUnsigned64(CBLists.ofCollectionUnsigned64(data))
    );
  }

  @Provide
  private static Arbitrary<Set<Double>> normalDoubles()
  {
    return Arbitraries.doubles()
      .between(-1.0, 1.0)
      .set();
  }

  @Property
  public void testFloat16Identity(
    final @ForAll("normalDoubles") Set<Double> data)
  {
    assertEquals(
      data,
      CBSets.toSetFloat16(CBLists.ofCollectionFloat16(data))
    );
  }

  @Property
  public void testFloat32Identity(
    final @ForAll("normalDoubles") Set<Double> data)
  {
    assertEquals(
      data,
      CBSets.toSetFloat32(CBLists.ofCollectionFloat32(data))
    );
  }

  @Property
  public void testFloat64Identity(
    final @ForAll("normalDoubles") Set<Double> data)
  {
    assertEquals(
      data,
      CBSets.toSetFloat64(CBLists.ofCollectionFloat64(data))
    );
  }
}
