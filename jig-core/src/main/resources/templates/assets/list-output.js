const ListOutputApp = (() => {
    const Jig = globalThis.Jig;

    function getTypeLabel(item) {
        return Jig.glossary.getTypeTerm(item.packageName + "." + item.typeName).title;
    }

    function getPackageLabel(item) {
        return Jig.glossary.getPackageTerm(item.packageName).title;
    }

    function getReturnTypeLabel(item) {
        return Jig.glossary.getTypeTerm(item.returnTypeFqn ?? "").title;
    }

    function getParameterTypeLabels(item) {
        return (item.parameterTypeFqns ?? []).map(fqn => Jig.glossary.getTypeTerm(fqn).title);
    }

    function getMethodLabel(item) {
        const fqn = item.methodFqn ?? "";
        if (!fqn) return "";
        const term = Jig.glossary.findTerm(fqn);
        if (!term) return "";
        const hashIdx = fqn.lastIndexOf('#');
        const parenIdx = fqn.indexOf('(', hashIdx);
        const methodName = fqn.substring(hashIdx + 1, parenIdx);
        return term.title !== methodName ? term.title : "";
    }

    function formatFieldTypes(fieldTypes) {
        if (!fieldTypes) return "";
        if (Array.isArray(fieldTypes)) return fieldTypes.join("\n");
        return String(fieldTypes);
    }

    function markIfTrue(value) {
        return value ? "◯" : "";
    }

    // 種別名から導出する。新しい種別を足すときに ID を別途登録しなくてよい
    const kebabCase = name => name.replace(/[A-Z]/g, char => `-${char.toLowerCase()}`);
    const tableIdOf = name => `${kebabCase(name)}-list`;
    const csvFileOf = name => `list-output-${kebabCase(name)}.csv`;
    const csvButtonIdOf = name => `export-${kebabCase(name)}-csv`;

    /**
     * 一覧の種別ごとの定義。CSV 出力・テーブル描画の両方がここだけを見る。
     *
     *  - items   : 一覧データから対象の配列を取り出す関数
     *  - columns : 列の定義
     *    - label : 表示ヘッダ
     *    - get   : item から値を取り出す関数
     *    - type  : テーブルセルに付与する className（"number" のみ使用中）
     */
    const TABLES = {
        controller: {
            items: d => d.applications.controllers,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "クラス名", get: i => i.typeName ?? ""},
                {label: "メソッドシグネチャ", get: i => i.methodSignature ?? ""},
                {label: "メソッド戻り値の型", get: i => i.returnType ?? ""},
                {label: "クラス別名", get: getTypeLabel},
                {label: "使用しているフィールドの型", get: i => formatFieldTypes(i.usingFieldTypes)},
                {label: "循環的複雑度", get: i => i.cyclomaticComplexity ?? "", type: "number"},
                {label: "パス", get: i => i.path ?? ""},
            ],
        },
        service: {
            items: d => d.applications.services,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "クラス名", get: i => i.typeName ?? ""},
                {label: "メソッドシグネチャ", get: i => i.methodSignature ?? ""},
                {label: "メソッド戻り値の型", get: i => i.returnType ?? ""},
                {label: "イベントハンドラ", get: i => markIfTrue(i.eventHandler)},
                {label: "クラス別名", get: getTypeLabel},
                {label: "メソッド別名", get: getMethodLabel},
                {label: "メソッド戻り値の型の別名", get: getReturnTypeLabel},
                {label: "メソッド引数の型の別名", get: i => formatFieldTypes(getParameterTypeLabels(i))},
                {label: "使用しているフィールドの型", get: i => formatFieldTypes(i.usingFieldTypes)},
                {label: "循環的複雑度", get: i => i.cyclomaticComplexity ?? "", type: "number"},
                {label: "使用しているサービスのメソッド", get: i => formatFieldTypes(i.usingServiceMethods)},
                {label: "使用しているリポジトリのメソッド", get: i => formatFieldTypes(i.usingRepositoryMethods)},
                {label: "null使用", get: i => markIfTrue(i.useNull)},
                {label: "stream使用", get: i => markIfTrue(i.useStream)},
            ],
        },
        repository: {
            items: d => d.applications.repositories,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "クラス名", get: i => i.typeName ?? ""},
                {label: "メソッドシグネチャ", get: i => i.methodSignature ?? ""},
                {label: "メソッド戻り値の型", get: i => i.returnType ?? ""},
                {label: "クラス別名", get: getTypeLabel},
                {label: "メソッド戻り値の型の別名", get: getReturnTypeLabel},
                {label: "メソッド引数の型の別名", get: i => formatFieldTypes(getParameterTypeLabels(i))},
                {label: "循環的複雑度", get: i => i.cyclomaticComplexity ?? "", type: "number"},
                {label: "INSERT", get: i => formatFieldTypes(i.insertTables)},
                {label: "SELECT", get: i => formatFieldTypes(i.selectTables)},
                {label: "UPDATE", get: i => formatFieldTypes(i.updateTables)},
                {label: "DELETE", get: i => formatFieldTypes(i.deleteTables)},
                {label: "UNKNOWN", get: i => formatFieldTypes(i.unknownTables)},
                {label: "関連元クラス数", get: i => i.callerTypeCount ?? "", type: "number"},
                {label: "関連元メソッド数", get: i => i.callerMethodCount ?? "", type: "number"},
            ],
        },
        businessPackage: {
            items: d => d.businessRules.packages,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "パッケージ別名", get: getPackageLabel},
                {label: "クラス数", get: i => i.classCount ?? "", type: "number"},
            ],
        },
        businessAll: {
            items: d => d.businessRules.all,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "クラス名", get: i => i.typeName ?? ""},
                {label: "クラス別名", get: getTypeLabel},
                {label: "ビジネスルールの種類", get: i => i.businessRuleKind ?? ""},
                {label: "関連元ビジネスルール数", get: i => i.incomingBusinessRuleCount ?? "", type: "number"},
                {label: "関連先ビジネスルール数", get: i => i.outgoingBusinessRuleCount ?? "", type: "number"},
                {label: "関連元クラス数", get: i => i.incomingClassCount ?? "", type: "number"},
                {label: "非PUBLIC", get: i => markIfTrue(i.nonPublic)},
                {label: "同パッケージからのみ参照", get: i => markIfTrue(i.samePackageOnly)},
                {label: "関連元クラス", get: i => i.incomingClassList ?? ""},
            ],
        },
        businessEnum: {
            items: d => d.businessRules.enums,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "クラス名", get: i => i.typeName ?? ""},
                {label: "クラス別名", get: getTypeLabel},
                {label: "定数宣言", get: i => i.constants ?? ""},
                {label: "フィールド", get: i => i.fields ?? ""},
                {label: "使用箇所数", get: i => i.usageCount ?? "", type: "number"},
                {label: "使用箇所", get: i => i.usagePlaces ?? ""},
                {label: "パラメーター有り", get: i => markIfTrue(i.hasParameters)},
                {label: "振る舞い有り", get: i => markIfTrue(i.hasBehavior)},
                {label: "多態", get: i => markIfTrue(i.isPolymorphic)},
            ],
        },
        businessCollection: {
            items: d => d.businessRules.collections,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "クラス名", get: i => i.typeName ?? ""},
                {label: "クラス別名", get: getTypeLabel},
                {label: "フィールドの型", get: i => i.fieldTypes ?? ""},
                {label: "使用箇所数", get: i => i.usageCount ?? "", type: "number"},
                {label: "使用箇所", get: i => i.usagePlaces ?? ""},
                {label: "メソッド数", get: i => i.methodCount ?? "", type: "number"},
                {label: "メソッド一覧", get: i => i.methods ?? ""},
            ],
        },
        businessValidation: {
            items: d => d.businessRules.validations,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "クラス名", get: i => i.typeName ?? ""},
                {label: "クラス別名", get: getTypeLabel},
                {label: "メンバ名", get: i => i.memberName ?? ""},
                {label: "メンバクラス名", get: i => i.memberType ?? ""},
                {label: "アノテーションクラス名", get: i => i.annotationType ?? ""},
                {label: "アノテーション記述", get: i => i.annotationDescription ?? ""},
            ],
        },
        businessSmell: {
            items: d => d.businessRules.methodSmells,
            columns: [
                {label: "パッケージ名", get: i => i.packageName ?? ""},
                {label: "クラス名", get: i => i.typeName ?? ""},
                {label: "メソッドシグネチャ", get: i => i.methodSignature ?? ""},
                {label: "メソッド戻り値の型", get: i => i.returnType ?? ""},
                {label: "クラス別名", get: getTypeLabel},
                {label: "メンバを使用していない", get: i => markIfTrue(i.notUseMember)},
                {label: "基本型の授受を行なっている", get: i => markIfTrue(i.primitiveInterface)},
                {label: "NULLリテラルを使用している", get: i => markIfTrue(i.referenceNull)},
                {label: "NULL判定をしている", get: i => markIfTrue(i.nullDecision)},
                {label: "真偽値を返している", get: i => markIfTrue(i.returnsBoolean)},
                {label: "voidを返している", get: i => markIfTrue(i.returnsVoid)},
            ],
        },
    };

    const headerDefinitions = Object.fromEntries(
        Object.entries(TABLES).map(([name, {columns}]) => [name, columns.map(c => c.label)])
    );

    function buildCsv(name, items) {
        const {columns} = TABLES[name];
        const rows = items.map(item => columns.map(c => c.get(item)));
        return Jig.dom.buildCsv(headerDefinitions[name], rows);
    }

    function renderTable(name, items) {
        const {columns} = TABLES[name];
        Jig.dom.renderTableRows(tableIdOf(name), items,
            (row, item) => columns.forEach(c => row.appendChild(Jig.dom.createCell(c.get(item), c.type))),
            {clear: true}
        );
    }

    function getListData() {
        const listData = Jig.data.list.get() || {};
        const emptyBusinessRules = {
            packages: [],
            all: [],
            enums: [],
            collections: [],
            validations: [],
            methodSmells: [],
        };
        const emptyApplications = {
            controllers: [],
            services: [],
            repositories: [],
        };
        if (Array.isArray(listData)) {
            return {
                businessRules: emptyBusinessRules,
                applications: {...emptyApplications, controllers: listData},
            };
        }
        if (listData.businessRules || listData.applications) {
            return {
                businessRules: {...emptyBusinessRules, ...(listData.businessRules ?? {})},
                applications: {...emptyApplications, ...(listData.applications ?? {})},
            };
        }
        return {
            businessRules: emptyBusinessRules,
            applications: {
                controllers: listData.controllers ?? [],
                services: listData.services ?? [],
                repositories: listData.repositories ?? [],
            },
        };
    }

    function renderTableHeader(tableElementId, headers) {
        const table = document.getElementById(tableElementId);
        if (!table) return;

        const thead = Jig.dom.createElement("thead");
        const tr = Jig.dom.createElement("tr");
        headers.forEach(headerText => {
            const th = Jig.dom.i18nText("th", headerText);
            tr.appendChild(th);
        });
        thead.appendChild(tr);
        table.prepend(thead);
    }

    function activateTabGroup(group, tabName) {
        const tabs = document.querySelectorAll(`.list-output-tab[data-tab-group="${group}"]`);
        const buttons = document.querySelectorAll(`.tab-button[data-tab-group="${group}"]`);
        tabs.forEach(tab => {
            tab.classList.toggle("is-active", tab.dataset.tab === tabName);
        });
        buttons.forEach(button => {
            const isActive = button.dataset.tab === tabName;
            button.classList.toggle("is-active", isActive);
            button.setAttribute("aria-selected", String(isActive));
        });
    }

    function init() {
        const names = Object.keys(TABLES);
        names.forEach(name => renderTableHeader(tableIdOf(name), headerDefinitions[name]));

        const data = getListData();
        names.forEach(name => renderTable(name, TABLES[name].items(data)));

        document.querySelectorAll(".list-output-tabs .tab-button").forEach(button => {
            button.addEventListener("click", () => activateTabGroup(button.dataset.tabGroup, button.dataset.tab));
        });

        names.forEach(name => {
            document.getElementById(csvButtonIdOf(name))?.addEventListener("click",
                () => Jig.dom.downloadCsv(buildCsv(name, TABLES[name].items(data)), csvFileOf(name)));
        });
    }

    return {
        init,
        getListData,
        formatFieldTypes,
        getTypeLabel,
        getPackageLabel,
        getReturnTypeLabel,
        getParameterTypeLabels,
        getMethodLabel,
        buildCsv,
        renderTable,
        activateTabGroup,
        headerDefinitions,
        renderTableHeader,
    };
})();

Jig.bootstrap.register("list-output", ListOutputApp.init);

if (typeof module !== "undefined" && module.exports) {
    module.exports = ListOutputApp;
}
