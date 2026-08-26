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
    @Key("error-invalid-world")
    public static String ERROR_INVALID_WORLD = "Invalid world name or id";
    @Key("error-missing-world")
    public static String ERROR_MISSING_WORLD = "You must enter a world name or id!";

    @Key("command-fullrender-started")
    public static String FULLRENDER_STARTED = "Starting fullrender on <grey><world>";
    @Key("command-fullrender-finished")
    public static String FULLRENDER_FINISHED = "Finished fullrender on <grey><world></grey> in <yellow><elapsed></yellow> at <dark_aqua><cps>cps</dark_aqua> (<grey><chunks></grey> total chunks scanned)";
    @Key("command-fullrender-errored")
    public static String FULLRENDER_ERRORED = "<red>Fullrender errored on <grey><world>\n<red>Error: <error>";

    private static final Lang CONFIG = new Lang();

    /**
     * Constructs a new instance of Lang.
     */
    private Lang() {
        Path dir = LiveMap.api().getDataPath().resolve("lang");

        // extract locale dir from jar
        FileUtil.extractDir("/lang/", dir, false);

        super(dir.resolve(Config.LANGUAGE_FILE));
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
