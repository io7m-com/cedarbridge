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

package com.io7m.cedarbridge.codegen.java.internal.serializer_instantiation;

import com.io7m.cedarbridge.runtime.api.CBQualifiedTypeName;
import com.io7m.cedarbridge.runtime.api.CBTypeArgument;
import com.io7m.cedarbridge.runtime.api.CBTypeArguments;
import com.squareup.javapoet.CodeBlock;

import java.util.List;
import java.util.Objects;

/**
 * An operation that builds a type argument for the given named type.
 */

public final class OpBuildTypeArgumentForNamed
  extends OpAbstract implements OpBuildTypeArgumentType
{
  private final CBQualifiedTypeName typeName;
  private final String localName;

  /**
   * An operation that builds a type argument for the given named type.
   *
   * @param order       The program order
   * @param inTypeName  The target type name
   * @param inLocalName The local variable name
   */

  public OpBuildTypeArgumentForNamed(
    final int order,
    final CBQualifiedTypeName inTypeName,
    final String inLocalName)
  {
    super(order);
    this.typeName =
      Objects.requireNonNull(inTypeName, "typeName");
    this.localName =
      Objects.requireNonNull(inLocalName, "localName");
  }

  @Override
  public String javaLocalName()
  {
    return this.localName;
  }

  @Override
  public CodeBlock serialize()
  {
    final var packName = this.typeName.packageName();
    final var tyName = this.typeName.typeName();
    return CodeBlock.builder()
      .addStatement(
        "// Build type argument for named type $L:$L", packName, tyName)
      .addStatement(
        "final $T $L = $T.of(new $T($S,$S),$T.of())",
        CBTypeArgument.class,
        this.javaLocalName(),
        CBTypeArguments.class,
        CBQualifiedTypeName.class,
        packName,
        tyName,
        List.class
      ).build();
  }

  /**
   * @return The target type name
   */

  public CBQualifiedTypeName typeName()
  {
    return this.typeName;
  }
}
