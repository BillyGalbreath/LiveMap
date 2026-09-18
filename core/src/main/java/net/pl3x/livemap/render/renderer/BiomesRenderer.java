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

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.pl3x.livemap.configuration.ColorsConfig;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A basic renderer.
 */
public class BiomesRenderer extends Renderer {
    /**
     * Constructs a new instance of BiomesRenderer.
     *
     * @param map Renderer properties
     */
    public BiomesRenderer(@NotNull Map<String, Object> map) {
        super(BIOMES, map);
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        int pixelColor = 0;

        // check if anything is even there to render (we ignore transparent black)
        if (data.getTopState().getColor() != 0) {
            Biome biome = data.getBiome();
            pixelColor = 0xFF000000 | ColorsConfig.BIOME_COLORS.getOrDefault(biome.getId(), 0);

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
        }

        // store pixel data on tile
        tile.setPixel(data.getBlockX(), data.getBlockZ(), pixelColor);
    }
}
