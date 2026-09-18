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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a registry of key-value pairs that keep insertion order (backed by synchronized LinkedHashMap).
 *
 * @param <T> Type of registry
 */
public abstract class OrderedRegistry<T> extends ConcurrentHashMap<String, T> {
    private final Map<String, T> synchronizedMap = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Rebuilds the registry.
     */
    public abstract void rebuild();

    @Override
    @Nullable
    public T put(@NotNull String key, @NotNull T value) {
        return this.synchronizedMap.put(key, value);
    }

    @Override
    public void clear() {
        this.synchronizedMap.clear();
    }

    @Override
    @NotNull
    public Collection<T> values() {
        return this.synchronizedMap.values();
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super String, ? super T> action) {
        this.synchronizedMap.forEach(action);
    }

    @Override
    public int size() {
        return this.synchronizedMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.synchronizedMap.isEmpty();
    }

    @Override
    @Nullable
    public T get(@NotNull Object key) {
        return this.synchronizedMap.get(key);
    }

    @Override
    @Nullable
    public T getOrDefault(@NotNull Object key, @Nullable T defaultValue) {
        return this.synchronizedMap.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean containsKey(@NotNull Object key) {
        return this.synchronizedMap.containsKey(key);
    }

    @Override
    public boolean containsValue(@NotNull Object value) {
        return this.synchronizedMap.containsValue(value);
    }

    @Override
    @NotNull
    public Set<Entry<String, T>> entrySet() {
        return this.synchronizedMap.entrySet();
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends T> m) {
        this.synchronizedMap.putAll(m);
    }

    @Override
    @Nullable
    public T remove(@NotNull Object key) {
        return this.synchronizedMap.remove(key);
    }

    @Override
    public boolean remove(@NotNull Object key, @NotNull Object value) {
        return this.synchronizedMap.remove(key, value);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof OrderedRegistry<?> && this.synchronizedMap.equals(o);
    }

    @Override
    public int hashCode() {
        return this.synchronizedMap.hashCode();
    }

    @Override
    @NotNull
    public String toString() {
        return this.synchronizedMap.toString();
    }
}
