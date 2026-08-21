package net.pl3x.livemap.world;

import net.pl3x.livemap.util.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a registry of renderable worlds.
 */
public abstract class WorldRegistry extends Registry<World> {
    /**
     * Constructs a new instance of WorldRegistry.
     */
    public WorldRegistry() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    /**
     * Removes the mapping for the specified key from this registry if present.
     *
     * @param key key whose mapping is to be removed from the registry
     * @return the previous value associated with {@code key}, or
     * {@code null} if there was no mapping for {@code key}.
     */
    @Nullable
    public World remove(@NotNull String key) {
        World world = super.remove(key);
        if (world != null) {
            // todo
            //LiveMap.api().getEventRegistry().callEvent(new WorldUnloadedEvent(world));
            //world.getMarkerTask().cancel();
            //world.getLiveDataTask().cancel();
            //world.getRegionFileWatcher().stop();
            //world.cleanup();
        }
        return world;
    }
}
