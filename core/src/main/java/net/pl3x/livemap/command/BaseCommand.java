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

package net.pl3x.livemap.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.world.World;
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

        executes(this::execute);
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
     * @return Arbitrary number that doesn't mean anything
     * @throws CommandSyntaxException If the command failed to parse or execute
     */
    protected abstract int execute(@NotNull CommandContext<S> context) throws CommandSyntaxException;

    /**
     * Create a new point argument with the specified name.
     *
     * @param name Name of the argument
     * @return Point argument
     */
    @NotNull
    protected ArgumentBuilder<S, RequiredArgumentBuilder<S, Point>> point(@NotNull String name) {
        return LiveMap.api().getArgumentParser().point(name);
    }

    /**
     * Create a new integer argument with the specified name.
     *
     * @param name Name of the argument
     * @return Integer argument
     */
    @NotNull
    protected ArgumentBuilder<S, RequiredArgumentBuilder<S, Integer>> integer(@NotNull String name) {
        return LiveMap.api().getArgumentParser().integer(name);
    }

    /**
     * Create a new integer argument with the specified name.
     *
     * @param name Name of the argument
     * @param min  Minimum value allowed
     * @return Integer argument
     */
    @NotNull
    protected ArgumentBuilder<S, RequiredArgumentBuilder<S, Integer>> integer(@NotNull String name, int min) {
        return LiveMap.api().getArgumentParser().integer(name, min);
    }

    /**
     * Create a new integer argument with the specified name.
     *
     * @param name Name of the argument
     * @param min  Minimum value allowed
     * @param max  Maximum value allowed
     * @return Integer argument
     */
    @NotNull
    protected ArgumentBuilder<S, RequiredArgumentBuilder<S, Integer>> integer(@NotNull String name, int min, int max) {
        return LiveMap.api().getArgumentParser().integer(name, min, max);
    }

    /**
     * Create a new world argument with the specified name.
     *
     * @param name Name of the argument
     * @return World argument
     */
    @NotNull
    protected ArgumentBuilder<S, RequiredArgumentBuilder<S, World>> world(@NotNull String name) {
        return LiveMap.api().getArgumentParser().world(name);
    }
}
