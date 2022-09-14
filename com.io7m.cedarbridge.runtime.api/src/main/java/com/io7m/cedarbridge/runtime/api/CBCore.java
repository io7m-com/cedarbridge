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
    return new CBFloat16(x);
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
    return new CBFloat32(x);
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
    return new CBFloat64(x);
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
    return new CBIntegerUnsigned64(x);
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
    return new CBIntegerUnsigned32(x);
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
    return new CBIntegerUnsigned16(x);
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
    return new CBIntegerUnsigned8(x);
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
    return new CBIntegerSigned64(x);
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
    return new CBIntegerSigned32((int) x);
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
    return new CBIntegerSigned32(x);
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
    return new CBIntegerSigned16(x);
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
    return new CBIntegerSigned8(x);
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
    return new CBString(x);
  }
}
