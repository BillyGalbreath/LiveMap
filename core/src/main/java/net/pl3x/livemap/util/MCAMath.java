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

import org.jetbrains.annotations.NotNull;

/**
 * Math helpers for calculating MCA related data.
 */
public final class MCAMath {
    private MCAMath() {
    }

    /**
     * Having a long array where each long contains as many values as
     * fit in it without overflowing, returning the "valueIndex"-th
     * value when each value has "bitsPerValue" bits.
     *
     * @param data         Raw long array data
     * @param valueIndex   Index of requested value
     * @param bitsPerValue Number of bits per value
     * @return The requested value at specified index
     */
    public static long getValueFromLongArray(long @NotNull [] data, int valueIndex, int bitsPerValue) {
        int valuesPerLong = 64 / bitsPerValue;
        int longIndex = valueIndex / valuesPerLong;
        int bitIndex = (valueIndex % valuesPerLong) * bitsPerValue;

        long value = data[longIndex] >>> bitIndex;

        return value & (0xFFFFFFFFFFFFFFFFL >>> -bitsPerValue);
    }

    /**
     * Treating the long array "data" as a continuous stream of
     * bits, returning the "valueIndex"-th value when each
     * value has "bitsPerValue" bits.
     *
     * @param data         Raw long array data
     * @param valueIndex   Index of requested value
     * @param bitsPerValue Number of bits per value
     * @return The requested value at specified index
     */
    public static long getValueFromLongStream(long @NotNull [] data, int valueIndex, int bitsPerValue) {
        int bitIndex = valueIndex * bitsPerValue;
        int firstLong = bitIndex >> 6; // index / 64
        int bitoffset = bitIndex & 0x3F; // Math.floorMod(index, 64)

        long value = data[firstLong] >>> bitoffset;

        if (bitoffset > 0 && firstLong + 1 < data.length) {
            long value2 = data[firstLong + 1];
            //noinspection ShiftOutOfRange - overflow is purposeful
            value2 = value2 << -bitoffset;
            value = value | value2;
        }

        return value & (0xFFFFFFFFFFFFFFFFL >>> -bitsPerValue);
    }

    /**
     * Extracts the 4 bits of the upper or lower side of the byte stored in <code>value</code>.
     * <p>
     * The value is treated as an unsigned byte.
     *
     * @param value Byte to use
     * @param upper True for upper half
     * @return The specified half of byte
     */
    public static int getByteHalf(int value, boolean upper) {
        return ((value & 0xFF) >> (upper ? 4 : 0)) & 0xF;
    }

    /**
     * Computes the ceiling of the base-2 logarithm of <code>n</code>.
     *
     * @param n Value to be used as input
     * @return Ceiling of the base-2 logarithm of x
     */
    public static int ceilLog2(int n) {
        return Integer.SIZE - Integer.numberOfLeadingZeros(n - 1);
    }
}
