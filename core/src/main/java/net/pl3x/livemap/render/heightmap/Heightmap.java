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

package net.pl3x.livemap.render.heightmap;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a heightmap.
 */
public abstract class Heightmap {
    private final Type type;

    /**
     * Constructs a new instance of Heightmap.
     *
     * @param type The type of heightmap
     */
    public Heightmap(@NotNull Type type) {
        this.type = type;
    }

    /**
     * Get type of heightmap.
     *
     * @return Heightmap's type
     */
    @NotNull
    public Type getType() {
        return this.type;
    }

    /**
     * Represents a type of heightmap.
     *
     * @param id    Unique id for type
     * @param clazz Heightmap class this type represents
     */
    public record Type(@NotNull String id, @NotNull Class<? extends Heightmap> clazz) {
        private static final Map<String, Type> BY_NAME = new HashMap<>();

        @NotNull
        private static Type register(@NotNull String name, @NotNull Class<? extends Heightmap> clazz) {
            Type type = new Type(name, clazz);
            BY_NAME.put(name.toLowerCase(Locale.ROOT), type);
            return type;
        }

        public static final Type BASIC = register("basic", BasicHeightmap.class);
        public static final Type FANCY = register("fancy", FancyHeightmap.class);

        /**
         * Get heightmap type instance by name.
         *
         * @param name Name of heightmap type
         * @return Requested heightmap type
         */
        @Nullable
        public static Type get(@NotNull String name) {
            return BY_NAME.get(name.toLowerCase(Locale.ROOT));
        }

        /**
         * Create a new heightmap of this type.
         *
         * @return A new heightmap
         */
        @NotNull
        public Heightmap create() {
            try {
                return clazz().getConstructor().newInstance();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
