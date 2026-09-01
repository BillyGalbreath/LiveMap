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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.render.iterator.RegionSpiralIterator;
import net.pl3x.livemap.render.iterator.SpiralIterator;
import net.pl3x.livemap.thread.WorkerThreadFactory;
import net.pl3x.livemap.thread.WorkerThreadPool;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the main task to loop over the worlds and render them one at a time.
 */
public class RenderScheduler {
    public static final CompletableFuture<?>[] EMPTY_FUTURE_ARRAY = new CompletableFuture[0];

    private final WorkerThreadPool worldExecutor;
    private final WorkerThreadPool regionExecutor;
    private ScheduledFuture<?> future;

    private final AtomicBoolean manualRunning = new AtomicBoolean(false);
    private final AtomicReference<Thread> runningThread = new AtomicReference<>(null);
    private final Object renderMutex = new Object();

    /**
     * Constructs a new instance of RenderScheduler.
     */
    public RenderScheduler() {
        this.worldExecutor = WorkerThreadFactory.createExecutor("RendererScheduler");
        this.regionExecutor = WorkerThreadFactory.createExecutor("RenderRegion", Config.RENDER_THREADS);
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
        this.future = this.worldExecutor.scheduleAtFixedRateAsync(
            () -> {
                // task
                run(false);
            },
            (e) -> {
                // callback
                if (e != null) {
                    Logger.error("Error running scheduled render task", e);
                }
            },
            10, // wait 10 seconds before starting
            60, // check every 60 seconds
            TimeUnit.SECONDS
        );

        Future.State state = this.future.state();
        Logger.debug("Started render scheduler: %s".formatted(state.name()));
    }

    /**
     * Stop the render scheduler loop when the plugin unloads.
     */
    public void stop() {
        // safely stop current running thread
        interruptRunningThread();

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
        // atomically set manualRunning to true ONLY if it was false
        if (!this.manualRunning.compareAndSet(false, true)) {
            return null;
        }

        // safely stop current running thread
        interruptRunningThread();

        // manually run
        return this.worldExecutor.submit(() -> run(true));
    }

    /**
     * Capture and interrupt the thread safely without blocking execution.
     */
    private void interruptRunningThread() {
        Thread activeThread = this.runningThread.get();
        if (activeThread != null) {
            Logger.debug("Interrupting active scheduled render thread");
            activeThread.interrupt();
        }
    }

    /**
     * The loop.
     *
     * @param manual True if manually triggered instead of scheduled
     */
    private void run(boolean manual) {
        if (!manual) {
            // set runningThread safely if no other thread beat us to it
            if (this.manualRunning.get() || !this.runningThread.compareAndSet(null, Thread.currentThread())) {
                return;
            }
        }

        // lock strictly the execution context, keeping trigger() unblocked
        synchronized (this.renderMutex) {
            Logger.debug("Checking worlds for pending regions");
            try {
                // snapshot to prevent possible CME
                List<World> worlds = List.copyOf(LiveMap.api().getWorldRegistry().values());

                for (World world : worlds) {
                    // check if current thread was interrupted by trigger()
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    // protect against a world that was discarded midway through the loop
                    if (world.isDiscarded()) {
                        continue;
                    }

                    // safely process world
                    this.renderWorld(world);
                }
            } catch (Exception e) {
                Logger.error("Error during render task", e);
            } finally {
                // finished - cleanup
                if (manual) {
                    this.manualRunning.set(false);
                } else {
                    this.runningThread.compareAndSet(Thread.currentThread(), null);
                }

                // clear interrupted status flag to prevent thread-reuse state pollution in the pool
                // noinspection ResultOfMethodCallIgnored
                Thread.interrupted();
            }
        }
    }

    /**
     * Render world if there are any queued regions waiting to render.
     *
     * @param world World to render
     */
    private void renderWorld(@NotNull World world) {
        Set<Long> pending = world.getPendingRegions();
        if (pending.isEmpty()) {
            return;
        }

        Logger.debug("Begin rendering %d pending region(s) for %s".formatted(pending.size(), world.getName()));

        // track active jobs so we can wait for this world to finish all queued regions
        List<CompletableFuture<Void>> runningRenders = new ArrayList<>();
        final Thread worldThread = Thread.currentThread();

        // the hasNext supplier keeps the iterator bounded by active queue allocations and live states
        SpiralIterator spiral = new RegionSpiralIterator(world.getCenter(), () ->
            !pending.isEmpty() && !world.isDiscarded() && !worldThread.isInterrupted()
        );

        // drive the spiral loop sequentially on the world thread
        while (spiral.hasNext()) {
            long index = spiral.next();

            // atomically remove index from queue or skip if it was not queued
            if (!pending.remove(index)) {
                continue;
            }

            // Offload the validated region task onto the multithreaded worker pool
            CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
                try {
                    // double check world state and interruptions
                    if (world.isDiscarded() || worldThread.isInterrupted()) {
                        return;
                    }

                    // render the region
                    this.renderRegion(world.getRegion(index), worldThread);
                } catch (Exception e) {
                    Logger.error("Failed executing parallel render task for region index: " + index, e);
                }
            }, this.regionExecutor);

            runningRenders.add(task);
        }

        // block until all render threads are finished
        try {
            CompletableFuture.allOf(runningRenders.toArray(EMPTY_FUTURE_ARRAY)).join();
        } catch (Exception e) {
            Logger.error("Error awaiting regional worker tasks in " + world.getName(), e);
        }

        Logger.debug("Finished rendering for %s".formatted(world.getName()));
    }

    /**
     * Render specified region.
     *
     * @param region       Region to render
     * @param parentThread The parent thread that spawned this render
     */
    private void renderRegion(@NotNull Region region, @NotNull Thread parentThread) {
        for (int chunkX = 0; chunkX < 32; chunkX++) {
            for (int chunkZ = 0; chunkZ < 32; chunkZ++) {
                // check world state and interruptions, for instant responsiveness
                if (region.getWorld().isDiscarded() || parentThread.isInterrupted()) {
                    return;
                }

                //
                // todo
                //
            }
        }
    }
}
