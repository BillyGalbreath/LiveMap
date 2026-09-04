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

package net.pl3x.livemap.world;

import java.util.ArrayList;
import java.util.List;
import net.pl3x.livemap.render.iterator.SpiralIterator;
import net.pl3x.livemap.render.renderer.Renderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages fair, round-robin region dispatching across multiple active worlds.
 */
public class WorldDispatcher {
    private final List<WorldQueue> worldQueues = new ArrayList<>();
    private int currentIndex = 0;

    /**
     * Add a world to the queue.
     *
     * @param world     World to add
     * @param renderers Snapshot of world's renderers
     * @param spiral    Iterator of region indexes waiting to be rendered
     */
    public void addQueue(@NotNull World world, @NotNull List<Renderer> renderers, @NotNull SpiralIterator spiral) {
        this.worldQueues.add(new WorldQueue(world, renderers, spiral));
    }

    /**
     * Check if there are no worlds in queue.
     *
     * @return True if no worlds in queue
     */
    public boolean isEmpty() {
        return this.worldQueues.isEmpty();
    }

    /**
     * Get number of pending regions across all worlds.
     *
     * @return Number of pending regions
     */
    public synchronized int totalPending() {
        int total = 0;
        for (WorldQueue queue : this.worldQueues) {
            total += queue.spiral.size();
        }
        return total;
    }

    /**
     * Atomically pulls the next region task in round-robin fashion across worlds.
     *
     * @return Next WorldRegionTask, or null if all worlds are finished.
     */
    @Nullable
    public synchronized Ticket pollNext() {
        // loop until we find work or exhaustively confirm all queues are dead
        while (!this.worldQueues.isEmpty()) {
            // enforce safety bounds against structural shifts
            if (this.currentIndex >= this.worldQueues.size()) {
                this.currentIndex = 0;
            }

            WorldQueue queue = this.worldQueues.get(this.currentIndex);

            // purge explicitly broken or unloaded worlds immediately
            if (queue.world.isDiscarded()) {
                this.worldQueues.remove(this.currentIndex);
                continue;
            }

            // safely verify if this specific world has regions left to handle
            if (queue.spiral.hasNext()) {
                long regionIndex = queue.spiral.nextLong();

                // advance round-robin pointer for the NEXT requesting thread
                this.currentIndex = (this.currentIndex + 1) % this.worldQueues.size();

                return new Ticket(queue.world, queue.renderers, regionIndex);
            } else {
                // queue is legitimately exhausted. purge it and let loop continue naturally
                this.worldQueues.remove(this.currentIndex);
            }
        }
        return null;
    }

    /**
     * Returns all unpulled regions back into their respective world buffers upon cancellation.
     */
    public synchronized void returnRemainingAll() {
        for (WorldQueue queue : this.worldQueues) {
            if (!queue.world.isDiscarded()) {
                queue.spiral.returnRemaining(queue.world.getPendingRegions());
            }
        }
    }

    /**
     * Represents a queue of regions waiting to be rendered for a specific world.
     *
     * @param world     World for queue
     * @param renderers Snapshot of world's renderers
     * @param spiral    Iterator of pending region indexes
     */
    public record WorldQueue(@NotNull World world, @NotNull List<Renderer> renderers, @NotNull SpiralIterator spiral) {
    }

    /**
     * Represents a single region render task for a specific world.
     *
     * @param world     World for task
     * @param renderers Snapshot of world's renderers
     * @param region    Region to render
     */
    public record Ticket(@NotNull World world, @NotNull List<Renderer> renderers, long region) {
    }
}
