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

package net.pl3x.livemap;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * LiveMap's logger.
 */
public final class Logger {
    static java.util.logging.Logger logger;

    static {
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
    }

    private Logger() {
    }

    /**
     * Log a message at the INFO level.
     *
     * @param message the message string to be logged
     */
    public static void info(@NotNull String message) {
        logger.log(Level.INFO, message);
    }

    /**
     * Log an exception (throwable) at the INFO level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void info(@NotNull String message, @NotNull Throwable throwable) {
        logger.log(Level.INFO, message, throwable);
    }

    /**
     * Log a message at the WARN level.
     *
     * @param message the message string to be logged
     */
    public static void warn(@NotNull String message) {
        logger.log(Level.WARNING, message);
    }

    /**
     * Log an exception (throwable) at the WARN level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void warn(@NotNull String message, @NotNull Throwable throwable) {
        logger.log(Level.WARNING, message, throwable);
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param message the message string to be logged
     */
    public static void error(@NotNull String message) {
        logger.log(Level.SEVERE, message);
    }

    /**
     * Log an exception (throwable) at the ERROR level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void error(@NotNull String message, @NotNull Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Log filter to hide undertow logger output.
     */
    public static class LogFilter implements Filter {
        private final List<String> filters = new ArrayList<>();

        /**
         * Flag to let filter know when to hide logger output
         */
        public static boolean HIDE_UNDERTOW_LOGS = false;

        /**
         * Constructs a new instance of LogFilter.
         */
        public LogFilter() {
            this.filters.add("io.undertow");
            this.filters.add("org.xnio");
            this.filters.add("org.xnio.nio");
            this.filters.add("org.jboss.threads");
        }

        @NotNull
        private Result checkMessage(@NotNull String message) {
            if (HIDE_UNDERTOW_LOGS) {
                for (String filter : this.filters) {
                    if (message.contains(filter)) {
                        return Result.DENY;
                    }
                }
            }
            return Result.NEUTRAL;
        }

        @Override
        @NotNull
        public Result getOnMismatch() {
            return Result.NEUTRAL;
        }

        @Override
        @NotNull
        public Result getOnMatch() {
            return Result.NEUTRAL;
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object... objects) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1, @NotNull Object o2) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1, @NotNull Object o2, @NotNull Object o3) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1, @NotNull Object o2, @NotNull Object o3, @NotNull Object o4) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1, @NotNull Object o2, @NotNull Object o3, @NotNull Object o4, @NotNull Object o5) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1, @NotNull Object o2, @NotNull Object o3, @NotNull Object o4, @NotNull Object o5, @NotNull Object o6) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1, @NotNull Object o2, @NotNull Object o3, @NotNull Object o4, @NotNull Object o5, @NotNull Object o6, @NotNull Object o7) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1, @NotNull Object o2, @NotNull Object o3, @NotNull Object o4, @NotNull Object o5, @NotNull Object o6, @NotNull Object o7, @NotNull Object o8) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull String s, @NotNull Object o, @NotNull Object o1, @NotNull Object o2, @NotNull Object o3, @NotNull Object o4, @NotNull Object o5, @NotNull Object o6, @NotNull Object o7, @NotNull Object o8, @NotNull Object o9) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull Object o, @NotNull Throwable throwable) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull org.apache.logging.log4j.core.Logger logger, @NotNull org.apache.logging.log4j.Level level, @NotNull Marker marker, @NotNull Message message, @NotNull Throwable throwable) {
            return checkMessage(logger.getName());
        }

        @Override
        @NotNull
        public Result filter(@NotNull LogEvent logEvent) {
            return checkMessage(logEvent.getLoggerName());
        }

        @Override
        @Nullable
        public State getState() {
            try {
                return State.STARTED;
            } catch (Exception exception) {
                return null;
            }
        }

        @Override
        public void initialize() {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public boolean isStarted() {
            return true;
        }

        @Override
        public boolean isStopped() {
            return false;
        }
    }
}
