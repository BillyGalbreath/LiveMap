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
import java.util.concurrent.ExecutorService;
import net.pl3x.livemap.command.argument.ArgumentParser;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.player.PlayerRegistry;
import net.pl3x.livemap.scheduler.Scheduler;
import net.pl3x.livemap.world.WorldRegistry;
import net.pl3x.livemap.world.block.BlockRegistry;
import net.pl3x.livemap.world.chunk.ChunkLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Check if plugin is enabled.
     *
     * @return True if enabled
     */
    boolean isEnabled();

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
    @Nullable
    HttpdServer getHttpdServer();

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
     * Get the chunk loader.
     *
     * @return The chunk loader
     */
    @NotNull
    ChunkLoader getChunkLoader();

    /**
     * Get the task scheduler.
     *
     * @return The task scheduler
     */
    @NotNull
    Scheduler getScheduler();

    /**
     * Get the executor service (a.k.a., thread pool).
     *
     * @return The executor service
     */
    @Nullable
    ExecutorService getExecutor();

    /**
     * Command custom argument parser.
     *
     * @return Custom argument parser
     */
    @NotNull
    ArgumentParser getArgumentParser();
}
