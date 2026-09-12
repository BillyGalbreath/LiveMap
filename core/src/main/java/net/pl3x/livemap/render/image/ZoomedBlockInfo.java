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
import net.pl3x.livemap.render.image.io.IO;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a buffer containing blockinfo data from multiple regions at higher zoom levels.
 */
public class ZoomedBlockInfo extends ZoomedCanvas {
    /**
     * Constructs a new instance of ZoomedBlockInfo.
     *
     * @param imageBuffer The image buffer
     * @param zoom        This zoom level
     */
    public ZoomedBlockInfo(@NotNull BufferedImage imageBuffer, int zoom) {
        super(imageBuffer, zoom);
    }

    @Override
    @NotNull
    protected IO.Type getIO() {
        return IO.getType("blockinfo");
    }
}
