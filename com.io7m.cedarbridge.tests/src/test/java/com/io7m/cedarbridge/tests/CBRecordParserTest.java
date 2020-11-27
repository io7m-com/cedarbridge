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
import com.io7m.cedarbridge.schema.parser.internal.CBParseContext;
import com.io7m.cedarbridge.schema.parser.internal.CBParserStrings;
import com.io7m.cedarbridge.schema.parser.internal.CBRecordParser;
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

public final class CBRecordParserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBRecordParserTest.class);

  private CBStringsType strings;
  private CBRecordParser parser;
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
    this.parser = new CBRecordParser();
    this.context =
      new CBParseContext(this.strings, this.serializer, this::addError);
  }

  @Test
  public void testEmptyRecord()
    throws Exception
  {
    final var record =
      this.parser.parse(
        this.context.current(),
        expression("[record Q]")
      );

    assertEquals(0, record.fields().size());
    assertEquals("Q", record.name().text());
    assertEquals(0, record.parameters().size());
  }

  @Test
  public void testLists3()
    throws Exception
  {
    final var record =
      this.parser.parse(
        this.context.current(),
        expression(
          "[record Q (parameter A) (parameter B) (field f0 [List A]) (field f1 [List B])]")
      );

    assertEquals(2, record.fields().size());
    final var field0 = record.fields().get(0);
    assertEquals("f0", field0.name().text());
    final var field1 = record.fields().get(1);
    assertEquals("f1", field1.name().text());

    assertEquals("Q", record.name().text());

    assertEquals(2, record.parameters().size());
    final var tp0 = record.parameters().get(0);
    assertEquals("A", tp0.text());
    final var tp1 = record.parameters().get(1);
    assertEquals("B", tp1.text());
  }

  @Test
  public void testVector3()
    throws Exception
  {
    final var record =
      this.parser.parse(
        this.context.current(),
        expression(
          "[record Q (field f0 Float) (field f1 Float) (field f2 Float)]")
      );

    assertEquals(3, record.fields().size());
    final var field0 = record.fields().get(0);
    assertEquals("f0", field0.name().text());
    final var field1 = record.fields().get(1);
    assertEquals("f1", field1.name().text());
    final var field2 = record.fields().get(2);
    assertEquals("f2", field2.name().text());

    assertEquals("Q", record.name().text());
    assertEquals(0, record.parameters().size());
  }

  @Test
  public void testFieldBad0()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record X (field x Y z)]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorRecordInvalidField", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testFieldBad1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record X (field 23 Y)]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorRecordFieldNameInvalid", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNameBad1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record x]")
      );
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
      this.parser.parse(
        this.context.current(),
        expression("[record [X B]]")
      );
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
      this.parser.parse(
        this.context.current(),
        expression("[record X (parameter)]")
      );
    });
    LOG.debug("", ex);
    assertEquals(
      "errorRecordInvalidTypeParameter",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testParametersBad1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record X (parameter A B)]")
      );
    });
    LOG.debug("", ex);
    assertEquals(
      "errorRecordInvalidTypeParameter",
      this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testParametersBad2()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record X (parameter a)]")
      );
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
      this.parser.parse(
        this.context.current(),
        expression("[record X (what)]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorRecordUnrecognizedMember", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense1()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record X what]")
      );
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
      this.parser.parse(
        this.context.current(),
        expression("[record X ([])]")
      );
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
      this.parser.parse(
        this.context.current(),
        expression("[]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorRecordInvalidDeclaration", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense4()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[import x y]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorRecordKeyword", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testNonsense5()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("[record X ()]")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorRecordUnrecognizedMember", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }

  @Test
  public void testQuotedBad()
    throws Exception
  {
    final var ex = assertThrows(CBParseFailedException.class, () -> {
      this.parser.parse(
        this.context.current(),
        expression("\"hello\"")
      );
    });
    LOG.debug("", ex);
    assertEquals("errorUnexpectedExpressionForm", this.takeError().errorCode());
    assertEquals(0, this.errors.size());
  }
}
