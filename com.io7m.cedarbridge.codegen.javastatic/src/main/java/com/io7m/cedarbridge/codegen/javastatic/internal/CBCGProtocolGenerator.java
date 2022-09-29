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
import com.io7m.cedarbridge.runtime.api.CBAbstractProtocol;
import com.io7m.cedarbridge.runtime.api.CBProtocolType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolDeclarationType;
import com.io7m.jodist.ClassName;
import com.io7m.jodist.CodeBlock;
import com.io7m.jodist.JavaFile;
import com.io7m.jodist.MethodSpec;
import com.io7m.jodist.ParameterizedTypeName;
import com.io7m.jodist.TypeSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.protoInterfaceNameOf;
import static com.io7m.cedarbridge.codegen.javastatic.internal.CBCGJavaTypeNames.protoNameOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * A generator of Java classes for protocols.
 */

public final class CBCGProtocolGenerator
  implements CBCGJavaClassGeneratorType<CBProtocolDeclarationType>
{
  /**
   * A generator of Java classes for protocols.
   */

  public CBCGProtocolGenerator()
  {

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

    final var classBuilder =
      TypeSpec.classBuilder(protoNameOf(proto));
    final var messageClassName =
      protoInterfaceNameOf(proto);

    classBuilder.superclass(ParameterizedTypeName.get(
      ClassName.get(CBAbstractProtocol.class),
      messageClassName
    ));
    classBuilder.addSuperinterface(
      ParameterizedTypeName.get(
        ClassName.get(CBProtocolType.class),
        messageClassName)
    );
    classBuilder.addModifiers(PUBLIC, FINAL);
    classBuilder.addJavadoc(
      "Protocol {@code $L.$L}.",
      pack.name(),
      proto.name()
    );

    classBuilder.addMethod(createConstructor(proto, messageClassName));

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

  private static MethodSpec createConstructor(
    final CBProtocolDeclarationType proto,
    final ClassName messageClassName)
  {
    final var serializers =
      proto.versions()
        .values()
        .stream()
        .map(CBCGJavaTypeNames::protoVersionedSerializerNameOf)
        .map(n -> CodeBlock.of("$T.widen(new $T())", CBAbstractProtocol.class, n))
        .map(CodeBlock::toString)
        .collect(Collectors.joining(", "));

    final var builder = MethodSpec.constructorBuilder();
    builder.addJavadoc("Construct a protocol.");
    builder.addModifiers(PUBLIC);
    builder.addStatement(
      "super($T.class, $S, $S, $T.of($L))",
      messageClassName,
      proto.owner().name(),
      proto.name(),
      List.class,
      serializers
    );
    return builder.build();
  }
}
