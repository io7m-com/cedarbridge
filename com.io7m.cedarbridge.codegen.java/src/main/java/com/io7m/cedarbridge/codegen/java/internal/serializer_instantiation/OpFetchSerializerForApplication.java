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
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.ParameterizedTypeName;

import java.util.Objects;

/**
 * An operation that fetches a serializer for the given type application.
 */

public final class OpFetchSerializerForApplication
  extends OpAbstract implements OpFetchSerializerType
{
  private final String localName;
  private final ParameterizedTypeName javaTypeName;
  private final OpBuildTypeArgumentForApplication typeArgument;

  /**
   * An operation that fetches a serializer for the given type application.
   *
   * @param order          The program order
   * @param inJavaTypeName The target Java type
   * @param inTypeArgument The operation that builds a type argument
   * @param inLocalName    The local variable name
   */

  public OpFetchSerializerForApplication(
    final int order,
    final String inLocalName,
    final ParameterizedTypeName inJavaTypeName,
    final OpBuildTypeArgumentForApplication inTypeArgument)
  {
    super(order);
    this.localName =
      Objects.requireNonNull(inLocalName, "localName");
    this.javaTypeName =
      Objects.requireNonNull(inJavaTypeName, "javaTypeName");
    this.typeArgument =
      Objects.requireNonNull(inTypeArgument, "typeArgument");
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
        "// Fetch serializer for type application")
      .addStatement(
        "final $T<$T> $L = directory.serializerFor($L.target(),$L.arguments())",
        CBSerializerType.class,
        this.javaTypeName,
        this.localName,
        this.typeArgument.javaLocalName(),
        this.typeArgument.javaLocalName()
      ).build();
  }
}
