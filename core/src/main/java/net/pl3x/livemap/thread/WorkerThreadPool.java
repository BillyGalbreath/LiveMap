/*
 * This file is part of LiveMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020-2026 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pl3x.livemap.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.pl3x.livemap.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a pool of WorkerThreads.
 *
 * @see ForkJoinPool
 */
public class WorkerThreadPool extends ForkJoinPool {
    /**
     * Constructs a new instance of WorkerThreadPool.
     *
     * @param factory The factory for creating new threads
     */
    public WorkerThreadPool(@NotNull WorkerThreadFactory factory) {
        super(factory.getParallelism(), factory, null, false);
    }

    /**
     * Submits a periodic action that becomes enabled first after the
     * given initial delay, and subsequently with the given period;
     * that is, executions will commence after
     * {@code initialDelay}, then {@code initialDelay + period}, then
     * {@code initialDelay + 2 * period}, and so on.
     *
     * <p>The sequence of task executions continues indefinitely until
     * one of the following exceptional completions occur:
     * <ul>
     * <li>The task is {@linkplain Future#cancel explicitly cancelled}
     * <li>Method {@link #shutdownNow} is called
     * <li>Method {@link #shutdown} is called and the pool is
     * otherwise quiescent, in which case existing executions continue
     * but subsequent executions do not.
     * <li>An execution or the task encounters resource exhaustion.
     * <li>An execution of the task throws an exception.  In this case
     * calling {@link Future#get() get} on the returned future will throw
     * {@link ExecutionException}, holding the exception as its cause.
     * </ul>
     * Subsequent executions are suppressed.  Subsequent calls to
     * {@link Future#isDone isDone()} on the returned future will
     * return {@code true}.
     *
     * <p>If any execution of this task takes longer than its period, then
     * subsequent executions will run concurrently without waiting.
     *
     * @param command      The task to execute
     * @param callback     The task to execute after {@code command} completes
     * @param initialDelay The time to delay first execution
     * @param period       The period between successive executions
     * @param unit         The time unit of the initialDelay and period parameters
     * @return A ForkJoinTask implementing the ScheduledFuture
     *     interface.  The future's {@link Future#get() get()}
     *     method will never return normally, and will throw an
     *     exception upon task cancellation or abnormal
     *     termination of a task execution.
     * @throws RejectedExecutionException If the pool is shutdown or
     *                                    submission encounters resource exhaustion.
     * @throws NullPointerException       If command or unit is null
     * @throws IllegalArgumentException   If period less than or equal to zero
     * @see ForkJoinPool#scheduleAtFixedRate
     */
    @NotNull
    public ScheduledFuture<?> scheduleAtFixedRateAsync(
        @NotNull Runnable command,
        @NotNull Consumer<? super Throwable> callback,
        int initialDelay,
        int period,
        @NotNull TimeUnit unit
    ) {
        // localized lock for this specific recurring schedule
        final AtomicBoolean running = new AtomicBoolean(false);

        return scheduleAtFixedRate(() -> {
            // check if a previous tick is still running, and mark as running if not
            if (!running.compareAndSet(false, true)) {
                // return immediately if so, completely skipping this tick before any new thread is spawned
                Logger.debug("Previous tick still running, skipping this scheduled execution layer.");
                return;
            }

            // finally run the command, but async
            CompletableFuture.runAsync(command, this)
                .whenComplete((_, e) -> {
                    try {
                        // run the callback
                        callback.accept(e);
                    } finally {
                        // mark as not running anymore
                        running.set(false);
                    }
                });
        }, initialDelay, period, unit);
    }
}
