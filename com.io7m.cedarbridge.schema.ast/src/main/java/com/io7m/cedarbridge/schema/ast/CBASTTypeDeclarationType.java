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

package com.io7m.cedarbridge.schema.ast;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.List;

/**
 * The  type of type declarations.
 *
 * @param <T> The type of pass-specific data
 */

public interface CBASTTypeDeclarationType<T> extends CBASTDeclarationType<T>
{
  CBASTTypeName<T> name();

  List<CBASTTypeParameterName<T>> parameters();

  @ImmutablesStyleType
  @Value.Immutable
  interface CBASTTypeRecordType<T> extends CBASTTypeDeclarationType<T>
  {
    @Override
    CBASTTypeName<T> name();

    @Override
    List<CBASTTypeParameterName<T>> parameters();

    List<CBASTField<T>> fields();
  }

  @ImmutablesStyleType
  @Value.Immutable
  interface CBASTTypeVariantType<T> extends CBASTTypeDeclarationType<T>
  {
    @Override
    CBASTTypeName<T> name();

    @Override
    List<CBASTTypeParameterName<T>> parameters();

    List<CBASTTypeRecord<T>> cases();
  }
}
