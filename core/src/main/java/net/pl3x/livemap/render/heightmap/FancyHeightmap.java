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

package net.pl3x.livemap.render.heightmap;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.Image;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.MapBlurUtil;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A fancy Vintage Story-like heightmap.
 */
public class FancyHeightmap extends Heightmap {
    private final byte[] heightmap = new byte[512 << 9];

    /**
     * Constructs a new instance of FancyHeightmap.
     */
    public FancyHeightmap() {
        super(FANCY);
    }

    @Override
    public void preRender(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        // pre-fill to "flat"
        Arrays.fill(this.heightmap, (byte) 1);
    }

    @Override
    public void postRender(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        // blur the heightmap and keep original copy (for sharpening)
        byte[] copy = this.heightmap.clone();
        MapBlurUtil.blur(this.heightmap);

        // apply the heightmap to the tile
        for (int index = 0; index < this.heightmap.length; index++) {
            int color = tile.getPixel(index);
            // only draw heightmap on rendered blocks
            if (color != 0) {
                // sharpen heightmap a bit
                float shade = ((((int) ((this.heightmap[index] - 1) / 25.6F)) / 5F)
                    + ((((copy[index] - 1) / 25.6F) % 1) / 5F))
                    * 1.2F + 1F;
                tile.setPixel(index & 511, index >> 9, Colors.mul(color & 0xFFFFFF, shade) | 0xFF000000);
            }
        }
    }

    @Override
    public void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        float yDiff;
        if (data.getFluidState() == null) {
            // calculate actual heightmap if we're not in water
            yDiff = CalculateAltitudeDiff(data.getChunk(), data.getBlockX(), data.getBlockZ(), data.getBlockY());
        } else if (data.getFluidY() - data.getBlockY() <= 5) {
            // calculate heightmap and taper off the deeper we go in shallow water
            yDiff = CalculateAltitudeDiff(data.getChunk(), data.getBlockX(), data.getBlockZ(), data.getBlockY()) * 0.25F + 0.75F;
        } else {
            // water too deep so see heightmap detail, just use flat surface
            yDiff = 1F;
        }

        this.heightmap[Image.getIndex(data.getBlockX(), data.getBlockZ())] = (byte) (128 * yDiff - 127);
    }

    private float CalculateAltitudeDiff(@NotNull Chunk chunk, int blockX, int blockZ, int blockY) {
        Chunk.BlockData northwest = chunk.getWorld().getChunkFast(chunk, (blockX - 1) >> 4, (blockZ - 1) >> 4).getData(blockX - 1, blockZ - 1);
        Chunk.BlockData northeast = chunk.getWorld().getChunkFast(chunk, blockX >> 4, (blockZ - 1) >> 4).getData(blockX, blockZ - 1);
        Chunk.BlockData southwest = chunk.getWorld().getChunkFast(chunk, (blockX - 1) >> 4, blockZ >> 4).getData(blockX - 1, blockZ);

        int leftTop = blockY - (northwest == null ? blockY : northwest.getBlockY());
        int rightTop = blockY - (northeast == null ? blockY : northeast.getBlockY());
        int leftBot = blockY - (southwest == null ? blockY : southwest.getBlockY());

        int direction = Integer.signum(leftTop) + Integer.signum(rightTop) + Integer.signum(leftBot);
        int steepness = Math.max(Math.max(Math.abs(leftTop), Math.abs(rightTop)), Math.abs(leftBot));
        float slopeFactor = Math.min(0.5F, steepness / 10F) / 1.25F;

        if (direction > 0) {
            return 1.08F + slopeFactor;
        }
        if (direction < 0) {
            return 0.92F - slopeFactor;
        }
        return 1;
    }
}
