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

package net.pl3x.livemap.render;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.region.RegionQueue;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an executor to loop over the worlds and render them one at a time.
 */
public class RenderManager {
    private ScheduledFuture<?> future;

    /**
     * Start the render manager loop when the plugin loads.
     */
    public void start() {
        // check if already started
        if (this.future != null) {
            Logger.warn("Render manager is already started. Cannot start again.");
            return;
        }

        // start task to run every 60 seconds
        this.future = LiveMap.api().getExecutor().scheduleAtFixedRate(() -> {
            // snapshot to prevent possible CME
            List<World> worlds = new ArrayList<>(LiveMap.api().getWorldRegistry().values());
            // check each world on by one
            worlds.forEach(this::checkWorld);
        }, 0, 60, TimeUnit.SECONDS);

        Future.State state = this.future.state();
        Logger.debug("Started render manager: %b".formatted(state));
    }

    /**
     * Stop the render manager loop when the plugin unloads.
     */
    public void stop() {
        if (this.future != null) {
            boolean result = this.future.cancel(true);
            Future.State state = this.future.state();
            if (result) {
                Logger.debug("Successfully stopped render manager: %b".formatted(state));
            } else {
                Logger.debug("Could not stop render manager: %b".formatted(state));
            }
            this.future = null;
        }
    }

    /**
     * Check if world has any queued regions waiting to render.
     *
     * @param world World to check
     */
    public void checkWorld(@NotNull World world) {
        RegionQueue queue = world.getRegionQueue();
        while (!queue.isEmpty()) {
            Long index = queue.poll();
            //
            // todo
        }
    }
}
