// https://github.com/Draradech/FlowerMap (CC0-1.0 license)

package net.pl3x.livemap.world;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.pl3x.livemap.LiveMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PaperFlowerMap implements FlowerMap {
    public final Map<Biome, List<Feature>> biomeFeatureCache = Collections.synchronizedMap(new LinkedHashMap<>());

    public final ArrayList<ResourceKey<Feature>> canSpawnFromBonemealList = new ArrayList<>(10) {{
        add(VegetationFeatures.FLOWER_DEFAULT);
        add(VegetationFeatures.FLOWER_FLOWER_FOREST);
        add(VegetationFeatures.FLOWER_SWAMP);
        add(VegetationFeatures.FLOWER_PLAIN);
        add(VegetationFeatures.FLOWER_MEADOW);
        add(VegetationFeatures.FLOWER_CHERRY);
        add(VegetationFeatures.WILDFLOWER);
        add(VegetationFeatures.FLOWER_PALE_GARDEN);
    }};
    private final World world;

    public PaperFlowerMap(@NotNull World world) {
        this.world = world;
    }

    @Override
    @Nullable
    public net.pl3x.livemap.world.block.Block getColor(@NotNull net.pl3x.livemap.world.biome.Biome biome, int blockX, int blockY, int blockZ) {
        var nmsBiome = world.<ServerLevel>getLevel().registryAccess()
            .lookupOrThrow(Registries.BIOME)
            .getValue(Identifier.parse(biome.getId()));
        if (nmsBiome == null) {
            return null;
        }
        var flowers = getBoneMealFeatures(nmsBiome);
        if (flowers.isEmpty()) {
            return null;
        }
        var block = ((SimpleBlockFeature) flowers.getFirst()).toPlace().value()
            .getState(
                world.getLevel(),
                world.<ServerLevel>getLevel().getRandom(),
                new BlockPos(blockX, blockY, blockZ)
            ).getBlock();
        return LiveMap.api().getBlockRegistry().get(BuiltInRegistries.BLOCK.getKey(block).toString());
    }

    @NotNull
    private List<Feature> getBoneMealFeatures(@NotNull Biome biome) {
        // the biomes created from the builtin registry are missing tags
        // with the new can_spawn_from_bonemeal tag for vegetation features we can
        // no longer just call getFlowerFeatures (now called getBonemealFeatures)
        // iterate through the feature stream and collect matching features manually
        return this.biomeFeatureCache.computeIfAbsent(biome, _ ->
            biome.getGenerationSettings().features().stream()
                .flatMap(HolderSet::stream)
                .flatMap(feature -> feature.value().getFeatures())
                .filter(feature -> {
                    var key = feature.unwrapKey();
                    return key.isPresent() && this.canSpawnFromBonemealList.contains(key.get());
                })
                .map(Holder::value)
                .collect(ImmutableList.toImmutableList()));
    }
}
