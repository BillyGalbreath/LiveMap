package net.pl3x.livemap.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class PaperSender implements Sender {
    private final CommandSender sender;

    public PaperSender(@NotNull CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return this.sender.hasPermission(permission);
    }

    @Override
    public void sendMessage(@NotNull String message) {
        this.sender.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }
}
