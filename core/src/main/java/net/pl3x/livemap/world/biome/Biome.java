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

package net.pl3x.livemap.world.biome;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a Minecraft biome and its color and relevant properties.
 */
@SuppressWarnings("ClassCanBeRecord")
public class Biome {
    public static final Biome DEFAULT = new Biome(0, "minecraft:default", 0x000070, 0x9E814D, 0x73A74E, 0x8EB971, 0x3F76E4, (x, z, def) -> def);

    private final int index;
    private final String id;
    private final int color;
    private final int foliage;
    private final int dryFoliage;
    private final int grass;
    private final int water;
    private final GrassModifier grassModifier;

    /**
     * Constructs a new instance of Biome.
     *
     * @param index         Unique index number
     * @param id            Namespaced id
     * @param color         Color biome should appear on biome renderer
     * @param dryFoliage    Color tint for dry foliage
     * @param foliage       Color tint for foliage
     * @param grass         Color tint for grass
     * @param water         Color tint for water
     * @param grassModifier Color modifier to grass tint
     */
    public Biome(int index, @NotNull String id, int color, int dryFoliage, int foliage, int grass, int water, @NotNull GrassModifier grassModifier) {
        this.index = index;
        this.id = id;
        this.color = color;
        this.foliage = foliage;
        this.dryFoliage = dryFoliage;
        this.grass = grass;
        this.water = water;
        this.grassModifier = grassModifier;
    }

    /**
     * Get the grass tint color after running through the grass modifier.
     *
     * @param blockX X block coordinate
     * @param blockZ Z block coordinate
     * @return Modified grass tint color
     */
    public int getModifiedGrassColor(int blockX, int blockZ) {
        return getGrassModifier().modify(blockX, blockZ, getGrass());
    }

    /**
     * Get unique index number.
     *
     * @return Unique index number
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Get namespaced id.
     *
     * @return Namespaced id
     */
    @NotNull
    public String getId() {
        return this.id;
    }

    /**
     * Get map color for biome renderer.
     *
     * @return Biome color
     */
    public int getColor() {
        return this.color;
    }

    /**
     * Get color tint for dry foliage.
     *
     * @return Dry foliage tint color
     */
    public int getDryFoliage() {
        return this.dryFoliage;
    }

    /**
     * Get color tint for foliage.
     *
     * @return Foliage tint color
     */
    public int getFoliage() {
        return this.foliage;
    }

    /**
     * Get color tint for grass.
     *
     * @return Grass tint color
     */
    public int getGrass() {
        return this.grass;
    }

    /**
     * Get color tint for water.
     *
     * @return Water tint color
     */
    public int getWater() {
        return this.water;
    }

    /**
     * Get grass tint color modifier.
     *
     * <p>This is used for things like the swamp and dark forest biomes.
     *
     * @return Modifier for grass tint color
     */
    @NotNull
    public GrassModifier getGrassModifier() {
        return this.grassModifier;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        Biome other = (Biome) o;
        return this.index == other.index
            && this.id.equals(other.id)
            && this.color == other.color
            && this.dryFoliage == other.dryFoliage
            && this.foliage == other.foliage
            && this.grass == other.grass
            && this.water == other.water;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.index, this.id, this.color, this.dryFoliage, this.foliage, this.grass, this.water);
    }

    @Override
    @NotNull
    public String toString() {
        return "Biome["
            + "index=" + index
            + ",key=" + this.id
            + ",color=" + color
            + ",dryFoliage=" + dryFoliage
            + ",foliage=" + foliage
            + ",grass=" + grass
            + ",water=" + water
            + "]";
    }

    /**
     * Represents the grass tint color modifier.
     *
     * <p>This is used for things like the swamp and dark forest biomes.
     */
    @FunctionalInterface
    public interface GrassModifier {
        /**
         * Modify grass tint color at location.
         *
         * @param blockX X block coordinate
         * @param blockZ Z block coordinate
         * @param def    Default color value
         * @return Modified tint color
         */
        int modify(int blockX, int blockZ, int def);
    }
}
