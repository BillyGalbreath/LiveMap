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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.configuration.WorldConfig;
import net.pl3x.livemap.render.renderer.Renderer;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a task to write settings JSON data to disk on a schedule.
 */
public class UpdateSettingsTask extends JsonFileTask {
    /**
     * Creates a new instance of UpdateSettingsTask.
     */
    public UpdateSettingsTask() {
        super(LiveMap.api().getTilesDir().resolve("settings.json"));
    }

    @Override
    @NotNull
    protected String createJson() {
        return GSON.toJson(mapOf(
            "minecraft", "26.2",
            "max_players", LiveMap.api().getMaxPlayers(),
            "update_interval", 30,
            "friendly_urls", true,
            "format", Config.WEB_TILE_FORMAT,
            "lang", lang(),
            "ui", ui(),
            "worlds", worlds()
        ));
    }

    @NotNull
    private Map<String, Object> lang() {
        return mapOf(
            "locale", Config.LANGUAGE_LOCALE,
            "title", Config.WEBSITE_TITLE,
            "attribution", Config.WEBSITE_ATTRIBUTION,
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

    @NotNull
    private Map<String, Object> ui() {
        return mapOf(
            "logo", Config.WEBSITE_LOGO,
            "blockinfo", "bottomleft",
            "coords", "bottomcenter",
            "link", "bottomleft",
            "scale", "topleft",
            "sidebar", "unpinned"
        );
    }

    @NotNull
    private List<Map<String, Object>> worlds() {
        List<Map<String, Object>> json = new ArrayList<>();
        LiveMap.api().getWorldRegistry().forEach((_, world) -> {
            Map<String, Object> worldEntry = worldEntry(world);
            if (worldEntry != null) {
                json.add(worldEntry);
            }
        });
        return json;
    }

    @Nullable
    private Map<String, Object> worldEntry(@NotNull World world) {
        if (!world.isEnabled() || world.isDiscarded()) {
            return null; // skip world
        }

        WorldConfig config = world.getConfig();

        Map<String, Object> worldEntry = new LinkedHashMap<>();
        worldEntry.put("name", world.getName());
        worldEntry.put("display_name", config.NAME);
        worldEntry.put("type", world.getType());
        worldEntry.put("order", config.ORDER);
        worldEntry.put("spawn", world.getSpawn());
        worldEntry.put("center", world.getCenter());
        worldEntry.put("zooms", mapOf(
            "default", config.ZOOM_DEFAULT,
            "max_out", config.ZOOM_MAX_OUT,
            "max_in", config.ZOOM_MAX_IN
        ));
        worldEntry.put("renderers", renderers(world));

        // todo - write world specific settings to world directory?

        return worldEntry;
    }

    @NotNull
    private List<Map<String, Object>> renderers(@NotNull World world) {
        List<Map<String, Object>> renderers = new ArrayList<>();

        world.getRendererRegistry().forEach((_, renderer) -> {
            if (renderer.getType() == Renderer.BLOCKINFO) {
                return; // do not send blockinfo renderer here
            }
            Map<String, Object> rendererEntry = new LinkedHashMap<>();
            rendererEntry.put("id", renderer.getId());
            // rendererEntry.put("type", renderer.getType().getId());
            rendererEntry.put("name", renderer.getName());
            rendererEntry.put("icon", renderer.getIcon());
            // the browser doesn't need any of this
            /* rendererEntry.put("heightmap", renderer.getHeightmapType().getId());
            rendererEntry.put("biome_blend", renderer.getBiomeBlend());
            rendererEntry.put("translucent_fluids", renderer.isTranslucentFluids());
            rendererEntry.put("sprinkles", renderer.isSprinkles());*/
            renderers.add(rendererEntry);
        });

        return renderers;
    }
}
