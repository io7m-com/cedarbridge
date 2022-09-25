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

package com.io7m.cedarbridge.schema.typer.internal;

import com.io7m.cedarbridge.schema.ast.CBASTDocumentation;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTTypeApplication;
import com.io7m.cedarbridge.schema.ast.CBASTTypeExpressionType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeNamed;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariantCase;
import com.io7m.cedarbridge.schema.binder.api.CBBindingExternal;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeParameter;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.compiled.CBPackageBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBPackages;
import com.io7m.cedarbridge.schema.compiled.CBRecordBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionApplication;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.cedarbridge.schema.compiled.CBVariantBuilderType;
import com.io7m.cedarbridge.schema.compiled.CBVariantCaseBuilderType;
import com.io7m.cedarbridge.schema.typer.api.CBTypesForProtocolVersion;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A simple translator from ASTs to the more abstract compiled model.
 */

public final class CBTypePackageConverter
{
  /**
   * Construct a package builder.
   */

  public CBTypePackageConverter()
  {

  }

  private static CBVariantBuilderType buildVariant(
    final CBPackageBuilderType builder,
    final List<CBASTDocumentation> packageLevelDocumentation,
    final CBASTTypeVariant typeDecl)
  {
    final var typeName =
      typeDecl.name().text();
    final var variant =
      builder.findVariant(typeName);

    final var typeParameters = typeDecl.parameters();
    for (int index = 0; index < typeParameters.size(); ++index) {
      final var param = typeParameters.get(index);
      final var parameterName = param.text();
      variant.addTypeParameter(
        parameterName,
        compileDocumentation(typeDecl.documentations(), parameterName)
      );
    }

    for (final var caseV : typeDecl.cases()) {
      final var caseName =
        caseV.name().text();
      final var caseBuilder =
        variant.createCase(caseName);

      caseBuilder.setDocumentation(
        compileDocumentation(typeDecl.documentations(), caseName)
      );

      buildVariantCase(caseBuilder, caseV);
    }

    Objects.requireNonNull(variant.ownerPackage(), "ownerPackage");
    variant.setDocumentation(
      compileDocumentation(packageLevelDocumentation, typeName)
    );
    return variant;
  }

  private static void buildVariantCase(
    final CBVariantCaseBuilderType caseBuilder,
    final CBASTTypeVariantCase caseV)
  {
    for (final var field : caseV.fields()) {
      final var fieldName = field.name().text();
      caseBuilder.createField(
        fieldName,
        buildTypeExpression(caseBuilder.owner(), field.type()),
        compileDocumentation(caseV.documentations(), fieldName)
      );
    }

    Objects.requireNonNull(caseBuilder.owner(), "owner");
  }

  private static CBTypeExpressionType buildTypeExpression(
    final CBTypeDeclarationBuilderType owner,
    final CBASTTypeExpressionType type)
  {
    if (type instanceof CBASTTypeApplication application) {
      return buildTypeExpressionApplication(owner, application);
    }
    if (type instanceof CBASTTypeNamed named) {
      return buildTypeExpressionNamed(owner, named);
    }
    throw new UnreachableCodeException();
  }

  private static CBTypeExpressionType buildTypeExpressionNamed(
    final CBTypeDeclarationBuilderType typeBuilder,
    final CBASTTypeNamed astType)
  {
    final var binding = astType.userData().get(CBBindingType.class);
    final var ownerPackage = typeBuilder.ownerPackage();
    if (binding instanceof final CBBindingExternal bindingExternal) {
      return ownerPackage.referenceExternalType(bindingExternal.type());
    }
    if (binding instanceof CBBindingLocalTypeDeclaration) {
      return ownerPackage.referenceType(binding.name());
    }
    if (binding instanceof CBBindingLocalTypeParameter) {
      return typeBuilder.referenceParameter(binding.name());
    }
    throw new UnreachableCodeException();
  }

  private static CBTypeExpressionType buildTypeExpressionApplication(
    final CBTypeDeclarationBuilderType owner,
    final CBASTTypeApplication type)
  {
    final var arguments =
      type.arguments()
        .stream()
        .map(t -> buildTypeExpression(owner, t))
        .collect(Collectors.toList());
    final var target =
      (CBTypeExpressionType.CBTypeExprNamedType)
        buildTypeExpressionNamed(owner, type.target());

    return new CBTypeExpressionApplication(target, arguments);
  }

  private static CBRecordBuilderType buildRecord(
    final CBPackageBuilderType builder,
    final List<CBASTDocumentation> documentation,
    final CBASTTypeRecord typeDecl)
  {
    final var name =
      typeDecl.name().text();
    final var record =
      builder.findRecord(name);

    final var typeParameters = typeDecl.parameters();
    for (int index = 0; index < typeParameters.size(); ++index) {
      final var param = typeParameters.get(index);
      final var paramName = param.text();
      record.addTypeParameter(
        paramName,
        compileDocumentation(typeDecl.documentations(), paramName)
      );
    }

    for (final var field : typeDecl.fields()) {
      final var fieldName = field.name().text();
      record.createField(
        fieldName,
        buildTypeExpression(record, field.type()),
        compileDocumentation(typeDecl.documentations(), fieldName)
      );
    }

    record.setDocumentation(compileDocumentation(documentation, name));
    return record;
  }

  private static void buildProto(
    final CBPackageBuilderType builder,
    final List<CBASTDocumentation> documentation,
    final CBASTProtocolDeclaration protoDecl)
  {
    final var protoName =
      protoDecl.name().text();
    final var protoBuilder =
      builder.createProtocol(protoName);

    protoBuilder.setDocumentation(
      compileDocumentation(documentation, protoName)
    );

    for (final var version : protoDecl.versions()) {
      final var versionBuilder =
        protoBuilder.createVersion(version.version());
      final var evaluatedTypes =
        version.userData().get(CBTypesForProtocolVersion.class);

      for (final var type : evaluatedTypes.types()) {
        versionBuilder.addType(builder.referenceType(type.text()));
      }
    }
  }

  /**
   * Build a compiled package from the given AST. This assumes that all binding
   * analysis and type checking has been completed, and that the AST will have
   * the expected user data annotations.
   *
   * @param pack The package
   *
   * @return A compiled package
   */

  public CBPackageType build(
    final CBASTPackage pack)
  {
    Objects.requireNonNull(pack, "pack");

    final var packBuilder =
      CBPackages.createPackage(pack.name().text());

    for (final var imp : pack.imports()) {
      packBuilder.addImport(imp.userData().get(CBPackageType.class));
    }

    for (final var typeDecl : pack.types()) {
      final var name = typeDecl.name().text();
      if (typeDecl instanceof CBASTTypeRecord) {
        packBuilder.createRecord(name);
      } else if (typeDecl instanceof CBASTTypeVariant) {
        packBuilder.createVariant(name);
      } else {
        throw new UnreachableCodeException();
      }
    }

    for (final var typeDecl : pack.types()) {
      if (typeDecl instanceof CBASTTypeRecord rec) {
        buildRecord(packBuilder, pack.documentation(), rec);
      } else if (typeDecl instanceof CBASTTypeVariant var) {
        buildVariant(packBuilder, pack.documentation(), var);
      } else {
        throw new UnreachableCodeException();
      }
    }

    for (final var protoDecl : pack.protocols()) {
      buildProto(packBuilder, pack.documentation(), protoDecl);
    }

    return packBuilder.build();
  }

  private static List<String> compileDocumentation(
    final List<CBASTDocumentation> documentation,
    final String name)
  {
    final var texts = new ArrayList<String>();
    for (final var doc : documentation) {
      if (Objects.equals(doc.target(), name)) {
        texts.add(doc.text());
      }
    }
    return List.copyOf(texts);
  }
}
