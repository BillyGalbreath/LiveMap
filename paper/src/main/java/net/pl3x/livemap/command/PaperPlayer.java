package net.pl3x.livemap.command;

import java.util.UUID;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.PaperLiveMap;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

public class PaperPlayer extends PaperSender implements Player {
    private final org.bukkit.entity.Player player;

    public PaperPlayer(@NotNull org.bukkit.entity.Player player) {
        super(player);
        this.player = player;
    }

    @Override
    @NotNull
    public String getName() {
        return this.player.getName();
    }

    @Override
    @NotNull
    public UUID getUUID() {
        return this.player.getUniqueId();
    }

    @Override
    @NotNull
    public World getWorld() {
        return ((PaperLiveMap) LiveMap.api()).getWorldRegistry().get(this.player.getWorld());
    }
}
