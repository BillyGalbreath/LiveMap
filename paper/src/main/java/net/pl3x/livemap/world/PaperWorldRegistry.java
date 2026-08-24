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

import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.server.level.ServerLevel;
import net.pl3x.livemap.Logger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperWorldRegistry extends WorldRegistry {
    @Nullable
    public static String obj2key(@Nullable Object obj) {
        org.bukkit.World world = obj2bukkit(obj);
        return world == null ? null : world.getKey().toString();
    }

    @Nullable
    public static org.bukkit.World obj2bukkit(@Nullable Object obj) {
        return (obj == null) ? null : switch (obj) {
            case org.bukkit.World world -> world;
            case World world -> world.<ServerLevel>getLevel().getWorld();
            case String str -> str2bukkit(str.toLowerCase(Locale.ROOT));
            case NamespacedKey key -> Bukkit.getWorld(key);
            case UUID uuid -> Bukkit.getWorld(uuid);
            default -> null;
        };
    }

    @Nullable
    public static org.bukkit.World str2bukkit(@NotNull String str) {
        // try bukkit world name
        org.bukkit.World world = Bukkit.getWorld(str);
        if (world != null) {
            return world;
        }
        // try string representation of namespaced key
        NamespacedKey key = NamespacedKey.fromString(str);
        if (key != null) {
            world = Bukkit.getWorld(key);
            if (world != null) {
                return world;
            }
        }
        // try parsing string as uuid
        try {
            world = Bukkit.getWorld(UUID.fromString(str));
        } catch (IllegalArgumentException ignore) {
        }
        return world;
    }

    @Override
    public void rebuild() {
        // clear out old registered worlds
        clear();

        Logger.info("Gathering world information...");

        for (org.bukkit.World bukkit : Bukkit.getWorlds()) {
            World world = new PaperWorld(bukkit);
            put(obj2key(bukkit), world);
        }
    }

    @Override
    @Nullable
    public World get(@Nullable Object obj) {
        return super.get(obj2key(obj));
    }

    @NotNull
    public World get(@NotNull org.bukkit.World world) {
        return computeIfAbsent(world, _ -> new PaperWorld(world));
    }

    @NotNull
    public World computeIfAbsent(@NotNull org.bukkit.World world, @NotNull Function<? super String, ? extends World> func) {
        return super.computeIfAbsent(obj2key(world), func);
    }

    @Override
    @Nullable
    public World remove(@NotNull Object obj) {
        String key = obj2key(obj);
        return key == null ? null : super.remove(key);
    }
}
