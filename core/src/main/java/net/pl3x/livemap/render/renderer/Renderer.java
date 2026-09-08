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
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.Mathf;
import net.pl3x.livemap.util.Type;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
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
        preRender(tile, rand, cancelled);

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

        postRender(tile, rand, cancelled);

        return true;
    }

    /**
     * A chance to do things <em>before</em> the render has run.
     *
     * @param tile      Tile image
     * @param rand      Random for RNG stuff
     * @param cancelled Cancellation token
     */
    protected void preRender(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        // optional override
    }

    /**
     * A chance to do things <em>after</em> the render has run.
     *
     * @param tile      Tile image
     * @param rand      Random for RNG stuff
     * @param cancelled Cancellation token
     */
    protected void postRender(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
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
     * Process block custom color from colors.yml with block state and properties.
     *
     * @param data Block data to process
     * @return Custom color to render
     */
    protected int processBlockColor(@NotNull Chunk.BlockData data) {
        int color = data.getBlock().getColor();
        if (color == 0) {
            // nothing to color
            return color;
        }
        // check most popular block types first for efficiency
        if (data.getBlock().hasFlag(Block.FLAG_GRASS)) {
            return sampleNeighbors(data, (biome, x, z) -> biome.getGrassModifier().modify(x, z, biome.getGrass()));
        } else if (data.getBlock().hasFlag(Block.FLAG_FOLIAGE)) {
            return sampleNeighbors(data, (biome, _, _) -> biome.getFoliage());
        } else if (data.getBlock().hasFlag(Block.FLAG_DRY_FOLIAGE)) {
            return sampleNeighbors(data, (biome, _, _) -> biome.getDryFoliage());
        } else if (data.getBlockState().getMoisture() >= 0) {
            return data.getBlockState().getMoisture() >= 7 ? 0x512C0F : 0x8E6646; // from textures
        } else if (data.getBlockState().getPower() >= 0) {
            // redstone_lamp
            // copper_bulb, exposed_copper_bulb, oxidized_copper_bulb, weathered_copper_bulb
            // waxed_copper_bulb, waxed_exposed_copper_bulb, waxed_oxidized_copper_bulb, waxed_weathered_copper_bulb
            return LiveMap.api().getRedstoneColorForPower(data.getBlockState().getPower());
        } else if (data.getBlockState().getAge() >= 0) {
            return switch (data.getBlock().getId()) {
                case "minecraft:wheat" -> Colors.BLOCK_WHEAT_COLOR[data.getBlockState().getAge()];
                // case "minecraft:beetroot" -> ["#448D29", "#448C29", "#448528", "#58611F"];
                // case "minecraft:carrots" -> ["#2D6F28","#367A28","#387326","#4C7E26"];
                // case "minecraft:pitcher_crop" -> ["b5714a", "3c4a39", "476946", "7086b5", "6f6ccc"];
                // case "minecraft:potatoes" => ["3C8329", "46872A", "52832E", "51882E"];
                // sweet berry bush
                // torchflower
                case "minecraft:melon_stem", "minecraft:pumpkin_stem" -> Colors.BLOCK_STEM_COLOR[data.getBlockState().getAge()];
                case "minecraft:cocoa" -> Colors.BLOCK_COCOA_COLOR[data.getBlockState().getAge()];
                default -> color;
            };
        }
        return color;
    }

    /**
     * Process fluid custom color from colors.yml with fluid state and properties.
     *
     * @param tile       Tile image
     * @param data       Block data to process
     * @param pixelColor Current color to render
     * @return Altered color to render
     */
    protected int processFluidColor(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, int pixelColor) {
        Block fluid = data.getFluid();
        if (fluid == null) {
            return pixelColor;
        }

        int fluidColor;
        int fluidDepth = data.getFluidY() - data.getBlockY();

        // get translucent fluid color
        if (tile.getRenderer().isTranslucentFluids()) {
            float depthMod = fluidDepth * 0.025F;
            if (fluid.hasFlag(Block.FLAG_WATER)) {
                // translucent water
                fluidColor = sampleNeighbors(data, (biome, _, _) -> biome.getWater());
                // make color lighter in shallower depths
                fluidColor = Colors.lerpRGB(fluidColor, 0x000000, Math.clamp(Mathf.easeCubicOut(depthMod / 1.5F), 0, 0.45F));
                // make color more translucent in shallower depths
                fluidColor = (fluidColor & 0xFFFFFF) | ((int) (Mathf.easeQuinticOut(Math.clamp(depthMod * 5F, 0, 1)) * 0xFF) << 24);
            } else {
                // opaque (but shaded) lava
                fluidColor = 0xFF000000 | Colors.lerpRGB(fluid.getColor(), 0x000000, Math.clamp(Mathf.easeCubicOut(depthMod / 1.5F), 0, 0.3F));
            }
            return Colors.blend(fluidColor, pixelColor);
        }

        // get solid fluid color
        if (fluid.isWater()) {
            fluidColor = sampleNeighbors(data, (biome, _, _) -> biome.getWater());
        } else {
            fluidColor = fluid.getColor();
        }

        // vanilla style (checkerboard)
        double diffY = fluidDepth * 0.1D + (data.getBlockX() + data.getBlockZ() & 1) * 0.2D;
        return Colors.shade(fluidColor, diffY < 0.5D ? 0xFF : (diffY > 0.9D ? 0xB4 : 0xDC));
    }

    /**
     * Sample neighbor blocks in configured radius and mix their biome colors.
     *
     * @param data    Block data
     * @param sampler Biome color sampler
     * @return Merged custom color
     */
    protected int sampleNeighbors(@NotNull Chunk.BlockData data, @NotNull BiomeSampler sampler) {
        // get color of starting block
        int color = sampler.sample(data.getBiome(), data.getBlockX(), data.getBlockZ());

        // check if we should blend with neighbors
        int apothem = getBiomeBlend();
        if (apothem < 1) {
            return color;
        }

        // add starting block color
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int c = 1; // 1 for the starting block

        // scan the neighbors
        for (int x2 = data.getBlockX() - apothem; x2 < data.getBlockX() + apothem; x2++) {
            for (int z2 = data.getBlockZ() - apothem; z2 < data.getBlockZ() + apothem; z2++) {
                // don't re-scan the starting block
                if (x2 == data.getBlockX() && z2 == data.getBlockZ()) {
                    continue;
                }

                // neighbor might be in a different chunk so we call World#getChunkFast
                // so it can drill down into the correct region and chunk
                // (hopefully the one we're in, for speed)
                Chunk.BlockData data2 = data.getWorld()
                    .getChunkFast(data.getChunk(), x2 >> 4, z2 >> 4)
                    .getData(x2, z2);
                if (data2 == null) {
                    // chunk doesn't exist, so there's no data; skip
                    continue;
                }

                // add the neighbor block's biome adjusted color
                int color2 = sampler.sample(data2.getBiome(), x2, z2);
                if (color2 != 0) {
                    r += color2 >> 16 & 0xFF;
                    g += color2 >> 8 & 0xFF;
                    b += color2 & 0xFF;
                    c++;
                }
            }
        }
        // average the colors
        return ((r / c) << 16) | ((g / c) << 8) | (b / c);
    }

    /**
     * Represents a biome color sampler for coordinates.
     */
    @FunctionalInterface
    public interface BiomeSampler {
        /**
         * Sample the specified biome at the specified coordinates.
         *
         * @param biome Biome to check
         * @param x     X coordinate
         * @param z     Z coordinate
         * @return Color sampled
         */
        int sample(@NotNull Biome biome, int x, int z);
    }
}
