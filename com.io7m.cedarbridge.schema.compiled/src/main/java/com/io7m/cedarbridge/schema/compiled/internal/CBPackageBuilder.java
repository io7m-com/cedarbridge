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

package com.io7m.cedarbridge.schema.compiled.internal;

import com.io7m.cedarbridge.schema.compiled.CBPackageBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBProtocolVersionBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBRecordBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.cedarbridge.schema.compiled.CBTypeParameterType;
import com.io7m.cedarbridge.schema.compiled.CBVariantBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBVariantCaseBuilderType;
import com.io7m.cedarbridge.schema.names.CBFieldNames;
import com.io7m.cedarbridge.schema.names.CBPackageNames;
import com.io7m.cedarbridge.schema.names.CBTypeNames;
import com.io7m.cedarbridge.schema.names.CBTypeParameterNames;
import com.io7m.cedarbridge.schema.names.CBVariantCaseNames;
import com.io7m.jaffirm.core.PreconditionViolationException;
import com.io7m.jaffirm.core.Preconditions;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprParameterType;

/**
 * A package builder.
 */

public final class CBPackageBuilder implements CBPackageBuilderType
{
  private final String packageName;
  private final Map<String, CBTypeDeclarationBuilderType> typeBuilders;
  private final CBPackage packageNow;
  private final HashMap<String, ProtocolBuilder> protoBuilders;
  private boolean done;

  /**
   * Construct a package builder.
   *
   * @param inPackageName The package name
   */

  public CBPackageBuilder(
    final String inPackageName)
  {
    this.packageName =
      Objects.requireNonNull(inPackageName, "packageName");

    this.typeBuilders = new HashMap<>();
    this.protoBuilders = new HashMap<>();
    try {
      this.packageNow = new CBPackage(this.packageName);
    } catch (final PreconditionViolationException e) {
      throw new IllegalArgumentException(e);
    }
    this.done = false;
  }

  private void checkNotDone()
  {
    if (this.done) {
      throw new IllegalStateException("Builder has already completed");
    }
  }

  @Override
  public CBTypeDeclarationBuilderType createExternalType(
    final String inExternalPackageName,
    final String inExternalName,
    final String inTypeName)
  {
    Objects.requireNonNull(inExternalPackageName, "externalPackageName");
    Objects.requireNonNull(inExternalName, "externalName");
    Objects.requireNonNull(inTypeName, "name");

    this.checkNotDone();

    CBPackageNames.INSTANCE.checkValid(inExternalPackageName);
    CBTypeNames.INSTANCE.checkValid(inExternalName);
    CBTypeNames.INSTANCE.checkValid(inTypeName);

    if (this.typeBuilders.containsKey(inTypeName)) {
      throw new IllegalArgumentException(
        String.format("Type name %s already used", inTypeName)
      );
    }

    final var builder =
      new ExternalBuilder(inTypeName, inExternalPackageName, inExternalName);
    this.typeBuilders.put(inTypeName, builder);
    this.packageNow.addExternal(builder.external);
    return builder;
  }

  @Override
  public CBRecordBuilderType createRecord(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    this.checkNotDone();

    CBTypeNames.INSTANCE.checkValid(name);

    if (this.typeBuilders.containsKey(name)) {
      throw new IllegalArgumentException(
        String.format("Type name %s already used", name)
      );
    }

    final var builder = new RecordBuilder(name);
    this.typeBuilders.put(name, builder);
    this.packageNow.addRecord(builder.record);
    return builder;
  }

  @Override
  public CBRecordBuilderType findRecord(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    final var builder = this.typeBuilders.get(name);
    if (builder == null) {
      throw new IllegalStateException(
        String.format("No such type: %s", name)
      );
    }
    if (builder instanceof CBRecordBuilderType) {
      return (CBRecordBuilderType) builder;
    }
    throw new IllegalStateException(
      String.format("Not a record type: %s", name)
    );
  }

  @Override
  public CBVariantBuilderType createVariant(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    this.checkNotDone();

    CBTypeNames.INSTANCE.checkValid(name);

    if (this.typeBuilders.containsKey(name)) {
      throw new IllegalArgumentException(
        String.format("Type name %s already used", name)
      );
    }

    final var builder = new VariantBuilder(name);
    this.typeBuilders.put(name, builder);
    this.packageNow.addVariant(builder.variant);
    return builder;
  }

  @Override
  public CBVariantBuilderType findVariant(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    this.checkNotDone();

    final var builder = this.typeBuilders.get(name);
    if (builder == null) {
      throw new IllegalStateException(
        String.format("No such type: %s", name)
      );
    }
    if (builder instanceof CBVariantBuilderType) {
      return (CBVariantBuilderType) builder;
    }
    throw new IllegalStateException(
      String.format("Not a variant type: %s", name)
    );
  }

  @Override
  public CBTypeDeclarationBuilderType findType(
    final String name)
  {
    this.checkNotDone();
    return Optional.ofNullable(this.typeBuilders.get(name))
      .orElseThrow(() -> new IllegalArgumentException(
        String.format("No such type: %s", name)
      ));
  }

  @Override
  public CBTypeExprNamedType referenceType(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    this.checkNotDone();

    final var type = this.findType(name);
    if (type instanceof VariantBuilder) {
      return new CBTypeExpressionNamed(((VariantBuilder) type).variant);
    }
    if (type instanceof RecordBuilder) {
      return new CBTypeExpressionNamed(((RecordBuilder) type).record);
    }

    throw new UnsupportedOperationException();
  }

  @Override
  public CBTypeExprNamedType referenceExternalType(
    final CBTypeDeclarationType typeDeclaration)
  {
    Objects.requireNonNull(typeDeclaration, "typeDeclaration");
    this.checkNotDone();
    return new CBTypeExpressionNamed(typeDeclaration);
  }

  @Override
  public CBPackageType build()
  {
    this.checkNotDone();
    this.done = true;
    return this.packageNow;
  }

  @Override
  public CBPackageBuilderType addImport(
    final CBPackageType imported)
  {
    this.checkNotDone();
    this.packageNow.addImport(Objects.requireNonNull(imported, "imported"));
    return this;
  }

  @Override
  public CBProtocolBuilderType createProtocol(
    final String name)
  {
    Objects.requireNonNull(name, "name");

    this.checkNotDone();
    CBTypeNames.INSTANCE.checkValid(name);

    if (this.protoBuilders.containsKey(name)) {
      throw new IllegalArgumentException(
        String.format("Protocol name %s already used", name)
      );
    }

    final var builder = new ProtocolBuilder(name);
    this.protoBuilders.put(name, builder);
    this.packageNow.addProtocol(builder.protocol);
    return builder;
  }

  private final class ProtocolBuilder
    implements CBProtocolBuilderType
  {
    private final CBProtocolDeclaration protocol;

    ProtocolBuilder(
      final String inName)
    {
      this.protocol = new CBProtocolDeclaration(inName);
    }

    @Override
    public CBProtocolVersionBuilderType createVersion(
      final BigInteger version)
    {
      if (this.protocol.versions().containsKey(version)) {
        throw new IllegalArgumentException(
          String.format("Protocol version %s already exists", version)
        );
      }

      final var builder = new ProtocolVersionBuilder(this.protocol, version);
      this.protocol.addVersion(builder.version);
      return builder;
    }
  }

  private final class ProtocolVersionBuilder
    implements CBProtocolVersionBuilderType
  {
    private final CBProtocolVersionDeclaration version;

    ProtocolVersionBuilder(
      final CBProtocolDeclaration protocol,
      final BigInteger inVersion)
    {
      this.version = new CBProtocolVersionDeclaration(protocol, inVersion);
    }

    @Override
    public void addType(
      final CBTypeExprNamedType type)
    {
      Preconditions.checkPreconditionV(
        type.declaration().arity() == 0,
        "Type arity %d must be zero",
        Integer.valueOf(type.declaration().arity())
      );

      this.version.addType(type);
    }
  }

  private final class RecordBuilder implements CBRecordBuilderType
  {
    private final CBTypeDeclarationRecord record;

    RecordBuilder(
      final String name)
    {
      this.record = new CBTypeDeclarationRecord(name);
    }

    @Override
    public CBRecordBuilderType createField(
      final String name,
      final CBTypeExpressionType type)
    {
      CBPackageBuilder.this.checkNotDone();
      CBFieldNames.INSTANCE.checkValid(name);

      if (this.record.fieldsByName().containsKey(name)) {
        throw new IllegalArgumentException(
          String.format("Field name %s already used", name)
        );
      }

      this.record.addField(new CBField(name, type));
      return this;
    }

    @Override
    public CBTypeParameterType addTypeParameter(
      final String name)
    {
      CBPackageBuilder.this.checkNotDone();
      CBTypeParameterNames.INSTANCE.checkValid(name);

      if (this.record.parametersByName().containsKey(name)) {
        throw new IllegalArgumentException(
          String.format("Type parameter name %s already used", name)
        );
      }

      final var parameter = new CBTypeParameter(name, this.record.arity());
      this.record.addTypeParameter(parameter);
      return parameter;
    }

    @Override
    public void setExternalName(
      final String externalPackageName,
      final String name)
    {
      this.record.setExternalName(externalPackageName, name);
    }

    @Override
    public CBPackageBuilderType ownerPackage()
    {
      CBPackageBuilder.this.checkNotDone();
      return CBPackageBuilder.this;
    }

    @Override
    public CBTypeExprNamedType reference()
    {
      CBPackageBuilder.this.checkNotDone();
      return new CBTypeExpressionNamed(this.record);
    }

    @Override
    public CBTypeParameterType findParameter(
      final String name)
    {
      CBPackageBuilder.this.checkNotDone();
      return Optional.ofNullable(this.record.parametersByName().get(name))
        .orElseThrow(() -> new IllegalArgumentException(
          String.format("No such parameter: %s", name)
        ));
    }

    @Override
    public CBTypeExprParameterType referenceParameter(
      final String name)
    {
      CBPackageBuilder.this.checkNotDone();
      return new CBTypeExpressionParameter(this.findParameter(name));
    }
  }

  private final class VariantBuilder implements CBVariantBuilderType
  {
    private final CBTypeDeclarationVariant variant;

    VariantBuilder(
      final String name)
    {
      this.variant = new CBTypeDeclarationVariant(name);
    }

    @Override
    public CBVariantCaseBuilderType createCase(
      final String name)
    {
      CBPackageBuilder.this.checkNotDone();
      CBVariantCaseNames.INSTANCE.checkValid(name);

      if (this.variant.casesByName().containsKey(name)) {
        throw new IllegalArgumentException(
          String.format("Case name %s already used", name)
        );
      }

      final var caseV = new CBVariantCase(name);
      this.variant.addCase(caseV);
      return new CaseBuilder(this, caseV);
    }

    @Override
    public CBTypeParameterType addTypeParameter(
      final String name)
    {
      CBPackageBuilder.this.checkNotDone();
      CBTypeParameterNames.INSTANCE.checkValid(name);

      if (this.variant.parametersByName().containsKey(name)) {
        throw new IllegalArgumentException(
          String.format("Type parameter name %s already used", name)
        );
      }

      final var parameter = new CBTypeParameter(name, this.variant.arity());
      this.variant.addTypeParameter(parameter);
      return parameter;
    }

    @Override
    public void setExternalName(
      final String externalPackageName,
      final String name)
    {
      this.variant.setExternalName(externalPackageName, name);
    }

    @Override
    public CBPackageBuilderType ownerPackage()
    {
      CBPackageBuilder.this.checkNotDone();
      return CBPackageBuilder.this;
    }

    @Override
    public CBTypeExprNamedType reference()
    {
      CBPackageBuilder.this.checkNotDone();
      return new CBTypeExpressionNamed(this.variant);
    }

    @Override
    public CBTypeParameterType findParameter(
      final String name)
    {
      CBPackageBuilder.this.checkNotDone();
      return Optional.ofNullable(this.variant.parametersByName().get(name))
        .orElseThrow(() -> new IllegalArgumentException(
          String.format("No such parameter: %s", name)
        ));
    }

    @Override
    public CBTypeExprParameterType referenceParameter(
      final String name)
    {
      CBPackageBuilder.this.checkNotDone();
      return new CBTypeExpressionParameter(this.findParameter(name));
    }
  }

  private final class CaseBuilder implements CBVariantCaseBuilderType
  {
    private final CBVariantCase caseV;
    private final CBVariantBuilderType owner;

    CaseBuilder(
      final CBVariantBuilderType inOwner,
      final CBVariantCase inCaseV)
    {
      this.owner =
        Objects.requireNonNull(inOwner, "owner");
      this.caseV =
        Objects.requireNonNull(inCaseV, "caseV");
    }

    @Override
    public CBVariantCaseBuilderType createField(
      final String name,
      final CBTypeExpressionType type)
    {
      CBPackageBuilder.this.checkNotDone();
      CBFieldNames.INSTANCE.checkValid(name);

      if (this.caseV.fieldsByName().containsKey(name)) {
        throw new IllegalArgumentException(
          String.format("Field name %s already used", name)
        );
      }

      this.caseV.addField(new CBField(name, type));
      return this;
    }

    @Override
    public CBVariantBuilderType owner()
    {
      return this.owner;
    }
  }

  private final class ExternalBuilder implements CBTypeDeclarationBuilderType
  {
    private final String name;
    private final CBTypeDeclarationExternal external;

    ExternalBuilder(
      final String inName,
      final String externalPackage,
      final String externalType)
    {
      this.name =
        Objects.requireNonNull(inName, "name");
      this.external =
        new CBTypeDeclarationExternal(
          this.name,
          externalPackage,
          externalType
        );
    }

    @Override
    public CBPackageBuilderType ownerPackage()
    {
      CBPackageBuilder.this.checkNotDone();
      return CBPackageBuilder.this;
    }

    @Override
    public CBTypeExprNamedType reference()
    {
      CBPackageBuilder.this.checkNotDone();
      return new CBTypeExpressionNamed(this.external);
    }

    @Override
    public CBTypeParameterType findParameter(
      final String parameterName)
    {
      CBPackageBuilder.this.checkNotDone();
      throw new IllegalStateException(
        String.format("No such parameter: %s", parameterName));
    }

    @Override
    public CBTypeExprParameterType referenceParameter(
      final String parameterName)
    {
      CBPackageBuilder.this.checkNotDone();
      throw new IllegalStateException(
        String.format("No such parameter: %s", parameterName));
    }

    @Override
    public CBTypeParameterType addTypeParameter(
      final String parameterName)
    {
      CBPackageBuilder.this.checkNotDone();
      CBTypeParameterNames.INSTANCE.checkValid(parameterName);

      if (this.external.parametersByName().containsKey(parameterName)) {
        throw new IllegalArgumentException(
          String.format("Type parameter name %s already used", parameterName)
        );
      }

      final var parameter = new CBTypeParameter(
        parameterName,
        this.external.arity());
      this.external.addTypeParameter(parameter);
      return parameter;
    }

    @Override
    public void setExternalName(
      final String externalPackageName,
      final String externalName)
    {
      throw new UnsupportedOperationException();
    }
  }
}
