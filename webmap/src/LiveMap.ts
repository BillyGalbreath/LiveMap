import * as L from "leaflet";
import {CoordsControl} from "./control/CoordsControl";
import {LinkControl} from "./control/LinkControl";
import {ScaleControl} from "./control/ScaleControl";
import {Lang} from "./data/Lang";
import {Point} from "./data/Point";
import {UI} from "./data/UI";
import {World} from "./world/World";
import {Zooms} from "./data/Zooms";
import "./css/livemap.css";
import "./svg"

window.onload = function (): void {
  window.fetchJson<LiveMap>("tiles/settings.json")
    .then((options: LiveMap): LiveMap => new LiveMap(options))
    .catch((err: unknown): void => {
      console.error(`Error creating map\n`, err);
    });
};

export class LiveMap extends L.Map {
  declare _controlCorners: { [x: string]: HTMLDivElement; };
  declare _controlContainer?: HTMLElement;
  declare _container?: HTMLElement;

  private readonly _linkControl: LinkControl;
  private readonly _coordsControl: CoordsControl;
  // private readonly _blockInfoControl: BlockInfoControl; // todo

  // private readonly _sidebarControl: SidebarControl; // todo
  // private readonly _contextMenu: ContextMenu; // todo

  private readonly _minecraft: string;
  private readonly _max_players: number;
  private readonly _friendly_urls: boolean;
  private readonly _update_interval: number;
  private readonly _attribution: string;
  private readonly _zooms: Zooms;
  private readonly _ui: UI;
  private readonly _lang: Lang;

  private readonly _worlds: World[] = [];

  private _currentWorld?: World;

  constructor(options: LiveMap) {
    // create the map div element
    L.DomUtil.create("div", "loading", document.body).id = "map";

    super("map", {
      // we need a flat and simple crs
      crs: L.Util.extend(L.CRS.Simple, {
        // we need to flip the y-axis correctly
        // https://stackoverflow.com/a/62320569/3530727
        transformation: new L.Transformation(1, 0, 1, 0)
      }),
      // center map on spawn
      center: [0, 0],
      // always allow attribution in case a layer needs it
      attributionControl: true,
      // canvas is more efficient than svg
      preferCanvas: true,
      // prevent tile reloads from flashing
      fadeAnimation: true,
      // don't add zoom control here, we'll do it manually below the scale control
      zoomControl: false,

      // chrome based browsers on linux zoom twice as fast, so we have to double the ratio
      // effectively undoes the fix for Leaflet/Leaflet#4538 and Leaflet/Leaflet#7403
      // https://github.com/Leaflet/Leaflet/commit/96977a19358374b0166cff049862fa1f0fed5948
      //
      // remove this logic when this bug gets fixed: https://issues.chromium.org/issues/40887377
      // it seems intentional, so it might not get fixed https://issues.chromium.org/issues/40804672
      wheelPxPerZoomLevel: L.Browser.linux && L.Browser.chrome ? 120 : 60,
      // these get weird when changed. so don't.
      zoomSnap: 1,
      zoomDelta: 1,

      // for extra zoom in, make higher than maxNativeZoom
      // this is the stretched tiles to zoom in further
      // maxZoom = maxNativeZoom + extra
      // maxZoom = zoom.maxOut - (-zoom.maxIn)
      maxZoom: options.zooms.maxOut - options.zooms.maxIn,
      // the closest zoomed in possible (without stretching)
      // this is always 0. no exceptions!
      minZoom: 0
    });

    window.livemap = this;

    this._minecraft = options.minecraft;

    this._max_players = options.max_players;
    this._friendly_urls = options.friendly_urls;
    this._update_interval = options.update_interval;
    this._attribution = options.attribution;

    this._zooms = new Zooms(options.zooms);
    this._ui = new UI(options.ui);
    this._lang = new Lang(this.minecraft, options.lang);

    new ScaleControl(this); // todo
    // manually add the zoom control below the scale control
    L.control.zoom().addTo(this);

    // replace leaflet's attribution with our own
    this.attributionControl.setPrefix(this.attribution);

    // sort, build, and add worlds
    options.worlds
      .sort((w1: World, w2: World) => w1.order - w2.order)
      .forEach((world: World): void => {
        this.worlds.push(new World(world));
        window.customEvent("worldAdded", world);
      });

    // set custom page title from lang
    if (document.title.trim() == "") {
      document.title = window.lang("title");
    }

    // set up the controllers
    this._linkControl = new LinkControl(this);
    this._coordsControl = new CoordsControl(this);
    // this._blockInfoControl = new BlockInfoControl(this); // todo

    // this._sidebarControl = new SidebarControl(this); // todo

    // the fancy context menu and stuff
    // this._contextMenu = new ContextMenu(this); // todo

    // stuff to do after the map fully loads
    // but let loading screen show for at least 500ms
    let now = new Date().getMilliseconds();
    this.on("load", (): void => {
      const delay: number = Math.max(now + 500 - new Date().getMilliseconds(), 0);
      setTimeout((): void => this.onLoad(), delay);
    });
  }

  onLoad(): void {
    // get rid of the page logo and loading images
    const container: HTMLElement = this.getContainer();
    container.classList.remove("loading");
    container.addEventListener("transitionend", (e: TransitionEvent): void => {
      if (e.target === container) {
        document.querySelector(".logo")?.remove();
      }
    }, {passive: true});

    // fix map size on load - fixes android browser url bar pushing page off-screen
    // https://chanind.github.io/javascript/2019/09/28/avoid-100vh-on-mobile-web.html
    this.updateSizeToWindow();

    // replace layers.png with an svg
    const layers: HTMLElement | null = document.querySelector(".leaflet-control-layers-toggle");
    if (layers) {
      layers.appendChild(window.createSVGIcon("layers"));
      const svg: SVGElement = layers.firstChild as SVGElement;
      svg.style.width = "24px";
      svg.style.height = "24px";
      svg.style.margin = "3px";
    }

    // fix svg size issues in weird browsers like safari
    document.querySelectorAll("svg").forEach((svg: Element): void => {
      svg.setAttribute("preserveAspectRatio", "none");
    });

    // show the first world
    this.setWorld(this.worlds[0]);
  }

  // https://stackoverflow.com/a/60391674/3530727
  // noinspection JSUnusedGlobalSymbols - used internally
  _initControlPos(): void {
    this._controlCorners = {};
    this._controlContainer = L.DomUtil.create("div", "leaflet-control-container", this._container);
    const top: HTMLDivElement = L.DomUtil.create("div", `leaflet-control-container-top`, this._controlContainer);
    const middle: HTMLDivElement = L.DomUtil.create("div", `leaflet-control-container-middle`, this._controlContainer);
    const bottom: HTMLDivElement = L.DomUtil.create("div", `leaflet-control-container-bottom`, this._controlContainer);
    this._controlCorners[`topleft`] = L.DomUtil.create("div", `leaflet-top leaflet-left`, top);
    this._controlCorners[`topcenter`] = L.DomUtil.create("div", `leaflet-top leaflet-center`, top);
    this._controlCorners[`topright`] = L.DomUtil.create("div", `leaflet-top leaflet-right`, top);
    this._controlCorners[`middleleft`] = L.DomUtil.create("div", `leaflet-middle leaflet-left`, middle);
    this._controlCorners[`middlecenter`] = L.DomUtil.create("div", `leaflet-middle leaflet-center`, middle);
    this._controlCorners[`middleright`] = L.DomUtil.create("div", `leaflet-middle leaflet-right`, middle);
    this._controlCorners[`bottomleft`] = L.DomUtil.create("div", `leaflet-bottom leaflet-left`, bottom);
    this._controlCorners[`bottomcenter`] = L.DomUtil.create("div", `leaflet-bottom leaflet-center`, bottom);
    this._controlCorners[`bottomright`] = L.DomUtil.create("div", `leaflet-bottom leaflet-right`, bottom);
  }

  get linkControl(): LinkControl {
    return this._linkControl
  }

  get coordsControl(): CoordsControl {
    return this._coordsControl;
  }

  /*get blockInfoControl(): BlockInfoControl {
    return this._blockInfoControl;
  }

  get sidebarControl(): SidebarControl {
    return this._sidebarControl;
  }

  get contextMenu(): ContextMenu {
    return this._contextMenu;
  }*/

  public centerOn(point: Point, zoom?: number | string): void {
    if (zoom !== undefined) {
      this.setZoom(this.zooms.maxOut - +zoom);
    }
    this.setView(point.toLatLng());
  }

  public currentZoom(): number {
    return this.zooms.maxOut - this.getZoom();
  }

  public updateSizeToWindow(): void {
    const style: CSSStyleDeclaration = this.getContainer().style;
    style.width = `${window.innerWidth}px`;
    style.height = `${window.innerHeight}px`;
    this.invalidateSize();
  }

  public setWorld(world: World): void {
    world.setRenderer();
    this._currentWorld = world;
  }

  get worlds(): World[] {
    return this._worlds;
  }

  get currentWorld(): World | undefined {
    return this._currentWorld;
  }

  get minecraft(): string {
    return this._minecraft;
  }

  get max_players(): number {
    return this._max_players;
  }

  get friendly_urls(): boolean {
    return this._friendly_urls;
  }

  get update_interval(): number {
    return this._update_interval;
  }

  get attribution(): string {
    return this._attribution;
  }

  get zooms(): Zooms {
    return this._zooms;
  }

  get ui(): UI {
    return this._ui;
  }

  get lang(): Lang {
    return this._lang;
  }
}

window.createSVGIcon = (icon: string): DocumentFragment => {
  const template: HTMLTemplateElement = L.DomUtil.create("template");
  template.innerHTML = `<svg><use href="#icon-${icon}"></use></svg>`;
  return template.content;
}

window.customEvent = <T>(event: keyof (WindowEventMap), detail: T): void => {
  window.dispatchEvent(new CustomEvent(event, {detail}));
}

window.fetchJson = async <T>(url: string, init?: RequestInit): Promise<T> => {
  return fetch(url, init).then(async (res: Response): Promise<any> => {
    if (res.ok) {
      return await res.json();
    }
  });
}

window.isset = (obj: unknown): boolean => {
  return obj !== null && typeof obj !== "undefined";
}

window.iterate = <T>(arr: ArrayLike<T>, func: (key: string, value: T) => void): void => {
  Object.entries(arr).forEach((data: [string, T]): void => {
    func.call(arr, data[0], data[1]);
  });
}

window.lang = (key?: string): string => {
  return window.livemap.lang.translate(key);
}

String.prototype.formatted = function (this: string, ...args: any[]): string {
  return this.replace(/(%[sdi])/g, (orig: string): any => args.shift() ?? orig);
};

// https://stackoverflow.com/a/3955096
Array.prototype.remove = function <T>(obj: T, ax?: number): void {
  while ((ax = this.indexOf(obj)) !== -1) {
    this.splice(ax, 1);
  }
};

// update map size when window size, scale, or orientation changes
"orientationchange resize".split(" ").forEach((event: string): void => {
  window.addEventListener(event, (): void => {
    window.livemap.updateSizeToWindow();
  }, {passive: true});
});

const knownThemes: string[] = [];

for (let i: number = 0; i < document.styleSheets.length; i++) {
  const css: CSSStyleSheet = document.styleSheets[i];
  if (css.href?.endsWith("livemap.css")) {
    const rules: CSSRuleList = css.cssRules;
    for (let j: number = 0; j < rules.length; j++) {
      const rule: CSSStyleRule = rules[j] as CSSStyleRule;
      const match: RegExpExecArray | null = /html\[theme=\u0022(.+)\u0022]/.exec(rule.selectorText);
      if (match) {
        knownThemes.push(match[1]);
      }
    }
    break;
  }
}

window.matchMedia("(prefers-color-scheme: dark)")
  .addEventListener("change", (): void => setTheme());

const setTheme: () => void = (): void => {
  const prefersDark: boolean = knownThemes.length > 1 && window.matchMedia("(prefers-color-scheme: dark)").matches;
  const theme: string = (localStorage.getItem("theme") ?? knownThemes[+prefersDark]) ?? "dark";
  document.querySelector("html")!.setAttribute("theme", theme);

  // todo - locally store user's theme choice? (probably at the toggle, not here)
  //localStorage.setItem("theme", theme);
  //localStorage.removeItem("theme");

  const icon: HTMLLinkElement | null = document.querySelector(`link[rel="shortcut icon"]`);
  if (icon) {
    icon.href = prefersDark ? "favicon-white.ico" : "favicon.ico";
  }
};

setTheme();
