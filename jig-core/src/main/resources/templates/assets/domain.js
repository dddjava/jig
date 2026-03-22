/**
 * @typedef {Object} PackageType
 * @property {string} fqn
 * @property {{fqn: string}[]} [types]
 */
/**
 * @typedef {Object} DomainType
 * @property {string} fqn
 * @property {DomainField[]} fields
 * @property {DomainMethod} methods
 * @property {DomainMethod[]} staticMethods
 * @property {EnumInfo} [enumInfo]
 */
/**
 * @typedef {{
 *     name: string,
 *     typeRef: TypeRef,
 *     isDeprecated: boolean,
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
 * @typedef {Object} EnumInfo
 * @property {{name: string, params: string[]}} constants
 * @property {string[]} parameterNames
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
 * @returns  {Term | undefined}
 */
function findGlossary(fqn) {
    return globalThis.glossaryData[fqn];
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
    const typesMap = globalThis.domainData._typesMap;
    const domainType = typesMap ? typesMap.get(fqn) : globalThis.domainData.types.find(type => type.fqn === fqn);
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

/**
 * パッケージの直下の子パッケージを取得する
 * @param {PackageType} pkg
 * @returns {PackageType[]}
 */
function getDirectChildPackages(pkg) {
    const childPackagesMap = globalThis.domainData._childPackagesMap;
    if (childPackagesMap) {
        return childPackagesMap.get(pkg.fqn) || [];
    }
    // フォールバック（init 前に呼ばれた場合）
    const prefix = pkg.fqn + ".";
    return (globalThis.domainData.packages || []).filter(p => {
        if (!p.fqn.startsWith(prefix)) return false;
        // prefix より後にドットが含まれない = 直接の子
        return p.fqn.indexOf(".", prefix.length) === -1;
    });
}

function renderPackageNavItem(pkg) {
    // 子が1つだけでタイプを持たないパッケージを統合して表示
    let currentPkg = pkg;
    const mergedNames = [getGlossaryTitle(pkg.fqn)];

    while (true) {
        const childPackages = getDirectChildPackages(currentPkg);
        if (childPackages.length !== 1) break;
        if (currentPkg.types && currentPkg.types.length > 0) break;

        const childPkg = childPackages[0];
        mergedNames.push(getGlossaryTitle(childPkg.fqn));
        currentPkg = childPkg;
    }

    const summaryLink = createElement("a", {
        attributes: {href: "#" + currentPkg.fqn},
        textContent: mergedNames.join("/")
    });
    const details = createElement("details", {
        attributes: {open: ""},
        children: [
            createElement("summary", {
                className: "package",
                children: [summaryLink, document.createTextNode("/")]
            })
        ]
    });

    // 子パッケージを表示（統合後の currentPkg の直下のみ）
    const childPackages = getDirectChildPackages(currentPkg);
    childPackages.forEach(childPkg => {
        details.appendChild(renderPackageNavItem(childPkg));
    });

    // 子タイプを表示
    (currentPkg.types || []).forEach(child => {
        const link = createElement("a", {
            attributes: {href: "#" + child.fqn},
            textContent: getGlossaryTitle(child.fqn)
        });
        details.appendChild(createElement("div", {children: [link]}));
    });

    return details;
}

/**
 * @param {PackageType} pkg
 * @returns {string | null} Mermaid記法のダイアグラム文字列、または関連がない場合はnull
 */
function createRelationDiagram(pkg) {
    const relations = globalThis.domainData.relations;
    if (!relations || relations.length === 0) return null;

    const pkgTypeFqns = new Set((pkg.types || []).map(t => t.fqn));
    if (pkgTypeFqns.size === 0) return null;

    // このパッケージの型から出る関連
    const fromPkgRelations = relations.filter(r => pkgTypeFqns.has(r.from));
    if (fromPkgRelations.length === 0) return null;

    // 内部関連と外部関連に分類
    const internalRelations = fromPkgRelations.filter(r => pkgTypeFqns.has(r.to));
    if (internalRelations.length === 0) return null;

    const externalRelations = fromPkgRelations.filter(r => !pkgTypeFqns.has(r.to));

    function packageOf(fqn) {
        const idx = fqn.lastIndexOf('.');
        return idx < 0 ? fqn : fqn.substring(0, idx);
    }

    // サブグラフ内ノード（from全部 + 内部to）
    const internalFqns = new Set();
    fromPkgRelations.forEach(r => internalFqns.add(r.from));
    internalRelations.forEach(r => internalFqns.add(r.to));

    // 外部パッケージノード
    const externalPkgFqns = new Set();
    externalRelations.forEach(r => externalPkgFqns.add(packageOf(r.to)));

    // エッジ（重複排除）
    const edgeSet = new Set();
    internalRelations.forEach(r => edgeSet.add(`${r.from} --> ${r.to}`));
    externalRelations.forEach(r => edgeSet.add(`${r.from} --> ${packageOf(r.to)}`));

    function escapeMermaidLabel(label) {
        return label.replace(/"/g, '#quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function mermaidTypeBox(fqn) {
        return `${fqn}["${escapeMermaidLabel(getGlossaryTitle(fqn))}"]`;
    }

    function mermaidPackageBox(fqn) {
        return `${fqn}(["${escapeMermaidLabel(getGlossaryTitle(fqn))}"])`;
    }

    const i = '    ';
    const lines = ['\ngraph TB'];
    lines.push(`${i}subgraph ${pkg.fqn}`);
    lines.push(`${i}direction TB`);
    internalFqns.forEach(fqn => lines.push(`${i}${mermaidTypeBox(fqn)}`));
    lines.push(`${i}end`);
    externalPkgFqns.forEach(fqn => lines.push(`${i}${mermaidPackageBox(fqn)}`));
    [...internalFqns, ...externalPkgFqns].forEach(fqn =>
        lines.push(`${i}click ${fqn} "#${fqn}"`)
    );
    edgeSet.forEach(edge => lines.push(`${i}${edge}`));

    return lines.join('\n');
}

function renderSidebar(packages) {
    const container = document.getElementById("domain-sidebar-list");
    if (!container) return;
    container.innerHTML = "";

    // 直接の子パッケージ fqn の集合
    const childPackageFqns = new Set();
    (packages || []).forEach(pkg => {
        const children = getDirectChildPackages(pkg);
        children.forEach(child => {
            childPackageFqns.add(child.fqn);
        });
    });

    // トップレベルのパッケージのみを表示（直接の親を持たないもの）
    (packages || []).forEach(pkg => {
        if (!childPackageFqns.has(pkg.fqn)) {
            container.appendChild(renderPackageNavItem(pkg));
        }
    });
}

function createChildrenTable(pkg) {
    const types = pkg.types || [];
    const childPackages = getDirectChildPackages(pkg);

    // 子パッケージ（▶︎ プレフィックス） + 子タイプ を合わせて表示
    const allChildren = [
        ...childPackages.map(childPkg => ({
            isPackage: true,
            fqn: childPkg.fqn,
            title: getGlossaryTitle(childPkg.fqn)
        })),
        ...types.map(type => ({
            isPackage: false,
            fqn: type.fqn,
            title: getGlossaryTitle(type.fqn)
        }))
    ];

    if (allChildren.length === 0) return null;

    const tbody = createElement("tbody", {
        children: allChildren.map(child => {
            const prefix = child.isPackage ? "▶︎ " : "";
            const link = createElement("a", {
                attributes: {href: "#" + child.fqn},
                textContent: child.title
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
                createElement("td", {
                    children: [createTypeRefLink(field.typeRef)]
                })
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

function createEnumSection(type) {
    if (!type.enumInfo) return null;

    const constants = type.enumInfo.constants;
    const dl = createElement("dl", {
        children: constants.flatMap(constant => {
            const nodes = [createElement("dt", {textContent: constant.name})];
            const term = findGlossary(`${type.fqn}#${constant.name}`);
            if (term && term.title !== constant.name) {
                nodes.push(createElement("dd", {textContent: term.title}));
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

    const parameterNames = type.enumInfo.parameterNames;
    if (parameterNames.length) {
        const thead = createElement("thead", {
            children: [createElement("tr", {
                children: [
                    createElement("th", {textContent: "列挙定数名"}),
                    ...parameterNames.map(name => createElement("th", {textContent: name}))
                ]
            })]
        });
        const tbody = createElement("tbody", {
            children:
                constants.map(constant => createElement("tr", {
                    children: [
                        createElement("td", {className: "method-name", textContent: constant.name || ""}),
                        ...constant.params.map(param => createElement("td", {textContent: param}))
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
                    textContent: pkg.fqn
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

        const childrenTable = createChildrenTable(pkg);
        if (childrenTable) {
            section.appendChild(childrenTable);
        }

        const diagram = createRelationDiagram(pkg);
        if (diagram) {
            const mmdContainer = createElement("div", {className: "mermaid-diagram"});
            section.appendChild(mmdContainer);
            globalThis.Jig.observe.lazyRender(mmdContainer, () => {
                mmdContainer.innerHTML = "";
                globalThis.Jig.mermaid.renderWithControls(mmdContainer, diagram);
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
            section.appendChild(createEnumSection(type));
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
         *   packages: PackageType[],
         *   types: DomainType[]
         *   relations: {from: string, to: string}[]
         * }}
         */
        const data = globalThis.domainData;
        if (!data) return;

        // types を FQN → type の Map にインデックス化（O(n) → O(1) 検索）
        globalThis.domainData._typesMap = new Map(
            (data.types || []).map(type => [type.fqn, type])
        );

        // packages の直下の子を事前計算（O(n) → O(1) 取得）
        const childrenMap = new Map((data.packages || []).map(p => [p.fqn, []]));
        (data.packages || []).forEach(p => {
            const parentFqn = p.fqn.substring(0, p.fqn.lastIndexOf('.'));
            if (childrenMap.has(parentFqn)) {
                childrenMap.get(parentFqn).push(p);
            }
        });
        globalThis.domainData._childPackagesMap = childrenMap;

        renderSidebar(data.packages);

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
    module.exports = { DomainApp, getGlossaryMethodTerm, createTypeLink, createTypeRefLink, renderPackageNavItem, getDirectChildPackages };
}
