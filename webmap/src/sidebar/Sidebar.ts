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
import {PlayersList} from "./PlayersList";
import {WorldsList} from "./WorldsList";
import "../css/sidebar.css";

export class Sidebar {
    private readonly _livemap: LiveMap;
    private readonly _dom: HTMLElement;

    private readonly _pinControl: PinControl;
    private readonly _worldsList: WorldsList;
    private readonly _playersList: PlayersList;

    constructor(livemap: LiveMap) {
        this._livemap = livemap;

        this._dom = L.DomUtil.create("aside");

        //if (livemap.settings.ui.sidebar != "hide") {
        document.body.prepend(this._dom);
        //}

        // set up and show/hide the pin
        this._pinControl = new PinControl(this, this._dom);
        this.show(this._pinControl.pinned);

        // hide off-screen until map is ready
        this._dom.classList.add("hide");

        // set up the logo fancy div magic
        const holder: HTMLElement = L.DomUtil.create("div", "", this._dom);
        const logo: HTMLElement = L.DomUtil.create("div", "title", holder);
        L.DomUtil.create("div", "", holder); // this one is squishy :3

        const logohref: string = window.lang("sidebar.href");
        const logotext: string = window.lang("sidebar.title");

        let logoimg: string = window.lang("sidebar.logo");
        if (logohref) {
            logoimg += `<span>${logotext}</span>`;
        } else {
            logoimg += `<a href="${logohref}">${logotext}</a>`;
        }
        logoimg += `<span></span>`;
        logo.insertAdjacentHTML("beforeend", logoimg);

        // worlds
        this._worldsList = new WorldsList(this, this._dom);

        // players
        this._playersList = new PlayersList();

        this._dom.onclick = (): void => {
            // followPlayerMarker
        };
    }

    get livemap(): LiveMap {
        return this._livemap;
    }

    public show(show: boolean): void {
        this._dom.className = show ? "show" : "";
    }
}
