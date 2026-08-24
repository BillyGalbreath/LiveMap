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

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.minecraft.server.level.ServerLevel;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.biome.PaperBiomeRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class PaperWorld extends World {
    private final ServerLevel level;

    private final PaperBiomeRegistry biomeRegistry = new PaperBiomeRegistry(this);

    public PaperWorld(@NotNull org.bukkit.World world) {
        this(((CraftWorld) world).getHandle());
    }

    public PaperWorld(@NotNull ServerLevel level) {
        super(
            level.bukkitName,
            level.getSeed(),
            Point.of(level.getLevelData().getRespawnData().pos().getX(), level.getLevelData().getRespawnData().pos().getZ()),
            Type.get(level.dimension().identifier().toString()),
            level.getServer().storageSource.getDimensionPath(level.dimension()).resolve("region")
        );
        this.level = level;

        Logger.info(" &7&l-&r found &e%s&r (&3&o%s&r)".formatted(level.getTypeKey().identifier(), getName()));

        if (!isEnabled()) {
            Logger.info("   &7&l-&r &9skipping &3(&r&odisabled in config&3)");
            return;
        }

        getBiomeRegistry().rebuild();
    }

    @Override
    @NotNull
    public <T> T getLevel() {
        return Unsafe.cast(this.level);
    }

    @Override
    public boolean hasCeiling() {
        return this.level.dimensionType().hasCeiling();
    }

    @Override
    public int getMinY() {
        return this.level.getMinY();
    }

    @Override
    public int getMaxY() {
        return this.level.getMaxY();
    }

    @Override
    public int getHeight() {
        return this.level.dimensionType().height();
    }

    @Override
    @NotNull
    public PaperBiomeRegistry getBiomeRegistry() {
        return this.biomeRegistry;
    }

    @Override
    @NotNull
    public String toString() {
        return "Paper" + super.toString();
    }

    public static class Argument extends World.Argument implements CustomArgumentType<World, String> {
    }
}
