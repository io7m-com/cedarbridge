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

import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames;
import com.io7m.cedarbridge.codegen.java.internal.bindings.CBCGJavaNamePool;
import com.io7m.cedarbridge.codegen.java.internal.bindings.CBCGJavaProgramOrder;
import com.io7m.cedarbridge.codegen.java.internal.type_expressions.CBCGJavaTypeExpressions;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializerType;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolVersionDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBVariantCaseType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.junreachable.UnreachableCodeException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames.dataTypeNameOf;
import static com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames.dataTypeNameOfCase;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Functions to generate code for serializers.
 */

public final class CBCGJavaSerializers
{
  private CBCGJavaSerializers()
  {

  }

  /**
   * Create deserialization methods for the given protocol.
   *
   * @param proto The protocl
   *
   * @return The list of methods
   */

  public static List<MethodSpec> createDeserializeMethodsProtocol(
    final CBProtocolVersionDeclarationType proto)
  {
    Objects.requireNonNull(proto, "proto");
    return List.of();
  }

  static List<MethodSpec> createDeserializeMethods(
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
      .map(CBCGJavaSerializers::createDeserializeMethodVariantCase)
      .collect(Collectors.toList());
  }

  private static MethodSpec createDeserializeMethodVariantCase(
    final CBVariantCaseType caseV)
  {
    final var method =
      MethodSpec.methodBuilder(String.format("deserialize%s", caseV.name()));
    method.addModifiers(PRIVATE);
    method.addException(IOException.class);
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
        .sorted()
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

  /**
   * Create a deserialization method for the given protocol version.
   *
   * @param proto The protocol version
   *
   * @return A deserialization method
   */

  public static MethodSpec createDeserializeMethodProtocol(
    final CBProtocolVersionDeclarationType proto)
  {
    Objects.requireNonNull(proto, "proto");

    final var method = MethodSpec.methodBuilder("deserialize");
    method.addAnnotation(Override.class);
    method.addModifiers(PUBLIC);
    method.addException(IOException.class);
    method.addParameter(CBSerializationContextType.class, "context", FINAL);
    method.returns(CBCGJavaTypeNames.protoVersionedInterfaceNameOf(proto));

    method.addStatement(
      "final var variantIndex = context.readVariantIndex()"
    );

    final var switchStatement =
      method.beginControlFlow("switch (variantIndex)");

    final var types = proto.types();
    final var size = types.size();
    for (int index = 0; index < size; ++index) {
      final var type =
        types.get(index);
      final var typeDecl =
        type.declaration();

      final var switchCase =
        switchStatement.beginControlFlow(
          "case $L:",
          Integer.valueOf(index)
        );

      switchCase.addStatement(
        "return this.serializer$L.deserialize(context)",
        typeDecl.name());
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

  static MethodSpec createDeserializeMethod(
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
    method.addException(IOException.class);
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
    method.addException(IOException.class);
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

  static List<MethodSpec> createSerializeMethods(
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

  /**
   * Create serialization methods for the given protocol.
   *
   * @param proto The protocl
   *
   * @return The list of methods
   */

  public static List<MethodSpec> createSerializeMethodsProtocol(
    final CBProtocolVersionDeclarationType proto)
  {
    Objects.requireNonNull(proto, "proto");
    return List.of();
  }

  private static List<MethodSpec> createSerializeMethodsVariant(
    final CBVariantType type)
  {
    return type.cases()
      .stream()
      .map(CBCGJavaSerializers::createSerializeMethodVariantCase)
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
    method.addException(IOException.class);
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

  static MethodSpec createSerializeMethod(
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

  /**
   * Create a serialization method for the given protocol version.
   *
   * @param proto The protocol version
   *
   * @return A serialization method
   */

  public static MethodSpec createSerializeMethodProtocol(
    final CBProtocolVersionDeclarationType proto)
  {
    Objects.requireNonNull(proto, "proto");

    final var method = MethodSpec.methodBuilder("serialize");
    method.addAnnotation(Override.class);
    method.addModifiers(PUBLIC);
    method.addException(IOException.class);
    method.addParameter(CBSerializationContextType.class, "context", FINAL);
    method.addParameter(
      CBCGJavaTypeNames.protoVersionedInterfaceNameOf(proto),
      "value",
      FINAL);

    final var types = proto.types();
    for (int index = 0; index < types.size(); ++index) {
      final var type = types.get(index);
      final var valueClass = dataTypeNameOf(type.declaration());
      method.beginControlFlow(
        "if (value instanceof $T)",
        valueClass
      );
      method.addStatement(
        "context.writeVariantIndex($L)", Integer.valueOf(index));
      method.addStatement(
        "this.serializer$L.serialize(context, ($T) value)",
        type.declaration().name(),
        valueClass
      );
      method.addStatement("return");
      method.endControlFlow();
    }

    method.addStatement(
      "throw new $T($T.format($S,value.getClass()))",
      IllegalArgumentException.class,
      String.class,
      "Unrecognized variant case class: %s"
    );
    return method.build();
  }

  private static MethodSpec createSerializeMethodVariant(
    final CBVariantType type)
  {
    final var method = MethodSpec.methodBuilder("serialize");
    method.addAnnotation(Override.class);
    method.addModifiers(PUBLIC);
    method.addException(IOException.class);
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
    method.addException(IOException.class);
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

  static List<FieldSpec> createFields(
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

  /**
   * Create a list of class fields for the given protocol version.
   *
   * @param proto The protocol version
   *
   * @return A list of class fields
   */

  public static List<FieldSpec> createFieldsProtocol(
    final CBProtocolVersionDeclarationType proto)
  {
    Objects.requireNonNull(proto, "proto");

    return proto.types()
      .stream()
      .map(CBCGJavaSerializers::createFieldForProtoType)
      .collect(Collectors.toList());
  }

  private static List<FieldSpec> createFieldsVariant(
    final CBVariantType type)
  {
    return type.cases()
      .stream()
      .flatMap(c -> c.fields().stream())
      .map(CBCGJavaSerializers::createField)
      .collect(Collectors.toList());
  }

  private static List<FieldSpec> createFieldsRecord(
    final CBRecordType type)
  {
    return type.fields()
      .stream()
      .map(CBCGJavaSerializers::createField)
      .collect(Collectors.toList());
  }

  private static FieldSpec createFieldForProtoType(
    final CBTypeExprNamedType protoType)
  {
    final var type =
      CBCGJavaTypeExpressions.evaluateTypeExpression(protoType);
    final var fieldType =
      ParameterizedTypeName.get(ClassName.get(CBSerializerType.class), type);
    final var fieldName =
      String.format("serializer%s", protoType.declaration().name());

    return FieldSpec.builder(fieldType, fieldName, FINAL, PRIVATE)
      .build();
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

  static MethodSpec createConstructor(
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

  /**
   * Create a constructor for the given protocol version.
   *
   * @param proto The protocol version
   *
   * @return A constructor
   */

  public static MethodSpec createConstructorProtocol(
    final CBProtocolVersionDeclarationType proto)
  {
    Objects.requireNonNull(proto, "proto");

    final var method = MethodSpec.constructorBuilder();
    method.addModifiers(PUBLIC);

    for (final CBTypeExprNamedType protoType : proto.types()) {
      final var type =
        CBCGJavaTypeExpressions.evaluateTypeExpression(protoType);
      final var fieldType =
        ParameterizedTypeName.get(ClassName.get(CBSerializerType.class), type);

      final var name = protoType.declaration().name();
      method.addParameter(
        fieldType,
        String.format("in_%s", name),
        FINAL
      );

      method.addStatement(
        "this.serializer$L = $T.requireNonNull(in_$L,$S)",
        name,
        Objects.class,
        name,
        name
      );
    }

    return method.build();
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
