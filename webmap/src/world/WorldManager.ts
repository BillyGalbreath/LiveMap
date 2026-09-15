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
import {Url} from "../data/Url";
import {SortedMap} from "../util/SortedMap";
import {World} from "./World";

export class WorldManager {
    private readonly _livemap: LiveMap;

    private readonly _worlds: SortedMap<string, World> = new SortedMap(null, (a: readonly [string, World], b: readonly[string, World]) => a[1].order - b[1].order);

    private _current?: World;

    constructor(livemap: LiveMap) {
        this._livemap = livemap;

        const total: number = livemap.worlds.length;
        let count: number = 0;

        livemap.worlds.forEach((worldId: string): void => {
            window.fetchJson<World>(`tiles/${worldId}/settings.json`)
                .then((world: World): void => {
                    // must re-initialize to properly run ctor
                    world = new World(this._livemap, world);
                    this._worlds.set(world.id, world);
                    window.customEvent("worldAdded", world);
                })
                .catch((err: unknown): void => {
                    console.error(`Error loading world ${worldId}\n`, err);
                })
                .finally(() => {
                    if (++count < total) {
                        return; // wait for more worlds to load
                    }
                    this.setWorld();
                });
        });
    }

    get current(): World {
        return this._current ??= this._worlds.values().next().value!;
    }

    get worlds(): SortedMap<string, World> {
        return this._worlds;
    }

    public setWorld(worldId?: string): void {
        let rendererId: string | undefined;
        if (worldId == undefined) {
            // get from url if none specified
            const url = new Url(this._livemap, window.location.pathname);
            worldId = url.world;
            rendererId = url.renderer;
        }

        const world: World | undefined = this._worlds.get(worldId);
        if (world) {
            this._current = world;
            world.setRenderer(rendererId);
        }

        const url: Url = new Url(this._livemap, window.location.pathname);
        this._livemap.centerOn(url.point, url.zoom);
        this._livemap.linkControl.update();

        window.customEvent("worldSelected", world);
    }
}
