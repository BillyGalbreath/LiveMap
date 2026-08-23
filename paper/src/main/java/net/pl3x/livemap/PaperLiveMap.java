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
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.nio.file.Path;
import java.util.List;
import net.pl3x.livemap.command.LiveMapCommand;
import net.pl3x.livemap.command.PaperSource;
import net.pl3x.livemap.configuration.ColorsConfig;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.util.FileUtil;
import net.pl3x.livemap.world.PaperWorld;
import net.pl3x.livemap.world.PaperWorldRegistry;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.block.PaperBlockRegistry;
import net.pl3x.livemap.world.chunk.ChunkLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PaperLiveMap extends JavaPlugin implements LiveMap {
    private Path webDir;
    private Path tilesDir;

    private final PaperBlockRegistry blockRegistry = new PaperBlockRegistry();
    private final PaperWorldRegistry worldRegistry = new PaperWorldRegistry();

    private final ChunkLoader chunkLoader = new ChunkLoader();

    private final PaperArgs args = new PaperArgs();

    private HttpdServer httpdServer;
    private Metrics metrics;

    public PaperLiveMap() {
        super();
        Provider.api = this;
        Logger.logger = getLogger();
    }

    @Override
    public void onEnable() {
        Config.reload();
        Lang.reload();

        Path dir = Path.of(Config.WEB_DIR);
        this.webDir = dir.isAbsolute() ? dir : getDataPath().resolve(dir);
        this.tilesDir = getWebDir().resolve("tiles");

        // web dir has to extract before colors config to load biome colors correctly
        FileUtil.extractDir("/web/", getWebDir(), !Config.WEB_DIR_READONLY);

        ColorsConfig.reload();

        // registries
        getBlockRegistry().rebuild();
        getWorldRegistry().rebuild();

        // internal webserver
        this.httpdServer = new HttpdServer();
        getHttpdServer().start();

        // register commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register(
                new LiveMapCommand<>(PaperSource.getConverter()).build(),
                "LiveMap command. '/map help'",
                List.of("map")
            )
        );

        // scheduler

        // bstats metrics
        this.metrics = new Metrics(this, 26542);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        if (this.metrics != null) {
            this.metrics.shutdown();
            this.metrics = null;
        }

        if (this.httpdServer != null) {
            getHttpdServer().stop();
            this.httpdServer = null;
        }

        // block registry

        // biome registry

        // render registry

        // world registry
        getWorldRegistry().clear();
    }

    @Override
    @NotNull
    public Path getDataPath() {
        return super.getDataPath();
    }

    @Override
    @NotNull
    public Path getWebDir() {
        return this.webDir;
    }

    @Override
    @NotNull
    public Path getTilesDir() {
        return this.tilesDir;
    }

    @Override
    @NotNull
    public HttpdServer getHttpdServer() {
        return this.httpdServer;
    }

    @Override
    @NotNull
    public PaperBlockRegistry getBlockRegistry() {
        return this.blockRegistry;
    }

    @Override
    @NotNull
    public PaperWorldRegistry getWorldRegistry() {
        return this.worldRegistry;
    }

    @Override
    @NotNull
    public ChunkLoader getChunkLoader() {
        return this.chunkLoader;
    }

    @Override
    @NotNull
    public PaperArgs args() {
        return this.args;
    }

    // Paper will not let us use custom arguments without implementing
    // their CustomArgumentType interface so we have to grab Paper
    // specific versions from this stupid thing in order to use them.
    public static class PaperArgs implements Args {
        @Override
        @NotNull
        public <S> ArgumentBuilder<S, RequiredArgumentBuilder<S, World>> world(@NotNull String name) {
            return RequiredArgumentBuilder.argument(name, new PaperWorld.Argument());
        }
    }
}
