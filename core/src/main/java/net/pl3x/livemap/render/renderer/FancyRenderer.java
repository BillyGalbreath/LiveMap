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

import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.Mathf;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A fancy renderer.
 */
public class FancyRenderer extends Renderer {
    /**
     * Constructs a new instance of FancyRenderer.
     *
     * @param name              Display name for renderer
     * @param icon              Icon file for webmap
     * @param heightmap         The heightmap to use
     * @param biomeBlend        Number of blocks to blend biome tints
     * @param translucentFluids True to render fluids as translucent
     */
    public FancyRenderer(@NotNull String name, @NotNull String icon, @Nullable Heightmap heightmap, int biomeBlend, boolean translucentFluids) {
        super(Type.FANCY, name, icon, heightmap, biomeBlend, translucentFluids);
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data) {
        // calculate the base ground color
        int pixelColor = 0;

        // get true block color, unless an opaque fluid is covering it
        if (data.getFluidState() == null || tile.getRenderer().isTranslucentFluids()) {
            pixelColor = processBlockColor(tile, data);

            if (pixelColor == 0) {
                Logger.warn("No color: " + data.getBlock());
            }

            if (pixelColor != 0) {
                pixelColor |= 0xFF000000;

                // heightmap
                // todo

                // sprinkle the color so it looks less plain
                boolean greenery = data.getBlock().hasFlag(Block.FLAG_GRASS | Block.FLAG_FOLIAGE);
                pixelColor = Colors.sprinkle(pixelColor, greenery ? 50 : 20);
            }
        }

        // blend water color on top
        pixelColor = processFluidColor(tile, data, pixelColor);

        // store on tile
        tile.setPixel(data.getBlockX(), data.getBlockZ(), pixelColor);
    }

    private int processBlockColor(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data) {
        int color = data.getBlock().getColor();
        if (color == 0) {
            // nothing to color
            return color;
        }
        // check most popular block types first for efficiency
        if (data.getBlock().hasFlag(Block.FLAG_GRASS)) {
            return data.getBiome().getGrass();
            // return data.getBiome().getGrassModifier().modify(data.getBlockX(), data.getBlockZ(), data.getBiome().getGrass());
            // return sampleNeighbors(tile, data, (biome, x, z) -> biome.getGrassModifier().modify(x, z, biome.getGrass()));
        } else if (data.getBlock().hasFlag(Block.FLAG_FOLIAGE)) {
            return data.getBiome().getFoliage();
            // return sampleNeighbors(tile, data, (biome, x, z) -> biome.getFoliage());
        } else if (data.getBlock().hasFlag(Block.FLAG_DRY_FOLIAGE)) {
            return data.getBiome().getDryFoliage();
            // return sampleNeighbors(tile, data, (biome, x, z) -> biome.getDryFoliage());
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

    private int processFluidColor(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, int pixelColor) {
        Block fluid = data.getFluid();
        if (fluid == null) {
            return pixelColor;
        }

        int fluidColor;
        int fluidDepth = data.getFluidY() - data.getBlockY();

        // get translucent fluid color
        if (tile.getRenderer().isTranslucentFluids()) {
            // translucent style
            // float depthMod = fluidDepth / 60F;
            if (fluid.hasFlag(Block.FLAG_WATER)) {
                // translucent water
                fluidColor = sampleNeighbors(tile, data, (biome, _, _) -> biome.getWater());
                fluidColor = Colors.lerpARGB(fluidColor, 0xFF000000, Math.clamp(Mathf.easeCubicOut(fluidDepth / 1.5F), 0, 0.45F));
                fluidColor = fluidColor | (int) (0xBF + Mathf.easeQuinticOut(Math.clamp(fluidDepth * 5F, 0, 1)) * 0xFF);
            } else {
                // opaque (but shaded) lava
                fluidColor = Colors.lerpARGB(fluid.getColor(), 0xFF000000, Math.clamp(Mathf.easeCubicOut(fluidDepth / 1.5F), 0, 0.3F));
                fluidColor = fluidColor | 0xFF000000;
            }
            return Colors.blend(fluidColor, pixelColor);
        }

        // get solid fluid color
        if (fluid.hasFlag(Block.FLAG_WATER)) {
            fluidColor = sampleNeighbors(tile, data, (biome, _, _) -> biome.getWater());
        } else {
            fluidColor = fluid.getColor();
        }

        // vanilla style (checkerboard)
        double diffY = fluidDepth * 0.1D + (data.getBlockX() + data.getBlockZ() & 1) * 0.2D;
        return Colors.shade(fluidColor, diffY < 0.5D ? 0xFF : (diffY > 0.9D ? 0xB4 : 0xDC));
    }

    /**
     * Sample neighbor blocks in configured radius and mix their colors.
     *
     * @param tile    The tile canvas being drawn on
     * @param data    Block data
     * @param sampler Biome color sampler
     * @return Merged color
     */
    protected int sampleNeighbors(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull Sampler sampler) {
        // get color of starting block
        int color = sampler.sample(data.getBiome(), data.getBlockX(), data.getBlockZ());

        // check if we should blend with neighbors
        int apothem = tile.getRenderer().getBiomeBlend();
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

                // neighbor might be in a different chunk
                Chunk chunk2 = data.getChunk().getRegion().getChunkFast(data.getChunk(), x2 >> 4, z2 >> 4);
                if (!chunk2.isFull()) {
                    // chunk doesn't exist or isn't ready
                    continue;
                }

                // chunk may not have been loaded (meaning no Block.Data scanned) so get biome directly from registry
                Biome biome2 = data.getChunk().getBiome(x2, chunk2.getHeight(x2, z2), z2);

                // add the neighbor block's color
                int color2 = sampler.sample(biome2, x2, z2);
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
    public interface Sampler {
        /**
         * Sample the specified biome and the specified coordinates.
         *
         * @param biome Biome to check
         * @param x     X coordinate
         * @param z     Z coordinate
         * @return Color sampled
         */
        @NotNull
        Integer sample(@NotNull Biome biome, @NotNull Integer x, @NotNull Integer z);
    }
}
