globalThis.Jig ??= {};

globalThis.Jig.i18n = (() => {

    // [data-i18n] マーカー付き要素の翻訳辞書（日本語キー→対象言語値）。
    // JigDocument のラベル（用語集 等）も UI 文言と区別せずここに集約する。
    const builtinDictionaries = {
        en: {
            // JigDocument ラベル（Java 側 JigDocument enum のキーと対応）
            "用語集": "Glossary",
            "パッケージ関連": "Package relations",
            "ユースケース": "Usecase",
            "入力インタフェース": "Inbound interface",
            "インサイト": "Insight",
            "一覧出力": "List output",
            "ライブラリ依存情報": "Library dependencies",
            // UI 文言
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
            "絞り込み": "Filter",
            // タブ見出し
            "パッケージ関連図": "Package relation diagram",
            "パッケージ内パッケージ関連図": "Inner package relation diagram",
            "パッケージ内クラス関連図": "Inner class relation diagram",
            "クラス関連図": "Class relation diagram",
            "クラス図": "Class diagram",
            "概要": "Overview",
            "テキスト": "Text",
            "シミュレーション": "Simulation",
            "ユースケース図": "Usecase diagram",
            "シーケンス図": "Sequence diagram",
            "staticメソッド": "Static methods",
            // テーブルヘッダ
            "名前": "Name",
            "名称": "Name",
            "完全修飾名": "FQN",
            "クラス数": "Classes",
            "メソッド数": "Methods",
            "使用クラス数": "Used classes",
            "使用クラス数(Ce)": "Used classes (Ce)",
            "被使用クラス数(Ca)": "Used by (Ca)",
            "不安定性(I)": "Instability (I)",
            "凝集度欠如(LCOM)": "LCOM",
            "循環的複雑度": "Cyclomatic complexity",
            "循環的複雑度合計": "Total cyclomatic complexity",
            "規模": "Size",
            "規模合計": "Total size",
            "使用メソッド数": "Used methods",
            "使用フィールド数": "Used fields",
            "自クラスフィールド使用数": "Own field uses",
            "自クラスメソッド呼び出し数": "Own method calls",
            "関連数（依存元）": "Dependents (in)",
            "関連数（依存先）": "Dependencies (out)",
            "定義名": "Declaration",
            "ライブラリ": "Library",
            "含まれるパッケージ": "Packages",
            "呼び出し元クラス": "Caller classes",
            "列挙定数名": "Enum constant",
            "パス": "Path",
            "エントリーポイント": "Entry point",
            "ハンドラ": "Handler",
            "購読先": "Subscription",
            "スケジュール": "Schedule",
            "出力ポート / 操作": "Outbound port / operation",
            "列挙値": "Enum values",
            // glossary attribute meta
            "属性情報": "Attributes",
            "単純名": "Simple name",
            "由来": "Origin",
            "関連ドキュメント": "Related document",
            "ソースコード": "Source code",
            "ソースを開く": "Open source",
            // library-dependency controls
            "階層集約:": "Aggregation level:",
            "Java 標準（java.*, javax.*）を表示": "Show Java standard (java.*, javax.*)",
            "ライブラリの選択を解除": "Clear library selection",
            "ライブラリ一覧": "Library list",
            // package controls
            "階層探索": "Hierarchy",
            "関連探索": "Explore",
            "依存関係の簡略表示:": "Simplify dependencies:",
            "有効": "Enabled",
            "Deprecatedのみの関連を除外:": "Exclude deprecated-only relations:",
            "選択を全解除": "Clear all selections",
            "選択をデフォルトに戻す": "Reset to defaults",
            "依存の表示": "Show dependencies",
            "依存元:": "Callers:",
            "依存先:": "Callees:",
            "なし": "None",
            "直接": "Direct",
            "相互依存分析": "Mutual dependency analysis",
            "表示する関連": "Relations to show",
            "関連": "Relations",
            // 他のサイドバー section タイトル
            "永続化操作対象": "Persistence targets",
            "外部型": "External types",
            "エントリーポイント一覧": "Entry points",
        },
    };

    // セッション中のみ保持する現在言語（永続化しない）。
    let currentLang = null;

    // BCP47 タグ（"ja-JP" など）から先頭の言語コード（"ja"）だけを取り出す。
    function toLangCode(tag) {
        return String(tag || "").split('-')[0];
    }

    function resolveDictionary(lang) {
        return builtinDictionaries[lang] || null;
    }

    function resolveLanguage() {
        if (currentLang) return currentLang;
        // 初期言語は HTML の lang 属性から決定する（Java 側で {{lang}} を全テンプレートに置換済み）。
        return toLangCode(document.documentElement.lang || "ja");
    }

    // サポート言語は ja（カノニカルキー）と builtinDictionaries のキー集合から導出する。
    function availableLanguages() {
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
        // 属性翻訳: data-i18n-attr="属性名" を持つ要素の対応属性を翻訳する
        // （現在の属性値を ja キーとして使う）。
        root.querySelectorAll("[data-i18n-attr]").forEach(el => translateAttribute(el, dict));
    }

    function translateAttribute(el, dict) {
        const attrName = el.getAttribute("data-i18n-attr");
        if (!attrName) return;
        const datasetKey = `i18nAttrOrig_${attrName}`;
        if (el.dataset[datasetKey] == null) {
            el.dataset[datasetKey] = el.getAttribute(attrName) || "";
        }
        const key = el.dataset[datasetKey];
        el.setAttribute(attrName, (dict && dict[key]) || key);
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
        currentLang = toLangCode(lang);
        apply();
        document.dispatchEvent(new CustomEvent("jig:locale-change", {detail: {lang: currentLang}}));
    }

    return {
        apply,
        currentLanguage,
        setLanguage,
        availableLanguages,
        // テストが明示キー翻訳をセットアップするために参照する
        builtinDictionaries,
    };
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        globalThis.Jig.i18n.apply();
        // 動的に挿入された data-i18n / data-i18n-attr 要素も翻訳する。
        // 要素ノード追加に限定することで apply 自身が起こす text node 差し替えのループを避ける。
        if (typeof MutationObserver !== "undefined") {
            let scheduled = false;
            const observer = new MutationObserver(mutations => {
                const elementAdded = mutations.some(m =>
                    Array.from(m.addedNodes).some(n => n.nodeType === 1));
                if (!elementAdded || scheduled) return;
                scheduled = true;
                queueMicrotask(() => {
                    scheduled = false;
                    globalThis.Jig.i18n.apply();
                });
            });
            observer.observe(document.body, {childList: true, subtree: true});
        }
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.i18n;
}
