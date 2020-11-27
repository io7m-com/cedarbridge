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

package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.schema.parser.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.internal.CBPackageDeclarationParser;
import com.io7m.cedarbridge.schema.parser.internal.CBParseContext;
import com.io7m.cedarbridge.schema.parser.internal.CBParserStrings;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jsx.api.serializer.JSXSerializerType;
import com.io7m.jsx.serializer.JSXSerializerTrivialSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.io7m.cedarbridge.tests.CBTestExpressions.expression;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBPackageDeclarationParserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBPackageDeclarationParserTest.class);

  private CBStringsType strings;
  private CBPackageDeclarationParser parser;
  private CBParseContext context;
  private JSXSerializerType serializer;
  private ArrayList<CBError> errors;

  private CBError takeError()
  {
    return this.errors.remove(0);
  }

  private void addError(
    final CBError error)
  {
    LOG.error("{}", error.message());
    this.errors.add(error);
  }

  @BeforeEach
  public void setup()
  {
    this.errors = new ArrayList<CBError>();
    this.strings = CBParserStrings.create();
    this.serializer = new JSXSerializerTrivialSupplier().create();
    this.parser = new CBPackageDeclarationParser();
    this.context =
      new CBParseContext(this.strings, this.serializer, this::addError);
  }

  @Test
  public void testPackageOK0()
    throws Exception
  {
    final var packageV =
      this.parser.parse(
        this.context.current(),
        expression("[package com.io7m.cedarbridge]")
      );

    assertEquals("com.io7m.cedarbridge", packageV.name().text());
  }

  @Test
  public void testPackageBad0()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorPackageInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testPackageBad1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[import]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorPackageInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testPackageBad2()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorPackageInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testPackageBad3()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record x]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorPackageKeyword", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testPackageBad4()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[package com.io7m.cedarbridge X]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorPackageInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testPackageBad5()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[package X]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorPackageNameInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testPackageBad6()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[[] com.io7m.cedarbridge]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorPackageKeyword", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }
}
