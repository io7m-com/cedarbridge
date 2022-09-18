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

package com.io7m.cedarbridge.schema.parser.internal;

import com.io7m.cedarbridge.schema.parser.api.CBParseFailedException;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.SExpressionType.SSymbol;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The parsing context.
 */

public interface CBParseContextType extends AutoCloseable
{
  /**
   * Open a new parsing context expecting one of the given shapes.
   *
   * @param kind   The kind
   * @param shapes The shapes
   *
   * @return A new parsing context
   */

  CBParseContextType openExpectingOneOf(
    String kind,
    List<String> shapes);

  @Override
  void close()
    throws CBParseFailedException;

  /**
   * Set the expected language version.
   *
   * @param major The major version
   * @param minor The minor version
   */

  void setLanguageVersion(
    int major,
    int minor
  );

  /**
   * Check that the expression is of the given type.
   *
   * @param expression  The expression
   * @param specSection The quoted spec section
   * @param clazz       The class
   * @param <T>         The expression type
   *
   * @return expression
   *
   * @throws CBParseFailedException On errors
   */

  <T extends SExpressionType> T checkExpressionIs(
    SExpressionType expression,
    Optional<UUID> specSection,
    Class<T> clazz)
    throws CBParseFailedException;


  /**
   * Check that the expression is a keyword.
   *
   * @param expression  The expression
   * @param specSection The quoted spec section
   * @param name        The keyword
   * @param errorCode   The error code
   *
   * @return expression
   *
   * @throws CBParseFailedException On errors
   */

  SSymbol checkExpressionIsKeyword(
    SExpressionType expression,
    Optional<UUID> specSection,
    String name,
    String errorCode)
    throws CBParseFailedException;


  /**
   * Parsing failed.
   *
   * @param fatality    The fatality level
   * @param specSection The quoted spec section
   * @param errorCode   The error code
   * @param e           The exception
   * @param expression  The expression
   *
   * @return A parse exception
   */

  CBParseFailedException failed(
    SExpressionType expression,
    CBParseFailedException.Fatal fatality,
    Optional<UUID> specSection,
    String errorCode,
    Exception e);

  /**
   * Parsing failed.
   *
   * @param fatality    The fatality level
   * @param specSection The quoted spec section
   * @param errorCode   The error code
   * @param expression  The expression
   *
   * @return A parse exception
   */

  CBParseFailedException failed(
    SExpressionType expression,
    CBParseFailedException.Fatal fatality,
    Optional<UUID> specSection,
    String errorCode);

  /**
   * @return The number of errors encountered so far
   */

  int errorCount();

  /**
   * Parsing failed.
   *
   * @param lexical     The lexical position
   * @param fatality    The fatality level
   * @param specSection The quoted spec section
   * @param exception   The exception
   * @param errorCode   The error code
   *
   * @return A parse exception
   */

  CBParseFailedException failed(
    LexicalPosition<URI> lexical,
    CBParseFailedException.Fatal fatality,
    Optional<UUID> specSection,
    Exception exception,
    String errorCode);


  /**
   * Parsing failed.
   *
   * @param lexical     The lexical position
   * @param fatality    The fatality level
   * @param specSection The quoted spec section
   * @param errorCode   The error code
   *
   * @return A parse exception
   */

  CBParseFailedException failed(
    LexicalPosition<URI> lexical,
    CBParseFailedException.Fatal fatality,
    Optional<UUID> specSection,
    String errorCode);
}
