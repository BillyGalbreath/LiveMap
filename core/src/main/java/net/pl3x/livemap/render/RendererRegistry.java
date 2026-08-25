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

import java.util.List;
import java.util.Map;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.render.heightmap.Heightmap;
import net.pl3x.livemap.util.Registry;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * A registry of all map renderers.
 */
public class RendererRegistry extends Registry<Renderer> {
    // I'm not sure why this keeps giving "no
    // comment" warning. but here we are...
    @SuppressWarnings("doclint")
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

        Logger.info("   &7&l-&r registering renderers...");

        List<Map<String, Object>> list = this.world.getConfig().RENDERERS;
        for (Map<String, Object> map : list) {
            Renderer.Type type = Renderer.Type.get((String) map.get("type"));
            if (type == null) {
                Logger.warn("     &7&l-&r unknown renderer type&3: &f&o%s".formatted(map.get("type")));
                continue;
            }
            Renderer renderer;
            try {
                Heightmap.Type heightmap = Heightmap.Type.get((String) map.get("heightmap"));
                renderer = type.create(
                    (String) map.get("name"),
                    (String) map.get("icon"),
                    heightmap == null ? null : heightmap.create(),
                    (int) map.getOrDefault("biome-blend", 0),
                    (boolean) map.getOrDefault("translucent-fluids", false)
                );
            } catch (RuntimeException e) {
                Logger.error("     &7&l-&r unable to create renderer type %s".formatted(type.id()), e);
                continue;
            }
            put(renderer.getType().id(), renderer);
        }

        Logger.info("     &7&l-&r registered &3%d&r renderers".formatted(size()));
    }
}
