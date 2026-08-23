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

import io.undertow.UndertowLogger;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import java.nio.file.Files;
import java.nio.file.Path;
import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.util.FileUtil;
import org.jetbrains.annotations.NotNull;

final class TilesPathHandler extends io.undertow.server.handlers.PathHandler {
    private final ResourceHandler handler;

    TilesPathHandler(@NotNull ResourceManager manager) {
        this.handler = new ResourceHandler(manager, new ErrorHandler());
    }

    @Override
    public void handleRequest(@NotNull HttpServerExchange exchange) throws Exception {
        String url = exchange.getRelativePath();
        if (url.contains("/tiles/")) {
            // do not cache anything in the tiles directory (includes json files)
            exchange.getResponseHeaders().put(Headers.CACHE_CONTROL, "max-age=0, must-revalidate, no-cache");
        }
        handler.handleRequest(exchange);
    }

    private static class ErrorHandler implements HttpHandler {
        @Override
        public void handleRequest(@NotNull HttpServerExchange exchange) {
            String url = exchange.getRelativePath();
            if (url.contains("/tiles/") && url.endsWith(".png")) {
                // do not 404 on missing tiles (keeps client log clean)
                exchange.setStatusCode(StatusCodes.OK);
                return;
            }

            // set the 404 status
            exchange.setStatusCode(StatusCodes.NOT_FOUND);

            // check for a 404.html file
            Path file = LiveMap.api().getWebDir().resolve("404.html");
            if (Files.exists(file)) {
                // serve the 404.html file
                exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
                exchange.getResponseSender().send(FileUtil.readString(file));
            }

            // standard undertow logging
            if (UndertowLogger.PREDICATE_LOGGER.isDebugEnabled()) {
                UndertowLogger.PREDICATE_LOGGER.debugf("Response code set to [%s] for %s.", StatusCodes.NOT_FOUND, exchange);
            }
        }
    }
}
