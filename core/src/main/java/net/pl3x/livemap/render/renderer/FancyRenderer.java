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

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.Image;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.MapBlurUtil;
import net.pl3x.livemap.util.Mathf;
import net.pl3x.livemap.util.Type;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A fancy custom colored map renderer.
 */
public class FancyRenderer extends Renderer {
    /**
     * Constructs a new instance of FancyRenderer.
     *
     * @param name              Display name for renderer
     * @param icon              Icon file for webmap
     * @param heightmap         The heightmap type to use
     * @param biomeBlend        Number of blocks to blend biome tints
     * @param translucentFluids True to render fluids as translucent
     */
    public FancyRenderer(@NotNull String name, @NotNull String icon, @NotNull Type<Heightmap> heightmap, int biomeBlend, boolean translucentFluids) {
        super(FANCY, name, icon, heightmap, biomeBlend, translucentFluids);
    }

    @Override
    public boolean renderRegion(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        // setup shadowmap stuff (for heightmap)
        byte[] heightmap = new byte[512 << 9];
        Arrays.fill(heightmap, (byte) 0);

        // copy logic from super.renderRegion()
        int chunkStartX = tile.getRegion().getX() << 5;
        int chunkStartZ = tile.getRegion().getZ() << 5;

        // iterate each chunk in this region
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

                        // handle heightmap
                        float yDiff;
                        if (data.getFluidState() == null) {
                            // calculate actual heightmap if we're not in water
                            yDiff = CalculateAltitudeDiff(chunk, blockX, blockZ, data.getBlockY());
                        } else if (data.getFluidY() - data.getBlockY() <= 5) {
                            // calculate heightmap and taper off the deeper we go in shallow water
                            yDiff = CalculateAltitudeDiff(chunk, blockX, blockZ, data.getBlockY()) * 0.25F + 0.75F;
                        } else {
                            // water too deep so see heightmap detail, just use flat surface
                            yDiff = 1F;
                        }

                        // add shadowmap stuff here
                        heightmap[Image.getIndex(blockX, blockZ)] = (byte) (128 * yDiff - 127);
                    }
                }
            }
        }

        //
        byte[] copy = heightmap.clone();
        MapBlurUtil.blur(heightmap);

        // apply the shadowmap to the tile
        for (int index = 0; index < heightmap.length; index++) {
            int color = tile.getPixel(index);
            if (color != 0) {
                float shade = ((((int) ((heightmap[index] - 1) / 25.6F)) / 5F)
                    + ((((copy[index] - 1) / 25.6F) % 1) / 5F))
                    * 1.2F + 1F;
                tile.setPixel(index & 511, index >> 9, Colors.mul(color & 0xFFFFFF, shade) | 0xFF000000);
            }
        }

        return true;
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        int pixelColor = 0;

        // get true block color, unless an opaque fluid is covering it
        if (data.getFluidState() == null || tile.getRenderer().isTranslucentFluids()) {
            // either no fluid, or fluids are translucent. either way, we have to draw land
            pixelColor = processBlockColor(data);
        }

        /* heightmap handled differently - keeping this code for archive reasons (for now)
        // check if anything is even there to render (we ignore transparent black)
        if (pixelColor != 0) {
            // calculate heightmap
            int heightmap;
            if (data.getFluidState() == null || data.getFluidY() - data.getBlockY() <= 5) {
                // only calculate actual heightmap if we're in shallow enough water (or no water)
                heightmap = getHeightmap().getAlpha(data.getChunk(), data.getBlockX(), data.getBlockZ());
            } else {
                // water too deep so see heightmap detail, just use flat surface
                heightmap = getHeightmap().getMid();
            }

            // apply heightmap
            pixelColor = Colors.shade(pixelColor, 0xFF - heightmap * 2);
        }*/

        // blend water color on top of land (if any is there)
        pixelColor = processFluidColor(tile, data, pixelColor);

        // verify we have something to render, again
        if (pixelColor != 0) {
            // sprinkle the color so it looks less plain (idea from vintage story map)
            boolean greenery = data.getTopState().getBlock().hasFlag(Block.FLAG_GRASS | Block.FLAG_FOLIAGE);
            pixelColor = Colors.sprinkle(pixelColor, greenery ? 24 : 10);
        }

        // store pixel data on tile
        tile.setPixel(data.getBlockX(), data.getBlockZ(), pixelColor);
    }

    private int processBlockColor(@NotNull Chunk.BlockData data) {
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
            float depthMod = fluidDepth * 0.025F;
            if (fluid.hasFlag(Block.FLAG_WATER)) {
                // translucent water
                fluidColor = sampleNeighbors(data, (biome, _, _) -> biome.getWater());
                // make color lighter in shallower depths
                fluidColor = Colors.lerpARGB(fluidColor, 0xFF000000, Math.clamp(Mathf.easeCubicOut(depthMod / 1.5F), 0, 0.45F));
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
     * Sample neighbor blocks in configured radius and mix their colors.
     *
     * @param data    Block data
     * @param sampler Biome color sampler
     * @return Merged color
     */
    protected int sampleNeighbors(@NotNull Chunk.BlockData data, @NotNull Sampler sampler) {
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

    private float CalculateAltitudeDiff(@NotNull Chunk chunk, int blockX, int blockZ, int blockY) {
        Chunk.BlockData northwest = chunk.getWorld().getChunkFast(chunk, (blockX - 1) >> 4, (blockZ - 1) >> 4).getData(blockX - 1, blockZ - 1);
        Chunk.BlockData northeast = chunk.getWorld().getChunkFast(chunk, blockX >> 4, (blockZ - 1) >> 4).getData(blockX, blockZ - 1);
        Chunk.BlockData southwest = chunk.getWorld().getChunkFast(chunk, (blockX - 1) >> 4, blockZ >> 4).getData(blockX - 1, blockZ);

        int leftTop = blockY - (northwest == null ? blockY : northwest.getBlockY());
        int rightTop = blockY - (northeast == null ? blockY : northeast.getBlockY());
        int leftBot = blockY - (southwest == null ? blockY : southwest.getBlockY());

        int direction = Integer.signum(leftTop) + Integer.signum(rightTop) + Integer.signum(leftBot);
        int steepness = Math.max(Math.max(Math.abs(leftTop), Math.abs(rightTop)), Math.abs(leftBot));
        float slopeFactor = Math.min(0.5F, steepness / 10F) / 1.25F;

        if (direction > 0) {
            return 1.08F + slopeFactor;
        }
        if (direction < 0) {
            return 0.92F - slopeFactor;
        }
        return 1;
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
        int sample(@NotNull Biome biome, int x, int z);
    }
}
