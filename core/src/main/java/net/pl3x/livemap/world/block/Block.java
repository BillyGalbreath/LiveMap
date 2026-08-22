package net.pl3x.livemap.world.block;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a minecraft block.
 */
public class Block {
    private final BlockState defaultState;

    /**
     * Constructs a new instance of Block.
     *
     * @param index Unique identifying number
     * @param id    String id
     * @param color Map color
     */
    public Block(int index, @NotNull String id, int color) {
        this.defaultState = new BlockState(this);
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
}
