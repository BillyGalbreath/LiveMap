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

import {LiveMap} from "../LiveMap";
import {World} from "../world/World";
import {Point} from "./Point";

export class Url {
    private readonly _livemap: LiveMap;
    private readonly _basePath: string;
    private readonly _world?: string;
    private readonly _renderer?: string;
    private readonly _zoom?: number;
    private readonly _point?: Point;

    constructor(livemap: LiveMap, url: string, world?: World | undefined, point?: Point | undefined) {
        this._livemap = livemap;

        this._world = world?.id;
        this._renderer = world?.currentRenderer?.id;
        this._zoom = world?.currentZoom();
        this._point = point;

        if (this._world) {
            this._basePath = "/";
        } else {
            const match: RegExpExecArray | null = /^\/(.+?)(?:\/(.+?)?\/?(-?\d+)?\/?(-?\d+)?\/?(-?\d+)?(?:\/(.+)?)?)?$/.exec(url);
            if (match) {
                this._basePath = "/";
                this._world = match[1];
                this._renderer = match[2];
                this._zoom = Number(match[3]);
                this._point = Point.of(match[4] ?? 0, match[5] ?? 0);
            } else {
                this._basePath = window.location.pathname?.split("?")[0]?.replace("index.html", "") ?? "/";
                const url: URLSearchParams = new URLSearchParams(window.location.search);
                this._world = url.get("world") ?? undefined;
                this._renderer = url.get("renderer") ?? undefined;
                this._zoom = Number(url.get("zoom") ?? 0);
                this._point = Point.of(url.get("x") ?? 0, url.get("z") ?? 0);
            }
        }
    }

    get basePath(): string {
        return this._basePath;
    }

    get world(): string | undefined {
        return this._world;
    }

    get renderer(): string | undefined {
        return this._renderer;
    }

    get zoom(): number | undefined {
        return this._zoom;
    }

    get point(): Point | undefined {
        return this._point;
    }

    public toString(): string {
        return (this._livemap.friendly_urls ? `%s%s/%s/%i/%i/%i/` : `%s?world=%s&renderer=%s&zoom=%i&x=%i&z=%i`)
            .formatted(this.basePath, this.world, this.renderer, this.zoom, this.point?.x, this.point?.z);
    }
}
