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

package com.io7m.cedarbridge.codegen.spi;

import java.util.Objects;

/**
 * A description of a code generator.
 *
 * @param id           The ID of the generator
 * @param languageName The name of the target language
 * @param description  A humanly-readable description of the generator
 */

public record CBSPICodeGeneratorDescription(
  String id,
  String languageName,
  String description)
{
  /**
   * A description of a code generator.
   *
   * @param id           The ID of the generator
   * @param languageName The name of the target language
   * @param description  A humanly-readable description of the generator
   */

  public CBSPICodeGeneratorDescription
  {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(languageName, "languageName");
    Objects.requireNonNull(description, "description");
  }
}
