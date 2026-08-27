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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;
import net.pl3x.livemap.LiveMap;
import org.jetbrains.annotations.NotNull;

/**
 * A thread managed by a {@link ForkJoinPool}, which executes {@link ForkJoinTask}.
 */
public class WorkerThread extends ForkJoinWorkerThread {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    /**
     * Constructs a new instance of WorkerThread.
     *
     * @param pool Executor pool creating this thread
     * @param name Name to give thread
     */
    protected WorkerThread(@NotNull ForkJoinPool pool, @NotNull String name) {
        super(pool);
        setContextClassLoader(LiveMap.class.getClassLoader());
        setName(name.formatted(COUNTER.incrementAndGet()));
        setName((pool.getParallelism() > 1 ? "LiveMap-%s-%d" : "LiveMap-%s")
            .formatted(name, COUNTER.incrementAndGet())
        );
    }
}
