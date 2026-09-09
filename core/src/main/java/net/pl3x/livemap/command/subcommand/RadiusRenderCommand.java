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

package net.pl3x.livemap.command.subcommand;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.pl3x.livemap.command.BaseCommand;
import net.pl3x.livemap.command.Player;
import net.pl3x.livemap.command.Sender;
import net.pl3x.livemap.command.Source;
import net.pl3x.livemap.command.argument.ArgumentParser;
import net.pl3x.livemap.command.argument.PointArgumentType;
import net.pl3x.livemap.command.argument.WorldArgumentType;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.world.World;
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
        then(world("world")
            .executes(this::executeWorld)
            .then(integer("radius", 1)
                .executes(this::executeRadius)
                .then(point("center")
                    .executes(this::executeCenter)
                )
            )
        );
    }

    // executed with world but without radius and center args
    private int executeWorld(@NotNull CommandContext<S> context) throws CommandSyntaxException {
        throw ArgumentParser.ERROR_MUST_SPECIFY_RADIUS.create();
    }

    // executed with world and radius but without center arg
    private int executeRadius(@NotNull CommandContext<S> context) throws CommandSyntaxException {
        Sender sender = getSource(context).getSender();
        if (!(sender instanceof Player player)) {
            // console must specify center
            throw ArgumentParser.ERROR_MUST_SPECIFY_CENTER.create();
        }
        World world = context.getArgument("world", World.class);
        Integer radius = context.getArgument("radius", Integer.class);
        return execute(sender, world, radius, player.getLocation());
    }

    // executed with all args: world, radius, and center
    private int executeCenter(@NotNull CommandContext<S> context) {
        Sender sender = getSource(context).getSender();
        World world = WorldArgumentType.getWorld(context, "world");
        Integer radius = IntegerArgumentType.getInteger(context, "radius");
        Point center = PointArgumentType.getPoint(context, "center");
        return execute(sender, world, radius, center);
    }

    @Override
    // executed without any args
    protected int execute(@NotNull CommandContext<S> context) throws CommandSyntaxException {
        throw ArgumentParser.ERROR_MUST_SPECIFY_WORLD.create();
    }

    private int execute(@NotNull Sender sender, @NotNull World world, @NotNull Integer radius, @NotNull Point center) {
        sender.sendMessage("World " + world);
        sender.sendMessage("Radius " + radius);
        sender.sendMessage("Center " + center);

        // todo

        return Command.SINGLE_SUCCESS;
    }
}
