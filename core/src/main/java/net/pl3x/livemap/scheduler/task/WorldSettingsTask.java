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

import net.pl3x.livemap.render.renderer.Renderer;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a task to write world settings to disk as JSON on a schedule.
 */
public class WorldSettingsTask extends JsonFileTask {
    private final World world;

    /**
     * Constructs a new instance of WorldSettingsTask.
     *
     * @param world World for this task
     */
    public WorldSettingsTask(@NotNull World world) {
        super(world.getTilesDir().resolve("settings.json"));
        this.world = world;
    }

    @Override
    protected @NotNull String createJson() {
        return GSON.toJson(mapOf(
            "id", this.world.getId(),
            "display_name", this.world.getDisplayName(),
            "order", this.world.getOrder(),
            "type", this.world.getType().toString(),
            "center", this.world.getCenter(),
            "spawn", this.world.getSpawn(),
            "zooms", mapOf(
                "default", this.world.getConfig().ZOOM_DEFAULT,
                "max_out", this.world.getConfig().ZOOM_MAX_OUT,
                "max_in", this.world.getConfig().ZOOM_MAX_IN
            ),
            "renderers", this.world.getRendererRegistry().values().stream()
                .filter(renderer -> renderer.getType() != Renderer.BLOCKINFO)
                .map(renderer -> this.<String, Object>mapOf(
                    "id", renderer.getId(),
                    "name", renderer.getName(),
                    "icon", renderer.getIcon()
                )).toList()
        ));
    }
}
