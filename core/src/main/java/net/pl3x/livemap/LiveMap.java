package net.pl3x.livemap;

import java.nio.file.Path;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.world.WorldRegistry;
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
     * Get the logger.
     *
     * @return LiveMap's logger
     */
    @NotNull
    ComponentLogger getComponentLogger();

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
     * Get the world registry.
     *
     * @return The world registry
     */
    @NotNull
    WorldRegistry getWorldRegistry();
}
