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

package com.io7m.cedarbridge.tests.runtime.api;

import com.io7m.cedarbridge.runtime.time.CBDuration;
import com.io7m.cedarbridge.runtime.time.CBLocalDate;
import com.io7m.cedarbridge.runtime.time.CBLocalDateTime;
import com.io7m.cedarbridge.runtime.time.CBLocalTime;
import com.io7m.cedarbridge.runtime.time.CBOffsetDateTime;
import com.io7m.cedarbridge.runtime.time.CBZoneOffset;
import com.io7m.jbssio.vanilla.BSSReaders;
import com.io7m.jbssio.vanilla.BSSWriters;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.io7m.cedarbridge.runtime.bssio.CBSerializationContextBSSIO.createFromByteArray;
import static com.io7m.cedarbridge.runtime.bssio.CBSerializationContextBSSIO.createFromOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CBTimeTest
{
  private static final BSSReaders READERS = new BSSReaders();
  private static final BSSWriters WRITERS = new BSSWriters();

  @Provide
  public static Arbitrary<Duration> durations()
  {
    final var longs =
      Arbitraries.longs().between(0L, Long.MAX_VALUE >>> 1);

    return Combinators.combine(longs, longs)
      .as((x, y) -> Duration.ofSeconds(x.longValue(), y.longValue()));
  }

  @Provide
  public static Arbitrary<LocalDate> localDates()
  {
    final var longs = Arbitraries.longs().between(0L, 365241780471L);
    return longs.map(LocalDate::ofEpochDay);
  }

  @Provide
  public static Arbitrary<LocalTime> localTimes()
  {
    final var longs = Arbitraries.longs().between(0L, 86399999999999L);
    return longs.map(LocalTime::ofNanoOfDay);
  }

  @Provide
  public static Arbitrary<LocalDateTime> localDateTimes()
  {
    return Combinators.combine(localDates(), localTimes())
      .as(LocalDateTime::of);
  }

  @Provide
  public static Arbitrary<ZoneOffset> zoneOffsets()
  {
    return Arbitraries.integers()
      .between(-64800, 64800)
      .map(ZoneOffset::ofTotalSeconds);
  }

  @Provide
  public static Arbitrary<OffsetDateTime> offsetDateTimes()
  {
    return Combinators.combine(localDateTimes(), zoneOffsets())
      .as(OffsetDateTime::of);
  }

  @Property
  public void testDuration(
    final @ForAll("durations") Duration x)
    throws Exception
  {
    final var bao = new ByteArrayOutputStream();
    final var ctxOut = createFromOutputStream(WRITERS, bao);

    final var x0 = new CBDuration(x);
    CBDuration.serialize(ctxOut, x0);

    final var ctxIn = createFromByteArray(READERS, bao.toByteArray());
    final var x1 = CBDuration.deserialize(ctxIn);

    assertEquals(x0, x1);
    assertEquals(String.format("%s", x0), String.format("%s", x1));
    assertEquals(0, x0.compareTo(x1));
  }

  @Property
  public void testLocalDate(
    final @ForAll("localDates") LocalDate x)
    throws Exception
  {
    final var bao = new ByteArrayOutputStream();
    final var ctxOut = createFromOutputStream(WRITERS, bao);

    final var x0 = new CBLocalDate(x);
    CBLocalDate.serialize(ctxOut, x0);

    final var ctxIn = createFromByteArray(READERS, bao.toByteArray());
    final var x1 = CBLocalDate.deserialize(ctxIn);

    assertEquals(x0, x1);
    assertEquals(String.format("%s", x0), String.format("%s", x1));
    assertEquals(0, x0.compareTo(x1));
  }

  @Property
  public void testLocalTime(
    final @ForAll("localTimes") LocalTime x)
    throws Exception
  {
    final var bao = new ByteArrayOutputStream();
    final var ctxOut = createFromOutputStream(WRITERS, bao);

    final var x0 = new CBLocalTime(x);
    CBLocalTime.serialize(ctxOut, x0);

    final var ctxIn = createFromByteArray(READERS, bao.toByteArray());
    final var x1 = CBLocalTime.deserialize(ctxIn);

    assertEquals(x0, x1);
    assertEquals(String.format("%s", x0), String.format("%s", x1));
    assertEquals(0, x0.compareTo(x1));
  }

  @Property
  public void testLocalDateTime(
    final @ForAll("localDateTimes") LocalDateTime x)
    throws Exception
  {
    final var bao = new ByteArrayOutputStream();
    final var ctxOut = createFromOutputStream(WRITERS, bao);

    final var x0 = new CBLocalDateTime(x);
    CBLocalDateTime.serialize(ctxOut, x0);

    final var ctxIn = createFromByteArray(READERS, bao.toByteArray());
    final var x1 = CBLocalDateTime.deserialize(ctxIn);

    assertEquals(x0, x1);
    assertEquals(String.format("%s", x0), String.format("%s", x1));
    assertEquals(0, x0.compareTo(x1));
  }

  @Property
  public void testOffsetDateTime(
    final @ForAll("offsetDateTimes") OffsetDateTime x)
    throws Exception
  {
    final var bao = new ByteArrayOutputStream();
    final var ctxOut = createFromOutputStream(WRITERS, bao);

    final var x0 = new CBOffsetDateTime(x);
    CBOffsetDateTime.serialize(ctxOut, x0);

    final var ctxIn = createFromByteArray(READERS, bao.toByteArray());
    final var x1 = CBOffsetDateTime.deserialize(ctxIn);

    assertEquals(x0, x1);
    assertEquals(String.format("%s", x0), String.format("%s", x1));
    assertEquals(0, x0.compareTo(x1));
  }

  @Property
  public void testZoneOffset(
    final @ForAll("zoneOffsets") ZoneOffset x)
    throws Exception
  {
    final var bao = new ByteArrayOutputStream();
    final var ctxOut = createFromOutputStream(WRITERS, bao);

    final var x0 = new CBZoneOffset(x);
    CBZoneOffset.serialize(ctxOut, x0);

    final var ctxIn = createFromByteArray(READERS, bao.toByteArray());
    final var x1 = CBZoneOffset.deserialize(ctxIn);

    assertEquals(x0, x1);
    assertEquals(String.format("%s", x0), String.format("%s", x1));
    assertEquals(0, x0.compareTo(x1));
  }
}
