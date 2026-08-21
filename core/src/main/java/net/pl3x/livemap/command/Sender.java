package net.pl3x.livemap.command;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a command sender.
 */
public interface Sender {
    /**
     * Check if player has specified permission.
     *
     * @param permission Permission to check
     * @return True if sender has permission
     */
    boolean hasPermission(@NotNull String permission);

    /**
     * Sends a system chat message to this command sender.
     *
     * @param message Message to send
     */
    void sendMessage(@NotNull String message);
}
