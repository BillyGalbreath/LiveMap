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
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.thread.WorkerThreadFactory;
import net.pl3x.livemap.thread.WorkerThreadPool;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.region.Region;
import net.pl3x.livemap.world.region.RegionQueue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the main task to loop over the worlds and render them one at a time.
 */
public class RenderScheduler {
    private final WorkerThreadPool executor;
    private ScheduledFuture<?> future;

    private volatile Thread runningThread;
    private final AtomicBoolean manualRunning = new AtomicBoolean(false);
    private final Object runLock = new Object();

    private long nextRun;

    private int tmpVar;

    /**
     * Constructs a new instance of RenderScheduler.
     */
    public RenderScheduler() {
        this.executor = WorkerThreadFactory.createExecutor("RendererScheduler");
    }

    /**
     * Start the render scheduler loop when the plugin loads.
     */
    public void start() {
        // check if already started
        if (this.future != null) {
            Logger.warn("Render scheduler is already started. Cannot start again.");
            return;
        }

        // start task to run every second
        this.future = this.executor.scheduleAtFixedRateAsync(
            () -> {
                // task
                Logger.debug("########## Tick Start");
                run(false);
            },
            (e) -> {
                // callback
                if (e != null) {
                    Logger.error("Error running scheduled render task", e);
                }
            }
        );

        Future.State state = this.future.state();
        Logger.debug("Started render scheduler: %s".formatted(state.name()));
    }

    /**
     * Stop the render scheduler loop when the plugin unloads.
     */
    public void stop() {
        if (this.runningThread != null) {
            this.runningThread.interrupt();
        }

        if (this.future != null) {
            boolean result = this.future.cancel(true);
            Future.State state = this.future.state();
            if (result) {
                Logger.debug("Successfully stopped render scheduler: %b".formatted(state));
            } else {
                Logger.debug("Could not stop render scheduler: %b".formatted(state));
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
        if (!this.manualRunning.compareAndSet(false, true)) {
            // render is already manually running
            Logger.debug("Already manually running");
            return null;
        }

        //
        Thread thread = this.runningThread;
        if (thread != null) {
            // stop current scheduled run
            Logger.debug("Interrupt");
            thread.interrupt();
        }

        // manually run
        Logger.debug("--- Trigger ---");
        return this.executor.submit(() -> run(true));
    }

    private void run(boolean manual) {
        Logger.debug("Run");
        if (!manual) {
            synchronized (this.runLock) {
                long now = System.currentTimeMillis();

                if (this.manualRunning.get() || this.runningThread != null || this.nextRun > now) {
                    Logger.debug("Skip or Wait");
                    return;
                }

                this.runningThread = Thread.currentThread();
                this.nextRun = now + TimeUnit.SECONDS.toMillis(5);
            }
        }

        // start
        Logger.debug("Start !!! " + ++tmpVar);
        try {
            // snapshot to prevent possible CME
            List<World> worlds = new ArrayList<>(LiveMap.api().getWorldRegistry().values());

            // check each world on by one
            worlds.forEach(this::checkWorld);
        } catch (Exception e) {
            Logger.error("Error during render loop", e);
        } finally {
            Logger.debug("Done @@@ " + tmpVar);

            // finished - cleanup
            this.runningThread = null;
            if (manual) {
                this.manualRunning.set(false);
            }
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
            // sort queue around center point
            queue.sort(world.getCenter());

            Long index = queue.pop();
            if (index == null) {
                // sanity check; should not happen
                continue;
            }

            Region region = world.getRegion(index);
            //
            // todo
            //
        }
    }
}
