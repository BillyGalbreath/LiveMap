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

export class MenuRow {
  private readonly _text?: string;
  private readonly _alt?: string;
  private readonly _icon?: string;
  private readonly _key?: string;
  private readonly _action?: (e?: Event) => void;

  constructor(text?: string, icon?: string, key?: string, action?: (e?: Event) => void) {
    this._text = window.lang(text);
    this._alt = window.lang(`${text}.alt`);
    this._icon = icon;
    this._key = key?.toLowerCase();
    this._action = action;
  }

  get text(): string | undefined {
    return this._text;
  }

  get alt(): string | undefined {
    return this._alt;
  }

  get icon(): string | undefined {
    return this._icon;
  }

  get key(): string | undefined {
    return this._key;
  }

  get action(): ((e?: Event) => void) | undefined {
    return this._action;
  }

  public create(): HTMLElement {
    if (!this.text) {
      // no contents or text, make a divider
      return document.createElement("hr");
    }

    // init contents
    const iconElem: Element = document.createElement("p");
    const textElem: Element = document.createElement("p");
    const keyElem: Element = document.createElement("p");

    // populate contents
    if (this.icon) {
      iconElem.appendChild(window.createSVGIcon(this.icon));
    }
    textElem.innerHTML = this.text ?? "";
    keyElem.innerHTML = this.key ?? "";

    // create row
    const row: HTMLElement = document.createElement("div");
    row.appendChild(iconElem);
    row.appendChild(textElem);
    row.appendChild(keyElem);

    if (this.alt) {
      row.title = this.alt;
    }

    // attach click action
    if (this.action) {
      row.onclick = (e: MouseEvent) => this.action!(e);
    }

    return row;
  }
}
