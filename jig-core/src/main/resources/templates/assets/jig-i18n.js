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

    function shouldSkipNode(node) {
        for (let el = node.parentNode; el; el = el.parentNode) {
            if (!el.nodeName) return true;
            const tag = el.nodeName;
            if (tag === "SCRIPT" || tag === "STYLE" || tag === "CODE" || tag === "PRE") return true;
        }
        return false;
    }

    function translateInto(root, dict) {
        if (!root) return;
        const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
        const targets = [];
        for (let node = walker.nextNode(); node; node = walker.nextNode()) {
            if (shouldSkipNode(node)) continue;
            const trimmed = node.nodeValue.trim();
            if (trimmed && dict[trimmed]) {
                targets.push(node);
            }
        }
        targets.forEach(node => {
            const trimmed = node.nodeValue.trim();
            node.nodeValue = node.nodeValue.replace(trimmed, dict[trimmed]);
        });
    }

    function apply() {
        const locale = resolveLocale();
        const lang = locale.split('-')[0];
        document.documentElement.lang = lang;
        if (lang === "ja") return;
        const dict = dictionaries[lang];
        if (!dict) return;
        if (document.title && dict[document.title.trim()]) {
            document.title = dict[document.title.trim()];
        }
        translateInto(document.body, dict);
    }

    return {apply, translateInto, dictionaries};
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        globalThis.Jig.i18n.apply();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.i18n;
}
