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

package com.io7m.cedarbridge.codegen.javastatic.internal;

import com.io7m.cedarbridge.codegen.javastatic.internal.generics.CBGenericSerializerMethodRefNamed;
import com.io7m.cedarbridge.codegen.javastatic.internal.generics.CBGenericSerializerMethodRefParameter;
import com.io7m.cedarbridge.codegen.javastatic.internal.generics.CBGenericSerializerMethodRefType;
import com.io7m.cedarbridge.codegen.javastatic.internal.generics.CBGenericSerializerMethodRefViaLambda;
import com.io7m.cedarbridge.codegen.javastatic.internal.generics.CBGenericSerializerMethodRefs;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.runtime.api.CBDeserializeType;
import com.io7m.cedarbridge.runtime.api.CBDeserializerMethod;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializeType;
import com.io7m.cedarbridge.runtime.api.CBSerializerMethod;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolVersionDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBTypeParameterType;
import com.io7m.cedarbridge.schema.compiled.CBVariantCaseType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.jodist.ClassName;
import com.io7m.jodist.CodeBlock;
import com.io7m.jodist.FieldSpec;
import com.io7m.jodist.JavaFile;
import com.io7m.jodist.MethodSpec;
import com.io7m.jodist.ParameterSpec;
import com.io7m.jodist.ParameterizedTypeName;
import com.io7m.jodist.TypeName;
import com.io7m.jodist.TypeSpec;
import com.io7m.jodist.TypeVariableName;
import com.io7m.junreachable.UnreachableCodeException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.dataClassNameOf;
import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.dataClassNameOfCase;
import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.dataTypeNameOf;
import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.dataTypeNameOfCase;
import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.fieldAccessorName;
import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.typeParameterDeserializeMethodName;
import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.typeParameterSerializeMethodName;
import static com.io7m.cedarbridge.codegen.javastatic.internal.generics.CBGenericSerializerMethodDirection.DESERIALIZE;
import static com.io7m.cedarbridge.codegen.javastatic.internal.generics.CBGenericSerializerMethodDirection.SERIALIZE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.SEALED;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A generator of Java data classes for types.
 */

public final class CBCGDataClassGenerator
  implements CBCGJavaClassGeneratorType<CBTypeDeclarationType>
{
  /**
   * A generator of Java data classes for types.
   */

  public CBCGDataClassGenerator()
  {

  }

  private static TypeSpec makeVariant(
    final CBCGJavaNamePool names,
    final CBVariantType type)
  {
    final var className =
      dataClassNameOf(type);
    final var typeName =
      dataTypeNameOf(type);

    final var containerBuilder =
      TypeSpec.interfaceBuilder(className)
        .addSuperinterface(CBSerializableType.class)
        .addModifiers(PUBLIC);

    if (!type.cases().isEmpty()) {
      containerBuilder.addModifiers(SEALED);
    }

    final var typeParameters =
      type.parameters();
    final var typeVariables =
      CBCGJavaTypeExpressions.createTypeVariables(typeParameters);

    containerBuilder.addTypeVariables(typeVariables);

    final var protocols =
      type.owner().protocolVersionsForType(type);

    for (final var protocol : protocols) {
      containerBuilder.addSuperinterface(
        CBCGJavaTypeNames.protoVersionedInterfaceNameOf(protocol));
    }

    final TypeName superInterface;
    if (typeVariables.isEmpty()) {
      superInterface = className;
    } else {
      final var typeVarArray = new TypeName[typeVariables.size()];
      typeVariables.toArray(typeVarArray);
      superInterface = ParameterizedTypeName.get(className, typeVarArray);
    }

    var variantIndex = 0;
    for (final var caseV : type.cases()) {
      final var caseClassName =
        ClassName.get(type.owner().name(), caseV.name());
      final var caseClassType =
        dataTypeNameOfCase(caseV);

      final var innerClass =
        createRecordlikeClass(
          caseV.documentation(),
          names,
          caseClassName,
          caseClassType,
          Optional.of(superInterface),
          OptionalInt.of(variantIndex),
          caseV.fields(),
          typeParameters,
          protocols
        );

      containerBuilder.addType(innerClass);
      ++variantIndex;
    }

    containerBuilder.addMethod(
      createVariantSerializeMethod(
        names,
        typeName,
        typeParameters,
        type.cases()
      )
    );
    containerBuilder.addMethod(
      createVariantDeserializeMethod(
        names,
        typeName,
        typeParameters,
        type.cases()
      )
    );

    /*
     * Generate Javadoc.
     */

    {
      final var documentation = type.documentation();
      documentation.forEach(text -> containerBuilder.addJavadoc(text + "\n"));

      for (final var parameter : typeParameters) {
        final var doc = parameter.documentation();
        final var compiled = String.join(" ", doc);
        containerBuilder.addJavadoc(
          "@param <$L> $L\n",
          parameter.name(),
          compiled
        );
      }
    }

    return containerBuilder.build();
  }

  private static MethodSpec createVariantDeserializeMethod(
    final CBCGJavaNamePool names,
    final TypeName typeName,
    final List<CBTypeParameterType> parameters,
    final List<CBVariantCaseType> cases)
  {
    final var builder = MethodSpec.methodBuilder("deserialize");
    builder.addModifiers(STATIC, PUBLIC);
    builder.addAnnotation(CBDeserializerMethod.class);
    builder.addException(IOException.class);
    builder.returns(typeName);
    builder.addParameter(
      TypeName.get(CBSerializationContextType.class),
      "$context",
      FINAL
    );

    /*
     * Deserialize methods have one extra parameter for each type parameter
     * present on the type.
     */

    for (final var parameter : parameters) {
      builder.addTypeVariable(
        TypeVariableName.get(parameter.name(), CBSerializableType.class)
      );
      builder.addParameter(
        ParameterizedTypeName.get(
          ClassName.get(CBDeserializeType.class),
          TypeVariableName.get(parameter.name())
        ),
        typeParameterDeserializeMethodName(parameter),
        FINAL
      );
    }

    final var deserializerParameters =
      parameters.stream()
        .map(CBCGJavaTypeNames::typeParameterDeserializeMethodName)
        .map(n -> CodeBlock.of("$L", n))
        .collect(CodeBlock.joining(","));

    /*
     * Read the variant index, and switch on it. Call the necessary
     * deserializer function for each case.
     */

    builder.addStatement(
      "final var $L = $L.readVariantIndex()",
      "$i",
      "$context"
    );

    final var switchStatement =
      builder.beginControlFlow("switch ($L)", "$i");

    for (int index = 0; index < cases.size(); ++index) {
      final var caseV = cases.get(index);
      final var switchCase =
        switchStatement.beginControlFlow(
          "case $L:",
          Integer.valueOf(index)
        );

      if (parameters.isEmpty()) {
        switchCase.addStatement(
          "return $L.deserialize($L)",
          dataClassNameOf(caseV.owner()),
          "$context"
        );
      } else {
        switchCase.addStatement(
          "return $L.deserialize($L, $L)",
          dataClassNameOf(caseV.owner()),
          "$context",
          deserializerParameters
        );
      }

      switchCase.endControlFlow();
    }

    {
      final var defaultCase =
        switchStatement.beginControlFlow("default: ");

      defaultCase.addStatement(
        "throw $L.errorUnrecognizedVariantIndex($L)",
        "$context",
        "$i"
      );
      defaultCase.endControlFlow();
    }

    switchStatement.endControlFlow();

    /*
     * Generate JavaDoc for the method.
     */

    generateDeserializerJavadoc(typeName, parameters, builder);

    return builder.build();
  }

  private static MethodSpec createVariantSerializeMethod(
    final CBCGJavaNamePool names,
    final TypeName typeName,
    final List<CBTypeParameterType> parameters,
    final List<CBVariantCaseType> cases)
  {
    final var builder = MethodSpec.methodBuilder("serialize");
    builder.addModifiers(STATIC, PUBLIC);
    builder.addAnnotation(CBSerializerMethod.class);
    builder.addException(IOException.class);
    builder.addParameter(
      TypeName.get(CBSerializationContextType.class),
      "$context",
      FINAL
    );
    builder.addParameter(
      typeName,
      "$x",
      FINAL
    );

    /*
     * Serialize methods have one extra parameter for each type parameter
     * present on the type.
     */

    for (final var parameter : parameters) {
      builder.addTypeVariable(
        TypeVariableName.get(parameter.name(), CBSerializableType.class)
      );
      builder.addParameter(
        ParameterizedTypeName.get(
          ClassName.get(CBSerializeType.class),
          TypeVariableName.get(parameter.name())
        ),
        typeParameterSerializeMethodName(parameter),
        FINAL
      );
    }

    final var serializerParameters =
      parameters.stream()
        .map(CBCGJavaTypeNames::typeParameterSerializeMethodName)
        .map(n -> CodeBlock.of("$L", n))
        .collect(CodeBlock.joining(","));

    for (final var caseV : cases) {
      final var block = CodeBlock.builder();
      block.beginControlFlow(
        "if ($L instanceof $T $L)",
        "$x",
        dataTypeNameOfCase(caseV),
        "$y"
      );

      if (parameters.isEmpty()) {
        block.addStatement(
          "$T.serialize($L, $L)",
          dataClassNameOfCase(caseV),
          "$context",
          "$y"
        );
      } else {
        block.addStatement(
          "$T.serialize($L, $L, $L)",
          dataClassNameOfCase(caseV),
          "$context",
          "$y",
          serializerParameters
        );
      }

      block.addStatement("return");
      block.endControlFlow();
      builder.addCode(block.build());
    }

    builder.addStatement(
      "throw $L.errorUnrecognizedVariantCaseClass($L.getClass())",
      "$context",
      "$x"
    );

    /*
     * Generate JavaDoc for the method.
     */

    generateSerializerJavadoc(typeName, parameters, builder);

    return builder.build();
  }

  private static TypeSpec makeRecord(
    final CBCGJavaNamePool names,
    final CBRecordType type)
  {
    final var protocols =
      type.owner().protocolVersionsForType(type);

    return createRecordlikeClass(
      type.documentation(),
      names,
      dataClassNameOf(type),
      dataTypeNameOf(type),
      Optional.empty(),
      OptionalInt.empty(),
      type.fields(),
      type.parameters(),
      protocols
    );
  }

  private static TypeSpec createRecordlikeClass(
    final List<String> documentation,
    final CBCGJavaNamePool names,
    final ClassName className,
    final TypeName dataTypeName,
    final Optional<TypeName> containerInterface,
    final OptionalInt variantIndex,
    final List<CBFieldType> fieldList,
    final List<CBTypeParameterType> parameters,
    final List<CBProtocolVersionDeclarationType> protocols)
  {
    final var fields =
      fieldList
        .stream()
        .map(CBCGDataClassGenerator::createRecordParameter)
        .toList();

    final var typeVariables =
      CBCGJavaTypeExpressions.createTypeVariables(parameters);

    final var classBuilder = TypeSpec.recordBuilder(className);
    classBuilder.addModifiers(PUBLIC);
    classBuilder.addSuperinterface(CBSerializableType.class);

    for (final var protocol : protocols) {
      classBuilder.addSuperinterface(
        CBCGJavaTypeNames.protoVersionedInterfaceNameOf(protocol)
      );
    }

    if (containerInterface.isPresent()) {
      classBuilder.addModifiers(STATIC);
      classBuilder.addSuperinterface(containerInterface.get());
    }

    variantIndex.ifPresent(index -> {
      final var field =
        FieldSpec.builder(TypeName.INT, "VARIANT_INDEX", PRIVATE, STATIC, FINAL);
      field.initializer(CodeBlock.of("$L", Integer.valueOf(index)));
      classBuilder.addField(field.build());
    });

    classBuilder.addTypeVariables(typeVariables);
    classBuilder.addRecordComponents(fields);
    classBuilder.compactConstructor(createCompactConstructor(fields));

    classBuilder.addMethod(
      createRecordlikeSerializeMethod(
        names,
        dataTypeName,
        variantIndex,
        parameters,
        fieldList)
    );
    classBuilder.addMethod(
      createRecordlikeDeserializeMethod(
        names,
        dataTypeName,
        parameters,
        fieldList)
    );

    /*
     * Generate Javadoc.
     */

    {
      documentation.forEach(text -> classBuilder.addJavadoc(text + "\n"));

      for (final var field : fieldList) {
        final var doc = field.documentation();
        final var compiled = String.join(" ", doc);
        classBuilder.addJavadoc(
          "@param $L $L\n",
          fieldAccessorName(field.name()),
          compiled
        );
      }
      for (final var parameter : parameters) {
        final var doc = parameter.documentation();
        final var compiled = String.join(" ", doc);
        classBuilder.addJavadoc(
          "@param <$L> $L\n",
          parameter.name(),
          compiled
        );
      }
    }

    return classBuilder.build();
  }

  private static MethodSpec createRecordlikeDeserializeMethod(
    final CBCGJavaNamePool names,
    final TypeName typeName,
    final List<CBTypeParameterType> parameters,
    final List<CBFieldType> fields)
  {
    final var builder = MethodSpec.methodBuilder("deserialize");
    builder.addModifiers(STATIC, PUBLIC);
    builder.addAnnotation(CBDeserializerMethod.class);
    builder.addException(IOException.class);
    builder.returns(typeName);

    builder.addParameter(
      TypeName.get(CBSerializationContextType.class),
      "$context",
      FINAL
    );

    /*
     * Deserialize methods have one extra parameter for each type parameter
     * present on the type.
     */

    for (final var parameter : parameters) {
      builder.addTypeVariable(
        TypeVariableName.get(parameter.name(), CBSerializableType.class)
      );
      builder.addParameter(
        ParameterizedTypeName.get(
          ClassName.get(CBDeserializeType.class),
          TypeVariableName.get(parameter.name())
        ),
        typeParameterDeserializeMethodName(parameter),
        FINAL
      );
    }

    /*
     * Build a set of references to deserialize() methods for each field
     * type. Type applications are eta-expanded into local lambda expressions.
     */

    final var fieldMethodRefs = new ArrayList<FieldSerializeMethodReference>();
    for (final var field : fields) {
      fieldMethodRefs.add(buildDeserializeMethodRefFor(names, field));
    }

    /*
     * Write out all the required lambda expressions, if any.
     */

    for (final var fieldRef : fieldMethodRefs) {
      if (!fieldRef.lambas.isEmpty()) {
        builder.addComment(
          "// Lambda expressions for field %s".formatted(fieldRef.field.name())
        );
        for (final var lambda : fieldRef.lambas) {
          builder.addStatement(lambda);
        }
      }
    }

    /*
     * Generate the calls to deserialize() methods (or local lambda expressions).
     */

    final var localVars = new ArrayList<String>();
    builder.addComment("Deserialization calls in field order.");
    for (final var fieldRef : fieldMethodRefs) {
      localVars.add(callDeserializeMethod(builder, names, fieldRef));
    }

    final var variables =
      localVars.stream()
        .map(n -> CodeBlock.of("$L", n))
        .collect(CodeBlock.joining(","));

    final var constructorCall =
      CodeBlock.builder()
        .addStatement("return new $T($L)", typeName, variables)
        .build();

    builder.addCode(constructorCall);

    generateDeserializerJavadoc(typeName, parameters, builder);
    return builder.build();
  }

  private static void generateDeserializerJavadoc(
    final TypeName typeName,
    final List<CBTypeParameterType> parameters,
    final MethodSpec.Builder builder)
  {
    /*
     * Generate JavaDoc for the method.
     */

    builder.addJavadoc("Deserialize a value of type $T.\n", typeName);
    builder.addJavadoc("@param $L The serialization context.\n", "$context");
    builder.addJavadoc("@return A value of type $T.\n", typeName);

    for (final var parameter : parameters) {
      final var name = typeParameterDeserializeMethodName(parameter);
      builder.addJavadoc(
        "@param $L A deserializer for values of type $T.\n",
        name,
        TypeVariableName.get(parameter.name())
      );
      builder.addJavadoc(
        "@param <$L> The type of one or more deserialized fields.\n",
        TypeVariableName.get(parameter.name())
      );
    }
  }

  private static MethodSpec createRecordlikeSerializeMethod(
    final CBCGJavaNamePool names,
    final TypeName typeName,
    final OptionalInt variantIndex,
    final List<CBTypeParameterType> parameters,
    final List<CBFieldType> fields)
  {
    final var builder = MethodSpec.methodBuilder("serialize");
    builder.addModifiers(STATIC, PUBLIC);
    builder.addAnnotation(CBSerializerMethod.class);
    builder.addException(IOException.class);

    builder.addParameter(
      TypeName.get(CBSerializationContextType.class),
      "$context",
      FINAL
    );

    builder.addParameter(
      typeName,
      "$x",
      FINAL
    );

    /*
     * Serialize methods have one extra parameter for each type parameter
     * present on the type.
     */

    for (final var parameter : parameters) {
      builder.addTypeVariable(
        TypeVariableName.get(parameter.name(), CBSerializableType.class)
      );
      builder.addParameter(
        ParameterizedTypeName.get(
          ClassName.get(CBSerializeType.class),
          TypeVariableName.get(parameter.name())
        ),
        typeParameterSerializeMethodName(parameter),
        FINAL
      );
    }

    /*
     * Build a set of references to serialize() methods for each field
     * type. Type applications are eta-expanded into local lambda expressions.
     */

    final var fieldMethodRefs = new ArrayList<FieldSerializeMethodReference>();
    for (final var field : fields) {
      fieldMethodRefs.add(buildSerializeMethodRefFor(names, field));
    }

    /*
     * Write out all the required lambda expressions, if any.
     */

    for (final var fieldRef : fieldMethodRefs) {
      if (!fieldRef.lambas.isEmpty()) {
        builder.addComment(
          "// Lambda expressions for field %s".formatted(fieldRef.field.name())
        );
        for (final var lambda : fieldRef.lambas) {
          builder.addStatement(lambda);
        }
      }
    }

    /*
     * Generate the calls to serialize() methods (or local lambda expressions).
     */

    variantIndex.ifPresent(value -> {
      builder.addStatement("$L.writeVariantIndex(VARIANT_INDEX)", "$context");
    });

    builder.addComment("Serialization calls in field order.");
    for (final var fieldRef : fieldMethodRefs) {
      callSerializeMethod(builder, fieldRef);
    }

    generateSerializerJavadoc(typeName, parameters, builder);
    return builder.build();
  }

  private static void generateSerializerJavadoc(
    final TypeName typeName,
    final List<CBTypeParameterType> parameters,
    final MethodSpec.Builder builder)
  {
    /*
     * Generate JavaDoc for the method.
     */

    builder.addJavadoc("Serialize a value of type $T.\n", typeName);
    builder.addJavadoc("@param $L The serialization context.\n", "$context");
    builder.addJavadoc("@param $L The value to be serialized.\n", "$x");

    for (final var parameter : parameters) {
      final var name = typeParameterSerializeMethodName(parameter);
      builder.addJavadoc(
        "@param $L A serializer for values of type $T.\n",
        name,
        TypeVariableName.get(parameter.name())
      );
      builder.addJavadoc(
        "@param <$L> The type of one or more serialized fields.\n",
        TypeVariableName.get(parameter.name())
      );
    }
  }

  private static FieldSerializeMethodReference buildDeserializeMethodRefFor(
    final CBCGJavaNamePool names,
    final CBFieldType field)
  {
    final var type =
      field.type();
    final var ref =
      new CBGenericSerializerMethodRefs(names, type, DESERIALIZE)
        .build();

    if (ref instanceof CBGenericSerializerMethodRefNamed named) {
      return new FieldSerializeMethodReference(field, named, List.of());
    }

    if (ref instanceof CBGenericSerializerMethodRefParameter parameter) {
      return new FieldSerializeMethodReference(field, parameter, List.of());
    }

    if (ref instanceof CBGenericSerializerMethodRefViaLambda lambda) {
      final var lambdaRefs =
        CBGenericSerializerMethodRefs.findLambdasInDeclarationOrder(lambda);
      final var lambdaBlocks =
        new ArrayList<CodeBlock>(lambdaRefs.size());

      while (!lambdaRefs.isEmpty()) {
        lambdaBlocks.add(buildDeserializationLambda(lambdaRefs.pop()));
      }

      return new FieldSerializeMethodReference(field, lambda, lambdaBlocks);
    }

    throw new UnreachableCodeException();
  }

  private static FieldSerializeMethodReference buildSerializeMethodRefFor(
    final CBCGJavaNamePool names,
    final CBFieldType field)
  {
    final var type =
      field.type();
    final var ref =
      new CBGenericSerializerMethodRefs(names, type, SERIALIZE)
        .build();

    if (ref instanceof CBGenericSerializerMethodRefNamed named) {
      return new FieldSerializeMethodReference(field, named, List.of());
    }

    if (ref instanceof CBGenericSerializerMethodRefParameter parameter) {
      return new FieldSerializeMethodReference(field, parameter, List.of());
    }

    if (ref instanceof CBGenericSerializerMethodRefViaLambda lambda) {
      final var lambdaRefs =
        CBGenericSerializerMethodRefs.findLambdasInDeclarationOrder(lambda);
      final var lambdaBlocks =
        new ArrayList<CodeBlock>(lambdaRefs.size());

      while (!lambdaRefs.isEmpty()) {
        lambdaBlocks.add(buildSerializationLambda(lambdaRefs.pop()));
      }

      return new FieldSerializeMethodReference(field, lambda, lambdaBlocks);
    }

    throw new UnreachableCodeException();
  }

  private static void callSerializeMethod(
    final MethodSpec.Builder builder,
    final FieldSerializeMethodReference fieldRef)
  {
    final var field = fieldRef.field;
    final var ref = fieldRef.reference;

    builder.addStatement(
      "$L.begin($S)",
      "$context",
      field.name()
    );

    try {
      if (ref instanceof CBGenericSerializerMethodRefNamed named) {
        builder.addStatement(
          "$T.serialize($L, $L.$L)",
          dataClassNameOf(named.type().declaration()),
          "$context",
          "$x",
          fieldAccessorName(field.name())
        );
        return;
      }

      if (ref instanceof CBGenericSerializerMethodRefParameter parameter) {
        final var name =
          typeParameterSerializeMethodName(
            parameter.type().parameter());

        builder.addStatement(
          "$L.execute($L, $L.$L)",
          name,
          "$context",
          "$x",
          fieldAccessorName(field.name())
        );
        return;
      }

      if (ref instanceof CBGenericSerializerMethodRefViaLambda lambda) {
        builder.addStatement(
          "$L.execute($L, $L.$L)",
          lambda.lambdaName(),
          "$context",
          "$x",
          fieldAccessorName(field.name())
        );
        return;
      }

      throw new UnreachableCodeException();
    } finally {
      builder.addStatement(
        "$L.end($S)",
        "$context",
        field.name()
      );
    }
  }

  private static String callDeserializeMethod(
    final MethodSpec.Builder builder,
    final CBCGJavaNamePool names,
    final FieldSerializeMethodReference fieldRef)
  {
    final var field = fieldRef.field;
    final var ref = fieldRef.reference;

    builder.addStatement(
      "$L.begin($S)",
      "$context",
      field.name()
    );

    final var localVar = names.freshLocalVariable();

    try {
      if (ref instanceof CBGenericSerializerMethodRefNamed named) {
        builder.addStatement(
          "final var $L = $T.deserialize($L)",
          localVar,
          dataClassNameOf(named.type().declaration()),
          "$context"
        );
        return localVar;
      }

      if (ref instanceof CBGenericSerializerMethodRefParameter parameter) {
        final var name =
          typeParameterDeserializeMethodName(
            parameter.type().parameter());

        builder.addStatement(
          "final var $L = $L.execute($L)",
          localVar,
          name,
          "$context"
        );
        return localVar;
      }

      if (ref instanceof CBGenericSerializerMethodRefViaLambda lambda) {
        builder.addStatement(
          "final var $L = $L.execute($L)",
          localVar,
          lambda.lambdaName(),
          "$context"
        );
        return localVar;
      }

      throw new UnreachableCodeException();
    } finally {
      builder.addStatement(
        "$L.end($S)",
        "$context",
        field.name()
      );
    }
  }

  private static CodeBlock buildDeserializationLambda(
    final CBGenericSerializerMethodRefViaLambda lambda)
  {
    final var builder = CodeBlock.builder();
    builder.add(
      "final $T $L = ($T $L) -> {\n",
      lambda.lambdaType(),
      lambda.lambdaName(),
      CBSerializationContextType.class,
      lambda.lambdaContextName()
    );
    builder.indent();

    final var targetType =
      lambda.lambdaTarget();
    final var targetArgs =
      lambda.lambdaTargetRefs();

    final var arguments = new ArrayList<String>();
    for (final var targetArg : targetArgs) {
      if (targetArg instanceof CBGenericSerializerMethodRefParameter p) {
        arguments.add(
          typeParameterDeserializeMethodName(
            p.type().parameter())
        );
      } else if (targetArg instanceof CBGenericSerializerMethodRefNamed n) {
        arguments.add(
          "%s::deserialize".formatted(
            dataClassNameOf(n.type().declaration())
          )
        );
      } else if (targetArg instanceof CBGenericSerializerMethodRefViaLambda l) {
        arguments.add(l.lambdaName());
      }
    }

    builder.add(
      "return $T.deserialize($L, ",
      dataClassNameOf(targetType.declaration()),
      lambda.lambdaContextName()
    );

    final var argumentCount = arguments.size();
    for (int index = 0; index < argumentCount; ++index) {
      if (index + 1 < argumentCount) {
        builder.add("$L,", arguments.get(index));
      } else {
        builder.add("$L", arguments.get(index));
      }
    }

    builder.add(");\n");
    builder.unindent();
    builder.add("}");
    return builder.build();
  }

  private static CodeBlock buildSerializationLambda(
    final CBGenericSerializerMethodRefViaLambda lambda)
  {
    final var builder = CodeBlock.builder();
    builder.add(
      "final $T $L = ($T $L, $T $L) -> {\n",
      lambda.lambdaType(),
      lambda.lambdaName(),
      CBSerializationContextType.class,
      lambda.lambdaContextName(),
      CBCGJavaTypeExpressions.evaluateTypeExpression(lambda.type()),
      lambda.lambdaValueName()
    );
    builder.indent();

    final var targetType =
      lambda.lambdaTarget();
    final var targetArgs =
      lambda.lambdaTargetRefs();

    final var arguments = new ArrayList<String>();
    for (final var targetArg : targetArgs) {
      if (targetArg instanceof CBGenericSerializerMethodRefParameter p) {
        arguments.add(
          typeParameterSerializeMethodName(
            p.type().parameter())
        );
      } else if (targetArg instanceof CBGenericSerializerMethodRefNamed n) {
        arguments.add(
          "%s::serialize".formatted(
            dataClassNameOf(n.type().declaration())
          )
        );
      } else if (targetArg instanceof CBGenericSerializerMethodRefViaLambda l) {
        arguments.add(l.lambdaName());
      }
    }

    builder.add(
      "$T.serialize($L, $L, ",
      dataClassNameOf(targetType.declaration()),
      lambda.lambdaContextName(),
      lambda.lambdaValueName()
    );

    final var argumentCount = arguments.size();
    for (int index = 0; index < argumentCount; ++index) {
      if (index + 1 < argumentCount) {
        builder.add("$L,", arguments.get(index));
      } else {
        builder.add("$L", arguments.get(index));
      }
    }

    builder.add(");\n");
    builder.unindent();
    builder.add("}");
    return builder.build();
  }

  private static MethodSpec createCompactConstructor(
    final List<ParameterSpec> fields)
  {
    final var builder = MethodSpec.constructorBuilder();
    builder.addModifiers(PUBLIC);

    for (final var f : fields) {
      if (!f.type.isPrimitive()) {
        builder.addStatement(
          "$T.requireNonNull($L, $S)",
          Objects.class,
          f.name,
          f.name
        );
      }
    }

    return builder.build();
  }

  private static ParameterSpec createRecordParameter(
    final CBFieldType f)
  {
    return ParameterSpec.builder(
      CBCGJavaTypeExpressions.evaluateTypeExpression(f.type()),
      fieldAccessorName(f.name())
    ).build();
  }

  @Override
  public Path execute(
    final CBSPICodeGeneratorConfiguration configuration,
    final String packageName,
    final CBTypeDeclarationType type)
    throws CBSPICodeGeneratorException
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(packageName, "packageName");
    Objects.requireNonNull(type, "type");

    final var names = new CBCGJavaNamePool();
    final var pack = type.owner();
    final TypeSpec classDefinition;
    if (type instanceof CBRecordType r) {
      classDefinition = makeRecord(names, r);
    } else if (type instanceof CBVariantType v) {
      classDefinition = makeVariant(names, v);
    } else {
      throw new UnreachableCodeException();
    }

    final var javaFile =
      JavaFile.builder(pack.name(), classDefinition)
        .build();

    try {
      return javaFile.writeToPath(configuration.outputDirectory(), UTF_8);
    } catch (final IOException e) {
      throw new CBSPICodeGeneratorException(e);
    }
  }

  private record FieldSerializeMethodReference(
    CBFieldType field,
    CBGenericSerializerMethodRefType reference,
    List<CodeBlock> lambas)
  {

  }
}
