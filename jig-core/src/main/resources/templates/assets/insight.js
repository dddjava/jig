const InsightApp = (() => {
    const Jig = globalThis.Jig;

    // label は見出しの短縮名、title は正式名。どちらも i18n キー
    // lenses は「どこを見るか」のプリセット。閾値はプロジェクト間で比べられるよう固定値にする
    const VIEWS = [
        {
            id: "package", kind: "パッケージ", dataKey: "packages", tableId: "package-insight-list",
            columns: [
                {key: "numberOfTypes", label: "クラス数"},
                {key: "numberOfMethods", label: "メソッド数"},
                {key: "numberOfUsingTypes", label: "使用型", title: "使用クラス数"},
                {key: "cyclomaticComplexity", label: "複雑度", title: "循環的複雑度合計"},
                {key: "size", label: "規模", title: "規模合計"},
            ],
            lenses: [
                {id: "large-package", label: "大きいパッケージ", conditions: [{key: "numberOfTypes", min: 15}]},
                {id: "package-dependencies", label: "多くの型に依存する", conditions: [{key: "numberOfUsingTypes", min: 50}]},
            ],
        },
        {
            id: "type", kind: "クラス", dataKey: "types", tableId: "type-insight-list",
            columns: [
                {key: "numberOfMethods", label: "メソッド数"},
                {key: "numberOfUsingTypes", label: "Ce", title: "使用クラス数"},
                {key: "numberOfUsedByTypes", label: "Ca", title: "被使用クラス数"},
                {key: "instability", label: "I", title: "不安定性", ratio: true},
                {key: "lcom", label: "LCOM", title: "凝集度欠如", ratio: true},
                {key: "cyclomaticComplexity", label: "複雑度", title: "循環的複雑度合計"},
                {key: "size", label: "規模", title: "規模合計"},
            ],
            lenses: [
                {id: "large-type", label: "大きいクラス", conditions: [{key: "numberOfMethods", min: 30}]},
                {id: "low-cohesion", label: "凝集度が低い", conditions: [{key: "lcom", min: 0.8}, {key: "numberOfMethods", min: 10}]},
                {id: "widely-used", label: "多くから使われる", conditions: [{key: "numberOfUsedByTypes", min: 20}]},
                {id: "type-dependencies", label: "多くの型に依存する", conditions: [{key: "numberOfUsingTypes", min: 20}]},
            ],
        },
        {
            id: "method", kind: "メソッド", dataKey: "methods", tableId: "method-insight-list",
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
            lenses: [
                {id: "complex-method", label: "複雑なメソッド", conditions: [{key: "cyclomaticComplexity", min: 10}]},
                {id: "long-method", label: "長いメソッド", conditions: [{key: "size", min: 50}]},
                {id: "many-parameters", label: "引数が多い", conditions: [{key: "numberOfParameters", min: 5}]},
                {id: "method-dependencies", label: "多くの型に依存する", conditions: [{key: "numberOfUsingTypes", min: 15}]},
            ],
        },
    ];

    const NAME_SORT_KEY = "fqn";
    const SEARCH_DEBOUNCE_MS = 150;

    /**
     * 画面の状態。scope は {kind: "package", fqn} か {kind: "type", fqn, packageFqn}
     */
    const state = {viewId: null, scope: null, searchTerms: [], hideInitializers: false, lensId: null};
    /** @type {Map<string, {view: object, table: HTMLTableElement, entries: object[], sortState: object}>} */
    const panels = new Map();

    // 桁の多い合計値を読み違えないよう3桁区切りにする
    const integerFormat = new Intl.NumberFormat("en-US");

    function formatCount(value) {
        return integerFormat.format(value);
    }

    function parseInsightData() {
        return Jig.data.insight.get();
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

    function isInitializer(methodName) {
        return methodName === "<init>" || methodName === "<clinit>";
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

    /**
     * クリックで範囲を絞り込むリンク。drillViewId があればその表へ移る。
     */
    function createScopeLink(className, textContent, scope, drillViewId) {
        const button = Jig.dom.createElement("button", {className: `insight-link ${className}`, textContent});
        button.dataset.scopeKind = scope.kind;
        button.dataset.scopeFqn = scope.fqn;
        if (scope.packageFqn != null) button.dataset.scopePackageFqn = scope.packageFqn;
        if (drillViewId) button.dataset.drillViewId = drillViewId;
        return button;
    }

    function createPackageLink(packageFqn) {
        return createScopeLink("insight-name__package", packageFqn, {kind: "package", fqn: packageFqn});
    }

    const NAME_CELL_BUILDERS = {
        package(pkg) {
            const simpleName = pkg.fqn.slice(pkg.fqn.lastIndexOf(".") + 1);
            const term = glossaryTitle(Jig.glossary.getPackageTerm(pkg.fqn), simpleName);
            return {
                cell: createNameCell(
                    [createScopeLink("", pkg.fqn, {kind: "package", fqn: pkg.fqn}, "type")],
                    [createTermElement(term)],
                    pkg.fqn),
                term,
            };
        },
        type(type) {
            const typeName = typeNameInPackage(type.fqn, type.packageFqn);
            const simpleName = type.fqn.slice(type.fqn.lastIndexOf(".") + 1);
            const term = glossaryTitle(Jig.glossary.getTypeTerm(type.fqn), simpleName);
            return {
                cell: createNameCell(
                    [createScopeLink("insight-name__type", typeName, {kind: "type", fqn: type.fqn, packageFqn: type.packageFqn}, "method")],
                    [createPackageLink(type.packageFqn), createTermElement(term)],
                    type.fqn),
                term,
            };
        },
        method(method) {
            const {typeName, methodName, parameters} = parseMethod(method);
            const term = glossaryTitle(Jig.glossary.getMethodTerm(method.fqn, true), methodName);
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
            return {
                cell: createNameCell(
                    [createScopeLink("insight-name__type", typeName, {kind: "type", fqn: method.typeFqn, packageFqn: method.packageFqn}), "#", member, params],
                    [createPackageLink(method.packageFqn), createTermElement(term)],
                    method.fqn),
                term,
            };
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
            textContent: column.ratio ? value.toFixed(2) : formatCount(value),
        });
        cell.style.setProperty("--insight-bar", `${(Math.min(1, value / scale) * 100).toFixed(1)}%`);
        return cell;
    }

    function createEntry(view, item, scales) {
        const {cell, term} = NAME_CELL_BUILDERS[view.id](item);
        const row = Jig.dom.createElement("tr", {
            children: [cell, ...view.columns.map(column => createNumberCell(item[column.key], column, scales[column.key]))],
        });
        row.dataset.fqn = item.fqn;
        return {
            item,
            row,
            searchText: `${item.fqn} ${term ?? ""}`.toLowerCase(),
            initializer: view.id === "method" && isInitializer(parseMethod(item).methodName),
        };
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
                Jig.dom.createElement("col"),
                ...view.columns.map(() => Jig.dom.createElement("col", {className: "insight-col-number"})),
            ],
        });
        const headerRow = Jig.dom.createElement("tr", {
            children: [
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
     * 行要素を作り直さずに並べ替える。絞り込みによる非表示状態はそのまま保たれる。
     */
    function sortPanel(panel, key, descending) {
        const {table, entries, sortState} = panel;
        sortState.descending = descending;
        sortState.key = key;
        entries.sort(compareEntries(key, sortState.descending));
        table.querySelector("tbody").append(...entries.map(entry => entry.row));
        stripeVisibleRows(entries);
        table.querySelectorAll("thead th").forEach(th => {
            if (th.querySelector("button.insight-sort")?.dataset.sortKey === key) {
                th.setAttribute("aria-sort", sortState.descending ? "descending" : "ascending");
            } else {
                th.removeAttribute("aria-sort");
            }
        });
    }

    /**
     * 非表示の行があると :nth-child の縞がずれるため、表示中の行の並び順で交互に付ける
     */
    function stripeVisibleRows(entries) {
        let visibleIndex = 0;
        entries.forEach(entry => {
            if (entry.row.hidden) return;
            entry.row.classList.toggle("insight-row-alt", visibleIndex % 2 === 1);
            visibleIndex++;
        });
    }

    function createPanel(view, rawItems) {
        const table = document.getElementById(view.tableId);
        const items = enrichItems(view, rawItems);
        const scales = computeBarScales(view.columns, items);
        const entries = items.map(item => createEntry(view, item, scales));

        renderHeader(table, view);
        const fragment = document.createDocumentFragment();
        entries.forEach(entry => fragment.appendChild(entry.row));
        table.querySelector("tbody").replaceChildren(fragment);

        const panel = {view, table, entries, sortState: {key: null, descending: false}};
        table.querySelector("thead").addEventListener("click", event => {
            const button = event.target.closest("button.insight-sort");
            if (!button) return;
            const key = button.dataset.sortKey;
            // 数値は大きい順、名前は昇順から始める
            sortPanel(panel, key, panel.sortState.key === key ? !panel.sortState.descending : key !== NAME_SORT_KEY);
        });
        return panel;
    }

    // --- 絞り込み ---

    function isUnderPackage(packageFqn, scopePackageFqn) {
        return packageFqn === scopePackageFqn || packageFqn.startsWith(scopePackageFqn + ".");
    }

    function isUnderType(typeFqn, scopeTypeFqn) {
        return typeFqn === scopeTypeFqn || typeFqn.startsWith(scopeTypeFqn + "$");
    }

    function inScope(viewId, item) {
        const scope = state.scope;
        if (!scope) return true;
        if (scope.kind === "package") {
            return isUnderPackage(viewId === "package" ? item.fqn : item.packageFqn, scope.fqn);
        }
        if (viewId === "package") return item.fqn === scope.packageFqn;
        return isUnderType(viewId === "type" ? item.fqn : item.typeFqn, scope.fqn);
    }

    function matchesFilters(entry) {
        if (state.hideInitializers && entry.initializer) return false;
        return state.searchTerms.every(term => entry.searchText.includes(term));
    }

    function meetsCondition(item, condition) {
        return item[condition.key] >= condition.min;
    }

    function matchesLens(lens, item) {
        return lens.conditions.every(condition => meetsCondition(item, condition));
    }

    function activeLens(panel) {
        return panel.view.lenses.find(lens => lens.id === state.lensId) ?? null;
    }

    /**
     * 行の表示を切り替え、レンズの該当セルを強調する。レンズの件数はレンズ以外の条件で数える。
     */
    function applyFilters(panel) {
        const lens = activeLens(panel);
        const lensCounts = new Map(panel.view.lenses.map(l => [l.id, 0]));
        const hitColumnIndexes = new Set(lens ? lens.conditions.map(c => panel.view.columns.findIndex(col => col.key === c.key)) : []);
        let scopedCount = 0;
        let visibleCount = 0;
        panel.entries.forEach(entry => {
            const scoped = inScope(panel.view.id, entry.item);
            const filtered = scoped && matchesFilters(entry);
            if (filtered) panel.view.lenses.forEach(l => {
                if (matchesLens(l, entry.item)) lensCounts.set(l.id, lensCounts.get(l.id) + 1);
            });
            const visible = filtered && (!lens || matchesLens(lens, entry.item));
            if (scoped) scopedCount++;
            if (visible) visibleCount++;
            entry.row.hidden = !visible;
            panel.view.columns.forEach((column, i) => {
                // 先頭は名前のセル
                entry.row.cells[i + 1].classList.toggle("insight-hit", hitColumnIndexes.has(i));
            });
        });
        stripeVisibleRows(panel.entries);
        return {scopedCount, visibleCount, lensCounts};
    }

    // --- 画面部品 ---

    function renderTabs() {
        const tabs = document.getElementById("insight-tabs");
        tabs.replaceChildren(...[...panels.values()].map(({view, entries}) => {
            const count = entries.filter(entry => inScope(view.id, entry.item)).length;
            const tab = Jig.dom.createElement("button", {
                className: "insight-tab",
                attributes: {role: "tab", "aria-selected": String(view.id === state.viewId), "aria-controls": view.tableId},
                children: [
                    Jig.dom.kind.badgeElement(view.kind),
                    Jig.dom.i18nText("span", view.kind),
                    Jig.dom.createElement("span", {className: "insight-tab__count", textContent: formatCount(count)}),
                ],
            });
            tab.dataset.viewId = view.id;
            return tab;
        }));
    }

    /**
     * パッケージの各階層をリンクにし、上位のパッケージへ範囲を広げられるようにする。
     * linkLast が false なら末尾（現在の範囲）はリンクにしない。
     */
    function createPackageCrumbs(packageFqn, linkLast) {
        const segments = packageFqn.split(".");
        const crumbs = Jig.dom.createElement("span", {
            className: "insight-scope__crumb",
            children: segments.flatMap((segment, i) => {
                const isLast = i === segments.length - 1;
                const node = isLast && !linkLast
                    ? Jig.dom.createElement("span", {textContent: segment})
                    : createScopeLink("", segment, {kind: "package", fqn: segments.slice(0, i + 1).join(".")});
                return i ? [".", node] : [node];
            }),
        });
        return [Jig.dom.kind.badgeElement("パッケージ"), crumbs];
    }

    function renderScope() {
        const container = document.getElementById("insight-scope");
        const scope = state.scope;
        const children = [Jig.dom.i18nText("span", "範囲", {className: "insight-scope__label"})];
        if (!scope) {
            children.push(Jig.dom.i18nText("span", "すべて", {className: "insight-scope__crumb"}));
        } else {
            const clear = Jig.dom.i18nText("button", "すべて", {className: "insight-link insight-scope__crumb"});
            clear.dataset.scopeClear = "";
            children.push(clear, " › ");
            if (scope.kind === "package") {
                children.push(...createPackageCrumbs(scope.fqn, false));
            } else {
                children.push(
                    ...createPackageCrumbs(scope.packageFqn, true),
                    " › ",
                    Jig.dom.kind.badgeElement("クラス"),
                    Jig.dom.createElement("span", {className: "insight-scope__crumb", textContent: typeNameInPackage(scope.fqn, scope.packageFqn)}));
            }
        }
        container.replaceChildren(...children);
    }

    function createLensRule(view, lens) {
        return Jig.dom.createElement("span", {
            className: "insight-lens__rule",
            children: lens.conditions.flatMap((condition, i) => [
                i ? ", " : null,
                Jig.dom.i18nText("span", view.columns.find(column => column.key === condition.key).label),
                `≥${condition.min}`,
            ]),
        });
    }

    function renderLenses(view, lensCounts) {
        const container = document.getElementById("insight-lenses");
        if (view.lenses.length === 0) {
            container.replaceChildren();
            return;
        }
        container.replaceChildren(
            Jig.dom.i18nText("span", "観点", {className: "insight-lenses__label"}),
            ...view.lenses.map(lens => {
                const count = lensCounts.get(lens.id);
                const active = lens.id === state.lensId;
                const button = Jig.dom.createElement("button", {
                    className: "insight-lens",
                    attributes: {"aria-pressed": String(active)},
                    children: [
                        Jig.dom.i18nText("span", lens.label),
                        createLensRule(view, lens),
                        Jig.dom.createElement("span", {className: "insight-lens__count", textContent: formatCount(count)}),
                    ],
                });
                button.disabled = count === 0 && !active;
                button.dataset.lensId = lens.id;
                return button;
            }));
    }

    function renderStatus({scopedCount, visibleCount}) {
        document.getElementById("insight-status").replaceChildren(
            Jig.dom.i18nText("span", "表示"),
            ` ${formatCount(visibleCount)} / ${formatCount(scopedCount)}`,
        );
    }

    function render() {
        panels.forEach(panel => {
            panel.table.hidden = panel.view.id !== state.viewId;
        });
        renderTabs();
        renderScope();
        const panel = panels.get(state.viewId);
        const result = applyFilters(panel);
        renderLenses(panel.view, result.lensCounts);
        renderStatus(result);
        const hideInitializers = document.getElementById("insight-hide-initializers");
        if (hideInitializers) hideInitializers.disabled = state.viewId !== "method";
    }

    // --- イベント ---

    function scopeFromDataset(dataset) {
        if (dataset.scopeKind === "package") return {kind: "package", fqn: dataset.scopeFqn};
        return {kind: "type", fqn: dataset.scopeFqn, packageFqn: dataset.scopePackageFqn};
    }

    function selectView(viewId) {
        // レンズは表ごとに異なるため、表を移ったら外す
        if (viewId !== state.viewId) state.lensId = null;
        state.viewId = viewId;
    }

    /**
     * 選んだレンズの最初の条件で大きい順に並べる。外したときは並び順を変えない。
     */
    function toggleLens(lensId) {
        const panel = panels.get(state.viewId);
        state.lensId = state.lensId === lensId ? null : lensId;
        const lens = activeLens(panel);
        if (lens) sortPanel(panel, lens.conditions[0].key, true);
    }

    function setupEvents(main) {
        main.addEventListener("click", event => {
            const tab = event.target.closest("button.insight-tab");
            if (tab) {
                selectView(tab.dataset.viewId);
                render();
                return;
            }
            const lensButton = event.target.closest("button.insight-lens");
            if (lensButton) {
                toggleLens(lensButton.dataset.lensId);
                render();
                return;
            }
            const link = event.target.closest("button.insight-link");
            if (!link) return;
            if ("scopeClear" in link.dataset) {
                state.scope = null;
            } else {
                state.scope = scopeFromDataset(link.dataset);
                if (link.dataset.drillViewId && panels.has(link.dataset.drillViewId)) {
                    selectView(link.dataset.drillViewId);
                }
            }
            render();
        });

        let searchTimer;
        document.getElementById("insight-search")?.addEventListener("input", event => {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(() => {
                state.searchTerms = event.target.value.toLowerCase().split(/\s+/).filter(Boolean);
                render();
            }, SEARCH_DEBOUNCE_MS);
        });

        document.getElementById("insight-hide-initializers")?.addEventListener("change", event => {
            state.hideInitializers = event.target.checked;
            render();
        });
    }

    function init() {
        const main = document.querySelector("main");
        const insightData = parseInsightData();
        if (!insightData) {
            document.getElementById("insight-controls")?.remove();
            Jig.dom.renderDataLoadError(main, "insight-data.js");
            return;
        }

        VIEWS.forEach(view => {
            const items = insightData[view.dataKey] || [];
            if (items.length === 0) {
                document.getElementById(view.tableId)?.remove();
                return;
            }
            panels.set(view.id, createPanel(view, items));
        });

        if (panels.size === 0) {
            document.getElementById("insight-controls")?.remove();
            Jig.dom.renderEmptyDocument(main, "Insight");
            return;
        }

        state.viewId = panels.keys().next().value;
        setupEvents(main);
        render();
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
