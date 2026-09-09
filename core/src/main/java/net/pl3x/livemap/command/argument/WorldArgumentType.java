/*
 * This file is part of LiveMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020-2026 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pl3x.livemap.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a custom command argument for our world type.
 */
public class WorldArgumentType implements ArgumentType<World> {
    /**
     * Get a new world argument type.
     *
     * @return World argument type
     */
    @NotNull
    public static WorldArgumentType world() {
        return new WorldArgumentType();
    }

    /**
     * Get world from command context by argument name.
     *
     * @param context Command context
     * @param name    Argument name
     * @return Specified world
     */
    @NotNull
    public static World getWorld(@NotNull CommandContext<?> context, @NotNull String name) {
        return context.getArgument(name, World.class);
    }

    @Override
    @NotNull
    public World parse(@NotNull StringReader reader) throws CommandSyntaxException {
        String input = StringArgumentType.string().parse(reader);
        World world = LiveMap.api().getWorldRegistry().get(input);
        if (world == null) {
            throw ArgumentParser.ERROR_WORLD_NOT_FOUND.create();
        }
        return world;
    }

    @Override
    @NotNull
    public <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        for (var entry : LiveMap.api().getWorldRegistry().entrySet()) {
            if (entry.getKey().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(entry.getKey());
            }
            if (entry.getValue().getName().toLowerCase(Locale.ROOT).startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(entry.getValue().getName());
            }
        }
        return builder.buildFuture();
    }

    /**
     * Gets the native type that this argument uses,
     * the type that is sent to the client.
     *
     * @return native argument type
     */
    @NotNull
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.string();
    }
}
