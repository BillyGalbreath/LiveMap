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
import {BlockInfo, World} from "../world/World";
import {ControlBox} from "./ControlBox";

export class BlockInfoControl extends ControlBox {
    private readonly _dom: HTMLElement;

    private _blockPalette: Map<number, string> = new Map();

    constructor(livemap: LiveMap) {
        super(livemap, "bottomleft");

        this._dom = L.DomUtil.create("div", "leaflet-control-layers blockinfo");
        this._dom.title = window.lang("blockinfo.title");

        L.DomEvent.disableClickPropagation(this._dom);

        window.fetchPalette('tiles/blocks.gz', "block", this._blockPalette);

        this.addTo(livemap);
    }

    onAdd(map: L.Map): HTMLElement {
        map.addEventListener("mousemove", this.onEvent);
        // this.update();
        return this._dom;
    }

    onRemove(map: L.Map): void {
        map.removeEventListener("mousemove", this.onEvent);
    }

    private onEvent: (e?: L.LeafletMouseEvent) => void = (e?: L.LeafletMouseEvent): void => {
        return this.update(Point.of(e?.latlng ?? 0));
    }

    public update(point?: Point): void {
        const world: World = this._livemap.worldManager.currentWorld;

        const x: number = point?.x ?? 0;
        const z: number = point?.z ?? 0;
        const regionX: number = x >> 9;
        const regionZ: number = z >> 9;
        const zoom: number = world.currentZoom() < 0 ? 0 : world.currentZoom();
        const step: number = 1 << zoom;
        const fileX: number = Math.floor(regionX / step);
        const fileZ: number = Math.floor(regionZ / step);
        const tileX: number = Math.floor((x / step) & 511);
        const tileZ: number = Math.floor((z / step) & 511);

        let blockName: string = window.lang("blockinfo.unknown.block");
        let biomeName: string = window.lang("blockinfo.unknown.biome");
        let y: number | undefined;

        const blockInfo: BlockInfo | undefined = world.getBlockInfo(zoom, fileX, fileZ);
        if (blockInfo !== undefined) {
            const index: number = (tileZ << 9) | tileX;
            const offset: number = 16 + index * 8;
            const packed: bigint = blockInfo.view.getBigUint64(offset, false);
            if (packed !== undefined) {
                const blockId: number = Number((packed >> 32n) & 0xFFFFn);
                const biomeId: number = Number((packed >> 16n) & 0xFFFFn);
                const yPos: number = Number(packed & 0xFFFFn);

                if (blockId !== 0) {
                    blockName = this._blockPalette.get(blockId) ?? blockName;
                    y = yPos + blockInfo.minY + 1;
                }
                if (biomeId !== 0) {
                    biomeName = world.biomePalette.get(biomeId) ?? biomeName;
                }
            }
        }

        this._livemap.coordsControl.y = y;
        this._dom.innerHTML = window.lang("blockinfo.value")
            .replace(/<block>/g, blockName)
            .replace(/<biome>/g, biomeName);
    }
}
