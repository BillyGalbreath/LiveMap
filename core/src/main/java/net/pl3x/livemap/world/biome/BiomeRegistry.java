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

import net.pl3x.livemap.util.Registry;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;

/**
 * A registry of all known biomes to be rendered.
 */
public abstract class BiomeRegistry extends Registry<Biome> {
    private final World world;
    private final long hashedSeed;

    /**
     * Constructs a new instance of BiomeRegistry.
     *
     * @param world      World this registry belongs to
     * @param hashedSeed The world's hashed seed
     */
    public BiomeRegistry(@NotNull World world, long hashedSeed) {
        this.world = world;
        this.hashedSeed = hashedSeed;
    }

    /**
     * Get the world for this registry.
     *
     * @return The owning world
     */
    @NotNull
    public World getWorld() {
        return this.world;
    }

    /**
     * Get the default color for foliage/grass based on
     * temperature and humidity from image gradient images.
     *
     * @param temperature Biome temperature
     * @param humidity    Biome humidity
     * @param map         Map of pixels from image gradient
     * @return The requested default color
     */
    protected static int getDefaultColor(double temperature, double humidity, int @NotNull [] map) {
        int i = (int) ((1.0 - temperature) * 255.0);
        int j = (int) ((1.0 - (humidity * temperature)) * 255.0);
        int k = j << 8 | i;
        return k > map.length ? 0 : map[k];
    }

    /**
     * Get biome at specified block coordinates.
     *
     * @param region Possible region (used as cache for faster lookups)
     * @param blockX X block coordinate
     * @param blockY Y block coordinate
     * @param blockZ Z block coordinate
     * @return The biome at specified coordinates
     */
    @NotNull
    public Biome getBiome(@NotNull Region region, int blockX, int blockY, int blockZ) {
        int absX = blockX - 2;
        int absY = blockY - 2;
        int absZ = blockZ - 2;
        int parentX = absX >> 2;
        int parentY = absY >> 2;
        int parentZ = absZ >> 2;
        double fractX = (double) (absX & 3) / 4.0D;
        double fractY = (double) (absY & 3) / 4.0D;
        double fractZ = (double) (absZ & 3) / 4.0D;
        int minI = 0;
        double minFiddleDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 8; ++i) {
            boolean xEven = (i & 4) == 0;
            boolean yEven = (i & 2) == 0;
            boolean zEven = (i & 1) == 0;
            int cornerX = xEven ? parentX : parentX + 1;
            int cornerY = yEven ? parentY : parentY + 1;
            int cernerZ = zEven ? parentZ : parentZ + 1;
            double distanceX = xEven ? fractX : fractX - 1.0D;
            double distanceY = yEven ? fractY : fractY - 1.0D;
            double distanceZ = zEven ? fractZ : fractZ - 1.0D;
            double next = getFiddledDistance(this.hashedSeed, cornerX, cornerY, cernerZ, distanceX, distanceY, distanceZ);
            if (minFiddleDistance > next) {
                minI = i;
                minFiddleDistance = next;
            }
        }
        int biomeX = ((minI & 4) == 0 ? parentX : parentX + 1) << 2;
        int biomeY = ((minI & 2) == 0 ? parentY : parentY + 1) << 2;
        int biomeZ = ((minI & 1) == 0 ? parentZ : parentZ + 1) << 2;
        return getWorld().getChunk(region, blockX >> 4, blockZ >> 4).getBiome(biomeX, biomeY, biomeZ);
    }

    private double getFiddledDistance(long seed, int xRandom, int yRandom, int zRandom, double distanceX, double distanceY, double distanceZ) {
        long rval = salt(seed, xRandom);
        rval = salt(rval, yRandom);
        rval = salt(rval, zRandom);
        rval = salt(rval, xRandom);
        rval = salt(rval, yRandom);
        rval = salt(rval, zRandom);
        double fiddleX = getFiddle(rval);
        rval = salt(rval, seed);
        double fiddleY = getFiddle(rval);
        rval = salt(rval, seed);
        double fiddleZ = getFiddle(rval);
        return square(distanceZ + fiddleZ) + square(distanceY + fiddleY) + square(distanceX + fiddleX);
    }

    private static double getFiddle(long rval) {
        return (double) ((rval >> 24 & 1023L) - 512L) * 8.7890625E-4;
    }

    private long salt(long seed, long salt) {
        return seed * (seed * 6364136223846793005L + 1442695040888963407L) + salt;
    }

    private double square(double n) {
        return n * n;
    }
}
