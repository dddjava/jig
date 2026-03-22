/**
 * @typedef {{
 *     fqn: string,
 *     fields: DomainField[],
 *     methods: DomainMethod[],
 *     staticMethods: DomainMethod[],
 * }} DomainType
 */
/**
 * @typedef {{
 *     name: string,
 * }} DomainField
 */
/**
 * @typedef {{
 *     fqn: string,
 *     parameterTypeRefs: TypeRef[],
 *     returnTypeRef: TypeRef
 * }} DomainMethod
 */
/**
 * @typedef {Object} TypeRef
 * @property {string} fqn
 * @property {TypeRef[]} [typeArgumentRefs]
 */
/**
 * @typedef {{
 *     title: string,
 *     description: string,
 * }} Term
 */

const createElement = globalThis.Jig.dom.createElement;

function getGlossaryTitle(fqn) {
    const term = globalThis.glossaryData?.[fqn];
    return term?.title ?? (fqn.substring(fqn.lastIndexOf('.') + 1) || fqn);
}

function getGlossaryDescription(fqn) {
    return globalThis.glossaryData?.[fqn]?.description ?? "";
}

/**
 * @returns Term
 */
function getGlossaryMethodTerm(method) {
    const fqn = method.fqn;
    const term = globalThis.glossaryData[fqn];
    if (term) return term;

    // 引数の完全修飾名を単純名に変換して再取得
    const glossaryFqn = fqn.substring(0, fqn.lastIndexOf('(') + 1)
        + method.parameterTypeRefs
            .map(typeRef => typeRef.fqn)
            .map(x => x.substring(x.lastIndexOf('.') + 1))
            .join(",")
        + ')';
    const term2 = globalThis.glossaryData[glossaryFqn];
    if (term2) return term2;

    // glossaryになし
    // hoge.fuga.piyo#foo(bar, baz) => foo
    const name = fqn.substring(fqn.lastIndexOf('#') + 1, fqn.lastIndexOf('('));
    return {title: name, description: ""}
}

/**
 * @param {TypeRef} typeRef
 * @param {string} className
 */
function createTypeRefLink(typeRef, className= undefined) {
    if (typeRef.typeArgumentRefs && typeRef.typeArgumentRefs.length) {
        const typeElements= createTypeLink(typeRef.fqn);
        const argumentElements = typeRef.typeArgumentRefs
            .map(typeRef => createTypeRefLink(typeRef))
            // カンマを挟む。HTML Elementが文字列になってしまうのでjoinは使えない。
            .flatMap((v, i) => i ? [', ', v] : [v]);

        return createElement("span", {
            className: className,
            children: [typeElements, '<', ...argumentElements, '>']
        })
    }

    // 型引数なし
    return createTypeLink(typeRef.fqn, className);
}

/**
 * @param {string} fqn
 * @param {string} className
 */
function createTypeLink(fqn, className = undefined) {
    const domainType = globalThis.domainData.types.find(type => type.fqn === fqn);
    if (!domainType) {
        // domain型でなければ単純名のspan
        const simpleName = fqn.substring(fqn.lastIndexOf('.') + 1);
        return createElement('span', {
            className: (className ? className + ' ' : '') + "weak", // この文脈ではリンクしないものは弱くする。文脈なので個別じゃなくしたほうがよさそう。
            textContent: simpleName
        });
    }

    // domainに含まれるのはページ内リンク
    return createElement("a", {
        className: className,
        attributes: {href: `#${fqn}`},
        textContent: getGlossaryTitle(fqn)
    });
}

function renderTreeNode(node) {
    if (!node) return null;

    if (node.kind === "package") {
        const summaryLink = createElement("a", {
            attributes: {href: node.href || "#"},
            textContent: getGlossaryTitle(node.fqn)
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
        textContent: getGlossaryTitle(node.fqn)
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
                textContent: getGlossaryTitle(child.fqn)
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
        children: methods.map(method => {
            const methodTerm = getGlossaryMethodTerm(method);
            return createElement("tr", {
                children: [
                    createElement("td", {className: "method-name", textContent: methodTerm.title}),
                    createElement("td", {
                        children: method.parameterTypeRefs.map(param => createTypeRefLink(param, "method-argument-item"))
                    }),
                    createElement("td", {
                        children: [createTypeRefLink(method.returnTypeRef)]
                    }),
                    createElement("td", {
                        className: "markdown",
                        innerHTML: globalThis.Jig.markdown.parse(methodTerm.description)
                    })
                ]
            })
        })
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
            createElement("h4", {textContent: "列挙値"}),
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
            className: "jig-card jig-card--type",
            id: pkg.fqn,
            children: [
                createElement("h3", {
                    children: [createElement("a", {textContent: getGlossaryTitle(pkg.fqn)})]
                }),
                createElement("div", {
                    className: "fully-qualified-name",
                    textContent: pkg.fqn || ""
                })
            ]
        });

        const pkgDescription = getGlossaryDescription(pkg.fqn);
        if (pkgDescription) {
            section.appendChild(createElement("section", {
                className: "markdown",
                innerHTML: globalThis.Jig.markdown.parse(pkgDescription)
            }));
        }

        const childrenTable = createChildrenTable(pkg.children);
        if (childrenTable) {
            section.appendChild(childrenTable);
        }

        if (pkg.relationDiagram) {
            const mmdContainer = createElement("div", {className: "mermaid-diagram"});
            section.appendChild(mmdContainer);
            globalThis.Jig.observe.lazyRender(mmdContainer, () => {
                mmdContainer.innerHTML = "";
                globalThis.Jig.mermaid.renderWithControls(mmdContainer, pkg.relationDiagram);
            });
        }

        container.appendChild(section);
    });
}

function renderTypes(types, container) {
    if (!types || types.length === 0) return;

    types.forEach(type => {
        const titleLink = createElement("a", {
            textContent: getGlossaryTitle(type.fqn),
            className: type.isDeprecated ? "deprecated" : ""
        });

        const section = createElement("section", {
            className: "jig-card jig-card--type",
            id: type.fqn,
            children: [
                createElement("h3", {children: [titleLink]}),
                createElement("div", {className: "fully-qualified-name", textContent: type.fqn || ""})
            ]
        });

        const typeDescription = getGlossaryDescription(type.fqn);
        if (typeDescription) {
            section.appendChild(createElement("section", {
                className: "markdown",
                innerHTML: globalThis.Jig.markdown.parse(typeDescription)
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
        /**
         * @type {{
         *   tree: [],
         *   packages: [],
         *   types: DomainType[]
         * }}
         */
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

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        DomainApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = { DomainApp, getGlossaryMethodTerm, createTypeLink, createTypeRefLink };
}
