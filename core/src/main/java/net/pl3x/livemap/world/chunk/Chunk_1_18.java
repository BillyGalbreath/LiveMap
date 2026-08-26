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
import net.pl3x.livemap.util.MCAMath;
import net.pl3x.livemap.util.PackedIntArrayAccess;
import net.pl3x.livemap.world.Region;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.block.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a chunk in the 1.18+ format.
 */
public final class Chunk_1_18 extends Chunk {
    private static final Section[] EMPTY_SECTION_ARRAY = new Section[0];
    private static final SectionNBT[] EMPTY_SECTION_NBT_ARRAY = new SectionNBT[0];
    private static final HeightmapsNBT EMPTY_HEIGHTMAPS_NBT = new HeightmapsNBT();
    private static final BlockStatesNBT EMPTY_BLOCKSTATES_NBT = new BlockStatesNBT();
    private static final BiomesNBT EMPTY_BIOMES_NBT = new BiomesNBT();

    private final boolean isFull;

    private long inhabitedTime;

    private PackedIntArrayAccess heightmap;

    private Section[] sections = EMPTY_SECTION_ARRAY;

    /**
     * Constructs a new instance of Chunk_1_18.
     *
     * @param region   Region chunk belongs to
     * @param chunkNBT NBT data for chunk
     */
    public Chunk_1_18(@NotNull Region region, @NotNull Chunk_1_18.NBT chunkNBT) {
        super(region, chunkNBT);

        this.isFull = chunkNBT.status.endsWith("full");
        if (!this.isFull) {
            // chunk not fully generated. don't load anything.
            return;
        }

        this.inhabitedTime = chunkNBT.inhabitedTime;

        int bitsPerHeightmapElement = MCAMath.ceilLog2(getWorld().getHeight() + 1);
        this.heightmap = new PackedIntArrayAccess(bitsPerHeightmapElement, chunkNBT.heightmaps.worldSurface);
        if (!this.heightmap.isExpectedSize(VALUES_PER_HEIGHTMAP)) {
            this.heightmap = null;
        }

        SectionNBT[] sectionsNBT = chunkNBT.sections;
        if (sectionsNBT != null && sectionsNBT.length > 0) {
            this.sections = new Section[sectionsNBT.length];
            for (SectionNBT sectionNBT : sectionsNBT) {
                Section section = new Section(getWorld(), sectionNBT);
                this.sections[section.getY() - this.getY()] = section;
            }
        }
    }

    @Override
    public boolean isFull() {
        return this.isFull;
    }

    @Override
    public long getInhabitedTime() {
        return inhabitedTime;
    }

    @Override
    public int getHeight(int blockX, int blockZ) {
        if (heightmap == null) {
            return getWorld().getMinY();
        }
        return this.heightmap.get(((getZ() & 0xF) << 4) | (getX() & 0xF)) + getWorld().getMinY();
    }

    @Override
    @NotNull
    public BlockState getBlockState(int x, int y, int z) {
        int sectionY = y >> 4;
        Section section = getSection(sectionY);
        return section == null ? Block.AIR.getDefaultState() : section.getBlockState(x, y, z);
    }

    @Override
    @NotNull
    public Biome getBiome(int x, int y, int z) {
        int sectionY = y >> 4;
        Section section = getSection(sectionY);
        return section == null ? Biome.DEFAULT : section.getBiome(x, y, z);
    }

    @Override
    public int getLight(int x, int y, int z) {
        int sectionY = y >> 4;
        Section section = getSection(sectionY);
        return section == null ? 15 : section.getLight(x, y, z);
    }

    /**
     * Get chunk section at specified Y chunk coordinate.
     *
     * @param y Y chunk coordinate
     * @return Requested chunk section
     */
    @Nullable
    public Section getSection(int y) {
        y -= this.sections[0].getY();
        if (y < 0 || y >= this.sections.length) return null;
        return this.sections[y];
    }

    /**
     * Represents a chunk section (16x16x16 blocks).
     */
    public static class Section extends Chunk.Section {
        private final SectionNBT nbt;

        private final Biome[] biomePalette;
        private final PackedIntArrayAccess blocks;
        private final PackedIntArrayAccess biomes;
        private final byte[] light;

        /**
         * Constructs a new instance of Section.
         *
         * @param world The world this chunk section belongs to
         * @param nbt   The section's raw nbt data
         */
        public Section(@NotNull World world, @NotNull SectionNBT nbt) {
            this.nbt = nbt;

            this.biomePalette = new Biome[nbt.biomes.palette.length];
            for (int i = 0; i < this.biomePalette.length; i++) {
                this.biomePalette[i] = world.getBiomeRegistry().getOrDefault(nbt.biomes.palette[i], Biome.DEFAULT);
            }

            this.blocks = new PackedIntArrayAccess(nbt.blockStates.data, BLOCKS_PER_SECTION);
            this.biomes = new PackedIntArrayAccess(Math.max(MCAMath.ceilLog2(this.biomePalette.length), 1), nbt.biomes.data);

            this.light = nbt.light;
        }

        /**
         * Get the Y chunk coordinate for this section.
         *
         * @return Y chunk coordinate
         */
        public int getY() {
            return this.nbt.y;
        }

        /**
         * Get block state palette for this section.
         *
         * @return Block state palette
         */
        @NotNull
        public BlockState[] getBlockPalette() {
            return this.nbt.blockStates.palette;
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
            return switch (getBlockPalette().length) {
                case 0 -> Block.AIR.getDefaultState();
                case 1 -> getBlockPalette()[0];
                default -> {
                    int id = this.blocks.get(((y & 0xF) << 8) | ((z & 0xF) << 4) | (x & 0xF));
                    yield id < getBlockPalette().length ? getBlockPalette()[id] : Block.AIR.getDefaultState();
                }
            };
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
            return switch (this.biomePalette.length) {
                case 0 -> Biome.DEFAULT;
                case 1 -> this.biomePalette[0];
                default -> {
                    int id = this.biomes.get(((y & 0xC) << 2) | (z & 0xC) | ((x & 0xC) >> 2));
                    yield id < this.biomePalette.length ? this.biomePalette[id] : Biome.DEFAULT;
                }
            };
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
            if (this.light.length == 0) {
                return 0;
            }

            int i = ((y & 0xF) << 8) | ((z & 0xF) << 4) | x & 0xF;
            return MCAMath.getByteHalf(this.light[i >> 1], (i & 0x1) != 0);
        }
    }

    /**
     * Represents raw NBT data for chunks.
     */
    @SuppressWarnings("CanBeFinal")
    public static class NBT extends Chunk.NBT {
        @NBTName("Status")
        String status = "minecraft:empty";

        @NBTName("InhabitedTime")
        long inhabitedTime = 0;

        @NBTName("Heightmaps")
        HeightmapsNBT heightmaps = EMPTY_HEIGHTMAPS_NBT;

        @NBTName("sections")
        SectionNBT[] sections = EMPTY_SECTION_NBT_ARRAY;

        /**
         * Constructs a new instance of Chunk_1_18.NBT.
         */
        public NBT() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }
    }

    /**
     * Represents raw NBT data for chunk heightmaps.
     */
    @SuppressWarnings("CanBeFinal")
    public static class HeightmapsNBT {
        @NBTName("WORLD_SURFACE")
        long[] worldSurface = EMPTY_LONG_ARRAY;

        /**
         * Constructs a new instance of HeightmapsNBT.
         */
        public HeightmapsNBT() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }
    }

    /**
     * Represents raw NBT data for chunk sections.
     */
    @SuppressWarnings("CanBeFinal")
    public static class SectionNBT extends Chunk.Section.NBT {
        @NBTName("block_states")
        BlockStatesNBT blockStates = EMPTY_BLOCKSTATES_NBT;

        @NBTName("biomes")
        BiomesNBT biomes = EMPTY_BIOMES_NBT;

        /**
         * Constructs a new instance of Chunk_1_18.Section.NBT.
         */
        public SectionNBT() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }
    }

    /**
     * Represents raw NBT data for block states.
     */
    @SuppressWarnings("CanBeFinal")
    public static class BlockStatesNBT {
        @NBTName("palette")
        BlockState[] palette = EMPTY_BLOCKSTATE_ARRAY;

        @NBTName("data")
        long[] data = EMPTY_LONG_ARRAY;

        /**
         * Constructs a new instance of BlockStatesNBT.
         */
        public BlockStatesNBT() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }
    }

    /**
     * Represents raw NBT data for biomes.
     */
    @SuppressWarnings("CanBeFinal")
    public static class BiomesNBT {
        @NBTName("palette")
        String[] palette = EMPTY_STRING_ARRAY;

        @NBTName("data")
        long[] data = EMPTY_LONG_ARRAY;

        /**
         * Constructs a new instance of BiomesNBT.
         */
        public BiomesNBT() {
            // Explicit constructor to satisfy Javadoc and linter tools
        }
    }
}
