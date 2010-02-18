/*
 * $Id: GameModule.java 5488 2009-04-11 14:01:20Z uckelman $
 *
 * Copyright (c) 2009 by Joel Uckelman
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASSAL.tools.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * A {@link Callable} which calls {@code Callable}s from a queue.
 *
 * {@code QueueJoiner} permits one queue of {@code Callable}s to be joined
 * into a single {@code Callable} and inserted into another queue. One reason
 * for doing this is to make a single queue of {@code Callable}s available
 * from multiple threads. E.g., if a task run by an {@link ExecutorService}
 * needs to submit other tasks to the same {@code ExecutorService}, those
 * child tasks ey can be queued and submitted via a {@code QueueJoiner}, while
 * the original task can work on the same queue using a second
 * {@code QueueJoiner}. This prevents the {@code ExecutorService} from
 * deadlocking in the event that the child tasks are unable to make it to the
 * front of the {@code ExecutorService}'s queue, because the thread of the
 * original task will eventually clear the queue on its own.
 * 
 * @author Joel Uckelman
 * @since 3.1.11
 */
public class QueueJoiner implements Callable<Void> {
  protected final BlockingQueue<Callable<?>> queue;

  /**
   * Creates a {@link Callable} which calls {@code Callable}s from a queue.
   *
   * @param queue the queue to drain
   */
  public QueueJoiner(BlockingQueue<Callable<?>> queue) {
    this.queue = queue;
  }

  /**
   * Calls {@link Callable}s from the queue until the queue is empty.
   *
   * @throws InterruptedException if the current thread was interrupted
   * @throws Exception when any {@code Callable} from the queue throws
   */
  public Void call() throws Exception {
    Callable<?> c;
    while ((c = queue.poll()) != null) {
      c.call();
      if (Thread.interrupted()) throw new InterruptedException(); 
    }
    return null;
  }
}
