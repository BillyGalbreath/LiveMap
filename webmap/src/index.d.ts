import {LiveMap} from "./LiveMap";

declare global {
    interface Window {
        livemap: LiveMap
    }
}

module "leaflet" {
    export namespace Browser {
        const linux: boolean;
    }
}
