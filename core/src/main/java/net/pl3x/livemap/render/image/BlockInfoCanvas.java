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

package net.pl3x.livemap.render.image;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import net.pl3x.livemap.render.image.io.BlockInfo;
import net.pl3x.livemap.render.image.io.IO;
import net.pl3x.livemap.render.renderer.Renderer;
import net.pl3x.livemap.util.ByteUtil;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a TileCanvas to hold BlockInfo specific data instead of images.
 */
public class BlockInfoCanvas extends TileCanvas {
    private final ByteBuffer byteBuffer;

    /**
     * Constructs a new instance of BlockInfoCanvas.
     *
     * @param region   Region this blockinfo belongs to
     * @param renderer The renderer populating this blockinfo
     */
    public BlockInfoCanvas(@NotNull Region region, @NotNull Renderer renderer) {
        super(region, renderer, IO.getType("blockinfo"));

        // allocate bytebuffer
        this.byteBuffer = ByteBuffer.allocate(512 * 512 * Long.BYTES + BlockInfo.HEADER_SIZE);

        // 16 bit header
        int y = region.getWorld().getMinY();
        this.byteBuffer.put(0, ByteUtil.toBytes(0x6C6976656D61705FL)); // livemap_
        this.byteBuffer.put(8, ByteUtil.toBytes(0x7634723031000000L | (y & 0xFFFFFF))); // v4r01
    }

    /**
     * Set bytes to the internal ByteBuffer at specified index.
     *
     * @param offset Offset to place bytes at
     * @param bytes  The bytes to place
     */
    public void setBytes(int offset, byte[] bytes) {
        this.byteBuffer.put(offset, bytes);
        this.dirty = true;
    }

    @Override
    @NotNull
    protected ZoomedCanvas createZoomedCanvas(int zoom) {
        return new ZoomedBlockInfo(getIO().createBuffer(), zoom);
    }

    @Override
    protected void writePixels(@NotNull BufferedImage buffer, int zoom) {
        byte[] bytes = Unsafe.<BlockInfo.CustomBufferedImage>cast(buffer).getBytes();

        if (zoom == 0) {
            byte[] src = this.byteBuffer.array();
            System.arraycopy(src, 0, bytes, 0, src.length);
            return;
        }

        // how many pixels to increment in each direction
        int step = 1 << zoom;

        // calculate where in the buffer do we start writing pixels.
        // zoom level increments the number of regions in a single tile,
        // so we want to ensure we are only writing in this tile region's
        // section of the buffer
        int baseX = (getRegion().getX() * (512 >> zoom)) & 511;
        int baseZ = (getRegion().getZ() * (512 >> zoom)) & 511;

        // walk the pixels
        for (int z = 0; z < 512; z += step) {
            int targetZ = baseZ + (z >> zoom);
            int targetRowOffset = targetZ * 512;
            for (int x = 0; x < 512; x += step) {
                int targetX = baseX + (x >> zoom);

                int srcIndex = ((z << 9) | x) * Long.BYTES + BlockInfo.HEADER_SIZE;
                int destIndex = targetRowOffset + targetX;

                this.byteBuffer.get(srcIndex, bytes, destIndex, Long.BYTES);
            }
        }
    }
}
