package net.pl3x.livemap.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a base command.
 *
 * @param <S> CommandSourceStack
 */
public abstract class BaseCommand<S> extends LiteralArgumentBuilder<S> {
    private final Source.Converter<S> sourceConverter;
    private final String permission;

    /**
     * Constructs a new instance of BaseCommand.
     *
     * @param literal         Command name
     * @param sourceConverter Stack to source converter
     */
    protected BaseCommand(@NotNull String literal, @NotNull Source.Converter<S> sourceConverter) {
        super(literal);

        this.sourceConverter = sourceConverter;
        this.permission = "livemap.command.%s".formatted(literal);

        requires(stack -> this.sourceConverter.convert(stack).getSender().hasPermission(permission));

        executes(ctx -> {
            execute(ctx);
            return 1;
        });
    }

    /**
     * Get the command source.
     *
     * @param context Command context
     * @return Command source
     */
    @NotNull
    protected Source getSource(@NotNull CommandContext<S> context) {
        return this.sourceConverter.convert(context.getSource());
    }

    /**
     * Executes this command.
     *
     * @param context Command context
     * @throws CommandSyntaxException If the command failed to parse or execute
     */
    protected abstract void execute(@NotNull CommandContext<S> context) throws CommandSyntaxException;
}
