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

import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaClassGeneratorType;
import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames;
import com.io7m.cedarbridge.codegen.java.internal.type_expressions.CBCGJavaTypeExpressions;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.runtime.api.CBSerializableType;
import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolVersionDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBRecordType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBTypeParameterType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.junreachable.UnreachableCodeException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public final class CBCGDataClassGenerator
  implements CBCGJavaClassGeneratorType<CBTypeDeclarationType>
{
  public CBCGDataClassGenerator()
  {

  }

  private static TypeSpec makeVariant(
    final ClassName className,
    final CBVariantType type)
  {
    final var containerBuilder =
      TypeSpec.interfaceBuilder(className)
        .addSuperinterface(CBSerializableType.class)
        .addModifiers(PUBLIC);

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
      final var innerClass =
        createRecordClass(
          ClassName.get(type.owner().name(), caseV.name()),
          Optional.of(superInterface),
          OptionalInt.of(variantIndex),
          caseV.fields(),
          typeParameters,
          protocols
        );

      containerBuilder.addType(innerClass);
      ++variantIndex;
    }

    return containerBuilder.build();
  }

  private static TypeSpec makeRecord(
    final ClassName className,
    final CBRecordType type)
  {
    final var protocols =
      type.owner().protocolVersionsForType(type);

    return createRecordClass(
      className,
      Optional.empty(),
      OptionalInt.empty(),
      type.fields(),
      type.parameters(),
      protocols
    );
  }

  private static TypeSpec createRecordClass(
    final ClassName className,
    final Optional<TypeName> containerInterface,
    final OptionalInt variantIndex,
    final List<CBFieldType> fieldList,
    final List<CBTypeParameterType> parameters,
    final List<CBProtocolVersionDeclarationType> protocols)
  {
    final var fields =
      fieldList
        .stream()
        .map(CBCGDataClassGenerator::createFieldDefinition)
        .collect(Collectors.toList());

    final var fieldAccessors =
      fieldList
        .stream()
        .map(CBCGDataClassMethodGeneration::createFieldAccessorMethod)
        .collect(Collectors.toList());

    final var typeVariables =
      CBCGJavaTypeExpressions.createTypeVariables(parameters);
    final var constructor =
      CBCGDataClassMethodGeneration.createFieldSetConstructor(fieldList);
    final var equals =
      CBCGDataClassMethodGeneration.createEqualsMethod(className, fields);
    final var hashCode =
      CBCGDataClassMethodGeneration.createHashCodeMethod(fields);

    final var classBuilder = TypeSpec.classBuilder(className);
    classBuilder.addModifiers(FINAL, PUBLIC);
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
        FieldSpec.builder(TypeName.INT, "VARIANT_INDEX", PUBLIC, STATIC, FINAL);
      field.initializer(CodeBlock.of("$L", Integer.valueOf(index)));
      classBuilder.addField(field.build());
    });

    classBuilder.addTypeVariables(typeVariables);
    classBuilder.addMethod(constructor);
    classBuilder.addMethod(equals);
    classBuilder.addMethod(hashCode);
    classBuilder.addMethods(fieldAccessors);
    classBuilder.addFields(fields);
    return classBuilder.build();
  }

  private static FieldSpec createFieldDefinition(
    final CBFieldType f)
  {
    return FieldSpec.builder(
      CBCGJavaTypeExpressions.evaluateTypeExpression(f.type()),
      f.name(),
      FINAL,
      PRIVATE
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

    final var pack = type.owner();
    final var className =
      CBCGJavaTypeNames.dataClassNameOf(type);

    final TypeSpec classDefinition;
    if (type instanceof CBRecordType) {
      classDefinition = makeRecord(className, (CBRecordType) type);
    } else if (type instanceof CBVariantType) {
      classDefinition = makeVariant(className, (CBVariantType) type);
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
}
