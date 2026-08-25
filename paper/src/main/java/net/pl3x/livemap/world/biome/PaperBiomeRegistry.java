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

package net.pl3x.livemap.world.biome;

import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.ColorsConfig;
import net.pl3x.livemap.render.image.Colors;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

public class PaperBiomeRegistry extends BiomeRegistry {
    private final World world;

    public PaperBiomeRegistry(@NotNull World world) {
        this.world = world;
    }

    public void rebuild() {
        clear();

        Logger.info("   &7&l-&r gathering biomes information...");

        var entries = this.world.<ServerLevel>getLevel()
            .registryAccess().lookupOrThrow(Registries.BIOME).entrySet();
        for (var entry : entries) {
            String id = entry.getKey().identifier().toString();

            if (!ColorsConfig.BIOME_COLORS.containsKey(id)) {
                Logger.warn("     &7&l-&r biome not in colors.yml&3:&f&o %s &r&3(&r%s&3)".formatted(id, Colors.toHex(0)));
            }

            var biome = entry.getValue();
            float temperature = Math.clamp(biome.getBaseTemperature(), 0.0F, 1.0F);
            float humidity = Math.clamp(biome.climateSettings.downfall(), 0.0F, 1.0F);
            put(id, new Biome(
                0,// todo - index (saved to disk for persistent BlockInfo)
                id,
                ColorsConfig.BIOME_COLORS.getOrDefault(id, 0),
                Objects.requireNonNullElseGet(ColorsConfig.OVERRIDES_DRY_FOLIAGE.get(id),            // custom
                    () -> biome.getSpecialEffects().dryFoliageColorOverride().orElseGet(             // vanilla
                        () -> getDefaultColor(temperature, humidity, Colors.COLORMAP_DRY_FOLIAGE))), // fallback
                Objects.requireNonNullElseGet(ColorsConfig.OVERRIDES_FOLIAGE.get(id),
                    () -> biome.getSpecialEffects().foliageColorOverride().orElseGet(
                        () -> getDefaultColor(temperature, humidity, Colors.COLORMAP_FOLIAGE))),
                Objects.requireNonNullElseGet(ColorsConfig.OVERRIDES_GRASS.get(id),
                    () -> biome.getSpecialEffects().grassColorOverride().orElseGet(
                        () -> getDefaultColor(temperature, humidity, Colors.COLORMAP_GRASS))),
                Objects.requireNonNullElseGet(ColorsConfig.OVERRIDES_WATER.get(id),
                    () -> biome.getSpecialEffects().waterColor()),
                (x, z, color) -> biome.getSpecialEffects().grassColorModifier().modifyColor(x, z, color)
            ));
        }

        Logger.info("   &7&l-&r registered &3%d&r biomes".formatted(size()));
    }
}
