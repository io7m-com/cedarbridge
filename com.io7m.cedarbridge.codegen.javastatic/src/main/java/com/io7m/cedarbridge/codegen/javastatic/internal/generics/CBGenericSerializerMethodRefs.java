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


package com.io7m.cedarbridge.codegen.javastatic.internal.generics;

import com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaNamePool;
import com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeExpressions;
import com.io7m.cedarbridge.runtime.api.CBDeserializeType;
import com.io7m.cedarbridge.runtime.api.CBSerializeType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.jodist.ClassName;
import com.io7m.jodist.ParameterizedTypeName;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Objects;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprApplicationType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprParameterType;

/**
 * Functions to calculate or generate serializer method references.
 */

public final class CBGenericSerializerMethodRefs
{
  private final CBCGJavaNamePool names;
  private final CBTypeExpressionType expr;
  private final CBGenericSerializerMethodDirection direction;

  /**
   * Create a method reference generator.
   *
   * @param inNames     The local name pool
   * @param inExpr      The root type expression
   * @param inDirection The serialization method direction
   */

  public CBGenericSerializerMethodRefs(
    final CBCGJavaNamePool inNames,
    final CBTypeExpressionType inExpr,
    final CBGenericSerializerMethodDirection inDirection)
  {
    this.names =
      Objects.requireNonNull(inNames, "names");
    this.expr =
      Objects.requireNonNull(inExpr, "expr");
    this.direction =
      Objects.requireNonNull(inDirection, "direction");
  }

  /**
   * @return A method reference for the root expression
   */

  public CBGenericSerializerMethodRefType build()
  {
    return this.buildStep(this.expr);
  }

  private CBGenericSerializerMethodRefType buildStep(
    final CBTypeExpressionType exprNow)
  {
    if (exprNow instanceof CBTypeExprParameterType param) {
      return new CBGenericSerializerMethodRefParameter(param);
    }

    if (exprNow instanceof CBTypeExprNamedType named) {
      return new CBGenericSerializerMethodRefNamed(named);
    }

    if (exprNow instanceof CBTypeExprApplicationType app) {
      final var lambdaReturnType =
        CBCGJavaTypeExpressions.evaluateTypeExpression(exprNow);

      final var lambdaType =
        switch (this.direction) {
          case SERIALIZE -> {
            yield ParameterizedTypeName.get(
              ClassName.get(CBSerializeType.class),
              lambdaReturnType
            );
          }
          case DESERIALIZE -> {
            yield ParameterizedTypeName.get(
              ClassName.get(CBDeserializeType.class),
              lambdaReturnType
            );
          }
        };

      final var lambdaName =
        this.names.freshLocalVariable();
      final var lambdaContextName =
        this.names.freshLocalVariable();
      final var lambdaValueName =
        this.names.freshLocalVariable();

      final var targets = new ArrayList<CBGenericSerializerMethodRefType>();
      for (final var subExpr : app.arguments()) {
        targets.add(this.buildStep(subExpr));
      }

      return new CBGenericSerializerMethodRefViaLambda(
        app,
        lambdaType,
        lambdaReturnType,
        lambdaName,
        lambdaContextName,
        lambdaValueName,
        app.target(),
        targets
      );
    }

    throw new UnreachableCodeException();
  }

  /**
   * Find all lambda expressions inside the given method reference. The lambdas
   * are returned in declaration order.
   *
   * @param ref The reference
   *
   * @return The stack of lambda expressions
   */

  public static Deque<CBGenericSerializerMethodRefViaLambda> findLambdasInDeclarationOrder(
    final CBGenericSerializerMethodRefType ref)
  {
    final var lambdaRefs = new ArrayDeque<CBGenericSerializerMethodRefViaLambda>();
    findLambdasInDeclarationOrder(ref, lambdaRefs);
    return lambdaRefs;
  }

  private static void findLambdasInDeclarationOrder(
    final CBGenericSerializerMethodRefType ref,
    final Deque<CBGenericSerializerMethodRefViaLambda> stack)
  {
    if (ref instanceof CBGenericSerializerMethodRefNamed) {
      return;
    }

    if (ref instanceof CBGenericSerializerMethodRefParameter) {
      return;
    }

    if (ref instanceof CBGenericSerializerMethodRefViaLambda lambda) {
      stack.push(lambda);
      for (final var subRef : lambda.lambdaTargetRefs()) {
        findLambdasInDeclarationOrder(subRef, stack);
      }
    }
  }
}
