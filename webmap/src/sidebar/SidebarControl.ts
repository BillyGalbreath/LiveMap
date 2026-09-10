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
import {PinControl} from "./PinControl";
import {RenderersControl} from "./RenderersControl";

export class SidebarControl {
  private readonly _livemap: LiveMap;
  private readonly _dom: HTMLElement;

  private readonly _renderersControl: RenderersControl;

  constructor(livemap: LiveMap) {
    this._livemap = livemap;

    this._renderersControl = new RenderersControl(this._livemap);

    this._dom = L.DomUtil.create("aside");

    //if (livemap.settings.ui.sidebar != "hide") {
    document.body.prepend(this._dom);
    //}

    // set up and show/hide the pin
    const pin: PinControl = new PinControl(this._livemap, this._dom);
    this.show(pin.pinned);

    // set up the logo fancy div magic
    const holder: HTMLElement = L.DomUtil.create("div", "", this._dom);
    const logo: HTMLElement = L.DomUtil.create("div", "title", holder);
    L.DomUtil.create("div", "", holder); // this one is squishy :3

    const logolink: string = "https://mods.vintagestory.at/livemap";//this._livemap.settings.ui.logolink;
    const logotext: string = "LiveMap";//this._livemap.settings.ui.logotext;

    let rawhtml: string = this._livemap.ui.logo;
    if (logolink) {
      rawhtml += `<span>${logotext}</span>`;
    } else {
      rawhtml += `<a href="${logolink}">${logotext}</a>`;
    }
    rawhtml += `<span></span>`;
    logo.insertAdjacentHTML("beforeend", rawhtml);

    // add these after the logo
    this._dom.appendChild(this.renderersControl.dom);
    //this._dom.appendChild(this._livemap.playersLayer.dom);

    this._dom.onclick = (): void => {
      // todo followPlayerMarker(null)
    };
    this._dom.onmouseleave = (): void => {
      if (!pin.pinned) {
        this.show(false);
      }
    };
    this._dom.onmouseenter = (): void => {
      if (!pin.pinned) {
        this.show(true);
      }
    };
  }

  get renderersControl(): RenderersControl {
    return this._renderersControl;
  }

  public show(show: boolean): void {
    this._dom.className = show ? "show" : "";
  }
}
