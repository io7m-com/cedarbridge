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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A pastebin model. This model manages a collection of pastes.
 */

public final class CBExPastebin
{
  private static final CBExPastebin INSTANCE = new CBExPastebin();
  private final ConcurrentHashMap<UUID, Paste> pastes;

  private CBExPastebin()
  {
    this.pastes = new ConcurrentHashMap<UUID, Paste>();
  }

  /**
   * @return The pastebin model instance
   */

  public static CBExPastebin get()
  {
    return INSTANCE;
  }

  /**
   * Find a paste with the given id.
   *
   * @param id The id
   *
   * @return The paste, if any
   */

  public Optional<Paste> find(
    final UUID id)
  {
    return Optional.ofNullable(this.pastes.get(id));
  }

  /**
   * Delete the paste with the given id.
   *
   * @param id The id
   */

  public void delete(
    final UUID id)
  {
    this.pastes.remove(id);
  }

  /**
   * Create a paste with the given key and text.
   *
   * @param key  The key
   * @param text The text
   *
   * @return The created paste
   */

  public Paste create(
    final String key,
    final String text)
  {
    final var id = UUID.randomUUID();
    final var paste = new Paste(id, key, text);
    this.pastes.put(id, paste);
    return paste;
  }

  /**
   * @return The number of pastes
   */

  public int size()
  {
    return this.pastes.size();
  }

  /**
   * A paste.
   *
   * A paste consists of some text, with an associated key, and a unique ID.
   * The key is set when the paste is created, and can only be deleted by
   * providing the key.
   */

  public static final class Paste
  {
    private final UUID id;
    private final String key;
    private final String text;

    Paste(
      final UUID inId,
      final String inKey,
      final String inText)
    {
      this.id =
        Objects.requireNonNull(inId, "id");
      this.key =
        Objects.requireNonNull(inKey, "key");
      this.text =
        Objects.requireNonNull(inText, "text");
    }

    /**
     * @return The paste ID
     */

    public UUID id()
    {
      return this.id;
    }

    /**
     * @return The paste text
     */

    public String text()
    {
      return this.text;
    }

    /**
     * @return The paste key
     */

    public String key()
    {
      return this.key;
    }
  }
}
