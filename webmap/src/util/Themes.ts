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
