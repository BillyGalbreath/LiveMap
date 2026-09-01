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

import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.block.BlockState;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 1.13 is where it all starts for us, the flattening.
 */
class Chunk_1_13 extends Chunk {
    Chunk_1_13(@NotNull Region region, @NotNull Chunk_1_13.NBT chunkNBT) {
        super(region, chunkNBT);
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
    public BlockState getBlockState(int x, int y, int z) {
        return Block.AIR.getDefaultState();
    }

    @Override
    @NotNull
    public Biome getBiome(int x, int y, int z) {
        return Biome.DEFAULT;
    }

    @Override
    public int getLight(int x, int y, int z) {
        return 15;
    }

    @Nullable
    private Section getSection(int y) {
        return null;
    }

    private static class Section extends Chunk.Section {
        private Section(@NotNull World world, @NotNull SectionNBT nbt) {
        }

        private int getY() {
            return 0;
        }

        @NotNull
        private BlockState[] getBlockPalette() {
            return EMPTY_BLOCKSTATE_ARRAY;
        }

        @NotNull
        private BlockState getBlockState(int x, int y, int z) {
            return Block.AIR.getDefaultState();
        }

        @NotNull
        private Biome getBiome(int x, int y, int z) {
            return Biome.DEFAULT;
        }

        private int getLight(int x, int y, int z) {
            return 0;
        }
    }

    @SuppressWarnings("CanBeFinal")
    static class NBT extends Chunk.NBT {
    }

    @SuppressWarnings("CanBeFinal")
    private static class SectionNBT extends Chunk.Section.NBT {
    }
}
