export class Zooms {
  private readonly _def: number;
  private readonly _maxOut: number;
  private readonly _maxIn: number;

  constructor(zoom: Zooms) {
    this._def = zoom.def;
    this._maxIn = zoom.maxIn;
    this._maxOut = zoom.maxOut;
  }

  get def(): number {
    return this._def;
  }

  get maxOut(): number {
    return this._maxOut;
  }

  get maxIn(): number {
    return this._maxIn;
  }
}
