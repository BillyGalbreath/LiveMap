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

package net.pl3x.livemap;

import java.nio.file.Path;
import net.pl3x.livemap.command.argument.ArgumentParser;
import net.pl3x.livemap.configuration.BlocksConfig;
import net.pl3x.livemap.configuration.ColorsConfig;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.player.PlayerRegistry;
import net.pl3x.livemap.render.RenderScheduler;
import net.pl3x.livemap.scheduler.TickScheduler;
import net.pl3x.livemap.util.FileUtil;
import net.pl3x.livemap.world.WorldRegistry;
import net.pl3x.livemap.world.block.BlockRegistry;
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

        private static Path webDir;
        private static Path tilesDir;

        private static HttpdServer httpdServer;
        private static RenderScheduler renderScheduler;
        private static TickScheduler tickScheduler;

        private static Metrics metrics;

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
     * Check if plugin is enabled.
     *
     * @return True if enabled
     */
    boolean isEnabled();

    /**
     * Register brigadier commands with the server platform.
     */
    void registerCommands();

    /**
     * Register tick scheduler with server platform.
     */
    void registerTickScheduler();

    /**
     * Unregister tick scheduler with server platform.
     */
    void unregisterTickScheduler();

    /**
     * Get the version of LiveMap.
     *
     * @return LiveMap's version
     */
    @NotNull
    String getVersion();

    /**
     * Get the name of the platform (Paper, Fabric, etc.).
     *
     * @return Name of platform
     */
    @NotNull
    String getPlatformName();

    /**
     * Get the version of the platform (1.21.11, 26.2, etc.).
     *
     * @return Version of platform
     */
    @NotNull
    String getPlatformVersion();

    /**
     * Get the server's online mode.
     *
     * @return Online mode
     */
    boolean getOnlineMode();

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
    default Path getWebDir() {
        return Provider.webDir;
    }

    /**
     * Get the path to the tiles directory.
     *
     * @return Path to tiles directory
     */
    @NotNull
    default Path getTilesDir() {
        return Provider.tilesDir;
    }

    /**
     * Get the internal web server.
     *
     * @return The internal web server
     */
    @NotNull
    default HttpdServer getHttpdServer() {
        if (Provider.httpdServer == null) {
            Provider.httpdServer = new HttpdServer();
        }
        return Provider.httpdServer;
    }

    /**
     * Get the render scheduler.
     *
     * @return The render scheduler
     */
    @NotNull
    default RenderScheduler getRenderScheduler() {
        if (Provider.renderScheduler == null) {
            Provider.renderScheduler = new RenderScheduler();
        }
        return Provider.renderScheduler;
    }

    /**
     * Get the tick scheduler.
     *
     * @return The tick scheduler
     */
    @NotNull
    default TickScheduler getTickScheduler() {
        if (Provider.tickScheduler == null) {
            Provider.tickScheduler = new TickScheduler();
        }
        return Provider.tickScheduler;
    }

    /**
     * Get the block registry.
     *
     * @return The block registry
     */
    @NotNull
    BlockRegistry getBlockRegistry();

    /**
     * Get the player registry.
     *
     * @return The player registry
     */
    @NotNull
    PlayerRegistry getPlayerRegistry();

    /**
     * Get the world registry.
     *
     * @return The world registry
     */
    @NotNull
    WorldRegistry getWorldRegistry();

    /**
     * Command custom argument parser.
     *
     * @return Custom argument parser
     */
    @NotNull
    ArgumentParser getArgumentParser();

    /**
     * Enables LiveMap.
     */
    default void enable() {
        // main configs
        Config.reload();
        Lang.reload();

        if (Config.STARTUP_BANNER) {
            Logger.info("   &3╻  ╻╻ ╻┏━╸&9┏┳┓┏━┓┏━┓");
            Logger.info("   &3┃  ┃┃┏┛┣╸ &9┃┃┃┣━┫┣━┛");
            Logger.info("   &3┗━╸╹┗┛ ┗━╸&9╹ ╹╹ ╹╹  ");
            // 22 spaces to line up with right side of above
            Logger.info("&a%22s".formatted(getVersion()));
            Logger.info("&b%22s".formatted("Running %s".formatted(getPlatformName())));
            Logger.info("&c%22s".formatted("v%s".formatted(getPlatformVersion())));
        }

        // calculate directories
        Path dir = Path.of(Config.WEB_DIR);
        Provider.webDir = dir.isAbsolute() ? dir : getDataPath().resolve(dir);
        Provider.tilesDir = getWebDir().resolve("tiles");

        // web dir has to extract before colors config to load biome colors correctly
        FileUtil.extractDir("/web/", getWebDir(), !Config.WEB_DIR_READONLY);

        // other configs
        BlocksConfig.reload();
        ColorsConfig.reload();

        Logger.info("Gathering information...");

        // build registries
        getBlockRegistry().rebuild();
        getWorldRegistry().rebuild();

        // internal webserver
        getHttpdServer().start();

        // register commands
        registerCommands();

        // tick scheduler with the server
        registerTickScheduler();

        // start tasks
        getRenderScheduler().start();

        // bStats metrics
        Provider.metrics = new Metrics();

        Logger.info("Finished loading");
    }

    /**
     * Disables LiveMap.
     */
    default void disable() {
        // stop bStats
        if (Provider.metrics != null) {
            Provider.metrics.shutdown();
            Provider.metrics = null;
        }

        // stop tasks
        if (Provider.renderScheduler != null) {
            Provider.renderScheduler.stop();
            Provider.renderScheduler = null;
        }

        // stop our tick scheduler
        unregisterTickScheduler();
        Provider.tickScheduler = null;

        // stop http server
        if (Provider.httpdServer != null) {
            Provider.httpdServer.stop();
            Provider.httpdServer = null;
        }

        // clear registries
        getBlockRegistry().clear();
        getWorldRegistry().clear();

        // clear remaining provider instances
        Provider.webDir = null;
        Provider.tilesDir = null;

        Logger.info("Finished unloading");
    }
}
