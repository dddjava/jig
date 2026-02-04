function getListData() {
    const jsonText = document.getElementById("list-data")?.textContent || "{}";
    /** @type {{
     * businessRules?: {
     * packages?: Array<{packageName: string, packageLabel: string, classCount: number}>,
     * all?: Array<{packageName: string, typeName: string, typeLabel: string, businessRuleKind: string, incomingBusinessRuleCount: number, outgoingBusinessRuleCount: number, incomingClassCount: number, nonPublic: boolean, samePackageOnly: boolean, incomingClassList: string}>,
     * enums?: Array<{packageName: string, typeName: string, typeLabel: string, constants: string, fields: string, usageCount: number, usagePlaces: string, hasParameters: boolean, hasBehavior: boolean, isPolymorphic: boolean}>,
     * collections?: Array<{packageName: string, typeName: string, typeLabel: string, fieldTypes: string, usageCount: number, usagePlaces: string, methodCount: number, methods: string}>,
     * validations?: Array<{packageName: string, typeName: string, typeLabel: string, memberName: string, memberType: string, annotationType: string, annotationDescription: string}>,
     * methodSmells?: Array<{packageName: string, typeName: string, methodSignature: string, returnType: string, typeLabel: string, notUseMember: boolean, primitiveInterface: boolean, referenceNull: boolean, nullDecision: boolean, returnsBoolean: boolean, returnsVoid: boolean}>
     * },
     * applications?: {
     * controllers?: Array<{packageName: string, typeName: string, methodSignature: string, returnType: string, typeLabel: string, usingFieldTypes: string[], cyclomaticComplexity: number, path: string}>,
     * services?: Array<{packageName: string, typeName: string, methodSignature: string, returnType: string, eventHandler: boolean, typeLabel: string, methodLabel: string, returnTypeLabel: string, parameterTypeLabels: string[], usingFieldTypes: string[], cyclomaticComplexity: number, usingServiceMethods: string[], usingRepositoryMethods: string[], useNull: boolean, useStream: boolean}>,
     * repositories?: Array<{packageName: string, typeName: string, methodSignature: string, returnType: string, typeLabel: string, returnTypeLabel: string, parameterTypeLabels: string[], cyclomaticComplexity: number, insertTables: string[], selectTables: string[], updateTables: string[], deleteTables: string[], callerTypeCount: number, callerMethodCount: number}>
     * }
     * } | Array<{packageName: string, typeName: string, methodSignature: string, returnType: string, typeLabel: string, usingFieldTypes: string[], cyclomaticComplexity: number, path: string}> */
    const listData = JSON.parse(jsonText);
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

function escapeCsvValue(value) {
    const text = String(value ?? "")
        .replace(/\r\n/g, "\n")
        .replace(/\r/g, "\n");
    return `"${text.replace(/"/g, "\"\"")}"`;
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

function buildControllerCsv(items) {
    const header = headerDefinitions.controller;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.methodSignature ?? "",
        item.returnType ?? "",
        item.typeLabel ?? "",
        formatFieldTypes(item.usingFieldTypes),
        item.cyclomaticComplexity ?? "",
        item.path ?? "",
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function buildServiceCsv(items) {
    const header = headerDefinitions.service;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.methodSignature ?? "",
        item.returnType ?? "",
        markIfTrue(item.eventHandler),
        item.typeLabel ?? "",
        item.methodLabel ?? "",
        item.returnTypeLabel ?? "",
        formatFieldTypes(item.parameterTypeLabels),
        formatFieldTypes(item.usingFieldTypes),
        item.cyclomaticComplexity ?? "",
        formatFieldTypes(item.usingServiceMethods),
        formatFieldTypes(item.usingRepositoryMethods),
        markIfTrue(item.useNull),
        markIfTrue(item.useStream),
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function buildRepositoryCsv(items) {
    const header = headerDefinitions.repository;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.methodSignature ?? "",
        item.returnType ?? "",
        item.typeLabel ?? "",
        item.returnTypeLabel ?? "",
        formatFieldTypes(item.parameterTypeLabels),
        item.cyclomaticComplexity ?? "",
        formatFieldTypes(item.insertTables),
        formatFieldTypes(item.selectTables),
        formatFieldTypes(item.updateTables),
        formatFieldTypes(item.deleteTables),
        item.callerTypeCount ?? "",
        item.callerMethodCount ?? "",
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function buildBusinessPackageCsv(items) {
    const header = headerDefinitions.businessPackage;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.packageLabel ?? "",
        item.classCount ?? "",
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function buildBusinessAllCsv(items) {
    const header = headerDefinitions.businessAll;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.typeLabel ?? "",
        item.businessRuleKind ?? "",
        item.incomingBusinessRuleCount ?? "",
        item.outgoingBusinessRuleCount ?? "",
        item.incomingClassCount ?? "",
        markIfTrue(item.nonPublic),
        markIfTrue(item.samePackageOnly),
        item.incomingClassList ?? "",
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function buildBusinessEnumCsv(items) {
    const header = headerDefinitions.businessEnum;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.typeLabel ?? "",
        item.constants ?? "",
        item.fields ?? "",
        item.usageCount ?? "",
        item.usagePlaces ?? "",
        markIfTrue(item.hasParameters),
        markIfTrue(item.hasBehavior),
        markIfTrue(item.isPolymorphic),
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function buildBusinessCollectionCsv(items) {
    const header = headerDefinitions.businessCollection;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.typeLabel ?? "",
        item.fieldTypes ?? "",
        item.usageCount ?? "",
        item.usagePlaces ?? "",
        item.methodCount ?? "",
        item.methods ?? "",
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function buildBusinessValidationCsv(items) {
    const header = headerDefinitions.businessValidation;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.typeLabel ?? "",
        item.memberName ?? "",
        item.memberType ?? "",
        item.annotationType ?? "",
        item.annotationDescription ?? "",
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function buildBusinessSmellCsv(items) {
    const header = headerDefinitions.businessSmell;
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.methodSignature ?? "",
        item.returnType ?? "",
        item.typeLabel ?? "",
        markIfTrue(item.notUseMember),
        markIfTrue(item.primitiveInterface),
        markIfTrue(item.referenceNull),
        markIfTrue(item.nullDecision),
        markIfTrue(item.returnsBoolean),
        markIfTrue(item.returnsVoid),
    ]);
    const lines = [header, ...rows].map(row => row.map(escapeCsvValue).join(","));
    return lines.join("\r\n");
}

function downloadCsv(text, filename) {
    const blob = new Blob([text], {type: "text/csv;charset=utf-8;"});
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = filename;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    URL.revokeObjectURL(url);
}

function renderControllerTable(items) {
    const tableBody = document.querySelector("#controller-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.typeName,
            item.methodSignature,
            item.returnType,
            item.typeLabel,
            formatFieldTypes(item.usingFieldTypes),
            item.cyclomaticComplexity,
            item.path,
        ];
        values.forEach((value, index) => {
            const cell = document.createElement("td");
            if (index === 6) {
                cell.className = "number";
            }
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderServiceTable(items) {
    const tableBody = document.querySelector("#service-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.typeName,
            item.methodSignature,
            item.returnType,
            markIfTrue(item.eventHandler),
            item.typeLabel,
            item.methodLabel,
            item.returnTypeLabel,
            formatFieldTypes(item.parameterTypeLabels),
            formatFieldTypes(item.usingFieldTypes),
            item.cyclomaticComplexity,
            formatFieldTypes(item.usingServiceMethods),
            formatFieldTypes(item.usingRepositoryMethods),
            markIfTrue(item.useNull),
            markIfTrue(item.useStream),
        ];
        values.forEach((value, index) => {
            const cell = document.createElement("td");
            if (index === 10) {
                cell.className = "number";
            }
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderRepositoryTable(items) {
    const tableBody = document.querySelector("#repository-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.typeName,
            item.methodSignature,
            item.returnType,
            item.typeLabel,
            item.returnTypeLabel,
            formatFieldTypes(item.parameterTypeLabels),
            item.cyclomaticComplexity,
            formatFieldTypes(item.insertTables),
            formatFieldTypes(item.selectTables),
            formatFieldTypes(item.updateTables),
            formatFieldTypes(item.deleteTables),
            item.callerTypeCount,
            item.callerMethodCount,
        ];
        values.forEach((value, index) => {
            const cell = document.createElement("td");
            if ([7, 12, 13].includes(index)) {
                cell.className = "number";
            }
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderBusinessPackageTable(items) {
    const tableBody = document.querySelector("#business-package-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.packageLabel,
            item.classCount,
        ];
        values.forEach((value, index) => {
            const cell = document.createElement("td");
            if (index === 2) {
                cell.className = "number";
            }
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderBusinessAllTable(items) {
    const tableBody = document.querySelector("#business-all-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.typeName,
            item.typeLabel,
            item.businessRuleKind,
            item.incomingBusinessRuleCount,
            item.outgoingBusinessRuleCount,
            item.incomingClassCount,
            markIfTrue(item.nonPublic),
            markIfTrue(item.samePackageOnly),
            item.incomingClassList,
        ];
        values.forEach((value, index) => {
            const cell = document.createElement("td");
            if ([4, 5, 6].includes(index)) {
                cell.className = "number";
            }
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderBusinessEnumTable(items) {
    const tableBody = document.querySelector("#business-enum-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.typeName,
            item.typeLabel,
            item.constants,
            item.fields,
            item.usageCount,
            item.usagePlaces,
            markIfTrue(item.hasParameters),
            markIfTrue(item.hasBehavior),
            markIfTrue(item.isPolymorphic),
        ];
        values.forEach((value, index) => {
            const cell = document.createElement("td");
            if (index === 5) {
                cell.className = "number";
            }
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderBusinessCollectionTable(items) {
    const tableBody = document.querySelector("#business-collection-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.typeName,
            item.typeLabel,
            item.fieldTypes,
            item.usageCount,
            item.usagePlaces,
            item.methodCount,
            item.methods,
        ];
        values.forEach((value, index) => {
            const cell = document.createElement("td");
            if ([4, 6].includes(index)) {
                cell.className = "number";
            }
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderBusinessValidationTable(items) {
    const tableBody = document.querySelector("#business-validation-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.typeName,
            item.typeLabel,
            item.memberName,
            item.memberType,
            item.annotationType,
            item.annotationDescription,
        ];
        values.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderBusinessSmellTable(items) {
    const tableBody = document.querySelector("#business-smell-list tbody");
    if (!tableBody) return;
    tableBody.innerHTML = "";

    const fragment = document.createDocumentFragment();
    items.forEach(item => {
        const row = document.createElement("tr");
        const values = [
            item.packageName,
            item.typeName,
            item.methodSignature,
            item.returnType,
            item.typeLabel,
            markIfTrue(item.notUseMember),
            markIfTrue(item.primitiveInterface),
            markIfTrue(item.referenceNull),
            markIfTrue(item.nullDecision),
            markIfTrue(item.returnsBoolean),
            markIfTrue(item.returnsVoid),
        ];
        values.forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = value ?? "";
            row.appendChild(cell);
        });
        fragment.appendChild(row);
    });

    tableBody.appendChild(fragment);
}

function renderTableHeader(tableElementId, headers) {
    const table = document.getElementById(tableElementId);
    if (!table) return;

    const thead = document.createElement("thead");
    const tr = document.createElement("tr");

    headers.forEach(headerText => {
        const th = document.createElement("th");
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

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", function () {
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
                downloadCsv(csvText, "list-output-business-package.csv");
            });
        }

        const businessAllExportButton = document.getElementById("export-business-all-csv");
        if (businessAllExportButton) {
            businessAllExportButton.addEventListener("click", () => {
                const csvText = buildBusinessAllCsv(data.businessRules.all);
                downloadCsv(csvText, "list-output-business-all.csv");
            });
        }

        const businessEnumExportButton = document.getElementById("export-business-enum-csv");
        if (businessEnumExportButton) {
            businessEnumExportButton.addEventListener("click", () => {
                const csvText = buildBusinessEnumCsv(data.businessRules.enums);
                downloadCsv(csvText, "list-output-business-enum.csv");
            });
        }

        const businessCollectionExportButton = document.getElementById("export-business-collection-csv");
        if (businessCollectionExportButton) {
            businessCollectionExportButton.addEventListener("click", () => {
                const csvText = buildBusinessCollectionCsv(data.businessRules.collections);
                downloadCsv(csvText, "list-output-business-collection.csv");
            });
        }

        const businessValidationExportButton = document.getElementById("export-business-validation-csv");
        if (businessValidationExportButton) {
            businessValidationExportButton.addEventListener("click", () => {
                const csvText = buildBusinessValidationCsv(data.businessRules.validations);
                downloadCsv(csvText, "list-output-business-validation.csv");
            });
        }

        const businessSmellExportButton = document.getElementById("export-business-smell-csv");
        if (businessSmellExportButton) {
            businessSmellExportButton.addEventListener("click", () => {
                const csvText = buildBusinessSmellCsv(data.businessRules.methodSmells);
                downloadCsv(csvText, "list-output-business-smell.csv");
            });
        }

        const controllerExportButton = document.getElementById("export-controller-csv");
        if (controllerExportButton) {
            controllerExportButton.addEventListener("click", () => {
                const csvText = buildControllerCsv(data.applications.controllers);
                downloadCsv(csvText, "list-output-controller.csv");
            });
        }

        const serviceExportButton = document.getElementById("export-service-csv");
        if (serviceExportButton) {
            serviceExportButton.addEventListener("click", () => {
                const csvText = buildServiceCsv(data.applications.services);
                downloadCsv(csvText, "list-output-service.csv");
            });
        }

        const repositoryExportButton = document.getElementById("export-repository-csv");
        if (repositoryExportButton) {
            repositoryExportButton.addEventListener("click", () => {
                const csvText = buildRepositoryCsv(data.applications.repositories);
                downloadCsv(csvText, "list-output-repository.csv");
            });
        }
    });
}

// Nodeのテスト用エクスポート。ブラウザでは無視される。
if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        getListData,
        escapeCsvValue,
        formatFieldTypes,
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
}
