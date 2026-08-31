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
import java.util.Collections;
import java.util.List;
import net.pl3x.livemap.util.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A registry of renderable worlds.
 */
public abstract class WorldRegistry extends Registry<World> {
    /**
     * Constructs a new instance of WorldRegistry.
     */
    public WorldRegistry() {
        // Explicit constructor to satisfy Javadoc and linter tools
    }

    /**
     * Removes the mapping for the specified key from this registry if present.
     *
     * @param key key whose mapping is to be removed from the registry
     * @return the previous value associated with {@code key}, or
     *     {@code null} if there was no mapping for {@code key}.
     */
    @Nullable
    public World remove(@NotNull String key) {
        World world = super.remove(key);
        if (world != null) {
            world.discard();
        }
        return world;
    }

    @Override
    public void clear() {
        // thread safe accumulator
        List<World> removed = Collections.synchronizedList(new ArrayList<>());

        // safely extract elements atomically from the ConcurrentHashMap
        this.forEach((key, world) -> {
            if (super.remove(key, world)) {
                removed.add(world);
            }
        });

        // cleanup world data safely
        removed.forEach(World::discard);
    }
}
