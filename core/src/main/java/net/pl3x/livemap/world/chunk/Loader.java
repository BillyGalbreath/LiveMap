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
    /**
     * The chunk loaders.
     *
     * @see Loader#getForVersion(int)
     */
    public static final Loader<?>[] LOADERS = new Loader[] {
        new Loader<>(EmptyChunk::new),
        new Loader<>(Chunk_1_20::new),
        new Loader<>(Chunk_1_18::new),
        new Loader<>(Chunk_1_16::new),
        new Loader<>(Chunk_1_15::new),
        new Loader<>(Chunk_1_13::new)
    };

    public static final BlueNBT BLUENBT = new BlueNBT();

    static {
        BLUENBT.setNamingStrategy(NamingStrategy.lowerCaseWithDelimiter("_"));
        BLUENBT.register(TypeToken.of(BlockState.class), new BlockStateDeserializer());
    }

    /**
     * Get chunk loader for specified chunk version.
     *
     * <p>Chunk versions with a loader:<br/>
     * &emsp;&bull; {@code 3837} <em>(1.20.5)</em> - tile/block entity structure - component overhaul<br/>
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
     */
    @NotNull
    public static <NBT extends Chunk.NBT> Loader<NBT> getForVersion(int version) {
        // @formatter:off
        // get correct loader
        // https://minecraft.wiki/w/Data_version#List_of_data_versions
        return Unsafe.cast(Loader.LOADERS[
              (version >= 3837) ? 1 // 1.20.5 - tile/block entity structure - component overhaul
            : (version >= 2834) ? 2 // 1.18 - larger and configurable world height
            : (version >= 2529) ? 3 // 1.16 - unstretched bit packing (blockstates and heightmaps)
            : (version >= 2203) ? 4 // 1.15 - 3d biomes
            : (version >= 1451) ? 5 // 1.13 - the flattening
            :                     0 // older than 1.13; ignore
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
