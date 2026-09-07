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

package net.pl3x.livemap.render.renderer;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.Type;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a map renderer.
 */
public abstract class Renderer {
    public static final Type<Renderer> BASIC = Type.register(new Type<>("basic", BasicRenderer.class));
    public static final Type<Renderer> BIOMES = Type.register(new Type<>("biomes", BiomesRenderer.class));
    public static final Type<Renderer> FANCY = Type.register(new Type<>("fancy", FancyRenderer.class));
    public static final Type<Renderer> FLOWERMAP = Type.register(new Type<>("flowermap", FlowerMapRenderer.class));
    public static final Type<Renderer> INHABITED = Type.register(new Type<>("inhabited", InhabitedRenderer.class));
    public static final Type<Renderer> NETHER_ROOF = Type.register(new Type<>("nether_roof", NetherRoofRenderer.class));

    private final Type<Renderer> type;
    private final String name;
    private final String icon;
    private final Type<Heightmap> heightmapType;
    private final int biomeBlend;
    private final boolean translucentFluids;

    /**
     * Constructs a new instance of Renderer.
     *
     * @param type              The type of renderer
     * @param name              Display name for renderer
     * @param icon              Icon file for webmap
     * @param heightmapType     The heightmap type to use
     * @param biomeBlend        Number of blocks to blend biome tints
     * @param translucentFluids True to render fluids as translucent
     *
     */
    public Renderer(
        @NotNull Type<Renderer> type,
        @NotNull String name,
        @NotNull String icon,
        @NotNull Type<Heightmap> heightmapType,
        int biomeBlend,
        boolean translucentFluids
    ) {
        this.type = type;
        this.name = name;
        this.icon = icon;
        this.heightmapType = heightmapType;
        this.biomeBlend = biomeBlend;
        this.translucentFluids = translucentFluids;
    }

    /**
     * Get renderer type.
     *
     * @return Type of renderer
     */
    @NotNull
    public Type<Renderer> getType() {
        return this.type;
    }

    /**
     * Get display name for webmap.
     *
     * @return Display name
     */
    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * Get the icon for webmap.
     *
     * @return The icon
     */
    @NotNull
    public String getIcon() {
        return this.icon;
    }

    /**
     * Get the heightmap type for this renderer.
     *
     * @return The heightmap type
     */
    @NotNull
    public Type<Heightmap> getHeightmapType() {
        return this.heightmapType;
    }

    /**
     * Get the number of blocks to blend biome tints together.
     *
     * @return Number of blocks to blend biome tints
     */
    public int getBiomeBlend() {
        return this.biomeBlend;
    }

    /**
     * Check if fluids are translucent for this map render.
     *
     * @return True if fluids are translucent
     */
    public boolean isTranslucentFluids() {
        return this.translucentFluids;
    }

    /**
     * Render the specified region.
     *
     * @param tile      Tile image to render to
     * @param rand      Random for RNG stuff
     * @param cancelled Cancellation token
     * @return True if the entire region was rendered, false if aborted
     */
    public boolean renderRegion(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        int chunkStartX = tile.getRegion().getX() << 5;
        int chunkStartZ = tile.getRegion().getZ() << 5;

        for (int chunkX = chunkStartX; chunkX < chunkStartX + 32; chunkX++) {
            int blockStartX = chunkX << 4;
            for (int chunkZ = chunkStartZ; chunkZ < chunkStartZ + 32; chunkZ++) {
                // check world state and interruptions, for instant responsiveness
                if (tile.getWorld().isDiscarded() || cancelled.get()) {
                    return false; // aborted
                }

                Chunk chunk = tile.getRegion().getChunk(chunkX, chunkZ);
                if (!chunk.isFull()) {
                    continue; // chunk not fully generated
                }

                int blockStartZ = chunkZ << 4;

                for (int blockX = blockStartX; blockX < blockStartX + 16; blockX++) {
                    for (int blockZ = blockStartZ; blockZ < blockStartZ + 16; blockZ++) {
                        Chunk.BlockData data = chunk.getData(blockX, blockZ);
                        if (data == null) {
                            continue; // this shouldn't happen, but just in case
                        }
                        renderBlock(tile, data, rand);
                    }
                }
            }
        }

        return true;
    }

    /**
     * A chance to do things <em>before</em> the render has run.
     *
     * @param tile Tile image
     * @param data Block data
     * @param rand Random for RNG stuff
     */
    protected void preRender(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        // optional override
    }

    /**
     * Render the block on the tile using the block data.
     *
     * @param tile Tile image
     * @param data Block data
     * @param rand Random for RNG stuff
     */
    protected abstract void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand);

    /**
     * A chance to do things <em>after</em> the render has run.
     *
     * @param tile Tile image
     * @param data Block data
     * @param rand Random for RNG stuff
     */
    protected void postRender(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        // optional override
    }
}
