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

package net.pl3x.livemap.scheduler.task;

import java.util.Comparator;
import java.util.Map;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a task to write global webmap settings to disk as JSON on a schedule.
 */
public class WebmapSettingsTask extends JsonFileTask {
    /**
     * Creates a new instance of UpdateSettingsTask.
     */
    public WebmapSettingsTask() {
        super(LiveMap.api().getTilesDir().resolve("settings.json"));

        // initial run immediately
        LiveMap.api().getTickScheduler().addTask(0, this);
    }

    @Override
    @NotNull
    protected String createJson() {
        return GSON.toJson(mapOf(
            "minecraft", LiveMap.api().getPlatformVersion(),
            "max_players", LiveMap.api().getMaxPlayers(),
            "update_interval", 30,
            "friendly_urls", true,
            "format", Config.WEB_TILE_FORMAT,
            "lang", lang(),
            "worlds", LiveMap.api().getWorldRegistry().values().stream()
                .filter(World::isEnabled)
                // ensure worlds are sorted by order
                .sorted(Comparator.comparingInt(World::getOrder))
                // only give list of ids, each world has its own settings file
                .map(World::getId).toList()
        ));
    }

    @NotNull
    private Map<String, Object> lang() {
        return mapOf(
            "locale", Config.LANGUAGE_LOCALE,
            "browser.title", Lang.UI_BROWSER_TITLE,
            "sidebar.href", Lang.UI_SIDEBAR_HREF,
            "sidebar.title", Lang.UI_SIDEBAR_TITLE,
            "sidebar.logo", Lang.UI_SIDEBAR_LOGO,
            "attribution", Lang.UI_ATTRIBUTION,
            "blockinfo", mapOf(
                "label", Lang.UI_BLOCKINFO_LABEL,
                "value", Lang.UI_BLOCKINFO_VALUE,
                "unknown", mapOf(
                    "block", Lang.UI_BLOCKINFO_UNKNOWN_BLOCK,
                    "biome", Lang.UI_BLOCKINFO_UNKNOWN_BIOME
                )
            ),
            "coords", mapOf("label", Lang.UI_COORDS_LABEL, "value", Lang.UI_COORDS_VALUE),
            "link", mapOf("label", Lang.UI_LINK_LABEL, "value", Lang.UI_LINK_VALUE),
            "players", mapOf("label", Lang.UI_PLAYERS_LABEL, "value", Lang.UI_PLAYERS_VALUE),
            "worlds", mapOf("label", Lang.UI_WORLDS_LABEL, "value", Lang.UI_WORLDS_VALUE),
            "layers", mapOf("label", Lang.UI_LAYERS_LABEL, "value", Lang.UI_LAYERS_VALUE),
            "markers", mapOf("label", Lang.UI_MARKERS_LABEL, "value", Lang.UI_MARKERS_VALUE)
        );
    }
}
