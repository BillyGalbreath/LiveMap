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
import {Point} from "../data/Point";
import {Zooms} from "../data/Zooms";
import {Url} from "../data/Url";
import {BlockInfo} from "../palette/BlockInfo";
import {Renderer} from "./Renderer";

export class World {
    private readonly _livemap: LiveMap;

    private readonly _id: string;
    private readonly _display_name: string;
    private readonly _order: number;
    private readonly _type: string;
    private readonly _center: Point;
    private readonly _spawn: Point;
    private readonly _zooms: Zooms;
    private readonly _renderers: Map<string, Renderer> = new Map();

    private _renderer?: Renderer;

    private _biomePalette: Map<number, string> = new Map();
    private _blockInfo: Map<number, Map<string, BlockInfo>> = new Map();

    private _tickTimer?: NodeJS.Timeout;

    constructor(livemap: LiveMap, world: World) {
        this._livemap = livemap;

        this._id = world.id;
        this._display_name = world.display_name;
        this._order = world.order;
        this._type = world.type;
        this._center = Point.of(world.center);
        this._spawn = Point.of(world.spawn);
        this._zooms = new Zooms(world.zooms);

        world.renderers.forEach((renderer: Renderer): void => {
            // must re-initialize to properly run ctor
            renderer = new Renderer(this, renderer);
            this.renderers.set(renderer.id, renderer);
            window.customEvent("rendererAdded", renderer);
        });

        window.fetchPalette(`tiles/${this.id}/biomes.gz`, "biome", this._biomePalette);
    }

    get id(): string {
        return this._id;
    }

    get display_name(): string {
        return this._display_name;
    }

    get type(): string {
        return this._type;
    }

    get order(): number {
        return this._order;
    }

    get spawn(): Point {
        return this._spawn;
    }

    get center(): Point {
        return this._center;
    }

    get zooms(): Zooms {
        return this._zooms;
    }

    get renderers(): Map<string, Renderer> {
        return this._renderers;
    }

    get renderer(): Renderer {
        return this._renderer ??= this.renderers.values().next().value!;
    }

    get blockInfo(): Map<number, Map<string, BlockInfo>> {
        return this._blockInfo;
    }

    get biomePalette(): Map<number, string> {
        return this._biomePalette;
    }

    get background(): string {
        switch (this.type) {
            case "nether":
                return `url("images/sky/nether.png")`;
            case "the_end":
                return `url("images/sky/the_end.png")`;
            case "overworld":
            default:
                return `url("images/sky/overworld.png")`;
        }
    }

    public currentZoom(): number {
        return this.zooms.max_out - this._livemap.getZoom();
    }

    private tick(): void {
        // do not "redraw". use "refresh" to prevent flickering and flashing
        this.renderer.refresh();

        // schedule next tick
        this._tickTimer = setTimeout(
            (): void => this.tick(),
            this._livemap.update_interval * 1000
        );
    }

    public setRenderer(rendererId?: string): void {
        // stop tick timer
        clearTimeout(this._tickTimer);

        // remove old renderer tile layer from map
        this._renderer?.remove();

        // update min/max zoom limits
        this._livemap.options.maxZoom = this.zooms.max_out + this.zooms.max_in;
        this._livemap.setMaxZoom(this.zooms.max_out + this.zooms.max_in);

        // make sure we have a real renderer
        if (rendererId === undefined) {
            // get from url if none specified
            const url = new Url(this._livemap, window.location.pathname);
            rendererId = url.renderer;
        }

        const renderer: Renderer | undefined = this._renderers.get(rendererId);
        if (renderer) {
            this._renderer = renderer;
        }

        // set new renderer tiles layer and add to map
        this.renderer.addTo(this._livemap);

        const url: Url = new Url(this._livemap, window.location.pathname);
        this._livemap.centerOn(url.point, url.zoom);
        this._livemap.linkControl.update();

        this._livemap.getContainer().style.backgroundImage = this.background;

        // start ticking
        this.tick();

        window.customEvent("rendererSelected", renderer);
    }

    public loadBlockInfo(zoom: number, x: number, z: number): void {
        window.fetchBytes<ArrayBuffer>(`tiles/${this.id}/${zoom}/blockinfo/${x}_${z}.livemap.gz`)
            .then((buffer?: ArrayBuffer): void => {
                this.setBlockInfo(zoom, x, z, buffer);
            });
    }

    public getBlockInfo(zoom: number, x: number, z: number): BlockInfo | undefined {
        return this.blockInfo.get(zoom < 0 ? 0 : zoom)?.get(`${x}_${z}`);
    }

    public setBlockInfo(zoom: number, x: number, z: number, buffer?: ArrayBuffer): void {
        let infoMap: Map<string, BlockInfo> | undefined = this.blockInfo.get(zoom < 0 ? 0 : zoom);
        if (infoMap == undefined) {
            infoMap = new Map<string, BlockInfo>();
            this.blockInfo.set(zoom, infoMap);
        }

        if (buffer == undefined) {
            infoMap.delete(`${x}_${z}`);
        } else {
            infoMap.set(`${x}_${z}`, new BlockInfo(new Uint8Array(buffer)));
        }

        this._livemap.blockInfoControl.update(Point.ZERO);
        this._livemap.coordsControl.update();
    }

    public unsetBlockInfo(zoom: number, x: number, z: number): void {
        this.blockInfo.get(zoom)?.delete(`${x}_${z}`);
    }
}
