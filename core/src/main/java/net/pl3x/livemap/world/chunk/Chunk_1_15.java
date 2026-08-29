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
 * Represents a chunk in the 1.15 format.
 */
public final class Chunk_1_15 extends Chunk {
    /**
     * Constructs a new instance of Chunk_1_15.
     *
     * @param region   Region chunk belongs to
     * @param chunkNBT NBT data for chunk
     */
    public Chunk_1_15(@NotNull Region region, @NotNull Chunk_1_15.NBT chunkNBT) {
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

    /**
     * Get chunk section at specified Y chunk coordinate.
     *
     * @param y Y chunk coordinate
     * @return Requested chunk section
     */
    @Nullable
    public Section getSection(int y) {
        return null;
    }

    /**
     * Represents a chunk section (16x16x16 blocks).
     */
    public static class Section extends Chunk.Section {
        /**
         * Constructs a new instance of Section.
         *
         * @param world The world this chunk section belongs to
         * @param nbt   The section's raw nbt data
         */
        public Section(@NotNull World world, @NotNull SectionNBT nbt) {
        }

        /**
         * Get the Y chunk coordinate for this section.
         *
         * @return Y chunk coordinate
         */
        public int getY() {
            return 0;
        }

        /**
         * Get block state palette for this section.
         *
         * @return Block state palette
         */
        @NotNull
        public BlockState[] getBlockPalette() {
            return EMPTY_BLOCKSTATE_ARRAY;
        }

        /**
         * Get block state at specified block coordinates.
         *
         * @param x X block coordinate
         * @param y Y block coordinate
         * @param z Z block coordinate
         * @return Requested block state
         */
        @NotNull
        public BlockState getBlockState(int x, int y, int z) {
            return Block.AIR.getDefaultState();
        }

        /**
         * Get biome at specified block coordinates.
         *
         * @param x X block coordinate
         * @param y Y block coordinate
         * @param z Z block coordinate
         * @return Requested biome
         */
        @NotNull
        public Biome getBiome(int x, int y, int z) {
            return Biome.DEFAULT;
        }

        /**
         * Get light value at specified block coordinates.
         *
         * @param x X block coordinate
         * @param y Y block coordinate
         * @param z Z block coordinate
         * @return Requested light value
         */
        public int getLight(int x, int y, int z) {
            return 0;
        }
    }

    /**
     * Represents raw NBT data for chunks.
     */
    @SuppressWarnings("CanBeFinal")
    public static class NBT extends Chunk.NBT {
        /**
         * Constructs a new instance of NBT.
         */
        public NBT() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }
    }

    /**
     * Represents raw NBT data for chunk sections.
     */
    @SuppressWarnings("CanBeFinal")
    public static class SectionNBT extends Chunk.Section.NBT {
        /**
         * Constructs a new instance of SectionNBT.
         */
        public SectionNBT() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }
    }
}
