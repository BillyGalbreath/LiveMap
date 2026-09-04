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

import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A basic renderer.
 */
public class BasicRenderer extends Renderer {
    /**
     * Constructs a new instance of BasicRenderer.
     *
     * @param name              Display name for renderer
     * @param icon              Icon file for webmap
     * @param heightmap         The heightmap to use
     * @param biomeBlend        Number of blocks to blend biome tints
     * @param translucentFluids True to render fluids as translucent
     */
    public BasicRenderer(@NotNull String name, @NotNull String icon, @Nullable Heightmap heightmap, int biomeBlend, boolean translucentFluids) {
        super(Type.BASIC, name, icon, heightmap, biomeBlend, translucentFluids);
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data) {
        int pixelColor = data.getTopState().getBlock().getVanilla();
        int amount;
        if (data.getFluidState() == null) {
            boolean greenery = data.getBlock().hasFlag(Block.FLAG_GRASS | Block.FLAG_FOLIAGE);
            amount = greenery ? 50 : 20;
        } else {
            amount = 15;
        }
        tile.setPixel(data.getBlockX(), data.getBlockZ(), pixelColor == 0 ? 0 : Colors.sprinkle(pixelColor, amount));
    }
}
