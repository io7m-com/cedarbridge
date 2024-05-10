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


package com.io7m.cedarbridge.tests;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.schema.compiler.CBSchemaCompilerFactory;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CBSchemaCompilerTest
{
  private CBSchemaCompilerFactory compilers;
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.compilers = new CBSchemaCompilerFactory();
    this.directory = CBTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    CBTestDirectories.deleteDirectory(this.directory);
  }

  @Test
  public void testBug19()
    throws IOException
  {
    final var file =
      CBTestDirectories.resourceOf(
        CBSchemaCompilerTest.class,
        this.directory,
        "bug19.cbs"
      );

    final var configuration =
      new CBSchemaCompilerConfiguration(
        List.of(),
        List.of(file)
      );

    final var errors = new ArrayList<CBError>();
    final var compiler =
      this.compilers.createCompiler(configuration, errors::add);

    assertThrows(CBSchemaCompilerException.class, compiler::execute);
    assertTrue(errors.get(0).message().contains("[document"));
  }
}
