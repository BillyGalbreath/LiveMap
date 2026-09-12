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

import java.nio.file.Path;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.util.FileUtil;

/**
 * LiveMap's main config.
 */
public final class Config extends AbstractConfig {
    @Key("settings.debug-mode")
    @Comment("""
        Extra logger/console output. (can be spammy)""")
    public static boolean DEBUG_MODE = false;
    @Key("settings.language-locale")
    @Comment("""
        The language used from the locale folder.""")
    public static String LANGUAGE_LOCALE = "en_us";
    @Key("settings.startup-banner")
    @Comment("""
        Shows a little banner when the plugin enables.
        You can turn it off here if you dont like it.""")
    public static boolean STARTUP_BANNER = true;

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

    @Key("settings.web-tile.image-format")
    @Comment("""
        The image format for tile images.
        Built in types: bmp, gif, jpg, jpeg, png, webp""")
    public static String WEB_TILE_FORMAT = "png";
    @Key("settings.web-tile.image-quality")
    @Comment("""
        The quality for image tiles (0.0 - 1.0)
        0.0 is low quality, high compression, small file size
        1.0 is high quality, no compression, large file size
        Note: Not all image formats honor this setting.""")
    public static double WEB_TILE_QUALITY = 0.0D;
    @Key("settings.web-tile.buffer-size")
    @Comment("""
        The buffer size when writing tile images to disk.
        Default is 512kb (524288).""")
    public static int WEB_TILE_BUFFER = 524288;

    @Key("settings.website.title")
    @Comment("""
        The title that appears in the title of the web browser.""")
    public static String WEBSITE_TITLE = "LiveMap v4";
    @Key("settings.website.logo")
    @Comment("""
        The title that appears in the title of the web browser.""")
    public static String WEBSITE_LOGO = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 200 220\" fill=\"currentColor\"><path d=\"M199.6 34.5 134.2.3h-.2c-.1-.2-.3-.2-.5-.2h-.4a2.6 2.6 0 0 0-.9.1h-.2L67.7 34 3.5.3a2.4 2.4 0 0 0-3.5 2v181c0 1 .5 1.7 1.3 2.1l65.2 34.2.5.2h.2a2.5 2.5 0 0 0 1.1 0h.2l.5-.2 64-33.6 64.3 33.7a2.5 2.5 0 0 0 2.3 0c.7-.5 1.2-1.3 1.2-2.1v-181a2 2 0 0 0-1.3-2zM196 213.7l-61.9-32.5h-.2l-.5-.2h-.4a2.6 2.6 0 0 0-.9.2h-.2L70 213.7V38l63-33 63 33z\"/><path d=\"M135.5 16.6v157.2c0 1.3-1 2.4-2.4 2.4-1.3 0-2.4-1-2.4-2.4V16.6c0-1.3 1.1-2.4 2.4-2.4 1.3 0 2.4 1.1 2.4 2.4z\"/></svg>";
    @Key("settings.website.attribution")
    @Comment("""
        The title that appears in the title of the web browser.""")
    public static String WEBSITE_ATTRIBUTION = "LiveMap &copy; 2020-2026";

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
    @Key("settings.internal-webserver.url")
    @Comment("""
        The url that is displayed in the /map command to your players.
        IMPORTANT: This is ONLY a display text. It does NOT configure anything.""")
    public static String HTTPD_URL = "http://localhost:8080";

    @Key("settings.performance.render-threads")
    @Comment("""
        The number of process-threads to use for loading and scanning chunks.
        Value of -1 will use half of the available logical cpu-cores. (recommended)
        Warning: Using all available cpu-threads may cause thread starvation and impact system performance.""")
    public static int RENDER_THREADS = -1;

    private static final Config CONFIG = new Config();

    private Config() {
        super(LiveMap.api().getDataPath().resolve("config.yml"));
    }

    /**
     * Reloads configuration from YAML file.
     */
    public static void reload() {
        Path file = CONFIG.getPath().getFileName();
        Path dir = CONFIG.getPath().getParent();

        // extract default config from jar
        FileUtil.extractFile("%s".formatted(file), dir, false);

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
