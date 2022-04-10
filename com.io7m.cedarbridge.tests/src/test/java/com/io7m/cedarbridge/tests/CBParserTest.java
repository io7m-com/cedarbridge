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

package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.CBExpressionSources;
import com.io7m.cedarbridge.schema.parser.CBParserFactory;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.api.CBParserType;
import com.io7m.cedarbridge.schema.parser.internal.CBImportParser;
import com.io7m.cedarbridge.schema.parser.internal.CBParseContext;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jsx.api.serializer.JSXSerializerType;
import org.apache.commons.io.input.BrokenInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBParserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBParserTest.class);

  private CBStringsType strings;
  private CBImportParser parser;
  private CBParseContext context;
  private JSXSerializerType serializer;
  private ArrayList<CBError> errors;
  private CBParserFactory parsers;
  private Path directory;

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
    throws IOException
  {
    this.errors = new ArrayList<CBError>();
    this.parsers = new CBParserFactory();
    this.directory = CBTestDirectories.createTempDirectory();
  }

  @Test
  public void testBasic()
    throws Exception
  {
    try (var stream = this.stream("basic.cbs")) {
      try (var parser = this.parser(stream)) {
        final var parsedPackage = parser.execute();
        assertEquals("com.io7m.cedarbridge", parsedPackage.name().text());

        final var types = parsedPackage.types();
        assertEquals(5, types.size());
        final var type0 = types.get(0);
        assertEquals("UnitType", type0.name().text());
        final var type1 = types.get(1);
        assertEquals("Option", type1.name().text());
        final var type2 = types.get(2);
        assertEquals("List", type2.name().text());
        final var type3 = types.get(3);
        assertEquals("Pair", type3.name().text());
        final var type4 = types.get(4);
        assertEquals("Map", type4.name().text());

        final var imports = parsedPackage.imports();
        assertEquals(1, imports.size());
        final var import0 = imports.get(0);
        assertEquals("x.y.z", import0.target().text());
        assertEquals("b", import0.shortName().text());
      }
    }
  }

  @Test
  public void testBroken0()
    throws Exception
  {
    try (var stream = this.stream("errorParseBroken0.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals("errorSExpressionInvalid", this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testBroken1()
    throws Exception
  {
    try (var stream = this.stream("errorParseBroken1.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals("errorSExpressionInvalid", this.takeError().errorCode());
        assertEquals(
          "errorUnexpectedExpressionForm",
          this.takeError().errorCode());
        assertEquals("errorDeclarationInvalid", this.takeError().errorCode());
        assertEquals("errorSExpressionInvalid", this.takeError().errorCode());
        assertEquals("errorSExpressionInvalid", this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testBasicNoPackage()
    throws Exception
  {
    try (var stream = this.stream("errorParseBasicNoPackage.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals("errorPackageNameMissing", this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testBasicTooManyPackageNames()
    throws Exception
  {
    try (var stream = this.stream("errorParseBasicTooManyPackageNames.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals("errorPackageNameMultiple", this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testUnrecognizedThing()
    throws Exception
  {
    try (var stream = this.stream("errorParseUnrecognizedThing.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorDeclarationUnrecognized",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testLanguageNotFirst0()
    throws Exception
  {
    try (var stream = this.stream("errorLanguageNotFirst0.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorLanguageFirst",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testLanguageNotFirst1()
    throws Exception
  {
    try (var stream = this.stream("errorLanguageNotFirst1.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorLanguageFirst",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testLanguageUnrecognized0()
    throws Exception
  {
    try (var stream = this.stream("errorLanguageUnrecognized.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorLanguageBadName",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testLanguageUnrecognizedVersion0()
    throws Exception
  {
    try (var stream = this.stream("errorLanguageUnrecognizedVersion0.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorLanguageBadVersion",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testLanguageUnrecognizedVersion1()
    throws Exception
  {
    try (var stream = this.stream("errorLanguageUnrecognizedVersion1.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorLanguageBadVersion",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testLanguageUnrecognizedVersion2()
    throws Exception
  {
    try (var stream = this.stream("errorLanguageUnrecognizedVersion2.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorLanguageBadVersion",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testLanguageUnrecognizedVersion3()
    throws Exception
  {
    try (var stream = this.stream("errorLanguageUnrecognizedVersion3.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorLanguageBadVersion",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  @Test
  public void testLanguageBroken0()
    throws Exception
  {
    try (var stream = this.stream("errorLanguageBroken.cbs")) {
      try (var parser = this.parser(stream)) {
        assertThrows(CBParseFailedException.class, parser::execute);
        assertEquals(
          "errorDeclarationInvalid",
          this.takeError().errorCode());
        assertEquals(0, this.errors.size());
      }
    }
  }

  private CBParserType parser(
    final InputStream stream)
  {
    final var sources =
      new CBExpressionSources();
    final var source =
      sources.create(URI.create("urn:test"), stream);
    return this.parsers.createParser(this::addError, source);
  }

  @Test
  public void testIOError0()
    throws Exception
  {
    final var stream = new BrokenInputStream();
    try (var parser = this.parser(stream)) {
      assertThrows(CBParseFailedException.class, parser::execute);
      assertEquals("errorIO", this.takeError().errorCode());
      assertEquals(0, this.errors.size());
    } catch (final IOException e) {
      //
    }
  }

  private InputStream stream(
    final String name)
    throws IOException
  {
    return CBTestDirectories.resourceStreamOf(
      CBParserTest.class, this.directory, name);
  }
}
