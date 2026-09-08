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
import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.Type;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A basic vanilla colored map renderer.
 */
public class BasicRenderer extends Renderer {
    /**
     * Constructs a new instance of BasicRenderer.
     *
     * @param name              Display name for renderer
     * @param icon              Icon file for webmap
     * @param heightmap         The heightmap type to use
     * @param biomeBlend        Number of blocks to blend biome tints
     * @param translucentFluids True to render fluids as translucent
     */
    public BasicRenderer(@NotNull String name, @NotNull String icon, @NotNull Type<Heightmap> heightmap, int biomeBlend, boolean translucentFluids) {
        super(BASIC, name, icon, heightmap, biomeBlend, translucentFluids);
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        // get vanilla style color
        int pixelColor = data.getTopState().getBlock().getVanilla();

        // check if anything is even there to render (we ignore transparent black)
        if (pixelColor != 0) {
            // calculate heightmap
            int heightmap;
            if (data.getFluid() == null) {
                // dry land
                heightmap = tile.getHeightmap().getAlpha(data.getChunk(), data.getBlockX(), data.getBlockZ());
            } else {
                // fluids get flat surface since opaque
                heightmap = tile.getHeightmap().getMid();

                // but let's alter for vanilla style heightmap (checkerboard)
                int fluidDepth = data.getFluidY() - data.getBlockY();
                double diffY = fluidDepth * 0.1D + (data.getBlockX() + data.getBlockZ() & 1) * 0.2D;
                pixelColor = Colors.shade(pixelColor, diffY < 0.5D ? 0xFF : (diffY > 0.9D ? 0xB4 : 0xDC));
            }

            // apply heightmap
            pixelColor = Colors.shade(pixelColor, 0xFF - heightmap);
        }

        // store pixel data on tile
        tile.setPixel(data.getBlockX(), data.getBlockZ(), pixelColor);
    }
}
