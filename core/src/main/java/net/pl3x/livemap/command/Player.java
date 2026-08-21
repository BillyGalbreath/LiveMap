package net.pl3x.livemap.command;

import java.util.UUID;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a player in a world.
 */
public interface Player extends Sender {
    /**
     * Get this player's name.
     *
     * @return Name of player
     */
    @NotNull
    String getName();

    /**
     * Get the unique id for this player.
     *
     * @return Unique id
     */
    @NotNull
    UUID getUUID();

    /**
     * Get the world this player is in.
     *
     * @return World player is in
     */
    @NotNull
    World getWorld();
}
