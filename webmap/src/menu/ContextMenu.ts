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
import {Point} from "../data/Point";
import {MenuRow} from "./MenuRow";
import {Notifications} from "./Notifications";

export class ContextMenu {
  private readonly _livemap: LiveMap;
  private readonly _menu: [HTMLElement, HTMLElement];
  private readonly _rows: MenuRow[];
  private readonly _pointRegex: RegExp = /\[?(-?\d+),(-?\d+)]?/;

  private _point?: Point;
  private _cur: boolean = false;

  public constructor(livemap: LiveMap) {
    this._livemap = livemap;

    // create the menu rows
    this._rows = this.createRows();

    // create dual menus to swap between
    this._menu = [
      this.createMenu(),
      this.createMenu()
    ];

    // setup browser's listeners
    window.onblur = document.onblur = (): void => this.close();
    window.oncontextmenu = (e: MouseEvent) => this.open(e);
    window.onkeydown = (e: KeyboardEvent) => this.keydown(e);

    // setup map's listeners
    this._livemap.on("load unload resize viewreset move movestart moveend zoom zoomstart zoomend" +
      " zoomlevelschange click dblclick mousedown preclick", (): void => this.close());
  }

  private createRows(): MenuRow[] {
    const rows: MenuRow[] = [];
    rows.push(new MenuRow("[ 0, 0 ]"));
    rows.push(new MenuRow());
    if (navigator.clipboard) {
      // some browsers don't support clipboard api :(
      rows.push(new MenuRow("menu.copy", "copy", "Ctrl+C", () => this.copy()));
      rows.push(new MenuRow("menu.paste", "paste", "Ctrl+V", () => this.paste()));
      rows.push(new MenuRow());
      rows.push(new MenuRow("menu.share", "link", "Ctrl+S", () => this.share()));
      rows.push(new MenuRow());
    }
    rows.push(new MenuRow("menu.center", "center", "F10", () => this.center()));
    return rows;
  }

  private createMenu(): HTMLElement {
    // create the menu
    const wrapper: HTMLElement = L.DomUtil.create("div", "contextmenu-wrapper");
    const menu: HTMLElement = document.body.appendChild(wrapper)
      .appendChild(L.DomUtil.create("div"))
      .appendChild(L.DomUtil.create("div", "contextmenu"));

    // add the rows to the menu
    this._rows.forEach((row: MenuRow): void => menu.append(row.create()));

    // prevent menu from opening real context menu
    menu.addEventListener("contextmenu", (e: Event): void => this.stopPropagation(e));

    return wrapper;
  }

  private close(): void {
    // hide the current menu
    this._menu[+this._cur].classList.remove("show");

    this._point = undefined;
  }

  private open(e: MouseEvent): void {
    // close current menu (mobile needs this)
    this.close();

    // show the next menu at mouse position, while keeping it inside the viewable area
    const menu: HTMLElement = this._menu[+(this._cur = !this._cur)];
    menu.classList.add("show");
    menu.style.top = `${Math.min(window.innerHeight - menu.offsetHeight - 25, e.y)}px`;
    menu.style.left = `${Math.min(window.innerWidth - menu.offsetWidth - 25, e.x)}px`;

    // stop the event from bubbling up the stack
    this.stopPropagation(e);

    // update coordinates in first row
    this._point = this._livemap.coordsControl.point;
    menu.querySelector("div:first-child p:nth-child(2)")!.innerHTML = this._point.toString("[ {x}, {z} ]");
  }

  private keydown(e: KeyboardEvent): void {
    // close menu when escape is pressed
    if (e.key === "Escape") {
      this.close();
      return;
    }

    // get current key combo
    const combo: string = this.combo(e);

    // find and run any actions for this key combo
    this._rows.forEach((row: MenuRow): void => {
      if (row.key == combo && row.action) {
        row.action(e);
        this.stopPropagation(e);
      }
    });
  }

  private combo(e: KeyboardEvent): string {
    if (e.key == "Control" || e.key == "Shift" || e.key == "Alt") {
      return "";
    }

    let combo: string = "";
    if (e.ctrlKey) {
      combo += `${combo ? "+" : ""}ctrl`;
    }
    if (e.shiftKey) {
      combo += `${combo ? "+" : ""}shift`;
    }
    if (e.altKey) {
      combo += `${combo ? "+" : ""}alt`;
    }
    combo += `${combo ? "+" : ""}${e.key}`;
    return combo.toLowerCase();
  }

  public copy(): void {
    const point: string = (this._point ?? this._livemap.coordsControl.point).toString("[{x},{z}]");
    navigator.clipboard.writeText(point)
      .then((): void => {
        Notifications.success(window.lang("menu.notif.copy").replace("<point>", point.toString()));
      })
      .catch((e: any): void => {
        console.error("Could not copy location\n", e);
        Notifications.danger(window.lang("menu.notif.copy.failed"));
      })
      .finally((): void => {
        this.close();
      });
  }

  public paste(): void {
    navigator.clipboard.readText()
      .then((text: string): void => {
        const match: RegExpExecArray | null = this._pointRegex.exec(text.replace(/\s+/g, ""));
        if (match) {
          const point: Point = Point.of(parseInt(match[1]), parseInt(match[2]));
          Notifications.info(window.lang("menu.notif.paste").replace("<point>", point.toString("[{x},{z}]")));
          this._livemap.currentWorld?.centerOn(Point.of(parseInt(match[1]), parseInt(match[2])));
        } else {
          Notifications.warning(window.lang("menu.notif.paste.invalid"));
        }
      })
      .catch((e: any): void => {
        console.error("Could not paste location\n", e);
        Notifications.danger(window.lang("menu.notif.paste.failed"));
      })
      .finally((): void => {
        this.close();
      });
  }

  public share(point?: Point): void {
    point ??= this._point ?? this._livemap.coordsControl.point;
    const text: string = `${window.location.origin}${this._livemap.linkControl.getUrlFromPoint(point)}`;
    navigator.clipboard.writeText(text)
      .then((): void => {
        Notifications.success(window.lang("menu.notif.share"));
      })
      .catch((e: any): void => {
        console.error("Could not copy shareable url\n", e);
        Notifications.danger(window.lang("menu.notif.share.failed"));
      })
      .finally((): void => {
        this.close();
      });
  }

  public center(): void {
    this._livemap.currentWorld?.centerOn(this._livemap.coordsControl.point);
    this._livemap.coordsControl.update();
    this._livemap.linkControl.update();
    Notifications.success(window.lang("menu.notif.center"));
    this.close();
  }

  private stopPropagation(e: Event): void {
    // stop the event in the current element
    // and prevent it from bubbling up the stack
    e.preventDefault();
    e.stopPropagation();
    e.stopImmediatePropagation();
  }
}
