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

package com.io7m.cedarbridge.bridgedoc.api;

import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorConfiguration;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorDescription;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorException;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorFactoryType;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorResult;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * A directory of documentation generators.
 */

public final class CBDocGenerators implements CBDocGeneratorDirectoryType
{
  private final List<CBDocGeneratorFactoryType> generators;

  /**
   * Construct a directory using the given documentation generators.
   *
   * @param inGenerators The generators
   */

  public CBDocGenerators(
    final List<CBDocGeneratorFactoryType> inGenerators)
  {
    this.generators =
      List.copyOf(Objects.requireNonNull(inGenerators, "generators"));
  }

  /**
   * Construct a directory, loading available generators using {@link ServiceLoader}.
   */

  public CBDocGenerators()
  {
    this(fromServiceLoader());
  }

  private static List<CBDocGeneratorFactoryType> fromServiceLoader()
  {
    final var iter =
      ServiceLoader.load(CBSPIDocGeneratorFactoryType.class)
        .iterator();

    final var factories = new ArrayList<CBDocGeneratorFactoryType>();
    while (iter.hasNext()) {
      final var factory = iter.next();
      factories.add(new DocGeneratorFactory(factory));
    }
    return factories;
  }

  private static CBDocGeneratorResult convertResult(
    final CBSPIDocGeneratorResult result)
  {
    return new CBDocGeneratorResult(result.createdFiles());
  }

  private static CBDocGeneratorDescription convertDescription(
    final CBSPIDocGeneratorDescription description)
  {
    return new CBDocGeneratorDescription(
      description.id(),
      description.languageName(),
      description.description()
    );
  }

  private static CBSPIDocGeneratorConfiguration convertConfiguration(
    final CBDocGeneratorConfiguration configuration)
  {
    return new CBSPIDocGeneratorConfiguration(configuration.outputDirectory());
  }

  @Override
  public List<CBDocGeneratorFactoryType> availableGenerators()
  {
    return this.generators;
  }

  private static final class DocGeneratorFactory
    implements CBDocGeneratorFactoryType
  {
    private final CBSPIDocGeneratorFactoryType factory;
    private CBDocGeneratorDescription description;

    DocGeneratorFactory(
      final CBSPIDocGeneratorFactoryType inFactory)
    {
      this.factory =
        Objects.requireNonNull(inFactory, "factory");
      this.description =
        convertDescription(inFactory.description());
    }

    @Override
    public CBDocGeneratorDescription description()
    {
      return this.description;
    }

    @Override
    public CBDocGeneratorType createGenerator(
      final CBDocGeneratorConfiguration configuration)
    {
      return new DocGenerator(
        this.factory.createGenerator(convertConfiguration(configuration))
      );
    }
  }

  private static final class DocGenerator implements CBDocGeneratorType
  {
    private final CBSPIDocGeneratorType generator;

    private DocGenerator(
      final CBSPIDocGeneratorType inGenerator)
    {
      this.generator =
        Objects.requireNonNull(inGenerator, "generator");
    }

    @Override
    public CBDocGeneratorResult execute(
      final CBPackageType pack)
      throws CBDocGeneratorException
    {
      try {
        return convertResult(this.generator.execute(pack));
      } catch (final CBSPIDocGeneratorException e) {
        throw new CBDocGeneratorException(e);
      }
    }
  }
}
