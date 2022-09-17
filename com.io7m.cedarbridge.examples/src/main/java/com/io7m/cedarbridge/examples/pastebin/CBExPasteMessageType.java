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

package com.io7m.cedarbridge.examples.pastebin;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

import java.util.UUID;

/**
 * The core pastebin message protocol.
 */

public interface CBExPasteMessageType
{
  /**
   * Create a paste.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBPasteCreateType extends CBExPasteMessageType
  {
    /**
     * @return The paste key required to delete the paste
     */

    String key();

    /**
     * @return The paste text
     */

    String text();
  }

  /**
   * A paste was created.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBPasteCreatedType extends CBExPasteMessageType
  {
    /**
     * @return The paste ID
     */

    UUID id();
  }

  /**
   * Delete a paste.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBPasteDeleteType extends CBExPasteMessageType
  {
    /**
     * @return The paste ID
     */

    UUID id();

    /**
     * @return The paste key required to delete the paste
     */

    String key();
  }

  /**
   * A paste was deleted.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBPasteDeletedType extends CBExPasteMessageType
  {
    /**
     * @return The paste ID
     */

    UUID id();
  }

  /**
   * Get a paste.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBPasteGetType extends CBExPasteMessageType
  {
    /**
     * @return The paste ID
     */

    UUID id();
  }

  /**
   * A paste was retrieved.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBPasteGetResultType extends CBExPasteMessageType
  {
    /**
     * @return The paste ID
     */

    UUID id();

    /**
     * @return The paste text
     */

    String text();
  }

  /**
   * An error occurred.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBErrorType extends CBExPasteMessageType
  {
    /**
     * @return The error message
     */

    String message();
  }
}
