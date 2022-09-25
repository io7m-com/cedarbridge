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

import com.io7m.cedarbridge.errors.CBExceptionTracker;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolDeclaration;
import com.io7m.cedarbridge.schema.ast.CBASTProtocolVersion;
import com.io7m.cedarbridge.schema.ast.CBASTTypeName;
import com.io7m.cedarbridge.schema.binder.api.CBBindingLocalTypeDeclaration;
import com.io7m.cedarbridge.schema.binder.api.CBBindingType;
import com.io7m.cedarbridge.schema.typer.api.CBTypeAssignment;
import com.io7m.cedarbridge.schema.typer.api.CBTypeCheckFailedException;
import com.io7m.cedarbridge.schema.typer.api.CBTypesForProtocolVersion;
import com.io7m.junreachable.UnreachableCodeException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.io7m.cedarbridge.schema.names.CBUUIDs.uuid;

/**
 * Type checking of protocol declarations.
 */

public final class CBTypeProtocolChecker
  implements CBElementCheckerType<CBASTProtocolDeclaration>
{
  private static final Optional<UUID> SPEC_SECTION_KIND_0 =
    uuid("15d2bb7d-2dbc-4a0c-8a25-7003cb3f7e3a");

  private static final Optional<UUID> SPEC_SECTION_FIRST_NO_REMOVALS =
    uuid("0f1809c1-9828-497e-bb57-c3b40c82e051");

  private static final Optional<UUID> SPEC_SECTION_BECOMES_EMPTY =
    uuid("200ef52c-2863-468d-9056-45e29a6e93dc");

  private static final Optional<UUID> SPEC_SECTION_REMOVAL_WASNT_PRESENT =
    uuid("7715beef-680d-4f29-b418-9f79e24c599b");

  private static final Optional<UUID> SPEC_SECTION_REMOVAL_ALREADY_PRESENT =
    uuid("78e0481d-8da4-426f-bbce-cfba8f636f0d");

  private static final Optional<UUID> SPEC_SECTION_TOO_MANY_TYPES =
    uuid("c2ea4d3a-8854-4621-abb2-1154bd861b63");

  /**
   * Type checking of protocol declarations.
   */

  public CBTypeProtocolChecker()
  {

  }

  private static void checkDeclaration(
    final CBTyperContextType context,
    final CBASTProtocolDeclaration decl)
    throws CBTypeCheckFailedException
  {
    final var tracker = new CBExceptionTracker<CBTypeCheckFailedException>();
    for (final var version : decl.versions()) {
      try {
        checkVersion(context, version);
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }

    final var sorted = new ArrayList<>(decl.versions());
    sorted.sort(Comparator.comparing(CBASTProtocolVersion::version));

    CBASTProtocolVersion previous = null;
    for (int index = 0; index < sorted.size(); ++index) {
      final CBASTProtocolVersion current = sorted.get(index);
      try {
        evaluateVersion(context, Optional.ofNullable(previous), current);
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
      previous = current;
    }

    tracker.throwIfNecessary();
  }

  private static void evaluateVersion(
    final CBTyperContextType context,
    final Optional<CBASTProtocolVersion> previousOpt,
    final CBASTProtocolVersion current)
    throws CBTypeCheckFailedException
  {
    final var currentRemoved =
      current.typesRemoved();
    final var currentAdded =
      current.typesAdded();

    if (previousOpt.isEmpty()) {
      if (!currentRemoved.isEmpty() || current.typesRemovedAll()) {
        throw context.failed(
          SPEC_SECTION_FIRST_NO_REMOVALS,
          current.lexical(),
          "errorTypeProtocolFirstNoRemovals"
        );
      }

      checkResultingTypes(context, current, new HashSet<>(currentAdded));
      return;
    }

    final var previous =
      previousOpt.get();
    final var previousSet =
      previous.userData()
        .get(CBTypesForProtocolVersion.class);

    final var currentEvaluation = new HashSet<>(previousSet.types());
    if (current.typesRemovedAll()) {
      currentEvaluation.clear();
    } else {
      for (final var r : currentRemoved) {
        final var existing =
          currentEvaluation.stream()
            .filter(t -> {
              final var b0 =
                t.userData().get(CBBindingType.class);
              final var b1 =
                r.userData().get(CBBindingType.class);
              return Objects.equals(b0, b1);
            }).findFirst();

        if (existing.isEmpty()) {
          throw context.failed(
            SPEC_SECTION_REMOVAL_WASNT_PRESENT,
            current.lexical(),
            "errorTypeProtocolWasNotPresent",
            r.text()
          );
        }

        currentEvaluation.remove(existing.get());
      }
    }

    for (final var a : currentAdded) {
      final var existing =
        currentEvaluation.stream()
          .filter(t -> {
            final var b0 =
              t.userData().get(CBBindingType.class);
            final var b1 =
              a.userData().get(CBBindingType.class);
            return Objects.equals(b0, b1);
          }).findFirst();

      if (existing.isPresent()) {
        throw context.failed(
          SPEC_SECTION_REMOVAL_ALREADY_PRESENT,
          current.lexical(),
          "errorTypeProtocolAlreadyPresent",
          a.text()
        );
      }

      currentEvaluation.add(a);
    }

    checkResultingTypes(context, current, currentEvaluation);
  }

  private static void checkResultingTypes(
    final CBTyperContextType context,
    final CBASTProtocolVersion current,
    final HashSet<CBASTTypeName> types)
    throws CBTypeCheckFailedException
  {
    if (types.isEmpty()) {
      throw context.failed(
        SPEC_SECTION_BECOMES_EMPTY,
        current.lexical(),
        "errorTypeProtocolBecameEmpty"
      );
    }

    if (types.size() >= 255) {
      throw context.failed(
        SPEC_SECTION_TOO_MANY_TYPES,
        current.lexical(),
        "errorTypeProtocolTooManyTypes",
        current.version(),
        Integer.valueOf(types.size())
      );
    }

    current.userData()
      .put(
        CBTypesForProtocolVersion.class,
        new CBTypesForProtocolVersion(Set.copyOf(types)));
  }

  private static void checkVersion(
    final CBTyperContextType context,
    final CBASTProtocolVersion version)
    throws CBTypeCheckFailedException
  {
    final var tracker = new CBExceptionTracker<CBTypeCheckFailedException>();

    for (final var type : version.typesAdded()) {
      try {
        checkVersionType(context, type);
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }

    for (final var type : version.typesRemoved()) {
      try {
        checkVersionType(context, type);
      } catch (final CBTypeCheckFailedException e) {
        tracker.addException(e);
      }
    }

    tracker.throwIfNecessary();
  }

  private static void checkVersionType(
    final CBTyperContextType context,
    final CBASTTypeName type)
    throws CBTypeCheckFailedException
  {
    final var binding =
      type.userData().get(CBBindingType.class);

    if (binding instanceof CBBindingLocalTypeDeclaration) {
      final var targetType =
        ((CBBindingLocalTypeDeclaration) binding).type();
      final var typeAssignment =
        targetType.userData().get(CBTypeAssignment.class);

      if (typeAssignment.arity() != 0) {
        throw context.failed(
          SPEC_SECTION_KIND_0,
          type.lexical(),
          "errorTypeProtocolKind0",
          targetType.name().text(),
          Integer.valueOf(typeAssignment.arity())
        );
      }
    } else {
      throw new UnreachableCodeException();
    }
  }

  @Override
  public void check(
    final CBTyperContextType context,
    final CBASTProtocolDeclaration item)
    throws CBTypeCheckFailedException
  {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(item, "item");

    checkDeclaration(context, item);
  }
}
