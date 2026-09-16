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
import {Renderer} from "../world/Renderer";
import {World} from "../world/World";

export {};

declare global {
    // noinspection JSUnusedGlobalSymbols (jetbrains WEB-42616)
    interface Window {
        livemap: LiveMap

        createSVGIcon(icon: string): DocumentFragment;

        customEvent<T>(event: keyof (WindowEventMap), detail: T): void;

        fetchBytes(url: string): Promise<ArrayBuffer | undefined>;

        fetchJson<T>(url: string, init?: RequestInit): Promise<T>;

        fetchPalette(url: string, type: string, palette: Map<number, string>): void;

        isset(obj: unknown): boolean;

        iterate<T>(arr: ArrayLike<T>, func: (key: string, value: T) => void): void;

        lang(key?: string): string;
    }

    interface WindowEventMap {
        rendererAdded: CustomEvent<Renderer>;
        rendererSelected: CustomEvent<Renderer>;
        worldAdded: CustomEvent<World>;
        worldSelected: CustomEvent<World>;
    }

    interface String {
        formatted(...args: any[]): string;
    }

    interface Array<T> {
        remove(obj: T): void;
    }
}
