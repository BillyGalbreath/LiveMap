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
import {LiveMap} from "./LiveMap";

declare global {
  interface Window {
    livemap: LiveMap

    createSVGIcon(icon: string): DocumentFragment;

    customEvent<T>(event: keyof (WindowEventMap), detail: T): void;

    fetchBytes<T>(url: string): Promise<T>;

    fetchJson<T>(url: string, init?: RequestInit): Promise<T>;

    fetchPalette(url: string, type: string, palette: Map<number, string>): void;

    isset(obj: unknown): boolean;

    iterate<T>(arr: ArrayLike<T>, func: (key: string, value: T) => void): void;

    lang(key?: string): string;
  }

  interface WindowEventMap {
    rendererSelected: CustomEvent<World>;
    worldAdded: CustomEvent<World>;
    worldRemoved: CustomEvent<World>;
    worldSelected: CustomEvent<World>;
  }

  interface String {
    formatted(...args: any[]): string;
  }

  interface Array<T> {
    remove(obj: T): void;
  }
}

module "leaflet" {
  export namespace Browser {
    const linux: boolean;
  }

  export function ellipse(latLng: L.LatLngExpression, radii: L.PointTuple, tilt: number, options: L.PathOptions): Ellipse;

  interface Ellipse extends L.Path {
    setRadius(radii: L.PointTuple): this;

    getRadius(): L.Point;

    setTilt(tilt: number): this;

    getBounds(): L.LatLngBounds;

    getLatLng(): L.LatLng;

    setLatLng(latLng: L.LatLngExpression): this;
  }

  interface Map {
    _fadeAnimated: boolean;
  }
}
