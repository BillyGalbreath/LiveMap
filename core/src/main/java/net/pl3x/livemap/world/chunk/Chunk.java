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

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTName;
import de.bluecolored.bluenbt.NamingStrategy;
import de.bluecolored.bluenbt.TypeToken;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.BlockState;
import net.pl3x.livemap.world.block.BlockStateDeserializer;
import net.pl3x.livemap.world.region.Region;
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

    // target sequence: TAG_Int (0x03) + Name Length (0x00 0x0B) + "DataVersion"
    private static final byte[] DATA_VERSION_SEQ = {0x03, 0x00, 0x0B, 'D', 'a', 't', 'a', 'V', 'e', 'r', 's', 'i', 'o', 'n'};

    /**
     * Get chunk's data version early without loading the end nbt.
     *
     * @param raf         The region file
     * @param compression The compression type
     * @return The chunk's data version, or -1 if was unable to determine
     * @throws IOException if an I/O error occurs
     */
    public static int getChunkDataVersion(@NotNull RandomAccessFile raf, @NotNull CompressionType compression) throws IOException {
        // grab a tiny chunk of compressed data (1024 bytes is plenty for headers)
        byte[] compressedBuffer = new byte[1024];
        int bytesRead = raf.read(compressedBuffer);
        if (bytesRead <= 0) {
            return -1;
        }

        // create an uncompression stream
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedBuffer, 0, bytesRead);
             BufferedInputStream bis = new BufferedInputStream(compression.decompress(bais))
        ) {
            // decompress just enough to scan the early NBT headers
            byte[] raw = bis.readNBytes(2048);

            int i = jumpToSequence(raw, DATA_VERSION_SEQ);
            if (i != -1 && i + 4 <= raw.length) {
                // the next 4 bytes contain the integer value
                // @formatter:off
                return ((raw[i    ] & 0xFF) << 24)
                    | ((raw[i + 1] & 0xFF) << 16)
                    | ((raw[i + 2] & 0xFF) << 8)
                    |  (raw[i + 3] & 0xFF);
                // @formatter:on
            }
        }
        // `DataVersion` tag not found
        return -1;
    }

    private static int jumpToSequence(byte @NotNull [] src, @SuppressWarnings("SameParameterValue") byte @NotNull [] seq) {
        int i, j;
        boolean match;
        for (i = 0; i <= src.length - seq.length; i++) {
            match = true;
            for (j = 0; j < seq.length; j++) {
                if (src[i + j] != seq[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return i + seq.length;
            }
        }
        return -1;
    }

    private final NBT nbt;
    private final Region region;

    /**
     * Constructs a new instance of Chunk.
     *
     * @param region Region chunk belongs to
     * @param nbt    The chunk's raw nbt data
     */
    protected Chunk(@NotNull Region region, @NotNull Chunk.NBT nbt) {
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
     * Get the version of the chunk NBT structure.
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
     * Get the lowest Y section position.
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
    }

    /**
     * Represents a chunk section (16x16x16 blocks).
     */
    @SuppressWarnings("CanBeFinal")
    protected static class Section {
        @SuppressWarnings("CanBeFinal")
        static class NBT {
            @NBTName("Y")
            int y = 0;

            @NBTName("BlockLight")
            byte[] light = EMPTY_BYTE_ARRAY;
        }
    }

    /**
     * The loader of chunks.
     *
     * @param <NBT> Chunk nbt type
     */
    public static final class Loader<NBT extends Chunk.NBT> {
        private static final Loader<?>[] LOADERS = new Loader[] {
            new Loader<>(EmptyChunk::new),
            new Loader<>(Chunk_1_20::new),
            new Loader<>(Chunk_1_18::new),
            new Loader<>(Chunk_1_16::new),
            new Loader<>(Chunk_1_15::new),
            new Loader<>(Chunk_1_13::new)
        };

        private static final BlueNBT BLUENBT = new BlueNBT();

        static {
            BLUENBT.setNamingStrategy(NamingStrategy.lowerCaseWithDelimiter("_"));
            BLUENBT.register(TypeToken.of(BlockState.class), new BlockStateDeserializer());
        }

        /**
         * Get chunk loader for specified chunk version.
         *
         * <p>Chunk versions with a loader:<br/>
         * &emsp;&bull; {@code 3837} <em>(1.20.5)</em> - block entity structure and component overhaul<br/>
         * &emsp;&bull; {@code 2834} <em>(1.18)</em> - larger and configurable world height<br/>
         * &emsp;&bull; {@code 2529} <em>(1.16)</em> - unstretched bit packing (blockstates and heightmaps)<br/>
         * &emsp;&bull; {@code 2203} <em>(1.15)</em> - 3d biomes<br/>
         * &emsp;&bull; {@code 1451} <em>(1.13)</em> - the flattening
         *
         * <p>This method will return the first loader greater than or equal to the specified {@code version}.
         *
         * <p>Any version less than {@code 1451} will return a loader that only "loads" {@link EmptyChunk}.
         *
         * @param version Chunk version
         * @param <NBT>   Type of chunk nbt
         * @return Chunk loader
         * @see <a href="https://minecraft.wiki/w/Data_version#List_of_data_versions">https://minecraft.wiki/w/Data_version#List_of_data_versions</a>
         */
        @NotNull
        public static <NBT extends Chunk.NBT> Loader<NBT> getForVersion(int version) {
            // @formatter:off
            return Unsafe.cast(Loader.LOADERS[
                (version >= 3837) ? 1
                    : (version >= 2834) ? 2
                    : (version >= 2529) ? 3
                    : (version >= 2203) ? 4
                    : (version >= 1451) ? 5
                    :                     0
                ]);
            // @formatter:on
        }

        private final TypeToken<NBT> type;
        private final Ctor<NBT> ctor;

        private Loader(@NotNull Ctor<NBT> ctor) {
            this.type = new TypeToken<>() {
            };
            this.ctor = ctor;
        }

        /**
         * Load input stream into a chunk for specified region.
         *
         * @param region Owning region
         * @param in     Input stream of chunk payload data
         * @return Newly loaded chunk
         * @throws IOException if an I/O error occurs
         */
        @NotNull
        public Chunk load(@NotNull Region region, @NotNull InputStream in) throws IOException {
            try {
                return this.ctor.create(region, BLUENBT.read(in, this.type));
            } catch (Exception e) {
                throw new IOException("Failed to parse chunk-data (%s): %s"
                    .formatted(this.type.getRawType().getSimpleName(), e), e);
            }
        }

        private interface Ctor<NBT extends Chunk.NBT> {
            @NotNull
            Chunk create(@NotNull Region region, @NotNull NBT nbt);
        }
    }
}
