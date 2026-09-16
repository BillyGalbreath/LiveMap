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

window.onload = function (): void {
    // create the map div element first
    L.DomUtil.create("div", "loading", document.body).id = "map";

    window.fetchJson<LiveMap>("tiles/settings.json")
        .then((options: LiveMap): LiveMap => new LiveMap(options))
        .catch((err: unknown): void => {
            console.error(`Error creating map\n`, err);
        });
};

window.createSVGIcon = (icon: string): DocumentFragment => {
    const template: HTMLTemplateElement = L.DomUtil.create("template");
    template.innerHTML = `<svg><use href="#icon-${icon}"></use></svg>`;
    return template.content;
}

window.customEvent = <T>(event: keyof (WindowEventMap), detail: T): void => {
    window.dispatchEvent(new CustomEvent(event, {detail}));
}

window.fetchBytes = async (url: string): Promise<ArrayBuffer | undefined> => {
    return fetch(url, {headers: {"Content-Disposition": "inline"}})
        .then(async (res: Response): Promise<ArrayBuffer | undefined> => {
            if (res.ok) {
                return await res.arrayBuffer();
            }
        });
}

window.fetchJson = async <T>(url: string, init?: RequestInit): Promise<T> => {
    return fetch(url, init).then(async (res: Response): Promise<any> => {
        if (res.ok) {
            return await res.json();
        }
    });
}

window.fetchPalette = (url: string, type: string, palette: Map<number, string>): void => {
    window.fetchJson<[string, string]>(url).then((json: [string, string]): void => {
        if (json == undefined) {
            return;
        }
        Object.entries(json).forEach((data: [string, string]): void => {
            let name: string = data[1];
            const index: number = name.indexOf(':');
            if (index !== -1) {
                const namespace: string = name.substring(0, index);
                name = window.lang(`${type}.${namespace}.${name.substring(index + 1)}`);
            }
            palette.set(+data[0], name);
        });
    });
}

window.isset = (obj: unknown): boolean => {
    return obj !== null && typeof obj !== "undefined";
}

window.iterate = <T>(arr: ArrayLike<T>, func: (key: string, value: T) => void): void => {
    Object.entries(arr).forEach((data: [string, T]): void => {
        func.call(arr, data[0], data[1]);
    });
}

window.lang = (key?: string): string => {
    return window.livemap.lang.get(key);
}

String.prototype.formatted = function (this: string, ...args: any[]): string {
    return this.replace(/(%[sdi])/g, (orig: string): any => args.shift() ?? orig);
};

// https://stackoverflow.com/a/3955096
Array.prototype.remove = function <T>(obj: T, ax?: number): void {
    while ((ax = this.indexOf(obj)) !== -1) {
        this.splice(ax, 1);
    }
};

// update map size when window size, scale, or orientation changes
"orientationchange resize".split(" ").forEach((event: string): void => {
    window.addEventListener(event, (): void => {
        window.livemap.updateSizeToWindow();
    }, {passive: true});
});
