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

/**
 * Cedarbridge documentation.
 */

open module com.io7m.cedarbridge.documentation
{
  requires com.io7m.cedarbridge.bridgedoc.api;
  requires com.io7m.cedarbridge.bridgedoc.spi;
  requires com.io7m.cedarbridge.bridgedoc.xhtml;
  requires com.io7m.cedarbridge.cmdline;
  requires com.io7m.cedarbridge.codegen.api;
  requires com.io7m.cedarbridge.codegen.javastatic;
  requires com.io7m.cedarbridge.codegen.spi;
  requires com.io7m.cedarbridge.errors;
  requires com.io7m.cedarbridge.exprsrc.api;
  requires com.io7m.cedarbridge.exprsrc;
  requires com.io7m.cedarbridge.runtime.api;
  requires com.io7m.cedarbridge.runtime.bssio;
  requires com.io7m.cedarbridge.runtime.container_protocol;
  requires com.io7m.cedarbridge.schema.ast;
  requires com.io7m.cedarbridge.schema.binder.api;
  requires com.io7m.cedarbridge.schema.binder;
  requires com.io7m.cedarbridge.schema.compiled;
  requires com.io7m.cedarbridge.schema.compiler.api;
  requires com.io7m.cedarbridge.schema.compiler;
  requires com.io7m.cedarbridge.schema.core_types;
  requires com.io7m.cedarbridge.schema.loader.api;
  requires com.io7m.cedarbridge.schema.names;
  requires com.io7m.cedarbridge.schema.parser.api;
  requires com.io7m.cedarbridge.schema.parser;
  requires com.io7m.cedarbridge.schema.typer.api;
  requires com.io7m.cedarbridge.schema.typer;
  requires com.io7m.cedarbridge.strings.api;
  requires com.io7m.cedarbridge.version;

  requires com.io7m.claypot.core;
  requires java.xml;
  requires jcommander;

  exports com.io7m.cedarbridge.documentation;
}
