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
import java.util.concurrent.atomic.AtomicBoolean;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

/**
 * A basic renderer.
 */
public class InhabitedRenderer extends Renderer {
    private static final int UNINHABITED_COLOR = 0x880000FF;
    private static final int INHABITED_COLOR = 0x88FF0000;

    /**
     * Constructs a new instance of InhabitedRenderer.
     *
     * @param map Renderer properties
     */
    public InhabitedRenderer(@NotNull Map<String, Object> map) {
        super(INHABITED, map);
    }

    @Override
    public boolean renderRegion(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled, @NotNull Map<String, TileCanvas> renderedTiles) {
        // preRender(tile, rand, cancelled, renderedTiles); // don't need for this renderer

        // try to get fancy renderer's tile
        TileCanvas base = renderedTiles.get("fancy");
        if (base == null) {
            // try for basic renderer as backup
            base = renderedTiles.get("basic");
        }
        // if we found a renderer, copy it to current tile
        int[] pixels = tile.getPixels();
        if (base != null) {
            System.arraycopy(base.getPixels(), 0, pixels, 0, 512 << 9);
        }

        // ensure tile will get saved to disk
        tile.setDirty(true);

        int chunkStartX = tile.getRegion().getX() << 5;
        int chunkStartZ = tile.getRegion().getZ() << 5;

        for (int cz = 0; cz < 32; cz++) {
            // check world state and interruptions, for instant responsiveness
            if (tile.getWorld().isDiscarded() || cancelled.get()) {
                return false; // aborted
            }

            int chunkZ = chunkStartZ + cz;
            int localZBase = cz << 4; // 0, 16, 32... 496

            for (int cx = 0; cx < 32; cx++) {
                int chunkX = chunkStartX + cx;

                Chunk chunk = tile.getRegion().getChunk(chunkX, chunkZ);
                if (!chunk.isFull()) {
                    continue; // chunk not fully generated
                }

                // no need to pre-scan here since we only care about inhabited time
                // chunk.preScan();

                long inhabitedTime = chunk.getInhabitedTime();
                int overlayColor;
                if (inhabitedTime <= 0) {
                    // constant blue
                    overlayColor = UNINHABITED_COLOR;
                } else {
                    // we hsb lerp between blue and red with ratio being the
                    // percent inhabited time is of the maxed out inhabited time
                    float ratio = Math.min(inhabitedTime / 3600000F, 1F);
                    overlayColor = Colors.lerpHSB(UNINHABITED_COLOR, INHABITED_COLOR, ratio, false);
                }

                int fgA = overlayColor >>> 24;
                int invA = 255 - fgA;
                int fgRB = (overlayColor & 0x00FF00FF) * fgA;
                int fgG = (overlayColor & 0x0000FF00) * fgA;

                int localXBase = cx << 4;

                for (int z = 0; z < 16; z++) {
                    int rowOffset = ((localZBase + z) << 9) + localXBase;
                    for (int x = 0; x < 16; x++) {
                        int idx = rowOffset + x;
                        int bg = pixels[idx];
                        if (bg == 0) {
                            continue; // transparent/empty background
                        }
                        // parallel channel blend
                        int rb = (fgRB + (bg & 0x00FF00FF) * invA) >>> 8 & 0x00FF00FF;
                        int g = (fgG + (bg & 0x0000FF00) * invA) >>> 8 & 0x0000FF00;
                        pixels[idx] = 0xFF000000 | rb | g;
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand, @NotNull Map<String, TileCanvas> renderedTiles) {
    }
}
