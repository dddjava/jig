function getListData() {
    const jsonText = document.getElementById("list-data")?.textContent || "{}";
    /** @type {{controllers?: Array<{
     * packageName: string,
     * typeName: string,
     * methodSignature: string,
     * returnType: string,
     * typeLabel: string,
     * usingFieldTypes: string[],
     * cyclomaticComplexity: number,
     * path: string
     * }>, services?: Array<{
     * packageName: string,
     * typeName: string,
     * methodSignature: string,
     * returnType: string,
     * eventHandler: boolean,
     * typeLabel: string,
     * methodLabel: string,
     * returnTypeLabel: string,
     * parameterTypeLabels: string[],
     * usingFieldTypes: string[],
     * cyclomaticComplexity: number,
     * usingServiceMethods: string[],
     * usingRepositoryMethods: string[],
     * useNull: boolean,
     * useStream: boolean
     * }>, repositories?: Array<{
     * packageName: string,
     * typeName: string,
     * methodSignature: string,
     * returnType: string,
     * typeLabel: string,
     * returnTypeLabel: string,
     * parameterTypeLabels: string[],
     * cyclomaticComplexity: number,
     * insertTables: string,
     * selectTables: string,
     * updateTables: string,
     * deleteTables: string,
     * callerTypeCount: number,
     * callerMethodCount: number
     * }>} | Array<{
     * packageName: string,
     * typeName: string,
     * methodSignature: string,
     * returnType: string,
     * typeLabel: string,
     * usingFieldTypes: string[],
     * cyclomaticComplexity: number,
     * path: string
     * }>} */
    const listData = JSON.parse(jsonText);
    if (Array.isArray(listData)) {
        return {
            controllers: listData,
            services: [],
            repositories: [],
        };
    }
    return {
        controllers: listData.controllers ?? [],
        services: listData.services ?? [],
        repositories: listData.repositories ?? [],
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

function buildControllerCsv(items) {
    const header = [
        "パッケージ名",
        "クラス名",
        "メソッドシグネチャ",
        "メソッド戻り値の型",
        "クラス別名",
        "使用しているフィールドの型",
        "循環的複雑度",
        "パス",
    ];
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
    const header = [
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
    ];
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
    const header = [
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
    ];
    const rows = items.map(item => [
        item.packageName ?? "",
        item.typeName ?? "",
        item.methodSignature ?? "",
        item.returnType ?? "",
        item.typeLabel ?? "",
        item.returnTypeLabel ?? "",
        formatFieldTypes(item.parameterTypeLabels),
        item.cyclomaticComplexity ?? "",
        item.insertTables ?? "",
        item.selectTables ?? "",
        item.updateTables ?? "",
        item.deleteTables ?? "",
        item.callerTypeCount ?? "",
        item.callerMethodCount ?? "",
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
            item.insertTables,
            item.selectTables,
            item.updateTables,
            item.deleteTables,
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

function activateTab(tabName) {
    const tabs = document.querySelectorAll(".list-output-tab");
    const buttons = document.querySelectorAll(".list-output-tabs .tab-button");
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
        const data = getListData();
        renderControllerTable(data.controllers);
        renderServiceTable(data.services);
        renderRepositoryTable(data.repositories);

        const tabButtons = document.querySelectorAll(".list-output-tabs .tab-button");
        tabButtons.forEach(button => {
            button.addEventListener("click", () => activateTab(button.dataset.tab));
        });

        const controllerExportButton = document.getElementById("export-controller-csv");
        if (controllerExportButton) {
            controllerExportButton.addEventListener("click", () => {
                const csvText = buildControllerCsv(data.controllers);
                downloadCsv(csvText, "list-output-controller.csv");
            });
        }

        const serviceExportButton = document.getElementById("export-service-csv");
        if (serviceExportButton) {
            serviceExportButton.addEventListener("click", () => {
                const csvText = buildServiceCsv(data.services);
                downloadCsv(csvText, "list-output-service.csv");
            });
        }

        const repositoryExportButton = document.getElementById("export-repository-csv");
        if (repositoryExportButton) {
            repositoryExportButton.addEventListener("click", () => {
                const csvText = buildRepositoryCsv(data.repositories);
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
        renderControllerTable,
        renderServiceTable,
        renderRepositoryTable,
    };
}
