globalThis.Jig ??= {};

globalThis.Jig.i18n = (() => {

    // テンプレート HTML に [data-i18n] マーカー付きで埋め込まれた静的テキストの翻訳辞書。
    // JigDocument のラベルはサーバ側を唯一のソースとし navigation-data.translations.<lang> から取り込む。
    // ここで持つのはテンプレート HTML/JS にのみ現れる UI 文言。
    const builtinDictionaries = {
        en: {
            "入力": "Input",
            "出力": "Output",
            "フィールド": "Fields",
            "折りたたむ": "Collapse",
            "展開": "Expand",
        },
    };

    // セッション中のみ保持する現在言語（永続化しない）。
    let currentLang = null;

    function resolveDictionary(lang) {
        const builtin = builtinDictionaries[lang];
        if (!builtin) return null;
        const serverAll = globalThis.Jig?.data?.navigation?.get?.()?.translations;
        const serverForLang = serverAll && typeof serverAll === "object" ? serverAll[lang] : null;
        return serverForLang ? {...builtin, ...serverForLang} : builtin;
    }

    function resolveLanguage() {
        if (currentLang) return currentLang;
        const tag = globalThis.Jig?.data?.navigation?.get?.()?.locale
            || document.documentElement.lang
            || "ja";
        return tag.split('-')[0];
    }

    function availableLanguages() {
        const fromData = globalThis.Jig?.data?.navigation?.get?.()?.availableLocales;
        if (Array.isArray(fromData) && fromData.length > 0) {
            return fromData.map(tag => String(tag).split('-')[0]);
        }
        // builtin に ja は無いので明示的に足す
        return ["ja", ...Object.keys(builtinDictionaries)];
    }

    function originalText(el) {
        if (el.dataset.i18nOriginal == null) {
            el.dataset.i18nOriginal = el.textContent.trim();
        }
        return el.dataset.i18nOriginal;
    }

    function resolveKey(el) {
        const explicitKey = el.getAttribute("data-i18n");
        return (explicitKey && explicitKey.length > 0) ? explicitKey : originalText(el);
    }

    // dict が falsy なら原文（ja キー）に復元する。
    function translate(root, dict) {
        if (!root || typeof root.querySelectorAll !== "function") return;
        root.querySelectorAll("[data-i18n]").forEach(el => {
            const key = resolveKey(el);
            el.textContent = (dict && dict[key]) || key;
        });
    }

    function apply() {
        const lang = resolveLanguage();
        document.documentElement.lang = lang;
        const dict = lang === "ja" ? null : resolveDictionary(lang);
        // <title data-i18n> も document 全体のクエリで一緒に処理される
        translate(document, dict);
    }

    function currentLanguage() {
        return resolveLanguage();
    }

    function setLanguage(lang) {
        currentLang = String(lang).split('-')[0];
        apply();
        document.dispatchEvent(new CustomEvent("jig:locale-change", {detail: {lang: currentLang}}));
    }

    return {
        apply,
        translate,
        resolveDictionary,
        builtinDictionaries,
        currentLanguage,
        setLanguage,
        availableLanguages,
    };
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        globalThis.Jig.i18n.apply();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.i18n;
}
