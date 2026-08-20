package net.pl3x.livemap.world.block;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a state of a block.
 */
public class BlockState {
    /**
     * The default blockstate for when no block exists, aka air.
     */
    public static final BlockState AIR = new BlockState(Blocks.AIR);

    private final Block block;

    /**
     * Constructs a new instance of BlockState.
     *
     * @param block Block represented by this block state
     */
    public BlockState(@NotNull Block block) {
        this.block = block;
    }

    /**
     * Gets the block represented by this block state.
     *
     * @return the block represented by this block state
     */
    @NotNull
    public Block getBlock() {
        return this.block;
    }
}
