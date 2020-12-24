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

package com.io7m.cedarbridge.examples.pastebin;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CBExPastebin
{
  private static final CBExPastebin INSTANCE = new CBExPastebin();
  private final ConcurrentHashMap<UUID, Paste> pastes;

  private CBExPastebin()
  {
    this.pastes = new ConcurrentHashMap<UUID, Paste>();
  }

  public static CBExPastebin get()
  {
    return INSTANCE;
  }

  public Optional<Paste> find(
    final UUID id)
  {
    return Optional.ofNullable(this.pastes.get(id));
  }

  public void delete(
    final UUID id)
  {
    this.pastes.remove(id);
  }

  public Paste create(
    final String key,
    final String text)
  {
    final var id = UUID.randomUUID();
    final var paste = new Paste(id, key, text);
    this.pastes.put(id, paste);
    return paste;
  }

  public int size()
  {
    return this.pastes.size();
  }

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
      this.id = Objects.requireNonNull(inId, "id");
      this.key = Objects.requireNonNull(inKey, "key");
      this.text = Objects.requireNonNull(inText, "text");
    }

    public UUID id()
    {
      return this.id;
    }

    public String text()
    {
      return this.text;
    }

    public String key()
    {
      return this.key;
    }
  }
}
