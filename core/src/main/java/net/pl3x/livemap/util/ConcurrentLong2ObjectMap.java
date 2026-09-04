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

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A type-specific {@link Map} with primitive long keys.
 *
 * @param <V> Type of value
 */
public class ConcurrentLong2ObjectMap<V> {
    private final Long2ObjectMap<V> map = new Long2ObjectOpenHashMap<>();
    private final StampedLock lock = new StampedLock();

    /**
     * Returns the value to which the given key is mapped.
     *
     * @param key the key
     * @return the corresponding value
     */
    @Nullable
    public V get(long key) {
        long stamp = this.lock.readLock();
        try {
            return this.map.get(key);
        } finally {
            this.lock.unlockRead(stamp);
        }
    }

    /**
     * Adds a pair to the map.
     *
     * @param key   the key
     * @param value the value
     * @return the old value
     */
    @Nullable
    public V put(long key, @NotNull V value) {
        long stamp = this.lock.writeLock();
        try {
            return this.map.put(key, value);
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    /**
     * If the specified key is not already associated with a value, attempts to compute its value using
     * the given mapping function and enters it into this map.
     *
     * @param key             key with which the specified value is to be associated
     * @param mappingFunction the function to compute a value
     * @return the current (existing or computed) value associated with the specified key
     */
    @NotNull
    public V computeIfAbsent(long key, @NotNull Long2ObjectFunction<? extends V> mappingFunction) {
        long stamp = this.lock.readLock();
        try {
            if (this.map.containsKey(key)) {
                // exists, quick return
                return this.map.get(key);
            }

            // does not exist, need to write
            long writeStamp = this.lock.tryConvertToWriteLock(stamp);
            if (writeStamp != 0L) {
                // success
                stamp = writeStamp;
            } else {
                // failed - unlock read and wait for write
                this.lock.unlockRead(stamp);
                stamp = this.lock.writeLock();

                // double check in case other thread modified
                if (this.map.containsKey(key)) {
                    return this.map.get(key);
                }
            }

            // key is still missing. safe to compute
            V newValue = mappingFunction.apply(key);
            this.map.put(key, newValue);
            return newValue;

        } finally {
            // unlock either read or write, whichever we have
            this.lock.unlock(stamp);
        }
    }

    /**
     * Removes all the mappings from this map. The map will be empty after this call returns.
     */
    public void clear() {
        long stamp = this.lock.writeLock();
        try {
            this.map.clear();
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }
}
