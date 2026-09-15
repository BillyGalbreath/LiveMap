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

import * as L from "leaflet";
import {LiveMap} from "../LiveMap";
import {Point} from "../data/Point";
import {Url} from "../data/Url";
import {World} from "../world/World";
import {ControlBox} from "./ControlBox";

export class LinkControl extends ControlBox {
    private readonly _dom: HTMLAnchorElement;

    constructor(livemap: LiveMap) {
        super(livemap, "bottomleft");

        this._dom = L.DomUtil.create("a", "leaflet-control-layers link");
        this._dom.title = window.lang("link.title");
        this._dom.appendChild(window.createSVGIcon("link"));

        L.DomEvent.disableClickPropagation(this._dom);

        this._dom.onclick = (e: MouseEvent): void => {
            e.preventDefault();
            window.history.replaceState({}, window.lang("title"), this._dom.href);
            this._livemap.contextMenu.share(Point.of(this._livemap.getCenter()).round());
        }

        // add to the map once we have a dom to add
        this.addTo(livemap);
    }

    onAdd(map: L.Map): HTMLAnchorElement {
        map.addEventListener("moveend", this.update);
        map.addEventListener("zoomend", this.update);
        return this._dom;
    }

    onRemove(map: L.Map): void {
        map.removeEventListener("moveend", this.update);
        map.removeEventListener("zoomend", this.update);
    }

    public update: () => void = (): void => {
        this._dom.href = this.getUrlFromView().toString();
        // find out how to prevent chrome from spamming history
        window.history.replaceState({}, "", this._dom.href);
    }

    public getUrlFromView(): Url {
        return this.getUrlFromPoint(Point.of(this._livemap.getCenter()).round());
    }

    public getUrlFromPoint(point: Point): Url {
        const world: World = this._livemap.worldManager.current
        const url: Url = new Url(this._livemap, window.location.pathname);
        return new Url(
            this._livemap,
            url.basePath,
            world.id,
            world.renderer.id,
            world.currentZoom(),
            point.x,
            point.z
        );
    }
}
