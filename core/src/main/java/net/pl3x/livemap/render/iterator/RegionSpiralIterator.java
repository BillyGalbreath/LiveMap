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

import it.unimi.dsi.fastutil.longs.LongCollection;
import java.util.function.BooleanSupplier;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public class RegionSpiralIterator extends SpiralIterator {
    /**
     * Constructs a new SpiralIterator for regions at the given center.
     *
     * @param center  Center point
     * @param regions Collection of regions to iterate
     * @param hasNext Supplier to determine if there is a next element
     */
    public RegionSpiralIterator(@NotNull Point center, @NotNull LongCollection regions, @Nullable BooleanSupplier hasNext) {
        this(center.getX(), center.getZ(), regions, hasNext);
    }

    /**
     * Constructs a new SpiralIterator for regions at the given center.
     *
     * @param regionX Center x coordinate
     * @param regionZ Center z coordinate
     * @param regions Collection of regions to iterate
     * @param hasNext Supplier to determine if there is a next element
     */
    public RegionSpiralIterator(int regionX, int regionZ, @NotNull LongCollection regions, @Nullable BooleanSupplier hasNext) {
        super(regionX, regionZ, regions, hasNext);
    }

    @Override
    protected int unpackX(long packed) {
        return Region.unpackX(packed);
    }

    @Override
    protected int unpackZ(long packed) {
        return Region.unpackZ(packed);
    }
}
