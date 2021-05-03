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
import com.io7m.cedarbridge.codegen.java.internal.type_expressions.CBCGJavaTypeExpressions;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.runtime.api.CBSerializerType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames.dataTypeNameOf;
import static com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames.serializerClassNameOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * A generator of code for Java serializer classes.
 */

public final class CBCGJavaSerializerGenerator
  implements CBCGJavaClassGeneratorType<CBTypeDeclarationType>
{
  /**
   * A generator of code for Java serializer classes.
   */

  public CBCGJavaSerializerGenerator()
  {

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

    final var dataTypeName =
      dataTypeNameOf(type);
    final var serializerClassName =
      serializerClassNameOf(type);

    final var superName =
      ClassName.get(CBSerializerType.class);
    final var parameterizedTypeName =
      ParameterizedTypeName.get(superName, dataTypeName);
    final var constructor =
      CBCGJavaSerializers.createConstructor(type);
    final var fields =
      CBCGJavaSerializers.createFields(type);
    final var deserializeMethods =
      CBCGJavaSerializers.createDeserializeMethods(type);
    final var serializeMethods =
      CBCGJavaSerializers.createSerializeMethods(type);

    final var classBuilder = TypeSpec.classBuilder(serializerClassName);
    classBuilder.addModifiers(FINAL, PUBLIC);
    classBuilder.addSuperinterface(parameterizedTypeName);
    classBuilder.addTypeVariables(
      CBCGJavaTypeExpressions.createTypeVariables(type.parameters())
    );
    classBuilder.addMethod(constructor);
    classBuilder.addFields(fields);
    classBuilder.addMethod(CBCGJavaSerializers.createDeserializeMethod(type));
    classBuilder.addMethod(CBCGJavaSerializers.createSerializeMethod(type));
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
}
