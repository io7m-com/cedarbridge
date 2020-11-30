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

import com.io7m.cedarbridge.schema.ast.CBASTField;
import com.io7m.cedarbridge.schema.ast.CBASTPackage;
import com.io7m.cedarbridge.schema.ast.CBASTTypeExpressionType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeRecord;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariant;
import com.io7m.cedarbridge.schema.ast.CBASTTypeVariantCase;
import com.io7m.cedarbridge.schema.binder.api.CBBindingExternal;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.io7m.cedarbridge.schema.ast.CBASTTypeExpressionType.CBASTTypeApplicationType;
import static com.io7m.cedarbridge.schema.ast.CBASTTypeExpressionType.CBASTTypeNamedType;
import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType.CBBindingLocalTypeDeclarationType;
import static com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType.CBBindingLocalTypeParameterType;

/**
 * A simple translator from ASTs to the more abstract compiled model.
 */

public final class CBPackageBuilder
{
  /**
   * Construct a package builder.
   */

  public CBPackageBuilder()
  {

  }

  private static CBTypeDeclarationVariant buildVariant(
    final Map<String, CBTypeDeclarationType> typeDecls,
    final CBTypeDeclarationVariant variant,
    final CBASTTypeVariant typeDecl)
  {
    for (final var param : typeDecl.parameters()) {
      variant.addTypeParameter(new CBTypeParameter(param.text()));
    }
    for (final var caseV : typeDecl.cases()) {
      variant.addCase(buildVariantCase(typeDecls, variant, caseV));
    }
    return variant;
  }

  private static CBVariantCase buildVariantCase(
    final Map<String, CBTypeDeclarationType> typeDecls,
    final CBTypeDeclarationType owner,
    final CBASTTypeVariantCase caseV)
  {
    final var base = new CBVariantCase(caseV.name().text());
    for (final var field : caseV.fields()) {
      base.addField(buildField(typeDecls, owner, field));
    }
    return base;
  }

  private static CBField buildField(
    final Map<String, CBTypeDeclarationType> typeDecls,
    final CBTypeDeclarationType owner,
    final CBASTField field)
  {
    return new CBField(
      field.name().text(),
      buildTypeExpression(typeDecls, owner, field.type()));
  }

  private static CBTypeExpressionType buildTypeExpression(
    final Map<String, CBTypeDeclarationType> typeDecls,
    final CBTypeDeclarationType owner,
    final CBASTTypeExpressionType type)
  {
    if (type instanceof CBASTTypeApplicationType) {
      return buildTypeExpressionApplication(
        typeDecls,
        owner,
        (CBASTTypeApplicationType) type
      );
    }
    if (type instanceof CBASTTypeNamedType) {
      return buildTypeExpressionNamed(
        typeDecls,
        owner,
        (CBASTTypeNamedType) type
      );
    }
    throw new UnreachableCodeException();
  }

  private static CBTypeExpressionType buildTypeExpressionNamed(
    final Map<String, CBTypeDeclarationType> typeDecls,
    final CBTypeDeclarationType owner,
    final CBASTTypeNamedType type)
  {
    final var binding = type.userData().get(CBBindingType.class);
    if (binding instanceof CBBindingExternal) {
      return new CBTypeExpressionNamed(
        ((CBBindingExternal) binding).type()
      );
    }
    if (binding instanceof CBBindingLocalTypeDeclarationType) {
      return new CBTypeExpressionNamed(
        typeDecls.get(binding.name())
      );
    }
    if (binding instanceof CBBindingLocalTypeParameterType) {
      return new CBTypeExpressionParameter(
        owner.parametersByName().get(binding.name())
      );
    }
    throw new UnreachableCodeException();
  }

  private static CBTypeExpressionType buildTypeExpressionApplication(
    final Map<String, CBTypeDeclarationType> typeDecls,
    final CBTypeDeclarationType owner,
    final CBASTTypeApplicationType type)
  {
    final Stream<CBASTTypeExpressionType> subExpressions =
      Stream.concat(Stream.of(type.target()), type.arguments().stream());

    return new CBTypeExpressionApplication(
      subExpressions.map(t -> buildTypeExpression(typeDecls, owner, t))
        .collect(Collectors.toList())
    );
  }

  private static CBTypeDeclarationRecord buildRecord(
    final Map<String, CBTypeDeclarationType> typeDecls,
    final CBTypeDeclarationRecord record,
    final CBASTTypeRecord typeDecl)
  {
    for (final var param : typeDecl.parameters()) {
      record.addTypeParameter(new CBTypeParameter(param.text()));
    }
    for (final var field : typeDecl.fields()) {
      record.addField(buildField(typeDecls, record, field));
    }
    return record;
  }

  /**
   * Build a compiled package from the given AST. This assumes that all
   * binding analysis and type checking has been completed, and that the
   * AST will have the expected user data annotations.
   *
   * @param pack The package
   *
   * @return A compiled package
   */

  public CBPackage build(
    final CBASTPackage pack)
  {
    Objects.requireNonNull(pack, "pack");

    final var base = new CBPackage(pack.name().text());
    for (final var imp : pack.imports()) {
      base.addImport(imp.userData().get(CBPackageType.class));
    }

    final var typeDecls =
      new HashMap<String, CBTypeDeclarationType>(pack.types().size());

    for (final var typeDecl : pack.types()) {
      final var name = typeDecl.name().text();
      if (typeDecl instanceof CBASTTypeRecord) {
        typeDecls.put(name, new CBTypeDeclarationRecord(name));
      } else if (typeDecl instanceof CBASTTypeVariant) {
        typeDecls.put(name, new CBTypeDeclarationVariant(name));
      } else {
        throw new UnreachableCodeException();
      }
    }

    for (final var typeDecl : pack.types()) {
      final var name = typeDecl.name().text();
      if (typeDecl instanceof CBASTTypeRecord) {
        base.addRecord(
          buildRecord(
            typeDecls,
            (CBTypeDeclarationRecord) typeDecls.get(name),
            (CBASTTypeRecord) typeDecl
          ));
      } else if (typeDecl instanceof CBASTTypeVariant) {
        base.addVariant(
          buildVariant(
            typeDecls,
            (CBTypeDeclarationVariant) typeDecls.get(name),
            (CBASTTypeVariant) typeDecl
          ));
      } else {
        throw new UnreachableCodeException();
      }
    }
    return base;
  }
}
