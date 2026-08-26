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
import java.util.List;
import net.pl3x.livemap.LiveMap;

/**
 * Configurable block types config.
 */
public final class BlocksConfig extends AbstractConfig {
    @Key("blocks.air")
    @Comment("""
        List of blocks that are considered air when it comes
        to coloring. Blocks listed here will not be rendered.""")
    public static List<String> BLOCKS_AIR = new ArrayList<>() {{
        add("minecraft:air");
        add("minecraft:cave_air");
        add("minecraft:void_air");
    }};

    @Key("blocks.dry-foliage")
    @Comment("""
        List of blocks that are considered dry foliage when it comes
        to coloring. Blocks listed here will use the biome's
        dry foliage color when rendering.""")
    public static List<String> BLOCKS_DRY_FOLIAGE = new ArrayList<>() {{
        add("minecraft:leaf_litter");
    }};

    @Key("blocks.foliage")
    @Comment("""
        List of blocks that are considered foliage when it comes
        to coloring. Blocks listed here will use the biome's
        foliage color when rendering.
        Note: Birch and Spruce are intentionally absent by default.""")
    public static List<String> BLOCKS_FOLIAGE = new ArrayList<>() {{
        add("minecraft:acacia_leaves");
        //add("minecraft:birch_leaves"); // birch 0x80A755
        add("minecraft:dark_oak_leaves");
        add("minecraft:jungle_leaves");
        add("minecraft:mangrove_leaves"); // mangrove? 0x92C648
        add("minecraft:oak_leaves");
        //add("minecraft:spruce_leaves"); // evergreen 0x619961
        add("minecraft:vine");
    }};

    @Key("blocks.glass")
    @Comment("""
        List of blocks that are considered glass when it comes
        to coloring. Blocks listed here will be translucent
        if the renderer is configured for it.""")
    public static List<String> BLOCKS_GLASS = new ArrayList<>() {{
        add("minecraft:glass");
        add("minecraft:black_stained_glass");
        add("minecraft:blue_stained_glass");
        add("minecraft:brown_stained_glass");
        add("minecraft:cyan_stained_glass");
        add("minecraft:gray_stained_glass");
        add("minecraft:green_stained_glass");
        add("minecraft:light_blue_stained_glass");
        add("minecraft:light_gray_stained_glass");
        add("minecraft:lime_stained_glass");
        add("minecraft:magenta_stained_glass");
        add("minecraft:orange_stained_glass");
        add("minecraft:pink_stained_glass");
        add("minecraft:purple_stained_glass");
        add("minecraft:red_stained_glass");
        add("minecraft:white_stained_glass");
        add("minecraft:yellow_stained_glass");
        add("minecraft:glass_pane");
        add("minecraft:black_stained_glass_pane");
        add("minecraft:blue_stained_glass_pane");
        add("minecraft:brown_stained_glass_pane");
        add("minecraft:cyan_stained_glass_pane");
        add("minecraft:gray_stained_glass_pane");
        add("minecraft:green_stained_glass_pane");
        add("minecraft:light_blue_stained_glass_pane");
        add("minecraft:light_gray_stained_glass_pane");
        add("minecraft:lime_stained_glass_pane");
        add("minecraft:magenta_stained_glass_pane");
        add("minecraft:orange_stained_glass_pane");
        add("minecraft:pink_stained_glass_pane");
        add("minecraft:purple_stained_glass_pane");
        add("minecraft:red_stained_glass_pane");
        add("minecraft:white_stained_glass_pane");
        add("minecraft:yellow_stained_glass_pane");
        add("minecraft:tinted_glass");
    }};

    @Key("blocks.grass")
    @Comment("""
        List of blocks that are considered grass when it comes
        to coloring. Blocks listed here will use the biome's
        grass color modifier when rendering.""")
    public static List<String> BLOCKS_GRASS = new ArrayList<>() {{
        add("minecraft:bush");
        add("minecraft:fern");
        add("minecraft:grass");
        add("minecraft:grass_block");
        add("minecraft:large_fern");
        add("minecraft:potted_fern");
        add("minecraft:short_grass");
        add("minecraft:tall_grass");
    }};

    @Key("blocks.water")
    @Comment("""
        List of blocks that are considered water when it comes
        to coloring. Blocks listed here will use the biome's
        water color when rendering.""")
    public static List<String> BLOCKS_WATER = new ArrayList<>() {{
        add("minecraft:bubble_column");
        add("minecraft:kelp");
        add("minecraft:kelp_plant");
        add("minecraft:seagrass");
        add("minecraft:tall_seagrass");
        add("minecraft:water");
        add("minecraft:water_cauldron");
    }};

    @Key("blocks.flat")
    @Comment("""
        List of blocks that are considered "flat" when it comes
        to heightmaps. Blocks listed here will use the Y coordinate
        below them when rendering.""")
    public static List<String> BLOCKS_FLAT = new ArrayList<>() {{
        add("minecraft:acacia_pressure_plate");
        add("minecraft:acacia_trapdoor");
        add("minecraft:bamboo_pressure_plate");
        add("minecraft:bamboo_trapdoor");
        add("minecraft:birch_pressure_plate");
        add("minecraft:birch_trapdoor");
        add("minecraft:black_carpet");
        add("minecraft:blue_carpet");
        add("minecraft:brown_carpet");
        add("minecraft:cherry_pressure_plate");
        add("minecraft:cherry_trapdoor");
        add("minecraft:crimson_pressure_plate");
        add("minecraft:crimson_trapdoor");
        add("minecraft:cyan_carpet");
        add("minecraft:dark_oak_pressure_plate");
        add("minecraft:dark_oak_trapdoor");
        add("minecraft:green_carpet");
        add("minecraft:gray_carpet");
        add("minecraft:heavy_weighted_pressure_plate");
        add("minecraft:iron_trapdoor");
        add("minecraft:jungle_pressure_plate");
        add("minecraft:jungle_trapdoor");
        add("minecraft:leaf_litter");
        add("minecraft:light_blue_carpet");
        add("minecraft:light_gray_carpet");
        add("minecraft:light_weighted_pressure_plate");
        add("minecraft:lime_carpet");
        add("minecraft:magenta_carpet");
        add("minecraft:mangrove_pressure_plate");
        add("minecraft:mangrove_trapdoor");
        add("minecraft:moss_carpet");
        add("minecraft:oak_pressure_plate");
        add("minecraft:oak_trapdoor");
        add("minecraft:orange_carpet");
        add("minecraft:pale_moss_carpet");
        add("minecraft:pale_oak_pressure_plate");
        add("minecraft:pink_carpet");
        add("minecraft:polished_blackstone_pressure_plate");
        add("minecraft:purple_carpet");
        add("minecraft:red_carpet");
        add("minecraft:redstone_wire");
        add("minecraft:snow");
        add("minecraft:spruce_pressure_plate");
        add("minecraft:spruce_trapdoor");
        add("minecraft:stone_pressure_plate");
        add("minecraft:warped_pressure_plate");
        add("minecraft:warped_trapdoor");
        add("minecraft:white_carpet");
        add("minecraft:yellow_carpet");
    }};

    private static final BlocksConfig CONFIG = new BlocksConfig();

    /**
     * Constructs a new instance of BlocksConfig.
     */
    private BlocksConfig() {
        super(LiveMap.api().getDataPath().resolve("blocks.yml"));
    }

    /**
     * Reloads configuration from YAML file.
     */
    public static void reload() {
        CONFIG.reload0();
        CONFIG.save();
    }
}
