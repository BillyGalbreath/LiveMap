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

import it.unimi.dsi.fastutil.longs.Long2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.jetbrains.annotations.NotNull;

/**
 * A semi-persistent mapping from keys to values. Values are automatically
 * loaded by the cache, and stored in the cache until either evicted or
 * manually invalidated.
 *
 * @param <V> The type of mapped values
 */
public class LongLoadingCache<V> {
    private final long ttlMillis;
    private final long maxEntries;
    private final Loader<V> loader;

    private final Long2ObjectLinkedOpenHashMap<V> valueMap = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2LongLinkedOpenHashMap timestampMap = new Long2LongLinkedOpenHashMap();

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Constructs a new instance of LongLoadingCache.
     *
     * @param ttlMillis  Number of milliseconds before entries will expire
     * @param maxEntries Maximum number of entries
     * @param loader     The cache loader used to obtain new values
     */
    public LongLoadingCache(long ttlMillis, long maxEntries, @NotNull Loader<V> loader) {
        this.ttlMillis = ttlMillis;
        this.maxEntries = maxEntries;
        this.loader = loader;
    }

    /**
     * Returns the value associated with the {@code key} in this cache, obtaining that value from
     * the loader if necessary.
     *
     * <p>If another call to {@link get} is currently loading the value for the {@code key}, this thread
     * simply waits for that thread to finish and returns its loaded value. Note that multiple threads
     * can concurrently load values for distinct keys.
     *
     * <p>If the specified key is not already associated with a value, attempts to compute its value and
     * enters it into this cache. The entire method invocation is performed atomically, so the function
     * is applied at most once per key. Some attempted update operations on this cache by other threads
     * may be blocked while the computation is in progress, so the computation should be short and simple,
     * and must not attempt to update any other mappings of this cache.
     *
     * @param key The key with which the specified value is to be associated
     * @return The current (existing or computed) value associated with the specified key
     */
    @NotNull
    public V get(long key) {
        long now = System.currentTimeMillis();

        this.lock.lock();
        try {
            // check for existing value and move to back if exists
            V value = this.valueMap.getAndMoveToLast(key);
            if (value != null) {
                long lastWrite = this.timestampMap.get(key);
                if (now - lastWrite < this.ttlMillis) {
                    // reset expiration time and move to back
                    this.timestampMap.putAndMoveToLast(key, now);
                    return value;
                }
            }

            // compute fresh value
            value = this.loader.load(key);

            // evict the oldest, if full
            if (this.valueMap.size() >= this.maxEntries && !this.valueMap.containsKey(key)) {
                // firstLongKey() returns the oldest inserted key in O(1) time
                long oldestKey = this.valueMap.firstLongKey();
                this.valueMap.remove(oldestKey);
                this.timestampMap.remove(oldestKey);
            }

            // insert new value at the back
            this.valueMap.putAndMoveToLast(key, value);
            this.timestampMap.putAndMoveToLast(key, now);
            return value;
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Maintenance method to purge expired entries.
     */
    public void cleanUp() {
        long now = System.currentTimeMillis();

        this.lock.lock();
        try {
            // loop until the cache is empty, or we hit an unexpired entry
            while (!this.timestampMap.isEmpty()) {
                // get oldest key in O(1) time with zero allocation
                long oldestKey = this.timestampMap.firstLongKey();
                long lastWrite = this.timestampMap.get(oldestKey);

                // if the oldest entry hasn't expired, then nothing after it has either
                if (now - lastWrite < this.ttlMillis) {
                    break;
                }

                // it's expired, evict it
                this.valueMap.remove(oldestKey);
                this.timestampMap.remove(oldestKey);
            }
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Atomically clear all elements from the cache.
     */
    public void invalidateAll() {
        this.lock.lock();
        try {
            this.valueMap.clear();
            this.timestampMap.clear();
        } finally {
            this.lock.unlock();
        }
    }

    /**
     * Computes or retrieves values, based on a key.
     *
     * @param <V> the type of values.
     */
    @FunctionalInterface
    public interface Loader<V> {
        /**
         * Computes or retrieves the value corresponding to {@code key}.
         *
         * <p><b>Warning:</b> loading <b>must not</b> attempt to update any mappings of this cache directly.
         *
         * @param key The key whose value should be loaded
         * @return The value associated with {@code key}
         */
        @NotNull
        V load(long key);
    }
}
