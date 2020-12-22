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
import com.io7m.cedarbridge.codegen.java.internal.protocols.CBCGProtocolInterfaceGenerator;
import com.io7m.cedarbridge.codegen.java.internal.protocols.CBCGProtocolVersionedInterfaceGenerator;
import com.io7m.cedarbridge.codegen.java.internal.protocols.CBCGProtocolVersionedSerializerClassGenerator;
import com.io7m.cedarbridge.codegen.java.internal.protocols.CBCGProtocolVersionedSerializerFactoryClassGenerator;
import com.io7m.cedarbridge.codegen.java.internal.serializer_factories.CBCGJavaSerializerFactoryGenerator;
import com.io7m.cedarbridge.codegen.java.internal.serializers.CBCGJavaSerializerGenerator;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorResult;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class CBCGJava implements CBSPICodeGeneratorType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBCGJava.class);

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

    final var protos = pack.protocols();
    for (final var entry : protos.entrySet()) {
      final var proto = entry.getValue();

      final var wroteProto =
        new CBCGProtocolInterfaceGenerator()
          .execute(this.configuration, proto);

      LOG.debug("generate: {}", wroteProto);
      resultBuilder.addCreatedFiles(wroteProto);

      for (final var version : proto.versions().values()) {
        final var wroteInterface =
          new CBCGProtocolVersionedInterfaceGenerator()
            .execute(this.configuration, version);
        final var wroteSerializer =
          new CBCGProtocolVersionedSerializerClassGenerator()
            .execute(this.configuration, version);
        final var wroteSerializerFactory =
          new CBCGProtocolVersionedSerializerFactoryClassGenerator()
            .execute(this.configuration, version);

        LOG.debug("generate: {}", wroteInterface);
        LOG.debug("generate: {}", wroteSerializer);
        LOG.debug("generate: {}", wroteSerializerFactory);

        resultBuilder.addCreatedFiles(wroteInterface);
        resultBuilder.addCreatedFiles(wroteSerializer);
        resultBuilder.addCreatedFiles(wroteSerializerFactory);
      }
    }

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

      LOG.debug("generate: {}", wroteData);
      LOG.debug("generate: {}", wroteSerializer);
      LOG.debug("generate: {}", wroteSerializerFactory);

      resultBuilder.addCreatedFiles(wroteData);
      resultBuilder.addCreatedFiles(wroteSerializer);
      resultBuilder.addCreatedFiles(wroteSerializerFactory);
    }

    return resultBuilder.build();
  }
}
