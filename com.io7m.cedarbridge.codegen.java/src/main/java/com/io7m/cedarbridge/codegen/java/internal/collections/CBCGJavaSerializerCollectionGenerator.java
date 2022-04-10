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

package com.io7m.cedarbridge.codegen.java.internal.collections;

import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaClassGeneratorType;
import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.runtime.api.CBSerializerCollection;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A generator for serializer collections.
 */

public final class CBCGJavaSerializerCollectionGenerator implements
  CBCGJavaClassGeneratorType<Collection<CBTypeDeclarationType>>
{
  /**
   * A generator for serializer collections.
   */

  public CBCGJavaSerializerCollectionGenerator()
  {

  }

  private static MethodSpec generateCollectionCreator(
    final String packageName,
    final List<ClassName> classes)
  {
    final var method =
      MethodSpec.methodBuilder("makeCollection")
        .addModifiers(PRIVATE, STATIC);
    method.returns(CBSerializerCollection.class);
    method.addStatement(
      "final var builder = $T.builder()",
      CBSerializerCollection.class);
    method.addStatement("builder.setPackageName($S)", packageName);
    for (final var clazz : classes) {
      method.addStatement("builder.addSerializers(new $T())", clazz);
    }
    method.addStatement("return builder.build()");
    return method.build();
  }

  private static FieldSpec generateCollectionField()
  {
    final var field =
      FieldSpec.builder(
        CBSerializerCollection.class,
        "COLLECTION",
        PRIVATE,
        STATIC,
        FINAL
      );

    field.initializer("makeCollection()");
    return field.build();
  }

  private static MethodSpec generateConstructor()
  {
    return MethodSpec.constructorBuilder()
      .addModifiers(PRIVATE)
      .build();
  }

  private static MethodSpec generateCollectionGet(
    final String packageName)
  {
    return MethodSpec.methodBuilder("get")
      .addModifiers(PUBLIC, FINAL, STATIC)
      .returns(CBSerializerCollection.class)
      .addStatement("return COLLECTION")
      .addJavadoc("@return The serializers for package {@code $L}", packageName)
      .build();
  }

  @Override
  public Path execute(
    final CBSPICodeGeneratorConfiguration configuration,
    final String packageName,
    final Collection<CBTypeDeclarationType> types)
    throws CBSPICodeGeneratorException
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(packageName, "packageName");
    Objects.requireNonNull(types, "types");

    final var classes =
      types.stream()
        .map(CBCGJavaTypeNames::serializerFactoryClassNameOf)
        .collect(Collectors.toList());

    final var className =
      CBCGJavaTypeNames.serializerCollectionClassNameOf(packageName);

    final var classBuilder = TypeSpec.classBuilder(className);
    classBuilder.addModifiers(PUBLIC, FINAL);
    classBuilder.addMethod(generateConstructor());
    classBuilder.addMethod(generateCollectionCreator(packageName, classes));
    classBuilder.addField(generateCollectionField());
    classBuilder.addMethod(generateCollectionGet(packageName));
    classBuilder.addJavadoc("Serializers for package {@code $L}.", packageName);

    final var classDefinition = classBuilder.build();

    final var javaFile =
      JavaFile.builder(packageName, classDefinition)
        .build();

    try {
      return javaFile.writeToPath(configuration.outputDirectory(), UTF_8);
    } catch (final IOException e) {
      throw new CBSPICodeGeneratorException(e);
    }
  }
}
