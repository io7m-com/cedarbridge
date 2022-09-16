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


package com.io7m.cedarbridge.bridgedoc.xhtml;

import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorConfiguration;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorDescription;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorFactoryType;
import com.io7m.cedarbridge.bridgedoc.spi.CBSPIDocGeneratorType;
import com.io7m.cedarbridge.bridgedoc.xhtml.internal.CBXGenerator;

/**
 * An XHTML generator.
 */

public final class CBXGeneratorFactory implements CBSPIDocGeneratorFactoryType
{
  private static final CBSPIDocGeneratorDescription DESCRIPTION =
    new CBSPIDocGeneratorDescription(
      "com.io7m.cedarbridge.xhtml",
      "XHTML",
      "A generator that produces XHTML documentation."
    );

  /**
   * An XHTML generator.
   */

  public CBXGeneratorFactory()
  {

  }

  @Override
  public CBSPIDocGeneratorDescription description()
  {
    return DESCRIPTION;
  }

  @Override
  public CBSPIDocGeneratorType createGenerator(
    final CBSPIDocGeneratorConfiguration configuration)
  {
    return new CBXGenerator(configuration);
  }
}
