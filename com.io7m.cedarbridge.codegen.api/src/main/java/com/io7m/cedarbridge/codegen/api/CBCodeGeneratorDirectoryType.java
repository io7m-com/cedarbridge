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

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * A directory of code generators.
 */

public interface CBCodeGeneratorDirectoryType
{
  /**
   * @return The available code generators
   */

  List<CBCodeGeneratorFactoryType> availableGenerators();

  /**
   * Find a code generator that has the given language name.
   *
   * @param languageName The language name (case insensitive)
   *
   * @return A code generator, if any
   */

  default Optional<CBCodeGeneratorFactoryType> findByLanguageName(
    final String languageName)
  {
    Objects.requireNonNull(languageName, "languageName");

    final var languageUpper = languageName.toUpperCase(Locale.ROOT);
    return this.availableGenerators()
      .stream()
      .filter(g -> languageNameMatches(g, languageUpper))
      .findFirst();
  }

  private static boolean languageNameMatches(
    final CBCodeGeneratorFactoryType factory,
    final String languageName)
  {
    final var descriptionName =
      factory.description()
        .languageName()
        .toUpperCase(Locale.ROOT);

    return descriptionName.equals(languageName);
  }
}
