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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.pl3x.livemap.render.heightmap.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a type of object.
 *
 * @param <T> The type of object that this type object represents
 */
public class Type<T> {
    private static final Map<Class<?>, Map<String, Type<?>>> BY_CLASS = new HashMap<>();

    /**
     * Register a new type.
     *
     * @param type Object type to register
     * @param <T>  The type of object this object type represents
     * @return The registered object type
     * @throws IllegalStateException If the type ID is already registered
     */
    @NotNull
    public static <T> Type<T> register(@NotNull Type<T> type) {
        Map<String, Type<?>> innerMap = BY_CLASS.computeIfAbsent(type.clazz.getSuperclass(), _ -> new HashMap<>());
        Type<?> existing = innerMap.putIfAbsent(type.id, type);
        if (existing != null) {
            throw new IllegalStateException(
                String.format("Duplicate ID '%s' registered for %s", type.id, type.clazz.getName())
            );
        }
        return type;
    }

    /**
     * Get type object by ID and object type.
     *
     * @param type The object type the type object represents
     * @param id   Unique ID for the object type
     * @param <T>  The type of object this object type represents
     * @return Requested type object, or null if no type object exists by that object type and ID
     */
    @Nullable
    public static <T> Type<T> get(@NotNull Class<T> type, @NotNull String id) {
        Map<String, Type<?>> innerMap = BY_CLASS.get(type);
        if (innerMap == null) {
            return null;
        }
        return Unsafe.cast(innerMap.get(id.toLowerCase(Locale.ROOT)));
    }

    private final String id;
    private final Class<? extends T> clazz;

    private final int hash;

    /**
     * Constructs a new instance of Type.
     *
     * @param id    Unique ID for type
     * @param clazz Class this type represents
     */
    public Type(@NotNull String id, @NotNull Class<? extends T> clazz) {
        this.id = id.toLowerCase(Locale.ROOT);
        this.clazz = clazz;

        this.hash = Objects.hash(this.id, this.clazz);
    }

    /**
     * Get unique ID for this type.
     *
     * @return Unique type ID
     */
    @NotNull
    public String getId() {
        return this.id;
    }

    /**
     * Create a new object of this type.
     *
     * @return A new object of this type
     */
    @NotNull
    public T create() {
        try {
            return this.clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new renderer of this type.
     *
     * @param name              Display name for renderer
     * @param icon              Icon file for webmap
     * @param heightmap         The heightmap to use
     * @param biomeBlend        Number of blocks to blend biome tints
     * @param translucentFluids True to render fluids as translucent
     * @return A new renderer
     */
    @NotNull
    public T create(
        @NotNull String name,
        @NotNull String icon,
        @Nullable Type<Heightmap> heightmap,
        int biomeBlend,
        boolean translucentFluids
    ) {
        heightmap = heightmap == null ? Heightmap.NOOP : heightmap;
        try {
            return this.clazz
                .getConstructor(String.class, String.class, Type.class, int.class, boolean.class)
                .newInstance(name, icon, heightmap, biomeBlend, translucentFluids);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        Type<T> that = Unsafe.cast(obj);
        return Objects.equals(this.id, that.id)
            && Objects.equals(this.clazz, that.clazz);
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public String toString() {
        return "Type["
            + "id=" + this.id
            + ",clazz=" + this.clazz.getSimpleName()
            + "]";
    }
}
