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

package com.io7m.cedarbridge.schema.compiled.internal;

import com.io7m.cedarbridge.schema.compiled.CBFieldType;
import com.io7m.cedarbridge.schema.compiled.CBVariantCaseType;
import com.io7m.cedarbridge.schema.compiled.CBVariantType;
import com.io7m.cedarbridge.schema.names.CBVariantCaseNames;
import com.io7m.jaffirm.core.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A variant case.
 */

public final class CBVariantCase implements CBVariantCaseType
{
  private final List<CBFieldType> fieldsRead;
  private final List<CBFieldType> fields;
  private final String name;
  private CBTypeDeclarationVariant owner;
  private List<String> documentation;

  /**
   * Construct a case.
   *
   * @param inName The case name
   */

  public CBVariantCase(
    final String inName)
  {
    this.name =
      Objects.requireNonNull(inName, "name");
    this.documentation =
      List.of();
    this.fields =
      new ArrayList<>();

    Preconditions.checkPreconditionV(
      inName,
      CBVariantCaseNames.INSTANCE.isValid(inName),
      "Variant case name must be valid"
    );

    this.fieldsRead =
      Collections.unmodifiableList(this.fields);
  }

  /**
   * Add a field to the case.
   *
   * @param field The field
   */

  public void addField(
    final CBField field)
  {
    Objects.requireNonNull(field, "field");

    final var fName = field.name();
    Preconditions.checkPreconditionV(
      this.fields.stream()
        .filter(f -> Objects.equals(f.name(), fName))
        .findAny()
        .isEmpty(),
      "A field named %s cannot already exist!",
      fName
    );

    this.fields.add(field);
    field.setOwner(this);
  }

  @Override
  public List<CBFieldType> fields()
  {
    return this.fieldsRead;
  }

  @Override
  public CBVariantType owner()
  {
    return this.owner;
  }

  @Override
  public String name()
  {
    return this.name;
  }

  @Override
  public List<String> documentation()
  {
    return this.documentation;
  }

  /**
   * Set the owner of the variant case.
   *
   * @param newOwner The owner
   */

  public void setOwner(
    final CBTypeDeclarationVariant newOwner)
  {
    this.owner = Objects.requireNonNull(newOwner, "cbPackage");
  }

  /**
   * Set the documentation for the case.
   *
   * @param text The documentation
   */

  public void setDocumentation(
    final List<String> text)
  {
    this.documentation = Objects.requireNonNull(text, "text");
  }
}
