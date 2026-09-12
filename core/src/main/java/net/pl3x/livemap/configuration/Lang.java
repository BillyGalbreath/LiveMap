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
