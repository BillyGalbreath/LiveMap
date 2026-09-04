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
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * IO utils for Portable Network Graphics (PNG) images.
 */
public final class Png extends IO.Type {
    /**
     * The native image metadata format for PNG (Portable Network Graphics) images within the Java Image I/O API.
     *
     * @see <a href="https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/imageio/metadata/doc-files/png_metadata.html">PNG Metadata Format Specification</a>
     */
    public static final String METADATA_FORMAT = "javax_imageio_png_1.0";

    Png() {
        super("png");
    }

    @Override
    @Nullable
    public BufferedImage read(@NotNull Path path) {
        BufferedImage buffer = null;
        ImageReader reader = null;
        try (ImageInputStream in = ImageIO.createImageInputStream(Files.newInputStream(path))) {
            reader = ImageIO.getImageReadersBySuffix(getExtension()).next();
            reader.setInput(in, false, true);
            buffer = reader.read(0);
            in.flush();
        } catch (IOException e) {
            Logger.warn("Could not read tile image: " + path, e);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
        return buffer;
    }

    @Override
    public void write(@NotNull Path path, @NotNull BufferedImage buffer) {
        Path tmp = FileUtil.tmp(path);
        ImageWriter writer = null;
        try (ImageOutputStream out = ImageIO.createImageOutputStream(tmp.toFile())) {
            writer = ImageIO.getImageWritersBySuffix(getExtension()).next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                if (param.getCompressionType() == null) {
                    param.setCompressionType(param.getCompressionTypes()[0]);
                }
                param.setCompressionQuality(0.0F);
            }
            writer.setOutput(out);
            writer.write(null, new IIOImage(buffer, null, null), param);
            out.flush();
        } catch (IOException e) {
            Logger.warn("Could not write tile image: " + tmp, e);
        } finally {
            if (writer != null) {
                writer.dispose();
            }
        }
        try {
            FileUtil.atomicMove(tmp, path);
        } catch (IOException e) {
            Logger.warn("Could not write tile image: " + path, e);
        }
    }
}
