import * as L from "leaflet";
import {Palette} from "../palette/Palette";
import {LiveMap} from "../LiveMap";

window.onload = function (): void {
    // create the map div element first
    L.DomUtil.create("div", "loading", document.body).id = "map";

    window.fetchJson<LiveMap>("tiles/settings.json")
        .then((options: LiveMap): LiveMap => new LiveMap(options))
        .catch((err: unknown): void => {
            console.error(`Error creating map\n`, err);
        });
};

window.createSVGIcon = (icon: string): DocumentFragment => {
    const template: HTMLTemplateElement = L.DomUtil.create("template");
    template.innerHTML = `<svg><use href="#icon-${icon}"></use></svg>`;
    return template.content;
}

window.customEvent = <T>(event: keyof (WindowEventMap), detail: T): void => {
    window.dispatchEvent(new CustomEvent(event, {detail}));
}

window.fetchBytes = async <T>(url: string): Promise<T> => {
    return fetch(url, {headers: {"Content-Disposition": "inline"}})
        .then(async (res: Response): Promise<any> => {
            if (res.ok) {
                return await res.arrayBuffer();
            }
        });
}

window.fetchJson = async <T>(url: string, init?: RequestInit): Promise<T> => {
    return fetch(url, init).then(async (res: Response): Promise<any> => {
        if (res.ok) {
            return await res.json();
        }
    });
}

window.fetchPalette = (url: string, type: string, palette: Map<number, string>): void => {
    window.fetchJson<Palette>(url).then((json: Palette): void => {
        if (json == undefined) {
            return;
        }
        Object.entries(json).forEach((data: [string, string]): void => {
            let name: string = data[1];
            const index: number = name.indexOf(':');
            if (index !== -1) {
                const namespace: string = name.substring(0, index);
                name = window.lang(`${type}.${namespace}.${name.substring(index + 1)}`);
            }
            palette.set(+data[0], name);
        });
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
    return window.livemap.lang.get(key);
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
