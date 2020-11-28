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

import com.io7m.cedarbridge.schema.compiled.CBTypeType;
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
   * The kind of object to which the name binds.
   */

  enum Kind
  {
    /**
     * The binding names a type.
     */

    BINDING_TYPE,

    /**
     * The binding names a type parameter.
     */

    BINDING_TYPE_PARAMETER,

    /**
     * The binding names a field.
     */

    BINDING_FIELD_NAME,

    /**
     * The binding names a variant case.
     */

    BINDING_VARIANT_CASE
  }

  /**
   * @return The name
   */

  String name();

  @Override
  LexicalPosition<URI> lexical();

  /**
   * A binding to something declared within the same package.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBBindingLocalType extends CBBindingType
  {
    /**
     * @return The package-unique ID of the binding
     */

    BigInteger id();

    /**
     * @return The kind of the binding
     */

    Kind kind();
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

    CBTypeType type();
  }
}
