function createElement(tagName, options = {}) {
    const element = document.createElement(tagName);
    if (options.className) element.className = options.className;
    if (options.id) element.id = options.id;
    if (options.textContent != null) element.textContent = options.textContent;
    if (options.innerHTML != null) element.innerHTML = options.innerHTML;
    if (options.attributes) {
        for (const [key, value] of Object.entries(options.attributes)) {
            element.setAttribute(key, value);
        }
    }
    if (options.children) {
        options.children.forEach(child => {
            if (child) element.appendChild(child);
        });
    }
    return element;
}

function renderTreeNode(node) {
    if (!node) return null;

    if (node.kind === "package") {
        const summaryLink = createElement("a", {
            attributes: {href: node.href || "#"},
            textContent: node.name || ""
        });
        const details = createElement("details", {
            attributes: node.open ? {open: ""} : {open: ""}, // 互換性のため常にopen
            children: [
                createElement("summary", {
                    className: "package",
                    children: [summaryLink, document.createTextNode("/")]
                })
            ]
        });

        (node.children || []).forEach(child => {
            const rendered = renderTreeNode(child);
            if (rendered) details.appendChild(rendered);
        });
        return details;
    }

    const linkClass = node.isDeprecated ? "deprecated" : "";
    const link = createElement("a", {
        className: linkClass,
        attributes: {href: node.href || "#"},
        textContent: node.name || ""
    });
    return createElement("div", {children: [link]});
}

function renderSidebar(treeRoot) {
    const container = document.getElementById("domain-sidebar-list");
    if (!container) return;
    container.innerHTML = "";
    const rendered = renderTreeNode(treeRoot);
    if (rendered) container.appendChild(rendered);
}

function createChildrenTable(children) {
    if (!children || children.length === 0) return null;

    const tbody = createElement("tbody", {
        children: children.map(child => {
            const prefix = child.kind === "package" ? "▶︎ " : "";
            const link = createElement("a", {
                attributes: {href: child.href || "#"},
                textContent: child.name || ""
            });
            const cell = createElement("td", {
                children: [document.createTextNode(prefix), link]
            });
            return createElement("tr", {children: [cell]});
        })
    });

    return createElement("table", {
        children: [
            createElement("thead", {
                children: [createElement("tr", {children: [createElement("th", {textContent: "名前"})]})]
            }),
            tbody
        ]
    });
}

function createFieldsTable(fields) {
    if (!fields || fields.length === 0) return null;

    const tbody = createElement("tbody", {
        children: fields.map(field => createElement("tr", {
            children: [
                createElement("td", {
                    className: field.isDeprecated ? "deprecated" : "",
                    textContent: field.name || ""
                }),
                createElement("td", {innerHTML: field.typeHtml || ""})
            ]
        }))
    });

    return createElement("table", {
        className: "fields",
        children: [
            createElement("thead", {
                children: [createElement("tr", {
                    children: [
                        createElement("th", {attributes: {width: "20%"}, textContent: "フィールド"}),
                        createElement("th", {textContent: "フィールド型"})
                    ]
                })]
            }),
            tbody
        ]
    });
}

function createMethodsTable(kind, methods) {
    if (!methods || methods.length === 0) return null;

    const tbody = createElement("tbody", {
        children: methods.map(method => createElement("tr", {
            children: [
                createElement("td", {className: "method-name", textContent: method.labelWithSymbol || ""}),
                createElement("td", {
                    children: (method.argumentsLinks || []).map(arg => createElement("span", {
                        className: "method-argument-item",
                        innerHTML: arg
                    }))
                }),
                createElement("td", {innerHTML: method.returnTypeLink || ""}),
                createElement("td", {
                    className: "markdown",
                    innerHTML: method.description ? marked.parse(method.description) : ""
                })
            ]
        }))
    });

    return createElement("table", {
        children: [
            createElement("thead", {
                children: [createElement("tr", {
                    children: [
                        createElement("th", {attributes: {width: "20%"}, textContent: kind}),
                        createElement("th", {textContent: "引数"}),
                        createElement("th", {textContent: "戻り値型"}),
                        createElement("th", {textContent: "説明"})
                    ]
                })]
            }),
            tbody
        ]
    });
}

function createEnumSection(enumInfo) {
    if (!enumInfo) return null;

    const constants = enumInfo.constants || [];
    const dl = createElement("dl", {
        children: constants.flatMap(constant => {
            const nodes = [createElement("dt", {textContent: constant.simpleText || ""})];
            if (constant.hasAlias && constant.title) {
                nodes.push(createElement("dd", {textContent: constant.title}));
            }
            return nodes;
        })
    });

    const section = createElement("section", {
        children: [
            createElement("h3", {textContent: "列挙値"}),
            dl
        ]
    });

    const parameterNames = enumInfo.parameterNames || [];
    const parameterRows = enumInfo.parameterRows || [];
    if (parameterNames.length > 0 && parameterRows.length > 0) {
        const thead = createElement("thead", {
            children: [createElement("tr", {
                children: [
                    createElement("th", {textContent: "列挙定数名"}),
                    ...parameterNames.map(name => createElement("th", {textContent: name}))
                ]
            })]
        });
        const tbody = createElement("tbody", {
            children: parameterRows.map(row => createElement("tr", {
                children: [
                    createElement("td", {className: "method-name", textContent: row.name || ""}),
                    ...(row.params || []).map(param => createElement("td", {textContent: param}))
                ]
            }))
        });

        section.appendChild(createElement("details", {
            children: [
                createElement("summary", {textContent: "列挙引数"}),
                createElement("table", {className: "fields", children: [thead, tbody]})
            ]
        }));
    }

    return section;
}

function renderPackages(packages, container) {
    if (!packages || packages.length === 0) return;

    packages.forEach(pkg => {
        const section = createElement("section", {
            className: "package",
            id: pkg.fqn,
            children: [
                createElement("h2", {
                    children: [createElement("a", {textContent: pkg.label || ""})]
                }),
                createElement("small", {
                    className: "fully-qualified-name",
                    textContent: pkg.fqn || ""
                })
            ]
        });

        if (pkg.description) {
            section.appendChild(createElement("section", {
                className: "markdown",
                innerHTML: marked.parse(pkg.description)
            }));
        }

        const childrenTable = createChildrenTable(pkg.children);
        if (childrenTable) {
            section.appendChild(childrenTable);
        }

        if (pkg.relationDiagram) {
            section.appendChild(createElement("pre", {
                className: "mermaid",
                textContent: pkg.relationDiagram
            }));
        }

        container.appendChild(section);
    });
}

function renderTypes(types, container) {
    if (!types || types.length === 0) return;

    types.forEach(type => {
        const titleLink = createElement("a", {
            textContent: type.label || "",
            className: type.isDeprecated ? "deprecated" : ""
        });

        const section = createElement("section", {
            className: "type",
            id: type.fqn,
            children: [
                createElement("h2", {children: [titleLink]}),
                createElement("div", {className: "fully-qualified-name", textContent: type.fqn || ""})
            ]
        });

        if (type.description) {
            section.appendChild(createElement("section", {
                className: "markdown",
                innerHTML: marked.parse(type.description)
            }));
        }

        if (type.enumInfo) {
            section.appendChild(createEnumSection(type.enumInfo));
        }

        const fieldsTable = createFieldsTable(type.fields);
        if (fieldsTable) section.appendChild(fieldsTable);

        const methodTable = createMethodsTable("メソッド", type.methods);
        if (methodTable) section.appendChild(methodTable);

        const staticTable = createMethodsTable("staticメソッド", type.staticMethods);
        if (staticTable) section.appendChild(staticTable);

        container.appendChild(section);
    });
}

const DomainApp = {
    init() {
        const data = globalThis.domainData;
        if (!data) return;

        renderSidebar(data.tree);

        const main = document.getElementById("domain-main");
        if (!main) return;
        main.innerHTML = "";

        renderPackages(data.packages, main);
        renderTypes(data.types, main);
    }
};

DomainApp.init();
