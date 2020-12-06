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

package com.io7m.cedarbridge.codegen.java.internal.serializers;

import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaClassGeneratorType;
import com.io7m.cedarbridge.codegen.java.internal.bindings.CBCGJavaNamePool;
import com.io7m.cedarbridge.codegen.java.internal.bindings.CBCGJavaProgramOrder;
import com.io7m.cedarbridge.codegen.java.internal.type_expressions.CBCGJavaTypeExpressions;
import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializerType;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBVariantCaseType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.junreachable.UnreachableCodeException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames.dataTypeNameOf;
import static com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames.dataTypeNameOfCase;
import static com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames.serializerClassNameOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class CBCGJavaSerializerGenerator
  implements CBCGJavaClassGeneratorType
{
  public CBCGJavaSerializerGenerator()
  {

  }

  @Override
  public Path execute(
    final CBSPICodeGeneratorConfiguration configuration,
    final CBTypeDeclarationType type)
    throws CBSPICodeGeneratorException
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(type, "type");

    final var pack = type.owner();

    final var dataTypeName =
      dataTypeNameOf(type);
    final var serializerClassName =
      serializerClassNameOf(type);

    final var superName =
      ClassName.get(CBSerializerType.class);
    final var parameterizedTypeName =
      ParameterizedTypeName.get(superName, dataTypeName);
    final var constructor =
      createConstructor(type);
    final var fields =
      createFields(type);
    final var deserializeMethods =
      createDeserializeMethods(type);
    final var serializeMethods =
      createSerializeMethods(type);

    final var classBuilder = TypeSpec.classBuilder(serializerClassName);
    classBuilder.addModifiers(FINAL, PUBLIC);
    classBuilder.addSuperinterface(parameterizedTypeName);
    classBuilder.addTypeVariables(
      CBCGJavaTypeExpressions.createTypeVariables(type.parameters())
    );
    classBuilder.addMethod(constructor);
    classBuilder.addFields(fields);
    classBuilder.addMethod(createDeserializeMethod(type));
    classBuilder.addMethod(createSerializeMethod(type));
    classBuilder.addMethods(deserializeMethods);
    classBuilder.addMethods(serializeMethods);

    final var classDefinition = classBuilder.build();
    final var javaFile =
      JavaFile.builder(pack.name(), classDefinition)
        .build();

    try {
      return javaFile.writeToPath(configuration.outputDirectory(), UTF_8);
    } catch (final IOException e) {
      throw new CBSPICodeGeneratorException(e);
    }
  }

  private static List<MethodSpec> createSerializeMethods(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBRecordType) {
      return createSerializeMethodsRecord((CBRecordType) type);
    }
    if (type instanceof CBVariantType) {
      return createSerializeMethodsVariant((CBVariantType) type);
    }
    throw new UnreachableCodeException();
  }

  private static List<MethodSpec> createSerializeMethodsVariant(
    final CBVariantType type)
  {
    return type.cases()
      .stream()
      .map(CBCGJavaSerializerGenerator::createSerializeMethodVariantCase)
      .collect(Collectors.toList());
  }

  private static List<MethodSpec> createSerializeMethodsRecord(
    final CBRecordType type)
  {
    return List.of();
  }

  private static MethodSpec createSerializeMethodVariantCase(
    final CBVariantCaseType caseV)
  {
    final var method =
      MethodSpec.methodBuilder(String.format("serialize%s", caseV.name()));
    method.addModifiers(PRIVATE);
    method.addParameter(CBSerializationContextType.class, "context", FINAL);
    method.addParameter(dataTypeNameOfCase(caseV), "value", FINAL);

    final var calls =
      createFieldSerializerCalls(
        new CBCGJavaProgramOrder(),
        caseV.fields()
      );

    calls.forEach(op -> method.addCode(op.serialize()));
    return method.build();
  }

  private static List<MethodSpec> createDeserializeMethods(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBRecordType) {
      return createDeserializeMethodsRecord((CBRecordType) type);
    }
    if (type instanceof CBVariantType) {
      return createDeserializeMethodsVariant((CBVariantType) type);
    }
    throw new UnreachableCodeException();
  }

  private static List<MethodSpec> createDeserializeMethodsVariant(
    final CBVariantType type)
  {
    return type.cases()
      .stream()
      .map(CBCGJavaSerializerGenerator::createDeserializeMethodVariantCase)
      .collect(Collectors.toList());
  }

  private static MethodSpec createDeserializeMethodVariantCase(
    final CBVariantCaseType caseV)
  {
    final var method =
      MethodSpec.methodBuilder(String.format("deserialize%s", caseV.name()));
    method.addModifiers(PRIVATE);
    method.addParameter(CBSerializationContextType.class, "context", FINAL);
    method.returns(dataTypeNameOfCase(caseV));

    final var calls =
      createFieldDeserializerCalls(
        new CBCGJavaProgramOrder(),
        new CBCGJavaNamePool(),
        caseV.fields()
      );

    final var variables =
      calls.stream()
        .filter(op -> op instanceof OpCallType)
        .map(OpCallType.class::cast)
        .map(OpCallType::javaLocalName)
        .map(n -> CodeBlock.of("$L", n))
        .collect(CodeBlock.joining(","));

    final var constructorCall =
      CodeBlock.builder()
        .addStatement("return new $T($L)", dataTypeNameOfCase(caseV), variables)
        .build();

    calls.forEach(op -> method.addCode(op.serialize()));
    method.addCode(constructorCall);
    return method.build();
  }

  private static List<MethodSpec> createDeserializeMethodsRecord(
    final CBRecordType type)
  {
    return List.of();
  }

  private static MethodSpec createDeserializeMethod(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBRecordType) {
      return createDeserializeMethodRecord((CBRecordType) type);
    }
    if (type instanceof CBVariantType) {
      return createDeserializeMethodVariant((CBVariantType) type);
    }
    throw new UnreachableCodeException();
  }

  private static MethodSpec createDeserializeMethodVariant(
    final CBVariantType type)
  {
    final var method = MethodSpec.methodBuilder("deserialize");
    method.addAnnotation(Override.class);
    method.addModifiers(PUBLIC);
    method.addParameter(CBSerializationContextType.class, "context", FINAL);
    method.returns(dataTypeNameOf(type));

    method.addStatement(
      "final var variantIndex = context.readVariantIndex()"
    );

    final var switchStatement =
      method.beginControlFlow("switch (variantIndex)");

    for (final var caseV : type.cases()) {
      final var variantClass =
        ClassName.get(
          type.owner().name(),
          type.name(),
          caseV.name()
        );

      final var switchCase =
        switchStatement.beginControlFlow(
          "case $T.VARIANT_INDEX:",
          variantClass);

      switchCase.addStatement("return deserialize$L(context)", caseV.name());
      switchCase.endControlFlow();
    }

    final var defaultCase =
      switchStatement.beginControlFlow("default: ");

    defaultCase.addStatement(
      "throw new $T($T.format($S,variantIndex))",
      IllegalArgumentException.class,
      String.class,
      "Unrecognized variant case index: %d"
    );
    defaultCase.endControlFlow();

    switchStatement.endControlFlow();
    return method.build();
  }

  private static MethodSpec createDeserializeMethodRecord(
    final CBRecordType type)
  {
    final var method = MethodSpec.methodBuilder("deserialize");
    method.addAnnotation(Override.class);
    method.addModifiers(PUBLIC);
    method.addParameter(CBSerializationContextType.class, "context", FINAL);
    method.returns(dataTypeNameOf(type));

    final var calls =
      createFieldDeserializerCalls(
        new CBCGJavaProgramOrder(),
        new CBCGJavaNamePool(),
        type.fields()
      );

    final var variables =
      calls.stream()
        .filter(op -> op instanceof OpCallType)
        .map(OpCallType.class::cast)
        .map(OpCallType::javaLocalName)
        .map(n -> CodeBlock.of("$L", n))
        .collect(CodeBlock.joining(","));

    final var constructorCall =
      CodeBlock.builder()
        .addStatement("return new $T($L)", dataTypeNameOf(type), variables)
        .build();

    calls.forEach(op -> method.addCode(op.serialize()));
    method.addCode(constructorCall);
    return method.build();
  }

  private static MethodSpec createSerializeMethod(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBRecordType) {
      return createSerializeMethodRecord((CBRecordType) type);
    }
    if (type instanceof CBVariantType) {
      return createSerializeMethodVariant((CBVariantType) type);
    }
    throw new UnreachableCodeException();
  }

  private static MethodSpec createSerializeMethodVariant(
    final CBVariantType type)
  {
    final var method = MethodSpec.methodBuilder("serialize");
    method.addAnnotation(Override.class);
    method.addModifiers(PUBLIC);
    method.addParameter(CBSerializationContextType.class, "context", FINAL);
    method.addParameter(dataTypeNameOf(type), "value", FINAL);

    for (final var caseV : type.cases()) {
      final var conditional =
        method.beginControlFlow(
          "if (value instanceof $T)",
          CBCGJavaTypeNames.dataClassNameOfCase(caseV)
        );

      conditional.addStatement(
        "serialize$L(context, ($T) value)",
        caseV.name(),
        dataTypeNameOfCase(caseV)
      );
      conditional.addStatement("return");
      conditional.endControlFlow();
    }

    method.addStatement(
      "throw new $T($T.format($S,value.getClass()))",
      IllegalArgumentException.class,
      String.class,
      "Unrecognized variant case class: %s"
    );
    return method.build();
  }

  private static MethodSpec createSerializeMethodRecord(
    final CBRecordType type)
  {
    final var method = MethodSpec.methodBuilder("serialize");
    method.addAnnotation(Override.class);
    method.addModifiers(PUBLIC);
    method.addParameter(CBSerializationContextType.class, "context", FINAL);
    method.addParameter(dataTypeNameOf(type), "value", FINAL);

    final var calls =
      createFieldSerializerCalls(
        new CBCGJavaProgramOrder(),
        type.fields()
      );

    calls.forEach(op -> method.addCode(op.serialize()));
    return method.build();
  }

  private static List<CBCGSerializerCallOperationType> createFieldSerializerCalls(
    final CBCGJavaProgramOrder lines,
    final List<CBFieldType> fields)
  {
    return fields.stream()
      .map(f -> createFieldSerializerCall(lines, f))
      .collect(Collectors.toList());
  }

  private static CBCGSerializerCallOperationType createFieldSerializerCall(
    final CBCGJavaProgramOrder lines,
    final CBFieldType field)
  {
    return new OpCallSerialize(
      lines.next(),
      field.name()
    );
  }

  private static List<FieldSpec> createFields(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBRecordType) {
      return createFieldsRecord((CBRecordType) type);
    }
    if (type instanceof CBVariantType) {
      return createFieldsVariant((CBVariantType) type);
    }
    throw new UnreachableCodeException();
  }

  private static List<FieldSpec> createFieldsVariant(
    final CBVariantType type)
  {
    return type.cases()
      .stream()
      .flatMap(c -> c.fields().stream())
      .map(CBCGJavaSerializerGenerator::createField)
      .collect(Collectors.toList());
  }

  private static List<FieldSpec> createFieldsRecord(
    final CBRecordType type)
  {
    return type.fields()
      .stream()
      .map(CBCGJavaSerializerGenerator::createField)
      .collect(Collectors.toList());
  }

  private static FieldSpec createField(
    final CBFieldType field)
  {
    final var type =
      CBCGJavaTypeExpressions.evaluateTypeExpression(field.type());
    final var fieldType =
      ParameterizedTypeName.get(ClassName.get(CBSerializerType.class), type);
    return FieldSpec.builder(fieldType, field.name(), FINAL, PRIVATE)
      .build();
  }

  private static MethodSpec createConstructor(
    final CBTypeDeclarationType type)
  {
    if (type instanceof CBRecordType) {
      return createConstructorRecord((CBRecordType) type);
    }
    if (type instanceof CBVariantType) {
      return createConstructorVariant((CBVariantType) type);
    }
    throw new UnreachableCodeException();
  }

  private static List<CBCGSerializerCallOperationType> createFieldDeserializerCalls(
    final CBCGJavaProgramOrder lines,
    final CBCGJavaNamePool names,
    final List<CBFieldType> fields)
  {
    return fields.stream()
      .map(f -> createFieldDeserializerCall(lines, names, f))
      .collect(Collectors.toList());
  }

  private static CBCGSerializerCallOperationType createFieldDeserializerCall(
    final CBCGJavaProgramOrder lines,
    final CBCGJavaNamePool names,
    final CBFieldType field)
  {
    return new OpCallDeserialize(
      lines.next(),
      names.freshLocalVariable(),
      field.name()
    );
  }

  private static MethodSpec createConstructorRecord(
    final CBRecordType record)
  {
    final var method = MethodSpec.constructorBuilder();
    method.addModifiers(PUBLIC);

    for (final var field : record.fields()) {
      final var type =
        CBCGJavaTypeExpressions.evaluateTypeExpression(field.type());
      final var fieldType =
        ParameterizedTypeName.get(ClassName.get(CBSerializerType.class), type);

      method.addParameter(
        fieldType,
        String.format("in_%s", field.name()),
        FINAL
      );

      method.addStatement(
        "this.$L = $T.requireNonNull(in_$L,$S)",
        field.name(),
        Objects.class,
        field.name(),
        field.name()
      );
    }

    return method.build();
  }

  private static MethodSpec createConstructorVariant(
    final CBVariantType variant)
  {
    final var method = MethodSpec.constructorBuilder();
    method.addModifiers(PUBLIC);

    for (final var caseV : variant.cases()) {
      for (final var field : caseV.fields()) {
        final var type =
          CBCGJavaTypeExpressions.evaluateTypeExpression(field.type());
        final var fieldType =
          ParameterizedTypeName.get(
            ClassName.get(CBSerializerType.class),
            type);

        method.addParameter(
          fieldType,
          String.format("in_%s", field.name()),
          FINAL
        );

        method.addStatement(
          "this.$L = $T.requireNonNull(in_$L,$S)",
          field.name(),
          Objects.class,
          field.name(),
          field.name()
        );
      }
    }

    return method.build();
  }
}
