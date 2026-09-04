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

package net.pl3x.livemap.world;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.configuration.WorldConfig;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.render.renderer.RendererRegistry;
import net.pl3x.livemap.util.LongDoubleBuffer;
import net.pl3x.livemap.util.LongLoadingCache;
import net.pl3x.livemap.world.biome.BiomeRegistry;
import net.pl3x.livemap.world.chunk.Chunk;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a renderable world.
 */
public abstract class World {
    public static final Runnable CACHE_CLEANUP_TASK = () -> LiveMap.api().getWorldRegistry()
        // iterate all worlds
        .forEach((_, world) -> {
            Logger.debug("Cleaning up region cache on %s".formatted(world.getName()));
            world.regionCache.cleanUp();
        });

    private final String name;
    private final long seed;
    private final Point spawn;
    private final Type type;

    private final Path regionDir;
    private final Path tilesDir;

    private final WorldConfig config;

    private final LongLoadingCache<Region> regionCache;
    private final LongDoubleBuffer pendingRegions = new LongDoubleBuffer();

    private final AtomicBoolean discarded = new AtomicBoolean(false);

    /**
     * Constructs a new instance of World.
     *
     * @param name       Name of world
     * @param seed       Seed used for chunk generation
     * @param spawn      Spawn point
     * @param type       Type of world
     * @param regionsDir Regions directory
     */
    public World(@NotNull String name, long seed, @NotNull Point spawn, @NotNull Type type, @NotNull Path regionsDir) {
        this.name = name;
        this.seed = seed;
        this.spawn = spawn;
        this.type = type;

        this.regionDir = regionsDir;
        this.tilesDir = LiveMap.api().getTilesDir().resolve(name.replace(":", "-"));

        this.config = new WorldConfig(this);

        // hold loaded regions in memory for up to 1 minute (max of 100 at any given time)
        this.regionCache = new LongLoadingCache<>(
            TimeUnit.MINUTES.toMillis(1), 100,
            index -> new Region(this, index));
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
     * Get whether map rendering is enabled or not for this world.
     *
     * @return True if map rendering is enabled
     */
    public boolean isEnabled() {
        return getConfig().ENABLED;
    }

    /**
     * Get configuration for this world.
     *
     * @return World config
     */
    @NotNull
    public WorldConfig getConfig() {
        return this.config;
    }

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
     * Get this world's seed.
     *
     * @return World seed
     */
    public long getSeed() {
        return this.seed;
    }

    /**
     * Get this world's spawn point.
     *
     * @return Spawn point
     */
    @NotNull
    public Point getSpawn() {
        return this.spawn;
    }

    /**
     * Get the center point of the map for this world.
     *
     * <p>If no center point is set, the world's spawn will be used.
     *
     * @return Center point
     */
    @NotNull
    public Point getCenter() {
        return getConfig().CENTER == null ? getSpawn() : getConfig().CENTER;
    }

    /**
     * Get this world's type.
     *
     * @return Type of world
     */
    @NotNull
    public Type getType() {
        return this.type;
    }

    /**
     * Get this world's pending regions.
     *
     * @return The pending regions
     */
    @NotNull
    public LongDoubleBuffer getPendingRegions() {
        return this.pendingRegions;
    }

    /**
     * Get this world's region cache.
     *
     * @return Region cache
     */
    @NotNull
    public LongLoadingCache<Region> getRegionCache() {
        return this.regionCache;
    }

    /**
     * Get a region by index.
     *
     * <p>If region is not cached it will be loaded from disk.
     *
     * @param index Region index
     * @return Requested region
     */
    @NotNull
    public Region getRegion(long index) {
        return this.regionCache.get(index);
    }

    /**
     * Get a region by coordinates.
     *
     * <p>If the coordinates are in the supplied region, then the supplied region is returned.
     *
     * @param region  Cached region
     * @param regionX X coordinate
     * @param regionZ Z coordinate
     * @return Requested region
     */
    @NotNull
    public Region getRegionFast(@Nullable Region region, int regionX, int regionZ) {
        if (region != null && region.getX() == regionX && region.getZ() == regionZ) {
            return region;
        }
        return getRegion(Region.pack(regionX, regionZ));
    }

    /**
     * Get chunk at specified chunk coordinates.
     *
     * @param region Possible region (used as cache for faster lookups)
     * @param chunkX X chunk coordinate
     * @param chunkZ Z chunk coordinate
     * @return Requested chunk
     */
    @NotNull
    public Chunk getChunk(@Nullable Region region, int chunkX, int chunkZ) {
        return getRegionFast(region, chunkX >> 5, chunkZ >> 5).getChunk(chunkX, chunkZ);
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

    /**
     * Get whether world has a ceiling or not (a layer of blocks at the top, like the nether).
     *
     * @return True if world has a ceiling
     */
    public abstract boolean hasCeiling();

    /**
     * Get the lowest buildable Y position.
     *
     * <p>i.e. <code>-64</code> for vanilla overworld
     *
     * @return Lowest Y position
     */
    public abstract int getMinY();

    /**
     * Get the highest buildable Y position.
     *
     * <p>i.e. <code>319</code> for vanilla overworld
     *
     * @return Highest Y position
     */
    public abstract int getMaxY();

    /**
     * Get the total world height.
     *
     * <p>Note: This is <em>not</em> the same as max Y. This includes the full
     * distance from minY to maxY (<code>maxY - minY + 1</code>)
     *
     * <p>i.e. <code>384</code> for vanilla overworld<br>
     * &emsp;<sub>(<code>319 - -64 + 1 = 384</code>)</sub>
     *
     * @return Full height of the world
     */
    public abstract int getHeight();

    /**
     * Get world border's minimum X coordinate.
     *
     * @return Minimum X coordinate
     */
    public abstract double getBorderMinX();

    /**
     * Get world border's minimum Z coordinate.
     *
     * @return Minimum z coordinate
     */
    public abstract double getBorderMinZ();

    /**
     * Get world border's maximum X coordinate.
     *
     * @return Maximum X coordinate
     */
    public abstract double getBorderMaxX();

    /**
     * Get world border's maximum Z coordinate.
     *
     * @return Maximum Z coordinate
     */
    public abstract double getBorderMaxZ();

    /**
     * Get the world's seed, but hashed.
     *
     * <p>Uses the server's internal hashing method.
     *
     * @param seed World's seed
     * @return World's hashed seed
     */
    public abstract long hashSeed(long seed);

    /**
     * Get the biome registry.
     *
     * @return The biome registry
     */
    @NotNull
    public abstract BiomeRegistry getBiomeRegistry();

    /**
     * Get the renderer registry.
     *
     * @return The renderer registry
     */
    @NotNull
    public abstract RendererRegistry getRendererRegistry();

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        World other = (World) o;
        return getLevel() == other.getLevel();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    @NotNull
    public String toString() {
        return "World["
            + "name=" + getName()
            + ",seed=" + getSeed()
            + ",spawn=" + getSpawn()
            + ",type=" + getType()
            + "]";
    }

    /**
     * Atomically check if world has been unloaded and discarded.
     *
     * @return True if discarded
     */
    public boolean isDiscarded() {
        return this.discarded.get();
    }

    /**
     * Discard objects in memory for this world.
     */
    public void discard() {
        if (!this.discarded.compareAndSet(false, true)) {
            return; // already discarded
        }

        Logger.debug("World discarded, clearing data structures.");

        this.regionCache.invalidateAll();
        this.pendingRegions.clear();
        getBiomeRegistry().clear();
        getRendererRegistry().clear();
    }

    /**
     * Represents a custom command argument for our world type.
     */
    public static class Argument implements ArgumentType<World> {
        public static final SimpleCommandExceptionType ERROR_WORLD_NOT_FOUND = new SimpleCommandExceptionType(() -> Lang.ERROR_WORLD_NOT_FOUND);
        public static final SimpleCommandExceptionType ERROR_MISSING_WORLD = new SimpleCommandExceptionType(() -> Lang.ERROR_MISSING_WORLD);

        @Override
        @NotNull
        public World parse(@NotNull StringReader reader) throws CommandSyntaxException {
            String input = StringArgumentType.greedyString().parse(reader);
            World world = LiveMap.api().getWorldRegistry().get(input);
            if (world == null) {
                throw ERROR_WORLD_NOT_FOUND.create();
            }
            return world;
        }

        @Override
        @NotNull
        public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
            for (var entry : LiveMap.api().getWorldRegistry().entrySet()) {
                if (entry.getKey().startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(entry.getKey());
                }
                if (entry.getValue().getName().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase())) {
                    builder.suggest(entry.getValue().getName());
                }
            }
            return builder.buildFuture();
        }

        /**
         * Gets the native type that this argument uses,
         * the type that is sent to the client.
         *
         * @return native argument type
         */
        @NotNull
        public ArgumentType<String> getNativeType() {
            return StringArgumentType.greedyString();
        }
    }

    /**
     * Represents a world's type.
     */
    public enum Type {
        /**
         * The overworld (normal) world type.
         */
        OVERWORLD,
        /**
         * The nether world type.
         */
        NETHER,
        /**
         * The end world type.
         */
        THE_END,
        /**
         * Custom world type (non-vanilla).
         */
        CUSTOM;

        private final String name;

        Type() {
            this.name = name().toLowerCase(Locale.ROOT);
        }

        /**
         * Get the world type from a server level.
         *
         * @param dimension dimension name
         * @return world type
         */
        @NotNull
        public static Type get(@NotNull String dimension) {
            return switch (dimension) {
                case "minecraft:overworld" -> OVERWORLD;
                case "minecraft:the_nether" -> NETHER;
                case "minecraft:the_end" -> THE_END;
                default -> CUSTOM;
            };
        }

        @Override
        @NotNull
        public String toString() {
            return this.name;
        }
    }
}
