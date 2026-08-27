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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.marker.Point;
import net.pl3x.livemap.util.Unsafe;
import net.pl3x.livemap.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simpleyaml.configuration.ConfigurationSection;

/**
 * Per-world configuration.
 */
public final class WorldConfig extends AbstractConfig {
    @Key("enabled")
    @Comment("""
        Enables this world to be rendered on the map.""")
    public boolean ENABLED = true;
    @Key("order")
    @Comment("""
        The order the world shows in the world list on the webmap.""")
    public int ORDER = 0;
    @Key("name")
    @Comment("""
        The display name of the world in the world list.
        Use <world> to use the official world name.""")
    public String NAME = "<world>";

    @Key("render.scan-chunks")
    @Comment("""
        Actively scan chunks in the world(s) when they change.
        Set to false to allow worlds to show on webmap but not
        trigger any automatic chunk updates.
        Note: Commands are not affected by this.
        """)
    public boolean SCAN_CHUNKS = true;

    @Key("render.center")
    @Comment("""
        Point [0,0] where the map center is at in the webmap.
        Leave empty array [] to use spawn point instead.""")
    public Point CENTER = null;

    @Key("render.zoom.default")
    @Comment("""
        The default zoom when loading the map in browser.
        Normal sized tiles (1 pixel = 1 block) are
        always at zoom level 0.""")
    public int ZOOM_DEFAULT = 0;
    @Key("render.zoom.max-out")
    @Comment("""
        The maximum zoom out you can do on the map.
        Each additional level requires a new set of tiles
        to be rendered, so don't go too wild here.""")
    public int ZOOM_MAX_OUT = 3;
    @Key("render.zoom.min-out")
    @Comment("""
        Extra zoom in layers will stretch the original
        tile images so you can zoom in further without
        the extra cost of rendering more tiles.""")
    public int ZOOM_MIN_OUT = 2;

    @Key("render.renderers")
    @Comment("""
        List of renderers to use. Each renderer will draw a different type of map.""")
    public List<Map<String, Object>> RENDERERS = new ArrayList<>() {{
        add(new LinkedHashMap<>() {{
            put("type", "fancy");
            put("name", "Fancy");
            put("icon", "overworld_fancy.png");
            put("heightmap", "fancy");
            put("biome-blend", 3);
            put("translucent-fluids", true);
        }});
    }};

    private final World world;

    /**
     * Constructs a new instance of WorldConfig.
     *
     * @param world World this config belongs to
     */
    public WorldConfig(@NotNull World world) {
        super(LiveMap.api().getDataPath().resolve("config.yml"));
        this.world = world;

        reload();
    }

    /**
     * Reloads configuration from YAML file.
     */
    public void reload() {
        reload0();
    }

    @Override
    protected void cleanup() {
        // setup comments on sections that have no fields
        setComment("render", """
            Settings to control how rendering the world works.""");
        setComment("render.zoom", """
            Zoom settings control how the map zooms in and out and how
            the tile images are stored on disk.
            Warning: Changing these values will require a map reset.""");
        // todo https://github.com/Carleslc/Simple-YAML/issues/84
        setComment("render.renderers[0].type", """
            The built-in types include: basic, biomes, fancy, flowermap, inhabited, nether_roof.""");
        setComment("render.renderers[0].name", """
            The display name for this renderer. It is viewable on the webmap when mouse hovers over the icon.""");
        setComment("render.renderers[0].icon", """
            "This can be any image under 'web/images/icon/' directory.""");
        setComment("render.renderers[0].heightmap", """
            The built-in heightmaps include: basic, fancy.""");
        setComment("render.renderers[0].biome-blend", """
            Enables blending of biome grass/foliage/water colors similar to
            the client's biome blending option.
            Values are clamped to 0-7 to represent the 8 possible values in the client.""");
        setComment("render.renderers[0].translucent-fluids", """
            Enable translucent fluids.
            This will make the fluids look fancier and translucent,
            so you can see the blocks below in shallow fluids.""");

        // call directly on parent config because these nodes are outside the world's scope
        getConfig().setComment("world-settings", """
            These are the per-world settings. Each world can have their own unique values.""");
        getConfig().setComment("world-settings.default", """
            These are the default settings that will be applied to all worlds.
            You can override any of these on a per-world basis by adding the
            world by name below this section.""");

        // cleanup user input where needed
        this.RENDERERS.forEach(renderer -> {
            int before = (int) renderer.get("biome-blend");
            int after = Math.clamp(before, 0, 7);
            renderer.put("biome-blend", after);
        });
    }

    @Override
    protected void fields2Yaml() {
        // unable to tell if fields were populated from defaults or
        // per-world settings, so do not populate YAML from the fields.
    }

    @Override
    @Nullable
    protected Object getAndSetDefault(@NotNull String path, @Nullable Object def) {
        if (getConfig().get("world-settings.default." + path) == null) {
            set("world-settings.default." + path, def);
        }
        return get("world-settings." + this.world.getName() + "." + path,
            get("world-settings.default." + path, def));
    }

    @Override
    protected void setComment(@NotNull String path, @Nullable String comment) {
        getConfig().setComment("world-settings.default." + path, comment);
    }

    @Override
    @Nullable
    protected Object get(@NotNull String path) {
        if (path.contains("render.center")) {
            ConfigurationSection section = getConfig().getConfigurationSection(path);
            if (section == null) {
                return null;
            }
            Map<String, Object> map = section.getMapValues(false);
            if (map == null) {
                return null;
            }
            if (!(map.get("x") instanceof Number xNum)) {
                return null;
            }
            if (!(map.get("z") instanceof Number zNum)) {
                return null;
            }
            return Point.of(xNum, zNum);
        }
        return super.get(path);
    }

    @Override
    protected void set(@NotNull String path, @Nullable Object value) {
        if (path.contains("render.center")) {
            if (value == null) {
                value = new int[0];
            } else {
                Point center = Unsafe.cast(value);
                value = new int[] {center.getX(), center.getZ()};
            }
        }
        getConfig().set(path, value);
    }
}
