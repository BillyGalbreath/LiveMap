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

/**
 * A clean-room utility to apply multi-pass box blurring over heightmap data.
 *
 * <p>This class processes flat 1D structures representing 2D height grids, isolating
 * directional blending layers cleanly to preserve performance across multiple thread loops.
 */
public final class MapBlurUtil {
    private MapBlurUtil() {
    }

    /**
     * Blurs a 2D grid packed sequentially into a flat primitive array.
     *
     * @param data The alpha or height matrix array
     */
    public static void blur(byte[] data) {
        horizontal(data);
        vertical(data);
    }

    private static void horizontal(byte[] data) {
        int index = 0;
        byte[] data2 = new byte[512];
        for (int y = 0; y < 512; y++) {
            int hits = 0;
            int r = 0;
            for (int x = -1; x < 512; x++) {
                if (x - 2 >= 0) {
                    byte col = data[index + x - 2];
                    if (col != 0) {
                        r -= col;
                    }
                    hits--;
                }
                if (x + 1 < 512) {
                    byte col = data[index + x + 1];
                    if (col != 0) {
                        r += col;
                    }
                    hits++;
                }
                if (x >= 0) {
                    data2[x] = (byte) (r / hits);
                }
            }
            System.arraycopy(data2, 0, data, index, 512);
            index += 512;
        }
    }

    private static void vertical(byte[] data) {
        byte[] data2 = new byte[512];
        for (int x = 0; x < 512; x++) {
            int hits = 0;
            int r = 0;
            int index = -512 + x;
            for (int y = -1; y < 512; y++) {
                if (y - 2 >= 0) {
                    byte col = data[index - 1024];
                    if (col != 0) {
                        r -= col;
                    }
                    hits--;
                }
                int newPixel = y + 1;
                if (newPixel < 512) {
                    byte col = data[index + 512];
                    if (col != 0) {
                        r += col;
                    }
                    hits++;
                }
                if (y >= 0) {
                    byte color = (byte) (r / hits);
                    data2[y] = color;
                }
                index += 512;
            }
            for (int y = 0; y < 512; y++) {
                data[y * 512 + x] = data2[y];
            }
        }
    }
}
