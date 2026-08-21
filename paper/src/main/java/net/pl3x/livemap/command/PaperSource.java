package net.pl3x.livemap.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PaperSource implements Source {
    private final PaperSender sender;

    public PaperSource(@NotNull PaperSender sender) {
        this.sender = sender;
    }

    @NotNull
    @Override
    public PaperSender getSender() {
        return this.sender;
    }

    @NotNull
    public static Converter<CommandSourceStack> getConverter() {
        return stack -> {
            CommandSender sender = stack.getSender();
            return new PaperSource(
                sender instanceof Player player
                    ? new PaperPlayer(player)
                    : new PaperSender(sender)
            );
        };
    }
}
