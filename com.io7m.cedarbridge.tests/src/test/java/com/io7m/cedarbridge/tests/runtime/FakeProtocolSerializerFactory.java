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

package com.io7m.cedarbridge.tests.runtime;

import com.io7m.cedarbridge.runtime.api.CBProtocolMessageType;
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerFactoryType;
import com.io7m.cedarbridge.runtime.api.CBProtocolSerializerType;
import com.io7m.cedarbridge.runtime.api.CBSerializerDirectoryType;

import java.util.UUID;

public final class FakeProtocolSerializerFactory implements
  CBProtocolSerializerFactoryType<CBProtocolMessageType>
{
  private final UUID id;
  private final long version;
  private final Class<CBProtocolMessageType> clazz;

  public FakeProtocolSerializerFactory(
    final UUID id,
    final long version,
    final Class<CBProtocolMessageType> clazz)
  {
    this.id = id;
    this.version = version;
    this.clazz = clazz;
  }

  @Override
  public UUID id()
  {
    return this.id;
  }

  @Override
  public long version()
  {
    return this.version;
  }

  @Override
  public Class<CBProtocolMessageType> serializes()
  {
    return this.clazz;
  }

  @Override
  public CBProtocolSerializerType<CBProtocolMessageType> create(
    final CBSerializerDirectoryType directory)
  {
    throw new UnsupportedOperationException();
  }
}
