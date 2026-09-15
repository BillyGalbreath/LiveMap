/*
 * Copyright 2014 JD Fergason
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// https://github.com/jdfergason/Leaflet.Ellipse

import * as L from "leaflet";

export {};

declare module "leaflet" {
    export function ellipse(latLng: L.LatLngExpression, radii: L.PointTuple, tilt: number, options?: EllipseOptions): Path.Ellipse;

    interface EllipseOptions extends L.PathOptions {
        fill: boolean;
        startAngle: number;
        endAngle: number;
    }

    namespace Path {
        interface Ellipse extends L.Path {
            constructor(options: EllipseOptions);

            setRadius(radii: L.PointTuple): this;

            getRadius(): L.Point;

            setTilt(tilt: number): this;

            getBounds(): L.LatLngBounds;

            getLatLng(): L.LatLng;

            setLatLng(latLng: L.LatLngExpression): this;
        }
    }
}
