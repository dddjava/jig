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
        return listData;
    }
    return listData.controllers ?? [];
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

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", function () {
        if (!document.body.classList.contains("list-output")) return;
        const items = getListData();
        renderControllerTable(items);

        const exportButton = document.getElementById("export-csv");
        if (exportButton) {
            exportButton.addEventListener("click", () => {
                const csvText = buildControllerCsv(items);
                downloadCsv(csvText, "list-output.csv");
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
        renderControllerTable,
    };
}
