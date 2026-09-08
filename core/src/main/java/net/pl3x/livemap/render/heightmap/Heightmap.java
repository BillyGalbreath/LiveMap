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

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.util.Type;
import net.pl3x.livemap.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a heightmap.
 */
public abstract class Heightmap {
    public static final Type<Heightmap> NOOP = Type.register(new Type<>("noop", NoopHeightmap.class));
    public static final Type<Heightmap> BASIC = Type.register(new Type<>("basic", BasicHeightmap.class));
    public static final Type<Heightmap> FANCY = Type.register(new Type<>("fancy", FancyHeightmap.class));

    private final Type<Heightmap> type;

    /**
     * Constructs a new instance of Heightmap.
     *
     * @param type The heightmap type
     */
    protected Heightmap(@NotNull Type<Heightmap> type) {
        this.type = type;
    }

    /**
     * Get heightmap's type.
     *
     * @return Type of heightmap
     */
    @NotNull
    public Type<Heightmap> getType() {
        return this.type;
    }

    /**
     * The absolute minimum alpha this heightmap can produce.
     *
     * @return Minimum alpha
     */
    public int getMin() {
        return 0x00;
    }

    /**
     * The normal alpha for no height difference.
     *
     * @return Normal alpha
     */
    public int getMid() {
        return 0x22;
    }

    /**
     * The absolute maximum alpha this heightmap can produce.
     *
     * @return Maximum alpha
     */
    public int getMax() {
        return 0x44;
    }

    /**
     * Get heightmap alpha for specified block coordinates.
     *
     * @param chunk  Possible chunk (used as cache for faster lookups)
     * @param blockX X block coordinate
     * @param blockZ Z block coordinate
     * @return The calculated heightmap alpha for block coordinates
     */
    public int getAlpha(@NotNull Chunk chunk, int blockX, int blockZ) {
        return getMid();
    }

    /**
     * Get heightmap alpha for height difference.
     *
     * @param y1    First Y coordinate
     * @param y2    Second Y coordinate
     * @param alpha Default height alpha (no difference)
     * @param step  Alpha difference
     * @return The calculated heightmap alpha for height difference
     */
    public int getAlpha(int y1, int y2, int alpha, int step) {
        int direction = Integer.compare(y2, y1);
        int newAlpha = alpha + (direction * step);
        return Math.clamp(newAlpha, getMin(), getMax());
    }

    /**
     * A chance to do things <em>before</em> the render has run.
     *
     * @param tile      Tile image
     * @param rand      Random for RNG stuff
     * @param cancelled Cancellation token
     */
    public void preRender(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        // optional override
    }

    /**
     * A chance to do things <em>after</em> the render has run.
     *
     * @param tile      Tile image
     * @param rand      Random for RNG stuff
     * @param cancelled Cancellation token
     */
    public void postRender(@NotNull TileCanvas tile, @NotNull ThreadLocalRandom rand, @NotNull AtomicBoolean cancelled) {
        // optional override
    }

    /**
     * A chance to do things <em>during</em> a block render.
     *
     * @param tile Tile image
     * @param data Block data
     * @param rand Random for RNG stuff
     */
    public void renderBlock(@NotNull TileCanvas tile, @NotNull Chunk.BlockData data, @NotNull ThreadLocalRandom rand) {
        // optional override
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        Heightmap other = (Heightmap) o;
        return getType().equals(other.getType());
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public @NotNull String toString() {
        return "Heightmap{"
            + "type=" + getType()
            + "}";
    }
}
