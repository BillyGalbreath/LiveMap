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
     * @param world  World to add
     * @param spiral Iterator of region indexes waiting to be rendered
     */
    public void addQueue(@NotNull World world, @NotNull SpiralIterator spiral) {
        this.worldQueues.add(new WorldQueue(world, spiral));
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
        int attempts = 0;
        while (!this.worldQueues.isEmpty() && attempts < this.worldQueues.size()) {
            WorldQueue queue = this.worldQueues.get(this.currentIndex);
            if (queue.spiral.hasNext() && !queue.world.isDiscarded()) {
                long regionIndex = queue.spiral.nextLong();

                // Advance to the next world for fair round-robin distribution
                this.currentIndex = (this.currentIndex + 1) % this.worldQueues.size();
                return new Ticket(queue.world, regionIndex);
            } else {
                // This world is exhausted or discarded, remove it from active rotation
                this.worldQueues.remove(this.currentIndex);
                if (this.worldQueues.isEmpty()) {
                    break;
                }
                this.currentIndex %= this.worldQueues.size();
            }
            attempts++;
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
     * @param world  World for queue
     * @param spiral Iterator of pending region indexes
     */
    public record WorldQueue(@NotNull World world, @NotNull SpiralIterator spiral) {
    }

    /**
     * Represents a single region render task for a specific world.
     *
     * @param world  World for task
     * @param region Region to render
     */
    public record Ticket(@NotNull World world, long region) {
    }
}
