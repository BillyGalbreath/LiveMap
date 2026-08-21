package net.pl3x.livemap.command;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a command source.
 */
public interface Source {
    /**
     * Get the command sender from this source.
     *
     * @return Command sender
     */
    @NotNull
    Sender getSender();

    /**
     * Converts CommandSourceStack to Source
     *
     * @param <S> CommandSourceStack
     */
    @FunctionalInterface
    interface Converter<S> {
        /**
         * Convert the CommandSourceStack to a Source
         *
         * @param stack CommandSourceStack to convert
         * @return Source of CommandSourceStack
         */
        @NotNull
        Source convert(@NotNull S stack);
    }
}
