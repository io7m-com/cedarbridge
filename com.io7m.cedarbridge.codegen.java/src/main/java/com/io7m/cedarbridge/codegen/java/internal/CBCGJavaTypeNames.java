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

package com.io7m.cedarbridge.codegen.java.internal;

import com.io7m.cedarbridge.schema.compiled.CBExternalType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBVariantCaseType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.stream.Collectors;

public final class CBCGJavaTypeNames
{
  private CBCGJavaTypeNames()
  {

  }

  public static ClassName protocolClassNameOf(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBExternalType) {
      return ClassName.get(
        ((CBExternalType) type).externalPackage(),
        ((CBExternalType) type).externalType()
      );
    }
    return ClassName.get(type.owner().name(), type.name());
  }

  public static ClassName dataClassNameOf(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBExternalType) {
      return ClassName.get(
        ((CBExternalType) type).externalPackage(),
        ((CBExternalType) type).externalType()
      );
    }
    return ClassName.get(type.owner().name(), type.name());
  }

  public static TypeName dataTypeNameOf(
    final CBTypeDeclarationType type)
  {
    final var typeParameters =
      type.parameters();
    final var className =
      ClassName.get(type.owner().name(), type.name());

    if (typeParameters.isEmpty()) {
      return className;
    }

    final var typeNames =
      typeParameters.stream()
        .map(p -> TypeVariableName.get(p.name()))
        .collect(Collectors.toList());

    final var typeArray = new TypeName[typeParameters.size()];
    typeNames.toArray(typeArray);
    return ParameterizedTypeName.get(className, typeArray);
  }

  public static TypeName dataTypeNameOfCase(
    final CBVariantCaseType caseV)
  {
    final var type =
      caseV.owner();
    final var typeParameters =
      type.parameters();
    final var className =
      ClassName.get(
        type.owner().name(),
        type.name(),
        caseV.name()
      );

    if (typeParameters.isEmpty()) {
      return className;
    }

    final var typeNames =
      typeParameters.stream()
        .map(p -> TypeVariableName.get(p.name()))
        .collect(Collectors.toList());

    final var typeArray = new TypeName[typeParameters.size()];
    typeNames.toArray(typeArray);
    return ParameterizedTypeName.get(className, typeArray);
  }


  public static ClassName dataClassNameOfCase(
    final CBVariantCaseType caseV)
  {
    final var type =
      caseV.owner();
    final var typeParameters =
      type.parameters();
    return ClassName.get(
        type.owner().name(),
        type.name(),
        caseV.name()
      );
  }

  public static ClassName serializerClassNameOf(
    final CBTypeDeclarationType type)
  {
    return ClassName.get(
      type.owner().name(),
      String.format("%sSerializer", type.name())
    );
  }

  public static ClassName serializerFactoryClassNameOf(
    final CBTypeDeclarationType type)
  {
    return ClassName.get(
      type.owner().name(),
      String.format("%sSerializerFactory", type.name())
    );
  }

  public static TypeName externalNameOf(
    final CBExternalType decl)
  {
    return ClassName.get(decl.externalPackage(), decl.externalType());
  }
}
