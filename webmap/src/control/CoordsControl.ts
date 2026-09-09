import * as L from "leaflet";
import {LiveMap} from "../LiveMap";
import {Point} from "../data/Point";
import {ControlBox} from "./ControlBox";

export class CoordsControl extends ControlBox {
  private readonly _dom: HTMLElement;

  private _point: Point = Point.of(0);
  private _y?: number;

  constructor(livemap: LiveMap) {
    super(livemap, livemap.ui.coords);

    this._dom = L.DomUtil.create("div", "leaflet-control-layers coordinates");
    this._dom.title = window.lang("coordinates.title");

    L.DomEvent.disableClickPropagation(this._dom);

    this.addTo(livemap);
  }

  onAdd(map: L.Map): HTMLElement {
    map.addEventListener("mousemove", this.update);
    this.update();
    return this._dom;
  }

  onRemove(map: L.Map): void {
    map.removeEventListener("mousemove", this.update);
  }

  public update: (e?: L.LeafletMouseEvent) => void = (e?: L.LeafletMouseEvent): void => {
    // update x,z coords
    this.point = Point.of(e?.latlng ?? 0).round();

    // update blockinfo (blockinfo will update our y coordinate)
    // this._livemap.blockInfoControl?.update(this.point); // todo

    // update the dom text
    this._dom.innerHTML = window.lang("coords.value")
      .replace(/<x>/g, this.point.x.toString().padStart(6, ' '))
      .replace(/<y>/g, (this.y?.toString() ?? '???').padStart(2, ' ').padEnd(3, ' '))
      .replace(/<z>/g, this.point.z.toString().padEnd(6, ' '));
  }

  get point(): Point {
    return this._point;
  }

  set point(point: Point) {
    this._point = point;
  }

  get y(): number | undefined {
    return this._y;
  }

  set y(y: number | undefined) {
    this._y = y;
  }
}
