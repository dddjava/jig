const createElement = globalThis.Jig.dom.createElement;
const createElementForTypeRef = globalThis.Jig.dom.createElementForTypeRef;
const { getTypeTerm, getMethodTerm, getFieldTerm } = globalThis.Jig.glossary;

const domainSettings = {
    diagramDirection: 'TB',
    showExternalRelations: true,
    showDeprecatedNodes: true,
    showFields: true,
    showMethods: true,
    showStaticMethods: true,
};

const diagramRegistry = []; // [{container, pkg}]
const renderedContainers = new Set(); // 実際に描画済みのコンテナ（設定変更時の再描画対象）

/**
 * @typedef {Object} TypeRef
 * @property {string} fqn
 * @property {TypeRef[]} [typeArgumentRefs]
 */

/**
 * @typedef {Object} DomainField
 * @property {string} name
 * @property {TypeRef} typeRef
 * @property {boolean} isDeprecated
 */

/**
 * @typedef {Object} DomainMethod
 * @property {string} fqn
 * @property {TypeRef[]} parameterTypeRefs
 * @property {TypeRef} returnTypeRef
 * @property {boolean} isDeprecated
 */

/**
 * @typedef {Object} EnumConstant
 * @property {string} name
 * @property {string[]} params
 */

/**
 * @typedef {Object} EnumInfo
 * @property {EnumConstant[]} constants
 * @property {string[]} parameterNames
 */

/**
 * @typedef {Object} DomainType
 * @property {string} fqn
 * @property {DomainField[]} fields
 * @property {DomainMethod[]} methods
 * @property {DomainMethod[]} staticMethods
 * @property {EnumInfo} [enumInfo]
 * @property {boolean} isDeprecated
 */

/**
 * @typedef {Object} PackageType
 * @property {string} fqn
 * @property {{fqn: string}[]} types
 */

/**
 * @typedef {Object} DomainData
 * @property {PackageType[]} packages
 * @property {DomainType[]} types
 * @property {Map<string, DomainType>} _typesMap
 * @property {Map<string, PackageType[]>} _childPackagesMap
 */

/**
 * @returns {DomainData}
 */
function getDomainData() {
    return globalThis.domainData;
}


/**
 * パッケージの直下の子パッケージを取得する
 * @param {PackageType} pkg
 * @returns {PackageType[]}
 */
function getDirectChildPackages(pkg) {
    return getDomainData()._childPackagesMap.get(pkg.fqn);
}

/**
 * @param {PackageType} pkg
 * @returns {HTMLElement}
 */
function renderPackageNavItem(pkg) {
    // 子が1つだけでタイプを持たないパッケージを統合して表示
    let currentPkg = pkg;
    const mergedNames = [getTypeTerm(pkg.fqn).title];

    while (true) {
        const childPackages = getDirectChildPackages(currentPkg);
        if (childPackages.length !== 1) break;
        if (currentPkg.types.length > 0) break;

        const childPkg = childPackages[0];
        mergedNames.push(getTypeTerm(childPkg.fqn).title);
        currentPkg = childPkg;
    }

    const summaryLink = createElement("a", {
        attributes: {href: "#" + globalThis.Jig.fqnToId("domain", currentPkg.fqn)},
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
    currentPkg.types.forEach(child => {
        const domainType = getDomainData()._typesMap?.get(child.fqn);
        const link = createElement("a", {
            attributes: {href: "#" + globalThis.Jig.fqnToId("domain", child.fqn)},
            className: domainType?.isDeprecated ? "deprecated" : "",
            textContent: getTypeTerm(child.fqn).title
        });
        details.appendChild(createElement("div", {children: [link]}));
    });

    return details;
}

/**
 * @param {PackageType} pkg
 * @returns {string | null}
 */
function createRelationDiagram(pkg) {
    const fqnToMermaidId = (fqn) => globalThis.Jig.fqnToId("n", fqn);
    const fqnToHtmlId = (fqn) => globalThis.Jig.fqnToId("domain", fqn);

    const typesMap = getDomainData()._typesMap;
    const relations = (globalThis.typeRelationsData?.relations || [])
        .filter(r => typesMap?.has(r.from) && typesMap?.has(r.to));

    let pkgTypeFqns = new Set(pkg.types.map(t => t.fqn));
    if (pkgTypeFqns.size === 0) return null;

    // Deprecated ノード非表示の場合、deprecated 型を除外
    if (!domainSettings.showDeprecatedNodes) {
        const typesMap = getDomainData()._typesMap;
        pkgTypeFqns = new Set([...pkgTypeFqns].filter(fqn => !typesMap?.get(fqn)?.isDeprecated));
        if (pkgTypeFqns.size === 0) return null;
    }

    // このパッケージの型から出る関連
    const fromPkgRelations = relations.filter(r => pkgTypeFqns.has(r.from));

    // 内部関連と外部関連に分類
    const internalRelations = fromPkgRelations.filter(r => pkgTypeFqns.has(r.to));

    const externalRelations = domainSettings.showExternalRelations
        ? fromPkgRelations.filter(r => !pkgTypeFqns.has(r.to))
        : [];

    function packageOf(fqn) {
        const idx = fqn.lastIndexOf('.');
        return idx < 0 ? fqn : fqn.substring(0, idx);
    }

    // 型が関連を持つ場合は関連から、ない場合はパッケージ内全型をノードにする
    const internalFqns = (fromPkgRelations.length > 0 || internalRelations.length > 0)
        ? new Set()
        : pkgTypeFqns;

    if (fromPkgRelations.length > 0 || internalRelations.length > 0) {
        fromPkgRelations.forEach(r => internalFqns.add(r.from));
        internalRelations.forEach(r => internalFqns.add(r.to));
    }

    // 外部パッケージノード
    const externalPkgFqns = new Set();
    externalRelations.forEach(r => externalPkgFqns.add(packageOf(r.to)));

    // エッジ（重複排除）
    const edgeSet = new Set();
    internalRelations.forEach(r => edgeSet.add(`${fqnToMermaidId(r.from)} --> ${fqnToMermaidId(r.to)}`));
    externalRelations.forEach(r => edgeSet.add(`${fqnToMermaidId(r.from)} --> ${fqnToMermaidId(packageOf(r.to))}`));

    function escapeMermaidLabel(label) {
        return label.replace(/"/g, '#quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    function mermaidTypeBox(fqn) {
        return `${fqnToMermaidId(fqn)}["${escapeMermaidLabel(getTypeTerm(fqn).title)}"]`;
    }

    function mermaidPackageBox(fqn) {
        return `${fqnToMermaidId(fqn)}@{shape: st-rect, label: "${escapeMermaidLabel(getTypeTerm(fqn).title)}"}`;
    }

    const i = '    ';
    const lines = [`\ngraph ${domainSettings.diagramDirection}`];
    lines.push(`${i}subgraph ${globalThis.Jig.fqnToId("sg", pkg.fqn)} ["${escapeMermaidLabel(getTypeTerm(pkg.fqn).title)}"]`);
    lines.push(`${i}direction ${domainSettings.diagramDirection}`);
    internalFqns.forEach(fqn => lines.push(`${i}${mermaidTypeBox(fqn)}`));
    lines.push(`${i}end`);
    externalPkgFqns.forEach(fqn => lines.push(`${i}${mermaidPackageBox(fqn)}`));
    [...internalFqns, ...externalPkgFqns].forEach(fqn =>
        lines.push(`${i}click ${fqnToMermaidId(fqn)} "#${fqnToHtmlId(fqn)}"`)
    );
    edgeSet.forEach(edge => lines.push(`${i}${edge}`));

    return lines.join('\n');
}

/**
 * @param {PackageType[]} packages
 * @returns {void}
 */
function renderSidebar(packages) {
    const container = document.getElementById("domain-sidebar-list");
    if (!container) return;
    container.innerHTML = "";

    // 直接の子パッケージ fqn の集合
    const childPackageFqns = new Set();
    packages.forEach(pkg => {
        const children = getDirectChildPackages(pkg);
        children.forEach(child => {
            childPackageFqns.add(child.fqn);
        });
    });

    // トップレベルのパッケージのみを表示（直接の親を持たないもの）
    packages.forEach(pkg => {
        if (!childPackageFqns.has(pkg.fqn)) {
            container.appendChild(renderPackageNavItem(pkg));
        }
    });
}

/**
 * @param {PackageType} pkg
 * @returns {HTMLElement | null}
 */
function createChildrenTable(pkg) {
    const types = pkg.types;
    const childPackages = getDirectChildPackages(pkg);

    // 子パッケージ（▶︎ プレフィックス） + 子タイプ を合わせて表示
    const allChildren = [
        ...childPackages.map(childPkg => ({
            isPackage: true,
            fqn: childPkg.fqn,
            title: getTypeTerm(childPkg.fqn).title
        })),
        ...types.map(type => ({
            isPackage: false,
            fqn: type.fqn,
            title: getTypeTerm(type.fqn).title
        }))
    ];

    if (allChildren.length === 0) return null;

    const tbody = createElement("tbody", {
        children: allChildren.map(child => {
            const prefix = child.isPackage ? "▶︎ " : "";
            // 型の場合は createTypeLink を使用して deprecated 処理を統一
            const link = child.isPackage
                ? createElement("a", {
                    attributes: {href: "#" + globalThis.Jig.fqnToId("domain", child.fqn)},
                    textContent: child.title
                })
                : createElementForTypeRef({fqn: child.fqn});
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

function createFieldsList(fields) {
    return globalThis.Jig.dom.createFieldsList(fields, createElementForTypeRef);
}

function createMethodsList(kind, methods) {
    return globalThis.Jig.dom.createMethodsList(kind, methods, createElementForTypeRef);
}

/**
 * @param {{enumInfo: EnumInfo | undefined, fqn: string}} type
 * @returns {HTMLElement | null}
 */
function createEnumSection(type) {
    if (!type.enumInfo) return null;

    const constants = type.enumInfo.constants;
    const dl = createElement("dl", {
        children: constants.flatMap(constant => {
            const nodes = [createElement("dt", {textContent: constant.name})];
            const term = getFieldTerm(`${type.fqn}#${constant.name}`);
            // 取れたかどうかに関わらず異なる場合のみ出す
            if (term && term.title !== constant.name) {
                nodes.push(createElement("dd", {textContent: term.title}));
            }
            return nodes;
        })
    });

    const section = createElement("section", {
        className: "jig-card jig-card--item",
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
                        createElement("td", {className: "method-name", textContent: constant.name}),
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

/**
 * @param {PackageType[]} packages
 * @param {HTMLElement} container
 * @returns {void}
 */
function renderPackages(packages, container) {
    if (packages.length === 0) return;

    packages.forEach(pkg => {
        const section = createElement("section", {
            className: "jig-card jig-card--type",
            id: globalThis.Jig.fqnToId("domain", pkg.fqn),
            children: [
                createElement("h3", {
                    children: [createElement("a", {textContent: getTypeTerm(pkg.fqn).title})]
                }),
                createElement("div", {
                    className: "fully-qualified-name",
                    textContent: pkg.fqn
                })
            ]
        });

        const pkgDescription = getTypeTerm(pkg.fqn).description;
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

        if (pkg.types.length > 0) {
            const mmdContainer = createElement("div", {className: "mermaid-diagram"});
            section.appendChild(mmdContainer);
            diagramRegistry.push({container: mmdContainer, pkg});
            globalThis.Jig.observe.lazyRender(mmdContainer, () => {
                renderedContainers.add(mmdContainer);
                mmdContainer.innerHTML = "";
                const diagram = createRelationDiagram(pkg);
                if (diagram) {
                    globalThis.Jig.mermaid.renderWithControls(mmdContainer, diagram);
                }
            });
        }

        container.appendChild(section);
    });
}

/**
 * @param {DomainType[]} types
 * @param {HTMLElement} container
 * @returns {void}
 */
function renderTypes(types, container) {
    if (types.length === 0) return;

    types.forEach(type => {
        const titleLink = createElement("a", {
            textContent: getTypeTerm(type.fqn).title,
            className: type.isDeprecated ? "deprecated" : ""
        });

        const section = createElement("section", {
            className: "jig-card jig-card--type",
            id: globalThis.Jig.fqnToId("domain", type.fqn),
            children: [
                createElement("h3", {children: [titleLink]}),
                createElement("div", {className: "fully-qualified-name", textContent: type.fqn})
            ]
        });

        const typeDescription = getTypeTerm(type.fqn).description;
        if (typeDescription) {
            section.appendChild(createElement("section", {
                className: "markdown",
                innerHTML: globalThis.Jig.markdown.parse(typeDescription)
            }));
        }

        if (type.enumInfo) {
            section.appendChild(createEnumSection(type));
        }

        const fieldsList = createFieldsList(type.fields);
        if (fieldsList) section.appendChild(fieldsList);

        const methodList = createMethodsList("メソッド", type.methods);
        if (methodList) section.appendChild(methodList);

        const staticList = createMethodsList("staticメソッド", type.staticMethods);
        if (staticList) section.appendChild(staticList);

        container.appendChild(section);
    });
}

/**
 * @returns {void}
 */
function updateDirectionIcon() {
    const verticalArrows = document.querySelector('.vertical-arrows');
    const horizontalArrows = document.querySelector('.horizontal-arrows');
    if (verticalArrows && horizontalArrows) {
        if (domainSettings.diagramDirection === 'TB') {
            verticalArrows.style.display = '';
            horizontalArrows.style.display = 'none';
        } else {
            verticalArrows.style.display = 'none';
            horizontalArrows.style.display = '';
        }
    }
}

/**
 * @returns {void}
 */
function rerenderDiagrams() {
    diagramRegistry
        .filter(({container}) => renderedContainers.has(container))
        .forEach(({container, pkg}) => {
            container.innerHTML = "";
            const diagram = createRelationDiagram(pkg);
            if (diagram) {
                globalThis.Jig.mermaid.renderWithControls(container, diagram);
            }
        });
}

/**
 * @returns {void}
 */
function initSettings() {
    const directionToggle = document.getElementById('direction-toggle');
    if (directionToggle) {
        directionToggle.addEventListener('click', () => {
            domainSettings.diagramDirection = domainSettings.diagramDirection === 'TB' ? 'LR' : 'TB';
            updateDirectionIcon();
            rerenderDiagrams();
        });
    }

    const externalCheckbox = document.getElementById('show-external-relations');
    if (externalCheckbox) {
        externalCheckbox.addEventListener('change', () => {
            domainSettings.showExternalRelations = externalCheckbox.checked;
            rerenderDiagrams();
        });
    }

    const deprecatedCheckbox = document.getElementById('show-deprecated-nodes');
    if (deprecatedCheckbox) {
        deprecatedCheckbox.addEventListener('change', () => {
            domainSettings.showDeprecatedNodes = deprecatedCheckbox.checked;
            rerenderDiagrams();
        });
    }

    const fieldsCheckbox = document.getElementById('show-fields');
    if (fieldsCheckbox) {
        fieldsCheckbox.addEventListener('change', () => {
            domainSettings.showFields = fieldsCheckbox.checked;
            applyVisibilitySettings();
        });
    }

    const methodsCheckbox = document.getElementById('show-methods');
    if (methodsCheckbox) {
        methodsCheckbox.addEventListener('change', () => {
            domainSettings.showMethods = methodsCheckbox.checked;
            applyVisibilitySettings();
        });
    }

    const staticMethodsCheckbox = document.getElementById('show-static-methods');
    if (staticMethodsCheckbox) {
        staticMethodsCheckbox.addEventListener('change', () => {
            domainSettings.showStaticMethods = staticMethodsCheckbox.checked;
            applyVisibilitySettings();
        });
    }
}

/**
 * @returns {void}
 */
function applyVisibilitySettings() {
    const main = document.getElementById('domain-main');
    if (!main) return;

    const fieldsSections = main.querySelectorAll('section.methods-section');
    fieldsSections.forEach(section => {
        const h4 = section.querySelector('h4');
        if (h4 && h4.textContent === 'フィールド') {
            section.style.display = domainSettings.showFields ? '' : 'none';
        } else if (h4 && h4.textContent === 'メソッド') {
            section.style.display = domainSettings.showMethods ? '' : 'none';
        } else if (h4 && h4.textContent === 'staticメソッド') {
            section.style.display = domainSettings.showStaticMethods ? '' : 'none';
        }
    });
}

const DomainApp = {
    /**
     * @returns {void}
     */
    init() {
        const data = getDomainData();
        if (!data) {
            const main = document.getElementById("domain-main");
            if (main) {
                main.appendChild(createElement("p", {
                    className: "jig-data-error",
                    textContent: "ドメインデータ（domain-data.js）が読み込まれていません。JIG を実行してデータファイルを生成してください。"
                }));
            }
            return;
        }

        diagramRegistry.length = 0;
        renderedContainers.clear();
        initSettings();

        // types を FQN → type の Map にインデックス化（O(n) → O(1) 検索）
        globalThis.domainData._typesMap = new Map(
            data.types.map(type => [type.fqn, type])
        );

        // packages の直下の子を事前計算（O(n) → O(1) 取得）
        const childrenMap = new Map(data.packages.map(p => [p.fqn, []]));
        data.packages.forEach(p => {
            const parentFqn = p.fqn.substring(0, p.fqn.lastIndexOf('.'));
            if (childrenMap.has(parentFqn)) {
                childrenMap.get(parentFqn).push(p);
            }
        });
        globalThis.domainData._childPackagesMap = childrenMap;

        globalThis.Jig.dom.typeLinkResolver = (fqn) => {
            const domainType = getDomainData()?._typesMap?.get(fqn);
            if (domainType) {
                return {
                    href: '#' + globalThis.Jig.fqnToId("domain", fqn),
                    className: domainType.isDeprecated ? 'deprecated' : undefined
                };
            }
            // domain型でなければ単純名 + weakクラス
            return {
                className: 'weak',
                text: fqn.substring(fqn.lastIndexOf('.') + 1)
            };
        };

        renderSidebar(data.packages);

        const main = document.getElementById("domain-main");
        if (!main) return;
        main.innerHTML = "";

        // optional データの警告表示
        const warnings = [];
        if (!globalThis.glossaryData) {
            warnings.push("用語集（glossary-data.js）が読み込まれていません");
        }
        if (!globalThis.typeRelationsData) {
            warnings.push("型関連情報（type-relations-data.js）が読み込まれていません");
        }

        if (warnings.length > 0) {
            warnings.forEach(warning => {
                main.appendChild(createElement("p", {
                    className: "jig-data-warning",
                    textContent: warning + "。一部の情報が表示されない可能性があります。"
                }));
            });
        }

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
    module.exports = { DomainApp, renderPackageNavItem, getDirectChildPackages, createRelationDiagram };
}
