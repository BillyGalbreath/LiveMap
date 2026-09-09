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
import it.unimi.dsi.fastutil.longs.LongCollection;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.command.BaseCommand;
import net.pl3x.livemap.command.Player;
import net.pl3x.livemap.command.Sender;
import net.pl3x.livemap.command.Source;
import net.pl3x.livemap.command.argument.ArgumentParser;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.util.FileUtil;
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
        then(world("world").executes(this::executeWorld));
    }

    // executed with specified world
    private int executeWorld(@NotNull CommandContext<S> context) {
        Sender sender = getSource(context).getSender();
        World world = context.getArgument("world", World.class);
        return execute(sender, world);
    }

    @Override
    // executed without any args
    protected int execute(@NotNull CommandContext<S> context) throws CommandSyntaxException {
        if (!(getSource(context).getSender() instanceof Player player)) {
            // console must specify world
            throw ArgumentParser.ERROR_MUST_SPECIFY_WORLD.create();
        }
        return execute(player, player.getWorld());
    }

    private int execute(@NotNull Sender sender, @NotNull World world) {
        sender.sendMessage(Lang.FULLRENDER_STARTING
            .replace("<world>", world.getName()));

        long started = System.nanoTime();

        // get all regions for world
        Collection<Path> paths = FileUtil.getRegionPaths(world);
        LongCollection regions = FileUtil.regionPathsToLongs(paths);

        // trigger render scheduler _now_
        CompletableFuture<Void> future = LiveMap.api().getRenderScheduler().trigger(() -> {
            // add all regions to the queue if and only if trigger is able to run
            // this prevents dumping the full list of regions to the queue on failed triggers
            world.getPendingRegions().addAll(regions);
        });

        // check for failed trigger
        if (future == null) {
            sender.sendMessage("<red>Unable to start fullrender (is it already running?)");
            return 0;
        }

        sender.sendMessage(Lang.FULLRENDER_STARTED
            .replace("<count>", Integer.toString(regions.size()))
            .replace("<world>", world.getName()));

        future.whenComplete((_, e) -> {
            if (e != null) {
                throw new RuntimeException(e);
            }

            long elapsed = System.nanoTime() - started;
            int chunks = 400 * 1024; // todo
            int cps = (int) (chunks / TimeUnit.NANOSECONDS.toSeconds(elapsed));

            sender.sendMessage(Lang.FULLRENDER_FINISHED
                .replace("<cps>", Integer.toString(cps))
                .replace("<chunks>", Integer.toString(chunks))
                .replace("<elapsed>", Duration.ofNanos(elapsed).toString())
                .replace("<world>", world.getName()));
        });

        return Command.SINGLE_SUCCESS;
    }
}
