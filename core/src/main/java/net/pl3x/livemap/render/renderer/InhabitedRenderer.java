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
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A basic renderer.
 */
public class InhabitedRenderer extends Renderer {
    /**
     * Constructs a new instance of InhabitedRenderer.
     *
     * @param map Renderer properties
     */
    public InhabitedRenderer(@NotNull Map<String, Object> map) {
        super(INHABITED, map);
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        int pixelColor = 0;

        // get true block color, unless an opaque fluid is covering it
        if (!data.getTopState().isFluid() || tile.getRenderer().isTranslucentFluids()) {
            // either no fluid, or fluids are translucent. either way, we have to draw land
            pixelColor = processBlockColor(data);
        }

        // blend water color on top of land (if any is there)
        pixelColor = processFluidColor(tile, data, pixelColor);

        // verify we have something to render, again
        if (pixelColor != 0) {
            // since we have something to render lets calculate heightmap
            tile.getHeightmap().renderBlock(tile, data, rand);
        }

        // we hsb lerp between blue and red with ratio being the
        // percent inhabited time is of the maxed out inhabited time
        float ratio = Math.clamp(data.getChunk().getInhabitedTime() / 3600000F, 0F, 1F);
        int inhabitedRGB = Colors.lerpHSB(0x880000FF, 0x88FF0000, ratio, false);

        // set the color, mixing our heatmap on top
        // set a low enough alpha, so we can see the basic map underneath
        pixelColor = Colors.blend(inhabitedRGB, 0xFF000000 | pixelColor);

        // store pixel data on tile
        tile.setPixel(data.getBlockX(), data.getBlockZ(), pixelColor);
    }
}
