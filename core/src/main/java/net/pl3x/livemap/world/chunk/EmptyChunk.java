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

package net.pl3x.livemap.world.chunk;

import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.block.BlockState;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an empty or (non-existent) chunk.
 */
public class EmptyChunk extends Chunk {
    /**
     * Constructs a new instance of EmptyChunk.
     *
     * @param region Region chunk belongs to
     */
    public EmptyChunk(@NotNull Region region) {
        super(region, new NBT());
    }

    @Override
    public boolean isFull() {
        return false;
    }

    @Override
    public long getInhabitedTime() {
        return 0;
    }

    @Override
    public int getHeight(int blockX, int blockZ) {
        return 0;
    }

    @Override
    @NotNull
    public BlockState getBlockState(int blockX, int blockY, int blockZ) {
        return Block.AIR.getDefaultState();
    }

    @Override
    @NotNull
    public Biome getBiome(int blockX, int blockY, int blockZ) {
        return Biome.DEFAULT;
    }

    @Override
    public int getLight(int blockX, int blockY, int blockZ) {
        return 0;
    }

    @Override
    @NotNull
    public String toString() {
        return "EmptyChunk["
            + "world=" + getWorld()
            + ",xPos=" + getX()
            + ",yPos=" + getY()
            + ",zPos=" + getZ()
            + "]";
    }
}
