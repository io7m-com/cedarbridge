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
import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalFieldName;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalProtocolDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalProtocolVersionDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeParameter;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalVariantCase;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.loader.api.CBLoaderType;
import com.io7m.cedarbridge.strings.api.CBStringsType;
import com.io7m.jlexing.core.LexicalPosition;

import java.math.BigInteger;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.io7m.cedarbridge.errors.CBErrorType.Severity.ERROR;
import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType;

public final class CBBinderContext
{
  private final LinkedList<Context> stack;
  private final CBStringsType strings;
  private final Consumer<CBError> errorConsumer;
  private final CBExpressionLineLogType lineLog;
  private final HashMap<String, CBPackageType> packagesByShortName;
  private final HashMap<String, LexicalPosition<URI>> packagesByShortNameImports;
  private final CBLoaderType loader;
  private final String packageName;
  private BigInteger idPool;
  private int errors;

  public CBBinderContext(
    final CBStringsType inStrings,
    final CBLoaderType inLoader,
    final CBExpressionLineLogType inLineLog,
    final Consumer<CBError> inErrorConsumer,
    final String inPackageName)
  {
    this.packageName =
      Objects.requireNonNull(inPackageName, "inPackageName");
    Objects.requireNonNull(inErrorConsumer, "errorConsumer");

    this.loader =
      Objects.requireNonNull(inLoader, "loader");
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

    this.idPool =
      BigInteger.ONE;
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
    private final Context parent;
    private final Map<String, CBBindingLocalType> typeBindings;
    private final Map<String, CBBindingLocalType> fieldBindings;
    private final Map<String, CBBindingLocalType> caseBindings;
    private final Map<String, CBBindingLocalProtocolDeclaration> protoBindings;
    private final Map<BigInteger, CBBindingLocalProtocolVersionDeclaration> protoVersionBindings;

    Context(
      final CBBinderContext inRoot,
      final Context inParent)
    {
      this.root =
        Objects.requireNonNull(inRoot, "root");
      this.parent =
        inParent;
      this.typeBindings =
        new HashMap<>();
      this.fieldBindings =
        new HashMap<>();
      this.caseBindings =
        new HashMap<>();
      this.protoBindings =
        new HashMap<>();
      this.protoVersionBindings =
        new HashMap<>();
    }

    @Override
    public CBLoaderType loader()
    {
      return this.root.loader;
    }

    @Override
    public CBBinderContextType openBindingScope()
    {
      final var context = new Context(this.root, this);
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

    private CBBindingLocalType findTypeBinding(
      final String name)
    {
      Objects.requireNonNull(name, "name");

      final var existing = this.typeBindings.get(name);
      if (existing != null) {
        return existing;
      }
      if (this.parent != null) {
        return this.parent.findTypeBinding(name);
      }
      return null;
    }

    private CBBindingLocalType findProtoBinding(
      final String name)
    {
      Objects.requireNonNull(name, "name");

      final var existing = this.protoBindings.get(name);
      if (existing != null) {
        return existing;
      }
      if (this.parent != null) {
        return this.parent.findTypeBinding(name);
      }
      return null;
    }

    private CBBindingLocalType findFieldBinding(
      final String name)
    {
      Objects.requireNonNull(name, "name");

      final var existing = this.fieldBindings.get(name);
      if (existing != null) {
        return existing;
      }
      if (this.parent != null) {
        return this.parent.findFieldBinding(name);
      }
      return null;
    }

    private CBBindingLocalType findCaseBinding(
      final String name)
    {
      Objects.requireNonNull(name, "name");

      final var existing = this.caseBindings.get(name);
      if (existing != null) {
        return existing;
      }
      if (this.parent != null) {
        return this.parent.findCaseBinding(name);
      }
      return null;
    }

    private CBBindingLocalProtocolVersionDeclaration findVersionBinding(
      final BigInteger version)
    {
      Objects.requireNonNull(version, "version");

      final var existing =
        this.protoVersionBindings.get(version);
      if (existing != null) {
        return existing;
      }
      if (this.parent != null) {
        return this.parent.findVersionBinding(version);
      }
      return null;
    }

    @Override
    public CBBindingLocalType bindType(
      final CBASTTypeDeclarationType type)
      throws CBBindFailedException
    {
      Objects.requireNonNull(type, "type");

      final var lexical = type.lexical();
      final var name = type.name().text();
      final var existing = this.findTypeBinding(name);
      if (existing != null) {
        throw this.failedWithOther(
          lexical,
          existing.lexical(),
          "errorBindingConflict",
          name
        );
      }

      final var binding =
        CBBindingLocalTypeDeclaration.builder()
          .setLexical(lexical)
          .setId(this.root.idPool)
          .setName(name)
          .setType(type)
          .build();

      this.root.idPool = this.root.idPool.add(BigInteger.ONE);
      this.typeBindings.put(name, binding);
      return binding;
    }

    @Override
    public CBBindingLocalType bindProtocol(
      final CBASTProtocolDeclaration proto)
      throws CBBindFailedException
    {
      Objects.requireNonNull(proto, "proto");

      final var lexical = proto.lexical();
      final var name = proto.name().text();
      final var existing = this.findProtoBinding(name);
      if (existing != null) {
        throw this.failedWithOther(
          lexical,
          existing.lexical(),
          "errorBindingConflict",
          name
        );
      }

      final var binding =
        CBBindingLocalProtocolDeclaration.builder()
          .setLexical(lexical)
          .setId(this.root.idPool)
          .setName(name)
          .setProtocol(proto)
          .build();

      this.root.idPool = this.root.idPool.add(BigInteger.ONE);
      this.protoBindings.put(name, binding);
      return binding;
    }

    @Override
    public CBBindingLocalType bindTypeParameter(
      final String name,
      final LexicalPosition<URI> lexical)
      throws CBBindFailedException
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(lexical, "lexical");

      final var existing = this.findTypeBinding(name);
      if (existing != null) {
        throw this.failedWithOther(
          lexical,
          existing.lexical(),
          "errorBindingConflict",
          name
        );
      }

      final var binding =
        CBBindingLocalTypeParameter.builder()
          .setLexical(lexical)
          .setId(this.root.idPool)
          .setName(name)
          .build();

      this.root.idPool = this.root.idPool.add(BigInteger.ONE);
      this.typeBindings.put(name, binding);
      return binding;
    }

    @Override
    public CBBindingLocalType bindField(
      final String name,
      final LexicalPosition<URI> lexical)
      throws CBBindFailedException
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(lexical, "lexical");

      final var existing = this.findFieldBinding(name);
      if (existing != null) {
        throw this.failedWithOther(
          lexical,
          existing.lexical(),
          "errorBindingConflict",
          name
        );
      }

      final var binding =
        CBBindingLocalFieldName.builder()
          .setLexical(lexical)
          .setId(this.root.idPool)
          .setName(name)
          .build();

      this.root.idPool = this.root.idPool.add(BigInteger.ONE);
      this.fieldBindings.put(name, binding);
      return binding;
    }

    @Override
    public CBBindingLocalType checkTypeBinding(
      final String text,
      final LexicalPosition<URI> lexical)
      throws CBBindFailedException
    {
      final var existing = this.findTypeBinding(text);
      if (existing == null) {
        throw this.failed(
          lexical,
          "errorBindingMissing",
          text
        );
      }
      return existing;
    }

    @Override
    public CBBindingLocalType bindVariantCase(
      final String name,
      final LexicalPosition<URI> lexical)
      throws CBBindFailedException
    {
      Objects.requireNonNull(name, "name");
      Objects.requireNonNull(lexical, "lexical");

      final var existing = this.findCaseBinding(name);
      if (existing != null) {
        throw this.failedWithOther(
          lexical,
          existing.lexical(),
          "errorBindingConflict",
          name
        );
      }

      final var binding =
        CBBindingLocalVariantCase.builder()
          .setLexical(lexical)
          .setId(this.root.idPool)
          .setName(name)
          .build();

      this.root.idPool = this.root.idPool.add(BigInteger.ONE);
      this.caseBindings.put(name, binding);
      return binding;
    }

    @Override
    public CBPackageType checkPackageBinding(
      final String text,
      final LexicalPosition<URI> lexical)
      throws CBBindFailedException
    {
      Objects.requireNonNull(text, "text");
      Objects.requireNonNull(lexical, "lexical");

      final var pack = this.root.packagesByShortName.get(text);
      if (pack == null) {
        throw this.failed(
          lexical,
          "errorPackageUnavailable",
          text
        );
      }

      return pack;
    }

    @Override
    public String currentPackage()
    {
      return this.root.packageName;
    }

    @Override
    public CBBindingLocalType bindProtocolVersion(
      final BigInteger version,
      final LexicalPosition<URI> lexical)
      throws CBBindFailedException
    {
      Objects.requireNonNull(version, "version");
      Objects.requireNonNull(lexical, "lexical");

      final var existing = this.findVersionBinding(version);
      if (existing != null) {
        throw this.failedWithOther(
          lexical,
          existing.lexical(),
          "errorBindingConflict",
          version
        );
      }

      final var binding =
        CBBindingLocalProtocolVersionDeclaration.builder()
          .setLexical(lexical)
          .setId(this.root.idPool)
          .setVersion(version)
          .setName(version.toString())
          .build();

      this.root.idPool = this.root.idPool.add(BigInteger.ONE);
      this.protoVersionBindings.put(version, binding);
      return binding;
    }
  }
}
