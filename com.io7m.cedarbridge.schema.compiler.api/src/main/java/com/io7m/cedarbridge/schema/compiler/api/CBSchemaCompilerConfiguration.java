/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.cedarbridge.schema.compiler.api;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * The compiler configuration.
 *
 * @param includeDirectories The list of directories within which to search for
 *                           packages
 * @param filesToCompile     The list of files to compile
 */

public record CBSchemaCompilerConfiguration(
  List<Path> includeDirectories,
  List<Path> filesToCompile)
{
  /**
   * The compiler configuration.
   *
   * @param includeDirectories The list of directories within which to search
   *                           for packages
   * @param filesToCompile     The list of files to compile
   */

  public CBSchemaCompilerConfiguration
  {
    Objects.requireNonNull(includeDirectories, "includeDirectories");
    Objects.requireNonNull(filesToCompile, "filesToCompile");

    includeDirectories
      .forEach(CBSchemaCompilerConfiguration::checkAbsolute);
    filesToCompile
      .forEach(CBSchemaCompilerConfiguration::checkAbsolute);
  }

  private static void checkAbsolute(
    final Path directory)
  {
    if (!directory.isAbsolute()) {
      throw new IllegalArgumentException(
        String.format("Path %s must be an absolute path", directory)
      );
    }
  }
}
