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
import {Sidebar} from "./Sidebar";
import {World} from "../world/World";
import {Renderer} from "../world/Renderer";

export class WorldsList {
    private readonly _dom: HTMLDetailsElement;
    private readonly _worldList: HTMLUListElement;

    private readonly _sidebar: Sidebar;

    constructor(sidebar: Sidebar, parentDom: HTMLElement) {
        this._sidebar = sidebar;

        this._dom = L.DomUtil.create("details", "", parentDom);
        this._dom.open = true;

        const summary: HTMLElement = L.DomUtil.create("summary", "", this._dom);
        summary.textContent = window.lang("worlds.label");

        this._worldList = L.DomUtil.create("ul", "", this._dom);
        this._worldList.id = "worlds";

        addEventListener("worldAdded", () => this.rebuildList());
    }

    private rebuildList(): void {
        this._worldList.innerHTML = "";

        this._sidebar.livemap.worldManager.worlds.forEach((world: World) => {
            const worldEntry: HTMLLIElement = L.DomUtil.create("li", "", this._worldList);
            const p: HTMLParagraphElement = L.DomUtil.create("p", "", worldEntry);
            p.textContent = world.display_name;

            const rendererList: HTMLUListElement = L.DomUtil.create("ul", "", worldEntry);
            rendererList.id = "renderers";
            world.renderers.forEach((renderer: Renderer) => {
                this.rendererBtn(renderer, rendererList)
            });
        });
    }

    private rendererBtn(renderer: Renderer, list: HTMLUListElement): HTMLLIElement {
        const entry: HTMLLIElement = L.DomUtil.create("li", "", list);
        const img: HTMLButtonElement = L.DomUtil.create("button", "", entry);
        img.style.backgroundImage = `url('images/icon/renderers/${renderer.icon}')`;
        img.title = renderer.name;
        img.onclick = (ev: Event) => {
            renderer.world.setRenderer(renderer.id);
            //window.customEvent("rendererSelected", renderer);
        }
        return entry;
    }
}
