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
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.api.CBParsed;
import com.io7m.cedarbridge.schema.parser.internal.CBParseContext;
import com.io7m.cedarbridge.schema.parser.internal.CBParserStrings;
import com.io7m.cedarbridge.schema.parser.internal.CBVariantParser;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBVariantParserTest extends CBElementParserContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBVariantParserTest.class);

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

  private CBASTTypeVariant parse(
    final String text)
    throws CBParseFailedException
  {
    final var parser = new CBVariantParser();
    final var expr = this.expression(text);
    final var context = new CBParseContext(
      this.strings,
      this.source,
      this::addError);
    return parser.parse(context.current(), expr);
  }

  @BeforeEach
  public void setup()
  {
    this.errors = new ArrayList<CBError>();
    this.strings = CBParserStrings.create();
  }

  @Test
  public void testEmptyVariant()
    throws Exception
  {
    final var variant =
      this.parse("[variant Q]");

    assertEquals(0, variant.cases().size());
    assertEquals("Q", variant.name().text());
    assertEquals(0, variant.parameters().size());
  }

  @Test
  public void testOption()
    throws Exception
  {
    final var variant =
      this.parse(
        "[variant " +
          "Option " +
          "(parameter A) " +
          "(case Some [field value A]) " +
          "(case None) " +
          "]");

    assertEquals(2, variant.cases().size());
    final var record0 = variant.cases().get(0);
    assertEquals("Some", record0.name().text());
    final var record1 = variant.cases().get(1);
    assertEquals("None", record1.name().text());

    assertEquals("Option", variant.name().text());

    assertEquals(1, variant.parameters().size());
    final var tp0 = variant.parameters().get(0);
    assertEquals("A", tp0.text());
  }

  @Test
  public void testNameBad1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant x]");
    });
    LOG.debug("", ex);
    assertEquals("errorTypeNameInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNameBad2()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant (X B)]");
    });
    LOG.debug("", ex);
    assertEquals("errorUnexpectedExpressionForm", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testParametersBad0()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X (parameter)]");
    });
    LOG.debug("", ex);
    assertEquals(
      "errorVariantInvalidTypeParameter",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testParametersBad1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X (parameter A B)]");
    });
    LOG.debug("", ex);
    assertEquals(
      "errorVariantInvalidTypeParameter",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testParametersBad2()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X (parameter a)]");
    });
    LOG.debug("", ex);
    assertEquals("errorTypeParameterNameInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense0()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X (what)]");
    });
    LOG.debug("", ex);
    assertEquals(
      "errorVariantUnrecognizedMember",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X what]");
    });
    LOG.debug("", ex);
    assertEquals("errorUnexpectedExpressionForm", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense2()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X ([])]");
    });
    LOG.debug("", ex);
    assertEquals("errorUnexpectedExpressionForm", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense3()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[]");
    });
    LOG.debug("", ex);
    assertEquals(
      "errorVariantInvalidDeclaration",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense4()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[import x y]");
    });
    LOG.debug("", ex);
    assertEquals("errorVariantKeyword", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense5()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X ()]");
    });
    LOG.debug("", ex);
    assertEquals(
      "errorVariantUnrecognizedMember",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense6()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X (case Q [])]");
    });
    LOG.debug("", ex);
    assertEquals(
      "errorFieldInvalid",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense7()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X (unrecognized)]");
    });
    LOG.debug("", ex);
    assertEquals(
      "errorVariantUnrecognizedMember",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense8()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("[variant X (case)]");
    });
    LOG.debug("", ex);
    assertEquals(
      "errorVariantCaseInvalidDeclaration",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testQuotedBad()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parse("\"hello\"");
    });
    LOG.debug("", ex);
    assertEquals("errorUnexpectedExpressionForm", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }
}
