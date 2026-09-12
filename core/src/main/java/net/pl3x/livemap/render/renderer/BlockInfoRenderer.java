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
import net.pl3x.livemap.render.image.BlockInfoCanvas;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.render.image.io.BlockInfo;
import net.pl3x.livemap.util.ByteUtil;
import net.pl3x.livemap.util.Type;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.biome.Biome;
import net.pl3x.livemap.world.block.Block;
import net.pl3x.livemap.world.chunk.Chunk;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;

/**
 * A basic vanilla colored map renderer.
 */
public class BlockInfoRenderer extends Renderer {
    /**
     * Constructs a new instance of BlockInfoRenderer.
     *
     * @param id                Unique id (per world)
     * @param name              Display name for renderer
     * @param icon              Icon file for webmap
     * @param heightmap         The heightmap type to use
     * @param biomeBlend        Number of blocks to blend biome tints
     * @param translucentFluids True to render fluids as translucent
     * @param sprinkles         True to "sprinkle" random color variations into image
     */
    public BlockInfoRenderer(
        @NotNull String id,
        @NotNull String name,
        @NotNull String icon,
        @NotNull Type<Heightmap> heightmap,
        int biomeBlend,
        boolean translucentFluids,
        boolean sprinkles
    ) {
        super(BLOCKINFO, id, name, icon, heightmap, biomeBlend, translucentFluids, sprinkles);
    }

    @Override
    @NotNull
    public TileCanvas createTileCanvas(@NotNull Region region) {
        return new BlockInfoCanvas(region, this);
    }

    @Override
    protected void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        int topY = data.getTopY() - tile.getWorld().getMinY();

        Block block = data.getTopState().getBlock();
        Biome biome = data.getBiome();

        long blockIndex = block.getIndex() == -1 ? Block.AIR.getIndex() : block.getIndex();
        long biomeIndex = biome.getIndex() == -1 ? Biome.DEFAULT.getIndex() : biome.getIndex();

        // 11111111 11111111 11111111 11111111 11111111 11111111 11111111 11111111 - 64 bits -        (18446744073709551615)
        // 11111111 11111111                                                       - 16 bits - unused (65535)
        //                   11111111 11111111                                     - 16 bits - block  (65535)
        //                                     11111111 11111111                   - 16 bits - biome  (65535)
        //                                                       11111111 11111111 - 16 bits - yPos   (65535)
        long packed = ((blockIndex & 65535) << 32) | ((biomeIndex & 65535) << 16) | (topY & 65535);

        int index = ((data.getBlockZ() & 511) << 9) | (data.getBlockX() & 511);
        int offset = BlockInfo.HEADER_SIZE + index * Long.BYTES;

        Unsafe.<BlockInfoCanvas>cast(tile).setBytes(offset, ByteUtil.toBytes(packed));
    }
}
