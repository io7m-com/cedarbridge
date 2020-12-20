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

package com.io7m.cedarbridge.codegen.java.internal.serializer_instantiation;

import com.io7m.cedarbridge.codegen.java.internal.bindings.CBCGJavaNamePool;
import com.io7m.cedarbridge.codegen.java.internal.bindings.CBCGJavaProgramOrder;
import com.io7m.cedarbridge.codegen.java.internal.type_expressions.CBCGJavaTypeExpressions;
import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames;
import com.io7m.cedarbridge.runtime.api.CBQualifiedTypeName;
import com.io7m.cedarbridge.runtime.api.CBSerializerDirectoryType;
import com.io7m.cedarbridge.runtime.api.CBSerializerType;
import com.io7m.cedarbridge.runtime.api.CBTypeArgument;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.jaffirm.core.Postconditions;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.junreachable.UnreachableCodeException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprApplicationType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprParameterType;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PROTECTED;

public final class CBCGSerializerInstantiations
{
  private CBCGSerializerInstantiations()
  {

  }

  public static MethodSpec generateInstantiationMethod(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBRecordType) {
      return generateInstantiationMethodForRecord((CBRecordType) type);
    }
    if (type instanceof CBVariantType) {
      return generateInstantiationMethodForVariant((CBVariantType) type);
    }
    throw new UnreachableCodeException();
  }

  private static MethodSpec generateInstantiationMethodForVariant(
    final CBVariantType variant)
  {
    final var method =
      MethodSpec.methodBuilder("createActual");
    method.addAnnotation(Override.class);
    method.addModifiers(PROTECTED);
    method.addParameter(
      CBSerializerDirectoryType.class,
      "directory",
      FINAL
    );
    method.addParameter(
      ParameterizedTypeName.get(List.class, CBTypeArgument.class),
      "arguments",
      FINAL
    );
    method.returns(
      ParameterizedTypeName.get(
        ClassName.get(CBSerializerType.class),
        CBCGJavaTypeNames.dataTypeNameOf(variant)
      )
    );

    final var ops =
      generateInstantiationStatementsForVariant(
        new CBCGJavaProgramOrder(),
        new CBCGJavaNamePool(),
        variant
      );

    ops.stream().sorted().forEach(op -> method.addCode(op.serialize()));
    return method.build();
  }

  private static MethodSpec generateInstantiationMethodForRecord(
    final CBRecordType record)
  {
    final var method =
      MethodSpec.methodBuilder("createActual");
    method.addAnnotation(Override.class);
    method.addModifiers(PROTECTED);
    method.addParameter(
      CBSerializerDirectoryType.class,
      "directory",
      FINAL
    );
    method.addParameter(
      ParameterizedTypeName.get(List.class, CBTypeArgument.class),
      "arguments",
      FINAL
    );
    method.returns(
      ParameterizedTypeName.get(
        ClassName.get(CBSerializerType.class),
        CBCGJavaTypeNames.dataTypeNameOf(record)
      )
    );

    final var ops =
      generateInstantiationStatementsForRecord(
        new CBCGJavaProgramOrder(),
        new CBCGJavaNamePool(),
        record
      );

    ops.stream().sorted().forEach(op -> method.addCode(op.serialize()));
    return method.build();
  }

  public static List<CBCGSerializerInstantiationOperationType> generateInstantiationStatementsForRecord(
    final CBCGJavaProgramOrder lines,
    final CBCGJavaNamePool names,
    final CBRecordType record)
  {
    final var operations =
      new ArrayList<CBCGSerializerInstantiationOperationType>();
    final var typeArguments =
      new HashMap<CBTypeExpressionType, OpBuildTypeArgumentType>();

    for (final var field : record.fields()) {
      buildTypeArguments(names, lines, typeArguments, field.type());
    }

    typeArguments.values()
      .stream()
      .sorted()
      .forEach(operations::add);

    final var serializerFetches =
      new HashMap<CBTypeExpressionType, OpFetchSerializerType>();

    for (final var field : record.fields()) {
      fetchSerializers(
        names,
        lines,
        typeArguments,
        serializerFetches,
        field.type()
      );
    }

    serializerFetches.values()
      .stream()
      .sorted()
      .forEach(operations::add);

    operations.add(
      generateConstructor(
        lines,
        serializerFetches,
        record
      )
    );

    return operations;
  }

  public static List<CBCGSerializerInstantiationOperationType> generateInstantiationStatementsForVariant(
    final CBCGJavaProgramOrder lines,
    final CBCGJavaNamePool names,
    final CBVariantType variant)
  {
    final var operations =
      new ArrayList<CBCGSerializerInstantiationOperationType>();
    final var typeArguments =
      new HashMap<CBTypeExpressionType, OpBuildTypeArgumentType>();

    for (final var caseV : variant.cases()) {
      for (final var field : caseV.fields()) {
        buildTypeArguments(names, lines, typeArguments, field.type());
      }
    }

    typeArguments.values()
      .stream()
      .sorted()
      .forEach(operations::add);

    final var serializerFetches =
      new HashMap<CBTypeExpressionType, OpFetchSerializerType>();

    for (final var caseV : variant.cases()) {
      for (final var field : caseV.fields()) {
        fetchSerializers(
          names,
          lines,
          typeArguments,
          serializerFetches,
          field.type()
        );
      }
    }

    serializerFetches.values()
      .stream()
      .sorted()
      .forEach(operations::add);

    operations.add(
      generateConstructor(
        lines,
        serializerFetches,
        variant
      )
    );

    return operations;
  }

  private static CBCGSerializerInstantiationOperationType generateConstructor(
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpFetchSerializerType> serializerFetches,
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBRecordType) {
      return generateConstructorForRecord(
        lines, serializerFetches, (CBRecordType) type
      );
    }
    if (type instanceof CBVariantType) {
      return generateConstructorForVariant(
        lines, serializerFetches, (CBVariantType) type
      );
    }
    throw new UnreachableCodeException();
  }

  private static CBCGSerializerInstantiationOperationType generateConstructorForVariant(
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpFetchSerializerType> serializerFetches,
    final CBVariantType variant)
  {
    final var fetches =
      variant.cases()
        .stream()
        .flatMap(c -> c.fields().stream())
        .map(f -> serializerFetches.get(f.type()))
        .collect(Collectors.toList());

    return new OpCallSerializerConstructor(
      lines.next(),
      variant.arity(),
      CBCGJavaTypeNames.serializerClassNameOf(variant),
      fetches
    );
  }

  private static CBCGSerializerInstantiationOperationType generateConstructorForRecord(
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpFetchSerializerType> serializerFetches,
    final CBRecordType record)
  {
    final var fetches =
      record.fields()
        .stream()
        .map(f -> serializerFetches.get(f.type()))
        .collect(Collectors.toList());

    return new OpCallSerializerConstructor(
      lines.next(),
      record.arity(),
      CBCGJavaTypeNames.serializerClassNameOf(record),
      fetches
    );
  }

  private static void fetchSerializers(
    final CBCGJavaNamePool names,
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final Map<CBTypeExpressionType, OpFetchSerializerType> serializerFetches,
    final CBTypeExpressionType type)
  {
    Preconditions.checkPreconditionV(
      !serializerFetches.containsKey(type),
      "Must not have already processed expression %s",
      type
    );

    if (type instanceof CBTypeExprParameterType) {
      fetchSerializerForParameter(
        names,
        lines,
        typeArguments,
        serializerFetches,
        (CBTypeExprParameterType) type);
      checkSerializerFetchPost(serializerFetches, type);
      return;
    }
    if (type instanceof CBTypeExprNamedType) {
      fetchSerializerForNamed(
        names,
        lines,
        typeArguments,
        serializerFetches,
        (CBTypeExprNamedType) type);
      checkSerializerFetchPost(serializerFetches, type);
      return;
    }
    if (type instanceof CBTypeExprApplicationType) {
      fetchSerializerForApplication(
        names,
        lines,
        typeArguments,
        serializerFetches,
        (CBTypeExprApplicationType) type);
      checkSerializerFetchPost(serializerFetches, type);
      return;
    }
    throw new UnreachableCodeException();
  }

  private static void fetchSerializerForApplication(
    final CBCGJavaNamePool names,
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final Map<CBTypeExpressionType, OpFetchSerializerType> serializerFetches,
    final CBTypeExprApplicationType type)
  {
    final var argumentJavaTypeNames =
      type.arguments()
        .stream()
        .map(CBCGJavaTypeExpressions::evaluateTypeExpression)
        .collect(Collectors.toList());

    final var argumentJavaTypeNameArray =
      new TypeName[argumentJavaTypeNames.size()];
    argumentJavaTypeNames.toArray(argumentJavaTypeNameArray);

    final var javaTypeName =
      ParameterizedTypeName.get(
        CBCGJavaTypeNames.dataClassNameOf(type.target().declaration()),
        argumentJavaTypeNameArray
      );

    final var op =
      new OpFetchSerializerForApplication(
        lines.next(),
        names.freshLocalVariable(),
        javaTypeName,
        (OpBuildTypeArgumentForApplication) typeArguments.get(type)
      );

    serializerFetches.put(type, op);
  }

  private static void fetchSerializerForNamed(
    final CBCGJavaNamePool names,
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final Map<CBTypeExpressionType, OpFetchSerializerType> serializerFetches,
    final CBTypeExprNamedType type)
  {
    final var op =
      new OpFetchSerializerForNamedType(
        lines.next(),
        (OpBuildTypeArgumentForNamed) typeArguments.get(type),
        CBCGJavaTypeNames.dataClassNameOf(type.declaration()),
        names.freshLocalVariable()
      );

    serializerFetches.put(type, op);
  }

  private static void fetchSerializerForParameter(
    final CBCGJavaNamePool names,
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final Map<CBTypeExpressionType, OpFetchSerializerType> serializerFetches,
    final CBTypeExprParameterType type)
  {
    final var op =
      new OpFetchSerializerForTypeVariable(
        lines.next(),
        TypeVariableName.get(type.parameter().name()),
        (OpBuildTypeArgumentForTypeVariable) typeArguments.get(type),
        names.freshLocalVariable()
      );

    serializerFetches.put(type, op);
  }

  private static void checkSerializerFetchPost(
    final Map<CBTypeExpressionType, OpFetchSerializerType> serializerFetches,
    final CBTypeExpressionType type)
  {
    Postconditions.checkPostconditionV(
      serializerFetches.containsKey(type),
      "Must have processed expression %s",
      type
    );
  }

  private static void buildTypeArguments(
    final CBCGJavaNamePool names,
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final CBTypeExpressionType type)
  {
    Preconditions.checkPreconditionV(
      !typeArguments.containsKey(type),
      "Must not have already processed expression %s",
      type
    );

    if (type instanceof CBTypeExprParameterType) {
      buildTypeArgumentsParameter(
        names,
        lines,
        typeArguments,
        (CBTypeExprParameterType) type);
      checkBuiltTypeArgumentsPost(typeArguments, type);
      return;
    }
    if (type instanceof CBTypeExprNamedType) {
      buildTypeArgumentsNamed(
        names,
        lines,
        typeArguments,
        (CBTypeExprNamedType) type);
      checkBuiltTypeArgumentsPost(typeArguments, type);
      return;
    }
    if (type instanceof CBTypeExprApplicationType) {
      buildTypeArgumentsApplication(
        names,
        lines,
        typeArguments,
        (CBTypeExprApplicationType) type);
      checkBuiltTypeArgumentsPost(typeArguments, type);
      return;
    }
    throw new UnreachableCodeException();
  }

  private static void checkBuiltTypeArgumentsPost(
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final CBTypeExpressionType type)
  {
    Postconditions.checkPostconditionV(
      typeArguments.containsKey(type),
      "Must have processed expression %s",
      type
    );
  }

  private static void buildTypeArgumentsApplication(
    final CBCGJavaNamePool names,
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final CBTypeExprApplicationType type)
  {
    final var arguments = type.arguments();
    for (final var argument : arguments) {
      buildTypeArguments(names, lines, typeArguments, argument);
    }

    final var targetType =
      type.target();
    final var targetDecl =
      targetType.declaration();
    final var qName =
      CBQualifiedTypeName.of(targetDecl.owner().name(), targetDecl.name());

    final var builtArguments =
      arguments.stream()
        .map(typeArguments::get)
        .collect(Collectors.toList());

    Preconditions.checkPreconditionV(
      targetDecl.arity() == builtArguments.size(),
      "Target declaration arity %d must match argument count %d",
      Integer.valueOf(targetDecl.arity()),
      Integer.valueOf(builtArguments.size())
    );

    final var op =
      new OpBuildTypeArgumentForApplication(
        lines.next(),
        qName,
        names.freshLocalVariable(),
        builtArguments
      );

    typeArguments.put(type, op);
  }

  private static void buildTypeArgumentsNamed(
    final CBCGJavaNamePool names,
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final CBTypeExprNamedType type)
  {
    final var typeDeclaration =
      type.declaration();
    final var qName =
      CBQualifiedTypeName.of(
        typeDeclaration.owner().name(),
        typeDeclaration.name());

    final var op =
      new OpBuildTypeArgumentForNamed(
        lines.next(),
        qName,
        names.freshLocalVariable()
      );

    typeArguments.put(type, op);
  }

  private static void buildTypeArgumentsParameter(
    final CBCGJavaNamePool names,
    final CBCGJavaProgramOrder lines,
    final Map<CBTypeExpressionType, OpBuildTypeArgumentType> typeArguments,
    final CBTypeExprParameterType type)
  {
    final var op =
      new OpBuildTypeArgumentForTypeVariable(
        lines.next(),
        type.parameter(),
        names.freshLocalVariable()
      );

    typeArguments.put(type, op);
  }
}