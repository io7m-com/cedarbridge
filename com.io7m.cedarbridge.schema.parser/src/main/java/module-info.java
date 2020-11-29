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

import com.io7m.cedarbridge.schema.parser.CBParserFactory;
import com.io7m.cedarbridge.schema.parser.api.CBParserFactoryType;

/**
 * Cedarbridge message protocol (Schema parser)
 */

module com.io7m.cedarbridge.schema.parser
{
  requires static org.osgi.annotation.bundle;
  requires static org.osgi.annotation.versioning;

  requires transitive com.io7m.jlexing.core;
  requires transitive com.io7m.jsx.parser.api;
  requires transitive com.io7m.cedarbridge.errors;

  requires transitive com.io7m.cedarbridge.schema.ast;
  requires transitive com.io7m.cedarbridge.schema.parser.api;

  requires com.io7m.cedarbridge.strings.api;
  requires com.io7m.jsx.core;
  requires com.io7m.jeucreader.core;

  provides CBParserFactoryType with CBParserFactory;

  uses com.io7m.jsx.api.lexer.JSXLexerSupplierType;
  uses com.io7m.jsx.api.parser.JSXParserSupplierType;
  uses com.io7m.jsx.api.serializer.JSXSerializerSupplierType;

  exports com.io7m.cedarbridge.schema.parser;
}
