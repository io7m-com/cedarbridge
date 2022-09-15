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

package com.io7m.cedarbridge.codegen.java.internal.serializers;

import com.io7m.jodist.CodeBlock;

import java.util.Objects;

/**
 * An operation that calls a deserialize method.
 */

public final class OpCallDeserialize extends OpAbstract implements OpCallType
{
  private final String localName;
  private final String fieldName;

  /**
   * An operation that calls a deserialize method.
   *
   * @param inOrder     The program order
   * @param inLocalName The local variable name
   * @param inFieldName The field name
   */

  public OpCallDeserialize(
    final int inOrder,
    final String inLocalName,
    final String inFieldName)
  {
    super(inOrder);
    this.localName =
      Objects.requireNonNull(inLocalName, "localName");
    this.fieldName =
      Objects.requireNonNull(inFieldName, "fieldName");
  }

  @Override
  public CodeBlock serialize()
  {
    return CodeBlock.builder()
      .addStatement("context.begin($S)", this.fieldName)
      .addStatement(
        "final var $L = this.$L.deserialize(context)",
        this.localName,
        this.fieldName)
      .addStatement("context.end($S)", this.fieldName)
      .build();
  }

  @Override
  public String javaLocalName()
  {
    return this.localName;
  }
}
