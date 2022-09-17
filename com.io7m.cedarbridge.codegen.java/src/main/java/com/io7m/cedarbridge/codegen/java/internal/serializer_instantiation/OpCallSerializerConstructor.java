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

import com.io7m.jodist.ClassName;
import com.io7m.jodist.CodeBlock;

import java.util.List;
import java.util.Objects;

/**
 * An operation that calls a serializer constructor.
 */

public final class OpCallSerializerConstructor extends OpAbstract
{
  private final int typeArity;
  private final ClassName serializerClassName;
  private final List<OpFetchSerializerType> fetches;

  /**
   * An operation that calls a serializer constructor.
   *
   * @param inOrder               The program order
   * @param inTypeArity           The type arity
   * @param inSerializerClassName The serializer class name
   * @param inFetches             The list of arguments to the constructor
   */

  public OpCallSerializerConstructor(
    final int inOrder,
    final int inTypeArity,
    final ClassName inSerializerClassName,
    final List<OpFetchSerializerType> inFetches)
  {
    super(inOrder);
    this.typeArity = inTypeArity;
    this.serializerClassName =
      Objects.requireNonNull(inSerializerClassName, "serializerClassName");
    this.fetches =
      Objects.requireNonNull(inFetches, "fetches");
  }

  @Override
  public CodeBlock serialize()
  {
    final var expressions =
      this.fetches.stream()
        .map(f -> CodeBlock.of(f.javaLocalName()))
        .collect(CodeBlock.joining(","));

    if (this.typeArity > 0) {
      return CodeBlock.builder()
        .addStatement(
          "return new $T<>($L)",
          this.serializerClassName, expressions)
        .build();
    }
    return CodeBlock.builder()
      .addStatement(
        "return new $T($L)",
        this.serializerClassName, expressions)
      .build();
  }
}
