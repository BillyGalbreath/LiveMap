/*
 * This file is part of LiveMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020-2026 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import "../css/themes.css";

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

    // locally store user's theme choice? (probably at the toggle, not here)
    //localStorage.setItem("theme", theme);
    //localStorage.removeItem("theme");

    const icon: HTMLLinkElement | null = document.querySelector(`link[rel="shortcut icon"]`);
    if (icon) {
        icon.href = prefersDark ? "favicon-white.ico" : "favicon.ico";
    }
};

setTheme();
