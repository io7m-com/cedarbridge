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

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Formatter;

@ImmutablesStyleType
@Value.Immutable(builder = false, copy = false)
public interface CBIntegerUnsigned32Type
  extends Comparable<CBIntegerUnsigned32>, CBIntegerType
{
  @Value.Parameter
  long value();

  @Value.Check
  default void checkPreconditions()
  {
    if (this.value() < 0L) {
      throw new IllegalArgumentException(
        String.format(
          "Value %s must be in the range [0, 4294967295]",
          Long.toUnsignedString(this.value()))
      );
    }

    if (this.value() > 0xffffffffL) {
      throw new IllegalArgumentException(
        String.format(
          "Value %s must be in the range [0, 4294967295]",
          Long.toUnsignedString(this.value()))
      );
    }
  }

  @Override
  default int compareTo(
    final CBIntegerUnsigned32 other)
  {
    return Long.compareUnsigned(this.value(), other.value());
  }

  @Override
  default void formatTo(
    final Formatter formatter,
    final int flags,
    final int width,
    final int precision)
  {
    try {
      formatter.out().append(Long.toUnsignedString(this.value()));
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
