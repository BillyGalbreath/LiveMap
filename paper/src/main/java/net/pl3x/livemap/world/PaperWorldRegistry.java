package net.pl3x.livemap.world;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

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
        clear();
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            put(obj2key(world), new PaperWorld(world));
        }
    }

    @Override
    @Nullable
    public World get(@Nullable Object obj) {
        return super.get(obj2key(obj));
    }


    @NotNull
    public World get(@NotNull org.bukkit.World world) {
        return computeIfAbsent(world, k -> new PaperWorld(world));
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
