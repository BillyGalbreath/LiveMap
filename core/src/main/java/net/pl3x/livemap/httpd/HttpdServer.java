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

package net.pl3x.livemap.httpd;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.handlers.resource.ResourceManager;
import java.io.IOException;
import net.pl3x.livemap.Logger;
import net.pl3x.livemap.configuration.Config;

/**
 * The internal undertow web server.
 */
public class HttpdServer {
    private Undertow server;

    /**
     * Start the web server.
     */
    public void start() {
        if (this.server != null) {
            stop();
        }

        if (!Config.HTTPD_ENABLED) {
            Logger.info("&aInternal webserver is disabled");
            return;
        }

        try (ResourceManager resourceManager = new FriendlyUrlPathResourceManager()) {
            Logger.HIDE_UNDERTOW_LOGS = true;

            this.server = Undertow.builder()
                .addHttpListener(Config.HTTPD_PORT, Config.HTTPD_BIND)
                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .setHandler(new TilesPathHandler(resourceManager))
                .build();

            this.server.start();

            Logger.HIDE_UNDERTOW_LOGS = false;

            Logger.info("&aInternal webserver started on &e%s&a:&e%s".formatted(Config.HTTPD_BIND, Config.HTTPD_PORT));
        } catch (IOException e) {
            this.server = null;
            Logger.error("An error occurred starting the internal webserver", e);
        }
    }

    /**
     * Stop the web server.
     */
    public void stop() {
        if (this.server == null) {
            return;
        }

        Logger.HIDE_UNDERTOW_LOGS = true;

        this.server.stop();
        this.server = null;

        Logger.HIDE_UNDERTOW_LOGS = false;

        Logger.info("&aInternal webserver stopped");
    }
}
