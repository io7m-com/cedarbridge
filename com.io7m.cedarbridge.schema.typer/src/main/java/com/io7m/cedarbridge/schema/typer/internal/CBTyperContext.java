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

package com.io7m.cedarbridge.schema.typer.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionLineLogType;
import com.io7m.cedarbridge.schema.ast.CBASTLanguage;
import com.io7m.cedarbridge.schema.names.CBSpecificationLocation;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckFailedException;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jlexing.core.LexicalPosition;

import java.net.URI;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.io7m.cedarbridge.errors.CBErrorType.Severity.ERROR;

/**
 * Contextual information for type checking.
 */

public final class CBTyperContext
{
  private final LinkedList<Context> stack;
  private final CBStringsType strings;
  private final Consumer<CBError> errorConsumer;
  private final CBExpressionLineLogType lineLog;
  private final CBASTLanguage language;
  private int errors;

  /**
   * Contextual information for type checking.
   *
   * @param inStrings       String resources
   * @param inErrorConsumer A consumer of errors
   * @param inLanguage      The language being checked
   * @param inLineLog       The line log for incoming expressions
   */

  public CBTyperContext(
    final CBStringsType inStrings,
    final CBExpressionLineLogType inLineLog,
    final CBASTLanguage inLanguage,
    final Consumer<CBError> inErrorConsumer)
  {
    this.language = Objects.requireNonNull(inLanguage, "inLanguage");
    Objects.requireNonNull(inErrorConsumer, "errorConsumer");

    this.lineLog =
      Objects.requireNonNull(inLineLog, "lineLog");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");

    this.errorConsumer = error -> {
      ++this.errors;
      inErrorConsumer.accept(error);
    };

    this.stack = new LinkedList<>();
    this.stack.push(new Context(this, null));
  }

  /**
   * @return The current typing context
   */

  public CBTyperContextType current()
  {
    return this.stack.peek();
  }

  private static final class Context implements CBTyperContextType
  {
    private final CBTyperContext root;
    private final Context parent;

    Context(
      final CBTyperContext inRoot,
      final Context inParent)
    {
      this.root =
        Objects.requireNonNull(inRoot, "root");
      this.parent =
        inParent;
    }

    @Override
    public void close()
    {
      if (this.root.stack.size() > 1) {
        this.root.stack.pop();
      }
    }

    private URI quoteSpec(
      final UUID uuid)
    {
      final var language = this.root.language;
      return CBSpecificationLocation.quoteSpec(
        language.major().intValueExact(),
        language.minor().intValueExact(),
        uuid
      );
    }

    @Override
    public CBTypeCheckFailedException failed(
      final Optional<UUID> specSection,
      final LexicalPosition<URI> lexical,
      final String errorCode,
      final Object... arguments)
    {
      Objects.requireNonNull(specSection, "specSection");
      Objects.requireNonNull(lexical, "lexical");
      Objects.requireNonNull(errorCode, "errorCode");

      final var error =
        this.root.strings.format(errorCode, arguments);
      final var context =
        this.root.lineLog.contextualize(lexical).orElse("");

      final String text;
      if (specSection.isPresent()) {
        text = this.root.strings.format(
          "errorTypeWithSpec",
          lexical.file().orElse(URI.create("urn:unspecified")),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error,
          this.quoteSpec(specSection.get()),
          context
        );
      } else {
        text = this.root.strings.format(
          "errorType",
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
          .setException(new CBTypeCheckFailedException())
          .setLexical(lexical)
          .setMessage(text)
          .setSeverity(ERROR)
          .build();

      this.root.errorConsumer.accept(errorValue);
      return new CBTypeCheckFailedException();
    }

  }
}
