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

import java.util.List;
import java.util.Map;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.util.Registry;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * A registry of all map renderers.
 */
public class RendererRegistry extends Registry<Renderer> {
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

        List<Map<String, Object>> list = this.world.getConfig().RENDERERS;
        for (Map<String, Object> map : list) {
            String typeStr = Unsafe.cast(map.get("type"));
            Renderer.Type type = Renderer.Type.get(typeStr);
            if (type == null) {
                Logger.warn("   &7&l-&r Unknown renderer type&3: &f&o%s".formatted(typeStr));
                continue;
            }
            Renderer renderer;
            try {
                Heightmap.Type heightmap = Heightmap.Type.get(Unsafe.cast(map.get("heightmap")));
                renderer = type.create(
                    Unsafe.cast(map.get("name")),
                    Unsafe.cast(map.get("icon")),
                    heightmap == null ? null : heightmap.create(),
                    Unsafe.cast(map.getOrDefault("biome-blend", 0)),
                    Unsafe.cast(map.getOrDefault("translucent-fluids", false))
                );
            } catch (RuntimeException e) {
                Logger.error("   &7&l-&r Unable to create renderer type %s".formatted(type.id()), e);
                continue;
            }
            put(renderer.getType().id(), renderer);
        }

        Logger.info("   &7&l-&r Registered &3%d&r renderers".formatted(size()));
    }
}
