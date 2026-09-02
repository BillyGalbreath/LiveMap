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

package net.pl3x.livemap.render.iterator;

import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.NoSuchElementException;
import java.util.function.BooleanSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An iterator that sorts and traverses a collection of 2D packed coordinates
 * in an outward clockwise spiral order from a center point.
 */
public abstract class SpiralIterator implements LongIterator {
    private final long[] elements;
    private final BooleanSupplier hasNext;
    private int cursor = 0;

    /**
     * Constructs a new SpiralIterator for a collection of packed coordinates.
     *
     * @param centerX  Center X coordinate
     * @param centerZ  Center Z coordinate
     * @param elements Collection of packed coordinates to iterate
     * @param hasNext  Optional live condition check (e.g. cancellation/interruption)
     */
    protected SpiralIterator(int centerX, int centerZ, @NotNull LongCollection elements, @Nullable BooleanSupplier hasNext) {
        this.elements = elements.toLongArray();
        this.hasNext = hasNext;

        // sort the primitive array in-place by spiral order
        LongArrays.quickSort(this.elements, (a, b) -> {
            long indexA = spiralIndex(unpackX(a) - centerX, unpackZ(a) - centerZ);
            long indexB = spiralIndex(unpackX(b) - centerX, unpackZ(b) - centerZ);
            return Long.compare(indexA, indexB);
        });
    }

    /**
     * Unpack the X coordinate from a packed index.
     *
     * @param packed Packed index
     * @return X coordinate
     */
    protected abstract int unpackX(long packed);

    /**
     * Unpack the Z coordinate from a packed index.
     *
     * @param packed Packed index
     * @return Z coordinate
     */
    protected abstract int unpackZ(long packed);

    @Override
    public boolean hasNext() {
        return this.cursor < this.elements.length
            && (this.hasNext == null || this.hasNext.getAsBoolean());
    }

    @Override
    public long nextLong() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return this.elements[this.cursor++];
    }

    /**
     * Total number of elements queued in this iterator.
     *
     * @return Total size
     */
    public int size() {
        return this.elements.length;
    }

    /**
     * Number of elements remaining to be processed.
     *
     * @return Remaining elements
     */
    public int remaining() {
        return Math.max(0, this.elements.length - this.cursor);
    }

    /**
     * Maps relative (dx, dz) coordinates to a 1D scalar position on a clockwise square spiral in O(1).
     *
     * @param dx Relative X coordinate from center
     * @param dz Relative Z coordinate from center
     * @return The 1D scalar position
     */
    public static long spiralIndex(int dx, int dz) {
        if (dx == 0 && dz == 0) {
            return 0L;
        }
        int k = Math.max(Math.abs(dx), Math.abs(dz)); // ring radius
        long innerArea = (2L * k - 1) * (2L * k - 1); // total points in all inner rings
        if (dz == -k) {
            return innerArea + (k - dx);            // north edge
        } else if (dx == -k) {
            return innerArea + (2L * k) + (k + dz); // west edge
        } else if (dz == k) {
            return innerArea + (4L * k) + (k + dx); // south edge
        } else {
            return innerArea + (6L * k) + (k - dz); // east edge
        }
    }
}
