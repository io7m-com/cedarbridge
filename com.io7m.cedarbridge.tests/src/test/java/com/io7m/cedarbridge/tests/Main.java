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
import com.io7m.cedarbridge.schema.binder.CBBinderFactory;
import com.io7m.cedarbridge.schema.parser.CBParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public final class Main
{
  private static final Logger LOG =
    LoggerFactory.getLogger(Main.class);

  private Main()
  {

  }

  public static void main(
    final String[] args)
    throws Exception
  {
    final var sources = new CBExpressionSources();
    final var parsers = new CBParserFactory();
    final var binders = new CBBinderFactory();
    final var loader = new CBFakeLoader();

    final var failed = new AtomicBoolean(false);

    final Consumer<CBError> onError =
      error -> {
        LOG.error("{}", error.message());
        failed.set(true);
      };

    try (var source = sources.create(URI.create("urn:stdin"), System.in)) {
      try (var parser = parsers.createParser(onError, source)) {
        final var pack = parser.execute();
        try (var binder = binders.createBinder(loader, onError, source, pack)) {
          binder.execute();
        }
      }
    }
  }
}
