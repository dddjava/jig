globalThis.Jig ??= {};

globalThis.Jig.i18n = (() => {

    // テンプレート HTML に [data-i18n] マーカー付きで埋め込まれた静的テキストの翻訳辞書。
    // JigDocument のラベルはサーバ側を唯一のソースとし navigation-data.translations から取り込む。
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

    function resolveDictionary(lang) {
        const builtin = builtinDictionaries[lang];
        if (!builtin) return null;
        const serverTranslations = globalThis.Jig?.data?.navigation?.get?.()?.translations;
        return serverTranslations ? {...builtin, ...serverTranslations} : builtin;
    }

    function resolveLanguage() {
        const tag = globalThis.Jig?.data?.navigation?.get?.()?.locale
            || document.documentElement.lang
            || "ja";
        return tag.split('-')[0];
    }

    function resolveKey(el) {
        const explicitKey = el.getAttribute("data-i18n");
        return (explicitKey && explicitKey.length > 0) ? explicitKey : el.textContent.trim();
    }

    function translate(root, dict) {
        if (!root || typeof root.querySelectorAll !== "function") return;
        root.querySelectorAll("[data-i18n]").forEach(el => {
            const translation = dict[resolveKey(el)];
            if (translation) {
                el.textContent = translation;
            }
        });
    }

    function apply() {
        const lang = resolveLanguage();
        document.documentElement.lang = lang;
        if (lang === "ja") return;
        const dict = resolveDictionary(lang);
        if (!dict) return;
        // <title data-i18n> も document 全体のクエリで一緒に処理される
        translate(document, dict);
    }

    return {apply, translate, resolveDictionary, builtinDictionaries};
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        globalThis.Jig.i18n.apply();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.i18n;
}
