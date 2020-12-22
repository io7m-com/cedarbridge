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

package com.io7m.cedarbridge.codegen.java.internal.protocols;

import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaClassGeneratorType;
import com.io7m.cedarbridge.codegen.java.internal.CBCGJavaTypeNames;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.runtime.api.CBProtocolType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolDeclarationType;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.DEFAULT;
import static javax.lang.model.element.Modifier.PUBLIC;

public final class CBCGProtocolInterfaceGenerator
  implements CBCGJavaClassGeneratorType<CBProtocolDeclarationType>
{
  public CBCGProtocolInterfaceGenerator()
  {

  }

  private static MethodSpec createNameMethod(
    final CBProtocolDeclarationType proto)
  {
    final var pack = proto.owner();

    return MethodSpec.methodBuilder("protocolName")
      .addModifiers(DEFAULT, PUBLIC)
      .addAnnotation(Override.class)
      .returns(String.class)
      .addStatement(
        "return $S",
        String.format("%s.%s", pack.name(), proto.name()))
      .build();
  }

  @Override
  public Path execute(
    final CBSPICodeGeneratorConfiguration configuration,
    final CBProtocolDeclarationType proto)
    throws CBSPICodeGeneratorException
  {
    Objects.requireNonNull(configuration, "configuration");
    Objects.requireNonNull(proto, "proto");

    final var pack = proto.owner();
    final var className = CBCGJavaTypeNames.protoInterfaceNameOf(proto);

    final var classBuilder = TypeSpec.interfaceBuilder(className);
    classBuilder.addSuperinterface(CBProtocolType.class);
    classBuilder.addModifiers(PUBLIC);
    classBuilder.addMethod(createNameMethod(proto));
    classBuilder.addJavadoc(
      "Protocol {@code $L.$L}",
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
