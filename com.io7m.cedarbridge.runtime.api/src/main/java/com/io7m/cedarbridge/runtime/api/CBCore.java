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

package com.io7m.cedarbridge.runtime.api;

/**
 * Convenience functions intended to be statically imported.
 */

public final class CBCore
{
  private CBCore()
  {

  }

  /**
   * Construct a Float16.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBFloat16 float16(
    final double x)
  {
    return CBFloat16.of(x);
  }

  /**
   * Construct a Float32.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBFloat32 float32(
    final double x)
  {
    return CBFloat32.of(x);
  }

  /**
   * Construct a Float64.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBFloat64 float64(
    final double x)
  {
    return CBFloat64.of(x);
  }

  /**
   * Construct an IntegerUnsigned64.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerUnsigned64 unsigned64(
    final long x)
  {
    return CBIntegerUnsigned64.of(x);
  }

  /**
   * Construct an IntegerUnsigned32.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerUnsigned32 unsigned32(
    final long x)
  {
    return CBIntegerUnsigned32.of(x);
  }

  /**
   * Construct an IntegerUnsigned32.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerUnsigned32 unsigned32(
    final int x)
  {
    return unsigned32(Integer.toUnsignedLong(x));
  }

  /**
   * Construct an IntegerUnsigned16.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerUnsigned16 unsigned16(
    final long x)
  {
    return unsigned16((int) x);
  }

  /**
   * Construct an IntegerUnsigned16.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerUnsigned16 unsigned16(
    final int x)
  {
    return CBIntegerUnsigned16.of(x);
  }

  /**
   * Construct an IntegerUnsigned8.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerUnsigned8 unsigned8(
    final long x)
  {
    return unsigned8((int) x);
  }

  /**
   * Construct an IntegerUnsigned8.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerUnsigned8 unsigned8(
    final int x)
  {
    return CBIntegerUnsigned8.of(x);
  }

  /**
   * Construct an IntegerSigned64.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerSigned64 signed64(
    final long x)
  {
    return CBIntegerSigned64.of(x);
  }

  /**
   * Construct an IntegerSigned32.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerSigned32 signed32(
    final long x)
  {
    return CBIntegerSigned32.of((int) x);
  }

  /**
   * Construct an IntegerSigned32.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerSigned32 signed32(
    final int x)
  {
    return CBIntegerSigned32.of(x);
  }

  /**
   * Construct an IntegerSigned16.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerSigned16 signed16(
    final long x)
  {
    return signed16((int) x);
  }

  /**
   * Construct an IntegerSigned16.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerSigned16 signed16(
    final int x)
  {
    return CBIntegerSigned16.of(x);
  }

  /**
   * Construct an IntegerSigned8.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerSigned8 signed8(
    final long x)
  {
    return signed8((int) x);
  }

  /**
   * Construct an IntegerSigned8.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBIntegerSigned8 signed8(
    final int x)
  {
    return CBIntegerSigned8.of(x);
  }

  /**
   * Construct a String.
   *
   * @param x The value
   *
   * @return A value
   */

  public static CBString string(
    final String x)
  {
    return CBString.of(x);
  }
}
