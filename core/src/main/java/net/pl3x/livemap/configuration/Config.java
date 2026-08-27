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

package net.pl3x.livemap.configuration;

import net.pl3x.livemap.LiveMap;

/**
 * LiveMap's main config.
 */
public final class Config extends AbstractConfig {
    @Key("settings.debug-mode")
    @Comment("""
        Extra logger/console output. (can be spammy)""")
    public static boolean DEBUG_MODE = false;
    @Key("settings.language-file")
    @Comment("""
        The language file to use from the locale folder.""")
    public static String LANGUAGE_FILE = "en_us.yml";

    @Key("settings.web-directory.path")
    @Comment("""
        The directory that houses the website and world tiles.
        Relative paths are from LiveMap's plugin directory,
        but absolute paths are supported, too.""")
    public static String WEB_DIR = "web";
    @Key("settings.web-directory.read-only")
    @Comment("""
        Set to true if you don't want LiveMap to overwrite
        the website files on startup. (Good for servers that
        customize these files)""")
    public static boolean WEB_DIR_READONLY = false;

    @Key("settings.internal-webserver.enabled")
    @Comment("""
        Enable the built-in web server for regular http.""")
    public static boolean HTTPD_ENABLED = true;
    @Key("settings.internal-webserver.bind")
    @Comment("""
        The interface the built-in web server should bind to for http requests.
        Warning: If you don't understand what this is leave it set to 0.0.0.0""")
    public static String HTTPD_BIND = "0.0.0.0";
    @Key("settings.internal-webserver.port")
    @Comment("""
        The port the built-in web server listens to for http requests.
        Make sure the port is allocated if using a panel like Pterodactyl.""")
    public static int HTTPD_PORT = 8080;

    @Key("settings.performance.render-threads")
    @Comment("""
        The number of process-threads to use for loading and scanning chunks.
        Value of -1 will use half of the available logical cpu-cores. (recommended)
        Warning: Using all available cpu-threads may cause thread starvation and impact system performance.""")
    public static int RENDER_THREADS = -1;

    private static final Config CONFIG = new Config();

    /**
     * Constructs a new instance of Config.
     */
    private Config() {
        super(LiveMap.api().getDataPath().resolve("config.yml"));
    }

    /**
     * Reloads configuration from YAML file.
     */
    public static void reload() {
        CONFIG.reload0();
    }

    @Override
    protected void cleanup() {
        // setup comments on sections that have no fields
        // @formatter:off - IntelliJ keeps adding whitespace to the empty lines
        setComment("settings", """
            -----------------------------------------------
                          ╻  ╻╻ ╻┏━╸┏┳┓┏━┓┏━┓
                          ┃  ┃┃┏┛┣╸ ┃┃┃┣━┫┣━┛
                          ┗━╸╹┗┛ ┗━╸╹ ╹╹ ╹╹
                             Configuration

            More information can be found on the wiki:
            https://pl3x.net/livemap/wiki

            Report bugs to the issue tracker
            https://pl3x.net/livemap/issues

            Support is offered on Discord:
            https://pl3x.net/discord

            -----------------------------------------------""");
        // @formatter:on
        setComment("settings.web-directory", """
            Settings for the directory all the web files sit in.""");
        setComment("settings.internal-webserver", """
            Settings for the built-in webserver.""");
        setComment("settings.performance", """
            Performance related settings.""");
    }

    @Override
    protected void fields2Yaml() {
        // nothing to update
    }
}
