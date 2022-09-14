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

package com.io7m.cedarbridge.schema.compiler.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerException;
import com.io7m.cedarbridge.schema.loader.api.CBLoadFailedException;
import com.io7m.cedarbridge.schema.loader.api.CBLoaderType;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jaffirm.core.Postconditions;
import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jlexing.core.LexicalPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The default loader implementation.
 */

public final class CBLoader implements CBLoaderType
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBLoader.class);

  private final LinkedList<PackageImport> imports;
  private final CBStringsType strings;
  private final List<Path> includePaths;
  private final Consumer<CBError> errors;
  private final HashMap<String, CBPackageType> packages;
  private final CBSchemaCompilerInternalFactory factory;

  /**
   * The default loader implementation.
   *
   * @param inErrors        An error consumer
   * @param inIncludePaths  The list of include paths
   * @param internalFactory An internal compiler factory
   */

  public CBLoader(
    final CBSchemaCompilerInternalFactory internalFactory,
    final List<Path> inIncludePaths,
    final Consumer<CBError> inErrors)
  {
    this.factory =
      Objects.requireNonNull(internalFactory, "internalFactory");
    this.includePaths =
      Objects.requireNonNull(inIncludePaths, "includePaths");
    this.errors =
      Objects.requireNonNull(inErrors, "errors");

    this.strings = CBSchemaCompilerStrings.create();
    this.imports = new LinkedList<>();
    this.packages = new HashMap<>();

    this.includePaths.forEach(p -> {
      Preconditions.checkPreconditionV(
        p, p.isAbsolute(), "Path must be absolute"
      );
    });
  }

  private static Path sourceFileOf(
    final Path includeBase,
    final List<String> components)
  {
    Preconditions.checkPreconditionV(
      includeBase,
      includeBase.isAbsolute(),
      "Path must be absolute");

    var transformed = includeBase;
    for (int index = 0; index < components.size() - 1; ++index) {
      transformed = transformed.resolve(components.get(index));
    }

    final var result =
      transformed.resolve(components.get(components.size() - 1) + ".cbs")
        .toAbsolutePath()
        .normalize();

    Postconditions.checkPostconditionV(
      result,
      result.isAbsolute(),
      "Path must be absolute");
    return result;
  }

  @Override
  public void register(
    final CBPackageType pack)
  {
    Objects.requireNonNull(pack, "pack");

    if (this.packages.containsKey(pack.name())) {
      throw new IllegalStateException(String.format(
        "Package %s is already registered",
        pack.name())
      );
    }

    LOG.debug("register package: {}", pack.name());
    this.packages.put(pack.name(), pack);
  }

  @Override
  public CBPackageType load(
    final String from,
    final String name)
    throws CBLoadFailedException
  {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(name, "name");

    try {
      if (this.isAlreadyInImportPath(name)) {
        this.imports.push(new PackageImport(from, name));
        this.errors.accept(this.errorCircularImport(name));
        throw new CBLoadFailedException();
      }

      this.imports.push(new PackageImport(from, name));
      final var existing = this.packages.get(name);
      if (existing != null) {
        return existing;
      }

      return this.tryCompilePackage(name);
    } finally {
      this.imports.pop();
    }
  }

  private CBPackageType tryCompilePackage(
    final String name)
    throws CBLoadFailedException
  {
    final var source = this.findSourceFile(name);

    final var configuration =
      new CBSchemaCompilerConfiguration(
        this.includePaths,
        List.of(source)
      );

    final var compiler =
      this.factory.createNewCompiler(
        this.errors,
        configuration,
        this
      );

    try {
      CBPackageType foundPackage = null;
      final var newPackages = compiler.execute();
      for (final var pack : newPackages.compiledPackages()) {
        final var newName = pack.name();
        this.register(pack);
        if (Objects.equals(newName, name)) {
          foundPackage = pack;
        }
      }

      if (foundPackage == null) {
        this.errors.accept(
          this.errorUnexpectedPackage(
            name, source, newPackages.compiledPackages()
          )
        );
        throw new CBLoadFailedException();
      }

      return foundPackage;
    } catch (final CBSchemaCompilerException e) {
      throw new CBLoadFailedException();
    }
  }

  private Path findSourceFile(
    final String name)
    throws CBLoadFailedException
  {
    final var paths = this.possibleFilePathsOf(name);
    for (final var path : paths) {
      if (Files.isRegularFile(path)) {
        return path;
      }
    }

    this.errors.accept(this.errorMissingFile(name, paths));
    throw new CBLoadFailedException();
  }

  private List<Path> possibleFilePathsOf(
    final String name)
  {
    final var components = List.of(name.split("\\."));
    return this.includePaths.stream()
      .map(base -> sourceFileOf(base, components))
      .map(Path::toAbsolutePath)
      .map(Path::normalize)
      .collect(Collectors.toList());
  }

  private CBError errorUnexpectedPackage(
    final String name,
    final Path file,
    final List<CBPackageType> newPackages)
  {
    final var lex =
      LexicalPosition.<URI>of(-1, -1, Optional.empty());

    final var names =
      newPackages.stream()
        .map(CBPackageType::name)
        .collect(Collectors.joining(", "));

    final var message =
      this.strings.format("errorFileUnexpectedPackage", name, file, names);

    return new CBError(
      lex,
      CBError.Severity.ERROR,
      new CBLoadFailedException(),
      "loadUnexpectedPackage",
      message
    );
  }

  private CBError errorMissingFile(
    final String name,
    final List<Path> paths)
  {
    final var lex =
      LexicalPosition.<URI>of(-1, -1, Optional.empty());

    final var pathText = new StringBuilder();
    for (final var path : paths) {
      pathText.append("    ");
      pathText.append(path);
      pathText.append(System.lineSeparator());
    }

    return new CBError(
      lex,
      CBError.Severity.ERROR,
      new CBLoadFailedException(),
      "loadMissingFile",
      this.strings.format("errorMissingFile", name, pathText)
    );
  }


  private CBError errorCircularImport(
    final String name)
  {
    final var lex =
      LexicalPosition.<URI>of(-1, -1, Optional.empty());

    final var pathText = new StringBuilder();
    for (final var importV : this.imports) {
      pathText.append("    ");
      pathText.append(importV.source);
      pathText.append(" → ");
      pathText.append(importV.target);
      pathText.append(System.lineSeparator());
    }

    return new CBError(
      lex,
      CBError.Severity.ERROR,
      new CBLoadFailedException(),
      "loadCircularImport",
      this.strings.format("errorCircularImport", name, pathText)
    );
  }

  private boolean isAlreadyInImportPath(
    final String name)
  {
    for (final var importV : this.imports) {
      if (Objects.equals(importV.source, name)
        || Objects.equals(importV.target, name)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void close()
  {

  }

  private static final class PackageImport
  {
    private final String source;
    private final String target;

    private PackageImport(
      final String inSource,
      final String inTarget)
    {
      this.source = Objects.requireNonNull(inSource, "source");
      this.target = Objects.requireNonNull(inTarget, "target");
    }
  }
}
