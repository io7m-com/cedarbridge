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
import com.io7m.cedarbridge.errors.CBExceptionTracker;
import com.io7m.cedarbridge.exprsrc.api.CBExpressionLineLogType;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBinderType;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.loader.api.CBLoaderType;
import com.io7m.cedarbridge.strings.api.CBStringsType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;

public final class CBBinder implements CBBinderType
{
  /**
   * The record spec section.
   */

  public static final Optional<UUID> SPEC_SEMANTICS_RECORD =
    uuid("1cf38aec-7544-4b5e-a5ac-bb01567ffe77");

  /**
   * The variant spec section.
   */

  public static final Optional<UUID> SPEC_SEMANTICS_VARIANT =
    uuid("17e2eaa4-f6ca-4a18-bd37-3f8e1be17247");

  /**
   * The protocol spec section.
   */

  public static final Optional<UUID> SPEC_SEMANTICS_PROTOCOL =
    uuid("3db896a5-bad2-4b08-9154-b48943f6f5a3");

  private final CBLoaderType loader;
  private final CBExpressionLineLogType lineLog;
  private final CBASTPackage parsedPackage;
  private final CBStringsType strings;
  private final Consumer<CBError> errors;

  public CBBinder(
    final CBStringsType inStrings,
    final CBLoaderType inLoader,
    final Consumer<CBError> inErrors,
    final CBExpressionLineLogType inLineLog,
    final CBASTPackage inParsedPackage)
  {
    this.strings =
      Objects.requireNonNull(inStrings, "inStrings");
    this.loader =
      Objects.requireNonNull(inLoader, "loader");
    this.errors =
      Objects.requireNonNull(inErrors, "errors");
    this.lineLog =
      Objects.requireNonNull(inLineLog, "inLineLog");
    this.parsedPackage =
      Objects.requireNonNull(inParsedPackage, "parsedPackage");
  }

  private static void bindTypeDeclarations(
    final CBBinderContextType context,
    final List<CBASTTypeDeclarationType> types)
    throws CBBindFailedException
  {
    final var exceptions = new CBExceptionTracker<CBBindFailedException>();
    for (final var type : types) {
      try (var subContext = context.openBindingScope()) {
        try {
          new CBTypeDeclarationBinder().bind(subContext, type);
        } catch (final CBBindFailedException e) {
          exceptions.addException(e);
        }
      }
    }
    exceptions.throwIfNecessary();
  }

  private static void collectTopLevelBindings(
    final CBBinderContextType context,
    final List<CBASTTypeDeclarationType> types,
    final List<CBASTProtocolDeclaration> protocols)
    throws CBBindFailedException
  {
    final var exceptions = new CBExceptionTracker<CBBindFailedException>();
    for (final var typeV : types) {
      final var name = typeV.name();
      try {
        final var binding =
          context.bindType(specSectionForType(typeV), typeV);
        name.userData().put(CBBindingType.class, binding);
      } catch (final CBBindFailedException e) {
        exceptions.addException(e);
      }
    }
    for (final var proto : protocols) {
      final var name = proto.name();
      try {
        final var binding =
          context.bindProtocol(SPEC_SEMANTICS_PROTOCOL, proto);
        name.userData().put(CBBindingType.class, binding);
      } catch (final CBBindFailedException e) {
        exceptions.addException(e);
      }
    }
    exceptions.throwIfNecessary();
  }

  private static Optional<UUID> specSectionForType(
    final CBASTTypeDeclarationType typeV)
  {
    if (typeV instanceof CBASTTypeRecord) {
      return SPEC_SEMANTICS_RECORD;
    }
    if (typeV instanceof CBASTTypeVariant) {
      return SPEC_SEMANTICS_VARIANT;
    }
    return Optional.empty();
  }

  private static void bindProtoDeclarations(
    final CBBinderContextType context,
    final List<CBASTProtocolDeclaration> protos)
    throws CBBindFailedException
  {
    final var exceptions = new CBExceptionTracker<CBBindFailedException>();
    for (final var proto : protos) {
      try {
        new CBProtocolDeclarationBinder().bind(context, proto);
      } catch (final CBBindFailedException e) {
        exceptions.addException(e);
      }
    }
    exceptions.throwIfNecessary();
  }

  @Override
  public void execute()
    throws CBBindFailedException
  {
    final var context =
      new CBBinderContext(
        this.strings,
        this.loader,
        this.lineLog,
        this.errors,
        this.parsedPackage.name().text(),
        this.parsedPackage.language()
      );

    final var contextMain = context.current();
    this.processImports(contextMain);
    this.processTypes(contextMain);
    this.processProtocols(contextMain);
  }

  private void processProtocols(
    final CBBinderContextType context)
    throws CBBindFailedException
  {
    bindProtoDeclarations(context, this.parsedPackage.protocols());
  }

  private void processTypes(
    final CBBinderContextType context)
    throws CBBindFailedException
  {
    final var types = this.parsedPackage.types();
    collectTopLevelBindings(context, types, this.parsedPackage.protocols());
    bindTypeDeclarations(context, types);
  }

  private void processImports(
    final CBBinderContextType context)
    throws CBBindFailedException
  {
    final var exceptions = new CBExceptionTracker<CBBindFailedException>();
    for (final var importV : this.parsedPackage.imports()) {
      try {
        new CBImportBinder().bind(context, importV);
      } catch (final CBBindFailedException e) {
        exceptions.addException(e);
      }
    }
    exceptions.throwIfNecessary();
  }

  @Override
  public void close()
  {

  }
}
