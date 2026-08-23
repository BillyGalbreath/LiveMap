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
            if (block != null) {
                // block already registered. let's fix its properties
                short flags = getPropertiesFlag(id, entry.getValue());
                block.setFlags((short) (block.getFlags() | flags));
                continue;
            }

            int vanilla = entry.getValue().defaultMapColor().col;

            if (!ColorsConfig.BLOCK_COLORS.containsKey(id)) {
                Logger.warn("Found block that is not in colors.yml: " + id + " (" + Colors.toHex(vanilla) + ")");
            }

            // todo - index?
            put(id, new Block(0, id, vanilla, getPropertiesFlag(id, entry.getValue())));
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
