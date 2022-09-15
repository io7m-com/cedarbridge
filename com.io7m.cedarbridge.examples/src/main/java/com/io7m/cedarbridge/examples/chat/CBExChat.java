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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A chat model. This model manages a collection of sessions and distributes
 * messages between the sessions.
 */

public final class CBExChat
{
  private static final CBExChat INSTANCE = new CBExChat();
  private final Map<String, Session> sessions;
  private final Object sessionsLock;

  private CBExChat()
  {
    this.sessionsLock = new Object();
    this.sessions = new HashMap<>();
  }

  /**
   * @return The chat model instance
   */

  public static CBExChat get()
  {
    return INSTANCE;
  }

  private void broadcast(
    final CBExChatMessageType message)
  {
    synchronized (this.sessionsLock) {
      for (final var session : this.sessions.values()) {
        session.queue.add(message);
      }
    }
  }

  private void sessionDelete(
    final Session session)
  {
    synchronized (this.sessionsLock) {
      this.sessions.remove(session.user);
      this.broadcast(
        CBChatEventLeft.builder()
          .setUser(session.user)
          .build()
      );
    }
  }

  /**
   * Create a new session for the given name.
   *
   * @param name The name
   *
   * @return A new session
   *
   * @throws ChatException On errors
   */

  public Session sessionCreateNew(
    final String name)
    throws ChatException
  {
    Objects.requireNonNull(name, "name");

    synchronized (this.sessionsLock) {
      final var existing = this.sessions.get(name);
      if (existing == null) {
        final var session = new Session(this, name);
        this.sessions.put(name, session);
        this.broadcast(
          CBChatEventJoined.builder()
            .setUser(name)
            .build()
        );
        return session;
      }
    }

    throw new ChatUserExists(String.format("User already exists: %s", name));
  }

  /**
   * A connected chat session.
   */

  public static final class Session implements AutoCloseable
  {
    private final CBExChat chat;
    private final String user;
    private final ConcurrentLinkedQueue<CBExChatMessageType> queue;

    Session(
      final CBExChat inChat,
      final String inUser)
    {
      this.chat =
        Objects.requireNonNull(inChat, "chat");
      this.user =
        Objects.requireNonNull(inUser, "user");
      this.queue =
        new ConcurrentLinkedQueue<>();
    }

    @Override
    public void close()
    {
      this.chat.sessionDelete(this);
    }

    /**
     * Take an event from the session, if one is ready
     *
     * @return An event
     */

    public Optional<CBExChatMessageType> takeEvent()
    {
      return Optional.ofNullable(this.queue.poll());
    }

    /**
     * Broadcast a message.
     *
     * @param message The message
     */

    public void speak(
      final String message)
    {
      this.chat.broadcast(
        CBChatEventSpoke.builder()
          .setUser(this.user)
          .setMessage(message)
          .build()
      );
    }
  }

  /**
   * The type of chat exceptions.
   */

  public static abstract class ChatException extends Exception
  {
    /**
     * The type of chat exceptions.
     *
     * @param message The error message
     */

    public ChatException(
      final String message)
    {
      super(message);
    }
  }

  /**
   * A user already exists.
   */

  public static final class ChatUserExists extends ChatException
  {
    /**
     * A user already exists.
     *
     * @param message The error message
     */

    public ChatUserExists(
      final String message)
    {
      super(message);
    }
  }
}
