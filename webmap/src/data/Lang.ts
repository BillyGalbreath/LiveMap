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

export class Lang implements ArrayLike<string> {
  private static readonly MCASSET_URL = `https://assets.mcasset.cloud/%s/assets/minecraft/lang/%s.json`

  private readonly _assets: Map<string, string> = new Map();

  readonly [n: number]: string;

  length: number = 0;

  constructor(minecraft: string, lang: Lang) {
    // add all our custom lang assets
    window.iterate(lang, (key: string, value: string): void => {
      this._assets.set(key, value);
      this.length = this._assets.size;
    });

    // what locale are we using?
    const locale: string = this.translate("locale") ?? "en_us";

    // make sure we play nice with mcasset.cloud and don't hit their site
    // too much. we'll check our storage first, and only download if it's
    // not there or if it's expired since last time we downloaded it.
    const key = `minecraft:${minecraft}:${locale}.json`;
    const storedItem = localStorage.getItem(key);
    if (storedItem) {
      // got the data - lets check if its valid
      const storedLang: StoredLang = JSON.parse(storedItem);
      if (window.isset(storedLang.expires) &&
        window.isset(storedLang.json) &&
        new Date().getTime() < storedLang.expires
      ) {
        // its valid - so lets parse the stored data
        const json: Lang = JSON.parse(storedLang.json) as Lang;
        window.iterate(json, (key: string, value: string): void => {
          // do not overwrite existing entries
          if (!this._assets.has(key)) {
            this._assets.set(key, value);
            this.length = this._assets.size;
          }
        });
        console.log(`Restored ${locale} lang from local storage`);
        // we're done
        return;
      }
    }

    console.log(`Loading ${locale} lang from assets.mcasset.cloud`);
    // could not restore locally saved lang data for one reason
    // or another. so lets try to load fresh data from mcasset.cloud
    const url = Lang.MCASSET_URL.formatted(minecraft, locale);
    window.fetchJson<Lang>(url).then((json: Lang): void => {
      // add entries to local map
      window.iterate(json, (key: string, value: string): void => {
        // we only care for biome/block names
        if (key.startsWith("biome.minecraft.") ||
          key.startsWith("block.minecraft.")) {
          this._assets.set(key, value);
          this.length = this._assets.size;
        }
      });
      console.log("Received response from assets.mcasset.cloud");

      // build expiring storage item
      const item: StoredLang = {
        // expire 24 hours from now
        expires: new Date().getTime() + (24 * 60 * 60 * 1000),
        // store map as json string
        json: JSON.stringify(Object.fromEntries(this._assets.entries())),
      }
      // store item
      localStorage.setItem(key, JSON.stringify(item));
    });
  }

  public translate(key?: string): string {
    return key ? (this._assets.get(key) ?? key) : "";
  }
}

interface StoredLang {
  expires: number;
  json: string;
}
