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
import {Url} from "../data/Url";
import {ControlBox} from "./ControlBox";
import {World} from "../world/World";

export class LinkControl extends ControlBox {
  private readonly _dom: HTMLAnchorElement;
  private readonly _url: Url;

  constructor(livemap: LiveMap) {
    super(livemap, livemap.ui.link);

    this._dom = L.DomUtil.create("a", "leaflet-control-layers link");
    this._dom.title = window.lang("link.title");
    this._dom.appendChild(window.createSVGIcon("link"));

    L.DomEvent.disableClickPropagation(this._dom);

    this._dom.onclick = (e: MouseEvent): void => {
      e.preventDefault();
      window.history.replaceState({}, window.lang("title"), this._dom.href);
      this._livemap.contextMenu.share(Point.of(this._livemap.getCenter()).round());
    }

    // add to the map once we have a dom to add
    this.addTo(livemap);

    // parse data from the browser's url
    this._url = new Url(this._livemap, window.location.pathname);

    // center the map on url coordinates or spawn (0, 0);
    // this sets up the map after ctor and before load
    // onLoad will not call until this is finished
    setTimeout((): void => {
      let world: World | undefined = this._livemap.getWorld(this._url.world);
      if (world === undefined) {
        world = this._livemap.worlds[0];
      }
      this._livemap.setWorld(world);
      this._livemap.sidebarControl.renderersControl.rendererType = this._url.renderer;
      world.centerOn(this._url.point, this._url.zoom);
    }, 0);
  }

  onAdd(map: L.Map): HTMLAnchorElement {
    map.addEventListener("moveend", this.update);
    map.addEventListener("zoomend", this.update);
    return this._dom;
  }

  onRemove(map: L.Map): void {
    map.removeEventListener("moveend", this.update);
    map.removeEventListener("zoomend", this.update);
  }

  public update: () => void = (): void => {
    this._dom.href = this.getUrlFromView().toString();
    // todo - find out how to prevent chrome from spamming history
    window.history.replaceState({}, "", this._dom.href);
  }

  public getUrlFromView(): Url {
    return this.getUrlFromPoint(Point.of(this._livemap.getCenter()).round());
  }

  public getUrlFromPoint(point: Point): Url {
    return new Url(
      this._livemap,
      this._url.basePath,
      this._url.world,
      this._livemap.sidebarControl.renderersControl.rendererType,
      this._livemap.currentWorld?.currentZoom() ?? 0,
      point.x,
      point.z
    );
  }
}
