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

export class SortedMap<K, V> extends Map<K, V> {
    private readonly compare: (a: readonly [K, V], b: readonly [K, V]) => number;

    constructor(
        entries?: readonly (readonly [K, V])[] | null,
        compareFn?: (a: readonly [K, V], b: readonly [K, V]) => number
    ) {
        super();

        this.compare = compareFn || (([_k1, _v1]: readonly [K, V], [_k2, _v2]: readonly [K, V]) => (_k1 > _k2 ? 1 : _k1 < _k2 ? -1 : 0));

        if (entries) {
            for (const [key, value] of entries) {
                this.set(key, value);
            }
        }
    }

    override set(key: K, value: V): this {
        super.set(key, value);

        const sorted: [K, V][] = [...super.entries()].sort(this.compare);

        super.clear();

        for (const [key, value] of sorted) {
            super.set(key, value);
        }

        return this;
    }
}
