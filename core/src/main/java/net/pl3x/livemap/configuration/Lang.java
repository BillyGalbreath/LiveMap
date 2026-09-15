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
 * LiveMap's language config.
 */
public final class Lang extends AbstractConfig {
    @Key("error-world-not-found")
    public static String ERROR_WORLD_NOT_FOUND = "World not found";
    @Key("error-must-specify-center")
    public static String ERROR_MUST_SPECIFY_CENTER = "You must specify center coordinates";
    @Key("error-must-specify-radius")
    public static String ERROR_MUST_SPECIFY_RADIUS = "You must specify radius";
    @Key("error-must-specify-world")
    public static String ERROR_MUST_SPECIFY_WORLD = "You must enter a world name or id";

    @Key("command-livemap")
    public static String COMMAND_LIVEMAP = "View the LiveMap at <click:open_url:'<url>'><url>></click>";

    @Key("command-fullrender-starting")
    public static String FULLRENDER_STARTING = "Starting fullrender on <grey><world>";
    @Key("command-fullrender-started")
    public static String FULLRENDER_STARTED = "Found <grey><count></grey> regions for <grey><world>";
    @Key("command-fullrender-finished")
    public static String FULLRENDER_FINISHED = "Finished fullrender on <grey><world></grey> in <yellow><elapsed></yellow> at <dark_aqua><cps>cps</dark_aqua> (<grey><chunks></grey> total chunks scanned)";
    @Key("command-fullrender-errored")
    public static String FULLRENDER_ERRORED = "<red>Fullrender errored on <grey><world>\n<red>Error: <error>";

    @Key("ui.browser.title")
    public static String UI_BROWSER_TITLE = "LiveMap v4";
    @Key("ui.sidebar.href")
    public static String UI_SIDEBAR_HREF = "https://modrinth.com/plugin/livemap/";
    @Key("ui.sidebar.title")
    public static String UI_SIDEBAR_TITLE = "LiveMap";
    @Key("ui.sidebar.logo")
    public static String UI_SIDEBAR_LOGO = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 200 220\" fill=\"currentColor\"><path d=\"M199.6 34.5 134.2.3h-.2c-.1-.2-.3-.2-.5-.2h-.4a2.6 2.6 0 0 0-.9.1h-.2L67.7 34 3.5.3a2.4 2.4 0 0 0-3.5 2v181c0 1 .5 1.7 1.3 2.1l65.2 34.2.5.2h.2a2.5 2.5 0 0 0 1.1 0h.2l.5-.2 64-33.6 64.3 33.7a2.5 2.5 0 0 0 2.3 0c.7-.5 1.2-1.3 1.2-2.1v-181a2 2 0 0 0-1.3-2zM196 213.7l-61.9-32.5h-.2l-.5-.2h-.4a2.6 2.6 0 0 0-.9.2h-.2L70 213.7V38l63-33 63 33z\"/><path d=\"M135.5 16.6v157.2c0 1.3-1 2.4-2.4 2.4-1.3 0-2.4-1-2.4-2.4V16.6c0-1.3 1.1-2.4 2.4-2.4 1.3 0 2.4 1.1 2.4 2.4z\"/></svg>";
    @Key("ui.attribution")
    public static String UI_ATTRIBUTION = "LiveMap &copy; 2020-2026";
    @Key("ui.blockinfo.unknown.block")
    public static String UI_BLOCKINFO_UNKNOWN_BLOCK = "Unknown Block";
    @Key("ui.blockinfo.unknown.biome")
    public static String UI_BLOCKINFO_UNKNOWN_BIOME = "Unknown Biome";
    @Key("ui.blockinfo.label")
    public static String UI_BLOCKINFO_LABEL = "BlockInfo";
    @Key("ui.blockinfo.value")
    public static String UI_BLOCKINFO_VALUE = "<block><br/><biome>";
    @Key("ui.coords.label")
    public static String UI_COORDS_LABEL = "Coordinates";
    @Key("ui.coords.value")
    public static String UI_COORDS_VALUE = "<x>, <y>, <z>";
    @Key("ui.link.label")
    public static String UI_LINK_LABEL = "Sharable Link";
    @Key("ui.link.value")
    public static String UI_LINK_VALUE = "";
    @Key("ui.players.label")
    public static String UI_PLAYERS_LABEL = "Players (<online>/<max>)";
    @Key("ui.players.value")
    public static String UI_PLAYERS_VALUE = "No players are currently online";
    @Key("ui.worlds.label")
    public static String UI_WORLDS_LABEL = "Worlds";
    @Key("ui.worlds.value")
    public static String UI_WORLDS_VALUE = "No worlds have been configured";
    @Key("ui.layers.label")
    public static String UI_LAYERS_LABEL = "Layers";
    @Key("ui.layers.value")
    public static String UI_LAYERS_VALUE = "No layers have been configured";
    @Key("ui.markers.label")
    public static String UI_MARKERS_LABEL = "Markers";
    @Key("ui.markers.value")
    public static String UI_MARKERS_VALUE = "No markers have been configured";

    private static final Lang CONFIG = new Lang();

    private Lang() {
        Path dir = LiveMap.api().getDataPath().resolve("lang");

        // extract lang dir from jar
        FileUtil.extractDir("/lang/", dir, false);

        super(dir.resolve(Config.LANGUAGE_LOCALE + ".yml"));
    }

    /**
     * Reloads configuration from YAML file.
     */
    public static void reload() {
        CONFIG.reload0();
    }

    @Override
    protected void fields2Yaml() {
        // nothing to update
    }
}
