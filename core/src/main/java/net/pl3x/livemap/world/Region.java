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

package net.pl3x.livemap.world;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Objects;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.world.chunk.Chunk;
import net.pl3x.livemap.world.chunk.EmptyChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a region in a world.
 */
public class Region extends Point {
    /**
     * Packs a region's coordinates.
     *
     * @param regionX X region coordinate
     * @param regionZ Z region coordinate
     * @return Region's packed coordinates
     */
    public static long pack(int regionX, int regionZ) {
        // 64 bits; upper 32 are z; lower 32 are x
        return ((long) regionX << 32) | (regionZ & 0xFFFFFFFFL);
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
        this(world, unpackX(packed), unpackZ(packed));
    }

    /**
     * Constructs a new instance of Region.
     *
     * @param world   Owning world
     * @param regionX X coordinate
     * @param regionZ Z coordinate
     */
    public Region(@NotNull World world, int regionX, int regionZ) {
        super(regionX, regionZ);
        this.world = world;
        this.file = this.world.getRegionsDir().resolve("r.%d.%d.mca".formatted(regionX, regionZ)).toFile();

        this.hash = Objects.hash(world, regionX, regionZ);
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
     * Get chunk at specified chunk coordinates.
     * <p>
     * If no chunk exists there, an EmptyChunk will be returned.
     *
     * @param chunkX X chunk coordinate
     * @param chunkZ Z chunk Coordinate
     * @return Requested chunk
     */
    @NotNull
    public Chunk getChunk(int chunkX, int chunkZ) {
        int index = ((chunkZ & 0x1F) << 5) | (chunkX & 0x1F);
        Chunk chunk = this.chunks[index];
        if (chunk == null) {
            try (RandomAccessFile raf = new RandomAccessFile(getFile(), "r")) {
                chunk = loadChunk(raf, index);
            } catch (EOFException | FileNotFoundException ignore) {
            } catch (IOException e) {
                Logger.error("Failed to load chunk at region &3[&e%d&r, &e%d&3]".formatted(chunkX, chunkZ), e);
            }
            if (chunk == null) {
                return this.chunks[index] = new EmptyChunk(this);
            }
        }
        return chunk;
    }

    /**
     * Load all chunks in the world (that exist).
     *
     * @throws IOException if an I/O error occurs
     */
    public void loadChunks() throws IOException {
        if (!getFile().exists() || getFile().length() <= 0) {
            return;
        }
        try (RandomAccessFile raf = new RandomAccessFile(getFile(), "r")) {
            for (int index = 0; index < this.chunks.length; index++) {
                //LiveMap.api().getRegionProcessor().checkPaused(); // todo
                loadChunk(raf, index);
            }
        } catch (EOFException ignore) {
        }
    }

    /**
     * Load chunk from disk.
     *
     * @param raf   The region file
     * @param index Index of chunk position inside region (0-1023)
     * @return Requested chunk (may return EmptyChunk if none exists)
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public Chunk loadChunk(@NotNull RandomAccessFile raf, int index) throws IOException {
        raf.seek(index * 4L);

        byte[] header = new byte[4];
        raf.readFully(header, 0, 4);

        long offset = (header[0] & 0xFF) << 16;
        offset |= (header[1] & 0xFF) << 8;
        offset |= header[2] & 0xFF;
        offset <<= 12;
        int size = (header[3] & 0xFF) * 4096;

        if (size <= 0) return this.chunks[index] = new EmptyChunk(this);
        return this.chunks[index] = LiveMap.api().getChunkLoader().load(raf, offset, this);
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
