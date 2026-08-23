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

import com.mojang.brigadier.context.CommandContext;
import net.pl3x.livemap.command.subcommand.FullRenderCommand;
import net.pl3x.livemap.command.subcommand.RadiusRenderCommand;
import net.pl3x.livemap.command.subcommand.ReloadCommand;
import org.jetbrains.annotations.NotNull;

/**
 * The livemap command.
 *
 * @param <S> CommandSourceStack
 */
public class LiveMapCommand<S> extends BaseCommand<S> {
    /**
     * Constructs a new instance of LiveMapCommand.
     *
     * @param sourceConverter Stack to source converter
     */
    public LiveMapCommand(@NotNull Source.Converter<S> sourceConverter) {
        super("livemap", sourceConverter);
        then(new FullRenderCommand<>(sourceConverter));
        then(new RadiusRenderCommand<>(sourceConverter));
        then(new ReloadCommand<>(sourceConverter));
    }

    @Override
    protected void execute(@NotNull CommandContext<S> context) {
        Sender sender = getSource(context).getSender();

        sender.sendMessage("// todo (map)");
        // todo
    }
}
