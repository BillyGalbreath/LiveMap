package net.pl3x.livemap.world.block;

import net.pl3x.livemap.util.Registry;

/**
 * A registry of all known blocks to be rendered.
 */
public class BlockRegistry extends Registry<Block> {
    /**
     * Constructs a new instance of BlockRegistry.
     */
    public BlockRegistry() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    @Override
    public void rebuild() {
    }
}
