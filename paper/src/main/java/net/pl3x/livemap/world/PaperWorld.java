package net.pl3x.livemap.world;

import net.minecraft.server.level.ServerLevel;
import net.pl3x.livemap.util.Unsafe;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class PaperWorld extends World {
    private final ServerLevel level;

    public PaperWorld(@NotNull org.bukkit.World world) {
        this(((CraftWorld) world).getHandle());
    }

    public PaperWorld(@NotNull ServerLevel level) {
        super(
            level.serverLevelData.getLevelName(),
            level.getServer().storageSource.getDimensionPath(level.dimension()).resolve("region")
        );
        this.level = level;
    }

    @Override
    @NotNull
    public <T> T getLevel() {
        return Unsafe.cast(this.level);
    }
}
