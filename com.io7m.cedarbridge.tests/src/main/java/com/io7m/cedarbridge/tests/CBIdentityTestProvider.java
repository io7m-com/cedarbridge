/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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


package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.runtime.api.CBBooleanType;
import com.io7m.cedarbridge.runtime.api.CBCore;
import com.io7m.cedarbridge.runtime.api.CBList;
import com.io7m.cedarbridge.runtime.api.CBString;
import com.io7m.cedarbridge.tests.identity.Enum0;
import com.io7m.cedarbridge.tests.identity.EveryScalar;
import com.io7m.cedarbridge.tests.identity.GenericABC;
import com.io7m.cedarbridge.tests.identity.ListOfEnum0;
import com.io7m.cedarbridge.tests.identity.ProtocolIdentityv1Type;
import com.io7m.cedarbridge.tests.identity.SomeGenerics;
import com.io7m.cedarbridge.tests.identity.SomeOptionals;
import com.io7m.cedarbridge.tests.identity.SomeStrings;
import com.io7m.cedarbridge.tests.identity.Vector3;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Optional;
import java.util.Set;

import static com.io7m.cedarbridge.runtime.api.CBCore.float16;
import static com.io7m.cedarbridge.runtime.api.CBCore.float32;
import static com.io7m.cedarbridge.runtime.api.CBCore.float64;
import static com.io7m.cedarbridge.runtime.api.CBCore.signed16;
import static com.io7m.cedarbridge.runtime.api.CBCore.signed32;
import static com.io7m.cedarbridge.runtime.api.CBCore.signed64;
import static com.io7m.cedarbridge.runtime.api.CBCore.signed8;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned16;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned32;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned64;
import static com.io7m.cedarbridge.runtime.api.CBCore.unsigned8;
import static com.io7m.cedarbridge.runtime.api.CBOptionType.fromOptional;
import static java.util.Optional.empty;

public final class CBIdentityTestProvider
  implements ArbitraryProvider
{
  @Override
  public boolean canProvideFor(
    final TypeUsage targetType)
  {
    return targetType.isOfType(ProtocolIdentityv1Type.class);
  }

  @Override
  public Set<Arbitrary<?>> provideFor(
    final TypeUsage targetType,
    final SubtypeProvider subtypeProvider)
  {
    return Set.of(
      enum0(),
      everyScalar(),
      listOfEnum0(),
      someOptionals(),
      someStrings(),
      someGenerics(),
      vector3()
    );
  }

  private static Arbitrary<SomeGenerics> someGenerics()
  {
    final var s8a =
      Arbitraries.integers().between(Byte.MIN_VALUE, Byte.MAX_VALUE);
    final var s16a =
      Arbitraries.integers().between(Short.MIN_VALUE, Short.MAX_VALUE);
    final var s32a =
      Arbitraries.integers().between(Integer.MIN_VALUE, Integer.MAX_VALUE);
    final var s64a =
      Arbitraries.longs().between(Long.MIN_VALUE, Long.MAX_VALUE);

    final var s0a =
      Arbitraries.strings();
    final var s1a =
      Arbitraries.strings();
    final var s2a =
      Arbitraries.strings();

    return Combinators.combine(s8a, s16a, s32a, s64a)
      .flatAs((s8, s16, s32, s64) -> {
        return Combinators.combine(s0a, s1a, s2a)
          .flatAs((s0, s1, s2) -> {
            return Combinators.combine(s8a, s8a.list(), s8a)
              .as((i0, i0l, i0o) -> {
                return new SomeGenerics(
                  new GenericABC<>(
                    signed8(s8),
                    signed16(s16),
                    signed32(s32)
                  ),
                  new GenericABC<>(
                    new CBString(s0),
                    new CBString(s1),
                    new CBString(s2)
                  ),
                  new GenericABC<>(
                    signed8(i0),
                    new CBList<>(i0l.stream().map(CBCore::signed8).toList()),
                    fromOptional(i0o % 2 == 0 ? Optional.of(signed8(i0o)) : empty())
                  )
                );
              });
          });
      });
  }

  private static Arbitrary<SomeOptionals> someOptionals()
  {
    final var s8a =
      Arbitraries.integers().between(Byte.MIN_VALUE, Byte.MAX_VALUE);
    final var s16a =
      Arbitraries.integers().between(Short.MIN_VALUE, Short.MAX_VALUE);
    final var s32a =
      Arbitraries.integers().between(Integer.MIN_VALUE, Integer.MAX_VALUE);
    final var s64a =
      Arbitraries.longs().between(Long.MIN_VALUE, Long.MAX_VALUE);

    return Combinators.combine(s8a, s16a, s32a, s64a)
      .as((s8, s16, s32, s64) -> {
        return new SomeOptionals(
          fromOptional(s8 % 2 == 0 ? Optional.of(signed8(s8)) : empty()),
          fromOptional(s16 % 2 == 0 ? Optional.of(signed16(s16)) : empty()),
          fromOptional(s32 % 2 == 0 ? Optional.of(signed32(s16)) : empty()),
          fromOptional(s64 % 2 == 0 ? Optional.of(signed64(s16)) : empty())
        );
      });
  }

  private static Arbitrary<EveryScalar> everyScalar()
  {
    final var s8a =
      Arbitraries.integers().between(Byte.MIN_VALUE, Byte.MAX_VALUE);
    final var s16a =
      Arbitraries.integers().between(Short.MIN_VALUE, Short.MAX_VALUE);
    final var s32a =
      Arbitraries.integers().between(Integer.MIN_VALUE, Integer.MAX_VALUE);
    final var s64a =
      Arbitraries.longs().between(Long.MIN_VALUE, Long.MAX_VALUE);

    final var u8a =
      Arbitraries.integers().between(0, 255);
    final var u16a =
      Arbitraries.integers().between(0, 65535);
    final var u32a =
      Arbitraries.longs().between(0L, 4294967295L);
    final var u64a =
      Arbitraries.longs();

    final var ds0 =
      Arbitraries.of(0.0);
    final var ds1 =
      Arbitraries.of(-1.0, 0.0, 1.0);
    final var ds2 =
      Arbitraries.of(-1.0, 0.0, 1.0);
    final var bs =
      Arbitraries.integers()
        .map(i -> Boolean.valueOf(i.intValue() % 2 == 0));

    return Combinators.combine(s8a, s16a, s32a, s64a)
      .flatAs((s8, s16, s32, s64) -> {
        return Combinators.combine(u8a, u16a, u32a, u64a)
          .flatAs((u8, u16, u32, u64) -> {
            return Combinators.combine(ds0, ds1, ds2, bs)
              .as((d0, d1, d2, b) -> {
                return new EveryScalar(
                  signed8(s8.intValue()),
                  signed16(s16.intValue()),
                  signed32(s32.intValue()),
                  signed64(s64.longValue()),
                  unsigned8(u8.intValue()),
                  unsigned16(u16.intValue()),
                  unsigned32(u32.intValue()),
                  unsigned64(u64.longValue()),
                  float16(d0.doubleValue()),
                  float32(d1.doubleValue()),
                  float64(d2.doubleValue()),
                  CBBooleanType.fromBoolean(b.booleanValue())
                );
              });
          });
      });
  }

  private static Arbitrary<SomeStrings> someStrings()
  {
    final var s0a =
      Arbitraries.strings();
    final var s1a =
      Arbitraries.strings();
    final var s2a =
      Arbitraries.strings();
    final var s3as =
      Arbitraries.strings()
        .list();

    return Combinators.combine(s0a, s1a, s2a, s3as).as((s0, s1, s2, sz0) -> {
      return new SomeStrings(
        new CBString(s0),
        new CBString(s1),
        new CBString(s2),
        new CBList<>(sz0.stream().map(CBString::new).toList())
      );
    });
  }

  private static Arbitrary<ListOfEnum0> listOfEnum0()
  {
    return enum0()
      .list()
      .map(enum0s -> {
        return new ListOfEnum0(new CBList<>(enum0s));
      });
  }

  private static Arbitrary<Vector3> vector3()
  {
    return Arbitraries.floats()
      .list()
      .ofSize(3)
      .map(floats -> {
        return new Vector3(
          float32(floats.get(0).doubleValue()),
          float32(floats.get(1).doubleValue()),
          float32(floats.get(2).doubleValue())
        );
      });
  }

  private static Arbitrary<Enum0> enum0()
  {
    return Arbitraries.integers()
      .between(0, 3)
      .map(integer -> {
        return switch (integer.intValue()) {
          case 0 -> new Enum0.Case0();
          case 1 -> new Enum0.Case1();
          case 2 -> new Enum0.Case2();
          case 3 -> new Enum0.Case3();
          default -> throw new IllegalStateException();
        };
      });
  }
}
