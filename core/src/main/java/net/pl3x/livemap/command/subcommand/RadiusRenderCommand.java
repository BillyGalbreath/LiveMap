package net.pl3x.livemap.command.subcommand;

import com.mojang.brigadier.context.CommandContext;
import net.pl3x.livemap.command.BaseCommand;
import net.pl3x.livemap.command.Sender;
import net.pl3x.livemap.command.Source;
import org.jetbrains.annotations.NotNull;

/**
 * The radiusrender command.
 *
 * @param <S> CommandSourceStack
 */
public class RadiusRenderCommand<S> extends BaseCommand<S> {
    /**
     * Constructs a new instance of RadiusRenderCommand.
     *
     * @param sourceConverter Stack to source converter
     */
    public RadiusRenderCommand(@NotNull Source.Converter<S> sourceConverter) {
        super("radiusrender", sourceConverter);
    }

    @Override
    protected void execute(@NotNull CommandContext<S> context) {
        Sender sender = getSource(context).getSender();

        sender.sendMessage("// todo (radiusrender)");
        // todo - add regions within radius to render queue
    }
}
