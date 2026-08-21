package net.pl3x.livemap.command.subcommand;

import com.mojang.brigadier.context.CommandContext;
import net.pl3x.livemap.command.BaseCommand;
import net.pl3x.livemap.command.Sender;
import net.pl3x.livemap.command.Source;
import org.jetbrains.annotations.NotNull;

/**
 * The fullrender command.
 *
 * @param <S> CommandSourceStack
 */
public class FullRenderCommand<S> extends BaseCommand<S> {
    /**
     * Constructs a new instance of FullRenderCommand.
     *
     * @param sourceConverter Stack to source converter
     */
    public FullRenderCommand(@NotNull Source.Converter<S> sourceConverter) {
        super("fullrender", sourceConverter);
    }

    @Override
    protected void execute(@NotNull CommandContext<S> context) {
        Sender sender = getSource(context).getSender();

        sender.sendMessage("// todo (fullrender)");
        // todo
    }
}
