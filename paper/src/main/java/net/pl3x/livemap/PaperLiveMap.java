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

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import net.pl3x.livemap.command.LiveMapCommand;
import net.pl3x.livemap.command.PaperSource;
import net.pl3x.livemap.command.argument.PaperArgumentParser;
import net.pl3x.livemap.configuration.BlocksConfig;
import net.pl3x.livemap.configuration.ColorsConfig;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.httpd.HttpdServer;
import net.pl3x.livemap.player.PlayerRegistry;
import net.pl3x.livemap.scheduler.Scheduler;
import net.pl3x.livemap.thread.WorkerThreadFactory;
import net.pl3x.livemap.util.FileUtil;
import net.pl3x.livemap.world.PaperWorldRegistry;
import net.pl3x.livemap.world.block.PaperBlockRegistry;
import net.pl3x.livemap.world.chunk.ChunkLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperLiveMap extends JavaPlugin implements LiveMap {
    private Path webDir;
    private Path tilesDir;

    private final PaperBlockRegistry blockRegistry;
    private final PlayerRegistry playerRegistry;
    private final PaperWorldRegistry worldRegistry;

    private final ChunkLoader chunkLoader;
    private final Scheduler scheduler;
    private final PaperArgumentParser argumentParser;

    private ExecutorService executor;
    private HttpdServer httpdServer;
    private Metrics metrics;

    public PaperLiveMap() {
        super();
        Provider.api = this;
        Logger.logger = getLogger();

        this.blockRegistry = new PaperBlockRegistry();
        this.playerRegistry = new PlayerRegistry();
        this.worldRegistry = new PaperWorldRegistry();

        this.chunkLoader = new ChunkLoader();
        this.scheduler = new Scheduler();
        this.argumentParser = new PaperArgumentParser();
    }

    @Override
    public void onEnable() {
        Logger.info("   &3╻  ╻╻ ╻┏━╸&9┏┳┓┏━┓┏━┓");
        Logger.info("   &3┃  ┃┃┏┛┣╸ &9┃┃┃┣━┫┣━┛");
        Logger.info("   &3┗━╸╹┗┛ ┗━╸&9╹ ╹╹ ╹╹  ");
        Logger.info("&d%22s".formatted(getVersion()));

        saveDefaultConfig();

        // main configs
        Config.reload();
        Lang.reload();

        // calculate directories
        Path dir = Path.of(Config.WEB_DIR);
        this.webDir = dir.isAbsolute() ? dir : getDataPath().resolve(dir);
        this.tilesDir = getWebDir().resolve("tiles");

        // web dir has to extract before colors config to load biome colors correctly
        FileUtil.extractDir("/web/", getWebDir(), !Config.WEB_DIR_READONLY);

        // other configs
        BlocksConfig.reload();
        ColorsConfig.reload();

        Logger.info("Gathering information for registries...");

        // build registries
        getBlockRegistry().rebuild();
        getWorldRegistry().rebuild();

        // internal webserver
        this.httpdServer = new HttpdServer();
        this.httpdServer.start();

        // register commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register(
                new LiveMapCommand<>(PaperSource.getConverter()).build(),
                "LiveMap command. '/map help'",
                List.of("map")
            )
        );

        // tick our scheduler with the server
        Bukkit.getScheduler().runTaskTimer(this,
            () -> getScheduler().tick(), 1, 1);

        // thread pool executor service
        this.executor = WorkerThreadFactory.createService("Renderer", Config.RENDER_THREADS);

        // bStats metrics
        this.metrics = new Metrics();
    }

    @Override
    public void onDisable() {
        if (this.metrics != null) {
            this.metrics.shutdown();
            this.metrics = null;
        }

        if (getExecutor() != null) {
            this.executor.shutdownNow();
            this.executor = null;
        }

        Bukkit.getScheduler().cancelTasks(this);

        if (getHttpdServer() != null) {
            this.httpdServer.stop();
            this.httpdServer = null;
        }

        // clear registries
        getBlockRegistry().clear();
        getWorldRegistry().clear();
    }

    @Override
    @NotNull
    public String getVersion() {
        return "v%s".formatted(getPluginMeta().getVersion());
    }

    @Override
    @NotNull
    public String getPlatformName() {
        return getServer().getName();
    }

    @Override
    @NotNull
    public String getPlatformVersion() {
        return getServer().getVersion();
    }

    @Override
    public boolean getOnlineMode() {
        return getServer().getOnlineMode();
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
    @Nullable
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
    public PlayerRegistry getPlayerRegistry() {
        return this.playerRegistry;
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
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    @Nullable
    public ExecutorService getExecutor() {
        return this.executor;
    }

    @Override
    @NotNull
    public PaperArgumentParser getArgumentParser() {
        return this.argumentParser;
    }
}
