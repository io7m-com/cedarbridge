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

package com.io7m.cedarbridge.runtime.container_protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Functions to parse and serialize versioning container protocol messages.
 */

public final class CBContainerProtocolMessages
{
  private CBContainerProtocolMessages()
  {

  }

  /**
   * Parse an "available" message.
   *
   * @param data The message data
   *
   * @return A parsed message
   */

  public static CBContainerProtocolAvailable parseAvailable(
    final byte[] data)
  {
    return parseAvailable(ByteBuffer.wrap(data));
  }

  /**
   * Parse an "available" message.
   *
   * @param data The message data
   *
   * @return A parsed message
   */

  public static CBContainerProtocolAvailable parseAvailable(
    final ByteBuffer data)
  {
    data.order(BIG_ENDIAN);

    final var code = data.getInt();
    if (code != 0x43420000) {
      final var lineSeparator = System.lineSeparator();
      throw new IllegalArgumentException(
        new StringBuilder(128)
          .append("Invalid command number.")
          .append(lineSeparator)
          .append("  Expected: 0x43420000")
          .append(lineSeparator)
          .append("  Received: 0x")
          .append(Integer.toUnsignedString(code, 16))
          .append(lineSeparator)
          .toString()
      );
    }

    final var containerMin = data.getInt();
    final var containerMax = data.getInt();
    final var reserved0 = data.getInt();

    final var appIdHigh = data.getLong();
    final var appIdLow = data.getLong();
    final var appId = new UUID(appIdHigh, appIdLow);

    final var appMin = data.getLong();
    final var appMax = data.getLong();

    return CBContainerProtocolAvailable.builder()
      .setContainerProtocolMinimumVersion(Integer.toUnsignedLong(containerMin))
      .setContainerProtocolMaximumVersion(Integer.toUnsignedLong(containerMax))
      .setApplicationProtocolId(appId)
      .setApplicationProtocolMinimumVersion(appMin)
      .setApplicationProtocolMaximumVersion(appMax)
      .build();
  }

  /**
   * Parse a "use" message.
   *
   * @param data The message data
   *
   * @return A parsed message
   */

  public static CBContainerProtocolUse parseUse(
    final ByteBuffer data)
  {
    data.order(BIG_ENDIAN);

    final var code = data.getInt();
    if (code != 0x43420001) {
      final var lineSeparator = System.lineSeparator();
      throw new IllegalArgumentException(
        new StringBuilder(128)
          .append("Invalid command number.")
          .append(lineSeparator)
          .append("  Expected: 0x43420001")
          .append(lineSeparator)
          .append("  Received: 0x")
          .append(Integer.toUnsignedString(code, 16))
          .append(lineSeparator)
          .toString()
      );
    }

    final var containerV = data.getInt();
    final var appIdHigh = data.getLong();
    final var appIdLow = data.getLong();
    final var appId = new UUID(appIdHigh, appIdLow);
    final var appV = data.getLong();

    return CBContainerProtocolUse.builder()
      .setContainerProtocolVersion(Integer.toUnsignedLong(containerV))
      .setApplicationProtocolId(appId)
      .setApplicationProtocolVersion(appV)
      .build();
  }

  /**
   * Parse a "use" message.
   *
   * @param data The message data
   *
   * @return A parsed message
   */

  public static CBContainerProtocolUse parseUse(
    final byte[] data)
  {
    return parseUse(ByteBuffer.wrap(data));
  }

  /**
   * Parse a "response" message.
   *
   * @param data The message data
   *
   * @return A parsed message
   */

  public static CBContainerProtocolResponse parseResponse(
    final ByteBuffer data)
  {
    data.order(BIG_ENDIAN);

    final var code = data.getInt();
    if (code != 0x43420002) {
      final var lineSeparator = System.lineSeparator();
      throw new IllegalArgumentException(
        new StringBuilder(128)
          .append("Invalid command number.")
          .append(lineSeparator)
          .append("  Expected: 0x43420002")
          .append(lineSeparator)
          .append("  Received: 0x")
          .append(Integer.toUnsignedString(code, 16))
          .append(lineSeparator)
          .toString()
      );
    }

    final var ok = data.getInt() == 1;
    final var length = data.getInt();

    final String text;
    try (var output = new ByteArrayOutputStream()) {
      final var textBuffer = new byte[length];
      data.get(textBuffer);
      output.writeBytes(textBuffer);
      text = output.toString(UTF_8);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }

    return CBContainerProtocolResponse.builder()
      .setOk(ok)
      .setMessage(text)
      .build();
  }

  /**
   * Parse a "response" message.
   *
   * @param data The message data
   *
   * @return A parsed message
   */

  public static CBContainerProtocolResponse parseResponse(
    final byte[] data)
  {
    return parseResponse(ByteBuffer.wrap(data));
  }

  /**
   * Serialize a "response" message.
   *
   * @param message The message
   *
   * @return A serialized message
   */

  public static ByteBuffer serializeResponse(
    final CBContainerProtocolResponse message)
  {
    final var textBytes =
      message.message().getBytes(UTF_8);

    final var buffer = ByteBuffer.allocate(textBytes.length + 12);
    buffer.order(BIG_ENDIAN);
    buffer.putInt(0x43420002);
    buffer.putInt(message.ok() ? 1 : 0);
    buffer.putInt(textBytes.length);
    buffer.put(textBytes);

    if (buffer.remaining() != 0) {
      throw new IllegalStateException("Buffer has remaining space");
    }

    buffer.flip();
    return buffer;
  }

  /**
   * Serialize a "use" message.
   *
   * @param message The message
   *
   * @return A serialized message
   */

  public static ByteBuffer serializeUse(
    final CBContainerProtocolUse message)
  {
    final var buffer = ByteBuffer.allocate(32);
    buffer.order(BIG_ENDIAN);
    buffer.putInt(0x43420001);
    buffer.putInt(Math.toIntExact(message.containerProtocolVersion()));
    buffer.putLong(message.applicationProtocolId().getMostSignificantBits());
    buffer.putLong(message.applicationProtocolId().getLeastSignificantBits());
    buffer.putLong(message.applicationProtocolVersion());

    if (buffer.remaining() != 0) {
      throw new IllegalStateException("Buffer has remaining space");
    }

    buffer.flip();
    return buffer;
  }

  /**
   * Serialize an "available" message.
   *
   * @param message The message
   *
   * @return A serialized message
   */

  public static ByteBuffer serializeAvailable(
    final CBContainerProtocolAvailable message)
  {
    final var buffer = ByteBuffer.allocate(48);
    buffer.order(BIG_ENDIAN);
    buffer.putInt(0x43420000);
    buffer.putInt(Math.toIntExact(message.containerProtocolMinimumVersion()));
    buffer.putInt(Math.toIntExact(message.containerProtocolMaximumVersion()));
    buffer.putInt(0);
    buffer.putLong(message.applicationProtocolId().getMostSignificantBits());
    buffer.putLong(message.applicationProtocolId().getLeastSignificantBits());
    buffer.putLong(message.applicationProtocolMinimumVersion());
    buffer.putLong(message.applicationProtocolMaximumVersion());

    if (buffer.remaining() != 0) {
      throw new IllegalStateException("Buffer has remaining space");
    }

    buffer.flip();
    return buffer;
  }
}
