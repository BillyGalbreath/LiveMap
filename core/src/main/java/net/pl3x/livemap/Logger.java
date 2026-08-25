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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pl3x.livemap.configuration.Config;
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

    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)[§&]([0-9a-fk-or])");
    private static final Map<Character, String> ANSI_MAP = new HashMap<>() {{
        put('0', uni(30)); // black
        put('1', uni(34)); // dark blue
        put('2', uni(32)); // dark green
        put('3', uni(36)); // dark aqua
        put('4', uni(31)); // dark red
        put('5', uni(35)); // dark purple
        put('6', uni(33)); // gold
        put('7', uni(37)); // gray
        put('8', uni(90)); // dark gray
        put('9', uni(94)); // blue
        put('a', uni(92)); // green
        put('b', uni(96)); // aqua
        put('c', uni(91)); // red
        put('d', uni(95)); // light purple
        put('e', uni(93)); // yellow
        put('f', uni(37)); // white
        put('l', uni(1));  // bold
        put('m', uni(9));  // strikethrough
        put('n', uni(4));  // underline
        put('o', uni(3));  // italic
        //put('r', uni(0));  // reset (handled manually)
    }};
    private static final String RESET = uni(0);
    private static final String RESET_INFO = uni(0);
    private static final String RESET_DEBUG = uni(0, 37);
    private static final String RESET_WARN = uni(0, 93, 1);
    private static final String RESET_ERROR = uni(0, 31, 1);

    @NotNull
    private static String uni(int @NotNull ... code) {
        StringBuilder builder = new StringBuilder(6);
        for (int i : code) {
            builder.append("\u001B[%dm".formatted(i));
        }
        return builder.toString();
    }

    @NotNull
    private static String ansi(@NotNull String msg, @NotNull String reset) {
        Matcher matcher = COLOR_PATTERN.matcher(msg = msg.replaceAll("(?i)[§&]r", reset));
        StringBuilder builder = new StringBuilder(msg.length() + 32).append(reset);

        while (matcher.find()) {
            char code = Character.toLowerCase(matcher.group(1).charAt(0));
            String replacement = ANSI_MAP.getOrDefault(code, matcher.group());
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(builder);

        return builder.append(RESET).toString();
    }

    static {
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addFilter(new LogFilter());
    }

    private Logger() {
    }

    /**
     * Log a debug message at the INFO level, if debug mode is enabled in config.
     *
     * @param message the debug message string to be logged
     */
    public static void debug(@NotNull String message) {
        if (Config.DEBUG_MODE) {
            logger.log(Level.INFO, ansi("&7[&eDEBUG&7] %s".formatted(message), RESET_DEBUG));
        }
    }

    /**
     * Log an exception (throwable) at the INFO level with an accompanying
     * debug message, if debug mode is enabled in config.
     *
     * @param message   the debug message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void debug(@NotNull String message, @NotNull Throwable throwable) {
        if (Config.DEBUG_MODE) {
            logger.log(Level.INFO, ansi("&7[&eDEBUG&7] %s".formatted(message), RESET_DEBUG), throwable);
        }
    }

    /**
     * Log a message at the INFO level.
     *
     * @param message the message string to be logged
     */
    public static void info(@NotNull String message) {
        logger.log(Level.INFO, ansi(message, RESET_INFO));
    }

    /**
     * Log an exception (throwable) at the INFO level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void info(@NotNull String message, @NotNull Throwable throwable) {
        logger.log(Level.INFO, ansi(message, RESET_INFO), throwable);
    }

    /**
     * Log a message at the WARN level.
     *
     * @param message the message string to be logged
     */
    public static void warn(@NotNull String message) {
        logger.log(Level.WARNING, ansi(message, RESET_WARN));
    }

    /**
     * Log an exception (throwable) at the WARN level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void warn(@NotNull String message, @NotNull Throwable throwable) {
        logger.log(Level.WARNING, ansi(message, RESET_WARN), throwable);
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param message the message string to be logged
     */
    public static void error(@NotNull String message) {
        logger.log(Level.SEVERE, ansi(message, RESET_ERROR));
    }

    /**
     * Log an exception (throwable) at the ERROR level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void error(@NotNull String message, @NotNull Throwable throwable) {
        logger.log(Level.SEVERE, ansi(message, RESET_ERROR), throwable);
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
            return State.STARTED;
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
