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
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.Type;
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
    protected void preRender(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        tile.getHeightmap().postRender(tile, rand, cancelled);
    }

    @Override
    protected void postRender(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        tile.getHeightmap().postRender(tile, rand, cancelled);
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        int pixelColor = 0;

        // get true block color, unless an opaque fluid is covering it
        if (data.getFluidState() == null || tile.getRenderer().isTranslucentFluids()) {
            // either no fluid, or fluids are translucent. either way, we have to draw land
            pixelColor = processBlockColor(data);
        }

        // blend water color on top of land (if any is there)
        pixelColor = processFluidColor(tile, data, pixelColor);

        // verify we have something to render, again
        if (pixelColor != 0) {
            // sprinkle the color so it looks less plain (idea from vintage story map)
            boolean greenery = data.getTopState().getBlock().hasFlag(Block.FLAG_GRASS | Block.FLAG_FOLIAGE);
            pixelColor = Colors.sprinkle(pixelColor, greenery ? 24 : 10);

            // since we have something to render lets calculate heightmap here, too
            tile.getHeightmap().renderBlock(tile, data, rand);
        }

        // store pixel data on tile
        tile.setPixel(data.getBlockX(), data.getBlockZ(), pixelColor);
    }
}
