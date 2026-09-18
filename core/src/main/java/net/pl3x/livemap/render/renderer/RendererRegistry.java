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

package net.pl3x.livemap.render.renderer;

import java.util.Map;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.util.OrderedRegistry;
import net.pl3x.livemap.util.Type;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * A registry of all map renderers.
 */
public class RendererRegistry extends OrderedRegistry<Renderer> {
    private final World world;

    /**
     * Constructs a new instance of RenderRegistry.
     *
     * @param world World this registry belongs to
     */
    public RendererRegistry(@NotNull World world) {
        this.world = world;
    }

    @Override
    public void rebuild() {
        clear();

        // secret renderer to handle blockinfo. shhh...
        put(Renderer.BLOCKINFO.create(Map.of("id", "blockinfo", "type", Renderer.BLOCKINFO.getId())));

        for (Map<String, Object> map : this.world.getConfig().RENDERERS) {
            Type<Renderer> type = Type.get(Renderer.class, Unsafe.cast(map.get("type")));
            if (type == null) {
                Logger.warn("   &7&l-&r Unknown renderer type&3: &f&o%s".formatted(map.get("type")));
                continue;
            }
            try {
                put(type.create(map));
            } catch (RuntimeException e) {
                Logger.error("   &7&l-&r Unable to create renderer type %s".formatted(type.getId()), e);
            }
        }

        Logger.info("   &7&l-&r Registered &3%d&r renderers".formatted(size() - 1)); // hide blockinfo
    }

    private void put(@NotNull Renderer renderer) {
        put(renderer.getId(), renderer);
    }
}
