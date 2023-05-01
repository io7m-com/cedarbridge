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
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Convenience functions over sets.
 */

public final class CBSets
{
  private CBSets()
  {

  }

  /**
   * Transform a list to a Java set.
   *
   * @param f   The transform function
   * @param xs  The list
   * @param <A> The serializable type
   * @param <B> The Java type
   *
   * @return The Java set
   */

  public static <A extends CBSerializableType, B> Set<B> toSet(
    final CBList<A> xs,
    final Function<A, B> f)
  {
    return xs.values()
      .stream()
      .map(f)
      .collect(Collectors.toUnmodifiableSet());
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<ByteBuffer> toSetByteArray(
    final CBList<CBByteArray> xs)
  {
    return toSet(xs, CBByteArray::value);
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<UUID> toSetUUID(
    final CBList<CBUUID> xs)
  {
    return toSet(xs, CBUUID::value);
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<String> toSetString(
    final CBList<CBString> xs)
  {
    return toSet(xs, CBString::value);
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Boolean> toSetBoolean(
    final CBList<CBBooleanType> xs)
  {
    return toSet(xs, CBBooleanType::asBoolean);
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Integer> toSetSigned8(
    final CBList<CBIntegerSigned8> xs)
  {
    return toSet(xs, x -> Integer.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Integer> toSetSigned16(
    final CBList<CBIntegerSigned16> xs)
  {
    return toSet(xs, x -> Integer.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Long> toSetSigned32(
    final CBList<CBIntegerSigned32> xs)
  {
    return toSet(xs, x -> Long.valueOf((long) x.value()));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Long> toSetSigned64(
    final CBList<CBIntegerSigned64> xs)
  {
    return toSet(xs, x -> Long.valueOf((long) x.value()));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Integer> toSetUnsigned8(
    final CBList<CBIntegerUnsigned8> xs)
  {
    return toSet(xs, x -> Integer.valueOf(x.value() & 0xff));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Integer> toSetUnsigned16(
    final CBList<CBIntegerUnsigned16> xs)
  {
    return toSet(xs, x -> Integer.valueOf(x.value() & 0xffff));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Long> toSetUnsigned32(
    final CBList<CBIntegerUnsigned32> xs)
  {
    return toSet(xs, x -> Long.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Long> toSetUnsigned64(
    final CBList<CBIntegerUnsigned64> xs)
  {
    return toSet(xs, x -> Long.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Double> toSetFloat16(
    final CBList<CBFloat16> xs)
  {
    return toSet(xs, x -> Double.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Double> toSetFloat32(
    final CBList<CBFloat32> xs)
  {
    return toSet(xs, x -> Double.valueOf(x.value()));
  }

  /**
   * Transform a list to a Java set.
   *
   * @param xs The list
   *
   * @return The Java set
   */

  public static Set<Double> toSetFloat64(
    final CBList<CBFloat64> xs)
  {
    return toSet(xs, x -> Double.valueOf(x.value()));
  }
}
