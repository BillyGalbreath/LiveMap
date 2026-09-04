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

package net.pl3x.livemap.world.block;

import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a state of a block.
 */
public class BlockState {
    /**
     * Parse property from string to byte without the overhead of try/catch NumberFormatException.
     *
     * <p>Note: This is oversimplified by not processing negative values.
     *
     * @param property String value to parse
     * @return Property value as byte
     */
    private static byte parseProperty(@NotNull String property) {
        if (property.isBlank()) {
            return -1;
        }

        int len = property.length();
        int result = 0;

        // parse each digit
        for (int i = 0; i < len; i++) {
            char c = property.charAt(i);

            // instantly reject non-digits (including '-')
            if (c < '0' || c > '9') {
                return -1;
            }

            // add to the next digit
            result = result * 10 + (c - '0');
        }

        return (byte) result;
    }

    private final Block block;
    private final byte age;
    private final byte moisture;
    private final byte power;

    private final int hash;

    /**
     * Constructs a new instance of BlockState with no properties.
     *
     * @param block Block represented by this state
     */
    public BlockState(@NotNull Block block) {
        this.block = block;
        this.age = this.moisture = this.power = -1;

        this.hash = Objects.hash(block, this.age, this.moisture, this.power);
    }

    /**
     * Constructs a new instance of BlockState with specified properties.
     *
     * @param block      Block represented by this state
     * @param properties Properties for this state
     */
    public BlockState(@NotNull Block block, @NotNull Map<String, String> properties) {
        this.block = block;
        this.age = parseProperty(properties.getOrDefault("age", ""));
        this.moisture = parseProperty(properties.getOrDefault("moisture", ""));
        this.power = parseProperty(properties.getOrDefault("power", ""));

        this.hash = Objects.hash(block, this.age, this.moisture, this.power);
    }

    /**
     * Gets the block represented by this state.
     *
     * @return the block represented by this state
     */
    @NotNull
    public Block getBlock() {
        return this.block;
    }

    /**
     * Get state's age (crops).
     *
     * @return Age, or -1 if n/a
     */
    public byte getAge() {
        return this.age;
    }

    /**
     * Get state's moisture level (farmland).
     *
     * @return Moisture level, or -1 if n/a
     */
    public byte getMoisture() {
        return this.moisture;
    }

    /**
     * Get state's power output (redstone).
     *
     * @return Power output, or -1 if n/a
     */
    public byte getPower() {
        return this.power;
    }

    /**
     * Check if this state's block is air.
     *
     * @return True if state's block is air
     */
    public boolean isAir() {
        return getBlock().isAir();
    }

    /**
     * Check if this state's block is flat.
     *
     * @return True if state's block is flat
     */
    public boolean isFlat() {
        return getBlock().isFlat();
    }

    /**
     * Check if this state's block is foliage.
     *
     * @return True if state's block is foliage
     */
    public boolean isFoliage() {
        return getBlock().isFoliage();
    }

    /**
     * Check if this state's block is dry foliage.
     *
     * @return True if state's block is dry foliage
     */
    public boolean isDryFoliage() {
        return getBlock().isDryFoliage();
    }

    /**
     * Check if this state's block is grass.
     *
     * @return True if state's block is grass
     */
    public boolean isGrass() {
        return getBlock().isGrass();
    }

    /**
     * Check if this state's block is water.
     *
     * @return True if state's block is water
     */
    public boolean isWater() {
        return getBlock().isWater();
    }

    /**
     * Check if this state's block is a fluid.
     *
     * @return True if state's block is a fluid
     */
    public boolean isFluid() {
        return getBlock().isFluid();
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
        BlockState other = (BlockState) o;
        return getBlock().equals(other.getBlock())
            && getAge() == other.getAge()
            && getMoisture() == other.getMoisture()
            && getPower() == other.getPower();
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    @NotNull
    public String toString() {
        return "BlockState["
            + "block=" + getBlock()
            + ",age=" + getAge()
            + ",moisture=" + getMoisture()
            + ",power=" + getPower()
            + "]";
    }
}
