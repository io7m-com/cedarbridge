/*
 * Copyright © 2022 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.cedarbridge.codegen.javastatic.internal.generics;

import com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprNamedType;
import com.io7m.jodist.TypeName;

import java.util.List;

import static com.io7m.cedarbridge.schema.compiled.CBTypeExpressionType.CBTypeExprApplicationType;

/**
 * A reference to a serialize method that is enclosed inside a lambda
 * expression.
 *
 * @param type              The type
 * @param lambdaType        The type of the lambda expression
 * @param lambdaReturnType  The type of values serialized by the expression
 * @param lambdaName        The name of the variable to which the lambda
 *                          expression is assigned
 * @param lambdaContextName The name of the context variable inside the lambda
 *                          expression
 * @param lambdaValueName   The name of the value inside the lambda expression
 * @param lambdaTarget      The type targeted by the lambda expression
 * @param lambdaTargetRefs  The method references present inside the lambda
 */

public record CBGenericSerializerMethodRefViaLambda(
  CBTypeExprApplicationType type,
  TypeName lambdaType,
  TypeName lambdaReturnType,
  String lambdaName,
  String lambdaContextName,
  String lambdaValueName,
  CBTypeExprNamedType lambdaTarget,
  List<CBGenericSerializerMethodRefType> lambdaTargetRefs)
  implements CBGenericSerializerMethodRefType
{

}
