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
import org.jetbrains.annotations.NotNull;

/**
 * Represents a state of a block.
 */
public class BlockState {
    private static byte parseProperty(@NotNull String property) {
        try {
            return Integer.valueOf(property).byteValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private final Block block;
    private final byte age;
    private final byte moisture;
    private final byte power;

    /**
     * Constructs a new instance of BlockState with no properties.
     *
     * @param block Block represented by this state
     */
    public BlockState(@NotNull Block block) {
        this.block = block;
        this.age = this.moisture = this.power = -1;
    }

    /**
     * Constructs a new instance of BlockState with specified properties.
     *
     * @param block      Block represented by this state
     * @param properties Properties for this state
     */
    public BlockState(@NotNull Block block, @NotNull Map<String, String> properties) {
        this.block = block;
        this.age = parseProperty(properties.get("age"));
        this.moisture = parseProperty(properties.get("moisture"));
        this.power = parseProperty(properties.get("power"));
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

    /**
     * Check if this state's block has age.
     *
     * @return True if state's block has age
     */
    public boolean hasAge() {
        return getBlock().hasAge();
    }

    /**
     * Check if this state's block has moisture.
     *
     * @return True if state's block has moisture
     */
    public boolean hasMoisture() {
        return getBlock().hasMoisture();
    }

    /**
     * Check if this state's block has power.
     *
     * @return True if state's block has power
     */
    public boolean hasPower() {
        return getBlock().hasPower();
    }
}
