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

package net.pl3x.livemap.world.chunk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import net.jpountz.lz4.LZ4Factory;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a compression type for in/output streams.
 */
public enum CompressionType {
    /**
     * No compression type.
     */
    NONE(out -> out, in -> in),
    /**
     * Gzip compression type.
     */
    GZIP(GZIPOutputStream::new, GZIPInputStream::new),
    /**
     * Zip compression type.
     */
    ZIP(DeflaterOutputStream::new, InflaterInputStream::new),
    /**
     * LZ4 compression type.
     */
    LZ4(LZ4BlockOutputStream::new,
        // le sigh. rip constructor reference.
        // all this to avoid the deprecated ctor that does this same thing...
        in -> LZ4BlockInputStream.newBuilder()
            .withDecompressor(LZ4Factory.fastestInstance().fastDecompressor())
            .build(in)
    );

    private final StreamTransformer<OutputStream> compressor;
    private final StreamTransformer<InputStream> decompressor;

    CompressionType(@NotNull StreamTransformer<OutputStream> compressor, @NotNull StreamTransformer<InputStream> decompressor) {
        this.compressor = compressor;
        this.decompressor = decompressor;
    }

    /**
     * Compress output stream.
     *
     * @param out Output stream to compress
     * @return Compressed output stream
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public OutputStream compress(@NotNull OutputStream out) throws IOException {
        return compressor.apply(out);
    }

    /**
     * Decompress input stream.
     *
     * @param in Input stream to decompress
     * @return Decompressed input stream
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    public InputStream decompress(@NotNull InputStream in) throws IOException {
        return decompressor.apply(in);
    }

    @FunctionalInterface
    private interface StreamTransformer<T> {
        @NotNull
        T apply(@NotNull T original) throws IOException;
    }
}
