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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

/**
 * Factory of threads that do work to be used by a thread pool executor service.
 */
public class WorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
    /**
     * Create executor service with one thread.
     *
     * @param name Name to use when creating threads
     * @return New executor service
     */
    @NotNull
    public static ExecutorService createService(@NotNull String name) {
        return createService(new WorkerThreadFactory(name, 1));
    }

    /**
     * Create executor service.
     *
     * @param name    Name to use when creating threads
     * @param threads Max number of threads to use
     * @return New executor service
     */
    @NotNull
    public static ExecutorService createService(@NotNull String name, int threads) {
        int max = Runtime.getRuntime().availableProcessors();
        int half = Math.max(1, max / 2);
        int actual = Math.clamp(threads < 1 ? half : threads, 1, max);
        return createService(new WorkerThreadFactory(name, actual));
    }

    @NotNull
    private static ExecutorService createService(@NotNull WorkerThreadFactory factory) {
        return new ForkJoinPool(factory.threads, factory, null, false);
    }

    private final String name;
    private final int threads;

    private final AtomicInteger counter = new AtomicInteger();

    /**
     * Constructs a new instance of WorkerThreadFactory.
     *
     * @param name    Name to use when creating threads
     * @param threads Max number of threads to use
     */
    public WorkerThreadFactory(@NotNull String name, int threads) {
        this.name = name;
        this.threads = threads;
    }

    @Override
    @NotNull
    public WorkerThread newThread(@NotNull ForkJoinPool pool) {
        return new WorkerThread(pool, this.name);
    }
}
