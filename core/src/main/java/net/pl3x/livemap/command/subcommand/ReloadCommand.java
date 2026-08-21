package net.pl3x.livemap.command.subcommand;

import com.mojang.brigadier.context.CommandContext;
import net.pl3x.livemap.command.BaseCommand;
import net.pl3x.livemap.command.Sender;
import net.pl3x.livemap.command.Source;
import org.jetbrains.annotations.NotNull;

/**
 * The reload command.
 *
 * @param <S> CommandSourceStack
 */
public class ReloadCommand<S> extends BaseCommand<S> {
    /**
     * Constructs a new instance of ReloadCommand.
     *
     * @param sourceConverter Stack to source converter
     */
    public ReloadCommand(@NotNull Source.Converter<S> sourceConverter) {
        super("reload", sourceConverter);
    }

    @Override
    protected void execute(@NotNull CommandContext<S> context) {
        Sender sender = getSource(context).getSender();

        sender.sendMessage("// todo (reload)");
        // todo
    }
}
