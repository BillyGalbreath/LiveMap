package net.pl3x.livemap.world.block;

import java.util.Objects;
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

    // @formatter:off
    /**
     * This block is considered air.
     * <p>
     * Air blocks will be completely ignored.
     */
    public static final short FLAG_AIR         = 0b0000000000000001;
    /**
     * This block is considered flat.
     * <p>
     * Flat blocks will be ignored by the renderer to assist in better looking heightmap.
     */
    public static final short FLAG_FLAT        = 0b0000000000000010;
    /**
     * This block is considered foliage.
     * <p>
     * Foliage blocks will use biome color override.
     */
    public static final short FLAG_FOLIAGE     = 0b0000000000000100;
    /**
     * This block is considered dry foliage.
     * <p>
     * Dry foliage blocks will use biome color override.
     */
    public static final short FLAG_DRY_FOLIAGE = 0b0000000000001000;
    /**
     * This block is considered grass.
     * <p>
     * Grass blocks will use biome color modifier.
     */
    public static final short FLAG_GRASS       = 0b0000000000010000;
    /**
     * This block is considered water.
     * <p>
     * Water blocks will use biome color override.
     */
    public static final short FLAG_WATER       = 0b0000000000100000;
    /**
     * This block is considered fluid.
     * <p>
     * Fluid blocks can appear translucent, if configured.
     */
    public static final short FLAG_FLUID       = 0b0000000001000000;
    /**
     * This block is considered to have age.
     * <p>
     * Aged blocks will use Mojang's color modifier.
     */
    public static final short FLAG_AGE         = 0b0000000010000000;
    /**
     * This block is considered able to hold moisture.
     * <p>
     * Moisture blocks will use Mojang's color modifier.
     */
    public static final short FLAG_MOISTURE    = 0b0000000100000000;
    /**
     * This block is considered to have redstone power.
     * <p>
     * Redstone powered blocks will use Mojang's color modifier.
     */
    public static final short FLAG_POWER       = 0b0000001000000000;
    // @formatter:on

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
     * @param properties Properties flag(s)
     */
    public Block(int index, @NotNull String id, int vanilla, short properties) {
        this.index = index;
        this.id = id;

        int color = ColorsConfig.BLOCK_COLORS.getOrDefault(id, vanilla);
        this.color = color == 0 ? 0 : Colors.alpha(0xFF, color);
        this.vanilla = vanilla == 0 ? 0 : Colors.alpha(0xFF, vanilla);

        int flat = ColorsConfig.BLOCKS_FLAT.contains(id) ? FLAG_FLAT : 0;
        int air = ColorsConfig.BLOCKS_AIR.contains(id) ? FLAG_AIR : 0;
        int foliage = ColorsConfig.BLOCKS_FOLIAGE.contains(id) ? FLAG_FOLIAGE : 0;
        int dryFoliage = ColorsConfig.BLOCKS_DRY_FOLIAGE.contains(id) ? FLAG_DRY_FOLIAGE : 0;
        int grass = ColorsConfig.BLOCKS_GRASS.contains(id) ? FLAG_GRASS : 0;
        int water = ColorsConfig.BLOCKS_WATER.contains(id) ? FLAG_WATER : 0;
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
