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

export class ScaleControl extends ControlBox {
  private readonly _dom: HTMLElement;

  constructor(livemap: LiveMap) {
    super(livemap, livemap.ui.scale);

    this._dom = L.DomUtil.create("div", "leaflet-control-layers scale no-hover");
    this._dom.title = window.lang("scale.title");

    this._dom.appendChild(document.createElement("span"));

    this.addTo(livemap);
  }

  onAdd(map: L.Map): HTMLElement {
    map.on('move', this._update, this);
    map.whenReady(this._update, this);

    return this._dom;
  }

  onRemove(map: L.Map): void {
    map.off('move', this._update, this);

  }

  _update() {
    const distance: number = this._livemap.distance(
      this._livemap.containerPointToLatLng([0, 0]),
      this._livemap.containerPointToLatLng([2000, 0])
    );

    const meters: number = this._getRoundNum(distance);
    this._dom.style.width = `${Point.pixelsToMeters(Math.round(2000 * meters / distance))}px`;
    this._dom.children[0].innerHTML = meters < 1000 ? meters + ' m' : (meters / 1000) + ' km';
  }

  _getRoundNum(num: number): number {
    const pow10: number = Math.pow(10, `${Math.floor(num)}`.length - 1);
    const d: number = num / pow10;
    return pow10 * (d >= 10 ? 10 : d >= 5 ? 5 : d >= 3 ? 3 : d >= 2 ? 2 : 1);
  }
}
