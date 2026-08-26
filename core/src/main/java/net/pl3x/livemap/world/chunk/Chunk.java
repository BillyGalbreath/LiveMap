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

import de.bluecolored.bluenbt.NBTName;
import java.util.Objects;
import net.pl3x.livemap.world.Region;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a chunk in a region.
 */
public abstract class Chunk {
    static final int BLOCKS_PER_SECTION = 16 * 16 * 16;
    static final int VALUES_PER_HEIGHTMAP = 16 * 16;

    static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    static final int[] EMPTY_INT_ARRAY = new int[0];
    static final long[] EMPTY_LONG_ARRAY = new long[0];
    static final String[] EMPTY_STRING_ARRAY = new String[0];
    static final BlockState[] EMPTY_BLOCKSTATE_ARRAY = new BlockState[0];

    private final NBT nbt;
    private final Region region;

    /**
     * Constructs a new instance of Chunk.
     *
     * @param region Region chunk belongs to
     * @param nbt    The chunk's raw nbt data
     */
    public Chunk(@NotNull Region region, @NotNull Chunk.NBT nbt) {
        this.region = region;
        this.nbt = nbt;
    }

    /**
     * Get the world this chunk belongs to.
     *
     * @return Owning world
     */
    @NotNull
    public World getWorld() {
        return getRegion().getWorld();
    }

    /**
     * Get the region this chunk belongs to.
     *
     * @return Owning region
     */
    @NotNull
    public Region getRegion() {
        return this.region;
    }

    /**
     * Get the version of the chunk NBT structure
     *
     * @return NBT structure version
     */
    public int getVersion() {
        return this.nbt.version;
    }

    /**
     * Get the X chunk position.
     *
     * @return X chunk position
     */
    public int getX() {
        return this.nbt.xPos;
    }

    /**
     * Get the lowest Y section position
     *
     * @return Lowest Y section position
     */
    public int getY() {
        return this.nbt.yPos;
    }

    /**
     * Get the Z chunk position.
     *
     * @return Z chunk position
     */
    public int getZ() {
        return this.nbt.zPos;
    }

    /**
     * Whether chunk status is fully generated or not.
     *
     * @return True if chunk is fully generated
     */
    public abstract boolean isFull();

    /**
     * The cumulative number of ticks players have been in this chunk.
     *
     * @return Inhabited time
     */
    public abstract long getInhabitedTime();

    /**
     * Get the highest Y position from the heightmap at the specific block coordinates.
     *
     * @param blockX X block coordinate
     * @param blockZ Z block coordinate
     * @return Highest Y position
     */
    public abstract int getHeight(int blockX, int blockZ);

    /**
     * Get the block state at the specified block coordinates.
     *
     * @param blockX X block coordinate
     * @param blockY Y block coordinate
     * @param blockZ Z block coordinate
     * @return Block's state
     */
    @NotNull
    public abstract BlockState getBlockState(int blockX, int blockY, int blockZ);

    /**
     * Get the biome at the specified block coordinates.
     *
     * @param blockX X block coordinate
     * @param blockY Y block coordinate
     * @param blockZ Z block coordinate
     * @return Block's biome
     */
    @NotNull
    public abstract Biome getBiome(int blockX, int blockY, int blockZ);

    /**
     * Get the light value at the specified block coordinates.
     *
     * @param blockX X block coordinate
     * @param blockY Y block coordinate
     * @param blockZ Z block coordinate
     * @return Block's light value
     */
    public abstract int getLight(int blockX, int blockY, int blockZ);

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Chunk other = (Chunk) o;
        return getWorld().equals(other.getWorld())
            && getX() == other.getX()
            && getZ() == other.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWorld(), getX(), getZ());
    }

    @Override
    @NotNull
    public String toString() {
        return "Chunk["
            + "world=" + getWorld().getName()
            + ",xPos=" + getX()
            + ",zPos=" + getZ()
            + "]";
    }

    /**
     * Represents raw NBT data for chunks.
     */
    @SuppressWarnings("CanBeFinal")
    public static class NBT {
        @NBTName("DataVersion")
        int version = 0;

        @NBTName("xPos")
        int xPos;

        @NBTName("yPos")
        int yPos;

        @NBTName("zPos")
        int zPos;

        /**
         * Constructs a new instance of Chunk.NBT.
         */
        public NBT() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }
    }

    /**
     * Represents a chunk section (16x16x16 blocks).
     */
    @SuppressWarnings("CanBeFinal")
    public static class Section {
        /**
         * Constructs a new instance of Section.
         */
        public Section() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }

        /**
         * Represents raw NBT data for chunk sections.
         */
        @SuppressWarnings("CanBeFinal")
        public static class NBT {
            @NBTName("Y")
            int y = 0;

            @NBTName("BlockLight")
            byte[] light = EMPTY_BYTE_ARRAY;

            /**
             * Constructs a new instance of Chunk.Section.NBT.
             */
            public NBT() {
                // Explicit constructor to satisfy Javadoc and linter tools
            }
        }
    }
}
