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
import java.util.Optional;
import java.util.UUID;

/**
 * The contextual information used during binding analysis.
 */

public interface CBBinderContextType extends AutoCloseable
{
  /**
   * @return The package loader
   */

  CBLoaderType loader();

  /**
   * Open a new binding scope. This is typically called upon encountering
   * a declaration that generates a new name.
   *
   * @return The scope
   */

  CBBinderContextType openBindingScope();

  @Override
  void close()
    throws CBBindFailedException;

  /**
   * Register a package, making it available for subsequent analysis operations.
   *
   * @param specSection The quoted spec section
   * @param lexical     The lexical information
   * @param text        The short package name
   * @param packageV    The package
   *
   * @throws CBBindFailedException On errors
   */

  void registerPackage(
    Optional<UUID> specSection,
    LexicalPosition<URI> lexical,
    String text,
    CBPackageType packageV)
    throws CBBindFailedException;

  /**
   * Something failed.
   *
   * @param specSection The quoted spec section
   * @param lexical     The lexical information
   * @param errorCode   The error code
   * @param arguments   The error arguments
   *
   * @return A failure exception
   */

  CBBindFailedException failed(
    Optional<UUID> specSection,
    LexicalPosition<URI> lexical,
    String errorCode,
    Object... arguments);

  /**
   * Something failed and referred to an object at a different lexical position.
   *
   * @param specSection  The quoted spec section
   * @param lexical      The lexical information
   * @param lexicalOther The other lexical information
   * @param errorCode    The error code
   * @param arguments    The error arguments
   *
   * @return A failure exception
   */

  CBBindFailedException failedWithOther(
    Optional<UUID> specSection,
    LexicalPosition<URI> lexical,
    LexicalPosition<URI> lexicalOther,
    String errorCode,
    Object... arguments);

  /**
   * Bind the type declaration.
   *
   * @param specSection The quoted spec section
   * @param type        The type
   *
   * @return A local binding
   *
   * @throws CBBindFailedException On errors
   */

  CBBindingLocalType bindType(
    Optional<UUID> specSection,
    CBASTTypeDeclarationType type)
    throws CBBindFailedException;

  /**
   * Bind the protocol declaration.
   *
   * @param specSection The quoted spec section
   * @param proto       The protocol declaration
   *
   * @return A local binding
   *
   * @throws CBBindFailedException On errors
   */

  CBBindingLocalType bindProtocol(
    Optional<UUID> specSection,
    CBASTProtocolDeclaration proto)
    throws CBBindFailedException;

  /**
   * Bind the type parameter.
   *
   * @param specSection The quoted spec section
   * @param lexical     The lexical information for the identifier
   * @param text        The type parameter
   *
   * @return A local binding
   *
   * @throws CBBindFailedException On errors
   */

  CBBindingLocalType bindTypeParameter(
    Optional<UUID> specSection,
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  /**
   * Bind the field.
   *
   * @param specSection The quoted spec section
   * @param lexical     The lexical information for the identifier
   * @param text        The field
   *
   * @return A local binding
   *
   * @throws CBBindFailedException On errors
   */

  CBBindingLocalType bindField(
    Optional<UUID> specSection,
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  /**
   * Check that a type exists.
   *
   * @param specSection The quoted spec section
   * @param text        The type name
   * @param lexical     The lexical information for the identifier
   *
   * @return The binding
   *
   * @throws CBBindFailedException On errors
   */

  CBBindingLocalType checkTypeBinding(
    Optional<UUID> specSection,
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  /**
   * Bind the variant case.
   *
   * @param specSection The quoted spec section
   * @param lexical     The lexical information for the identifier
   * @param text        The variant case
   *
   * @return A local binding
   *
   * @throws CBBindFailedException On errors
   */

  CBBindingLocalType bindVariantCase(
    Optional<UUID> specSection,
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  /**
   * Check that a package exists.
   *
   * @param specSection The quoted spec section
   * @param text        The package name
   * @param lexical     The lexical information for the identifier
   *
   * @return The binding
   *
   * @throws CBBindFailedException On errors
   */

  CBPackageType checkPackageBinding(
    Optional<UUID> specSection,
    String text,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;

  /**
   * @return The name of the current package
   */

  String currentPackage();

  /**
   * Bind the protocol version.
   *
   * @param specSection The quoted spec section
   * @param lexical     The lexical information for the identifier
   * @param version     The version
   *
   * @return A local binding
   *
   * @throws CBBindFailedException On errors
   */

  CBBindingLocalType bindProtocolVersion(
    Optional<UUID> specSection,
    BigInteger version,
    LexicalPosition<URI> lexical)
    throws CBBindFailedException;
}
