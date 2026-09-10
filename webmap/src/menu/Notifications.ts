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

export class Notifications {
  private static _instance: Notifications = new Notifications();

  private readonly _dom: HTMLElement;

  constructor() {
    this._dom = L.DomUtil.create("div", "notifications");
    document.body.appendChild(this._dom);
  }

  public create(type: ("info" | "success" | "warning" | "danger"), text: string): void {
    const div: HTMLElement = this._dom.appendChild(L.DomUtil.create("div", type));
    div.appendChild(window.createSVGIcon(type));
    div.appendChild(L.DomUtil.create("p")).innerText = text;

    const handler = (): void => {
      div.removeEventListener("transitionend", handler);
      setTimeout((): void => {
        div.addEventListener("transitionend", (): void => {
          div.remove();
        }, {passive: true});
        div.classList.remove("show");
      }, 2500);
    };
    div.addEventListener("transitionend", handler, {passive: true});
    setTimeout((): void => div.classList.add("show"), 50);
  }

  public static info(text: string): void {
    this._instance.create("info", text);
  }

  public static success(text: string): void {
    this._instance.create("success", text);
  }

  public static warning(text: string): void {
    this._instance.create("warning", text);
  }

  public static danger(text: string): void {
    this._instance.create("danger", text);
  }
}
