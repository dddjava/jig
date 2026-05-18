globalThis.Jig ??= {};

globalThis.Jig.i18n = (() => {

    // 初期辞書。テンプレート HTML に直接埋め込まれている静的テキストのうち、
    // 翻訳しても意味が崩れない単独語句から登録する。長文や分割テキストは未対応。
    const dictionaries = {
        en: {
            "JIG": "JIG",
            "用語集": "Glossary",
            "パッケージ関連": "PackageRelation",
            "ドメインモデル": "DomainModel",
            "ユースケース": "Usecase",
            "入力インタフェース": "InboundInterface",
            "出力インタフェース": "OutboundInterface",
            "インサイト": "Insight",
            "一覧出力": "ListOutput",
            "ライブラリ依存情報": "LibraryDependency",
            "入力": "Input",
            "出力": "Output",
            "フィールド": "Fields",
            "折りたたむ": "Collapse",
            "展開": "Expand",
        },
    };

    function resolveLocale() {
        const tag = globalThis.Jig?.data?.navigation?.get?.()?.locale;
        return tag || document.documentElement.lang || "ja";
    }

    // [data-i18n] を持つ要素のみ翻訳する。マーカーが無ければ何もしない。
    // data-i18n="key" のように明示キーがあればそれを、無ければ textContent.trim() を辞書キーにする。
    function translateMarkedElements(root, dict) {
        if (!root || typeof root.querySelectorAll !== "function") return;
        root.querySelectorAll("[data-i18n]").forEach(el => {
            const explicitKey = el.getAttribute("data-i18n");
            const key = (explicitKey && explicitKey.length > 0) ? explicitKey : el.textContent.trim();
            const translation = dict[key];
            if (translation) {
                el.textContent = translation;
            }
        });
    }

    function apply() {
        const locale = resolveLocale();
        const lang = locale.split('-')[0];
        document.documentElement.lang = lang;
        if (lang === "ja") return;
        const dict = dictionaries[lang];
        if (!dict) return;
        const titleEl = document.querySelector("title[data-i18n]");
        if (titleEl) {
            const explicitKey = titleEl.getAttribute("data-i18n");
            const key = (explicitKey && explicitKey.length > 0) ? explicitKey : titleEl.textContent.trim();
            if (dict[key]) document.title = dict[key];
        }
        translateMarkedElements(document.body, dict);
    }

    return {apply, translateMarkedElements, dictionaries};
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        globalThis.Jig.i18n.apply();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.i18n;
}
