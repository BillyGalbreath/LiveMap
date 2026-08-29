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
import java.io.IOException;
import java.io.InputStream;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.block.BlockState;
import net.pl3x.livemap.world.block.BlockStateDeserializer;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;

/**
 * The loader of chunks.
 *
 * @param <NBT> Chunk nbt type
 */
public final class Loader<NBT extends Chunk.NBT> {
    public static final Loader<?>[] LOADERS = new Loader[] {
        new Loader<>(EmptyChunk::new), // don't worry about pre 1.13 chunks
        new Loader<>(Chunk_1_13::new),
        new Loader<>(Chunk_1_15::new),
        new Loader<>(Chunk_1_16::new),
        new Loader<>(Chunk_1_18::new)
    };

    public static final BlueNBT BLUENBT = new BlueNBT();

    static {
        BLUENBT.setNamingStrategy(NamingStrategy.lowerCaseWithDelimiter("_"));
        BLUENBT.register(TypeToken.of(BlockState.class), new BlockStateDeserializer());
    }

    /**
     * Get chunk loader for specified chunk version.
     *
     * @param chunkVersion Chunk version
     * @param <NBT>        Type of chunk nbt
     * @return Chunk loader
     */
    @NotNull
    public static <NBT extends Chunk.NBT> Loader<NBT> getForVersion(int chunkVersion) {
        // @formatter:off
        // get correct loader
        // https://minecraft.wiki/w/Data_version#List_of_data_versions
        return Unsafe.cast(Loader.LOADERS[
              (chunkVersion < 1519) ? 0 // wtf, older than 1.13
            : (chunkVersion < 2200) ? 1 // 1.13 - 1.14
            : (chunkVersion < 2500) ? 2 // 1.15
            : (chunkVersion < 2844) ? 3 // 1.16 - 1.18 (21w42a)
            :                         4 // 1.18+ (21w43a+)
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

    interface Ctor<NBT extends Chunk.NBT> {
        @NotNull
        Chunk create(@NotNull Region region, @NotNull NBT nbt);
    }
}
