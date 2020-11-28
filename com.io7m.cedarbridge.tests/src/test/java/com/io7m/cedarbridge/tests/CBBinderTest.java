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
import com.io7m.cedarbridge.exprsrc.CBExpressionSources;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceType;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.binder.CBBinderFactory;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.parser.CBParserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class CBBinderTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBBinderTest.class);

  private static final CBExpressionSources SOURCES =
    new CBExpressionSources();

  private CBExpressionSourceType source;
  private CBBinderFactory binders;
  private CBParserFactory parsers;
  private CBExpressionSources sources;
  private ArrayList<CBError> errors;
  private Path directory;
  private CBFakeLoader loader;

  private CBASTPackage parse(
    final String name)
    throws Exception
  {
    final var path =
      CBTestDirectories.resourceOf(CBBinderTest.class, this.directory, name);
    this.source =
      this.sources.create(path.toUri(), Files.newInputStream(path));
    final var parser =
      this.parsers.createParser(this::addError, this.source);
    return parser.execute();
  }

  private CBASTPackage bind(
    final String name)
    throws Exception
  {
    final var parsedPackage = this.parse(name);
    try (var binder =
           this.binders.createBinder(
             this.loader, this::addError, this.source, parsedPackage)) {
      return binder.execute();
    }
  }

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
    this.directory = CBTestDirectories.createTempDirectory();
    this.sources = new CBExpressionSources();
    this.parsers = new CBParserFactory();
    this.binders = new CBBinderFactory();
    this.loader = new CBFakeLoader();
  }

  @Test
  public void testUnresolvablePackage()
    throws Exception
  {
    this.loader = new CBFakeLoader();

    assertThrows(CBBindFailedException.class, () -> {
      this.bind("basic.cbs");
    });

    assertEquals("errorPackageUnavailable", this.takeError().errorCode());
  }

  @Test
  public void testImportConflict()
    throws Exception
  {
    this.loader = new CBFakeLoader();
    this.loader.addPackage(new CBFakePackage("x.y.z"));
    this.loader.addPackage(new CBFakePackage("a.b.c"));

    assertThrows(CBBindFailedException.class, () -> {
      this.bind("errorPackageTwice.cbs");
    });

    assertEquals("errorPackageShortNameUsed", this.takeError().errorCode());
  }
}
