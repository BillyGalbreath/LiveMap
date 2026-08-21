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
        // todo - reload plugin

        // caution - v3 has issues reloading while render queue is running. need to
        // pay special attention to ensure all tasks are stopped before reloading.
        // whether or not to automatically restart them, I don't know, yet.
    }
}
