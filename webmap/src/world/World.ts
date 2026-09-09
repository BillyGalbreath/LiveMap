import {Point} from "../data/Point";
import {Renderer} from "./Renderer";

export class World {
  private readonly _id: string;
  private readonly _name: string;
  private readonly _type: string;
  private readonly _order: number;
  private readonly _spawn: Point;
  private readonly _renderers: Renderer[] = [];

  private _currentRenderer?: Renderer;

  private _tickTimer?: NodeJS.Timeout;

  constructor(world: World) {
    this._id = world.id;
    this._name = world.name;
    this._type = world.type;
    this._order = world.order;
    this._spawn = Point.of(world.spawn);

    world.renderers.forEach((renderer: Renderer): void => {
      this.renderers.push(new Renderer(renderer));
    });
  }

  get id(): string {
    return this._id;
  }

  get name(): string {
    return this._name;
  }

  get type(): string {
    return this._type;
  }

  get order(): number {
    return this._order;
  }

  get spawn(): Point {
    return this._spawn;
  }

  get renderers(): Renderer[] {
    return this._renderers;
  }

  get currentRenderer(): Renderer | undefined {
    return this._currentRenderer;
  }

  get background(): string {
    switch (this.type) {
      case "nether":
        return `url("images/sky/nether.png")`;
      case "the_end":
        return `url("images/sky/the_end.png")`;
      case "overworld":
      default:
        return `url("images/sky/overworld.png")`;
    }
  }

  private tick(): void {
    // do not "redraw". use "refresh" to prevent flickering and flashing
    this._currentRenderer?.refresh();

    // schedule next tick
    this._tickTimer = setTimeout(
      (): void => this.tick(),
      window.livemap.update_interval * 1000
    );
  }

  public setRenderer(renderer?: Renderer): void {
    // stop tick timer
    clearTimeout(this._tickTimer);

    // remove old renderer tile layer from map
    this._currentRenderer?.remove();

    // make sure we have a real renderer
    if (!renderer || this._renderers.indexOf(renderer) < 0) {
      renderer = this.renderers[0];
    }

    // set new renderer tiles layer and add to map
    this._currentRenderer = renderer;
    renderer.addTo(window.livemap);

    window.livemap.getContainer().style.backgroundImage = this.background;

    // start ticking
    this.tick();

    window.customEvent("rendererSelected", this);
  }
}
