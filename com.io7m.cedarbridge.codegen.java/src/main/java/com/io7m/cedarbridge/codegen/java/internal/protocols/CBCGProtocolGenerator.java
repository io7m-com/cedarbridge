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

package com.io7m.cedarbridge.codegen.java.internal.protocols;

import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaClassGeneratorType;
import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.runtime.api.CBProtocolMessageType;
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerCollection;
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerCollectionType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolDeclarationType;
import com.io7m.jodist.ClassName;
import com.io7m.jodist.FieldSpec;
import com.io7m.jodist.JavaFile;
import com.io7m.jodist.MethodSpec;
import com.io7m.jodist.ParameterizedTypeName;
import com.io7m.jodist.TypeSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A generator of Java code for protocol declarations.
 */

public final class CBCGProtocolGenerator
  implements CBCGJavaClassGeneratorType<CBProtocolDeclarationType>
{
  /**
   * A generator of Java code for protocol declarations.
   */

  public CBCGProtocolGenerator()
  {

  }

  private static MethodSpec generateIdMethod()
  {
    return MethodSpec.methodBuilder("id")
      .addModifiers(PUBLIC, STATIC)
      .returns(UUID.class)
      .addStatement("return PROTOCOL_ID")
      .addJavadoc("@return The unique ID of the protocol")
      .build();
  }

  private static FieldSpec createUUIDField(
    final CBProtocolDeclarationType proto)
  {
    return FieldSpec.builder(UUID.class, "PROTOCOL_ID", PRIVATE, FINAL, STATIC)
      .initializer("$T.fromString($S)", UUID.class, proto.id())
      .build();
  }

  private static FieldSpec createSerializersField(
    final CBProtocolDeclarationType proto)
  {
    final var className =
      CBCGJavaTypeNames.protoInterfaceNameOf(proto);

    final var pName =
      ParameterizedTypeName.get(
        ClassName.get(CBProtocolSerializerCollectionType.class),
        className
      );

    return FieldSpec.builder(pName, "FACTORIES", PRIVATE, FINAL, STATIC)
      .initializer("createFactories()")
      .build();
  }

  private static MethodSpec generateFactoriesMethod(
    final CBProtocolDeclarationType proto)
  {
    final var className =
      CBCGJavaTypeNames.protoInterfaceNameOf(proto);

    final var pName =
      ParameterizedTypeName.get(
        ClassName.get(CBProtocolSerializerCollectionType.class),
        className
      );

    return MethodSpec.methodBuilder("factories")
      .addModifiers(PUBLIC, STATIC)
      .returns(pName)
      .addStatement("return FACTORIES")
      .addJavadoc("@return The collection of factories for the protocol")
      .build();
  }

  private static MethodSpec generateCreateSerializersMethod(
    final CBProtocolDeclarationType proto)
  {
    final var className =
      CBCGJavaTypeNames.protoInterfaceNameOf(proto);

    final var pName =
      ParameterizedTypeName.get(
        ClassName.get(CBProtocolSerializerCollectionType.class),
        className
      );

    final var method =
      MethodSpec.methodBuilder("createFactories")
        .addModifiers(PRIVATE, STATIC)
        .returns(pName);

    method.addStatement(
      "final var builder = $T.<$T>builder(PROTOCOL_ID)",
      CBProtocolSerializerCollection.class,
      className
    );

    for (final var version : proto.versions().values()) {
      final var serializerFactoryClass =
        CBCGJavaTypeNames.protoSerializerFactoryClassNameOf(version);
      method.addStatement(
        "builder.addFactory(new $T())",
        serializerFactoryClass);
    }

    method.addStatement("return builder.build()");
    return method.build();
  }

  private static MethodSpec generateConstructor()
  {
    return MethodSpec.constructorBuilder()
      .addModifiers(PRIVATE)
      .build();
  }

  @Override
  public Path execute(
    final CBSPICodeGeneratorConfiguration configuration,
    final String packageName,
    final CBProtocolDeclarationType proto)
    throws CBSPICodeGeneratorException
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(packageName, "packageName");
    Objects.requireNonNull(proto, "proto");

    final var pack = proto.owner();
    final var className = CBCGJavaTypeNames.protoNameOf(proto);

    final var classBuilder = TypeSpec.classBuilder(className);
    classBuilder.addSuperinterface(CBProtocolMessageType.class);
    classBuilder.addModifiers(PUBLIC);
    classBuilder.addField(createUUIDField(proto));
    classBuilder.addField(createSerializersField(proto));
    classBuilder.addMethod(generateIdMethod());
    classBuilder.addMethod(generateFactoriesMethod(proto));
    classBuilder.addMethod(generateConstructor());
    classBuilder.addMethod(generateCreateSerializersMethod(proto));
    classBuilder.addJavadoc(
      "Protocol {@code $L.$L}.",
      pack.name(),
      proto.name()
    );

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
