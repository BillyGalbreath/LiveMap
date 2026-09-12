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
import {BlockInfo} from "../palette/BlockInfo";
import {Block} from "../palette/Block";
import {ControlBox} from "./ControlBox";
import {World} from "../world/World";

export class BlockInfoControl extends ControlBox {
  private readonly _dom: HTMLElement;

  private _blockPalette: Map<number, string> = new Map();

  constructor(livemap: LiveMap) {
    super(livemap, livemap.ui.blockinfo);

    this._dom = L.DomUtil.create("div", "leaflet-control-layers blockinfo");
    this._dom.title = window.lang("blockinfo.title");

    L.DomEvent.disableClickPropagation(this._dom);

    window.fetchPalette('tiles/blocks.gz', "block", this._blockPalette);

    this.addTo(livemap);
  }

  onAdd(map: L.Map): HTMLElement {
    map.addEventListener("mousemove", this.onEvent);
    this.update();
    return this._dom;
  }

  onRemove(map: L.Map): void {
    map.removeEventListener("mousemove", this.onEvent);
  }

  private onEvent: (e?: L.LeafletMouseEvent) => void = (e?: L.LeafletMouseEvent): void => {
    return this.update(Point.of(e?.latlng ?? 0));
  }

  public update(point?: Point): void {
    const world: World | undefined = this._livemap.currentWorld;
    if (world == undefined) {
      return;
    }

    const x: number = point?.x ?? 0;
    const z: number = point?.z ?? 0;
    const regionX: number = x >> 9;
    const regionZ: number = z >> 9;
    const zoom: number = world.currentZoom() < 0 ? 0 : world.currentZoom();
    const step: number = 1 << zoom;
    const fileX: number = Math.floor(regionX / step);
    const fileZ: number = Math.floor(regionZ / step);
    const tileX: number = (x / step) & 511;
    const tileZ: number = (z / step) & 511;

    let blockName: string = window.lang("blockinfo.unknown.block");
    let biomeName: string = window.lang("blockinfo.unknown.biome");
    let y: number | undefined;

    const blockInfo: BlockInfo | undefined = world.getBlockInfo(zoom, fileX, fileZ);
    if (blockInfo !== undefined) {
      const block: Block = blockInfo.getBlock(tileX, tileZ);
      if (block != null) {
        if (block.block != 0) {
          blockName = this._blockPalette.get(block.block) ?? blockName;
        }
        if (block.biome != 0) {
          biomeName = world.biomePalette.get(block.biome) ?? biomeName;
        }

        if (block.block != 0) {
          y = block.yPos + 1;
        }
      }
    }

    this._livemap.coordsControl.y = y;
    this._dom.innerHTML = window.lang("blockinfo.value")
      .replace(/<block>/g, blockName)
      .replace(/<biome>/g, biomeName);
  }
}
