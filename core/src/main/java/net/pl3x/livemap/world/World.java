package net.pl3x.livemap.world;

import java.nio.file.Path;
import net.pl3x.livemap.LiveMap;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a renderable world.
 */
public abstract class World {
    private final String name;

    private final Path regionDir;
    private final Path tilesDir;

    /**
     * Constructs a new instance of World.
     *
     * @param name       Name of world
     * @param regionsDir Regions directory
     */
    public World(@NotNull String name, @NotNull Path regionsDir) {
        this.name = name;
        this.regionDir = regionsDir;
        this.tilesDir = LiveMap.api().getTilesDir().resolve(name.replace(":", "-"));
    }

    /**
     * Get this world's platform specific level.
     *
     * @param <T> Platform specific level type
     * @return This world's platform specific level
     */
    @NotNull
    public abstract <T> T getLevel();

    /**
     * Get the name of this world.
     *
     * @return World's name
     */
    @NotNull
    public String getName() {
        return this.name;
    }

    /**
     * Get path to regions directory.
     *
     * @return Regions directory
     */
    @NotNull
    public Path getRegionsDir() {
        return this.regionDir;
    }

    /**
     * Get path to tiles directory.
     *
     * @return Tiles directory
     */
    @NotNull
    public Path getTilesDir() {
        return this.tilesDir;
    }
}
