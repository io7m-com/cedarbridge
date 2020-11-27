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

package com.io7m.cedarbridge.schema.binder.internal;

import com.io7m.cedarbridge.errors.CBError;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionLineLogType;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jlexing.core.LexicalPosition;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

import static com.io7m.cedarbridge.errors.CBErrorType.Severity.ERROR;

public final class CBBinderContext
{
  private final LinkedList<Context> stack;
  private final CBStringsType strings;
  private final Consumer<CBError> errorConsumer;
  private final CBExpressionLineLogType lineLog;
  private final HashMap<String, CBPackageType> packagesByShortName;
  private final HashMap<String, LexicalPosition<URI>> packagesByShortNameImports;
  private int errors;

  public CBBinderContext(
    final CBStringsType inStrings,
    final CBExpressionLineLogType inLineLog,
    final Consumer<CBError> inErrorConsumer)
  {
    Objects.requireNonNull(inErrorConsumer, "inErrorConsumer");

    this.lineLog =
      Objects.requireNonNull(inLineLog, "inLineLog");
    this.strings =
      Objects.requireNonNull(inStrings, "strings");

    this.errorConsumer = error -> {
      ++this.errors;
      inErrorConsumer.accept(error);
    };

    this.stack = new LinkedList<>();
    this.stack.push(new Context(this));

    this.packagesByShortName =
      new HashMap<>();
    this.packagesByShortNameImports =
      new HashMap<>();
  }

  public CBBinderContextType current()
  {
    return this.stack.peek();
  }

  public int errorCount()
  {
    return this.errors;
  }

  private static final class Context implements CBBinderContextType
  {
    private final CBBinderContext root;

    Context(
      final CBBinderContext inRoot)
    {
      this.root =
        Objects.requireNonNull(inRoot, "root");
    }

    @Override
    public CBBinderContextType openBindingScope()
    {
      final var context = new Context(this.root);
      this.root.stack.push(context);
      return context;
    }

    @Override
    public void close()
    {
      if (this.root.stack.size() > 1) {
        this.root.stack.pop();
      }
    }

    @Override
    public void registerPackage(
      final LexicalPosition<URI> lexical,
      final String text,
      final CBPackageType packageV)
      throws CBBindFailedException
    {
      if (this.root.packagesByShortName.containsKey(text)) {
        throw this.failedWithOther(
          lexical,
          this.root.packagesByShortNameImports.get(text),
          "errorPackageShortNameUsed",
          text
        );
      }

      this.root.packagesByShortName.put(text, packageV);
      this.root.packagesByShortNameImports.put(text, lexical);
    }

    @Override
    public CBBindFailedException failed(
      final LexicalPosition<URI> lexical,
      final String errorCode,
      final Object... arguments)
    {
      Objects.requireNonNull(lexical, "lexical");
      Objects.requireNonNull(errorCode, "errorCode");

      final var error =
        this.root.strings.format(errorCode, arguments);
      final var context =
        this.root.lineLog.contextualize(lexical).orElse("");

      final var text =
        this.root.strings.format(
          "errorBind",
          lexical.file().orElse(URI.create("urn:unspecified")),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error,
          context
        );

      final var errorValue =
        CBError.builder()
          .setErrorCode(errorCode)
          .setException(new CBBindFailedException())
          .setLexical(lexical)
          .setMessage(text)
          .setSeverity(ERROR)
          .build();

      this.root.errorConsumer.accept(errorValue);
      return new CBBindFailedException();
    }

    @Override
    public CBBindFailedException failedWithOther(
      final LexicalPosition<URI> lexical,
      final LexicalPosition<URI> lexicalOther,
      final String errorCode,
      final Object... arguments)
    {
      Objects.requireNonNull(lexical, "lexical");
      Objects.requireNonNull(lexicalOther, "lexicalOther");
      Objects.requireNonNull(errorCode, "errorCode");

      final var error =
        this.root.strings.format(errorCode, arguments);
      final var context =
        this.root.lineLog.contextualize(lexical).orElse("");
      final var contextOther =
        this.root.lineLog.contextualize(lexicalOther).orElse("");

      final var text =
        this.root.strings.format(
          "errorBindWithOther",
          lexical.file().orElse(URI.create("urn:unspecified")),
          Integer.valueOf(lexical.line()),
          Integer.valueOf(lexical.column()),
          error,
          context,
          lexicalOther.file().orElse(URI.create("urn:unspecified")),
          Integer.valueOf(lexicalOther.line()),
          Integer.valueOf(lexicalOther.column()),
          contextOther
        );

      final var errorValue =
        CBError.builder()
          .setErrorCode(errorCode)
          .setException(new CBBindFailedException())
          .setLexical(lexical)
          .setMessage(text)
          .setSeverity(ERROR)
          .build();

      this.root.errorConsumer.accept(errorValue);
      return new CBBindFailedException();
    }
  }
}
