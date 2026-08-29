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

package net.pl3x.livemap.world.region;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.render.iterator.RegionSpiralIterator;
import net.pl3x.livemap.render.iterator.SpiralIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Task that checks which regions need to be scanned.
 *
 * <p>Methods are synchronized for thread safety.
 */
public class RegionQueue {
    private final Set<Long> unsorted = new HashSet<>();
    private final Deque<Long> sorted = new ConcurrentLinkedDeque<>();

    /**
     * Constructs a new instance of RegionQueue.
     */
    public RegionQueue() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    /**
     * Returns {@code true} if this queue contains no region indexes.
     *
     * @return {@code true} if this queue contains no region indexes
     */
    public synchronized boolean isEmpty() {
        return this.unsorted.isEmpty()
            && this.sorted.isEmpty();
    }

    /**
     * Adds the specified region index to this queue if it is not already
     * present.
     *
     * <p>If this queue already contains the region index, the call leaves
     * the queue unchanged and returns {@code false}.
     *
     * @param region Region index to be added to this queue
     * @return {@code true} if this queue did not already contain the specified element
     */
    public synchronized boolean add(@NotNull Long region) {
        return this.unsorted.add(region);
    }

    /**
     * Adds all the region indexes in the specified collection to this set
     * if they're not already present. If the specified collection is a set,
     * the {@code addAll} operation effectively modifies this queue so that its
     * value is the <i>union</i> of the two sets. The behavior of this
     * operation is undefined if the specified collection is modified while
     * the operation is in progress.
     *
     * @param regions collection containing region indexes to be added
     * @return {@code true} if this queue changed as a result of the call
     */
    public synchronized boolean addAll(@NotNull Collection<Long> regions) {
        return this.unsorted.addAll(regions);
    }

    /**
     * Removes all the region indexes from this queue (optional operation).
     * The queue will be empty after this method returns.
     *
     * @throws UnsupportedOperationException if the {@code clear} operation
     *                                       is not supported by this queue
     */
    public synchronized void clear() {
        this.sorted.clear();
        this.unsorted.clear();
    }

    /**
     * Retrieves and removes the head of the queue (in other words,
     * the first world in queue), or returns {@code null} if this
     * queue is empty.
     *
     * @return the first world of this queue, or {@code null} if
     *     this queue is empty
     */
    @Nullable
    public synchronized Long poll() {
        return this.sorted.poll();
    }

    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     */
    public synchronized int size() {
        return this.sorted.size();
    }

    /**
     * Sort the regions in a clockwise spiral around the specified center block.
     *
     * @param center Center block to spiral around
     */
    public synchronized void sort(@NotNull Point center) {
        int centerX = center.getX() >> 9;
        int centerZ = center.getZ() >> 9;

        Logger.debug("start sorting regions from " + centerX + "," + centerZ);

        // add back any unfinished regions
        this.unsorted.addAll(this.sorted);
        this.sorted.clear();

        // sort in spiral out from coordinates
        SpiralIterator iter = new RegionSpiralIterator(centerX, centerZ, () -> !this.unsorted.isEmpty());
        while (iter.hasNext()) {
            Long region = iter.next();
            this.unsorted.remove(region);
            this.sorted.add(region);
        }

        Logger.debug("finished sorting %d regions".formatted(this.sorted.size()));
    }
}
