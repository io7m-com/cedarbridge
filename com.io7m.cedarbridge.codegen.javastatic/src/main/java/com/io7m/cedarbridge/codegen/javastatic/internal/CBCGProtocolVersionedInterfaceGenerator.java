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
import com.io7m.cedarbridge.runtime.api.CBSerializationContextType;
import com.io7m.cedarbridge.runtime.api.CBSerializerMethod;
import com.io7m.cedarbridge.schema.compiled.CBProtocolVersionDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.jodist.ClassName;
import com.io7m.jodist.CodeBlock;
import com.io7m.jodist.JavaFile;
import com.io7m.jodist.MethodSpec;
import com.io7m.jodist.TypeName;
import com.io7m.jodist.TypeSpec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.SEALED;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * A generator of Java interface types for protocol version declarations.
 */

public final class CBCGProtocolVersionedInterfaceGenerator
  implements CBCGJavaClassGeneratorType<CBProtocolVersionDeclarationType>
{
  /**
   * A generator of Java interface types for protocol version declarations.
   */

  public CBCGProtocolVersionedInterfaceGenerator()
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
    final var className =
      CBCGJavaTypeNames.protoVersionedInterfaceNameOf(proto);

    final var classBuilder = TypeSpec.interfaceBuilder(className);
    classBuilder.addSuperinterface(CBCGJavaTypeNames.protoInterfaceNameOf(owner));
    classBuilder.addModifiers(PUBLIC, SEALED);
    classBuilder.addJavadoc(
      "Protocol {@code $L.$L}, version {@code $L}.",
      pack.name(),
      owner.name(),
      proto.version()
    );

    /*
     * Separately track the types contained directly within the protocol,
     * and the types that result from expanding all the variant cases.
     *
     * The variant cases need to be added in the "permits" clause, but should
     * not be referenced in serialize/deserialize methods.
     */

    final var directTypes = new ArrayList<TypeName>();
    final var allTypes = new ArrayList<TypeName>();

    for (final var t : proto.typesInOrder()) {
      final var td = t.declaration();
      directTypes.add(CBCGJavaTypeNames.dataTypeNameOf(td));
      allTypes.add(CBCGJavaTypeNames.dataTypeNameOf(td));

      if (td instanceof CBVariantType var) {
        for (final var c : var.cases()) {
          allTypes.add(CBCGJavaTypeNames.dataClassNameOfCase(c));
        }
      }
    }

    directTypes.sort(Comparator.comparing(TypeName::toString));
    allTypes.sort(Comparator.comparing(TypeName::toString));

    classBuilder.addPermittedSubclasses(allTypes);
    classBuilder.addMethod(createSerializeMethod(className, directTypes));
    classBuilder.addMethod(createDeserializeMethod(className, directTypes));

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

  private static MethodSpec createDeserializeMethod(
    final ClassName className,
    final ArrayList<TypeName> types)
  {
    final var builder = MethodSpec.methodBuilder("deserialize");
    builder.addModifiers(STATIC, PUBLIC);
    builder.addAnnotation(CBDeserializerMethod.class);
    builder.addException(IOException.class);
    builder.returns(className);
    builder.addParameter(
      TypeName.get(CBSerializationContextType.class),
      "$context",
      FINAL
    );

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

    for (int index = 0; index < types.size(); ++index) {
      final var type = types.get(index);
      final var switchCase =
        switchStatement.beginControlFlow(
          "case $L:",
          Integer.valueOf(index)
        );

      switchCase.addStatement(
        "return $T.deserialize($L)",
        type,
        "$context"
      );
      switchCase.endControlFlow();
    }

    {
      final var defaultCase =
        switchStatement.beginControlFlow("default: ");

      defaultCase.addStatement(
        "throw $L.errorUnrecognizedVariantIndex($T.class, $L)",
        "$context",
        className,
        "$i"
      );
      defaultCase.endControlFlow();
    }

    switchStatement.endControlFlow();

    /*
     * Generate JavaDoc for the method.
     */

    builder.addJavadoc("Deserialize a value of type $T.\n", className);
    builder.addJavadoc("@param $L The serialization context.\n", "$context");
    builder.addJavadoc("@return A value of type $T.\n", className);
    return builder.build();
  }

  private static MethodSpec createSerializeMethod(
    final ClassName className,
    final ArrayList<TypeName> types)
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
      className,
      "$x",
      FINAL
    );

    for (int index = 0; index < types.size(); ++index) {
      final var type = types.get(index);
      final var block = CodeBlock.builder();
      block.beginControlFlow(
        "if ($L instanceof $T $L)",
        "$x",
        type,
        "$y"
      );

      block.addStatement(
        "$L.writeVariantIndex($L)",
        "$context",
        Integer.toUnsignedString(index)
      );
      block.addStatement(
        "$T.serialize($L, $L)",
        type,
        "$context",
        "$y"
      );

      block.addStatement("return");
      block.endControlFlow();
      builder.addCode(block.build());
    }

    builder.addStatement(
      "throw $L.errorUnrecognizedVariantCaseClass($T.class, $L.getClass())",
      "$context",
      className,
      "$x"
    );

    /*
     * Generate JavaDoc for the method.
     */

    builder.addJavadoc("Serialize a value of type $T.\n", className);
    builder.addJavadoc("@param $L The serialization context.\n", "$context");
    builder.addJavadoc("@param $L The value to be serialized.\n", "$x");
    return builder.build();
  }
}
