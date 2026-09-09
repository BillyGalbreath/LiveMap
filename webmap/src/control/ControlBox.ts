import * as L from "leaflet";
import {LiveMap} from "../LiveMap";

export abstract class ControlBox extends L.Control {
  protected readonly _livemap: LiveMap;

  protected constructor(livemap: LiveMap, position: string) {
    super({position: position} as L.ControlOptions);
    this._livemap = livemap;
  }
}
