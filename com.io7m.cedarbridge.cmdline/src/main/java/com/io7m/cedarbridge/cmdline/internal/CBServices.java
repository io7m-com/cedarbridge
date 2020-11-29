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

package com.io7m.cedarbridge.cmdline.internal;

import com.io7m.cedarbridge.version.CBVersion;
import com.io7m.cedarbridge.version.CBVersions;

import java.io.IOException;
import java.net.URL;
import java.util.ServiceLoader;

public final class CBServices
{
  private CBServices()
  {

  }

  private static <T> T findService(
    final Class<T> service)
  {
    return ServiceLoader.load(service)
      .findFirst()
      .orElseThrow(() -> missingService(service));
  }

  private static IllegalStateException missingService(
    final Class<?> clazz)
  {
    return new IllegalStateException(String.format(
      "No available implementations of service: %s",
      clazz.getCanonicalName()));
  }

  public static CBVersion findApplicationVersion()
    throws IOException
  {
    final URL resource =
      CBServices.class.getResource(
        "/com/io7m/cedarbridge/cmdline/internal/version.properties"
      );

    try (var stream = resource.openStream()) {
      return CBVersions.ofStream(stream);
    }
  }
}