package net.pl3x.livemap.world.biome;

import net.pl3x.livemap.util.Registry;

/**
 * A registry of all known biomes to be rendered.
 */
public class BiomeRegistry extends Registry<Biome> {
    /**
     * Constructs a new instance of BiomeRegistry.
     */
    public BiomeRegistry() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    @Override
    public void rebuild() {
    }
}
