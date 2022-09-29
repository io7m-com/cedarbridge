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
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorResult;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A Java code generator.
 */

public final class CBCGJava implements CBSPICodeGeneratorType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBCGJava.class);

  private final CBSPICodeGeneratorConfiguration configuration;

  /**
   * A Java code generator.
   *
   * @param inConfiguration The code generator configuration
   */

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

    final var createdFiles = new ArrayList<Path>();
    this.generateProtocolClasses(pack, createdFiles);
    this.generateTypeClasses(pack, createdFiles);
    return new CBSPICodeGeneratorResult(createdFiles);
  }

  private void generateTypeClasses(
    final CBPackageType pack,
    final ArrayList<Path> createdFiles)
    throws CBSPICodeGeneratorException
  {
    final var types = pack.types();
    for (final var entry : types.entrySet()) {
      final var type =
        entry.getValue();
      final var wroteData =
        new CBCGDataClassGenerator()
          .execute(this.configuration, pack.name(), type);

      LOG.debug("generate: {}", wroteData);
      createdFiles.add(wroteData);
    }
  }

  private void generateProtocolClasses(
    final CBPackageType pack,
    final ArrayList<Path> createdFiles)
    throws CBSPICodeGeneratorException
  {
    final var protos = pack.protocols();
    for (final var entry : protos.entrySet()) {
      final var proto = entry.getValue();

      final var wroteProtoInterface =
        new CBCGProtocolInterfaceGenerator()
          .execute(this.configuration, pack.name(), proto);

      LOG.debug("generate: {}", wroteProtoInterface);
      createdFiles.add(wroteProtoInterface);

      final var wroteClass =
        new CBCGProtocolGenerator()
          .execute(this.configuration, pack.name(), proto);

      LOG.debug("generate: {}", wroteClass);
      createdFiles.add(wroteClass);

      for (final var version : proto.versions().values()) {
        final var wroteInterface =
          new CBCGProtocolVersionedInterfaceGenerator()
            .execute(this.configuration, pack.name(), version);
        final var wroteSerializer =
          new CBCGProtocolMessageSerializerGenerator()
            .execute(this.configuration, pack.name(), version);

        LOG.debug("generate: {}", wroteInterface);
        LOG.debug("generate: {}", wroteSerializer);

        createdFiles.add(wroteInterface);
        createdFiles.add(wroteSerializer);
      }
    }
  }
}
