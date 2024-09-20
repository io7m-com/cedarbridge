/*
 * Copyright © 2023 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

open module com.io7m.cedarbridge.tests
{
  requires com.io7m.cedarbridge.bridgedoc.api;
  requires com.io7m.cedarbridge.bridgedoc.spi;
  requires com.io7m.cedarbridge.bridgedoc.xhtml;
  requires com.io7m.cedarbridge.cmdline;
  requires com.io7m.cedarbridge.codegen.api;
  requires com.io7m.cedarbridge.codegen.javastatic;
  requires com.io7m.cedarbridge.codegen.spi;
  requires com.io7m.cedarbridge.errors;
  requires com.io7m.cedarbridge.examples;
  requires com.io7m.cedarbridge.exprsrc;
  requires com.io7m.cedarbridge.exprsrc.api;
  requires com.io7m.cedarbridge.runtime.api;
  requires com.io7m.cedarbridge.runtime.bssio;
  requires com.io7m.cedarbridge.runtime.container_protocol;
  requires com.io7m.cedarbridge.runtime.convenience;
  requires com.io7m.cedarbridge.runtime.time;
  requires com.io7m.cedarbridge.schema.ast;
  requires com.io7m.cedarbridge.schema.binder;
  requires com.io7m.cedarbridge.schema.binder.api;
  requires com.io7m.cedarbridge.schema.compiled;
  requires com.io7m.cedarbridge.schema.compiler;
  requires com.io7m.cedarbridge.schema.compiler.api;
  requires com.io7m.cedarbridge.schema.core_types;
  requires com.io7m.cedarbridge.schema.loader.api;
  requires com.io7m.cedarbridge.schema.names;
  requires com.io7m.cedarbridge.schema.parser;
  requires com.io7m.cedarbridge.schema.parser.api;
  requires com.io7m.cedarbridge.schema.time;
  requires com.io7m.cedarbridge.schema.typer;
  requires com.io7m.cedarbridge.schema.typer.api;
  requires com.io7m.cedarbridge.strings.api;
  requires com.io7m.cedarbridge.version;

  exports com.io7m.cedarbridge.tests.bridgedoc.xhtml;
  exports com.io7m.cedarbridge.tests.cmdline;
  exports com.io7m.cedarbridge.tests.codegen.javastatic;
  exports com.io7m.cedarbridge.tests.runtime.api;
  exports com.io7m.cedarbridge.tests.runtime.bssio;
  exports com.io7m.cedarbridge.tests.runtime.container_protocol;
  exports com.io7m.cedarbridge.tests.runtime.convenience;
  exports com.io7m.cedarbridge.tests;

  requires com.io7m.jbssio.vanilla;
  requires com.io7m.junreachable.core;
  requires java.compiler;
  requires net.bytebuddy.agent;
  requires net.bytebuddy;
  requires net.jqwik.api;
  requires org.apache.commons.io;
  requires org.mockito;
  requires org.slf4j;

  requires org.junit.jupiter.api;
  requires org.junit.jupiter.engine;
  requires org.junit.platform.commons;
  requires org.junit.platform.engine;
  requires org.junit.platform.launcher;

  provides net.jqwik.api.providers.ArbitraryProvider
    with com.io7m.cedarbridge.tests.CBIdentityTestProvider;

  uses net.jqwik.api.providers.ArbitraryProvider;
  uses com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceFactoryType;
  uses com.io7m.cedarbridge.schema.binder.api.CBBinderFactoryType;
  uses com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerFactoryType;
  uses com.io7m.cedarbridge.schema.parser.api.CBParserFactoryType;
  uses com.io7m.cedarbridge.schema.typer.api.CBTypeCheckerFactoryType;
}
