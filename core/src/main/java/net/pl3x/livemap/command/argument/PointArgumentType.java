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
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.pl3x.livemap.marker.Point;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command argument for a Point.
 */
public class PointArgumentType implements ArgumentType<Point> {
    public static final SimpleCommandExceptionType ERROR_INVALID_COORDINATE =
        new SimpleCommandExceptionType(() -> "Invalid coordinate syntax. Expected numbers");

    /**
     * Get a new point argument type.
     *
     * @return Point argument type
     */
    @NotNull
    public static PointArgumentType point() {
        return new PointArgumentType();
    }

    /**
     * Get point from command context by argument name.
     *
     * @param context Command context
     * @param name    Argument name
     * @return Specified point
     */
    @NotNull
    public static Point getPoint(@NotNull CommandContext<?> context, @NotNull String name) {
        return context.getArgument(name, Point.class);
    }

    @Override
    @NotNull
    public Point parse(@NotNull StringReader reader) throws CommandSyntaxException {
        int x = reader.readInt();

        // expect a space between X and Z coordinates
        if (!reader.canRead() || reader.peek() != ' ') {
            throw ERROR_INVALID_COORDINATE.createWithContext(reader);
        }

        // consume the space
        reader.skip();

        int z = reader.readInt();

        return Point.of(x, z);
    }

    @Override
    @NotNull
    public Collection<String> getExamples() {
        return Arrays.asList("0 0", "~ ~", "~-5 ~10", "150 ~-20");
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
