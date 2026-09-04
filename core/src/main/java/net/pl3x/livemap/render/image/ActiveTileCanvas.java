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

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an image canvas containing pixel data from many regions at higher zoom levels.
 */
public class ActiveTileCanvas {
    private final BufferedImage imageBuffer;
    private final AtomicInteger contributionCount = new AtomicInteger(0);
    private final int totalExpectedContributions;

    /**
     * Constructs a new instance of ActiveTileCanvas.
     *
     * @param baseTile The base tile at zoom 0.
     * @param zoom     This image's zoom level
     */
    public ActiveTileCanvas(@NotNull TileCanvas baseTile, int zoom) {
        // determine how many regions fit inside this tile at this zoom level
        // zoom 1 = 2x2 (4 regions), zoom 2 = 4x4 (16 regions), zoom 3 = 8x8 (64 regions)
        int sideLength = 1 << zoom;
        this.totalExpectedContributions = sideLength * sideLength;

        // initialize a clean, blank image canvas for this pooled tile path
        this.imageBuffer = baseTile.getIO().createBuffer();
    }

    /**
     * Get the image buffer.
     *
     * @return Image buffer
     */
    @NotNull
    public BufferedImage getImageBuffer() {
        return this.imageBuffer;
    }

    /**
     * Increments the contributions.
     *
     * @return True if this contribution completely fills the tile.
     */
    public boolean recordContribution() {
        return this.contributionCount.incrementAndGet() >= this.totalExpectedContributions;
    }

    /**
     * Get whether this canvas has had contributions or not.
     *
     * @return True if canvas has received at least one contribution
     */
    public boolean hasContributions() {
        return this.contributionCount.get() > 0;
    }
}
