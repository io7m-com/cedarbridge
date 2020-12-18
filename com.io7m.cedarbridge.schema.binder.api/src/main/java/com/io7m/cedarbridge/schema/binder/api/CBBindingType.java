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

package com.io7m.cedarbridge.schema.binder.api;

import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.compiled.CBTypeDeclarationType;
import com.io7m.immutables.styles.ImmutablesStyleType;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalType;
import org.immutables.value.Value;

import java.math.BigInteger;
import java.net.URI;

/**
 * A binding to a name.
 */

public interface CBBindingType extends LexicalType<URI>
{
  /**
   * @return The name
   */

  String name();

  @Override
  LexicalPosition<URI> lexical();

  /**
   * A binding to something declared within the same package.
   */


  interface CBBindingLocalType extends CBBindingType
  {
    /**
     * @return The package-unique ID of the binding
     */

    BigInteger id();

    /**
     * The binding names a type parameter.
     */

    @ImmutablesStyleType
    @Value.Immutable
    interface CBBindingLocalTypeParameterType extends CBBindingLocalType
    {

    }

    /**
     * The binding names a type declaration.
     */

    @ImmutablesStyleType
    @Value.Immutable
    interface CBBindingLocalTypeDeclarationType extends CBBindingLocalType
    {
      CBASTTypeDeclarationType type();
    }

    /**
     * The binding names a protocol declaration.
     */

    @ImmutablesStyleType
    @Value.Immutable
    interface CBBindingLocalProtocolDeclarationType extends CBBindingLocalType
    {
      CBASTProtocolDeclaration protocol();
    }

    /**
     * The binding names a protocol version.
     */

    @ImmutablesStyleType
    @Value.Immutable
    interface CBBindingLocalProtocolVersionDeclarationType extends CBBindingLocalType
    {
      BigInteger version();
    }

    /**
     * The binding names a variant case.
     */

    @ImmutablesStyleType
    @Value.Immutable
    interface CBBindingLocalVariantCaseType extends CBBindingLocalType
    {

    }

    /**
     * The binding names a field name.
     */

    @ImmutablesStyleType
    @Value.Immutable
    interface CBBindingLocalFieldNameType extends CBBindingLocalType
    {

    }
  }

  /**
   * A binding to a type declared within an external package.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBBindingExternalType extends CBBindingType
  {
    /**
     * @return The type
     */

    CBTypeDeclarationType type();
  }
}
