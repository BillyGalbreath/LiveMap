package net.pl3x.livemap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.pl3x.livemap.configuration.Config;
import org.jetbrains.annotations.NotNull;

/**
 * LiveMap's logger.
 */
public final class Logger {
    private Logger() {
    }

    private static ComponentLogger logger() {
        return LiveMap.api().getComponentLogger();
    }

    private static Component parse(@NotNull String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    /**
     * Log a debug message at the INFO level.
     *
     * @param message the debug message string to be logged
     */
    public static void debug(@NotNull String message) {
        if (Config.DEBUG_MODE) {
            info("<gray>[<yellow>DEBUG</yellow>] " + message);
        }
    }

    /**
     * Log an exception (throwable) at the INFO level with an accompanying debug message.
     *
     * @param message   the debug message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void debug(@NotNull String message, @NotNull Throwable throwable) {
        if (Config.DEBUG_MODE) {
            info("<gray>[<yellow>DEBUG</yellow>] " + message, throwable);
        }
    }

    /**
     * Log a message at the INFO level.
     *
     * @param message the message string to be logged
     */
    public static void info(@NotNull String message) {
        logger().info(message);
    }

    /**
     * Log an exception (throwable) at the INFO level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void info(@NotNull String message, @NotNull Throwable throwable) {
        logger().info(message, throwable);
    }

    /**
     * Log a message at the WARN level.
     *
     * @param message the message string to be logged
     */
    public static void warn(@NotNull String message) {
        logger().warn(message);
    }

    /**
     * Log an exception (throwable) at the WARN level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void warn(@NotNull String message, @NotNull Throwable throwable) {
        logger().warn(message, throwable);
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param message the message string to be logged
     */
    public static void error(@NotNull String message) {
        logger().error(message);
    }

    /**
     * Log an exception (throwable) at the ERROR level with an accompanying message.
     *
     * @param message   the message accompanying the exception
     * @param throwable the exception (throwable) to log
     */
    public static void error(@NotNull String message, @NotNull Throwable throwable) {
        logger().error(message, throwable);
    }
}
