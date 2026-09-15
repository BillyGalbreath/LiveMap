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

export class FancierMap extends L.Map {
    declare _controlCorners: { [x: string]: HTMLDivElement; };
    declare _controlContainer?: HTMLElement;
    declare _container?: HTMLElement;

    constructor(options: LiveMap) {
        super("map", {
            // we need a flat and simple crs
            crs: L.Util.extend(L.CRS.Simple, {
                // we need to flip the y-axis to match minecraft's z-axis
                // https://stackoverflow.com/a/62320569/3530727
                transformation: new L.Transformation(1, 0, 1, 0)
            }),
            // center map in World#setRenderer
            center: [0, 0],
            // always allow attribution in case a layer needs it
            attributionControl: true,
            // canvas is more efficient than svg
            preferCanvas: true,
            // prevent tile reloads from flashing
            fadeAnimation: true,
            // don't add zoom control here, we'll do it manually below the scale control
            zoomControl: false,

            // chrome based browsers on linux zoom twice as fast, so we have to double the ratio
            // effectively undoes the fix for Leaflet/Leaflet#4538 and Leaflet/Leaflet#7403
            // https://github.com/Leaflet/Leaflet/commit/96977a19358374b0166cff049862fa1f0fed5948
            //
            // remove this logic when this bug gets fixed: https://issues.chromium.org/issues/40887377
            // it seems intentional, so it might not get fixed https://issues.chromium.org/issues/40804672
            // well, they said they fixed it, but it still seems a problem here, so this workaround remains
            wheelPxPerZoomLevel: L.Browser.linux && L.Browser.chrome ? 120 : 60,
            // these get weird when changed. so don't.
            zoomSnap: 1,
            zoomDelta: 1,

            // moved to World#setRenderer
            maxZoom: 0,
            // the closest zoomed in possible (without stretching)
            // this is always 0. no exceptions!
            minZoom: 0
        });
    }

    // https://stackoverflow.com/a/60391674/3530727
    // noinspection JSUnusedGlobalSymbols - used internally
    _initControlPos(): void {
        this._controlCorners = {};
        this._controlContainer = L.DomUtil.create("div", "leaflet-control-container", this._container);
        const top: HTMLDivElement = L.DomUtil.create("div", `leaflet-control-container-top`, this._controlContainer);
        const middle: HTMLDivElement = L.DomUtil.create("div", `leaflet-control-container-middle`, this._controlContainer);
        const bottom: HTMLDivElement = L.DomUtil.create("div", `leaflet-control-container-bottom`, this._controlContainer);
        this._controlCorners[`topleft`] = L.DomUtil.create("div", `leaflet-top leaflet-left`, top);
        this._controlCorners[`topcenter`] = L.DomUtil.create("div", `leaflet-top leaflet-center`, top);
        this._controlCorners[`topright`] = L.DomUtil.create("div", `leaflet-top leaflet-right`, top);
        this._controlCorners[`middleleft`] = L.DomUtil.create("div", `leaflet-middle leaflet-left`, middle);
        this._controlCorners[`middlecenter`] = L.DomUtil.create("div", `leaflet-middle leaflet-center`, middle);
        this._controlCorners[`middleright`] = L.DomUtil.create("div", `leaflet-middle leaflet-right`, middle);
        this._controlCorners[`bottomleft`] = L.DomUtil.create("div", `leaflet-bottom leaflet-left`, bottom);
        this._controlCorners[`bottomcenter`] = L.DomUtil.create("div", `leaflet-bottom leaflet-center`, bottom);
        this._controlCorners[`bottomright`] = L.DomUtil.create("div", `leaflet-bottom leaflet-right`, bottom);
    }
}
