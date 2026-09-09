export class UI {
  private readonly _link: string;
  private readonly _coords: string;
  private readonly _blockinfo: string;
  private readonly _scale: string;
  private readonly _sidebar: string;
  private readonly _logo: string;

  constructor(ui: UI) {
    this._link = ui.link;
    this._coords = ui.coords;
    this._blockinfo = ui.blockinfo;
    this._scale = ui.scale;
    this._sidebar = ui.sidebar;
    this._logo = ui.logo;
  }

  get link(): string {
    return this._link;
  }

  get coords(): string {
    return this._coords;
  }

  get blockinfo(): string {
    return this._blockinfo;
  }

  get scale(): string {
    return this._scale;
  }

  get sidebar(): string {
    return this._sidebar;
  }

  get logo(): string {
    return this._logo;
  }
}
