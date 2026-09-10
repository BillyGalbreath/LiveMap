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
import {Renderer} from "../world/Renderer";

export class RenderersControl {
  private readonly _livemap: LiveMap;
  private readonly _dom: HTMLElement;

  private _renderers: Renderer[] = [];

  private _rendererType: string = "basic";

  constructor(livemap: LiveMap) {
    this._livemap = livemap;

    this._dom = L.DomUtil.create("ul");

    /*livemap.settings.renderers.forEach((renderer: Renderer): void => {
        this._renderers.push(new Renderer(renderer))
    });*/
  }

  get dom(): HTMLElement {
    return this._dom;
  }

  get rendererType(): string {
    return this._rendererType;
  }

  set rendererType(renderer: string | null) {
    this._rendererType = !renderer?.length ? this._renderers[0].id : renderer;
  }
}
