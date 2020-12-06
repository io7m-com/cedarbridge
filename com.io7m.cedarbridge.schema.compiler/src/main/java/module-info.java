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

import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceFactoryType;
import com.io7m.cedarbridge.schema.binder.api.CBBinderFactoryType;
import com.io7m.cedarbridge.schema.compiler.CBSchemaCompilerFactory;
import com.io7m.cedarbridge.schema.compiler.api.CBSchemaCompilerFactoryType;
import com.io7m.cedarbridge.schema.loader.api.CBLoaderFactoryType;
import com.io7m.cedarbridge.schema.parser.api.CBParserFactoryType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckerFactoryType;

/**
 * Cedarbridge message protocol (Schema compiler)
 */

module com.io7m.cedarbridge.schema.compiler
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires transitive com.io7m.cedarbridge.exprsrc.api;
  requires transitive com.io7m.cedarbridge.schema.binder.api;
  requires transitive com.io7m.cedarbridge.schema.compiler.api;
  requires transitive com.io7m.cedarbridge.schema.loader.api;
  requires transitive com.io7m.cedarbridge.schema.parser.api;
  requires transitive com.io7m.cedarbridge.schema.typer.api;
  requires com.io7m.cedarbridge.strings.api;

  requires org.slf4j;

  uses CBParserFactoryType;
  uses CBExpressionSourceFactoryType;
  uses CBTypeCheckerFactoryType;
  uses CBBinderFactoryType;
  uses CBLoaderFactoryType;

  provides CBSchemaCompilerFactoryType with CBSchemaCompilerFactory;

  exports com.io7m.cedarbridge.schema.compiler;
}
