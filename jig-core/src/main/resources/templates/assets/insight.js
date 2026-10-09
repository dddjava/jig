const InsightApp = (() => {
    const Jig = globalThis.Jig;

    // label は見出しの短縮名、title は正式名。どちらも i18n キー
    const VIEWS = [
        {
            id: "package", dataKey: "packages", countId: "package-count", headingId: "section-package", tableId: "package-insight-list",
            columns: [
                {key: "numberOfTypes", label: "クラス数"},
                {key: "numberOfMethods", label: "メソッド数"},
                {key: "numberOfUsingTypes", label: "使用型", title: "使用クラス数"},
                {key: "cyclomaticComplexity", label: "複雑度", title: "循環的複雑度合計"},
                {key: "size", label: "規模", title: "規模合計"},
            ],
        },
        {
            id: "type", dataKey: "types", countId: "type-count", headingId: "section-type", tableId: "type-insight-list",
            columns: [
                {key: "numberOfMethods", label: "メソッド数"},
                {key: "numberOfUsingTypes", label: "Ce", title: "使用クラス数"},
                {key: "numberOfUsedByTypes", label: "Ca", title: "被使用クラス数"},
                {key: "instability", label: "I", title: "不安定性", ratio: true},
                {key: "lcom", label: "LCOM", title: "凝集度欠如", ratio: true},
                {key: "cyclomaticComplexity", label: "複雑度", title: "循環的複雑度合計"},
                {key: "size", label: "規模", title: "規模合計"},
            ],
        },
        {
            id: "method", dataKey: "methods", countId: "method-count", headingId: "section-method", tableId: "method-insight-list",
            columns: [
                {key: "cyclomaticComplexity", label: "複雑度", title: "循環的複雑度"},
                {key: "size", label: "規模"},
                {key: "numberOfParameters", label: "引数", title: "引数の数"},
                {key: "numberOfUsingTypes", label: "使用型", title: "使用クラス数"},
                {key: "numberOfUsingMethods", label: "呼出", title: "使用メソッド数"},
                {key: "numberOfUsingFields", label: "参照", title: "使用フィールド数"},
                {key: "numberOfUsingOwnFields", label: "自参照", title: "自クラスフィールド使用数"},
                {key: "numberOfUsingOwnMethods", label: "自呼出", title: "自クラスメソッド呼び出し数"},
            ],
        },
    ];

    const NAME_SORT_KEY = "fqn";

    function parseInsightData() {
        return Jig.data.insight.get();
    }

    function setInsightCount(elementId, count) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = count;
        }
    }

    function typeNameInPackage(typeFqn, packageFqn) {
        return typeFqn.startsWith(packageFqn + ".") ? typeFqn.slice(packageFqn.length + 1) : typeFqn;
    }

    /**
     * メソッドFQNを表示用に分解する。引数は単純名にする。
     * @param {{fqn: string, typeFqn: string, packageFqn: string}} method
     */
    function parseMethod(method) {
        const hashIdx = method.fqn.indexOf("#");
        const parenIdx = method.fqn.indexOf("(", hashIdx);
        const paramsText = method.fqn.slice(parenIdx + 1, method.fqn.lastIndexOf(")"));
        return {
            typeName: typeNameInPackage(method.typeFqn, method.packageFqn),
            methodName: method.fqn.slice(hashIdx + 1, parenIdx),
            parameters: paramsText ? paramsText.split(",").map(param => param.slice(param.lastIndexOf(".") + 1)) : [],
        };
    }

    // 用語集に和名がある場合だけ返す（フォールバックの単純名は表示済みなので出さない）
    function glossaryTitle(term, fallback) {
        return term.title !== fallback ? term.title : null;
    }

    function createNameCell(mainChildren, subChildren, fqn) {
        const sub = subChildren.filter(Boolean);
        return Jig.dom.createElement("td", {
            className: "insight-name",
            attributes: {title: fqn},
            children: [
                Jig.dom.createElement("div", {className: "insight-name__main", children: mainChildren}),
                sub.length ? Jig.dom.createElement("div", {className: "insight-name__sub", children: sub}) : null,
            ],
        });
    }

    function createTermElement(title) {
        return title ? Jig.dom.createElement("span", {className: "insight-name__term", textContent: title}) : null;
    }

    function createPackageElement(packageFqn) {
        return Jig.dom.createElement("span", {className: "insight-name__package", textContent: packageFqn});
    }

    const NAME_CELL_BUILDERS = {
        package(pkg) {
            const simpleName = pkg.fqn.slice(pkg.fqn.lastIndexOf(".") + 1);
            return createNameCell(
                [pkg.fqn],
                [createTermElement(glossaryTitle(Jig.glossary.getPackageTerm(pkg.fqn), simpleName))],
                pkg.fqn);
        },
        type(type) {
            const typeName = typeNameInPackage(type.fqn, type.packageFqn);
            const simpleName = type.fqn.slice(type.fqn.lastIndexOf(".") + 1);
            return createNameCell(
                [Jig.dom.createElement("span", {className: "insight-name__type", textContent: typeName})],
                [createPackageElement(type.packageFqn), createTermElement(glossaryTitle(Jig.glossary.getTypeTerm(type.fqn), simpleName))],
                type.fqn);
        },
        method(method) {
            const {typeName, methodName, parameters} = parseMethod(method);
            const member = methodName === "<init>"
                ? Jig.dom.i18nText("span", "コンストラクタ", {className: "insight-badge"})
                : methodName === "<clinit>"
                    ? Jig.dom.i18nText("span", "static初期化", {className: "insight-badge"})
                    : Jig.dom.createElement("span", {className: "insight-name__member", textContent: methodName});
            // 長い引数リストでも列幅を崩さないよう、カンマの後で折り返せるようにする
            const params = Jig.dom.createElement("span", {
                className: "insight-name__params",
                children: ["(", ...parameters.flatMap((param, i) => i ? [", ", Jig.dom.createElement("wbr"), param] : [param]), ")"],
            });
            return createNameCell(
                [Jig.dom.createElement("span", {className: "insight-name__type", textContent: typeName}), "#", member, params],
                [createPackageElement(method.packageFqn), createTermElement(glossaryTitle(Jig.glossary.getMethodTerm(method.fqn, true), methodName))],
                method.fqn);
        },
    };

    function enrichItems(view, items) {
        if (view.id !== "method") return items;
        return items.map(method => ({...method, numberOfParameters: parseMethod(method).parameters.length}));
    }

    /**
     * 列ごとの上位1%の値。セル内バーの満幅に使う。外れ値1件でほかが潰れないよう最大値は使わない。
     */
    function computeBarScales(columns, items) {
        return Object.fromEntries(columns.map(column => {
            if (column.ratio) return [column.key, 1];
            const values = items.map(item => item[column.key]).sort((a, b) => a - b);
            return [column.key, Math.max(1, values[Math.floor((values.length - 1) * 0.99)])];
        }));
    }

    function createNumberCell(value, column, scale) {
        const cell = Jig.dom.createElement("td", {
            className: value === 0 ? "number insight-zero" : "number",
            textContent: column.ratio ? value.toFixed(2) : value,
        });
        cell.style.setProperty("--insight-bar", `${(Math.min(1, value / scale) * 100).toFixed(1)}%`);
        return cell;
    }

    function createZoomCell() {
        return Jig.dom.createElement("td", {
            children: [Jig.dom.createElement("i", {className: "zoom"})]
        });
    }

    function createRow(view, item, scales) {
        const row = Jig.dom.createElement("tr", {
            children: [
                createZoomCell(),
                NAME_CELL_BUILDERS[view.id](item),
                ...view.columns.map(column => createNumberCell(item[column.key], column, scales[column.key])),
            ],
        });
        row.dataset.fqn = item.fqn;
        if (item.packageFqn != null) row.dataset.packageFqn = item.packageFqn;
        if (item.typeFqn != null) row.dataset.typeFqn = item.typeFqn;
        return row;
    }

    function createHeaderCell(sortKey, label, title) {
        const button = Jig.dom.createElement("button", {
            className: "insight-sort",
            children: [
                Jig.dom.i18nText("span", label, {className: "insight-sort__label"}),
                title ? Jig.dom.i18nText("span", title, {className: "insight-sort__title"}) : null,
            ],
        });
        button.dataset.sortKey = sortKey;
        return Jig.dom.createElement("th", {children: [button]});
    }

    function renderHeader(table, view) {
        const colgroup = Jig.dom.createElement("colgroup", {
            children: [
                Jig.dom.createElement("col", {className: "insight-col-zoom"}),
                Jig.dom.createElement("col"),
                ...view.columns.map(() => Jig.dom.createElement("col", {className: "insight-col-number"})),
            ],
        });
        const headerRow = Jig.dom.createElement("tr", {
            children: [
                Jig.dom.createElement("th"),
                createHeaderCell(NAME_SORT_KEY, "名前"),
                ...view.columns.map(column => createHeaderCell(column.key, column.label, column.title)),
            ],
        });
        table.querySelector("colgroup")?.remove();
        table.prepend(colgroup);
        table.querySelector("thead").replaceChildren(headerRow);
    }

    function compareEntries(key, descending) {
        if (key === NAME_SORT_KEY) {
            return (a, b) => a.item.fqn.localeCompare(b.item.fqn) * (descending ? -1 : 1);
        }
        return (a, b) => (descending ? b.item[key] - a.item[key] : a.item[key] - b.item[key])
            || a.item.fqn.localeCompare(b.item.fqn);
    }

    /**
     * 行要素を作り直さずに並べ替える。ズームで付けた非表示状態を保つため。
     */
    function sortTable(table, entries, sortState, key) {
        // 数値は大きい順、名前は昇順から始める
        sortState.descending = sortState.key === key ? !sortState.descending : key !== NAME_SORT_KEY;
        sortState.key = key;
        entries.sort(compareEntries(key, sortState.descending));
        table.querySelector("tbody").append(...entries.map(entry => entry.row));
        table.querySelectorAll("thead th").forEach(th => {
            const button = th.querySelector("button.insight-sort");
            if (button && button.dataset.sortKey === key) {
                th.setAttribute("aria-sort", sortState.descending ? "descending" : "ascending");
            } else {
                th.removeAttribute("aria-sort");
            }
        });
    }

    function renderView(view, rawItems) {
        const table = document.getElementById(view.tableId);
        const tbody = table?.querySelector("tbody");
        if (!tbody) return;
        const items = enrichItems(view, rawItems);
        const scales = computeBarScales(view.columns, items);
        const entries = items.map(item => ({item, row: createRow(view, item, scales)}));

        renderHeader(table, view);
        const fragment = document.createDocumentFragment();
        entries.forEach(entry => fragment.appendChild(entry.row));
        tbody.replaceChildren(fragment);

        const sortState = {key: null, descending: false};
        table.querySelector("thead").addEventListener("click", event => {
            const button = event.target.closest("button.insight-sort");
            if (button) sortTable(table, entries, sortState, button.dataset.sortKey);
        });
    }

    function setupZoomIcons() {
        // 行が増えても addEventListener が増えないよう tbody 単位で委譲する
        document.querySelectorAll("table").forEach(table => {
            const tbody = table.querySelector("tbody");
            if (!tbody) return;
            tbody.addEventListener("click", (event) => {
                const icon = event.target.closest("i.zoom");
                if (!icon) return;

                const row = icon.closest("tr");
                if (!row) return;
                const fqn = row.dataset.fqn;

                // クリックされた行以外を非表示にする
                tbody.querySelectorAll("tr").forEach(r => {
                    if (r !== row && !fqnStartsWith(fqn, r)) {
                        r.classList.add("hidden-by-zoom");
                    }
                });

                zoomFamilyTables(table, row);
                // ズーム解除ボタンを表示
                document.getElementById("cancel-zoom")?.classList.remove("hidden");
            });
        });
    }

    function cancelZoom(buttonEl) {
        document.querySelectorAll("table tbody tr.hidden-by-zoom").forEach(row => {
            row.classList.remove("hidden-by-zoom");
        });
        if (buttonEl) buttonEl.classList.add("hidden");
    }

    function fqnStartsWith(prefix, targetRow) {
        return targetRow.dataset.fqn.startsWith(prefix);
    }

    function zoomFamilyTables(baseTable, baseRow) {
        baseTable.parentElement.querySelectorAll("table").forEach(table => {
            if (table === baseTable) return;

            const allRows = table.querySelectorAll("tbody tr");

            // zoomされているものがあったら一旦解除して
            const hiddenRows = table.querySelectorAll("tbody tr.hidden-by-zoom");
            if (hiddenRows.length > 0) {
                allRows.forEach(r => {
                    r.classList.remove("hidden-by-zoom");
                });
            }

            // 関係するもの以外を非表示にする
            // 元テーブルと対象テーブルの組み合わせでprefixが変わる
            let prefix = baseRow.dataset.fqn;
            if (baseTable.id.includes("package")) {
                prefix = prefix + '.';
            } else if (baseTable.id.includes("type")) {
                if (table.id.includes("package")) {
                    prefix = baseRow.dataset.packageFqn;
                } else if (table.id.includes("method")) {
                    prefix = prefix + '#';
                }
            } else if (baseTable.id.includes("method")) {
                if (table.id.includes("package")) {
                    prefix = baseRow.dataset.packageFqn;
                }
                if (table.id.includes("type")) {
                    prefix = baseRow.dataset.typeFqn;
                }
            }
            allRows.forEach(r => {
                if (!fqnStartsWith(prefix, r)) r.classList.add("hidden-by-zoom");
            });
        })
    }

    /**
     * 0件のセクションは見出し・テーブル・ナビゲーションリンクごと取り除く。
     */
    function removeSection(view) {
        document.getElementById(view.headingId)?.remove();
        document.getElementById(view.tableId)?.remove();

        const nav = document.getElementById("insight-section-nav");
        if (!nav) return;
        nav.querySelectorAll("a").forEach(link => {
            if (link.getAttribute("href") === `#${view.headingId}`) link.remove();
        });
        if (nav.querySelectorAll("a").length === 0) nav.remove();
    }

    function init() {
        const insightData = parseInsightData();
        if (!insightData) {
            Jig.dom.renderDataLoadError(document.querySelector("main"), "insight-data.js");
            return;
        }

        let renderedSectionCount = 0;
        VIEWS.forEach(view => {
            const items = insightData[view.dataKey] || [];
            if (items.length === 0) {
                removeSection(view);
                return;
            }
            renderView(view, items);
            setInsightCount(view.countId, items.length);
            renderedSectionCount++;
        });

        if (renderedSectionCount === 0) {
            Jig.dom.renderEmptyDocument(document.querySelector("main"), "Insight");
            return;
        }

        setupZoomIcons();
        const cancelBtn = document.getElementById("cancel-zoom");
        cancelBtn?.addEventListener("click", () => cancelZoom(cancelBtn));
    }

    return {
        init,
        parseInsightData,
        parseMethod,
        computeBarScales,
    }
})();

Jig.bootstrap.register("insight", InsightApp.init);

if (typeof module !== "undefined" && module.exports) {
    module.exports = InsightApp;
}
