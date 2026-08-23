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

        var entries = this.world.<ServerLevel>getLevel()
            .registryAccess().lookupOrThrow(Registries.BIOME).entrySet();
        for (var entry : entries) {
            String id = entry.getKey().identifier().toString();

            if (!ColorsConfig.BIOME_COLORS.containsKey(id)) {
                Logger.warn("Found biome that is not in colors.yml: " + id);
            }

            var biome = entry.getValue();
            float temperature = Math.clamp(biome.getBaseTemperature(), 0.0F, 1.0F);
            float humidity = Math.clamp(biome.climateSettings.downfall(), 0.0F, 1.0F);
            put(id, new Biome(
                0,// todo - index (saved to disk for persistent BlockInfo)
                id,
                ColorsConfig.BIOME_COLORS.getOrDefault(id, 0),
                Objects.requireNonNullElseGet(ColorsConfig.BIOME_DRY_FOLIAGE.get(id),                // custom
                    () -> biome.getSpecialEffects().dryFoliageColorOverride().orElseGet(             // vanilla
                        () -> getDefaultColor(temperature, humidity, Colors.COLORMAP_DRY_FOLIAGE))), // fallback
                Objects.requireNonNullElseGet(ColorsConfig.BIOME_FOLIAGE.get(id),
                    () -> biome.getSpecialEffects().foliageColorOverride().orElseGet(
                        () -> getDefaultColor(temperature, humidity, Colors.COLORMAP_FOLIAGE))),
                Objects.requireNonNullElseGet(ColorsConfig.BIOME_GRASS.get(id),
                    () -> biome.getSpecialEffects().grassColorOverride().orElseGet(
                        () -> getDefaultColor(temperature, humidity, Colors.COLORMAP_GRASS))),
                Objects.requireNonNullElseGet(ColorsConfig.BIOME_WATER.get(id),
                    () -> biome.getSpecialEffects().waterColor()),
                (x, z, color) -> biome.getSpecialEffects().grassColorModifier().modifyColor(x, z, color)
            ));
        }
        Logger.info("Registered %d biomes (%d in config)".formatted(size(), ColorsConfig.BIOME_COLORS.size()));
    }
}
