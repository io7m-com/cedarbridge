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

import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTTypeDeclarationType;
import com.io7m.cedarbridge.schema.binder.api.CBBindFailedException;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType.CBBindingLocalType;
import com.io7m.cedarbridge.schema.compiled.CBPackageType;
import com.io7m.cedarbridge.schema.loader.api.CBLoaderType;
import com.io7m.jlexing.core.LexicalPosition;

import java.math.BigInteger;
import java.net.URI;

public interface CBBinderContextType extends AutoCloseable
{
  CBLoaderType loader();

  CBBinderContextType openBindingScope();

  @Override
  void close()
    throws CBBindFailedException;

  void registerPackage(
    LexicalPosition<URI> lexical,
    String text,
    CBPackageType packageV)
    throws CBBindFailedException;

  CBBindFailedException failed(
    LexicalPosition<URI> lexical,
    String errorCode,
    Object... arguments);

  CBBindFailedException failedWithOther(
    LexicalPosition<URI> lexical,
    LexicalPosition<URI> lexicalOther,
    String errorCode,
    Object... arguments);

  CBBindingLocalType bindType(
    CBASTTypeDeclarationType type)
    throws CBBindFailedException;

  CBBindingLocalType bindProtocol(
    CBASTProtocolDeclaration proto)
    throws CBBindFailedException;

  CBBindingLocalType bindTypeParameter(
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  CBBindingLocalType bindField(
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  CBBindingLocalType checkTypeBinding(
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  CBBindingLocalType bindVariantCase(
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  CBPackageType checkPackageBinding(
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  String currentPackage();

  CBBindingLocalType bindProtocolVersion(
    BigInteger version,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;
}
