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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Config;
import net.pl3x.livemap.render.image.ActiveTileCanvas;
import net.pl3x.livemap.render.image.TileCanvas;
import net.pl3x.livemap.render.image.io.IO;
import net.pl3x.livemap.render.iterator.RegionSpiralIterator;
import net.pl3x.livemap.render.renderer.Renderer;
import net.pl3x.livemap.thread.WorkerThreadFactory;
import net.pl3x.livemap.thread.WorkerThreadPool;
import net.pl3x.livemap.world.World;
import net.pl3x.livemap.world.WorldDispatcher;
import net.pl3x.livemap.world.chunk.Chunk;
import net.pl3x.livemap.world.region.Region;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the main task to loop over the worlds and render them one at a time.
 */
public class RenderScheduler {
    public static final CompletableFuture<?>[] EMPTY_FUTURE_ARRAY = new CompletableFuture[0];

    private static void debug(@NotNull String message) {
        Logger.debug("[RenderScheduler] %s".formatted(message));
    }

    private final WorkerThreadPool worldExecutor;
    private final WorkerThreadPool regionExecutor;
    private ScheduledFuture<?> future;

    private final AtomicBoolean manualRunning = new AtomicBoolean(false);
    private final AtomicReference<Thread> runningThread = new AtomicReference<>(null);
    private volatile AtomicBoolean currentCancellation; // Run-scoped token
    private final Object renderMutex = new Object();

    private final Map<Path, ActiveTileCanvas> activeCanvases = new ConcurrentHashMap<>();

    /**
     * Constructs a new instance of RenderScheduler.
     */
    public RenderScheduler() {
        this.worldExecutor = WorkerThreadFactory.createExecutor("RendererScheduler");
        this.regionExecutor = WorkerThreadFactory.createExecutor("RenderRegion", Config.RENDER_THREADS);
    }

    /**
     * Get the targeted parallelism level of the region executor.
     *
     * @return The targeted parallelism level
     */
    public int getParallelism() {
        return this.regionExecutor.getParallelism();
    }

    /**
     * Start the render scheduler loop when the plugin loads.
     */
    public synchronized void start() {
        // check if already started
        if (this.future != null) {
            Logger.warn("Render scheduler is already started. Cannot start again.");
            return;
        }

        Logger.debug("Region executor threads: %d".formatted(this.regionExecutor.getParallelism()));

        // todo - maybe make this configurable?
        final int delay = 10; // wait 10 seconds before starting
        final int period = 60; // check every 60 seconds

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
            delay,
            period,
            TimeUnit.SECONDS
        );

        Future.State state = this.future.state();
        debug("Started render scheduler: %s".formatted(state.name()));
        if (state == Future.State.RUNNING) {
            debug("Waiting %d seconds then will repeat every %d seconds".formatted(delay, period));
        }
    }

    /**
     * Stop the render scheduler loop when the plugin unloads.
     */
    public void stop() {
        // safely stop current running thread
        cancelActiveRun();

        if (this.future != null) {
            this.future.cancel(true);
            this.future = null;
        }

        this.regionExecutor.shutdownNow();
        this.worldExecutor.shutdownNow();

        this.manualRunning.set(false);

        debug("Successfully stopped render scheduler.");
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
        // ensure executor is accepting new tasks
        if (this.worldExecutor.isShutdown()) {
            return null;
        }

        // atomically set manualRunning to true ONLY if it was false
        if (!this.manualRunning.compareAndSet(false, true)) {
            return null;
        }

        // safely stop current running thread
        cancelActiveRun();

        // manually run
        return this.worldExecutor.submit(() -> run(true));
    }

    /**
     * Safely cancels both the active worker tasks and interrupts the orchestrator thread.
     */
    private void cancelActiveRun() {
        // flip the cancellation token (workers stop at next chunk)
        AtomicBoolean token = this.currentCancellation;
        if (token != null) {
            token.set(true);
        }
        // interrupt the orchestrator thread (wakes up any blocking joins/sleeps)
        Thread activeThread = this.runningThread.get();
        if (activeThread != null) {
            debug("Interrupting active render thread");
            activeThread.interrupt();
        }
    }

    /**
     * The loop.
     *
     * @param manual True if manually triggered instead of scheduled
     */
    private void run(boolean manual) {
        // lock strictly the execution context, keeping trigger() unblocked
        synchronized (this.renderMutex) {
            if (!manual && this.manualRunning.get()) {
                return; // do not run scheduled run if manually running
            }

            // create a new cancellation token and register the active thread
            AtomicBoolean cancelled = new AtomicBoolean(false);
            this.currentCancellation = cancelled;
            this.runningThread.set(Thread.currentThread());

            debug("Checking worlds for pending regions");
            try {
                renderWorlds(cancelled);
            } catch (Exception e) {
                if (!cancelled.get()) {
                    Logger.error("Error during render task", e);
                }
            } finally {
                // finished - cleanup
                this.runningThread.set(null);
                this.currentCancellation = null;
                if (manual) {
                    this.manualRunning.set(false);
                }

                // clear interrupted status flag
                // noinspection ResultOfMethodCallIgnored
                Thread.interrupted();
            }
        }
    }

    /**
     * Render worlds if there are any queued regions waiting to render.
     *
     * @param cancelled Cancellation token
     */
    private void renderWorlds(@NotNull AtomicBoolean cancelled) {
        // snapshot to prevent possible CMEs
        List<World> worlds = List.copyOf(LiveMap.api().getWorldRegistry().values());
        WorldDispatcher dispatcher = new WorldDispatcher();

        for (World world : worlds) {
            if (world.isDiscarded() || cancelled.get()) {
                continue;
            }

            LongOpenHashSet pending = world.getPendingRegions().get();
            if (pending.isEmpty()) {
                debug("No regions pending for %s".formatted(world.getName()));
                continue;
            }

            debug("Found %d pending regions for %s".formatted(pending.size(), world.getName()));

            // create the iterator, passing the pending collection
            RegionSpiralIterator spiral = new RegionSpiralIterator(world.getCenter(), pending,
                () -> !world.isDiscarded() && !cancelled.get()
            );

            // store snapshot to prevent possible CMEs
            List<Renderer> renderers = List.copyOf(world.getRendererRegistry().values());

            dispatcher.addQueue(world, renderers, spiral);
        }

        if (dispatcher.isEmpty()) {
            return;
        }

        int totalRegions = dispatcher.totalPending();
        debug("Begin rendering %d total pending region(s) across all active worlds".formatted(totalRegions));

        // number of worker tasks to spawn (bounded by thread count and region count)
        int maxThreads = Math.max(1, getParallelism());
        int tasksToSpawn = Math.min(maxThreads, totalRegions);

        // track active jobs so we can wait for all queued regions to finish
        List<CompletableFuture<Void>> workers = new ArrayList<>(tasksToSpawn);

        // spawn the tasks
        for (int i = 0; i < tasksToSpawn; i++) {
            CompletableFuture<Void> worker = CompletableFuture.runAsync(() -> {
                while (!cancelled.get()) {
                    WorldDispatcher.Ticket ticket = dispatcher.pollNext();
                    if (ticket == null) {
                        break;
                    }

                    try {
                        boolean completed = this.renderRegion(
                            ticket.world().getRegion(ticket.region()),
                            ticket.renderers(),
                            cancelled
                        );

                        // add back current region (incomplete render)
                        if (!completed && !ticket.world().isDiscarded()) {
                            ticket.world().getPendingRegions().add(ticket.region());
                        }
                    } catch (Exception e) {
                        if (!cancelled.get()) {
                            Logger.error("Failed rendering %s region %d".formatted(ticket.world().getName(), ticket.region()), e);
                        } else if (!ticket.world().isDiscarded()) {
                            // add back current region (incomplete render)
                            ticket.world().getPendingRegions().add(ticket.region());
                        }
                    }
                }
            }, this.regionExecutor);

            workers.add(worker);
        }

        // await the worker pool tasks
        try {
            CompletableFuture.allOf(workers.toArray(EMPTY_FUTURE_ARRAY)).join();
        } catch (Exception e) {
            if (!cancelled.get()) {
                Logger.error("Error awaiting multi-world render tasks", e);
            }
        }

        // workers are done. anything left in the map is a partial tile on map borders
        if (!this.activeCanvases.isEmpty()) {
            debug("Flushing incomplete edge-of-the-map canvases to disk...");

            this.activeCanvases.forEach((file, canvas) -> {
                if (canvas.hasContributions()) {
                    try {
                        IO.getType(Config.WEB_TILE_FORMAT).write(file, canvas.getImageBuffer());
                    } catch (Throwable t) {
                        Logger.error("Failed to flush partial tile: " + file, t);
                    }
                }
            });

            this.activeCanvases.clear(); // wipe map entirely clean to drop memory footprints to 0
        }

        // return remaining regions that were never processed
        if (cancelled.get()) {
            dispatcher.returnRemainingAll();
        }

        // purge the object pool references so the GC can reclaim the memory
        Chunk.clearPool();

        // nudge the jvm to run gc
        System.gc();

        debug("Finished rendering");
    }

    /**
     * Render specified region.
     *
     * @param region    Region to render
     * @param renderers Snapshot of world's renderers
     * @param cancelled Cancellation token
     * @return True if the entire region was rendered, false if aborted
     * @throws IOException if an I/O error occurs
     */
    private boolean renderRegion(@NotNull Region region, @NotNull List<Renderer> renderers, @NotNull AtomicBoolean cancelled) throws IOException {
        Logger.debug("[%s] Rendering: %d,%d".formatted(Thread.currentThread().getName(), region.getX(), region.getZ()));

        // random obtained here to prevent a bajillion method calls deeper in the process
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // quickly load all chunks into memory
        region.loadAllChunks(cancelled);

        // render regions to tiles
        for (Renderer renderer : renderers) {
            TileCanvas tile = new TileCanvas(region, renderer);

            if (!renderer.renderRegion(region, tile, rand, cancelled)) {
                return false;
            }

            debug("Saving %s's render on %s for region %d,%d"
                .formatted(renderer.getName(), region.getWorld().getName(), region.getX(), region.getZ())
            );
            tile.save(this.activeCanvases);
        }

        // aggressively wipe the entire region reference tree from memory
        try {
            region.unload();
        } catch (Throwable e) {
            Logger.error("Error unloading region %d,%d".formatted(region.getX(), region.getZ()), e);
        }

        return true;
    }
}
