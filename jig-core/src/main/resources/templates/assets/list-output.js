const ListOutputApp = (() => {
    const Jig = globalThis.Jig;

    const headerDefinitions = {
        controller: [
            "パッケージ名",
            "クラス名",
            "メソッドシグネチャ",
            "メソッド戻り値の型",
            "クラス別名",
            "使用しているフィールドの型",
            "循環的複雑度",
            "パス",
        ],
        service: [
            "パッケージ名",
            "クラス名",
            "メソッドシグネチャ",
            "メソッド戻り値の型",
            "イベントハンドラ",
            "クラス別名",
            "メソッド別名",
            "メソッド戻り値の型の別名",
            "メソッド引数の型の別名",
            "使用しているフィールドの型",
            "循環的複雑度",
            "使用しているサービスのメソッド",
            "使用しているリポジトリのメソッド",
            "null使用",
            "stream使用",
        ],
        repository: [
            "パッケージ名",
            "クラス名",
            "メソッドシグネチャ",
            "メソッド戻り値の型",
            "クラス別名",
            "メソッド戻り値の型の別名",
            "メソッド引数の型の別名",
            "循環的複雑度",
            "INSERT",
            "SELECT",
            "UPDATE",
            "DELETE",
            "関連元クラス数",
            "関連元メソッド数",
        ],
        businessPackage: ["パッケージ名", "パッケージ別名", "クラス数"],
        businessAll: [
            "パッケージ名",
            "クラス名",
            "クラス別名",
            "ビジネスルールの種類",
            "関連元ビジネスルール数",
            "関連先ビジネスルール数",
            "関連元クラス数",
            "非PUBLIC",
            "同パッケージからのみ参照",
            "関連元クラス",
        ],
        businessEnum: [
            "パッケージ名",
            "クラス名",
            "クラス別名",
            "定数宣言",
            "フィールド",
            "使用箇所数",
            "使用箇所",
            "パラメーター有り",
            "振る舞い有り",
            "多態",
        ],
        businessCollection: [
            "パッケージ名",
            "クラス名",
            "クラス別名",
            "フィールドの型",
            "使用箇所数",
            "使用箇所",
            "メソッド数",
            "メソッド一覧",
        ],
        businessValidation: [
            "パッケージ名",
            "クラス名",
            "クラス別名",
            "メンバ名",
            "メンバクラス名",
            "アノテーションクラス名",
            "アノテーション記述",
        ],
        businessSmell: [
            "パッケージ名",
            "クラス名",
            "メソッドシグネチャ",
            "メソッド戻り値の型",
            "クラス別名",
            "メンバを使用していない",
            "基本型の授受を行なっている",
            "NULLリテラルを使用している",
            "NULL判定をしている",
            "真偽値を返している",
            "voidを返している",
        ],
    };

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
                applications: {
                    ...emptyApplications,
                    controllers: listData,
                },
            };
        }
        if (listData.businessRules || listData.applications) {
            return {
                businessRules: {
                    ...emptyBusinessRules,
                    ...(listData.businessRules ?? {}),
                },
                applications: {
                    ...emptyApplications,
                    ...(listData.applications ?? {}),
                },
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

    function formatFieldTypes(fieldTypes) {
        if (!fieldTypes) return "";
        if (Array.isArray(fieldTypes)) {
            return fieldTypes.join("\n");
        }
        return String(fieldTypes);
    }

    function markIfTrue(value) {
        return value ? "◯" : "";
    }

    function buildCsv(header, rows) {
        return [header, ...rows].map(row => row.map(Jig.dom.escapeCsvValue).join(",")).join("\r\n");
    }

    function renderTableRows(tableId, items, buildCells) {
        const tableBody = document.querySelector(`#${tableId} tbody`);
        if (!tableBody) return;
        tableBody.innerHTML = "";
        const fragment = document.createDocumentFragment();
        items.forEach(item => {
            const row = Jig.dom.createElement("tr");
            buildCells(item).forEach(cell => row.appendChild(cell));
            fragment.appendChild(row);
        });
        tableBody.appendChild(fragment);
    }

    function buildControllerCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            item.typeName ?? "",
            item.methodSignature ?? "",
            item.returnType ?? "",
            getTypeLabel(item),
            formatFieldTypes(item.usingFieldTypes),
            item.cyclomaticComplexity ?? "",
            item.path ?? "",
        ]);
        return buildCsv(headerDefinitions.controller, rows);
    }

    function buildServiceCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            item.typeName ?? "",
            item.methodSignature ?? "",
            item.returnType ?? "",
            markIfTrue(item.eventHandler),
            getTypeLabel(item),
            getMethodLabel(item),
            getReturnTypeLabel(item),
            formatFieldTypes(getParameterTypeLabels(item)),
            formatFieldTypes(item.usingFieldTypes),
            item.cyclomaticComplexity ?? "",
            formatFieldTypes(item.usingServiceMethods),
            formatFieldTypes(item.usingRepositoryMethods),
            markIfTrue(item.useNull),
            markIfTrue(item.useStream),
        ]);
        return buildCsv(headerDefinitions.service, rows);
    }

    function buildRepositoryCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            item.typeName ?? "",
            item.methodSignature ?? "",
            item.returnType ?? "",
            getTypeLabel(item),
            getReturnTypeLabel(item),
            formatFieldTypes(getParameterTypeLabels(item)),
            item.cyclomaticComplexity ?? "",
            formatFieldTypes(item.insertTables),
            formatFieldTypes(item.selectTables),
            formatFieldTypes(item.updateTables),
            formatFieldTypes(item.deleteTables),
            item.callerTypeCount ?? "",
            item.callerMethodCount ?? "",
        ]);
        return buildCsv(headerDefinitions.repository, rows);
    }

    function buildBusinessPackageCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            getPackageLabel(item),
            item.classCount ?? "",
        ]);
        return buildCsv(headerDefinitions.businessPackage, rows);
    }

    function buildBusinessAllCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            item.typeName ?? "",
            getTypeLabel(item),
            item.businessRuleKind ?? "",
            item.incomingBusinessRuleCount ?? "",
            item.outgoingBusinessRuleCount ?? "",
            item.incomingClassCount ?? "",
            markIfTrue(item.nonPublic),
            markIfTrue(item.samePackageOnly),
            item.incomingClassList ?? "",
        ]);
        return buildCsv(headerDefinitions.businessAll, rows);
    }

    function buildBusinessEnumCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            item.typeName ?? "",
            getTypeLabel(item),
            item.constants ?? "",
            item.fields ?? "",
            item.usageCount ?? "",
            item.usagePlaces ?? "",
            markIfTrue(item.hasParameters),
            markIfTrue(item.hasBehavior),
            markIfTrue(item.isPolymorphic),
        ]);
        return buildCsv(headerDefinitions.businessEnum, rows);
    }

    function buildBusinessCollectionCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            item.typeName ?? "",
            getTypeLabel(item),
            item.fieldTypes ?? "",
            item.usageCount ?? "",
            item.usagePlaces ?? "",
            item.methodCount ?? "",
            item.methods ?? "",
        ]);
        return buildCsv(headerDefinitions.businessCollection, rows);
    }

    function buildBusinessValidationCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            item.typeName ?? "",
            getTypeLabel(item),
            item.memberName ?? "",
            item.memberType ?? "",
            item.annotationType ?? "",
            item.annotationDescription ?? "",
        ]);
        return buildCsv(headerDefinitions.businessValidation, rows);
    }

    function buildBusinessSmellCsv(items) {
        const rows = items.map(item => [
            item.packageName ?? "",
            item.typeName ?? "",
            item.methodSignature ?? "",
            item.returnType ?? "",
            getTypeLabel(item),
            markIfTrue(item.notUseMember),
            markIfTrue(item.primitiveInterface),
            markIfTrue(item.referenceNull),
            markIfTrue(item.nullDecision),
            markIfTrue(item.returnsBoolean),
            markIfTrue(item.returnsVoid),
        ]);
        return buildCsv(headerDefinitions.businessSmell, rows);
    }

    function renderControllerTable(items) {
        renderTableRows("controller-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(item.typeName),
            Jig.dom.createCell(item.methodSignature),
            Jig.dom.createCell(item.returnType),
            Jig.dom.createCell(getTypeLabel(item)),
            Jig.dom.createCell(formatFieldTypes(item.usingFieldTypes)),
            Jig.dom.createCell(item.cyclomaticComplexity, "number"),
            Jig.dom.createCell(item.path),
        ]);
    }

    function renderServiceTable(items) {
        renderTableRows("service-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(item.typeName),
            Jig.dom.createCell(item.methodSignature),
            Jig.dom.createCell(item.returnType),
            Jig.dom.createCell(markIfTrue(item.eventHandler)),
            Jig.dom.createCell(getTypeLabel(item)),
            Jig.dom.createCell(getMethodLabel(item)),
            Jig.dom.createCell(getReturnTypeLabel(item)),
            Jig.dom.createCell(formatFieldTypes(getParameterTypeLabels(item))),
            Jig.dom.createCell(formatFieldTypes(item.usingFieldTypes)),
            Jig.dom.createCell(item.cyclomaticComplexity, "number"),
            Jig.dom.createCell(formatFieldTypes(item.usingServiceMethods)),
            Jig.dom.createCell(formatFieldTypes(item.usingRepositoryMethods)),
            Jig.dom.createCell(markIfTrue(item.useNull)),
            Jig.dom.createCell(markIfTrue(item.useStream)),
        ]);
    }

    function renderRepositoryTable(items) {
        renderTableRows("repository-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(item.typeName),
            Jig.dom.createCell(item.methodSignature),
            Jig.dom.createCell(item.returnType),
            Jig.dom.createCell(getTypeLabel(item)),
            Jig.dom.createCell(getReturnTypeLabel(item)),
            Jig.dom.createCell(formatFieldTypes(getParameterTypeLabels(item))),
            Jig.dom.createCell(item.cyclomaticComplexity, "number"),
            Jig.dom.createCell(formatFieldTypes(item.insertTables)),
            Jig.dom.createCell(formatFieldTypes(item.selectTables)),
            Jig.dom.createCell(formatFieldTypes(item.updateTables)),
            Jig.dom.createCell(formatFieldTypes(item.deleteTables)),
            Jig.dom.createCell(item.callerTypeCount, "number"),
            Jig.dom.createCell(item.callerMethodCount, "number"),
        ]);
    }

    function renderBusinessPackageTable(items) {
        renderTableRows("business-package-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(getPackageLabel(item)),
            Jig.dom.createCell(item.classCount, "number"),
        ]);
    }

    function renderBusinessAllTable(items) {
        renderTableRows("business-all-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(item.typeName),
            Jig.dom.createCell(getTypeLabel(item)),
            Jig.dom.createCell(item.businessRuleKind),
            Jig.dom.createCell(item.incomingBusinessRuleCount, "number"),
            Jig.dom.createCell(item.outgoingBusinessRuleCount, "number"),
            Jig.dom.createCell(item.incomingClassCount, "number"),
            Jig.dom.createCell(markIfTrue(item.nonPublic)),
            Jig.dom.createCell(markIfTrue(item.samePackageOnly)),
            Jig.dom.createCell(item.incomingClassList),
        ]);
    }

    function renderBusinessEnumTable(items) {
        renderTableRows("business-enum-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(item.typeName),
            Jig.dom.createCell(getTypeLabel(item)),
            Jig.dom.createCell(item.constants),
            Jig.dom.createCell(item.fields),
            Jig.dom.createCell(item.usageCount, "number"),
            Jig.dom.createCell(item.usagePlaces),
            Jig.dom.createCell(markIfTrue(item.hasParameters)),
            Jig.dom.createCell(markIfTrue(item.hasBehavior)),
            Jig.dom.createCell(markIfTrue(item.isPolymorphic)),
        ]);
    }

    function renderBusinessCollectionTable(items) {
        renderTableRows("business-collection-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(item.typeName),
            Jig.dom.createCell(getTypeLabel(item)),
            Jig.dom.createCell(item.fieldTypes),
            Jig.dom.createCell(item.usageCount, "number"),
            Jig.dom.createCell(item.usagePlaces),
            Jig.dom.createCell(item.methodCount, "number"),
            Jig.dom.createCell(item.methods),
        ]);
    }

    function renderBusinessValidationTable(items) {
        renderTableRows("business-validation-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(item.typeName),
            Jig.dom.createCell(getTypeLabel(item)),
            Jig.dom.createCell(item.memberName),
            Jig.dom.createCell(item.memberType),
            Jig.dom.createCell(item.annotationType),
            Jig.dom.createCell(item.annotationDescription),
        ]);
    }

    function renderBusinessSmellTable(items) {
        renderTableRows("business-smell-list", items, item => [
            Jig.dom.createCell(item.packageName),
            Jig.dom.createCell(item.typeName),
            Jig.dom.createCell(item.methodSignature),
            Jig.dom.createCell(item.returnType),
            Jig.dom.createCell(getTypeLabel(item)),
            Jig.dom.createCell(markIfTrue(item.notUseMember)),
            Jig.dom.createCell(markIfTrue(item.primitiveInterface)),
            Jig.dom.createCell(markIfTrue(item.referenceNull)),
            Jig.dom.createCell(markIfTrue(item.nullDecision)),
            Jig.dom.createCell(markIfTrue(item.returnsBoolean)),
            Jig.dom.createCell(markIfTrue(item.returnsVoid)),
        ]);
    }

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
        table.prepend(thead); // prepend so it's the first child
    }

    function activateTabGroup(group, tabName) {
        const tabs = document.querySelectorAll(`.list-output-tab[data-tab-group="${group}"]`);
        const buttons = document.querySelectorAll(`.tab-button[data-tab-group="${group}"]`);
        tabs.forEach(tab => {
            const isActive = tab.dataset.tab === tabName;
            tab.classList.toggle("is-active", isActive);
        });
        buttons.forEach(button => {
            const isActive = button.dataset.tab === tabName;
            button.classList.toggle("is-active", isActive);
            button.setAttribute("aria-selected", String(isActive));
        });
    }

    function init() {
        if (typeof document === "undefined") return;
        if (!document.body.classList.contains("list-output")) return;

        renderTableHeader("business-package-list", headerDefinitions.businessPackage);
        renderTableHeader("business-all-list", headerDefinitions.businessAll);
        renderTableHeader("business-enum-list", headerDefinitions.businessEnum);
        renderTableHeader("business-collection-list", headerDefinitions.businessCollection);
        renderTableHeader("business-validation-list", headerDefinitions.businessValidation);
        renderTableHeader("business-smell-list", headerDefinitions.businessSmell);
        renderTableHeader("controller-list", headerDefinitions.controller);
        renderTableHeader("service-list", headerDefinitions.service);
        renderTableHeader("repository-list", headerDefinitions.repository);

        const data = getListData();
        renderBusinessPackageTable(data.businessRules.packages);
        renderBusinessAllTable(data.businessRules.all);
        renderBusinessEnumTable(data.businessRules.enums);
        renderBusinessCollectionTable(data.businessRules.collections);
        renderBusinessValidationTable(data.businessRules.validations);
        renderBusinessSmellTable(data.businessRules.methodSmells);
        renderControllerTable(data.applications.controllers);
        renderServiceTable(data.applications.services);
        renderRepositoryTable(data.applications.repositories);

        const tabButtons = document.querySelectorAll(".list-output-tabs .tab-button");
        tabButtons.forEach(button => {
            button.addEventListener("click", () => activateTabGroup(button.dataset.tabGroup, button.dataset.tab));
        });

        const businessPackageExportButton = document.getElementById("export-business-package-csv");
        if (businessPackageExportButton) {
            businessPackageExportButton.addEventListener("click", () => {
                const csvText = buildBusinessPackageCsv(data.businessRules.packages);
                Jig.dom.downloadCsv(csvText, "list-output-business-package.csv");
            });
        }

        const businessAllExportButton = document.getElementById("export-business-all-csv");
        if (businessAllExportButton) {
            businessAllExportButton.addEventListener("click", () => {
                const csvText = buildBusinessAllCsv(data.businessRules.all);
                Jig.dom.downloadCsv(csvText, "list-output-business-all.csv");
            });
        }

        const businessEnumExportButton = document.getElementById("export-business-enum-csv");
        if (businessEnumExportButton) {
            businessEnumExportButton.addEventListener("click", () => {
                const csvText = buildBusinessEnumCsv(data.businessRules.enums);
                Jig.dom.downloadCsv(csvText, "list-output-business-enum.csv");
            });
        }

        const businessCollectionExportButton = document.getElementById("export-business-collection-csv");
        if (businessCollectionExportButton) {
            businessCollectionExportButton.addEventListener("click", () => {
                const csvText = buildBusinessCollectionCsv(data.businessRules.collections);
                Jig.dom.downloadCsv(csvText, "list-output-business-collection.csv");
            });
        }

        const businessValidationExportButton = document.getElementById("export-business-validation-csv");
        if (businessValidationExportButton) {
            businessValidationExportButton.addEventListener("click", () => {
                const csvText = buildBusinessValidationCsv(data.businessRules.validations);
                Jig.dom.downloadCsv(csvText, "list-output-business-validation.csv");
            });
        }

        const businessSmellExportButton = document.getElementById("export-business-smell-csv");
        if (businessSmellExportButton) {
            businessSmellExportButton.addEventListener("click", () => {
                const csvText = buildBusinessSmellCsv(data.businessRules.methodSmells);
                Jig.dom.downloadCsv(csvText, "list-output-business-smell.csv");
            });
        }

        const controllerExportButton = document.getElementById("export-controller-csv");
        if (controllerExportButton) {
            controllerExportButton.addEventListener("click", () => {
                const csvText = buildControllerCsv(data.applications.controllers);
                Jig.dom.downloadCsv(csvText, "list-output-controller.csv");
            });
        }

        const serviceExportButton = document.getElementById("export-service-csv");
        if (serviceExportButton) {
            serviceExportButton.addEventListener("click", () => {
                const csvText = buildServiceCsv(data.applications.services);
                Jig.dom.downloadCsv(csvText, "list-output-service.csv");
            });
        }

        const repositoryExportButton = document.getElementById("export-repository-csv");
        if (repositoryExportButton) {
            repositoryExportButton.addEventListener("click", () => {
                const csvText = buildRepositoryCsv(data.applications.repositories);
                Jig.dom.downloadCsv(csvText, "list-output-repository.csv");
            });
        }
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

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        ListOutputApp.init()
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = ListOutputApp;
}
