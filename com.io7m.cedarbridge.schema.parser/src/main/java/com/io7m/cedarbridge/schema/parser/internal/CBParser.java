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

package com.io7m.cedarbridge.schema.parser.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.schema.ast.CBASTDeclarationType;
import com.io7m.cedarbridge.schema.ast.CBASTImport;
import com.io7m.cedarbridge.schema.ast.CBASTPackageDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.parser.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.CBParsed;
import com.io7m.cedarbridge.schema.parser.CBParsedPackage;
import com.io7m.cedarbridge.schema.parser.CBParsedPackageType;
import com.io7m.cedarbridge.schema.parser.CBParserType;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.jsx.api.parser.JSXParserException;
import com.io7m.jsx.api.parser.JSXParserType;
import com.io7m.jsx.api.serializer.JSXSerializerType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class CBParser implements CBParserType
{
  private final Consumer<CBError> errors;
  private final URI uri;
  private final InputStream stream;
  private final JSXParserType parser;
  private final CBStringsType strings;
  private final JSXSerializerType serializer;

  public CBParser(
    final Consumer<CBError> inErrors,
    final URI inUri,
    final InputStream inStream,
    final JSXParserType inParser,
    final CBStringsType inStrings,
    final JSXSerializerType inSerializer)
  {
    this.errors =
      Objects.requireNonNull(inErrors, "errors");
    this.uri =
      Objects.requireNonNull(inUri, "uri");
    this.stream =
      Objects.requireNonNull(inStream, "inStream");
    this.parser =
      Objects.requireNonNull(inParser, "parser");
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.serializer =
      Objects.requireNonNull(inSerializer, "inSerializer");
  }

  private static List<CBASTTypeDeclarationType<CBParsed>> findTypes(
    final List<CBASTDeclarationType<CBParsed>> declarations)
  {
    return declarations.stream()
      .filter(i -> i instanceof CBASTTypeDeclarationType)
      .map(x -> (CBASTTypeDeclarationType<CBParsed>) x)
      .collect(Collectors.toList());
  }

  private static List<CBASTImport<CBParsed>> findImports(
    final List<CBASTDeclarationType<CBParsed>> declarations)
  {
    return declarations.stream()
      .filter(i -> i instanceof CBASTImport)
      .map(x -> (CBASTImport<CBParsed>) x)
      .collect(Collectors.toList());
  }

  @Override
  public CBParsedPackageType execute()
    throws CBParseFailedException
  {
    final var declParser =
      new CBDeclarationParser();
    final var context =
      new CBParseContext(this.strings, this.serializer, this.errors);
    final var declarations =
      new ArrayList<CBASTDeclarationType<CBParsed>>();

    while (true) {
      try {
        final var expressionOpt =
          this.parser.parseExpressionOrEOF();
        if (expressionOpt.isEmpty()) {
          break;
        }

        final var expression =
          expressionOpt.get();
        try {
          final var declaration =
            declParser.parse(context.current(), expression);

          declarations.add(declaration);
        } catch (final CBParseFailedException e) {
          // Ignored!
        }
      } catch (final JSXParserException e) {
        context.current().failed(
          e.lexical(),
          e,
          "errorSExpressionInvalid"
        );
      } catch (final IOException e) {
        context.current().failed(
          LexicalPositions.zeroWithFile(this.uri),
          e,
          "errorIO"
        );
        throw new CBParseFailedException();
      }
    }

    if (context.current().errorCount() > 0) {
      throw new CBParseFailedException();
    }

    return this.processDeclarations(context.current(), declarations);
  }

  private CBParsedPackageType processDeclarations(
    final CBParseContextType context,
    final List<CBASTDeclarationType<CBParsed>> declarations)
    throws CBParseFailedException
  {
    final var name =
      this.findName(context, declarations);
    final var imports =
      findImports(declarations);
    final var types =
      findTypes(declarations);

    return CBParsedPackage.builder()
      .setName(name.name())
      .setImports(imports)
      .setTypes(types)
      .build();
  }

  private CBASTPackageDeclaration<CBParsed> findName(
    final CBParseContextType context,
    final List<CBASTDeclarationType<CBParsed>> declarations)
    throws CBParseFailedException
  {
    final var names =
      declarations.stream()
        .filter(i -> i instanceof CBASTPackageDeclaration)
        .map(x -> (CBASTPackageDeclaration<CBParsed>) x)
        .collect(Collectors.toList());

    if (names.isEmpty()) {
      throw context.failed(
        LexicalPositions.zeroWithFile(this.uri),
        "errorPackageNameMissing"
      );
    }

    if (names.size() > 1) {
      throw context.failed(
        names.get(0).lexical(),
        "errorPackageNameMultiple"
      );
    }

    return names.get(0);
  }

  @Override
  public void close()
    throws IOException
  {
    this.stream.close();
  }
}
