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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.pl3x.livemap.configuration.Config;
import org.bstats.MetricsBase;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.CustomChart;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.json.JsonObjectBuilder;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

class Metrics {
    private final MetricsBase metricsBase;

    /**
     * Creates a new Metrics instance.
     */
    Metrics() {
        // Get and load the config file
        YamlFile config = new YamlFile(LiveMap.api().getDataPath().getParent()
            .resolve("bStats").resolve("config.yml").toFile());
        try {
            config.load();
        } catch (IOException ignore) {
        }

        boolean isFolia = false;
        try {
            // noinspection ConstantValue
            isFolia = Class.forName("io.papermc.paper.threadedregions.RegionizedServer") != null;
        } catch (Exception ignore) {
        }

        metricsBase = new MetricsBase(
            "bukkit", // report all data to the bukkit page
            config.getString("serverUuid", UUID.randomUUID().toString()),
            26542, // LiveMapMC
            config.getBoolean("enabled", true),
            this::appendPlatformData,
            this::appendServiceData,
            // See https://github.com/Bastian/bstats-metrics/pull/126
            isFolia ? null : submitDataTask -> LiveMap.api().getTickScheduler().addTask(0, false, submitDataTask),
            () -> LiveMap.api().isEnabled(),
            Logger::warn,
            Logger::info,
            config.getBoolean("logFailedRequests", false),
            config.getBoolean("logSentData", false),
            config.getBoolean("logResponseStatusText", false),
            false
        );

        addCustomChart(new SimplePie("unfiltered_server_software", () -> LiveMap.api().getPlatformName()));
        addCustomChart(new SimplePie("language_used", () -> Config.LANGUAGE_FILE.replace(".yml", "")));
        addCustomChart(new SimplePie("internal_web_server", () -> Boolean.toString(Config.HTTPD_ENABLED)));
        addCustomChart(new AdvancedPie("renderers_used", () -> new HashMap<>() {{
            // loop over worlds
            LiveMap.api().getWorldRegistry().forEach((_, world) ->
                // loop over renderers
                world.getRendererRegistry().forEach((renderer, _) ->
                    // increment count for renderer
                    put(renderer, getOrDefault(renderer, 0) + 1)
                )
            );
        }}));
        addCustomChart(new DrilldownPie("plugin_version", () -> {
            // split version and build for drilldown
            String[] version = LiveMap.api().getVersion().split("-");
            return Map.of(version[0], Map.of(version[1], 1));
        }));
    }

    /**
     * Shuts down the underlying scheduler service.
     */
    public void shutdown() {
        this.metricsBase.shutdown();
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(@NotNull CustomChart chart) {
        this.metricsBase.addCustomChart(chart);
    }

    private void appendPlatformData(@NotNull JsonObjectBuilder builder) {
        builder.appendField("playerAmount", LiveMap.api().getPlayerRegistry().size());
        builder.appendField("onlineMode", LiveMap.api().getOnlineMode() ? 1 : 0);
        builder.appendField("bukkitVersion", LiveMap.api().getPlatformVersion());
        builder.appendField("bukkitName", LiveMap.api().getPlatformName());

        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(@NotNull JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", LiveMap.api().getVersion());
    }
}
