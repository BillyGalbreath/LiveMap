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
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.util.Pool;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
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

    private static final Pool<BlockData> BLOCK_DATA_POOL = new Pool<>(BlockData::new);

    // reusable 256-byte name cache buffer (Minecraft NBT keys never exceed 255 characters)
    private static final ThreadLocal<byte[]> THREAD_LOCAL_NAME_BUFFER = ThreadLocal.withInitial(() -> new byte[256]);

    private static final byte[] DATA_VERSION_BYTES = {
        // D     a     t     a     V     e     r     s     i     o     n
        0x44, 0x61, 0x74, 0x61, 0x56, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E
    };

    /**
     * Checks if a raw byte array matches the "DataVersion" signature without allocating strings.
     *
     * @param buf Byte payload
     * @param len Payload length
     * @return True if payload matches data version signature
     */
    public static boolean isDataVersionSignature(byte[] buf, int len) {
        if (len != DATA_VERSION_BYTES.length) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (buf[i] != DATA_VERSION_BYTES[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Clear the block data pool entirely to release references after major runs.
     */
    public static void clearPool() {
        BLOCK_DATA_POOL.clear();
    }

    /**
     * Get chunk's data version early without loading the end nbt.
     *
     * @param compressedPayload The compressed payload for a chunk
     * @param payloadLength     The length of the payload
     * @param compression       The compression type
     * @return The chunk's data version, or -1 if was unable to determine
     * @throws IOException if an I/O error occurs
     */
    public static int getChunkDataVersion(byte[] compressedPayload, int payloadLength, @NotNull CompressionType compression) throws IOException {
        // we need the chunk version to find correct loader
        int version;
        try (ByteArrayInputStream vbais = new ByteArrayInputStream(compressedPayload, 0, payloadLength);
             InputStream vcis = compression.decompress(vbais);
             DataInputStream vdis = new DataInputStream(vcis)
        ) {
            // root compound envelope verification (0x0A)
            if (vdis.readByte() != 0x0A) {
                version = -1;
            } else {
                // skip root name string block safely
                int rootNameLen = vdis.readUnsignedShort();
                if (rootNameLen > 0) {
                    vdis.skipBytes(rootNameLen);
                }

                byte[] nameBuffer = THREAD_LOCAL_NAME_BUFFER.get();

                int foundVersion = -1;
                while (vdis.available() > 0) {
                    byte tagType = vdis.readByte();
                    if (tagType == 0x00) {
                        break; // TAG_End
                    }

                    int nameLen = vdis.readUnsignedShort();
                    byte[] nameBytes = nameLen <= nameBuffer.length ? nameBuffer : new byte[nameLen];
                    vdis.readFully(nameBytes, 0, nameLen);

                    if (tagType == 3 && isDataVersionSignature(nameBytes, nameLen)) {
                        foundVersion = vdis.readInt();
                        break;
                    }

                    // if it isn't our metadata token, step past the data structure carefully
                    if (Chunk.skipTagPayload(vdis, tagType)) {
                        break;
                    }
                }
                version = foundVersion;
            }
        } catch (EOFException e) {
            Logger.error("Error reading chunk version", e);
            version = -1; // handled safely if hitting payload bounds early
        }
        return version;
    }

    /**
     * Step over adjacent payload footprints.
     *
     * @param dis     Input stream
     * @param tagType Type of tag
     * @return True if we didn't hit a known tag
     */
    public static boolean skipTagPayload(@NotNull DataInputStream dis, byte tagType) throws IOException {
        switch (tagType) {
            case 1: // TAG_Byte
                dis.skipBytes(1);
                return false;
            case 2: // TAG_Short
                dis.skipBytes(2);
                return false;
            case 3: // TAG_Int
                dis.skipBytes(4);
                return false;
            case 4: // TAG_Long
                dis.skipBytes(8);
                return false;
            case 5: // TAG_Float
                dis.skipBytes(4);
                return false;
            case 6: // TAG_Double
                dis.skipBytes(8);
                return false;
            case 7: // TAG_Byte_Array
                int bLen = dis.readInt();
                dis.skipBytes(bLen);
                return false;
            case 8: // TAG_String
                dis.skipBytes(dis.readUnsignedShort());
                return false;
            case 9: // TAG_List
                byte listType = dis.readByte();
                int listSize = dis.readInt();
                for (int i = 0; i < listSize; i++) {
                    if (skipTagPayload(dis, listType)) {
                        return true;
                    }
                }
                return false;
            case 10: // TAG_Compound (Nested structural sweeps)
                byte nestedType;
                while ((nestedType = dis.readByte()) != 0) {
                    int nLen = dis.readUnsignedShort();
                    dis.skipBytes(nLen);
                    if (skipTagPayload(dis, nestedType)) {
                        return true;
                    }
                }
                return false;
            case 11: // TAG_Int_Array
                int iLen = dis.readInt();
                dis.skipBytes(iLen * 4);
                return false;
            case 12: // TAG_Long_Array
                int lLen = dis.readInt();
                dis.skipBytes(lLen * 8);
                return false;
            default: // Unmapped formatting token
                return true;
        }
    }

    private final Region region;
    private final int version;
    private final int xPos;
    private final int yPos;
    private final int zPos;

    private final BlockData[] data = new BlockData[256];

    private boolean preScanned;

    /**
     * Constructs a new instance of Chunk.
     *
     * @param region Region chunk belongs to
     * @param nbt    The chunk's raw nbt data
     */
    protected Chunk(@NotNull Region region, @NotNull Chunk.NBT nbt) {
        this.region = region;
        this.version = nbt.version;
        this.xPos = nbt.xPos;
        this.yPos = nbt.yPos;
        this.zPos = nbt.zPos;
    }

    /**
     * Pre-scan this chunk and store the block data so multiple renderers can share it.
     *
     * @return This chunk
     */
    @NotNull
    public Chunk preScan() {
        if (this.preScanned) {
            return this;
        }

        int blockStartX = getX() << 4;
        int blockStartZ = getZ() << 4;

        for (int blockX = blockStartX; blockX < blockStartX + 16; blockX++) {
            for (int blockZ = blockStartZ; blockZ < blockStartZ + 16; blockZ++) {
                BlockData data = BLOCK_DATA_POOL.get();
                data.setup(this, blockX, blockZ);

                data.blockY = getHeight(blockX, blockZ) + 1;

                // if world has a ceiling (i.e., nether), iterate down until we find air
                if (getWorld().hasCeiling()) {
                    data.blockY = getWorld().getMaxY();
                    do {
                        data.blockY -= 1;
                        data.blockstate = getBlockState(blockX, data.blockY, blockZ);
                    } while (data.blockY > getWorld().getMinY() && !data.blockstate.getBlock().isAir());
                }

                // iterate down from here until we find a renderable block
                do {
                    data.blockY -= 1;
                    data.blockstate = getBlockState(blockX, data.blockY, blockZ);
                    if (data.blockstate.getBlock().isFluid()) {
                        // if we found a fluid we need to store it and then
                        // continue iterating down until we hit a solid
                        if (data.fluidstate == null) {
                            // get fluid information for only the top fluid block
                            data.fluidY = data.blockY;
                            data.fluidstate = data.blockstate;
                        }
                        continue;
                    }

                    // todo render translucent glass?
                    //

                    // test if block is renderable. we ignore blocks with black color
                    if (data.blockstate.getBlock().getColor() != 0) {
                        break;
                    }
                } while (data.blockY > getWorld().getMinY());

                // if we found a flat block, render the block under it instead
                if (data.blockstate.getBlock().isFlat()) {
                    data.blockY--;
                }

                this.data[((blockZ & 0xF) << 4) + (blockX & 0xF)] = data;
            }
        }

        this.preScanned = true;

        return this;
    }

    /**
     * Flush all local block data entities back into the global object pool.
     */
    public void recycle() {
        for (int i = 0; i < this.data.length; i++) {
            BlockData data = this.data[i];
            if (data != null) {
                BLOCK_DATA_POOL.put(data);
                this.data[i] = null;
            }
        }
        this.preScanned = false;
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
        return this.version;
    }

    /**
     * Get the X chunk position.
     *
     * @return X chunk position
     */
    public int getX() {
        return this.xPos;
    }

    /**
     * Get the Y chunk position (lowest Y section position).
     *
     * @return Y chunk position
     */
    public int getMinY() {
        return this.yPos;
    }

    /**
     * Get the Z chunk position.
     *
     * @return Z chunk position
     */
    public int getZ() {
        return this.zPos;
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

    /**
     * Get pre-scanned block data at specified block coordinates.
     *
     * <p>This data can be reused by multiple renderers.
     *
     * @param blockX X block coordinate
     * @param blockZ Z block coordinate
     * @return Block's pre-scanned data
     */
    public @Nullable BlockData getData(int blockX, int blockZ) {
        return this.data[((blockZ & 0xF) << 4) + (blockX & 0xF)];
    }

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
    @SuppressWarnings("FieldMayBeFinal")
    public static class NBT {
        @NBTName("DataVersion")
        private int version = 0;

        @NBTName("xPos")
        private int xPos;

        @NBTName("yPos")
        private int yPos;

        @NBTName("zPos")
        private int zPos;
    }

    /**
     * Represents a chunk section (16x16x16 blocks).
     */
    @SuppressWarnings("FieldMayBeFinal")
    public static class Section {
        /**
         * Section NBT.
         */
        @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
        public static class NBT {
            @NBTName("Y")
            private int y = 0;

            @NBTName("BlockLight")
            private byte[] light = EMPTY_BYTE_ARRAY;

            /**
             * Get the Y position of this section.
             *
             * @return Y position
             */
            public int getY() {
                return this.y;
            }

            /**
             * Get block light nibbles.
             *
             * @return Block light
             */
            public byte[] getLight() {
                return this.light;
            }
        }
    }

    /**
     * The loader of chunks.
     *
     * @param <NBT> Chunk nbt type
     */
    public static final class Loader<NBT extends Chunk.NBT> {
        private static final Loader<? extends Chunk.NBT>[] LOADERS = new Loader<?>[] {
            new Loader<>(Chunk.NBT.class, EmptyChunk::new),
            new Loader<>(Chunk_1_20.NBT.class, Chunk_1_20::new),
            new Loader<>(Chunk_1_18.NBT.class, Chunk_1_18::new),
            new Loader<>(Chunk_1_16.NBT.class, Chunk_1_16::new),
            new Loader<>(Chunk_1_15.NBT.class, Chunk_1_15::new),
            new Loader<>(Chunk_1_13.NBT.class, Chunk_1_13::new)
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

        /**
         * Temp.
         */
        public final Class<NBT> type;
        private final Ctor<NBT> ctor;

        private Loader(@NotNull Class<NBT> type, @NotNull Ctor<NBT> ctor) {
            this.type = type;
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
                NBT nbt = BLUENBT.read(in, this.type);
                return this.ctor.create(region, nbt);
            } catch (Exception e) {
                throw new IOException("Failed to parse chunk-data (%s): %s"
                    .formatted(this.type.getSimpleName(), e), e);
            }
        }

        private interface Ctor<NBT extends Chunk.NBT> {
            @NotNull
            Chunk create(@NotNull Region region, @NotNull NBT nbt);
        }
    }

    /**
     * Represents pre-scanned block data that can be reused by multiple renderers.
     */
    public static class BlockData implements Pool.Reusable {
        protected Chunk chunk;
        protected int blockX;
        protected int blockZ;

        protected int blockY;
        protected int fluidY;
        protected BlockState blockstate;
        protected BlockState fluidstate;
        protected Biome biome;

        /**
         * Constructs a new instance of BlockData.
         */
        public BlockData() {
        }

        /**
         * Re-initializes with fresh data.
         *
         * @param chunk  Chunk this block data belongs to
         * @param blockX X block coordinate
         * @param blockZ Z block coordinate
         */
        public void setup(@NotNull Chunk chunk, int blockX, int blockZ) {
            this.chunk = chunk;
            this.blockX = blockX;
            this.blockZ = blockZ;

            this.blockY = 0;
            this.fluidY = 0;
            this.blockstate = null;
            this.fluidstate = null;
            this.biome = null;
        }

        /**
         * Get the block.
         *
         * @return The block
         */
        @NotNull
        public Block getBlock() {
            return this.blockstate.getBlock();
        }

        /**
         * Get the fluid, if there is one.
         *
         * @return The fluid, or null if not a fluid
         */
        @Nullable
        public Block getFluid() {
            return this.fluidstate == null ? null : this.fluidstate.getBlock();
        }

        /**
         * Get the chunk this block data belongs to.
         *
         * @return Owning chunk
         */
        @NotNull
        public Chunk getChunk() {
            return this.chunk;
        }

        /**
         * Get the region this block data belongs to.
         *
         * @return Owning region
         */
        @NotNull
        public Region getRegion() {
            return this.chunk.region;
        }

        /**
         * Get the world this block data belongs to.
         *
         * @return Owning world
         */
        @NotNull
        public World getWorld() {
            return this.chunk.region.getWorld();
        }

        /**
         * Get block's X coordinate.
         *
         * @return X block coordinate
         */
        public int getBlockX() {
            return this.blockX;
        }

        /**
         * Get block's Y coordinate.
         *
         * @return Y block coordinate
         */
        public int getBlockY() {
            return this.blockY;
        }

        /**
         * Get block's Z coordinate.
         *
         * @return Z block coordinate
         */
        public int getBlockZ() {
            return this.blockZ;
        }

        /**
         * Get fluid's Y coordinate.
         *
         * @return Y fluid coordinate
         */
        public int getFluidY() {
            return this.fluidY;
        }

        /**
         * Get the stored block state.
         *
         * @return Block's state
         */
        @NotNull
        public BlockState getBlockState() {
            return this.blockstate;
        }

        /**
         * Get the stored fluid state.
         *
         * @return Fluid's state, or null if no fluid
         */
        @Nullable
        public BlockState getFluidState() {
            return this.fluidstate;
        }

        /**
         * Get the top stored state, fluid or block.
         *
         * @return The top state
         */
        @NotNull
        public BlockState getTopState() {
            return this.fluidstate != null ? this.fluidstate : this.blockstate;
        }

        /**
         * Get the biome at this block.
         *
         * <p>This is lazy loaded the first time it is called.
         *
         * @return Block's biome
         */
        public @NotNull Biome getBiome() {
            if (this.biome == null) {
                // calculate real biome
                this.biome = getChunk().getRegion().getWorld().getBiomeRegistry()
                    .getBiome(getChunk().getRegion(), getBlockX(), getBlockY(), getBlockZ());
            }
            return this.biome;
        }
    }
}
