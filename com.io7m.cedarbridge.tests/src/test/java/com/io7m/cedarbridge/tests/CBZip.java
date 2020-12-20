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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class CBZip
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBZip.class);

  private CBZip()
  {

  }

  public static void create(
    final Path zipFile,
    final Path inputDirectory)
    throws IOException
  {
    try (var output = new ZipOutputStream(
      new BufferedOutputStream(Files.newOutputStream(zipFile)))) {
      try (var pathStream = Files.walk(inputDirectory)) {
        pathStream.map(inputDirectory::relativize)
          .forEach(path -> zipOneUnchecked(inputDirectory, output, path));
      }
      output.finish();
      output.flush();
    }
  }

  private static void zipOneUnchecked(
    final Path inputDirectory,
    final ZipOutputStream output,
    final Path path)
  {
    try {
      zipOne(inputDirectory, output, path);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void zipOne(
    final Path inputDirectory,
    final ZipOutputStream output,
    final Path path)
    throws IOException
  {
    LOG.info("zip: {}", path);

    final var inputFile = inputDirectory.resolve(path);
    if (Files.isRegularFile(inputFile)) {
      final var entry = new ZipEntry(path.toString().replace("\\", "/"));
      output.putNextEntry(entry);
      try (var stream = Files.newInputStream(inputFile)) {
        stream.transferTo(output);
      }
      output.closeEntry();
    }
  }
}
