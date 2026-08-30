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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.thread.WorkerThreadFactory;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.region.RegionQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the main task to loop over the worlds and render them one at a time.
 */
public class RenderScheduler {
    private final ForkJoinPool executor;
    private ScheduledFuture<?> future;

    private volatile Thread runningThread;
    private volatile boolean manualRunning;

    private long nextRun;

    /**
     * Constructs a new instance of RenderManager.
     */
    public RenderScheduler() {
        this.executor = WorkerThreadFactory.createExecutor("RendererScheduler");
    }

    /**
     * Start the render manager loop when the plugin loads.
     */
    public void start() {
        // check if already started
        if (this.future != null) {
            Logger.warn("Render manager is already started. Cannot start again.");
            return;
        }

        // start task to run every second
        this.future = this.executor.scheduleWithFixedDelay(
            () -> run(false), 1, 1, TimeUnit.SECONDS);

        Future.State state = this.future.state();
        Logger.debug("Started render manager: %s".formatted(state.name()));
    }

    /**
     * Stop the render manager loop when the plugin unloads.
     */
    public void stop() {
        if (this.runningThread != null) {
            this.runningThread.interrupt();
        }

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
     * Trigger the run task right now.
     *
     * <p>If a scheduled run is already running, it will be
     * interrupted before starting this manual trigger.
     *
     * <p>If a manual run is already running, this method
     * will return null without interrupting the run.
     *
     * @return Future scheduled to manually run now, or null
     */
    @Nullable
    public ForkJoinTask<?> trigger() {
        if (this.manualRunning) {
            // render is already manually running
            return null;
        }

        if (this.runningThread != null) {
            // stop current scheduled run
            this.runningThread.interrupt();
        }

        // manually run
        return this.executor.submit(() -> run(true));
    }

    private void run(boolean manual) {
        if (this.manualRunning || this.runningThread != null) {
            // skip this run, wait for next
            return;
        }

        if (manual) {
            // mark as manually running
            this.manualRunning = true;
        } else {
            // check if we need to wait
            long now = System.currentTimeMillis();
            if (this.nextRun > now) {
                return;
            }

            // save thread
            this.runningThread = Thread.currentThread();

            // schedule next run
            this.nextRun = now + TimeUnit.SECONDS.toMillis(5);
        }

        // start
        try {
            // snapshot to prevent possible CME
            List<World> worlds = new ArrayList<>(LiveMap.api().getWorldRegistry().values());

            // check each world on by one
            worlds.forEach(this::checkWorld);
        } catch (Throwable ignore) {
        }

        // finished - cleanup
        this.manualRunning = false;
        this.runningThread = null;
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
            //
        }
    }
}
