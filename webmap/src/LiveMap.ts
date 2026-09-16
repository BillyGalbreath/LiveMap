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
import {BlockInfoControl} from "./control/BlockInfoControl";
import {CoordsControl} from "./control/CoordsControl";
import {LinkControl} from "./control/LinkControl";
import {ScaleControl} from "./control/ScaleControl";
import {Lang} from "./data/Lang";
import {Point} from "./data/Point";
import {ContextMenu} from "./menu/ContextMenu";
import {Sidebar} from "./sidebar/Sidebar";
import {FancierMap} from "./util/FancierMap";
import {WorldManager} from "./world/WorldManager";
import "./util/Themes";
import "./util/Window";
import "./css/livemap.css";
import "./svg";

export class LiveMap extends FancierMap {
    private readonly _sidebar: Sidebar;
    private readonly _contextMenu: ContextMenu;
    private readonly _worldManager: WorldManager;

    private readonly _coordsControl: CoordsControl;
    private readonly _blockInfoControl: BlockInfoControl;
    private readonly _linkControl: LinkControl;

    private readonly _minecraft: string;
    private readonly _max_players: number;
    private readonly _update_interval: number;
    private readonly _friendly_urls: boolean;
    private readonly _format: string;

    private readonly _lang: Lang;

    private readonly _worlds: string[];

    constructor(options: LiveMap) {
        super(options);

        window.livemap = this;

        this._minecraft = options.minecraft;
        this._max_players = options.max_players;
        this._update_interval = options.update_interval;
        this._friendly_urls = options.friendly_urls;
        this._format = options.format;

        this._lang = new Lang(this.minecraft, options.lang);

        this._worlds = options.worlds;

        // set custom page title from lang, if one was not manually set already
        if (document.title.trim() == "") {
            document.title = window.lang("browser.title");
        }

        // fancy sidebar and stuffs
        this._sidebar = new Sidebar(this);
        this._worldManager = new WorldManager(this);
        this._contextMenu = new ContextMenu(this);

        // set up the leaflet controls
        new ScaleControl(this);
        L.control.zoom().addTo(this); // must be after scale
        this._blockInfoControl = new BlockInfoControl(this);
        this._coordsControl = new CoordsControl(this);
        this._linkControl = new LinkControl(this); // must be after blockinfo

        // replace leaflet's attribution with our own
        this.attributionControl.setPrefix(window.lang("attribution"));

        // stuff to do after the map fully loads
        let now: number = new Date().getMilliseconds();
        this.on("load", (): void => {
            // cleanup stuff while we transition the loading screen away
            this.cleanupLoading();

            // keep loading screen for at least 500 ms, then transition away
            const delay: number = Math.max(now + 500 - new Date().getMilliseconds(), 0);
            setTimeout((): void => this.finishedLoading(), delay);
        });
    }

    private cleanupLoading(): void {
        // fix map size on load - fixes android browser url bar pushing page off-screen
        // https://chanind.github.io/javascript/2019/09/28/avoid-100vh-on-mobile-web.html
        this.updateSizeToWindow();

        // replace leaflet's layers.png with an svg
        const layers: HTMLElement | null = document.querySelector(".leaflet-control-layers-toggle");
        if (layers) {
            layers.appendChild(window.createSVGIcon("layers"));
            const svg: SVGElement = layers.firstChild as SVGElement;
            svg.style.width = "24px";
            svg.style.height = "24px";
            svg.style.margin = "3px";
        }

        // fix svg size issues in weird browsers like safari
        document.querySelectorAll("svg").forEach((svg: Element): void => {
            svg.setAttribute("preserveAspectRatio", "none");
        });
    }

    private finishedLoading(): void {
        // get rid of the page logo and loading images
        const mapDom: HTMLElement = this.getContainer();
        mapDom.classList.remove("loading");
        mapDom.addEventListener("transitionend", (e: TransitionEvent): void => {
            if (e.target === mapDom) {
                // remove loading logo from dom
                document.querySelector(".logo")?.remove();
                // "activate" sidebar
                document.querySelector("aside")?.classList.remove("loading");
            }
        }, {passive: true});

        // show the first world
        // this.setWorld(this.worlds[0]);
    }

    get coordsControl(): CoordsControl {
        return this._coordsControl;
    }

    get blockInfoControl(): BlockInfoControl {
        return this._blockInfoControl;
    }

    get linkControl(): LinkControl {
        return this._linkControl
    }

    get sidebar(): Sidebar {
        return this._sidebar;
    }

    get worldManager(): WorldManager {
        return this._worldManager;
    }

    get contextMenu(): ContextMenu {
        return this._contextMenu;
    }

    get minecraft(): string {
        return this._minecraft;
    }

    get max_players(): number {
        return this._max_players;
    }

    get update_interval(): number {
        return this._update_interval;
    }

    get friendly_urls(): boolean {
        return this._friendly_urls;
    }

    get format(): string {
        return this._format;
    }

    get lang(): Lang {
        return this._lang;
    }

    get worlds(): string[] {
        return this._worlds;
    }

    public updateSizeToWindow(): void {
        const style: CSSStyleDeclaration = this.getContainer().style;
        style.width = `${window.innerWidth}px`;
        style.height = `${window.innerHeight}px`;
        this.invalidateSize();
    }

    public centerOn(point: Point, zoom?: number | string): void {
        if (zoom !== undefined) {
            this.setZoom(this.worldManager.current.zooms.max_out - +zoom);
        }
        this.setView(point.toLatLng());
    }
}
