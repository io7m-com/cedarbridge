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

package com.io7m.cedarbridge.codegen.java.internal;

import com.io7m.cedarbridge.codegen.java.internal.data_classes.CBCGDataClassGenerator;
import com.io7m.cedarbridge.codegen.java.internal.serializer_factories.CBCGJavaSerializerFactoryGenerator;
import com.io7m.cedarbridge.codegen.java.internal.serializers.CBCGJavaSerializerGenerator;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorResult;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;

import java.util.Objects;

public final class CBCGJava implements CBSPICodeGeneratorType
{
  private final CBSPICodeGeneratorConfiguration configuration;

  public CBCGJava(
    final CBSPICodeGeneratorConfiguration inConfiguration)
  {
    this.configuration =
      Objects.requireNonNull(inConfiguration, "configuration");
  }

  @Override
  public CBSPICodeGeneratorResult execute(
    final CBPackageType pack)
    throws CBSPICodeGeneratorException
  {
    Objects.requireNonNull(pack, "pack");

    final var resultBuilder =
      CBSPICodeGeneratorResult.builder();

    final var types = pack.types();
    for (final var entry : types.entrySet()) {
      final var type =
        entry.getValue();
      final var wroteData =
        new CBCGDataClassGenerator()
          .execute(this.configuration, type);
      final var wroteSerializer =
        new CBCGJavaSerializerGenerator()
          .execute(this.configuration, type);
      final var wroteSerializerFactory =
        new CBCGJavaSerializerFactoryGenerator()
          .execute(this.configuration, type);

      resultBuilder.addCreatedFiles(wroteData);
      resultBuilder.addCreatedFiles(wroteSerializer);
      resultBuilder.addCreatedFiles(wroteSerializerFactory);
    }

    return resultBuilder.build();
  }
}
