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

package com.io7m.cedarbridge.schema.compiled;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprApplicationType;

/**
 * An application of type arguments to a named type.
 */

public final class CBTypeExpressionApplication
  implements CBTypeExprApplicationType
{
  private final CBTypeExprNamedType target;
  private final List<CBTypeExpressionType> arguments;

  /**
   * Construct an expression.
   *
   * @param inTarget    The target type
   * @param inArguments The type arguments
   */

  public CBTypeExpressionApplication(
    final CBTypeExprNamedType inTarget,
    final Collection<? extends CBTypeExpressionType> inArguments)
  {
    this.target =
      Objects.requireNonNull(inTarget, "inTarget");
    this.arguments =
      List.copyOf(Objects.requireNonNull(inArguments, "inArguments"));
  }

  @Override
  public CBTypeExprNamedType target()
  {
    return this.target;
  }

  @Override
  public List<CBTypeExpressionType> arguments()
  {
    return this.arguments;
  }
}
