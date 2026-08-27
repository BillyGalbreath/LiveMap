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

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.nio.file.Path;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.scheduler.Scheduler;
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
     * Get the version of LiveMap.
     *
     * @return LiveMap's version
     */
    @NotNull
    String getVersion();

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
     * Get the task scheduler.
     *
     * @return The task scheduler
     */
    @NotNull
    Scheduler getScheduler();

    /**
     * Convenience methods to make using custom command arguments a little less painful.
     *
     * @return Instance of Args class
     */
    @NotNull
    Args args();

    /**
     * Convenience methods to make using custom command arguments a little less painful.
     */
    interface Args {
        /**
         * Create a new world argument with the name "world".
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
