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

import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * An iterator that spirals around a center point in a clockwise pattern.
 * <pre>
 *   30 31 32 33 34 35 36
 *   29 12 13 14 15 16 37
 *   28 11 02 03 04 17 38
 *   27 10 01 00 05 18 39
 *   26 09 08 07 06 19 40
 *   25 24 23 22 21 20 41
 *   48 47 46 45 44 43 42
 * </pre>
 */
public abstract class SpiralIterator implements LongIterator {
    private final Supplier<Boolean> hasNext;

    private int x;
    private int z;

    private long totalStepsInLeg = 1;
    private long currentStepInLeg = 0;
    private long legAxis;

    private Direction direction = Direction.WEST;

    /**
     * Constructs a new SpiralIterator at the given center.
     *
     * @param x       Center x coordinate
     * @param z       Center z coordinate
     * @param hasNext Supplier to determine if there is a next element
     */
    protected SpiralIterator(int x, int z, @NotNull Supplier<Boolean> hasNext) {
        this.x = x;
        this.z = z;
        this.hasNext = hasNext;
    }

    /**
     * Get current coordinate index.
     *
     * @return Current index
     */
    protected abstract long getCurrentIndex();

    /**
     * Get the current X coordinate.
     *
     * @return X coordinate
     */
    public int getCurrentX() {
        return this.x;
    }

    /**
     * Get the current Z coordinate.
     *
     * @return Z coordinate
     */
    public int getCurrentZ() {
        return this.z;
    }

    @Override
    public boolean hasNext() {
        return this.hasNext.get();
    }

    @Override
    public long nextLong() {
        // get current index
        final long index = getCurrentIndex();

        // set up for the next index
        switch (this.direction) {
            case SOUTH -> this.z++;
            case WEST -> this.x--;
            case NORTH -> this.z--;
            default -> this.x++;
        }

        // calculate where we are in the spiral
        ++this.currentStepInLeg;
        if (this.currentStepInLeg >= this.totalStepsInLeg) {
            this.currentStepInLeg = 0;
            this.direction = this.direction.next();
            if (++this.legAxis > 1) {
                this.legAxis = 0;
                this.totalStepsInLeg++;
            }
        }

        // return current index
        return index;
    }

    /**
     * Represents a cardinal direction the iterator is iterating.
     */
    protected enum Direction {
        EAST, SOUTH, WEST, NORTH;

        // cache values for efficiency
        protected static final Direction[] VALUES = values();

        /**
         * Gets the next direction in the enum.
         *
         * <p>If this is the last value then the first will be returned.
         *
         * @return The next direction
         */
        @NotNull
        protected Direction next() {
            // increment and overflow if needed
            return VALUES[(ordinal() + 1) & 3];
        }
    }
}
