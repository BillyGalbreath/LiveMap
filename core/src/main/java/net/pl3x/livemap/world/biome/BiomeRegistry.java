package net.pl3x.livemap.world.biome;

import net.pl3x.livemap.util.Registry;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Get the default color for foliage/grass based on
     * temperature and humidity from image gradient images.
     *
     * @param temperature Biome temperature
     * @param humidity    Biome humidity
     * @param map         Map of pixels from image gradient
     * @return The requested default color
     */
    protected static int getDefaultColor(double temperature, double humidity, int @NotNull [] map) {
        int i = (int) ((1.0 - temperature) * 255.0);
        int j = (int) ((1.0 - (humidity * temperature)) * 255.0);
        int k = j << 8 | i;
        return k > map.length ? 0 : map[k];
    }
}
