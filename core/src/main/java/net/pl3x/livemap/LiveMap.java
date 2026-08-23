package net.pl3x.livemap;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.nio.file.Path;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.WorldRegistry;
import net.pl3x.livemap.world.block.BlockRegistry;
import net.pl3x.livemap.world.chunk.ChunkLoader;
import org.jetbrains.annotations.NotNull;

/**
 * The LiveMap API.
 */
public interface LiveMap {
    /**
     * The LiveMap instance provider.
     */
    final class Provider {
        static LiveMap api;

        private Provider() {
        }
    }

    /**
     * Get instance of the LiveMap API.
     *
     * @return LiveMap instance
     */
    @NotNull
    static LiveMap api() {
        return Provider.api;
    }

    /**
     * Get the path that LiveMap data files are located in.
     *
     * @return Path to data directory
     */
    @NotNull
    Path getDataPath();

    /**
     * Get the path to the web directory.
     *
     * @return Path to web directory
     */
    @NotNull
    Path getWebDir();

    /**
     * Get the path to the tiles directory.
     *
     * @return Path to tiles directory
     */
    @NotNull
    Path getTilesDir();

    /**
     * Get the internal web server.
     *
     * @return The internal web server
     */
    @NotNull
    HttpdServer getHttpdServer();

    /**
     * Get the block registry.
     *
     * @return The block registry
     */
    @NotNull
    BlockRegistry getBlockRegistry();

    /**
     * Get the world registry.
     *
     * @return The world registry
     */
    @NotNull
    WorldRegistry getWorldRegistry();

    /**
     * Get the chunk loader.
     *
     * @return The chunk loader
     */
    @NotNull
    ChunkLoader getChunkLoader();

    /**
     * Convenience methods to make using custom command arguments a little less painful
     *
     * @return Instance of Args class
     */
    @NotNull
    Args args();

    /**
     * Convenience methods to make using custom command arguments a little less painful
     */
    interface Args {
        /**
         * Create a new world argument with the name "world"
         *
         * @param <S> Command source type
         * @return World argument
         */
        @NotNull
        default <S> ArgumentBuilder<S, RequiredArgumentBuilder<S, World>> world() {
            return world("world");
        }

        /**
         * Create a new world argument with the specified name.
         *
         * @param name Name of the argument
         * @param <S>  Command source type
         * @return World argument
         */
        @NotNull
        <S> ArgumentBuilder<S, RequiredArgumentBuilder<S, World>> world(@NotNull String name);
    }
}
