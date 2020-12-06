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

package com.io7m.cedarbridge.codegen.java.internal.type_expressions;

import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.schema.compiled.CBExternalType;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import com.io7m.cedarbridge.schema.compiled.CBTypeParameterType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.junreachable.UnreachableCodeException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprApplicationType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprParameterType;

public final class CBCGJavaTypeExpressions
{
  private CBCGJavaTypeExpressions()
  {

  }

  public static TypeName evaluateTypeExpression(
    final CBTypeExpressionType type)
  {
    Objects.requireNonNull(type, "type");

    if (type instanceof CBTypeExprNamedType) {
      return evaluateTypeExpressionNamed((CBTypeExprNamedType) type);
    }
    if (type instanceof CBTypeExprParameterType) {
      return evaluateTypeExpressionParameter((CBTypeExprParameterType) type);
    }
    if (type instanceof CBTypeExprApplicationType) {
      return evaluateTypeExpressionApplication((CBTypeExprApplicationType) type);
    }
    throw new UnreachableCodeException();
  }

  private static TypeName evaluateTypeExpressionApplication(
    final CBTypeExprApplicationType type)
  {
    final var arguments =
      type.arguments()
        .stream()
        .map(CBCGJavaTypeExpressions::evaluateTypeExpression)
        .collect(Collectors.toCollection(LinkedList::new));

    final var argumentArray = new TypeName[arguments.size()];
    arguments.toArray(argumentArray);

    final var target =
      evaluateTypeExpressionNamed(type.target());

    if (target instanceof ClassName) {
      final var name0 = ((ClassName) target);
      return ParameterizedTypeName.get(name0, argumentArray);
    }

    throw new IllegalStateException(
      "First expression of type application is not a class");
  }

  private static TypeName evaluateTypeExpressionParameter(
    final CBTypeExprParameterType type)
  {
    return TypeVariableName.get(type.parameter().name());
  }

  public static TypeName evaluateTypeExpressionNamed(
    final CBTypeExprNamedType type)
  {
    final var decl = type.declaration();
    if (decl instanceof CBRecordType) {
      return CBCGJavaTypeNames.dataClassNameOf(decl);
    }
    if (decl instanceof CBVariantType) {
      return CBCGJavaTypeNames.dataClassNameOf(decl);
    }
    if (decl instanceof CBExternalType) {
      return CBCGJavaTypeNames.externalNameOf((CBExternalType) decl);
    }
    throw new UnreachableCodeException();
  }

  public static List<TypeVariableName> createTypeVariables(
    final List<CBTypeParameterType> typeParameters)
  {
    return typeParameters
      .stream()
      .map(p -> TypeVariableName.get(p.name(), CBSerializableType.class))
      .collect(Collectors.toList());
  }
}
