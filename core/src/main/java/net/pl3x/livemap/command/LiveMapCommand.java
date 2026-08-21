package net.pl3x.livemap.command;

import com.mojang.brigadier.context.CommandContext;
import net.pl3x.livemap.command.subcommand.FullRenderCommand;
import net.pl3x.livemap.command.subcommand.RadiusRenderCommand;
import net.pl3x.livemap.command.subcommand.ReloadCommand;
import org.jetbrains.annotations.NotNull;

/**
 * The livemap command.
 *
 * @param <S> CommandSourceStack
 */
public class LiveMapCommand<S> extends BaseCommand<S> {
    /**
     * Constructs a new instance of LiveMapCommand.
     *
     * @param sourceConverter Stack to source converter
     */
    public LiveMapCommand(@NotNull Source.Converter<S> sourceConverter) {
        super("livemap", sourceConverter);
        then(new FullRenderCommand<>(sourceConverter));
        then(new RadiusRenderCommand<>(sourceConverter));
        then(new ReloadCommand<>(sourceConverter));
    }

    @Override
    protected void execute(@NotNull CommandContext<S> context) {
        Sender sender = getSource(context).getSender();

        sender.sendMessage("// todo (map)");
        // todo
    }
}
