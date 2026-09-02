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

import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceChangeListener;
import io.undertow.server.handlers.resource.ResourceManager;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.pl3x.livemap.LiveMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class FriendlyUrlPathResourceManager implements ResourceManager {
    private static final Pattern FRIENDLY_URLS = Pattern.compile("^/(.+?)(?:/(.+?)?/?(-?\\d+)?/?(-?\\d+)?/?(-?\\d+)?(?:/(.+)?)?)?$");

    private final ResourceManager wrapped;

    FriendlyUrlPathResourceManager() {
        this.wrapped = PathResourceManager.builder()
            .setBase(LiveMap.api().getWebDir())
            .setETagFunction(new LastModifiedETagFunction())
            .build();
    }

    @Override
    @Nullable
    public Resource getResource(@Nullable String input) throws IOException {
        // this is a cheap way of handling friendly urls. the server has
        // zero care about what world/renderer/zoom/coords the client
        // wants so we basically just chop out those parts from the
        // requested input and serve the requested content left after
        // all that. the client will not see this alteration, so it will
        // do the actual parsing of the world/renderer/zoom/coords from
        // the url for us.

        if (input != null && !input.isEmpty()) {
            Matcher matcher = FRIENDLY_URLS.matcher(input);
            // check for friendly url format and that world requested exists
            if (matcher.find() && LiveMap.api().getWorldRegistry().get(matcher.group(1)) != null) {
                // serve the requested destination without world, renderer,
                // zoom, and coords to let the client figure out this mess.
                input = matcher.group(6);
            }
        }

        // let the real PathResourceManager do its thing with the altered input
        return this.wrapped.getResource(input == null ? "/" : input);
    }

    @Override
    public boolean isResourceChangeListenerSupported() {
        return this.wrapped.isResourceChangeListenerSupported();
    }

    @Override
    public void registerResourceChangeListener(@NotNull ResourceChangeListener listener) {
        this.wrapped.registerResourceChangeListener(listener);
    }

    @Override
    public void removeResourceChangeListener(@NotNull ResourceChangeListener listener) {
        this.wrapped.removeResourceChangeListener(listener);
    }

    @Override
    public void close() throws IOException {
        this.wrapped.close();
    }
}
