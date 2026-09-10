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

export class PinControl {
  private readonly _dom: HTMLElement;
  private readonly _svg: SVGSVGElement;

  private _pinned: boolean = false;

  constructor(livemap: LiveMap, parent: HTMLElement) {
    this._dom = L.DomUtil.create("div", "", parent);
    this._dom.id = "pin";
    this._dom.onclick = (): void => {
      this.pin(!this.pinned);
      localStorage.setItem("sidebar.pinned", this.pinned ? "pinned" : "unpinned");
    };

    this._dom.appendChild(window.createSVGIcon("pin"));
    this._svg = this._dom.querySelector("svg")!;

    this.pin(livemap.ui.sidebar == "pinned" || localStorage.getItem("sidebar.pinned") == "pinned");
  }

  public get pinned(): boolean {
    return this._pinned;
  }

  public pin(pinned: boolean): void {
    this._pinned = pinned;

    this._dom.className = pinned ? "pinned" : "unpinned";
    const text: string = window.lang(`sidebar.${this._dom.className}`);

    this._svg.setAttribute("alt", text);
    this._svg.setAttribute("title", text);
  }
}
