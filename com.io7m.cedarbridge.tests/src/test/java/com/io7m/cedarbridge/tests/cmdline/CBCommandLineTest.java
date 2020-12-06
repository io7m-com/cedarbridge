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

package com.io7m.cedarbridge.tests.cmdline;

import com.io7m.cedarbridge.cmdline.MainExitless;
import com.io7m.cedarbridge.tests.CBTestDirectories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CBCommandLineTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBCommandLineTest.class);

  private ByteArrayOutputStream output;
  private PrintStream outputPrint;
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = CBTestDirectories.createTempDirectory();
    this.output = new ByteArrayOutputStream();
    this.outputPrint = new PrintStream(this.output);
    System.setOut(null);
    System.setErr(null);
  }

  void flush()
  {
    try {
      this.outputPrint.flush();
      this.output.flush();
    } catch (final IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  @Test
  public void testNoArguments()
    throws IOException
  {
    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{

      });
    });
  }

  @Test
  public void testHelp()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Commands:"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpHelp()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "version"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: version"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpCheck()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "check"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: check"));
    LOG.debug("{}", text);
  }

  @Test
  public void testHelpListCodeGenerators()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "help", "list-code-generators"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Usage: list-code-generators"));
    LOG.debug("{}", text);
  }

  @Test
  public void testVersion()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "version"
    });

    this.flush();
    final var text = this.output.toString();
    assertEquals("cedarbridge 1.0.0", text.trim());
    LOG.debug("{}", text);
  }

  @Test
  public void testCheckNothing()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "check"
    });

    this.flush();
    final var text = this.output.toString();
    assertEquals("", text.trim());
    LOG.debug("{}", text);
  }

  @Test
  public void testCheckError0()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    final var file =
      CBTestDirectories.resourceOf(
        CBCommandLineTest.class,
        this.directory,
        "errorBindDuplicateField0.cbs"
      );

    assertThrows(IOException.class, () -> {
      MainExitless.main(new String[]{
        "check",
        "--file",
        file.toString()
      });
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("errorBindDuplicateField0.cbs"));
    LOG.debug("{}", text);
  }

  @Test
  public void testListCodeGenerators()
    throws IOException
  {
    System.setOut(this.outputPrint);
    System.setErr(this.outputPrint);

    MainExitless.main(new String[]{
      "list-code-generators"
    });

    this.flush();
    final var text = this.output.toString();
    assertTrue(text.contains("Java 11+"));
    LOG.debug("{}", text);
  }
}
