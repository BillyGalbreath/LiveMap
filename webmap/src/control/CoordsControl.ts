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
import {ControlBox} from "./ControlBox";

export class CoordsControl extends ControlBox {
  private readonly _dom: HTMLElement;

  private _point: Point = Point.of(0);
  private _y?: number;

  constructor(livemap: LiveMap) {
    super(livemap, livemap.ui.coords);

    this._dom = L.DomUtil.create("div", "leaflet-control-layers coordinates");
    this._dom.title = window.lang("coordinates.title");

    L.DomEvent.disableClickPropagation(this._dom);

    this.addTo(livemap);
  }

  onAdd(map: L.Map): HTMLElement {
    map.addEventListener("mousemove", this.update);
    this.update();
    return this._dom;
  }

  onRemove(map: L.Map): void {
    map.removeEventListener("mousemove", this.update);
  }

  public update: (e?: L.LeafletMouseEvent) => void = (e?: L.LeafletMouseEvent): void => {
    // update x,z coords
    this.point = Point.of(e?.latlng ?? 0).round();

    // update blockinfo (blockinfo will update our y coordinate)
    this._livemap.blockInfoControl?.update(this.point);

    // update the dom text
    this._dom.innerHTML = window.lang("coords.value")
      .replace(/<x>/g, this.point.x.toString().padStart(6, ' '))
      .replace(/<y>/g, (this.y?.toString() ?? '???').padStart(2, ' ').padEnd(3, ' '))
      .replace(/<z>/g, this.point.z.toString().padEnd(6, ' '));
  }

  get point(): Point {
    return this._point;
  }

  set point(point: Point) {
    this._point = point;
  }

  get y(): number | undefined {
    return this._y;
  }

  set y(y: number | undefined) {
    this._y = y;
  }
}
