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
import {World} from "./World";

interface Tile {
  active?: boolean | undefined;
  coords: L.Coords;
  current: boolean;
  el: HTMLElement;
  loaded?: Date | undefined;
  retain?: boolean | undefined;
}

export class Renderer extends L.TileLayer {
  declare _url: string;

  private readonly _id: string;
  private readonly _name: string;
  private readonly _icon: string;

  constructor(world: World, renderer: Renderer) {
    super(`tiles/{world}/{zoom}/{renderer}/{x}_{z}.png`, {
      // tile sizes match regions sizes (512 blocks x 512 blocks)
      tileSize: 512,
      // dont wrap tiles at world edges
      noWrap: true,
      // set in LiveMap.ts now
      minZoom: 0, // window.livemap.options.minZoom,
      // set in LiveMap.ts now
      maxZoom: world.zooms.max_out + world.zooms.max_in,
      // the closest zoomed in possible (without stretching)
      // this is always 0. no exceptions!
      minNativeZoom: 0,
      // the farthest possible zoom out possible
      maxNativeZoom: world.zooms.max_out,
      // we need to counter effect the higher maxZoom here
      // zoomOffset = maxNativeZoom - maxZoom
      // zoomOffset = zoom.maxOut - (zoom.maxOut + (-zoom.maxIn))
      // zoomOffset = (-zoom.maxIn)
      zoomOffset: -world.zooms.max_in,
      // zoom stuff (this is a pita, btw)
      // this doesn't work right, so we leave it false and override _getZoomForUrl below
      zoomReverse: false
    });

    this._id = renderer.id;
    this._name = renderer.name;
    this._icon = renderer.icon;

    // when tiles load we need to load extra block info
    this.addEventListener("tileload", (event: L.TileEvent): void => {
      const zoom: number = world.zooms.max_out - event.coords.z;
      window.livemap.currentWorld?.loadBlockInfo(zoom, event.coords.x, event.coords.y);
    });

    // when tiles unload we need to remove the extra block info from memory
    this.addEventListener("tileunload", (event: L.TileEvent): void => {
      const zoom: number = world.zooms.max_out - event.coords.z;
      world.unsetBlockInfo(zoom, event.coords.x, event.coords.y);
    });

    // push this layer to the back (leaflet defaults it to 1)
    this.setZIndex(0);
  }

  get id(): string {
    return this._id;
  }

  get name(): string {
    return this._name;
  }

  get icon(): string {
    return this._icon;
  }

  // reverse zoom controls here instead of the flag in options
  _getZoomForUrl(): number {
    return (this.options.maxZoom! - this._tileZoom!) + this.options.zoomOffset!;
  }

  getTileUrl(coords: L.Coords): string {
    const world: World = window.livemap.currentWorld ?? window.livemap.worlds[0];
    const rendererId: string = world.currentRenderer.id;
    const data: { world: string; renderer: string; x: number; z: number; zoom: number } = {
      world: world.name,
      renderer: rendererId,
      x: coords.x,
      z: coords.y,
      zoom: this._getZoomForUrl()
    };
    return L.Util.template(this._url, L.Util.extend(data, this.options));
  }

  // https://github.com/Leaflet/Leaflet/issues/6659#issuecomment-491545545
  // https://gist.github.com/barryhunter/e42f0c4756e34d5d07db4a170c7ec680
  refresh(): void {
    for (const key in this._tiles) {
      const tile: Tile = this._tiles[key];
      if (!tile.current || !tile.active) {
        continue;
      }
      const oldSrc: string = (tile.el as HTMLImageElement).src;
      const newSrc: string = this.getTileUrl(tile.coords);
      if (oldSrc == newSrc) {
        continue;
      }
      this._map._fadeAnimated = false;
      const img = new Image();
      img.onload = (): void => {
        L.Util.requestAnimFrame((): void => {
          (tile.el as HTMLImageElement).src = newSrc;
        });
        setTimeout((): void => {
          this._map._fadeAnimated = true;
        }, 100);
      }
      img.src = newSrc;
    }
  }

  _tileOnLoad(done: L.DoneCallback, tile: HTMLElement): void {
    super._tileOnLoad(done, tile);
    //tile.setAttribute("loaded", "true");
  }

  _tileOnError(done: L.DoneCallback, tile: HTMLElement, e: Error): void {
    super._tileOnError(done, tile, e);
    //tile.setAttribute("loaded", "false");
  }

  // @method createTile(coords: Object, done?: Function): HTMLElement
  // Called only internally, overrides GridLayer's [`createTile()`](#gridlayer-createtile)
  // to return an `<img>` HTML element with the appropriate image URL given `coords`. The `done`
  // callback is called when the tile has been loaded.
  createTile(coords: L.Coords, done: L.DoneCallback): HTMLImageElement {
    const tile: HTMLImageElement = L.DomUtil.create("img");
    //tile.setAttribute("loaded", "false");

    L.DomEvent.on(tile, "load", L.Util.bind(this._tileOnLoad, this, done, tile));
    L.DomEvent.on(tile, "error", L.Util.bind(this._tileOnError, this, done, tile));

    // Alt tag is set to empty string to keep screen readers from reading
    // URL and for compliance reasons http://www.w3.org/TR/WCAG20-TECHS/H67
    tile.alt = "";

    // Set role="presentation" to force screen readers to ignore this
    // https://www.w3.org/TR/wai-aria/roles#textalternativecomputation
    tile.setAttribute("role", "presentation");

    // Retrieve image via a fetch instead of just setting the src
    // This works around the fact that browsers usually don't make a request
    // for an image that was previously loaded, without resorting to
    // changing the URL (which would break caching).
    fetch(this.getTileUrl(coords))
      .then((res: Response): void => {
        // Call leaflet's error handler if request fails for some reason
        if (!res.ok) {
          this._tileOnError(done, tile, new Error(res.statusText));
          return;
        }

        // Get image data and convert into object URL, so it can be used as a src
        // Leaflet's onload listener will take it from here
        res.blob().then((blob: Blob): void => {
          // don't use URL.createObjectURL, it creates memory leak
          const reader: FileReader = new FileReader();
          reader.readAsDataURL(blob);
          reader.onload = (): string => tile.src = String(reader.result);
        });
      }).catch((err: any) => this._tileOnError(done, tile, err));

    return tile;
  }
}
