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

import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.nio.file.Path;
import java.util.List;
import net.pl3x.livemap.command.LiveMapCommand;
import net.pl3x.livemap.command.PaperSource;
import net.pl3x.livemap.command.argument.PaperArgumentParser;
import net.pl3x.livemap.player.PlayerRegistry;
import net.pl3x.livemap.world.PaperWorldRegistry;
import net.pl3x.livemap.world.block.PaperBlockRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class PaperLiveMap extends JavaPlugin implements LiveMap {
    private final PaperBlockRegistry blockRegistry;
    private final PlayerRegistry playerRegistry;
    private final PaperWorldRegistry worldRegistry;

    private final PaperArgumentParser argumentParser;

    private boolean alreadyRegisteredCommands;

    public PaperLiveMap() {
        super();
        Provider.api = this;
        Logger.logger = getLogger();

        this.blockRegistry = new PaperBlockRegistry();
        this.playerRegistry = new PlayerRegistry();
        this.worldRegistry = new PaperWorldRegistry();

        this.argumentParser = new PaperArgumentParser();
    }

    @Override
    public void onEnable() {
        enable();
    }

    @Override
    public void onDisable() {
        disable();
    }

    @Override
    public void registerCommands() {
        if (this.alreadyRegisteredCommands) {
            return;
        }
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
            event.registrar().register(
                new LiveMapCommand<>(PaperSource.getConverter()).build(),
                "LiveMap command. '/map help'",
                List.of("map")
            )
        );
        this.alreadyRegisteredCommands = true;
    }

    @Override
    public void registerTickScheduler() {
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getScheduler().runTaskTimer(this,
            () -> getTickScheduler().tick(), 1, 1);
    }

    @Override
    public void unregisterTickScheduler() {
        Bukkit.getScheduler().cancelTasks(this);
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
        // grab it manually because Paper tags on extra redundant information :3
        String version = ServerBuildInfo.buildInfo().asString(ServerBuildInfo.StringRepresentation.VERSION_SIMPLE);
        // remove everything after and including the second hyphen (git commit hash)
        return version.substring(0, version.indexOf("-", version.indexOf("-") + 1));
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
    public PaperArgumentParser getArgumentParser() {
        return this.argumentParser;
    }
}
