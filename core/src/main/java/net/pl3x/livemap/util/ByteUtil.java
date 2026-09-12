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
 * Utility for byte related things.
 */
public final class ByteUtil {
    private ByteUtil() {
    }

    /**
     * Parse property from string to byte without the overhead of try/catch NumberFormatException.
     *
     * <p>Note: This is oversimplified by not processing negative values.
     *
     * @param value String value to parse
     * @return Property value as byte
     */
    public static byte parsePropertyByte(@NotNull String value) {
        if (value.isBlank()) {
            return -1;
        }

        int len = value.length();
        int result = 0;

        // parse each digit
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);

            // instantly reject non-digits (including '-')
            if (c < '0' || c > '9') {
                return -1;
            }

            // add to the next digit
            result = result * 10 + (c - '0');
        }

        return (byte) result;
    }

    /**
     * Convert a long into a byte array.
     *
     * @param value Long to convert
     * @return Converted byte array
     */
    public static byte[] toBytes(long value) {
        byte[] bytes = new byte[Long.BYTES];
        for (int i = 0; i < Long.BYTES; i++) {
            bytes[i] = (byte) (value >>> (Byte.SIZE * (Long.BYTES - 1 - i)));
        }
        return bytes;
    }

    /**
     * Convert byte array into a long.
     *
     * @param bytes Bytes to convert
     * @return Converted long
     */
    public static long toLong(byte[] bytes) {
        long value = 0;
        for (int i = 0; i < Long.BYTES; i++) {
            value |= (bytes[i] & 0xFFL) << (Byte.SIZE * (Long.BYTES - 1 - i));
        }
        return value;
    }
}
