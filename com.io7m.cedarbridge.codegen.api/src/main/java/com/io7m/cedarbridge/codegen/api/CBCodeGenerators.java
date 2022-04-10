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

package com.io7m.cedarbridge.codegen.api;

import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorConfiguration;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorDescription;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorException;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorFactoryType;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorResult;
import com.io7m.cedarbridge.codegen.spi.CBSPICodeGeneratorType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * A directory of code generators.
 */

public final class CBCodeGenerators implements CBCodeGeneratorDirectoryType
{
  private final List<CBCodeGeneratorFactoryType> generators;

  /**
   * Construct a directory using the given code generators.
   *
   * @param inGenerators The generators
   */

  public CBCodeGenerators(
    final List<CBCodeGeneratorFactoryType> inGenerators)
  {
    this.generators =
      List.copyOf(Objects.requireNonNull(inGenerators, "generators"));
  }

  /**
   * Construct a directory, loading available generators using {@link ServiceLoader}.
   */

  public CBCodeGenerators()
  {
    this(fromServiceLoader());
  }

  private static List<CBCodeGeneratorFactoryType> fromServiceLoader()
  {
    final var iter =
      ServiceLoader.load(CBSPICodeGeneratorFactoryType.class)
        .iterator();

    final var factories = new ArrayList<CBCodeGeneratorFactoryType>();
    while (iter.hasNext()) {
      final var factory = iter.next();
      factories.add(new CodeGeneratorFactory(factory));
    }
    return factories;
  }

  private static CBCodeGeneratorResult convertResult(
    final CBSPICodeGeneratorResult result)
  {
    return CBCodeGeneratorResult.builder()
      .setCreatedFiles(result.createdFiles())
      .build();
  }

  private static CBCodeGeneratorDescription convertDescription(
    final CBSPICodeGeneratorDescription description)
  {
    return CBCodeGeneratorDescription.builder()
      .setDescription(description.description())
      .setId(description.id())
      .setLanguageName(description.languageName())
      .build();
  }

  private static CBSPICodeGeneratorConfiguration convertConfiguration(
    final CBCodeGeneratorConfiguration configuration)
  {
    return CBSPICodeGeneratorConfiguration.builder()
      .setOutputDirectory(configuration.outputDirectory())
      .build();
  }

  @Override
  public List<CBCodeGeneratorFactoryType> availableGenerators()
  {
    return this.generators;
  }

  private static final class CodeGeneratorFactory
    implements CBCodeGeneratorFactoryType
  {
    private final CBSPICodeGeneratorFactoryType factory;
    private CBCodeGeneratorDescription description;

    CodeGeneratorFactory(
      final CBSPICodeGeneratorFactoryType inFactory)
    {
      this.factory =
        Objects.requireNonNull(inFactory, "factory");
      this.description =
        convertDescription(inFactory.description());
    }

    @Override
    public CBCodeGeneratorDescription description()
    {
      return this.description;
    }

    @Override
    public CBCodeGeneratorType createGenerator(
      final CBCodeGeneratorConfiguration configuration)
    {
      return new CodeGenerator(
        this.factory.createGenerator(convertConfiguration(configuration))
      );
    }
  }

  private static final class CodeGenerator implements CBCodeGeneratorType
  {
    private final CBSPICodeGeneratorType generator;

    private CodeGenerator(
      final CBSPICodeGeneratorType inGenerator)
    {
      this.generator =
        Objects.requireNonNull(inGenerator, "generator");
    }

    @Override
    public CBCodeGeneratorResult execute(
      final CBPackageType pack)
      throws CBCodeGeneratorException
    {
      try {
        return convertResult(this.generator.execute(pack));
      } catch (final CBSPICodeGeneratorException e) {
        throw new CBCodeGeneratorException(e);
      }
    }
  }
}
