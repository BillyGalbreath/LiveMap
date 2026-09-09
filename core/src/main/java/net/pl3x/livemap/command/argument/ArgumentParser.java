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

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.pl3x.livemap.configuration.Lang;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience methods to make using custom command arguments a little less painful.
 */
public interface ArgumentParser {
    SimpleCommandExceptionType ERROR_WORLD_NOT_FOUND = new SimpleCommandExceptionType(() -> Lang.ERROR_WORLD_NOT_FOUND);
    SimpleCommandExceptionType ERROR_MUST_SPECIFY_CENTER = new SimpleCommandExceptionType(() -> Lang.ERROR_MUST_SPECIFY_CENTER);
    SimpleCommandExceptionType ERROR_MUST_SPECIFY_RADIUS = new SimpleCommandExceptionType(() -> Lang.ERROR_MUST_SPECIFY_RADIUS);
    SimpleCommandExceptionType ERROR_MUST_SPECIFY_WORLD = new SimpleCommandExceptionType(() -> Lang.ERROR_MUST_SPECIFY_WORLD);

    /**
     * Create a new point argument with the specified name.
     *
     * @param name Name of the argument
     * @param <S>  Command source type
     * @return Point argument
     */
    @NotNull
    default <S> ArgumentBuilder<S, RequiredArgumentBuilder<S, Point>> point(@NotNull String name) {
        return RequiredArgumentBuilder.argument(name, PointArgumentType.point());
    }

    /**
     * Create a new integer argument with the specified name.
     *
     * @param name Name of the argument
     * @param <S>  Command source type
     * @return Integer argument
     */
    @NotNull
    default <S> ArgumentBuilder<S, RequiredArgumentBuilder<S, Integer>> integer(@NotNull String name) {
        return RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer());
    }

    /**
     * Create a new integer argument with the specified name.
     *
     * @param name Name of the argument
     * @param min  Minimum value allowed
     * @param <S>  Command source type
     * @return Integer argument
     */
    @NotNull
    default <S> ArgumentBuilder<S, RequiredArgumentBuilder<S, Integer>> integer(@NotNull String name, int min) {
        return RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(min));
    }

    /**
     * Create a new integer argument with the specified name.
     *
     * @param name Name of the argument
     * @param min  Minimum value allowed
     * @param max  Maximum value allowed
     * @param <S>  Command source type
     * @return Integer argument
     */
    @NotNull
    default <S> ArgumentBuilder<S, RequiredArgumentBuilder<S, Integer>> integer(@NotNull String name, int min, int max) {
        return RequiredArgumentBuilder.argument(name, IntegerArgumentType.integer(min, max));
    }

    /**
     * Create a new world argument with the specified name.
     *
     * @param name Name of the argument
     * @param <S>  Command source type
     * @return World argument
     */
    @NotNull
    default <S> ArgumentBuilder<S, RequiredArgumentBuilder<S, World>> world(@NotNull String name) {
        return RequiredArgumentBuilder.argument(name, WorldArgumentType.world());
    }
}
