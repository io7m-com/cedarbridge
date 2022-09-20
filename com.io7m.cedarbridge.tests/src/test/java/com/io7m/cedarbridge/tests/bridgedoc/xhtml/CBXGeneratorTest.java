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

package com.io7m.cedarbridge.tests.bridgedoc.xhtml;

import com.io7m.cedarbridge.bridgedoc.api.CBDocGeneratorConfiguration;
import com.io7m.cedarbridge.bridgedoc.api.CBDocGenerators;
import com.io7m.cedarbridge.cmdline.internal.CBServices;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilation;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerConfiguration;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerFactoryType;
import com.io7m.cedarbridge.schema.core_types.CBCore;
import com.io7m.cedarbridge.tests.CBTestDirectories;
import com.io7m.xstructural.api.XSProcessorException;
import com.io7m.xstructural.api.XSProcessorFactoryType;
import com.io7m.xstructural.api.XSProcessorRequest;
import com.io7m.xstructural.api.XSProcessorRequestType;
import com.io7m.xstructural.vanilla.XSProcessors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CBXGeneratorTest
{
  private Path directory;

  private static List<Path> document(
    final List<Path> files,
    final Path outputDirectory)
    throws Exception
  {
    final var languageName =
      "xhtml";
    final var docGenerators =
      new CBDocGenerators();
    final var compilers =
      CBServices.findService(CBSchemaCompilerFactoryType.class);

    final var docGeneratorFactory =
      docGenerators.findByLanguageName(languageName)
        .orElseThrow(() -> new IllegalArgumentException(String.format(
          "No documentation generator available for the language '%s'",
          languageName)
        ));

    final var compileFiles =
      files.stream()
        .map(Path::toAbsolutePath)
        .collect(Collectors.toList());

    final var configuration =
      new CBSchemaCompilerConfiguration(
        List.of(),
        compileFiles
      );

    final var compiler =
      compilers.createCompiler(configuration);

    compiler.loader().register(CBCore.get());

    final CBSchemaCompilation compilation =
      compiler.execute();

    final var docGeneratorConfiguration =
      new CBDocGeneratorConfiguration(outputDirectory);

    final var docGenerator =
      docGeneratorFactory.createGenerator(docGeneratorConfiguration);

    final var outputFiles = new ArrayList<Path>();
    for (final var packV : compilation.compiledPackages()) {
      outputFiles.addAll(docGenerator.execute(packV).createdFiles());
    }
    return List.copyOf(outputFiles);
  }

  /**
   * @return The available processors
   */

  private static XSProcessorFactoryType findProcessors()
  {
    return ServiceLoader.load(XSProcessorFactoryType.class)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("No processor service"));
  }

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = CBTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    CBTestDirectories.deleteDirectory(this.directory);
  }

  @Test
  public void testChat()
    throws Exception
  {
    final var file =
      CBTestDirectories.resourceOf(
        CBXGeneratorTest.class, this.directory, "chat.cbs");

    final var results = document(List.of(file), this.directory);
    assertEquals(2, results.size());

    this.validate();
  }

  @Test
  public void testPaste()
    throws Exception
  {
    final var file =
      CBTestDirectories.resourceOf(
        CBXGeneratorTest.class, this.directory, "pastebin.cbs");

    final var results = document(List.of(file), this.directory);
    assertEquals(2, results.size());

    this.validate();
  }

  private void validate()
    throws XSProcessorException
  {
    final var requestBuilder = XSProcessorRequest.builder();
    requestBuilder.setOutputDirectory(this.directory);
    requestBuilder.setSourceFile(this.directory);
    requestBuilder.setTask(XSProcessorRequestType.Task.VALIDATE_XHTML);

    final var request = requestBuilder.build();
    final var processors = findProcessors();
    final var processor = processors.create(request);
    processor.execute();

    final var proc = new XSProcessors();
    proc.create(request);
  }

  @Test
  public void testAdmin1()
    throws Exception
  {
    final var file =
      CBTestDirectories.resourceOf(
        CBXGeneratorTest.class, this.directory, "Admin1.cbs");

    final var results = document(List.of(file), this.directory);
    assertEquals(2, results.size());

    this.validate();
  }
}
