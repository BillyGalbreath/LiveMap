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

export class UI {
  private readonly _logo: string;
  private readonly _link: string;
  private readonly _coords: string;
  private readonly _blockinfo: string;
  private readonly _scale: string;
  private readonly _sidebar: string;

  constructor(ui: UI) {
    this._logo = ui.logo;
    this._blockinfo = ui.blockinfo;
    this._coords = ui.coords;
    this._link = ui.link;
    this._scale = ui.scale;
    this._sidebar = ui.sidebar;
  }

  get logo(): string {
    return this._logo;
  }

  get blockinfo(): string {
    return this._blockinfo;
  }

  get coords(): string {
    return this._coords;
  }

  get link(): string {
    return this._link;
  }

  get scale(): string {
    return this._scale;
  }

  get sidebar(): string {
    return this._sidebar;
  }
}
