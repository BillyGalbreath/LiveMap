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

package net.pl3x.livemap.render.image.io;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.util.FileUtil;
import net.pl3x.livemap.util.Unsafe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * IO utils for BlockInfo data.
 */
public class BlockInfo extends IO.Type {
    public static final int HEADER_SIZE = 16;

    BlockInfo() {
        super("livemap.gz");
    }

    @Override
    @NotNull
    public BufferedImage createBuffer() {
        return new CustomBufferedImage();
    }

    @Override
    public int colorType() {
        return BufferedImage.TYPE_CUSTOM;
    }

    @Override
    public @Nullable BufferedImage read(@NotNull Path path) {
        return null;
    }

    @Override
    public void write(@NotNull Path path, @NotNull BufferedImage buffer) {
        try {
            FileUtil.saveGzip(path, Unsafe.<CustomBufferedImage>cast(buffer).getBytes());
        } catch (Throwable t) {
            Logger.error("Failed to write blockinfo: " + path, t);
        }
    }

    /**
     * This is a fake BufferedImage that we can use to pass our ByteBuffer to the Renderers through the TileCanvas.
     */
    public static class CustomBufferedImage extends BufferedImage {
        private final ByteBuffer buffer;

        /**
         * Constructs a new CustomBufferedImage.
         */
        public CustomBufferedImage() {
            super(512, 512, BufferedImage.TYPE_BYTE_BINARY);
            this.buffer = ByteBuffer.allocate(512 * 512 * Long.BYTES + 16);
        }

        /**
         * Get underlying byte array.
         *
         * @return Byte array
         */
        public byte[] getBytes() {
            return this.buffer.array();
        }
    }
}
