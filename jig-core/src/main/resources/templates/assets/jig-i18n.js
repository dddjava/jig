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
            "出力日時": "Generated at",
            "主要パッケージ関連図": "Key package diagram",
            "ドメインパッケージ": "Domain package",
            "最上位パッケージ": "Top-level package",
            // sidebar 表示設定
            "表示設定": "Display settings",
            "表示対象": "Show",
            "表示要素": "Elements",
            "表示内容": "Content",
            "表示項目": "Items",
            "表示種別": "Type",
            "種類": "Kind",
            "ダイアグラム": "Diagram",
            "ダイアグラム表示": "Show diagram",
            "Deprecated ノード": "Deprecated nodes",
            "依存関係の簡略表示": "Simplify dependencies",
            "個別ダイアグラムの表示対象": "Per-diagram elements",
            "簡略表示": "Simplified view",
            "すべて": "All",
            "パッケージ": "Package",
            "文字列": "String",
            "数値": "Number",
            "日付": "Date",
            "期間": "Period",
            "区分": "Category",
            "コレクション": "Collection",
            "説明": "Description",
            "メンバ": "Members",
            "ハンドラのみ": "Handlers only",
            "定義名": "Declared name",
            "入力・出力": "Input/Output",
            "内部メソッド": "Internal methods",
            "出力インタフェース": "Outbound interface",
            "ドメインモデル": "Domain model",
            "ドメインのみ": "Domain only",
            "説明のない用語": "Terms without description",
            "永続化操作方法": "Persistence operations",
            "呼び出しユースケース": "Caller usecase",
            "ポート": "Port",
            "メソッド": "Method",
            "アダプタ": "Adapter",
            "外部アクセサ": "External accessor",
            "外部操作対象": "External target",
            "操作方法": "Operation",
            "リクエストハンドラ": "Request handler",
            "メッセージリスナー": "Message listener",
            "スケジューラー": "Scheduler",
            "その他": "Other",
            // glossary 追加項目
            "クラス": "Class",
            "属性情報を表示する": "Show attributes",
            "CSV出力": "Export CSV",
            "用語一覧": "Terms",
            // 他のサイドバー section タイトル
            "永続化操作対象": "Persistence targets",
            "外部型": "External types",
            "エントリーポイント一覧": "Entry points",
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

    // 子に要素ノードを含む場合は、直下のテキストノードのみ置換して子要素を保持する。
    // 例: <label><input> ラベル</label> でも <input> を壊さず " ラベル" のみ差し替える。
    function setTranslatedText(el, value) {
        let textNode = null;
        let hasChildElements = false;
        for (const node of el.childNodes) {
            if (node.nodeType === 1) hasChildElements = true;
            else if (node.nodeType === 3 && node.nodeValue.trim() && !textNode) textNode = node;
        }
        if (!hasChildElements) {
            el.textContent = value;
            return;
        }
        if (textNode) {
            const match = textNode.nodeValue.match(/^(\s*).*?(\s*)$/s);
            textNode.nodeValue = match[1] + value + match[2];
        } else {
            el.appendChild(document.createTextNode(value));
        }
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
            setTranslatedText(el, (dict && dict[key]) || key);
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
