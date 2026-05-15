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

    /**
     * カラム定義。各列を CSV 出力 / テーブル描画の両方で共有する。
     *
     *  - label : 表示ヘッダ
     *  - get   : item から値を取り出す関数
     *  - type  : テーブルセルに付与する className（"number" のみ使用中）
     */
    const columns = {
        controller: [
            {label: "パッケージ名", get: i => i.packageName ?? ""},
            {label: "クラス名", get: i => i.typeName ?? ""},
            {label: "メソッドシグネチャ", get: i => i.methodSignature ?? ""},
            {label: "メソッド戻り値の型", get: i => i.returnType ?? ""},
            {label: "クラス別名", get: getTypeLabel},
            {label: "使用しているフィールドの型", get: i => formatFieldTypes(i.usingFieldTypes)},
            {label: "循環的複雑度", get: i => i.cyclomaticComplexity ?? "", type: "number"},
            {label: "パス", get: i => i.path ?? ""},
        ],
        service: [
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
        repository: [
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
            {label: "関連元クラス数", get: i => i.callerTypeCount ?? "", type: "number"},
            {label: "関連元メソッド数", get: i => i.callerMethodCount ?? "", type: "number"},
        ],
        businessPackage: [
            {label: "パッケージ名", get: i => i.packageName ?? ""},
            {label: "パッケージ別名", get: getPackageLabel},
            {label: "クラス数", get: i => i.classCount ?? "", type: "number"},
        ],
        businessAll: [
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
        businessEnum: [
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
        businessCollection: [
            {label: "パッケージ名", get: i => i.packageName ?? ""},
            {label: "クラス名", get: i => i.typeName ?? ""},
            {label: "クラス別名", get: getTypeLabel},
            {label: "フィールドの型", get: i => i.fieldTypes ?? ""},
            {label: "使用箇所数", get: i => i.usageCount ?? "", type: "number"},
            {label: "使用箇所", get: i => i.usagePlaces ?? ""},
            {label: "メソッド数", get: i => i.methodCount ?? "", type: "number"},
            {label: "メソッド一覧", get: i => i.methods ?? ""},
        ],
        businessValidation: [
            {label: "パッケージ名", get: i => i.packageName ?? ""},
            {label: "クラス名", get: i => i.typeName ?? ""},
            {label: "クラス別名", get: getTypeLabel},
            {label: "メンバ名", get: i => i.memberName ?? ""},
            {label: "メンバクラス名", get: i => i.memberType ?? ""},
            {label: "アノテーションクラス名", get: i => i.annotationType ?? ""},
            {label: "アノテーション記述", get: i => i.annotationDescription ?? ""},
        ],
        businessSmell: [
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
    };

    // 後方互換: 既存の参照（テスト等）向けに headers のみの形でも公開する
    const headerDefinitions = Object.fromEntries(
        Object.entries(columns).map(([name, cols]) => [name, cols.map(c => c.label)])
    );

    function buildCsv(name, items) {
        const cols = columns[name];
        const rows = items.map(item => cols.map(c => c.get(item)));
        return Jig.dom.buildCsv(headerDefinitions[name], rows);
    }

    function renderTable(tableId, name, items) {
        const cols = columns[name];
        Jig.dom.renderTableRows(tableId, items,
            (row, item) => cols.forEach(c => row.appendChild(Jig.dom.createCell(c.get(item), c.type))),
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

    // 後方互換のための薄いラッパー（テスト・外部参照用）
    const buildControllerCsv = items => buildCsv("controller", items);
    const buildServiceCsv = items => buildCsv("service", items);
    const buildRepositoryCsv = items => buildCsv("repository", items);
    const buildBusinessPackageCsv = items => buildCsv("businessPackage", items);
    const buildBusinessAllCsv = items => buildCsv("businessAll", items);
    const buildBusinessEnumCsv = items => buildCsv("businessEnum", items);
    const buildBusinessCollectionCsv = items => buildCsv("businessCollection", items);
    const buildBusinessValidationCsv = items => buildCsv("businessValidation", items);
    const buildBusinessSmellCsv = items => buildCsv("businessSmell", items);

    const renderControllerTable = items => renderTable("controller-list", "controller", items);
    const renderServiceTable = items => renderTable("service-list", "service", items);
    const renderRepositoryTable = items => renderTable("repository-list", "repository", items);
    const renderBusinessPackageTable = items => renderTable("business-package-list", "businessPackage", items);
    const renderBusinessAllTable = items => renderTable("business-all-list", "businessAll", items);
    const renderBusinessEnumTable = items => renderTable("business-enum-list", "businessEnum", items);
    const renderBusinessCollectionTable = items => renderTable("business-collection-list", "businessCollection", items);
    const renderBusinessValidationTable = items => renderTable("business-validation-list", "businessValidation", items);
    const renderBusinessSmellTable = items => renderTable("business-smell-list", "businessSmell", items);

    function renderTableHeader(tableElementId, headers) {
        const table = document.getElementById(tableElementId);
        if (!table) return;

        const thead = Jig.dom.createElement("thead");
        const tr = Jig.dom.createElement("tr");
        headers.forEach(headerText => {
            const th = Jig.dom.createElement("th");
            th.textContent = headerText;
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

    // 各テーブルの (tableId, columnsキー, データ取得関数) の対応
    const TABLE_BINDINGS = [
        {tableId: "business-package-list", name: "businessPackage", get: d => d.businessRules.packages, csvFile: "list-output-business-package.csv", csvButtonId: "export-business-package-csv"},
        {tableId: "business-all-list", name: "businessAll", get: d => d.businessRules.all, csvFile: "list-output-business-all.csv", csvButtonId: "export-business-all-csv"},
        {tableId: "business-enum-list", name: "businessEnum", get: d => d.businessRules.enums, csvFile: "list-output-business-enum.csv", csvButtonId: "export-business-enum-csv"},
        {tableId: "business-collection-list", name: "businessCollection", get: d => d.businessRules.collections, csvFile: "list-output-business-collection.csv", csvButtonId: "export-business-collection-csv"},
        {tableId: "business-validation-list", name: "businessValidation", get: d => d.businessRules.validations, csvFile: "list-output-business-validation.csv", csvButtonId: "export-business-validation-csv"},
        {tableId: "business-smell-list", name: "businessSmell", get: d => d.businessRules.methodSmells, csvFile: "list-output-business-smell.csv", csvButtonId: "export-business-smell-csv"},
        {tableId: "controller-list", name: "controller", get: d => d.applications.controllers, csvFile: "list-output-controller.csv", csvButtonId: "export-controller-csv"},
        {tableId: "service-list", name: "service", get: d => d.applications.services, csvFile: "list-output-service.csv", csvButtonId: "export-service-csv"},
        {tableId: "repository-list", name: "repository", get: d => d.applications.repositories, csvFile: "list-output-repository.csv", csvButtonId: "export-repository-csv"},
    ];

    function init() {
        TABLE_BINDINGS.forEach(b => renderTableHeader(b.tableId, headerDefinitions[b.name]));

        const data = getListData();
        TABLE_BINDINGS.forEach(b => renderTable(b.tableId, b.name, b.get(data)));

        document.querySelectorAll(".list-output-tabs .tab-button").forEach(button => {
            button.addEventListener("click", () => activateTabGroup(button.dataset.tabGroup, button.dataset.tab));
        });

        TABLE_BINDINGS.forEach(b => {
            document.getElementById(b.csvButtonId)?.addEventListener("click",
                () => Jig.dom.downloadCsv(buildCsv(b.name, b.get(data)), b.csvFile));
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
        buildControllerCsv,
        buildServiceCsv,
        buildRepositoryCsv,
        buildBusinessPackageCsv,
        buildBusinessAllCsv,
        buildBusinessEnumCsv,
        buildBusinessCollectionCsv,
        buildBusinessValidationCsv,
        buildBusinessSmellCsv,
        renderControllerTable,
        renderServiceTable,
        renderRepositoryTable,
        renderBusinessPackageTable,
        renderBusinessAllTable,
        renderBusinessEnumTable,
        renderBusinessCollectionTable,
        renderBusinessValidationTable,
        renderBusinessSmellTable,
        activateTabGroup,
        headerDefinitions,
        renderTableHeader,
    };
})();

Jig.bootstrap.register("list-output", ListOutputApp.init);

if (typeof module !== "undefined" && module.exports) {
    module.exports = ListOutputApp;
}
