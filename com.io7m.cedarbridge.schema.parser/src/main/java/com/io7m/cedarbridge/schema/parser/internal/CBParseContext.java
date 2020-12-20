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
import com.io7m.cedarbridge.exprsrc.api.CBExpressionLineLogType;
import com.io7m.cedarbridge.schema.names.CBSpecificationLocation;
import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.io7m.cedarbridge.errors.CBErrorType.Severity.ERROR;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal;
import static com.io7m.cedarbridge.schema.parser.api.CBParseFailedException.Fatal.IS_NOT_FATAL;

public final class CBParseContext
{
  private final LinkedList<Context> stack;
  private final CBStringsType strings;
  private final Consumer<CBError> errorConsumer;
  private final CBExpressionLineLogType lines;
  private int errors;
  private int languageMajor;
  private int languageMinor;

  public CBParseContext(
    final CBStringsType inStrings,
    final CBExpressionLineLogType inLines,
    final Consumer<CBError> inErrorConsumer)
  {
    Objects.requireNonNull(inErrorConsumer, "inErrorConsumer");

    this.strings =
      Objects.requireNonNull(inStrings, "strings");
    this.lines =
      Objects.requireNonNull(inLines, "inLines");

    this.errorConsumer = error -> {
      ++this.errors;
      inErrorConsumer.accept(error);
    };

    this.stack = new LinkedList<>();
    this.stack.push(new Context(this, "?", List.of("?")));

    this.languageMajor = 1;
    this.languageMinor = 0;
  }

  public CBParseContextType current()
  {
    return this.stack.peek();
  }

  private static final class Context implements CBParseContextType
  {
    private final CBParseContext root;
    private final String expectingKind;
    private final List<String> expectingShapes;

    Context(
      final CBParseContext inRoot,
      final String inExpectingKind,
      final List<String> inExpectingShapes)
    {
      this.root =
        Objects.requireNonNull(inRoot, "root");
      this.expectingKind =
        Objects.requireNonNull(inExpectingKind, "expectingKind");
      this.expectingShapes =
        Objects.requireNonNull(inExpectingShapes, "expectingShapes");
    }

    @Override
    public CBParseContextType openExpectingOneOf(
      final String kind,
      final List<String> shapes)
    {
      final var newContext = new Context(this.root, kind, shapes);
      this.root.stack.push(newContext);
      return newContext;
    }

    @Override
    public void close()
    {
      if (this.root.stack.size() > 1) {
        this.root.stack.pop();
      }
    }

    @Override
    public void setLanguageVersion(
      final int major,
      final int minor)
    {
      this.root.languageMajor = major;
      this.root.languageMinor = minor;
    }

    @Override
    public <T extends SExpressionType> T checkExpressionIs(
      final SExpressionType expression,
      final Optional<UUID> specSection,
      final Class<T> clazz)
      throws CBParseFailedException
    {
      if (!clazz.isInstance(expression)) {
        throw this.failed(
          expression,
          IS_NOT_FATAL,
          specSection,
          "errorUnexpectedExpressionForm");
      }
      return clazz.cast(expression);
    }

    @Override
    public SExpressionSymbolType checkExpressionIsKeyword(
      final SExpressionType expression,
      final Optional<UUID> specSection,
      final String name,
      final String errorCode)
      throws CBParseFailedException
    {
      if (expression instanceof SExpressionSymbolType) {
        final var symbol = (SExpressionSymbolType) expression;
        if (!Objects.equals(symbol.text(), name)) {
          throw this.failed(expression, IS_NOT_FATAL, specSection, errorCode);
        }
        return symbol;
      }
      throw this.failed(expression, IS_NOT_FATAL, specSection, errorCode);
    }

    @Override
    public CBParseFailedException failed(
      final SExpressionType expression,
      final Fatal fatal,
      final Optional<UUID> specSection,
      final String errorCode,
      final Exception e)
    {
      Objects.requireNonNull(expression, "expression");
      Objects.requireNonNull(fatal, "fatal");
      Objects.requireNonNull(specSection, "specSection");
      Objects.requireNonNull(errorCode, "messageId");

      final var lexical =
        expression.lexical();
      final var error =
        this.root.strings.format(errorCode);
      final var expectedPattern =
        String.join("\n  | ", this.expectingShapes);
      final var expectedKindTranslated =
        this.root.strings.format(this.expectingKind);
      final var context =
        this.root.lines.contextualize(lexical).orElse("");

      final String text;
      if (specSection.isPresent()) {
        text = this.root.strings.format(
          "errorParseWithSpec",
          lexical.file().orElse(URI.create("urn:unspecified")),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error,
          context,
          expectedKindTranslated,
          CBSpecificationLocation.quoteSpec(
            this.root.languageMajor,
            this.root.languageMinor,
            specSection.get()
          ),
          expectedPattern
        );
      } else {
        text = this.root.strings.format(
          "errorParse",
          lexical.file().orElse(URI.create("urn:unspecified")),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error,
          context,
          expectedKindTranslated,
          expectedPattern
        );
      }

      final var errorValue =
        CBError.builder()
          .setErrorCode(errorCode)
          .setException(Objects.requireNonNullElseGet(
            e,
            () -> new CBParseFailedException(fatal)))
          .setLexical(lexical)
          .setMessage(text)
          .setSeverity(ERROR)
          .build();

      this.root.errorConsumer.accept(errorValue);
      return new CBParseFailedException(fatal);
    }


    @Override
    public CBParseFailedException failed(
      final SExpressionType expression,
      final Fatal fatal,
      final Optional<UUID> specSection,
      final String errorCode)
    {
      Objects.requireNonNull(expression, "expression");
      Objects.requireNonNull(errorCode, "messageId");
      Objects.requireNonNull(specSection, "specSection");
      return this.failed(expression, fatal, specSection, errorCode, null);
    }

    @Override
    public int errorCount()
    {
      return this.root.errors;
    }

    @Override
    public CBParseFailedException failed(
      final LexicalPosition<URI> lexical,
      final Fatal fatal,
      final Optional<UUID> specSection,
      final Exception exception,
      final String errorCode)
    {
      Objects.requireNonNull(lexical, "lexical");
      Objects.requireNonNull(fatal, "fatal");
      Objects.requireNonNull(specSection, "specSection");
      Objects.requireNonNull(errorCode, "errorCode");

      final var error =
        this.root.strings.format(errorCode);
      final var context =
        this.root.lines.contextualize(lexical).orElse("");

      final String text;
      if (specSection.isPresent()) {
        text = this.root.strings.format(
          "errorParseBasicWithSpec",
          lexical.file().orElse(URI.create("urn:unspecified")),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error,
          CBSpecificationLocation.quoteSpec(
            this.root.languageMajor,
            this.root.languageMinor,
            specSection.get()
          ),
          context
        );
      } else {
        text = this.root.strings.format(
          "errorParseBasic",
          lexical.file().orElse(URI.create("urn:unspecified")),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error,
          context
        );
      }


      final var errorValue =
        CBError.builder()
          .setErrorCode(errorCode)
          .setException(Objects.requireNonNullElseGet(
            exception,
            () -> new CBParseFailedException(fatal)))
          .setLexical(lexical)
          .setMessage(text)
          .setSeverity(ERROR)
          .build();

      this.root.errorConsumer.accept(errorValue);
      return new CBParseFailedException(fatal);
    }

    @Override
    public CBParseFailedException failed(
      final LexicalPosition<URI> lexical,
      final Fatal fatal,
      final Optional<UUID> specSection,
      final String errorCode)
    {
      return this.failed(
        lexical,
        fatal,
        specSection,
        new CBParseFailedException(fatal),
        errorCode
      );
    }
  }
}
