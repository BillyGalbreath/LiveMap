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
import java.nio.file.Path;
import net.pl3x.livemap.render.image.Tile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * IO utils for Joint Photographic Experts Group (JPEG) images.
 */
public class Jpeg extends IO.Type {
    Jpeg() {
        super("jpg");
    }

    @Override
    public @NotNull BufferedImage createBuffer() {
        // jpg does not support transparency
        return new BufferedImage(Tile.SIZE, Tile.SIZE, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public int color(int argb) {
        // jpg does not support transparency
        return argb & 0xFFFFFF;
    }

    @Override
    @Nullable
    public BufferedImage read(@NotNull Path path) {
        return null;
    }

    @Override
    public void write(@NotNull Path path, @NotNull BufferedImage buffer) {
    }
}
