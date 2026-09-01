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

package net.pl3x.livemap.render.renderer;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.pl3x.livemap.render.heightmap.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a map renderer.
 */
public abstract class Renderer {
    private final Type type;
    private final String name;
    private final String icon;
    private final Heightmap heightmap;
    private final int biomeBlend;
    private final boolean translucentFluids;

    /**
     * Constructs a new instance of Renderer.
     *
     * @param type              The type of renderer
     * @param name              Display name for renderer
     * @param icon              Icon file for webmap
     * @param heightmap         The heightmap to use
     * @param biomeBlend        Number of blocks to blend biome tints
     * @param translucentFluids True to render fluids as translucent
     *
     */
    public Renderer(
        @NotNull Type type,
        @NotNull String name,
        @NotNull String icon,
        @Nullable Heightmap heightmap,
        int biomeBlend,
        boolean translucentFluids
    ) {
        this.type = type;
        this.name = name;
        this.icon = icon;
        this.heightmap = heightmap;
        this.biomeBlend = biomeBlend;
        this.translucentFluids = translucentFluids;
    }

    /**
     * Get renderer type.
     *
     * @return Type of renderer
     */
    @NotNull
    public Type getType() {
        return this.type;
    }

    /**
     * Get display name for webmap.
     *
     * @return Display name
     */
    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * Get the icon for webmap.
     *
     * @return The icon
     */
    @NotNull
    public String getIcon() {
        return this.icon;
    }

    /**
     * Get the heightmap for this renderer.
     *
     * @return The heightmap
     */
    @Nullable
    public Heightmap getHeightmap() {
        return this.heightmap;
    }

    /**
     * Get the number of blocks to blend biome tints together.
     *
     * @return Number of blocks to blend biome tints
     */
    public int getBiomeBlend() {
        return this.biomeBlend;
    }

    /**
     * Check if fluids are translucent for this map render.
     *
     * @return True if fluids are translucent
     */
    public boolean isTranslucentFluids() {
        return this.translucentFluids;
    }

    /**
     * Represents a type of renderer.
     *
     */
    public static final class Type {
        private static final Map<String, Type> BY_NAME = new HashMap<>();

        public static final Type BASIC = register("basic", BasicRenderer.class);
        public static final Type BIOMES = register("biomes", BiomesRenderer.class);
        public static final Type FANCY = register("fancy", FancyRenderer.class);
        public static final Type FLOWERMAP = register("flowermap", FlowerMapRenderer.class);
        public static final Type INHABITED = register("inhabited", InhabitedRenderer.class);
        public static final Type NETHER_ROOF = register("nether_roof", NetherRoofRenderer.class);

        @NotNull
        private static Type register(@NotNull String name, @NotNull Class<? extends Renderer> clazz) {
            Type type = new Type(name, clazz);
            BY_NAME.put(name.toLowerCase(Locale.ROOT), type);
            return type;
        }

        private final @NotNull String id;
        private final @NotNull Class<? extends Renderer> clazz;

        /**
         * Constructs a new instance of Type.
         *
         * @param id    Unique id for type
         * @param clazz Renderer class this type represents
         */
        public Type(@NotNull String id, @NotNull Class<? extends Renderer> clazz) {
            this.id = id;
            this.clazz = clazz;
        }

        /**
         * Get renderer type instance by name.
         *
         * @param name Name of renderer type
         * @return Requested renderer type
         */
        @Nullable
        public static Type get(@NotNull String name) {
            return BY_NAME.get(name.toLowerCase(Locale.ROOT));
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
        public Renderer create(
            @NotNull String name,
            @NotNull String icon,
            @Nullable Heightmap heightmap,
            int biomeBlend,
            boolean translucentFluids
        ) {
            try {
                return this.clazz
                    .getConstructor(String.class, String.class, Heightmap.class, int.class, boolean.class)
                    .newInstance(name, icon, heightmap, biomeBlend, translucentFluids);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Get unique id for this type.
         *
         * @return Unique type id
         */
        @NotNull
        public String getId() {
            return this.id;
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
            var that = (Type) obj;
            return Objects.equals(this.id, that.id)
                && Objects.equals(this.clazz, that.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id, this.clazz);
        }

        @Override
        public String toString() {
            return "Type["
                + "id=" + this.id
                + "]";
        }
    }
}
