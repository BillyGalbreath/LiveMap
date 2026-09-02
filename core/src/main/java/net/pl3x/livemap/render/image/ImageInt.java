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

/**
 * Represents a collection of pixel integer data for an image in memory.
 */
public interface ImageInt extends Image {
    /**
     * Get value at specified pixel.
     *
     * @param x X pixel
     * @param z Z pixel
     * @return Requested value
     */
    default int getPixel(int x, int z) {
        return getPixel(getIndex(x, z));
    }

    /**
     * Get value at specified pixel.
     *
     * @param index Pixel index
     * @return Requested value
     */
    default int getPixel(int index) {
        return getPixels()[index];
    }

    /**
     * Set pixel to specified value.
     *
     * @param x     X pixel
     * @param z     Z pixel
     * @param value Value to set
     */
    default void setPixel(int x, int z, int value) {
        setPixel(getIndex(x, z), value);
    }

    /**
     * Set pixel to specified value.
     *
     * @param index Pixel index
     * @param value Value to set
     */
    default void setPixel(int index, int value) {
        getPixels()[index] = value;
    }

    /**
     * Get direct access to the raw pixel array.
     *
     * @return Raw pixel array
     */
    int[] getPixels();
}
