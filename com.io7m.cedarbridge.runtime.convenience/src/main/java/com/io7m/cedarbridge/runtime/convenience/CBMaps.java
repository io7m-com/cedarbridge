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

import com.io7m.cedarbridge.runtime.api.CBMap;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.runtime.api.CBString;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Functions over maps.
 */

public final class CBMaps
{
  private CBMaps()
  {

  }

  /**
   * Transform a map to a Java map.
   *
   * @param xs   The map
   * @param kf   The key transform function
   * @param vf   The value transform function
   * @param <KI> The type of serializable keys
   * @param <VI> The type of serializable values
   * @param <KO> The type of Java keys
   * @param <VO> The type of Java values
   *
   * @return The Java map
   */

  public static <
    KI extends CBSerializableType,
    VI extends CBSerializableType,
    KO,
    VO>
  Map<KO, VO>
  toMap(
    final CBMap<KI, VI> xs,
    final Function<KI, KO> kf,
    final Function<VI, VO> vf)
  {
    return xs.values()
      .entrySet()
      .stream()
      .map(e -> Map.entry(kf.apply(e.getKey()), vf.apply(e.getValue())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Transform a map to a Java map.
   *
   * @param xs   The map
   * @param vf   The value transform function
   * @param <VI> The type of serializable values
   * @param <VO> The type of Java values
   *
   * @return The Java map
   */

  public static <VI extends CBSerializableType, VO> Map<String, VO>
  toMapStringKey(
    final CBMap<CBString, VI> xs,
    final Function<VI, VO> vf)
  {
    return toMap(xs, CBString::value, vf);
  }

  /**
   * Transform a map to a Java map.
   *
   * @param xs The map
   *
   * @return The Java map
   */

  public static Map<String, String>
  toMapString(
    final CBMap<CBString, CBString> xs)
  {
    return toMap(xs, CBString::value, CBString::value);
  }

  /**
   * Transform a Java map to a map.
   *
   * @param xs   The Java map
   * @param kf   The key transform function
   * @param vf   The value transform function
   * @param <KI> The type of serializable keys
   * @param <VI> The type of serializable values
   * @param <KO> The type of Java keys
   * @param <VO> The type of Java values
   *
   * @return The Java map
   */

  public static <
    KI,
    VI,
    KO extends CBSerializableType,
    VO extends CBSerializableType>
  CBMap<KO, VO>
  ofMap(
    final Map<KI, VI> xs,
    final Function<KI, KO> kf,
    final Function<VI, VO> vf)
  {
    return new CBMap<>(
      xs.entrySet()
        .stream()
        .map(e -> Map.entry(kf.apply(e.getKey()), vf.apply(e.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
  }

  /**
   * Transform a Java map to a map.
   *
   * @param xs   The Java map
   * @param vf   The value transform function
   * @param <VI> The type of serializable values
   * @param <VO> The type of Java values
   *
   * @return The map
   */

  public static <VI, VO extends CBSerializableType> CBMap<CBString, VO>
  ofMapStringKey(
    final Map<String, VI> xs,
    final Function<VI, VO> vf)
  {
    return ofMap(xs, CBString::new, vf);
  }

  /**
   * Transform a Java map to a map.
   *
   * @param xs The Java map
   *
   * @return The map
   */

  public static CBMap<CBString, CBString>
  ofMapString(
    final Map<String, String> xs)
  {
    return ofMap(xs, CBString::new, CBString::new);
  }
}
