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

import {Point} from "../data/Point";
import {BlockInfo} from "../palette/BlockInfo";
import {Renderer} from "./Renderer";
import {Zooms} from "../data/Zooms";

export class World {
  private readonly _name: string;
  private readonly _display_name: string;
  private readonly _type: string;
  private readonly _order: number;
  private readonly _spawn: Point;
  private readonly _center: Point;
  private readonly _zooms: Zooms;
  private readonly _renderers: Renderer[] = [];

  private _currentRenderer?: Renderer;

  private _biomePalette: Map<number, string> = new Map();
  private _blockInfo: Map<number, Map<string, BlockInfo>> = new Map();

  private _tickTimer?: NodeJS.Timeout;

  constructor(world: World) {
    this._name = world.name;
    this._display_name = world.display_name;
    this._type = world.type;
    this._order = world.order;
    this._spawn = Point.of(world.spawn);
    this._center = Point.of(world.center);
    this._zooms = new Zooms(world.zooms);

    world.renderers.forEach((renderer: Renderer): void => {
      this.renderers.push(new Renderer(this, renderer));
    });

    window.fetchPalette(`tiles/${this.name}/biomes.gz`, "biome", this._biomePalette);
  }

  get name(): string {
    return this._name;
  }

  get display_name(): string {
    return this._display_name;
  }

  get type(): string {
    return this._type;
  }

  get order(): number {
    return this._order;
  }

  get spawn(): Point {
    return this._spawn;
  }

  get center(): Point {
    return this._center;
  }

  get zooms(): Zooms {
    return this._zooms;
  }

  get renderers(): Renderer[] {
    return this._renderers;
  }

  public getRenderer(id: string): Renderer | undefined {
    return this.renderers.find((r: Renderer): boolean => r.id === id);
  }

  get currentRenderer(): Renderer {
    return this._currentRenderer || this.renderers[0];
  }

  get blockInfo(): Map<number, Map<string, BlockInfo>> {
    return this._blockInfo;
  }

  get biomePalette(): Map<number, string> {
    return this._biomePalette;
  }

  public centerOn(point: Point, zoom?: number | string): void {
    if (zoom !== undefined) {
      window.livemap.setZoom(this.zooms.max_out - +zoom);
    }
    window.livemap.setView(point.toLatLng());
  }

  public currentZoom(): number {
    return this.zooms.max_out - window.livemap.getZoom();
  }

  get background(): string {
    switch (this.type) {
      case "nether":
        return `url("images/sky/nether.png")`;
      case "the_end":
        return `url("images/sky/the_end.png")`;
      case "overworld":
      default:
        return `url("images/sky/overworld.png")`;
    }
  }

  private tick(): void {
    // do not "redraw". use "refresh" to prevent flickering and flashing
    this._currentRenderer?.refresh();

    // schedule next tick
    this._tickTimer = setTimeout(
      (): void => this.tick(),
      window.livemap.update_interval * 1000
    );
  }

  public setRenderer(renderer?: Renderer): void {
    // stop tick timer
    clearTimeout(this._tickTimer);

    // remove old renderer tile layer from map
    this._currentRenderer?.remove();

    // update min/max zoom limits
    window.livemap.options.maxZoom = this.zooms.max_out + this.zooms.max_in;
    window.livemap.setMaxZoom(this.zooms.max_out + this.zooms.max_in);

    // make sure we have a real renderer
    if (!renderer || this._renderers.indexOf(renderer) < 0) {
      renderer = this.getRenderer(window.livemap.linkControl.getUrlFromPoint(Point.ZERO).renderer);
      if (renderer === undefined) {
        renderer = this.renderers[0];
      }
    }

    // set new renderer tiles layer and add to map
    this._currentRenderer = renderer;
    renderer.addTo(window.livemap);

    window.livemap.getContainer().style.backgroundImage = this.background;

    // start ticking
    this.tick();

    window.customEvent("rendererSelected", this);
  }

  public loadBlockInfo(zoom: number, x: number, z: number): void {
    if (!window.livemap.ui.blockinfo) {
      return;
    }
    window.fetchBytes<ArrayBuffer>(`tiles/${this.name}/${zoom}/blockinfo/${x}_${z}.livemap.gz`)
      .then((buffer?: ArrayBuffer): void => {
        this.setBlockInfo(zoom, x, z, buffer);
      });
  }

  public getBlockInfo(zoom: number, x: number, z: number): BlockInfo | undefined {
    return this.blockInfo.get(zoom < 0 ? 0 : zoom)?.get(`${x}_${z}`);
  }

  public setBlockInfo(zoom: number, x: number, z: number, buffer?: ArrayBuffer): void {
    let infoMap: Map<string, BlockInfo> | undefined = this.blockInfo.get(zoom < 0 ? 0 : zoom);
    if (infoMap == undefined) {
      infoMap = new Map<string, BlockInfo>();
      this.blockInfo.set(zoom, infoMap);
    }

    const blockInfo: null | BlockInfo = buffer == undefined ? null : new BlockInfo(new Uint8Array(buffer));

    if (blockInfo == null) {
      infoMap.delete(`${x}_${z}`);
    } else {
      infoMap.set(`${x}_${z}`, blockInfo);
    }
  }

  public unsetBlockInfo(zoom: number, x: number, z: number): void {
    this.blockInfo.get(zoom)?.delete(`${x}_${z}`);
  }
}
