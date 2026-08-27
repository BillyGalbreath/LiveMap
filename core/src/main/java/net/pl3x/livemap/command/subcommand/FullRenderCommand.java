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

    private void executeWorld(@NotNull CommandContext<S> context) throws CommandSyntaxException {
        Sender sender = getSource(context).getSender();
        World world = context.getArgument("world", World.class);
        execute(sender, world);
    }

    @Override
    protected void execute(@NotNull CommandContext<S> context) throws CommandSyntaxException {
        if (!(getSource(context).getSender() instanceof Player player)) {
            // console must specify world
            throw World.Argument.ERROR_MISSING_WORLD.create();
        }
        execute(player, player.getWorld());
    }

    private void execute(@NotNull Sender sender, @NotNull World world) throws CommandSyntaxException {
        sender.sendMessage(Lang.FULLRENDER_STARTED
            .replace("<world>", world.getName()));

        sender.sendMessage("// todo (fullrender)");
        // todo - add all regions to render queue
    }
}
