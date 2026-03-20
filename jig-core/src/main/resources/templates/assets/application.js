// ===== アプリケーション本体 =====

const ApplicationApp = {
    state: {
        data: null
    },

    init() {
        this.state.data = globalThis.applicationData;
        if (!this.state.data) return;
        this.render();
    },

    render() {
        const { packages, types } = this.state.data;
        this.renderList(packages, types);
    },

    renderList(packages, types) {
        const container = document.getElementById("application-list");
        if (!container) return;
        container.innerHTML = "";

        if ((!packages || packages.length === 0) && (!types || types.length === 0)) {
            container.textContent = "データなし";
            return;
        }

        packages.forEach(pkg => {
            container.appendChild(this.createPackageSection(pkg));
        });

        types.forEach(type => {
            container.appendChild(this.createTypeSection(type));
        });
    },

    createPackageSection(pkg) {
        const section = document.createElement("section");
        section.className = "package";

        const h2 = document.createElement("h2");
        const anchor = document.createElement("a");
        anchor.id = pkg.fqn;
        anchor.textContent = pkg.label;
        h2.appendChild(anchor);
        section.appendChild(h2);

        const small = document.createElement("small");
        small.className = "fully-qualified-name";
        small.textContent = pkg.fqn;
        section.appendChild(small);

        if (pkg.description) {
            const desc = document.createElement("section");
            desc.className = "markdown";
            desc.innerHTML = marked.parse(pkg.description);
            section.appendChild(desc);
        }

        if (pkg.children && pkg.children.length > 0) {
            const childSection = document.createElement("section");
            const table = document.createElement("table");
            const thead = document.createElement("thead");
            thead.innerHTML = "<tr><th>名前</th></tr>";
            table.appendChild(thead);
            const tbody = document.createElement("tbody");
            pkg.children.forEach(child => {
                const tr = document.createElement("tr");
                const td = document.createElement("td");
                if (child.isPackage) {
                    td.appendChild(document.createTextNode("▶︎ "));
                }
                const a = document.createElement("a");
                a.href = child.href;
                a.textContent = child.name;
                td.appendChild(a);
                tr.appendChild(td);
                tbody.appendChild(tr);
            });
            table.appendChild(tbody);
            childSection.appendChild(table);
            section.appendChild(childSection);
        }

        return section;
    },

    createTypeSection(type) {
        const section = document.createElement("section");
        section.className = "type";

        const h2 = document.createElement("h2");
        const anchor = document.createElement("a");
        anchor.id = type.fqn;
        anchor.textContent = type.label;
        if (type.isDeprecated) anchor.className = "deprecated";
        h2.appendChild(anchor);
        section.appendChild(h2);

        const fqnDiv = document.createElement("div");
        fqnDiv.className = "fully-qualified-name";
        fqnDiv.textContent = type.fqn;
        section.appendChild(fqnDiv);

        if (type.description) {
            const desc = document.createElement("section");
            desc.className = "markdown";
            desc.innerHTML = marked.parse(type.description);
            section.appendChild(desc);
        }

        if (type.fields && type.fields.length > 0) {
            section.appendChild(createFieldsTable(type.fields));
        }

        if (type.instanceMethods && type.instanceMethods.length > 0) {
            section.appendChild(createMethodsTable("メソッド", type.instanceMethods));
        }

        if (type.staticMethods && type.staticMethods.length > 0) {
            section.appendChild(createMethodsTable("staticメソッド", type.staticMethods));
        }

        return section;
    }
};

function createFieldsTable(fields) {
    const table = document.createElement("table");
    table.className = "fields";
    table.innerHTML = '<thead><tr><th width="20%">フィールド</th><th>フィールド型</th></tr></thead>';
    const tbody = document.createElement("tbody");
    fields.forEach(field => {
        const tr = document.createElement("tr");
        const nameTd = document.createElement("td");
        nameTd.textContent = field.name;
        if (field.isDeprecated) nameTd.className = "deprecated";
        const typeTd = document.createElement("td");
        typeTd.innerHTML = field.typeHtml;
        tr.appendChild(nameTd);
        tr.appendChild(typeTd);
        tbody.appendChild(tr);
    });
    table.appendChild(tbody);
    return table;
}

function createMethodsTable(kind, methods) {
    const table = document.createElement("table");
    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");
    [kind, "引数", "戻り値型", "説明"].forEach((text, i) => {
        const th = document.createElement("th");
        if (i === 0) th.setAttribute("width", "20%");
        th.textContent = text;
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");
    methods.forEach(method => {
        const tr = document.createElement("tr");

        const nameTd = document.createElement("td");
        nameTd.className = "method-name";
        nameTd.textContent = method.labelWithSymbol;
        tr.appendChild(nameTd);

        const paramsTd = document.createElement("td");
        (method.paramsHtml || []).forEach(paramHtml => {
            const span = document.createElement("span");
            span.className = "method-argument-item";
            span.innerHTML = paramHtml;
            paramsTd.appendChild(span);
        });
        tr.appendChild(paramsTd);

        const returnTd = document.createElement("td");
        returnTd.innerHTML = method.returnTypeHtml;
        tr.appendChild(returnTd);

        const descTd = document.createElement("td");
        descTd.className = "markdown";
        descTd.innerHTML = method.description ? marked.parse(method.description) : "";
        tr.appendChild(descTd);

        tbody.appendChild(tr);
    });
    table.appendChild(tbody);
    return table;
}

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => {
        ApplicationApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = { ApplicationApp };
}
