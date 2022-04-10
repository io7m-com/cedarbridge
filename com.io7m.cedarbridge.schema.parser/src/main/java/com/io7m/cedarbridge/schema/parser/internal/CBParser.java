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

package com.io7m.cedarbridge.schema.parser.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionSourceType;
import com.io7m.cedarbridge.schema.ast.CBASTDeclarationType;
import com.io7m.cedarbridge.schema.ast.CBASTImport;
import com.io7m.cedarbridge.schema.ast.CBASTLanguage;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.ast.CBASTPackageDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.cedarbridge.schema.parser.api.CBParserType;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jlexing.core.LexicalPositions;
import com.io7m.jsx.api.parser.JSXParserException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_FATAL;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;

/**
 * The default parser implementation.
 */

public final class CBParser implements CBParserType
{
  private static final Optional<UUID> SPEC_SECTION_S_EXPRESSION =
    uuid("d1cd60f0-8880-4754-964f-5b0489947c59");
  private static final Optional<UUID> SPEC_SECTION_PACKAGE_NAME_MULTIPLE =
    uuid("881f56d2-f2cf-413a-9bc0-b1a40b11c52a");
  private static final Optional<UUID> SPEC_SECTION_PACKAGE_NAME_MISSING =
    uuid("3423cdfb-1bc8-4c4c-a270-bbfde7fbc853");

  private final Consumer<CBError> errors;
  private final CBStringsType strings;
  private final CBExpressionSourceType source;

  /**
   * The default parser implementation.
   *
   * @param inStrings The string resources
   * @param inErrors  The error consumer
   * @param inSource  The expression source
   */

  public CBParser(
    final CBStringsType inStrings,
    final Consumer<CBError> inErrors,
    final CBExpressionSourceType inSource)
  {
    this.errors =
      Objects.requireNonNull(inErrors, "errors");
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.source =
      Objects.requireNonNull(inSource, "inSource");
  }

  private static List<CBASTTypeDeclarationType> findTypes(
    final List<CBASTDeclarationType> declarations)
  {
    return declarations.stream()
      .filter(i -> i instanceof CBASTTypeDeclarationType)
      .map(x -> (CBASTTypeDeclarationType) x)
      .collect(Collectors.toList());
  }

  private static List<CBASTProtocolDeclaration> findProtocols(
    final List<CBASTDeclarationType> declarations)
  {
    return declarations.stream()
      .filter(i -> i instanceof CBASTProtocolDeclaration)
      .map(x -> (CBASTProtocolDeclaration) x)
      .collect(Collectors.toList());
  }

  private static List<CBASTImport> findImports(
    final List<CBASTDeclarationType> declarations)
  {
    return declarations.stream()
      .filter(i -> i instanceof CBASTImport)
      .map(x -> (CBASTImport) x)
      .collect(Collectors.toList());
  }

  @Override
  public CBASTPackage execute()
    throws CBParseFailedException
  {
    final var context =
      new CBParseContext(this.strings, this.source, this.errors);
    final var declarations =
      new ArrayList<CBASTDeclarationType>();

    var language =
      CBASTLanguage.builder()
        .setLexical(LexicalPositions.zero())
        .setLanguage("cedarbridge")
        .setMajor(BigInteger.ONE)
        .setMinor(BigInteger.ZERO)
        .build();

    boolean tooLateForLanguage = false;

    while (true) {
      try {
        final var expressionOpt =
          this.source.parseExpressionOrEOF();
        if (expressionOpt.isEmpty()) {
          break;
        }

        final var expression = expressionOpt.get();
        try {
          final var declParser =
            new CBDeclarationParser(tooLateForLanguage);
          final var declaration =
            declParser.parse(context.current(), expression);

          if (declaration instanceof CBASTLanguage) {
            language = (CBASTLanguage) declaration;
          }

          tooLateForLanguage = true;
          declarations.add(declaration);
        } catch (final CBParseFailedException e) {
          if (e.fatality() == IS_FATAL) {
            break;
          }
        }
      } catch (final JSXParserException e) {
        context.current().failed(
          e.lexical(),
          IS_NOT_FATAL,
          SPEC_SECTION_S_EXPRESSION,
          e,
          "errorSExpressionInvalid"
        );
      } catch (final IOException e) {
        throw context.current().failed(
          LexicalPositions.zeroWithFile(this.source.source()),
          IS_FATAL,
          Optional.empty(),
          e,
          "errorIO"
        );
      }
    }

    if (context.current().errorCount() > 0) {
      throw new CBParseFailedException(IS_NOT_FATAL);
    }

    return this.processDeclarations(context.current(), language, declarations);
  }

  private CBASTPackage processDeclarations(
    final CBParseContextType context,
    final CBASTLanguage language,
    final List<CBASTDeclarationType> declarations)
    throws CBParseFailedException
  {
    final var name =
      this.findName(context, declarations);
    final var imports =
      findImports(declarations);
    final var types =
      findTypes(declarations);
    final var protocols =
      findProtocols(declarations);

    return CBASTPackage.builder()
      .setName(name.name())
      .setLanguage(language)
      .setImports(imports)
      .setTypes(types)
      .setProtocols(protocols)
      .build();
  }

  private CBASTPackageDeclaration findName(
    final CBParseContextType context,
    final List<CBASTDeclarationType> declarations)
    throws CBParseFailedException
  {
    final var names =
      declarations.stream()
        .filter(i -> i instanceof CBASTPackageDeclaration)
        .map(x -> (CBASTPackageDeclaration) x)
        .collect(Collectors.toList());

    if (names.isEmpty()) {
      throw context.failed(
        LexicalPositions.zeroWithFile(this.source.source()),
        IS_NOT_FATAL,
        SPEC_SECTION_PACKAGE_NAME_MISSING,
        "errorPackageNameMissing"
      );
    }

    if (names.size() > 1) {
      throw context.failed(
        names.get(0).lexical(),
        IS_NOT_FATAL,
        SPEC_SECTION_PACKAGE_NAME_MULTIPLE,
        "errorPackageNameMultiple"
      );
    }

    return names.get(0);
  }

  @Override
  public void close()
    throws IOException
  {
    this.source.close();
  }
}
