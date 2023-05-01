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

package com.io7m.cedarbridge.runtime.convenience;

import com.io7m.cedarbridge.runtime.api.CBBooleanType;
import com.io7m.cedarbridge.runtime.api.CBByteArray;
import com.io7m.cedarbridge.runtime.api.CBCore;
import com.io7m.cedarbridge.runtime.api.CBFloat16;
import com.io7m.cedarbridge.runtime.api.CBFloat32;
import com.io7m.cedarbridge.runtime.api.CBFloat64;
import com.io7m.cedarbridge.runtime.api.CBIntegerSigned16;
import com.io7m.cedarbridge.runtime.api.CBIntegerSigned32;
import com.io7m.cedarbridge.runtime.api.CBIntegerSigned64;
import com.io7m.cedarbridge.runtime.api.CBIntegerSigned8;
import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned16;
import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned32;
import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned64;
import com.io7m.cedarbridge.runtime.api.CBIntegerUnsigned8;
import com.io7m.cedarbridge.runtime.api.CBList;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.runtime.api.CBString;
import com.io7m.cedarbridge.runtime.api.CBUUID;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Convenience functions over lists (and collections).
 */

public final class CBLists
{
  private CBLists()
  {

  }

  /**
   * Transform a list to a Java list.
   *
   * @param f   The transform function
   * @param xs  The list
   * @param <A> The serializable type
   * @param <B> The Java type
   *
   * @return The Java list
   */

  public static <A extends CBSerializableType, B> List<B> toList(
    final CBList<A> xs,
    final Function<A, B> f)
  {
    return xs.values()
      .stream()
      .map(f)
      .toList();
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param f   The transform function
   * @param xs  The list
   * @param <A> The serializable type
   * @param <B> The Java type
   *
   * @return The Java set
   */

  public static <A, B extends CBSerializableType> CBList<B> ofCollection(
    final Collection<A> xs,
    final Function<A, B> f)
  {
    return new CBList<>(
      xs.stream()
        .map(f)
        .toList()
    );
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<ByteBuffer> toListByteArray(
    final CBList<CBByteArray> xs)
  {
    return toList(xs, CBByteArray::value);
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<UUID> toListUUID(
    final CBList<CBUUID> xs)
  {
    return toList(xs, CBUUID::value);
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<String> toListString(
    final CBList<CBString> xs)
  {
    return toList(xs, CBString::value);
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Boolean> toListBoolean(
    final CBList<CBBooleanType> xs)
  {
    return toList(xs, CBBooleanType::asBoolean);
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Integer> toListSigned8(
    final CBList<CBIntegerSigned8> xs)
  {
    return toList(xs, x -> Integer.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Integer> toListSigned16(
    final CBList<CBIntegerSigned16> xs)
  {
    return toList(xs, x -> Integer.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Long> toListSigned32(
    final CBList<CBIntegerSigned32> xs)
  {
    return toList(xs, x -> Long.valueOf((long) x.value()));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Long> toListSigned64(
    final CBList<CBIntegerSigned64> xs)
  {
    return toList(xs, x -> Long.valueOf((long) x.value()));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Integer> toListUnsigned8(
    final CBList<CBIntegerUnsigned8> xs)
  {
    return toList(xs, x -> Integer.valueOf(x.value() & 0xff));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Integer> toListUnsigned16(
    final CBList<CBIntegerUnsigned16> xs)
  {
    return toList(xs, x -> Integer.valueOf(x.value() & 0xffff));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Long> toListUnsigned32(
    final CBList<CBIntegerUnsigned32> xs)
  {
    return toList(xs, x -> Long.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Long> toListUnsigned64(
    final CBList<CBIntegerUnsigned64> xs)
  {
    return toList(xs, x -> Long.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Double> toListFloat16(
    final CBList<CBFloat16> xs)
  {
    return toList(xs, x -> Double.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Double> toListFloat32(
    final CBList<CBFloat32> xs)
  {
    return toList(xs, x -> Double.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java list.
   *
   * @param xs The list
   *
   * @return The Java list
   */

  public static List<Double> toListFloat64(
    final CBList<CBFloat64> xs)
  {
    return toList(xs, x -> Double.valueOf(x.value()));
  }


  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBByteArray> ofCollectionByteArray(
    final Collection<ByteBuffer> xs)
  {
    return ofCollection(xs, CBByteArray::new);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBUUID> ofCollectionUUID(
    final Collection<UUID> xs)
  {
    return ofCollection(xs, CBUUID::new);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBString> ofCollectionString(
    final Collection<String> xs)
  {
    return ofCollection(xs, CBString::new);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBBooleanType> ofCollectionBoolean(
    final Collection<Boolean> xs)
  {
    return ofCollection(xs, CBBooleanType::fromBoolean);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBIntegerSigned8> ofCollectionSigned8(
    final Collection<Integer> xs)
  {
    return ofCollection(xs, CBCore::signed8);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBIntegerSigned16> ofCollectionSigned16(
    final Collection<Integer> xs)
  {
    return ofCollection(xs, CBCore::signed16);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBIntegerSigned32> ofCollectionSigned32(
    final Collection<Long> xs)
  {
    return ofCollection(xs, CBCore::signed32);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBIntegerSigned64> ofCollectionSigned64(
    final Collection<Long> xs)
  {
    return ofCollection(xs, CBCore::signed64);
  }


  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBIntegerUnsigned8> ofCollectionUnsigned8(
    final Collection<Integer> xs)
  {
    return ofCollection(xs, CBCore::unsigned8);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBIntegerUnsigned16> ofCollectionUnsigned16(
    final Collection<Integer> xs)
  {
    return ofCollection(xs, CBCore::unsigned16);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBIntegerUnsigned32> ofCollectionUnsigned32(
    final Collection<Long> xs)
  {
    return ofCollection(xs, CBCore::unsigned32);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBIntegerUnsigned64> ofCollectionUnsigned64(
    final Collection<Long> xs)
  {
    return ofCollection(xs, CBCore::unsigned64);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBFloat16> ofCollectionFloat16(
    final Collection<Double> xs)
  {
    return ofCollection(xs, CBCore::float16);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBFloat32> ofCollectionFloat32(
    final Collection<Double> xs)
  {
    return ofCollection(xs, CBCore::float32);
  }

  /**
   * Transform a Java collection to a list.
   *
   * @param xs The Java collection
   *
   * @return The list
   */

  public static CBList<CBFloat64> ofCollectionFloat64(
    final Collection<Double> xs)
  {
    return ofCollection(xs, CBCore::float64);
  }
}
