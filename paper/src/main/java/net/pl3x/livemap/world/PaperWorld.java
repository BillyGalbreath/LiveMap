package net.pl3x.livemap.world;

import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.minecraft.server.level.ServerLevel;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.biome.BiomeRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class PaperWorld extends World {
    private final ServerLevel level;

    private final BiomeRegistry biomeRegistry = new BiomeRegistry();

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
    public BiomeRegistry getBiomeRegistry() {
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
