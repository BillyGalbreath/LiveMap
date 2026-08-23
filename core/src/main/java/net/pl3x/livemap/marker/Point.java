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

package net.pl3x.livemap.marker;

import com.google.gson.JsonObject;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a two-dimensional point.
 */
public class Point implements JsonSerializable {
    // @formatter:off
    /**
     * Point to the north and west.
     */
    public static final Point NORTHWEST = Point.of(-1, -1);
    /**
     * Point to the north.
     */
    public static final Point NORTH     = Point.of( 0, -1);
    /**
     * Point to the north and east.
     */
    public static final Point NORTHEAST = Point.of( 1, -1);
    /**
     * Point to the east.
     */
    public static final Point EAST      = Point.of( 1,  0);
    /**
     * Point to the south and east.
     */
    public static final Point SOUTHEAST = Point.of( 1,  1);
    /**
     * Point to the south.
     */
    public static final Point SOUTH     = Point.of( 0,  1);
    /**
     * Point to the south and west.
     */
    public static final Point SOUTHWEST = Point.of(-1,  1);
    /**
     * Point to the west.
     */
    public static final Point WEST      = Point.of(-1,  0);
    // @formatter:on

    /**
     * Point representing 0,0.
     */
    public static final Point ZERO = Point.of(0, 0);

    /**
     * Constructs a new instance of Point.
     *
     * @param x X coordinate
     * @param z Z coordinate
     * @return A new point
     */
    @NotNull
    public static Point of(int x, int z) {
        return new Point(x, z);
    }

    /**
     * Constructs a new instance of Point.
     *
     * @param x X coordinate
     * @param z Z coordinate
     * @return A new point
     */
    @NotNull
    public static Point of(double x, double z) {
        // floor first because casting alone truncates towards zero
        return of((int) Math.floor(x), (int) Math.floor(z));
    }

    private final int x;
    private final int z;

    /**
     * Constructs a new instance of Point.
     *
     * @param x X coordinate
     * @param z Z coordinate
     */
    public Point(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    @NotNull
    public JsonObject toJson() {
        JsonObjectWrapper wrapper = new JsonObjectWrapper();
        wrapper.addProperty("x", getX());
        wrapper.addProperty("z", getZ());
        return wrapper.getJsonObject();
    }

    /**
     * Constructs a new instance of Point from JSON.
     *
     * @param obj JSON object
     * @return A new point
     */
    @NotNull
    public static Point fromJson(@NotNull JsonObject obj) {
        return Point.of(obj.get("x").getAsInt(), obj.get("z").getAsInt());
    }

    /**
     * Get the X coordinate.
     *
     * @return X coordinate
     */
    public int getX() {
        return this.x;
    }

    /**
     * Get the Z coordinate.
     *
     * @return Z coordinate
     */
    public int getZ() {
        return this.z;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        Point other = (Point) o;
        return getX() == other.getX() &&
            getZ() == other.getZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z);
    }

    @Override
    @NotNull
    public String toString() {
        return "Point["
            + "x=" + getX()
            + ",z=" + getZ()
            + "]";
    }
}
