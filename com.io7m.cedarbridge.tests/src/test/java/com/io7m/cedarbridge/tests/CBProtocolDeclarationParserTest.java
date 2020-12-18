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
import com.io7m.cedarbridge.schema.ast.CBASTPackageDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolVersion;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.internal.CBPackageDeclarationParser;
import com.io7m.cedarbridge.schema.parser.internal.CBParseContext;
import com.io7m.cedarbridge.schema.parser.internal.CBParserStrings;
import com.io7m.cedarbridge.schema.parser.internal.CBProtocolParser;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBProtocolDeclarationParserTest extends
  CBElementParserContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBProtocolDeclarationParserTest.class);

  private CBStringsType strings;
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
  }

  private CBASTProtocolDeclaration parse(
    final String text)
    throws CBParseFailedException
  {
    final var parser = new CBProtocolParser();
    final var expr = this.expression(text);
    final var context = new CBParseContext(
      this.strings,
      this.source,
      this::addError);
    return parser.parse(context.current(), expr);
  }

  @Test
  public void testProtocolOK0()
    throws Exception
  {
    final var declV = this.parse("[protocol P]");
    assertEquals("P", declV.name().text());
    assertEquals(0, declV.versions().size());
  }

  @Test
  public void testProtocolOK1()
    throws Exception
  {
    final var declV = this.parse("[protocol P [version 0 T]]");
    assertEquals("P", declV.name().text());
    assertEquals(1, declV.versions().size());
    final var v0 = declV.versions().get(0);
    assertEquals(0L, v0.version().longValue());
    assertEquals(1, v0.types().size());
    assertEquals("T", v0.types().get(0).text());
  }

  @Test
  public void testProtocolBad0()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[]");
    });
    LOG.debug("", ex);
    assertEquals("errorProtocolInvalidDeclaration", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testProtocolBad1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[protocol]");
    });
    LOG.debug("", ex);
    assertEquals("errorProtocolInvalidDeclaration", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testProtocolBad2()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[protocol K [version]]");
    });
    LOG.debug("", ex);
    assertEquals("errorProtocolVersionInvalidDeclaration", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testProtocolBad3()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[protocol K [version z]]");
    });
    LOG.debug("", ex);
    assertEquals("errorProtocolVersionInvalidDeclaration", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testProtocolBad4()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[protocol K [version 3 4]]");
    });
    LOG.debug("", ex);
    assertEquals("errorTypeNameInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }
}
