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

package net.pl3x.livemap.util;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a primitive long keyed double buffer.
 */
public class LongDoubleBuffer {
    private final LongOpenHashSet[] buffer = new LongOpenHashSet[] {
        new LongOpenHashSet(),
        new LongOpenHashSet()
    };

    private final Object lock = new Object();

    private volatile int index = 1;

    /**
     * Ensures that this collection contains the specified long.
     *
     * @param value Value to add
     */
    public void add(long value) {
        synchronized (this.lock) {
            // write to current buffer
            this.buffer[this.index].add(value);
        }
    }

    /**
     * Adds all long values to this buffer.
     *
     * @param values Collection of values to add
     */
    public void addAll(@NotNull LongCollection values) {
        synchronized (this.lock) {
            // write to current buffer
            this.buffer[this.index].addAll(values);
        }
    }

    /**
     * Get current buffer, swap to and clear next buffer.
     *
     * <p>Next buffer will be empty after this method returns.
     *
     * @return Current buffer
     */
    @NotNull
    public LongOpenHashSet get() {
        synchronized (lock) {
            // swap to other buffer and clear it for new data
            this.buffer[this.index ^= 1].clear();

            // return original buffer we _were_ writing to
            return this.buffer[this.index ^ 1];
        }
    }

    /**
     * Removes all the elements from this double buffer.
     *
     * <p>Both buffers will be empty after this method returns.
     */
    public void clear() {
        synchronized (this.lock) {
            // clear both buffers and reset index
            this.buffer[0].clear();
            this.buffer[1].clear();
            this.index = 1;
        }
    }
}
