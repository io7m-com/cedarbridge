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

package com.io7m.cedarbridge.tests.runtime.container_protocol;

import com.io7m.cedarbridge.runtime.api.CBProtocolMessageType;
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerCollection;
import com.io7m.cedarbridge.runtime.container_protocol.CBContainerProtocolMessages;
import com.io7m.cedarbridge.runtime.container_protocol.CBContainerProtocolResponse;
import com.io7m.cedarbridge.tests.CBTestDirectories;
import com.io7m.cedarbridge.tests.runtime.FakeProtocolSerializerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CBContainerProtocolMessagesTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(CBContainerProtocolMessagesTest.class);

  private Path directory;

  @BeforeEach
  public void setup()
    throws Exception
  {
    this.directory = CBTestDirectories.createTempDirectory();
  }

  @Test
  public void testParseAvailableOK()
    throws Exception
  {
    final var data =
      this.readHeader("protoHeaderAvailable0.bin");
    final var header =
      CBContainerProtocolMessages.parseAvailable(data);

    assertEquals(1L, header.containerProtocolMinimumVersion());
    assertEquals(2L, header.containerProtocolMaximumVersion());

    assertEquals(
      "fb06cb82-4245-ff5a-d5e5-6d0060db3016",
      header.applicationProtocolId().toString());
    assertEquals(1L, header.applicationProtocolMinimumVersion());
    assertEquals(3L, header.applicationProtocolMaximumVersion());

    assertEquals(
      header,
      CBContainerProtocolMessages.parseAvailable(
        CBContainerProtocolMessages.serializeAvailable(header))
    );
    assertEquals(
      header,
      CBContainerProtocolMessages.parseAvailable(
        CBContainerProtocolMessages.serializeAvailableAsBytes(header))
    );
  }

  @Test
  public void testParseAvailableBadVersion0()
    throws Exception
  {
    final var data =
      this.readHeader("protoHeaderAvailableBadVersion0.bin");

    final var ex = assertThrows(IllegalArgumentException.class, () -> {
      CBContainerProtocolMessages.parseAvailable(data);
    });

    LOG.debug("", ex);
  }

  @Test
  public void testParseAvailableBadVersion1()
    throws Exception
  {
    final var data =
      this.readHeader("protoHeaderAvailableBadVersion1.bin");

    final var ex = assertThrows(IllegalArgumentException.class, () -> {
      CBContainerProtocolMessages.parseAvailable(data);
    });

    LOG.debug("", ex);
  }

  @Test
  public void testParseAvailableBadMagicNumber0()
    throws Exception
  {
    final var data =
      this.readHeader("protoHeaderAvailableBadMagic0.bin");

    final var ex = assertThrows(IllegalArgumentException.class, () -> {
      CBContainerProtocolMessages.parseAvailable(data);
    });

    LOG.debug("", ex);
  }

  @Test
  public void testParseUseOK()
    throws Exception
  {
    final var data =
      this.readHeader("protoHeaderUse0.bin");
    final var header =
      CBContainerProtocolMessages.parseUse(data);

    assertEquals(1L, header.containerProtocolVersion());

    assertEquals(
      "fb06cb82-4245-ff5a-d5e5-6d0060db3016",
      header.applicationProtocolId().toString());
    assertEquals(3L, header.applicationProtocolVersion());

    assertEquals(
      header,
      CBContainerProtocolMessages.parseUse(
        CBContainerProtocolMessages.serializeUse(header))
    );
    assertEquals(
      header,
      CBContainerProtocolMessages.parseUse(
        CBContainerProtocolMessages.serializeUseAsBytes(header))
    );
  }

  @Test
  public void testParseUseBadMagic0()
    throws Exception
  {
    final var data =
      this.readHeader("protoHeaderUseBadMagic0.bin");

    final var ex = assertThrows(IllegalArgumentException.class, () -> {
      CBContainerProtocolMessages.parseUse(data);
    });

    LOG.debug("", ex);
  }

  @Test
  public void testParseResponseOK()
    throws Exception
  {
    final var data =
      this.readHeader("protoHeaderResponse0.bin");
    final var header =
      CBContainerProtocolMessages.parseResponse(data);

    assertTrue(header.ok());
    assertEquals("No problem!", header.message());

    assertEquals(
      header,
      CBContainerProtocolMessages.parseResponse(
        CBContainerProtocolMessages.serializeResponse(header))
    );
    assertEquals(
      header,
      CBContainerProtocolMessages.parseResponse(
        CBContainerProtocolMessages.serializeResponseAsBytes(header))
    );
  }

  @Test
  public void testParseResponseBadMagic0()
    throws Exception
  {
    final var data =
      this.readHeader("protoHeaderResponseBadMagic0.bin");

    final var ex = assertThrows(IllegalArgumentException.class, () -> {
      CBContainerProtocolMessages.parseResponse(data);
    });

    LOG.debug("", ex);
  }

  @Test
  public void testErrorMessageLength()
    throws IOException
  {
    final var id = UUID.randomUUID();
    final var collection =
      CBProtocolSerializerCollection.builder(id)
        .addFactory(new FakeProtocolSerializerFactory(
          id,
          0xffff_ffff_ffff_fffdL,
          CBProtocolMessageType.class))
        .addFactory(new FakeProtocolSerializerFactory(
          id,
          0xffff_ffff_ffff_fffeL,
          CBProtocolMessageType.class))
        .build();

    final var ex =
      assertThrows(IllegalArgumentException.class, () -> {
        collection.checkSupportedVersion(id, 0xffff_ffff_ffff_ffffL);
      });

    final var response =
      CBContainerProtocolMessages.serializeResponseAsBytes(
        CBContainerProtocolResponse.builder()
          .setOk(false)
          .setMessage(ex.getMessage())
          .build()
      );

    assertEquals(256, response.length);

    final var file = this.directory.resolve("response.bin");
    LOG.debug("file: {}", file);
    Files.write(file, response);
  }

  @Test
  public void testErrorTooLong()
  {
    final var array = new ByteArrayOutputStream();
    for (int index = 0; index < 256; ++index) {
      array.write(index);
    }

    assertThrows(IllegalArgumentException.class, () -> {
      CBContainerProtocolResponse.builder()
        .setOk(false)
        .setMessage(array.toString(StandardCharsets.UTF_8))
        .build();
    });
  }

  private byte[] readHeader(final String name)
    throws IOException
  {
    try (var stream =
           CBTestDirectories.resourceStreamOf(
             CBContainerProtocolMessagesTest.class, this.directory, name)) {
      return stream.readAllBytes();
    }
  }
}
