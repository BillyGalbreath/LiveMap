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

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A basic renderer.
 */
public class FlowerMapRenderer extends Renderer {
    private final Object2IntMap<String> colorMap = new Object2IntArrayMap<>();

    /**
     * Constructs a new instance of FlowerMapRenderer.
     *
     * @param map Renderer properties
     */
    public FlowerMapRenderer(@NotNull Map<String, Object> map) {
        super(FLOWERMAP, map);
        this.colorMap.put("minecraft:dandelion", 0xFFFF00);
        this.colorMap.put("minecraft:poppy", 0xFF0000);
        this.colorMap.put("minecraft:allium", 0x9900FF);
        this.colorMap.put("minecraft:azure_bluet", 0xFFFDDD);
        this.colorMap.put("minecraft:red_tulip", 0xFF4D62);
        this.colorMap.put("minecraft:orange_tulip", 0xFFB55A);
        this.colorMap.put("minecraft:white_tulip", 0xDDFFFF);
        this.colorMap.put("minecraft:pink_tulip", 0xF5B4FF);
        this.colorMap.put("minecraft:oxeye_daisy", 0xFFEEDD);
        this.colorMap.put("minecraft:cornflower", 0x4100FF);
        this.colorMap.put("minecraft:lily_of_the_valley", 0xFFFFFF);
        this.colorMap.put("minecraft:blue_orchid", 0x00BFFF);
        this.colorMap.put("minecraft:pink_petals", 0xFF41BF);
        this.colorMap.put("minecraft:closed_eyeblossom", 0x7F3F00);
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        int pixelColor = 0;

        if (data.getBlock().getColor() != 0) {
            pixelColor = 0x7F7F7F;

            Block flower = data.getWorld().getFlowerMap().getColor(data.getBiome(), data.getBlockX(), data.getBlockY(), data.getBlockZ());
            if (flower != null) {
                pixelColor = (0xFF << 24) | (this.colorMap.getOrDefault(flower.getId(), pixelColor) & 0xFFFFFF);
            }

            // calculate heightmap
            int heightmap;
            if (data.getFluid() == null) {
                // dry land
                heightmap = tile.getHeightmap().getAlpha(tile, data, rand);
            } else {
                // fluids get flat surface
                heightmap = tile.getHeightmap().getMid();
            }

            // apply heightmap
            pixelColor = Colors.shade(pixelColor, 0xFF - heightmap);

            // blend water color on top of land (if any is there)
            pixelColor = processFluidColor(tile, data, pixelColor);
        }

        // store pixel data on tile
        tile.setPixel(data.getBlockX(), data.getBlockZ(), pixelColor);
    }
}
