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
