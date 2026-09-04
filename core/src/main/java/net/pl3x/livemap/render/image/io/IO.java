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
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * IO utils for PNG images.
 */
public final class IO extends Registry<IO.Type> {
    private static final IO INSTANCE = new IO();

    static {
        INSTANCE.rebuild();
    }

    /**
     * Get io type by id (aka, file extension).
     *
     * @param id Type id
     * @return Type of id
     * @throws IllegalStateException if type with id does not exist
     */
    @NotNull
    public static Type getType(@NotNull String id) {
        Type type = INSTANCE.get(id);
        if (type == null) {
            throw new IllegalStateException("Unknown or unsupported image format");
        }
        return type;
    }

    private IO() {
    }

    @Override
    public void rebuild() {
        clear();

        put("bmp", new Bmp());
        put("gif", new Gif());
        put("jpg", new Jpeg());
        put("jpeg", get("jpg"));
        put("png", new Png());
        put("webp", new WebP());
    }

    /**
     * IO utils for specific image type.
     */
    public abstract static class Type {
        private final String extension;

        Type(@NotNull String extension) {
            this.extension = extension;
            ImageIO.setUseCache(false);
        }

        /**
         * Create a new blank image buffer.
         *
         * @return Image buffer
         */
        @NotNull
        public BufferedImage createBuffer() {
            return new BufferedImage(TileCanvas.SIZE, TileCanvas.SIZE, BufferedImage.TYPE_INT_ARGB);
        }

        /**
         * Get the file extension for this image type.
         *
         * @return File extension
         */
        @NotNull
        public String getExtension() {
            return this.extension;
        }

        /**
         * Convert color for this image type.
         *
         * @param argb Color
         * @return Converted color
         */
        public int color(int argb) {
            return argb;
        }

        /**
         * Get the image's quality level as a percentage (0.0 - 1.0).
         *
         * @return Image quality
         */
        public float getTileQuality() {
            return 0.0F;
        }

        @NotNull
        ImageReader getReader() {
            return ImageIO.getImageReadersBySuffix(this.extension).next();
        }

        @NotNull
        ImageInputStream imageInputStream(@NotNull Path path) throws IOException {
            return ImageIO.createImageInputStream(path.toFile());
        }

        @NotNull
        ImageWriter getWriter() {
            return ImageIO.getImageWritersBySuffix(this.extension).next();
        }

        @NotNull
        ImageOutputStream imageOutputStream(@NotNull Path path) throws IOException {
            return ImageIO.createImageOutputStream(path.toFile());
        }

        /**
         * Read image from disk.
         *
         * @param path Path to image file
         * @return Buffered image
         */
        @Nullable
        public abstract BufferedImage read(@NotNull Path path);

        /**
         * Write image to disk.
         *
         * @param path   Path to image file
         * @param buffer Image to write to file
         */
        public abstract void write(@NotNull Path path, @NotNull BufferedImage buffer);
    }
}
