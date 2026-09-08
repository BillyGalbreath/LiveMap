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

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TypeDeserializer;
import java.io.IOException;
import java.util.Objects;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.util.ByteUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a state of a block.
 */
public class BlockState {
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
     * @param block    Block represented by this state
     * @param age      Age property for this state
     * @param moisture Moisture property for this state
     * @param power    Power property for this state
     */
    public BlockState(@NotNull Block block, byte age, byte moisture, byte power) {
        this.block = block;
        this.age = age;
        this.moisture = moisture;
        this.power = power;

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

    /**
     * Get the custom block color.
     *
     * @return Custom color
     */
    public int getColor() {
        return getBlock().getColor();
    }

    /**
     * Get vanilla's map color.
     *
     * @return Vanilla color
     */
    public int getVanilla() {
        return getBlock().getVanilla();
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

    /**
     * Deserializer for block states.
     */
    public static class Deserializer implements TypeDeserializer<BlockState> {
        /**
         * Read blockstate from NBT.
         *
         * @param reader NBT reader
         * @return New bock state instance
         * @throws IOException if an I/O error occurs
         */
        @Override
        @NotNull
        public BlockState read(@NotNull NBTReader reader) throws IOException {
            String id = null;
            byte age = -1;
            byte moisture = -1;
            byte power = -1;

            reader.beginCompound();

            while (reader.hasNext()) {
                switch (reader.name()) {
                    case "Name" -> id = reader.nextString();
                    case "Properties" -> {
                        reader.beginCompound();
                        while (reader.hasNext()) {
                            switch (reader.name()) {
                                case "age" -> age = ByteUtil.parsePropertyByte(reader.nextString());
                                case "moisture" -> moisture = ByteUtil.parsePropertyByte(reader.nextString());
                                case "power" -> power = ByteUtil.parsePropertyByte(reader.nextString());
                                default -> reader.skip(); // needed to push the reader
                            }
                        }
                        reader.endCompound();
                    }
                    default -> reader.skip();
                }
            }

            reader.endCompound();

            if (id == null) {
                return Block.AIR.getDefaultState();
            }

            Block block = LiveMap.api().getBlockRegistry().getOrDefault(id, Block.AIR);
            if ((age & moisture & power) == -1) {
                return block.getDefaultState();
            }
            return new BlockState(block, age, moisture, power);
        }
    }
}
