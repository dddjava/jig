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

    // UI chrome として翻訳対象にするタグのみホワイトリスト化する。
    // <p>/<li>/<dd> など説明文を含む可能性のあるタグは含めない。
    const TRANSLATE_TAGS = new Set([
        "TITLE", "H1", "H2", "H3", "H4", "H5", "H6",
        "TH", "BUTTON", "LABEL", "SUMMARY", "CAPTION", "LEGEND"
    ]);

    function isTranslatableNode(node) {
        const parent = node.parentNode;
        if (!parent || !parent.nodeName) return false;
        if (!TRANSLATE_TAGS.has(parent.nodeName)) return false;
        // 翻訳対象タグ内に他のブロック要素が混じる場合を考慮し、祖先に翻訳禁止タグがあれば除外
        for (let el = parent; el; el = el.parentNode) {
            const tag = el.nodeName;
            if (tag === "SCRIPT" || tag === "STYLE" || tag === "CODE" || tag === "PRE") return false;
        }
        return true;
    }

    function translateInto(root, dict) {
        if (!root) return;
        const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
        const targets = [];
        for (let node = walker.nextNode(); node; node = walker.nextNode()) {
            if (!isTranslatableNode(node)) continue;
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
