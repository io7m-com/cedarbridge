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

package com.io7m.cedarbridge.codegen.java.internal.data_classes;

import com.io7m.cedarbridge.codegen.java.internal.type_expressions.CBCGJavaTypeExpressions;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class CBCGDataClassMethodGeneration
{
  private CBCGDataClassMethodGeneration()
  {

  }

  public static MethodSpec createEqualsMethod(
    final ClassName className,
    final List<FieldSpec> fields)
  {
    final var methodBuilder =
      MethodSpec.methodBuilder("equals");
    methodBuilder.addAnnotation(Override.class);
    methodBuilder.addModifiers(PUBLIC);
    methodBuilder.returns(TypeName.BOOLEAN);

    methodBuilder.addParameter(Object.class, "other", FINAL);
    methodBuilder.addStatement("if (this == other) return true");
    methodBuilder.addStatement("if (other == null) return false");
    methodBuilder.addStatement(
      "if (!$T.equals(this.getClass(), other.getClass())) return false",
      Objects.class
    );

    if (!fields.isEmpty()) {
      methodBuilder.addStatement(
        "final var otherT = ($L) other",
        className.simpleName());
      for (final var field : fields) {
        methodBuilder.addStatement(
          "if (!$T.equals(this.$N(), otherT.$N())) return false",
          Objects.class,
          field.name,
          field.name
        );
      }
    }

    methodBuilder.addStatement("return true");
    return methodBuilder.build();
  }

  public static MethodSpec createHashCodeMethod(
    final List<FieldSpec> fields)
  {
    final var methodBuilder =
      MethodSpec.methodBuilder("hashCode");
    methodBuilder.addAnnotation(Override.class);
    methodBuilder.addModifiers(PUBLIC);
    methodBuilder.returns(TypeName.INT);

    methodBuilder.addStatement(
      "return $T.hash($L)",
      Objects.class,
      fields.stream()
        .map(f -> String.format("this.%s()", f.name))
        .collect(Collectors.joining(","))
    );
    return methodBuilder.build();
  }

  public static MethodSpec createFieldAccessorMethod(
    final CBFieldType field)
  {
    final var methodBuilder = MethodSpec.methodBuilder(field.name());
    methodBuilder.addModifiers(PUBLIC);
    methodBuilder.returns(CBCGJavaTypeExpressions.evaluateTypeExpression(field.type()));
    methodBuilder.addStatement("return this.$L", field.name());
    return methodBuilder.build();
  }

  public static MethodSpec createFieldSetConstructor(
    final List<CBFieldType> fields)
  {
    final var methodBuilder = MethodSpec.constructorBuilder();
    methodBuilder.addModifiers(PUBLIC);

    final var parameters =
      fields.stream()
        .map(CBCGDataClassMethodGeneration::createFieldSetConstructorParameter)
        .collect(Collectors.toList());

    methodBuilder.addParameters(parameters);

    for (final var field : fields) {
      final var fieldType =
        CBCGJavaTypeExpressions.evaluateTypeExpression(field.type());

      if (fieldType.isPrimitive()) {
        methodBuilder.addStatement(
          "this.$L = p$L", field.name(), field.name()
        );
      } else {
        methodBuilder.addStatement(
          "this.$L = $T.requireNonNull(p$L,$S)",
          field.name(),
          Objects.class,
          field.name(),
          field.name()
        );
      }
    }

    return methodBuilder.build();
  }

  public static ParameterSpec createFieldSetConstructorParameter(
    final CBFieldType field)
  {
    final var type =
      CBCGJavaTypeExpressions.evaluateTypeExpression(field.type());
    return ParameterSpec.builder(
      type,
      String.format("p%s", field.name()),
      FINAL
    ).build();
  }
}
