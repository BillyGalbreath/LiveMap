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

import java.util.Objects;
import net.pl3x.livemap.configuration.BlocksConfig;
import net.pl3x.livemap.configuration.ColorsConfig;
import net.pl3x.livemap.render.image.Colors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a minecraft block.
 */
public class Block {
    public static final Block AIR = new Block(0, "minecraft:air", 0x000000);

    // @formatter:off: SingleSpaceSeparator
    public static final int FLAG_AIR         = 0b0000000000000001;
    public static final int FLAG_FLAT        = 0b0000000000000010;
    public static final int FLAG_DRY_FOLIAGE = 0b0000000000000100;
    public static final int FLAG_FOLIAGE     = 0b0000000000001000;
    public static final int FLAG_GLASS       = 0b0000000000010000;
    public static final int FLAG_GRASS       = 0b0000000000100000;
    public static final int FLAG_WATER       = 0b0000000001000000;
    public static final int FLAG_FLUID       = 0b0000000010000000;
    // @formatter:on: SingleSpaceSeparator

    private final int index;
    private final String id;
    private final int color;
    private final int vanilla;
    private final int hash;

    private int flags;

    private final BlockState defaultState;

    /**
     * Constructs a new instance of Block.
     *
     * @param index   Persistent unique identifying number
     * @param id      String id
     * @param vanilla Vanilla's map color
     */
    public Block(int index, @NotNull String id, int vanilla) {
        this.index = index;
        this.id = id;

        int color = ColorsConfig.BLOCK_COLORS.getOrDefault(id, vanilla);
        this.color = color == 0 ? 0 : Colors.alpha(0xFF, color);
        this.vanilla = vanilla == 0 ? 0 : Colors.alpha(0xFF, vanilla);

        int flat = BlocksConfig.BLOCKS_FLAT.contains(id) ? FLAG_FLAT : 0;
        int air = BlocksConfig.BLOCKS_AIR.contains(id) ? FLAG_AIR : 0;
        int dryFoliage = BlocksConfig.BLOCKS_DRY_FOLIAGE.contains(id) ? FLAG_DRY_FOLIAGE : 0;
        int foliage = BlocksConfig.BLOCKS_FOLIAGE.contains(id) ? FLAG_FOLIAGE : 0;
        int glass = BlocksConfig.BLOCKS_GLASS.contains(id) ? FLAG_GLASS : 0;
        int grass = BlocksConfig.BLOCKS_GRASS.contains(id) ? FLAG_GRASS : 0;
        int water = BlocksConfig.BLOCKS_WATER.contains(id) ? FLAG_WATER : 0;
        int fluid = water | ("minecraft:lava".equals(id) ? FLAG_FLUID : 0);

        this.flags = flat | air | dryFoliage | foliage | glass | grass | water | fluid;

        this.defaultState = new BlockState(this);

        this.hash = Objects.hash(getId());
    }

    /**
     * Get the unique index number for this block.
     *
     * @return Index id
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Get the string id.
     *
     * @return String id
     */
    @NotNull
    public String getId() {
        return this.id;
    }

    /**
     * Get the custom block color.
     *
     * @return Custom color
     */
    public int getColor() {
        return this.color;
    }

    /**
     * Get vanilla's map color.
     *
     * @return Vanilla color
     */
    public int getVanilla() {
        return this.vanilla;
    }

    /**
     * Whether block has specified property flag(s) or not.
     *
     * @param mask Property flag(s)
     * @return {@code true} is block contains at least one specified
     *     property flag, otherwise {@code false} if block has none
     */
    public boolean hasFlag(int mask) {
        return (this.flags & mask) > 0;
    }

    /**
     * Returns the bit flags for this block's properties.
     *
     * @return Short flag bits
     */
    public int getFlags() {
        return this.flags;
    }

    /**
     * Replaces the flags with a new value.
     *
     * @param flags Short flag bits
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Check if this block is air.
     *
     * @return True if block is air
     */
    public boolean isAir() {
        return hasFlag(FLAG_AIR);
    }

    /**
     * Check if this block is flat.
     *
     * @return True if block is flat
     */
    public boolean isFlat() {
        return hasFlag(FLAG_FLAT);
    }

    /**
     * Check if this block is dry foliage.
     *
     * @return True if block is dry foliage
     */
    public boolean isDryFoliage() {
        return hasFlag(FLAG_DRY_FOLIAGE);
    }

    /**
     * Check if this block is foliage.
     *
     * @return True if block is foliage
     */
    public boolean isFoliage() {
        return hasFlag(FLAG_FOLIAGE);
    }

    /**
     * Check if this block is glass.
     *
     * @return True if block is glass
     */
    public boolean isGlass() {
        return hasFlag(FLAG_GLASS);
    }

    /**
     * Check if this block is grass.
     *
     * @return True if block is grass
     */
    public boolean isGrass() {
        return hasFlag(FLAG_GRASS);
    }

    /**
     * Check if this block is water.
     *
     * @return True if block is water
     */
    public boolean isWater() {
        return hasFlag(FLAG_WATER);
    }

    /**
     * Check if this block is a fluid.
     *
     * @return True if block is a fluid
     */
    public boolean isFluid() {
        return hasFlag(FLAG_FLUID);
    }

    /**
     * Get the default block state of this block.
     *
     * @return Default block state
     */
    @NotNull
    public BlockState getDefaultState() {
        return this.defaultState;
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
        Block other = (Block) o;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    public int hashCode() {
        return this.hash;
    }
}
