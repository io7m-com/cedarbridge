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

import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.runtime.api.CBDeserializerMethod;
import com.io7m.cedarbridge.runtime.api.CBProtocolMessageVersionedSerializerType;
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializerMethod;
import com.io7m.cedarbridge.schema.compiled.CBProtocolVersionDeclarationType;
import com.io7m.jodist.ClassName;
import com.io7m.jodist.FieldSpec;
import com.io7m.jodist.JavaFile;
import com.io7m.jodist.MethodSpec;
import com.io7m.jodist.ParameterizedTypeName;
import com.io7m.jodist.TypeName;
import com.io7m.jodist.TypeSpec;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Objects;

import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.protoVersionedInterfaceNameOf;
import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.protoVersionedSerializerNameOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A generator of Java serializers for versioned protocols.
 */

public final class CBCGProtocolMessageSerializerGenerator
  implements CBCGJavaClassGeneratorType<CBProtocolVersionDeclarationType>
{
  /**
   * A generator of Java interface types for protocol version declarations.
   */

  public CBCGProtocolMessageSerializerGenerator()
  {

  }

  @Override
  public Path execute(
    final CBSPICodeGeneratorConfiguration configuration,
    final String packageName,
    final CBProtocolVersionDeclarationType proto)
    throws CBSPICodeGeneratorException
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(packageName, "packageName");
    Objects.requireNonNull(proto, "proto");

    final var owner = proto.owner();
    final var pack = owner.owner();

    final var classBuilder =
      TypeSpec.classBuilder(protoVersionedSerializerNameOf(proto));
    final var messageClassName =
      protoVersionedInterfaceNameOf(proto);

    classBuilder.addSuperinterface(
      ParameterizedTypeName.get(
        ClassName.get(CBProtocolMessageVersionedSerializerType.class),
        messageClassName
      )
    );
    classBuilder.addModifiers(PUBLIC, FINAL);
    classBuilder.addJavadoc(
      "Protocol {@code $L.$L}, version {@code $L}.",
      pack.name(),
      owner.name(),
      proto.version()
    );

    classBuilder.addMethod(createConstructor());
    classBuilder.addField(createVersionField(proto));
    classBuilder.addMethod(createVersionMethod());
    classBuilder.addMethod(createMessageClassMethod(messageClassName));
    classBuilder.addMethod(createSerializeMethod(messageClassName));
    classBuilder.addMethod(createDeserializeMethod(messageClassName));

    final var classDefinition =
      classBuilder.build();

    final var javaFile =
      JavaFile.builder(pack.name(), classDefinition)
        .build();

    try {
      return javaFile.writeToPath(configuration.outputDirectory(), UTF_8);
    } catch (final IOException e) {
      throw new CBSPICodeGeneratorException(e);
    }
  }

  private static MethodSpec createConstructor()
  {
    final var builder = MethodSpec.constructorBuilder();
    builder.addJavadoc("Construct a serializer.");
    builder.addModifiers(PUBLIC);
    return builder.build();
  }

  private static FieldSpec createVersionField(
    final CBProtocolVersionDeclarationType proto)
  {
    final var builder =
      FieldSpec.builder(
        BigInteger.class,
        "$VERSION",
        PRIVATE,
        STATIC,
        FINAL
      );

    builder.initializer("new $T($S)", BigInteger.class, proto.version());
    return builder.build();
  }

  private static MethodSpec createVersionMethod()
  {
    final var builder = MethodSpec.methodBuilder("version");
    builder.addModifiers(PUBLIC);
    builder.addAnnotation(Override.class);
    builder.returns(BigInteger.class);
    builder.addStatement("return $L", "$VERSION");
    return builder.build();
  }

  private static MethodSpec createMessageClassMethod(
    final ClassName messageClassName)
  {
    final var builder = MethodSpec.methodBuilder("messageClass");
    builder.addModifiers(PUBLIC);
    builder.addAnnotation(Override.class);
    builder.returns(ParameterizedTypeName.get(
      ClassName.get(Class.class),
      messageClassName
    ));
    builder.addStatement("return $T.class", messageClassName);
    return builder.build();
  }

  private static MethodSpec createDeserializeMethod(
    final ClassName className)
  {
    final var builder = MethodSpec.methodBuilder("deserialize");
    builder.addModifiers(PUBLIC);
    builder.addAnnotation(CBDeserializerMethod.class);
    builder.addAnnotation(Override.class);
    builder.addException(IOException.class);
    builder.returns(className);
    builder.addParameter(
      TypeName.get(CBSerializationContextType.class),
      "$context",
      FINAL
    );
    builder.addStatement(
      "return $T.deserialize($L)",
      className,
      "$context"
    );
    return builder.build();
  }

  private static MethodSpec createSerializeMethod(
    final ClassName className)
  {
    final var builder = MethodSpec.methodBuilder("serialize");
    builder.addModifiers(PUBLIC);
    builder.addAnnotation(CBSerializerMethod.class);
    builder.addAnnotation(Override.class);
    builder.addException(IOException.class);
    builder.addParameter(
      TypeName.get(CBSerializationContextType.class),
      "$context",
      FINAL
    );
    builder.addParameter(
      className,
      "$x",
      FINAL
    );
    builder.addStatement(
      "$T.serialize($L, $L)",
      className,
      "$context",
      "$x"
    );
    return builder.build();
  }
}
