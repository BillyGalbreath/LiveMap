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
import de.bluecolored.bluenbt.NamingStrategy;
import de.bluecolored.bluenbt.TypeToken;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;
import net.pl3x.livemap.world.Region;
import net.pl3x.livemap.world.block.BlockState;
import net.pl3x.livemap.world.block.BlockStateDeserializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The loader of chunks.
 */
public class ChunkLoader {
    private static final CompressionType[] CHUNK_COMPRESSION = new CompressionType[5];

    static {
        CHUNK_COMPRESSION[0] = CompressionType.NONE;
        CHUNK_COMPRESSION[1] = CompressionType.GZIP;
        CHUNK_COMPRESSION[2] = CompressionType.ZIP;
        CHUNK_COMPRESSION[3] = CompressionType.NONE;
        CHUNK_COMPRESSION[4] = CompressionType.LZ4;
    }

    private static final BlueNBT BLUENBT = new BlueNBT();

    static {
        BLUENBT.setNamingStrategy(NamingStrategy.lowerCaseWithDelimiter("_"));
        BLUENBT.register(TypeToken.of(BlockState.class), new BlockStateDeserializer());
    }

    /**
     * Constructs a new instance of ChunkLoader.
     */
    public ChunkLoader() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    // sorted list of chunk-versions, loaders at the start of the list are preferred over loaders at the end
    private static final List<ChunkVersionLoader<?>> CHUNK_VERSION_LOADERS = List.of(
        new ChunkVersionLoader<>(Chunk_1_18.NBT.class, Chunk_1_18::new, 2844)// ,
        // new ChunkVersionLoader<>(Chunk_1_16.NBT.class, Chunk_1_16::new, 2500),
        // new ChunkVersionLoader<>(Chunk_1_15.NBT.class, Chunk_1_15::new, 2200),
        // new ChunkVersionLoader<>(Chunk_1_13.NBT.class, Chunk_1_13::new, 1519)
    );

    private ChunkVersionLoader<?> lastUsedLoader = CHUNK_VERSION_LOADERS.getFirst();

    /**
     * Load chunk from disk for specified region.
     *
     * @param raf    The region file
     * @param offset Starting byte position in file
     * @param region Region chunk belongs to
     * @return Loaded chunk (may return EmptyChunk if none exists)
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public Chunk load(@NotNull RandomAccessFile raf, long offset, @NotNull Region region) throws IOException {
        raf.seek(offset + 4);
        int compressionTypeId = Byte.toUnsignedInt(raf.readByte());

        CompressionType compression = CHUNK_COMPRESSION[compressionTypeId];
        if (compression == null) {
            throw new IOException("Unknown chunk compression-id: " + compressionTypeId);
        }

        // optimistic: try last used version
        ChunkVersionLoader<?> usedLoader = this.lastUsedLoader;
        Chunk chunk;
        InputStream decompressedIn = new BufferedInputStream(compression.decompress(new FileInputStream(raf.getFD())));
        chunk = usedLoader.load(region, decompressedIn);

        // check version and reload chunk if the wrong loader has been used and a better one has been found
        ChunkVersionLoader<?> actualLoader = findBestLoaderForVersion(chunk.getVersion());
        if (actualLoader != null && usedLoader != actualLoader) {
            raf.seek(offset + 5);
            decompressedIn = new BufferedInputStream(compression.decompress(new FileInputStream(raf.getFD())));
            chunk = actualLoader.load(region, decompressedIn);
            this.lastUsedLoader = actualLoader;
        }

        return chunk.isFull() ? chunk : new EmptyChunk(region);
    }

    @Nullable
    private ChunkVersionLoader<?> findBestLoaderForVersion(int version) {
        for (ChunkVersionLoader<?> loader : CHUNK_VERSION_LOADERS) {
            if (loader.mightSupport(version)) {
                return loader;
            }
        }
        return null;
    }

    private record ChunkVersionLoader<NBT extends Chunk.NBT>(@NotNull Class<NBT> type, @NotNull ChunkConstructor<NBT> ctor, int version) {
        @NotNull
        public Chunk load(@NotNull Region region, @NotNull InputStream in) throws IOException {
            try {
                NBT nbt = BLUENBT.read(in, this.type);
                return mightSupport(nbt.version) ? this.ctor.create(region, nbt) : new EmptyChunk(region);
            } catch (Exception e) {
                throw new IOException("Failed to parse chunk-data (%s): %s".formatted(this.type.getSimpleName(), e), e);
            }
        }

        public boolean mightSupport(int dataVersion) {
            return dataVersion >= this.version;
        }
    }

    private interface ChunkConstructor<NBT extends Chunk.NBT> {
        @NotNull
        Chunk create(@NotNull Region region, @NotNull NBT nbt);
    }
}
