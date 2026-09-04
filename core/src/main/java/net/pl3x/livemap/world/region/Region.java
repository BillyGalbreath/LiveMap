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

package net.pl3x.livemap.world.region;

import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NamingStrategy;
import de.bluecolored.bluenbt.TypeToken;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.util.PackedIntArrayAccess;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.block.BlockState;
import net.pl3x.livemap.world.block.BlockStateDeserializer;
import net.pl3x.livemap.world.chunk.Chunk;
import net.pl3x.livemap.world.chunk.CompressionType;
import net.pl3x.livemap.world.chunk.EmptyChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a region in a world.
 */
public class Region extends Point {
    private static final ThreadLocal<byte[]> THREAD_LOCAL_PAYLOAD_BUFFER = ThreadLocal.withInitial(() -> new byte[1024 * 1024]);

    private static final ThreadLocal<PackedIntArrayAccess> LOCAL_HEIGHTMAP_WRAPPER = ThreadLocal.withInitial(PackedIntArrayAccess::new);
    private static final ThreadLocal<PackedIntArrayAccess[]> LOCAL_BLOCK_STACK = ThreadLocal.withInitial(() -> new PackedIntArrayAccess[0]);
    private static final ThreadLocal<PackedIntArrayAccess[]> LOCAL_BIOME_STACK = ThreadLocal.withInitial(() -> new PackedIntArrayAccess[0]);

    private static final BlueNBT BLUENBT = new BlueNBT();

    static {
        BLUENBT.setNamingStrategy(NamingStrategy.lowerCaseWithDelimiter("_"));
        BLUENBT.register(TypeToken.of(BlockState.class), new BlockStateDeserializer());
    }

    /**
     * Get current thread's shared heightmap.
     *
     * @return Shared heightmap
     */
    @NotNull
    public static PackedIntArrayAccess getThreadLocalHeightmap() {
        return LOCAL_HEIGHTMAP_WRAPPER.get();
    }

    /**
     * Get thread local block stack.
     *
     * @param requiredSize Required size of stack
     * @return Thread local block stack of at least required size
     */
    @NotNull
    public static PackedIntArrayAccess @NotNull [] getThreadLocalBlockStack(int requiredSize) {
        PackedIntArrayAccess[] current = LOCAL_BLOCK_STACK.get();
        if (current.length < requiredSize) {
            PackedIntArrayAccess[] expanded = new PackedIntArrayAccess[requiredSize];
            System.arraycopy(current, 0, expanded, 0, current.length);
            for (int i = current.length; i < requiredSize; i++) {
                expanded[i] = new PackedIntArrayAccess();
            }
            LOCAL_BLOCK_STACK.set(expanded);
            return expanded;
        }
        return current;
    }

    /**
     * Get thread local biome stack.
     *
     * @param requiredSize Required size of stack
     * @return Thread local biome stack of at least required size
     */
    @NotNull
    public static PackedIntArrayAccess @NotNull [] getThreadLocalBiomeStack(int requiredSize) {
        PackedIntArrayAccess[] current = LOCAL_BIOME_STACK.get();
        if (current.length < requiredSize) {
            PackedIntArrayAccess[] expanded = new PackedIntArrayAccess[requiredSize];
            System.arraycopy(current, 0, expanded, 0, current.length);
            for (int i = current.length; i < requiredSize; i++) {
                expanded[i] = new PackedIntArrayAccess();
            }
            LOCAL_BIOME_STACK.set(expanded);
            return expanded;
        }
        return current;
    }

    /**
     * Packs a region's coordinates.
     *
     * @param regionX X region coordinate
     * @param regionZ Z region coordinate
     * @return Region's packed coordinates
     */
    public static long pack(int regionX, int regionZ) {
        // 64 bits; upper 32 are z; lower 32 are x
        return ((long) regionZ << 32) | (regionX & 0xFFFFFFFFL);
    }

    /**
     * Returns the X value from packed region coordinates.
     *
     * @param packed Packed region coordinates
     * @return Unpacked X coordinate
     */
    public static int unpackX(long packed) {
        // lower 32 bits are x
        return (int) packed;
    }

    /**
     * Returns the Z value from packed region coordinates.
     *
     * @param packed Packed region coordinates
     * @return Unpacked Z coordinate
     */
    public static int unpackZ(long packed) {
        // upper 32 bits are z
        return (int) (packed >>> 32);
    }

    private final long packed;
    private final World world;
    private final File file;

    private final Chunk[] chunks = new Chunk[1024];

    private final int hash;

    /**
     * Constructs a new instance of Region.
     *
     * @param world  Owning world
     * @param packed Packed region coordinates
     */
    public Region(@NotNull World world, long packed) {
        super(unpackX(packed), unpackZ(packed));

        this.packed = packed;
        this.world = world;
        this.file = this.world.getRegionsDir().resolve("r.%d.%d.mca".formatted(getX(), getZ())).toFile();

        this.hash = Objects.hash(world, getX(), getZ());
    }

    /**
     * Get the world this region belongs to.
     *
     * @return Owning world
     */
    @NotNull
    public World getWorld() {
        return this.world;
    }

    /**
     * Get the region's MCA file path.
     *
     * @return Region's MCA file path
     */
    @NotNull
    public File getFile() {
        return this.file;
    }

    /**
     * Get packed coordinate index for this region.
     *
     * @return Packed index
     */
    public long getIndex() {
        return this.packed;
    }

    /**
     * Get a chunk by coordinates.
     *
     * <p>If the coordinates are in the supplied chunk, then the supplied chunk is returned.
     *
     * @param chunk  Cached chunk
     * @param chunkX X coordinate
     * @param chunkZ Z coordinate
     * @return Requested chunk
     */
    @NotNull
    public Chunk getChunkFast(@NotNull Chunk chunk, int chunkX, int chunkZ) {
        if (chunk.getX() == chunkX && chunk.getZ() == chunkZ) {
            return chunk;
        }
        return getWorld().getRegionFast(this, chunkX >> 5, chunkZ >> 5).getChunk(chunkX, chunkZ);
    }

    /**
     * Get chunk at specified chunk coordinates.
     *
     * <p>If no chunk exists there, an EmptyChunk will be returned.
     *
     * @param chunkX X chunk coordinate
     * @param chunkZ Z chunk Coordinate
     * @return Requested chunk
     */
    @NotNull
    public Chunk getChunk(int chunkX, int chunkZ) {
        int index = ((chunkZ & 0x1F) << 5) | (chunkX & 0x1F);
        Chunk chunk = this.chunks[index];
        if (chunk != null) {
            return chunk;
        }
        try (RandomAccessFile raf = new RandomAccessFile(getFile(), "r")) {
            chunk = loadChunk(raf, index);
        } catch (EOFException | FileNotFoundException ignore) {
            ignore.printStackTrace();
        } catch (IOException e) {
            Logger.error("Failed to load chunk at region &3[&e%d&r, &e%d&3]".formatted(chunkX, chunkZ), e);
        }
        if (chunk == null) {
            return this.chunks[index] = new EmptyChunk(this);
        }
        return chunk;
    }

    /**
     * Load all chunks (that exist) in the region from disk.
     *
     * @param cancelled Cancellation token
     * @throws IOException if an I/O error occurs
     */
    public void loadAllChunks(@NotNull AtomicBoolean cancelled) throws IOException {
        if (!getFile().exists() || getFile().length() <= 0) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(getFile(), "r")) {
            for (int index = 0; index < this.chunks.length; index++) {
                if (getWorld().isDiscarded() || cancelled.get()) {
                    return; // aborted
                }

                loadChunk(raf, index);
            }
        } catch (EOFException ignore) {
            ignore.printStackTrace();
        }
    }

    /**
     * Load chunk from disk.
     *
     * @param raf   The region file
     * @param index Index of chunk position inside region (0-1023)
     * @return Requested chunk (may return EmptyChunk if none exists)
     * @throws IOException if an I/O error occurs
     * @see <a href="https://minecraft.wiki/w/Region_file_format#Header">Region_file_format#Header</a>
     */
    @NotNull
    public Chunk loadChunk(@NotNull RandomAccessFile raf, int index) throws IOException {
        // jump to the chunk's 4kib sector and read its header
        raf.seek(index * 4L);
        byte[] header = new byte[4];
        raf.readFully(header);

        // check reported size (technically should check all 4 bytes are 0, but whatever)
        if (header[3] == 0) {
            return this.chunks[index] = new EmptyChunk(this);
        }

        // extract the 3-byte sector offset
        // @formatter:off
        return this.chunks[index] = loadChunk(raf,
            ((header[0] & 0xFFL) << 28)
                | ((header[1] & 0xFFL) << 20)
                | ((header[2] & 0xFFL) << 12));
        // @formatter:on
    }

    /**
     * Load chunk from disk for specified region.
     *
     * @param raf    The region file
     * @param offset Sector offset for chunk data in the payload
     * @return Loaded chunk (may return EmptyChunk if none exists)
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public Chunk loadChunk(@NotNull RandomAccessFile raf, long offset) throws IOException {
        // seek to chunk data location and skip the 4-byte payload length
        raf.seek(offset);
        int length = raf.readInt();
        if (length <= 0) {
            return new EmptyChunk(this);
        }

        byte compressionTypeId = raf.readByte();
        CompressionType compression = CompressionType.byId(compressionTypeId);

        byte[] compressedPayload = THREAD_LOCAL_PAYLOAD_BUFFER.get();

        int payloadLength = length - 1; // Subtract the 1 byte for compressionTypeId;
        if (payloadLength > compressedPayload.length) {
            // if an atypical oversized custom chunk exceeds 1MB,
            // gracefully fall back to a manual allocation
            compressedPayload = new byte[payloadLength];
        }

        raf.readFully(compressedPayload, 0, payloadLength);

        // we need the chunk version to find correct loader
        int version = Chunk.getChunkDataVersion(compressedPayload, payloadLength, compression);
        Chunk.Loader<Chunk.NBT> chunkLoader = Chunk.Loader.getForVersion(version);

        try (
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedPayload, 0, payloadLength);
            InputStream cis = compression.decompress(bais);
            InputStream bis = new BufferedInputStream(cis)
        ) {
            // load the chunk
            Chunk chunk = chunkLoader.load(this, bis);

            // we only want full chunks
            return chunk.isFull() ? chunk.preScan() : new EmptyChunk(this);
        }
    }

    /**
     * Sever all references to loaded chunks inside this region
     * to allow immediate Garbage Collection reclamation.
     */
    public void unload() {
        for (int i = 0; i < this.chunks.length; i++) {
            Chunk chunk = this.chunks[i];
            if (chunk != null) {
                chunk.recycle(); // clear the BlockData pools
                this.chunks[i] = null; // break the heap reference chain
            }
        }
        LOCAL_HEIGHTMAP_WRAPPER.remove();
        LOCAL_BLOCK_STACK.remove();
        LOCAL_BIOME_STACK.remove();
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
        Region other = (Region) o;
        return getWorld().equals(other.getWorld())
            && getX() == other.getX()
            && getZ() == other.getZ();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    @NotNull
    public String toString() {
        return "Region["
            + "world=" + getWorld()
            + ",x=" + getX()
            + ",z=" + getZ()
            + "]";
    }
}
