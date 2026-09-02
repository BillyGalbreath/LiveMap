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

package net.pl3x.livemap.render.image;

import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a collection of pixel data for an image in memory.
 */
public interface Image {
    int SIZE = 512;
    int MASK = 511;

    /**
     * Get pixel index from pixel coordinates.
     *
     * @param x X pixel
     * @param z Z pixel
     * @return Index of pixel
     */
    default int getIndex(int x, int z) {
        return ((z & MASK) << 9) + (x & MASK);
    }

    /**
     * Represents a function that accepts one argument and produces a non-null result.
     *
     * <p>This is a {@link java.util.function functional interface}
     * whose functional method is {@link #apply(Object)}.
     *
     * @param <T> the type of the input to the function
     * @param <R> the type of the non-null result of the function
     */
    @FunctionalInterface
    interface ImageFunction<T, R> extends Function<T, R> {
        @Override
        @NotNull
        R apply(@NotNull T t);
    }
}
