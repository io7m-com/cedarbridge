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

import com.io7m.cedarbridge.schema.ast.CBASTPackageName;
import com.io7m.cedarbridge.schema.ast.CBASTTypeApplication;
import com.io7m.cedarbridge.schema.ast.CBASTTypeExpressionType;
import com.io7m.cedarbridge.schema.ast.CBASTTypeNamed;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBindingExternal;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.Objects;

public final class CBTypeExpressionBinder
  implements CBElementBinderType<CBASTTypeExpressionType>
{
  public CBTypeExpressionBinder()
  {

  }

  private static void bindExpression(
    final CBBinderContextType context,
    final CBASTTypeExpressionType item)
    throws CBBindFailedException
  {
    if (item instanceof CBASTTypeNamed) {
      bindTypeNamed(context, (CBASTTypeNamed) item);
    } else if (item instanceof CBASTTypeApplication) {
      bindTypeApplication(context, (CBASTTypeApplication) item);
    } else {
      throw new UnreachableCodeException();
    }
  }

  private static void bindTypeApplication(
    final CBBinderContextType context,
    final CBASTTypeApplication item)
    throws CBBindFailedException
  {
    var failed = false;

    try {
      bindExpression(context, item.target());
    } catch (final CBBindFailedException e) {
      failed = true;
    }

    for (final var argument : item.arguments()) {
      try {
        bindExpression(context, argument);
      } catch (final CBBindFailedException e) {
        failed = true;
      }
    }

    if (failed) {
      throw new CBBindFailedException();
    }
  }

  private static void bindTypeNamed(
    final CBBinderContextType context,
    final CBASTTypeNamed item)
    throws CBBindFailedException
  {
    final var packageNameOpt = item.packageName();
    if (packageNameOpt.isPresent()) {
      bindTypeNamedExternal(context, packageNameOpt.get(), item);
    } else {
      bindTypeNamedInternal(context, item);
    }
  }

  private static void bindTypeNamedInternal(
    final CBBinderContextType context,
    final CBASTTypeNamed item)
    throws CBBindFailedException
  {
    final var name = item.name();
    final var binding =
      context.checkTypeBinding(name.text(), name.lexical());

    item.userData().put(CBBindingType.class, binding);
  }

  private static void bindTypeNamedExternal(
    final CBBinderContextType context,
    final CBASTPackageName packageName,
    final CBASTTypeNamed item)
    throws CBBindFailedException
  {
    final var name =
      item.name();
    final var packageV =
      context.checkPackageBinding(packageName.text(), packageName.lexical());
    final var packageTypes =
      packageV.types();
    final var typeV =
      packageTypes.get(name.text());

    if (typeV == null) {
      throw context.failed(
        name.lexical(),
        "errorTypeMissing",
        name.text(),
        packageName.text()
      );
    }

    packageName.userData().put(CBPackageType.class, packageV);

    final var external =
      CBBindingExternal.builder()
        .setLexical(name.lexical())
        .setName(name.text())
        .setType(typeV)
        .build();

    item.userData().put(CBBindingType.class, external);
  }

  @Override
  public void bind(
    final CBBinderContextType context,
    final CBASTTypeExpressionType item)
    throws CBBindFailedException
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(item, "item");

    bindExpression(context, item);
  }
}
