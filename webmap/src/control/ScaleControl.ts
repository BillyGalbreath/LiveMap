import {ControlBox} from "./ControlBox";
import {LiveMap} from "../LiveMap";
import * as L from "leaflet";
import {Point} from "../data/Point";

export class ScaleControl extends ControlBox {
  private readonly _dom: HTMLElement;

  constructor(livemap: LiveMap) {
    super(livemap, livemap.ui.scale);

    this._dom = L.DomUtil.create("div", "leaflet-control-layers scale no-hover");
    this._dom.title = window.lang("scale.title");

    this._dom.appendChild(document.createElement("span"));

    this.addTo(livemap);
  }

  onAdd(map: L.Map): HTMLElement {
    map.on('move', this._update, this);
    map.whenReady(this._update, this);

    return this._dom;
  }

  onRemove(map: L.Map): void {
    map.off('move', this._update, this);

  }

  _update() {
    const distance: number = this._livemap.distance(
      this._livemap.containerPointToLatLng([0, 0]),
      this._livemap.containerPointToLatLng([2000, 0])
    );

    const meters: number = this._getRoundNum(distance);
    this._dom.style.width = `${Point.pixelsToMeters(Math.round(2000 * meters / distance))}px`;
    this._dom.children[0].innerHTML = meters < 1000 ? meters + ' m' : (meters / 1000) + ' km';
  }

  _getRoundNum(num: number): number {
    const pow10: number = Math.pow(10, `${Math.floor(num)}`.length - 1);
    const d: number = num / pow10;
    return pow10 * (d >= 10 ? 10 : d >= 5 ? 5 : d >= 3 ? 3 : d >= 2 ? 2 : 1);
  }
}
