import {LiveMap} from "./LiveMap";

declare global {
  interface Window {
    livemap: LiveMap

    createSVGIcon(icon: string): DocumentFragment;

    customEvent<T>(event: keyof (WindowEventMap), detail: T): void;

    fetchJson<T>(url: string): Promise<T>;

    isset(obj: unknown): boolean;

    iterate<T>(arr: ArrayLike<T>, func: (key: string, value: T) => void): void;

    lang(key?: string): string;
  }

  interface WindowEventMap {
    rendererSelected: CustomEvent<World>;
    worldAdded: CustomEvent<World>;
    worldRemoved: CustomEvent<World>;
    worldSelected: CustomEvent<World>;
  }

  interface String {
    formatted(...args: any[]): string;
  }

  interface Array<T> {
    remove(obj: T): void;
  }
}

module "leaflet" {
  export namespace Browser {
    const linux: boolean;
  }

  interface Map {
    _fadeAnimated: boolean;
  }
}
