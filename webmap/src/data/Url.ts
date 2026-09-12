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

import {LiveMap} from "../LiveMap";
import {World} from "../world/World";
import {Renderer} from "../world/Renderer";
import {Point} from "./Point";

export class Url {
  private readonly _livemap: LiveMap;
  private readonly _basePath: string;
  private readonly _world: string;
  private readonly _renderer: string;
  private readonly _zoom: number;
  private readonly _point: Point;

  constructor(livemap: LiveMap, url: string, worldName?: string | null, rendererId?: string | null, zoom?: string | number | null, x?: string | number | null, z?: string | number | null) {
    this._livemap = livemap;

    if (worldName) {
      this._basePath = "/";
    } else {
      const match: RegExpExecArray | null = /^\/(.+?)(?:\/(.+?)?\/?(-?\d+)?\/?(-?\d+)?\/?(-?\d+)?(?:\/(.+)?)?)?$/.exec(url);
      if (match) {
        this._basePath = "/";
        worldName = match[1];
        rendererId = match[2] ?? "basic";
        zoom = match[3] ?? 0;
        x = match[4] ?? 0;
        z = match[5] ?? 0;
      } else {
        this._basePath = window.location.pathname?.split("?")[0]?.replace("index.html", "") ?? "/";
        const url: URLSearchParams = new URLSearchParams(window.location.search);
        worldName = url.get("world");
        rendererId = url.get("renderer");
        zoom = url.get("zoom");
        x = url.get("x");
        z = url.get("z");
      }
    }

    // verify world exists
    let world: World | undefined = this._livemap.worlds.find((w: World): boolean => w.name === worldName);
    if (!world) {
      // fallback to first known world
      world = this._livemap.worlds[0];
    }

    // verify renderer
    let renderer: Renderer | undefined = world.getRenderer(rendererId ?? "");
    if (!renderer) {
      // fallback to world's first renderer
      renderer = world.renderers[0];
    }

    this._world = world.name;
    this._renderer = renderer.id;
    this._zoom = +(zoom ?? world.zooms.default);
    this._point = Point.of(x ?? 0, z ?? 0);
  }

  get basePath(): string {
    return this._basePath;
  }

  get world(): string {
    return this._world;
  }

  get renderer(): string {
    return this._renderer;
  }

  get zoom(): number {
    return this._zoom;
  }

  get x(): number {
    return this._point.x;
  }

  get z(): number {
    return this._point.z;
  }

  get point(): Point {
    return this._point;
  }

  public toString(): string {
    return (this._livemap.friendly_urls ? `%s%s/%s/%i/%i/%i/` : `%s?world=%s&renderer=%s&zoom=%i&x=%i&z=%i`)
      .formatted(this.basePath, this.world, this.renderer, this.zoom, this.point.x, this.point.z);
  }
}
