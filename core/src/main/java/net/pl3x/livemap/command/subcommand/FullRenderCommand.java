package net.pl3x.livemap.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.command.BaseCommand;
import net.pl3x.livemap.command.Player;
import net.pl3x.livemap.command.Sender;
import net.pl3x.livemap.command.Source;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.world.World;
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
        then(LiveMap.api().args().<S>world().executes(ctx -> {
            executeWorld(ctx);
            return Command.SINGLE_SUCCESS;
        }));
    }

    @Override
    protected void execute(@NotNull CommandContext<S> context) throws CommandSyntaxException {
        if (!(getSource(context).getSender() instanceof Player player)) {
            throw World.Argument.ERROR_MISSING_WORLD.create();
        }
        execute(player, player.getWorld());
    }

    private void executeWorld(@NotNull CommandContext<S> context) throws CommandSyntaxException {
        Sender sender = getSource(context).getSender();
        World world = context.getArgument("world", World.class);
        execute(sender, world);
    }

    private void execute(@NotNull Sender sender, @NotNull World world) throws CommandSyntaxException {
        sender.sendMessage(Lang.FULLRENDER_STARTED
            .replace("<world>", world.getName()));

        sender.sendMessage("// todo (fullrender)");
        // todo - add all regions to render queue
    }
}
