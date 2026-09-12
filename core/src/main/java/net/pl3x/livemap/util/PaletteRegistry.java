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

package net.pl3x.livemap.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a palette registry of indexable objects.
 *
 * @param <T> Type of indexable object
 */
public abstract class PaletteRegistry<T extends Indexed> extends Registry<T> {
    protected static final int MAX_INDEX = 0xFFFF; // 65535
    protected static final Gson GSON = new GsonBuilder().create();

    private Object2IntOpenHashMap<String> palette = new Object2IntOpenHashMap<>();
    private int lastIndex = 0;

    /**
     * Load the palette from disk.
     *
     * @param path Path to file
     */
    protected void loadPalette(@NotNull Path path) {
        this.palette.clear();

        if (!Files.exists(path)) {
            return;
        }
        try {
            Int2ObjectOpenHashMap<String> data = GSON.fromJson(FileUtil.readGzip(path), new TypeToken<>() {
            });
            this.palette = new Object2IntOpenHashMap<>(data.size());
            this.palette.defaultReturnValue(-1);
            for (Int2ObjectMap.Entry<String> entry : data.int2ObjectEntrySet()) {
                this.palette.put(entry.getValue(), entry.getIntKey());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save the palette to disk.
     *
     * @param path Path to file
     */
    protected void savePalette(@NotNull Path path) {
        Int2ObjectOpenHashMap<String> indexMap = new Int2ObjectOpenHashMap<>();
        forEach((id, indexedObj) -> indexMap.put(indexedObj.getIndex(), id));
        try {
            FileUtil.saveGzip(path, GSON.toJson(indexMap));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the next available index number.
     *
     * @param id Object id, used for tracking index numbers
     * @return Next index number
     */
    protected int getNextIndex(@NotNull String id) {
        if (size() > MAX_INDEX) {
            return -1;
        }

        int index = this.palette.getOrDefault(id, -1);
        if (index > -1) {
            return index;
        }

        while (true) {
            if (!this.palette.containsValue(this.lastIndex)) {
                this.palette.put(id, this.lastIndex);
                return this.lastIndex;
            }
            this.lastIndex++;
        }
    }
}
