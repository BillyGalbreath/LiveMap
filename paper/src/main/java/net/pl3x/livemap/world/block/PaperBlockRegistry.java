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

package net.pl3x.livemap.world.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.ColorsConfig;
import net.pl3x.livemap.render.image.Colors;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class PaperBlockRegistry extends BlockRegistry {
    @Override
    public void rebuild() {
        clear();

        //Blocks.registerDefaults(); // todo

        var entries = ((CraftWorld) Bukkit.getWorlds().getFirst()).getHandle()
            .registryAccess().lookupOrThrow(Registries.BLOCK).entrySet();
        for (var entry : entries) {
            String id = entry.getKey().identifier().toString();

            Block block = super.getOrDefault(id, null);
            short properties = getPropertiesFlag(id, entry.getValue());

            if (block != null) {
                block.setFlags((short) (block.getFlags() | properties));
                continue;
            }

            int vanilla = entry.getValue().defaultMapColor().col;

            if (!ColorsConfig.BLOCK_COLORS.containsKey(id)) {
                Logger.warn("Found block that is not in colors.yml: " + id + " (" + Colors.toHex(vanilla) + ")");
            }

            // todo - unique index for BlockInfo
            put(id, new Block(0, id, vanilla, properties));
        }
        Logger.info("Registered %d blocks (%d in config)".formatted(size(), ColorsConfig.BLOCK_COLORS.size()));
    }

    private short getPropertiesFlag(@NotNull String id, @NotNull net.minecraft.world.level.block.Block block) {
        short flag = 0;
        BlockState state = block.defaultBlockState();
        if (!state.isAir()) {
            var properties = state.getProperties();
            for (Property<?> property : properties) {
                flag = processPropertyFlag(property.getName(), id, flag);
            }
        }
        return flag;
    }
}
