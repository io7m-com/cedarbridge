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

import com.io7m.cedarbridge.runtime.api.CBSerializerType;
import com.io7m.jodist.ClassName;
import com.io7m.jodist.CodeBlock;

import java.util.Objects;

/**
 * An operation that fetches a serializer for the given type.
 */

public final class OpFetchSerializerForNamedType
  extends OpAbstract implements OpFetchSerializerType
{
  private final OpBuildTypeArgumentForNamed buildTypeArgument;
  private final ClassName javaClassName;
  private final String localName;

  /**
   * An operation that fetches a serializer for the given type.
   *
   * @param order                 The program order
   * @param inJavaTargetClassName The target Java class
   * @param inBuildTypeArgument   The operation that builds a type argument
   * @param inLocalName           The local variable name
   */

  public OpFetchSerializerForNamedType(
    final int order,
    final OpBuildTypeArgumentForNamed inBuildTypeArgument,
    final ClassName inJavaTargetClassName,
    final String inLocalName)
  {
    super(order);
    this.buildTypeArgument =
      Objects.requireNonNull(inBuildTypeArgument, "buildTypeArgument");
    this.javaClassName =
      Objects.requireNonNull(inJavaTargetClassName, "javaClassName");
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
    return CodeBlock.builder()
      .addStatement(
        "// Fetch serializer for named type $L:$L",
        this.buildTypeArgument.typeName().packageName(),
        this.buildTypeArgument.typeName().typeName())
      .addStatement(
        "final $T<$T> $L = directory.serializerFor($L.target(),$L.arguments())",
        CBSerializerType.class,
        this.javaClassName,
        this.javaLocalName(),
        this.buildTypeArgument.javaLocalName(),
        this.buildTypeArgument.javaLocalName()
      ).build();
  }
}
