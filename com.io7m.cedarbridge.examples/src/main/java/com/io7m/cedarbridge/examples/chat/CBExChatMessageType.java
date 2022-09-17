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

package com.io7m.cedarbridge.examples.chat;

import com.io7m.immutables.styles.ImmutablesStyleType;
import org.immutables.value.Value;

/**
 * The core chat protocol.
 */

public interface CBExChatMessageType
{
  /**
   * Join the server.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBChatCommandJoinType extends CBExChatMessageType
  {
    /**
     * @return The desired user name
     */

    String name();
  }

  /**
   * Speak a message to all connected clients.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBChatCommandSpeakType extends CBExChatMessageType
  {
    /**
     * @return The message
     */

    String message();
  }

  /**
   * A user joined the server.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBChatEventJoinedType extends CBExChatMessageType
  {
    /**
     * @return The user name
     */

    String user();
  }

  /**
   * A user left the server.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBChatEventLeftType extends CBExChatMessageType
  {
    /**
     * @return The user name
     */

    String user();
  }

  /**
   * A user spoke.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBChatEventSpokeType extends CBExChatMessageType
  {
    /**
     * @return The user name of the speaker
     */

    String user();

    /**
     * @return The message
     */

    String message();
  }

  /**
   * An error occurred.
   */

  @ImmutablesStyleType
  @Value.Immutable
  interface CBChatEventErrorType extends CBExChatMessageType
  {
    /**
     * @return The message
     */

    String message();
  }
}
