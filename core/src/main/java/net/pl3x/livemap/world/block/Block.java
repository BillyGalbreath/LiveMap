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
    /**
     * Default block of just air.
     */
    public static final Block AIR = new Block(0, "minecraft:air", 0x000000);

    /**
     * This block is considered air.
     * <p>
     * Air blocks will be completely ignored.
     */
    public static final short FLAG_AIR = 1;
    /**
     * This block is considered flat.
     * <p>
     * Flat blocks will be ignored by the renderer to assist in better looking heightmap.
     */
    public static final short FLAG_FLAT = 2;
    /**
     * This block is considered foliage.
     * <p>
     * Foliage blocks will use biome color override.
     */
    public static final short FLAG_FOLIAGE = 4;
    /**
     * This block is considered dry foliage.
     * <p>
     * Dry foliage blocks will use biome color override.
     */
    public static final short FLAG_DRY_FOLIAGE = 8;
    /**
     * This block is considered grass.
     * <p>
     * Grass blocks will use biome color modifier.
     */
    public static final short FLAG_GRASS = 16;
    /**
     * This block is considered water.
     * <p>
     * Water blocks will use biome color override.
     */
    public static final short FLAG_WATER = 32;
    /**
     * This block is considered fluid.
     * <p>
     * Fluid blocks can appear translucent, if configured.
     */
    public static final short FLAG_FLUID = 64;
    /**
     * This block is considered to have age.
     * <p>
     * Aged blocks will use Mojang's color modifier.
     */
    public static final short FLAG_PROPERTY_AGE = 128;
    /**
     * This block is considered able to hold moisture (farmland).
     * <p>
     * Moisture blocks will use Mojang's color modifier.
     */
    public static final short FLAG_PROPERTY_MOISTURE = 256;
    /**
     * This block is considered to have power (redstone wire).
     * <p>
     * Powered blocks will use Mojang's color modifier.
     */
    public static final short FLAG_PROPERTY_POWER = 512;

    private final int index;
    private final String id;
    private final int color;
    private final int vanilla;
    private final int hash;

    private short flags;

    private final BlockState defaultState;

    /**
     * Constructs a new instance of Block.
     *
     * @param index   Persistent unique identifying number
     * @param id      String id
     * @param vanilla Vanilla's map color
     */
    public Block(int index, @NotNull String id, int vanilla) {
        this(index, id, vanilla, (short) 0);
    }

    /**
     * Constructs a new instance of Block.
     *
     * @param index      Persistent unique identifying number
     * @param id         String id
     * @param vanilla    Vanilla's map color
     * @param properties Properties flag(s) (marks which blocks have which properties)
     */
    public Block(int index, @NotNull String id, int vanilla, short properties) {
        this.index = index;
        this.id = id;

        int color = ColorsConfig.BLOCK_COLORS.getOrDefault(id, vanilla);
        this.color = color == 0 ? 0 : Colors.alpha(0xFF, color);
        this.vanilla = vanilla == 0 ? 0 : Colors.alpha(0xFF, vanilla);

        int flat = BlocksConfig.BLOCKS_FLAT.contains(id) ? FLAG_FLAT : 0;
        int air = BlocksConfig.BLOCKS_AIR.contains(id) ? FLAG_AIR : 0;
        int foliage = BlocksConfig.BLOCKS_FOLIAGE.contains(id) ? FLAG_FOLIAGE : 0;
        int dryFoliage = BlocksConfig.BLOCKS_DRY_FOLIAGE.contains(id) ? FLAG_DRY_FOLIAGE : 0;
        int grass = BlocksConfig.BLOCKS_GRASS.contains(id) ? FLAG_GRASS : 0;
        int water = BlocksConfig.BLOCKS_WATER.contains(id) ? FLAG_WATER : 0;
        int fluid = water > 0 || "minecraft:lava".equals(id) ? FLAG_FLUID : 0;

        this.flags = (short) (flat | air | foliage | dryFoliage | grass | water | fluid | properties);

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
     * property flag, otherwise {@code false} if block has none
     */
    public boolean hasFlag(int mask) {
        return (this.flags & mask) > 0;
    }

    /**
     * Returns the bit flags for this block's properties.
     *
     * @return Short flag bits
     */
    public short getFlags() {
        return this.flags;
    }

    /**
     * Replaces the flags with a new value.
     *
     * @param flags Short flag bits
     */
    public void setFlags(short flags) {
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
     * Check if this block is foliage.
     *
     * @return True if block is foliage
     */
    public boolean isFoliage() {
        return hasFlag(FLAG_FOLIAGE);
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
     * Check if this block has age.
     *
     * @return True if block has age
     */
    public boolean hasAge() {
        return hasFlag(FLAG_PROPERTY_AGE);
    }

    /**
     * Check if this block has moisture.
     *
     * @return True if block has moisture
     */
    public boolean hasMoisture() {
        return hasFlag(FLAG_PROPERTY_MOISTURE);
    }

    /**
     * Check if this block has power.
     *
     * @return True if block has power
     */
    public boolean hasPower() {
        return hasFlag(FLAG_PROPERTY_POWER);
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
