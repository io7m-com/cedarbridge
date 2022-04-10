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
import com.squareup.javapoet.TypeVariableName;

import java.util.Objects;

/**
 * An operation that fetches a serializer for the given type variable.
 */

public final class OpFetchSerializerForTypeVariable
  extends OpAbstract implements OpFetchSerializerType
{
  private final TypeVariableName javaTypeVariableName;
  private final OpBuildTypeArgumentForTypeVariable runtimeTypeFetch;
  private final String localName;

  /**
   * An operation that fetches a serializer for the given type variable.
   *
   * @param order                  The program order
   * @param inJavaTypeVariableName The type variable
   * @param inRuntimeTypeFetch     The operation that builds a type variable
   * @param inLocalName            The local variable name
   */

  public OpFetchSerializerForTypeVariable(
    final int order,
    final TypeVariableName inJavaTypeVariableName,
    final OpBuildTypeArgumentForTypeVariable inRuntimeTypeFetch,
    final String inLocalName)
  {
    super(order);
    this.javaTypeVariableName =
      Objects.requireNonNull(inJavaTypeVariableName, "javaTypeVariableName");
    this.runtimeTypeFetch =
      Objects.requireNonNull(inRuntimeTypeFetch, "runtimeTypeFetch");
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
        "// Fetch serializer for type variable $L",
        this.javaTypeVariableName.name)
      .addStatement(
        "final $T<$T> $L = directory.serializerFor($L.target(),$L.arguments())",
        CBSerializerType.class,
        this.javaTypeVariableName,
        this.javaLocalName(),
        this.runtimeTypeFetch.javaLocalName(),
        this.runtimeTypeFetch.javaLocalName()
      ).build();
  }
}
