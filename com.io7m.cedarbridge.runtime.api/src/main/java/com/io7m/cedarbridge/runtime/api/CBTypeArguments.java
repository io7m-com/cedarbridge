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

import java.util.List;

/**
 * Functions over type arguments.
 */

public final class CBTypeArguments
{
  private CBTypeArguments()
  {

  }

  /**
   * Construct a type argument.
   *
   * @param target    The target type
   * @param arguments The type arguments
   *
   * @return The type argument
   */

  public static CBTypeArgument of(
    final CBQualifiedTypeName target,
    final List<CBTypeArgument> arguments)
  {
    return CBTypeArgument.builder()
      .setTarget(target)
      .setArguments(arguments)
      .build();
  }

  /**
   * Check that the argument count matches the parameter count of the target type.
   *
   * @param packageName   The package name
   * @param typeName      The type name
   * @param expectedCount The expected number of arguments
   * @param receivedCount The received number of arguments
   */

  public static void checkArgumentCount(
    final String packageName,
    final String typeName,
    final int expectedCount,
    final int receivedCount)
  {
    if (expectedCount != receivedCount) {
      final var message = new StringBuilder(128);
      final var lineSeparator = System.lineSeparator();
      message.append("Incorrect number of type parameters provided.");
      message.append(lineSeparator);
      message.append("  Type: ");
      message.append(packageName);
      message.append(':');
      message.append(typeName);
      message.append(lineSeparator);
      message.append("  Expected: ");
      message.append(receivedCount);
      message.append(" parameters");
      message.append(lineSeparator);
      message.append("  Received: ");
      message.append(expectedCount);
      message.append(" parameters");
      message.append(lineSeparator);
      throw new IllegalArgumentException(message.toString());
    }
  }
}
