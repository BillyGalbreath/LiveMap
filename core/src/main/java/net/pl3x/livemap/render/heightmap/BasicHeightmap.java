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

import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A basic Vanilla Minecraft-like heightmap.
 */
public class BasicHeightmap extends Heightmap {
    /**
     * Constructs a new instance of BasicHeightmap.
     */
    public BasicHeightmap() {
        super(BASIC);
    }

    @Override
    public int getAlpha(@NotNull Chunk chunk, int blockX, int blockZ) {
        Chunk.BlockData origin = chunk.getWorld()
            .getChunkFast(chunk, blockX >> 4, blockZ >> 4)
            .getData(blockX, blockZ);
        if (origin == null) {
            return getMid();
        }

        Chunk.BlockData north = chunk.getWorld()
            .getChunkFast(chunk, blockX >> 4, (blockZ - 1) >> 4)
            .getData(blockX, blockZ - 1);
        if (north == null) {
            return getMid();
        }

        return getAlpha(
            origin.getBlockY(),
            north.getBlockY(),
            getMid(),
            getMid()
        );
    }
}
