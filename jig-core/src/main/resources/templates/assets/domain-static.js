function getDomainData() {
    const jsonText = document.getElementById("domain-data")?.textContent || "{}";
    try {
        const data = JSON.parse(jsonText);
        return {
            packages: Array.isArray(data.packages) ? data.packages : [],
            classes: Array.isArray(data.classes) ? data.classes : [],
        };
    } catch (e) {
        return { packages: [], classes: [] };
    }
}

function createElement(tag, options = {}) {
    const element = document.createElement(tag);
    if (options.className) element.className = options.className;
    if (options.id) element.id = options.id;
    if (options.textContent !== undefined) element.textContent = options.textContent;
    if (options.attributes) {
        for (const [key, value] of Object.entries(options.attributes)) {
            element.setAttribute(key, value);
        }
    }
    if (options.style) {
        for (const [key, value] of Object.entries(options.style)) {
            element.style[key] = value;
        }
    }
    if (options.children) {
        options.children.forEach(child => {
            if (child) element.appendChild(child);
        });
    }
    return element;
}

function renderNoData(container) {
    if (!container) return;
    container.textContent = "";
    container.appendChild(createElement("p", { className: "weak", textContent: "データなし" }));
}

function createSidebarSection(title, items) {
    if (!items || items.length === 0) return null;

    return createElement("section", {
        className: "in-page-sidebar__section",
        children: [
            createElement("p", {
                className: "in-page-sidebar__title",
                textContent: title
            }),
            createElement("ul", {
                className: "in-page-sidebar__links",
                children: items.map(({ id, label }) => createElement("li", {
                    className: "in-page-sidebar__item",
                    children: [
                        createElement("a", {
                            className: "in-page-sidebar__link",
                            attributes: { href: "#" + id },
                            textContent: label
                        })
                    ]
                }))
            })
        ]
    });
}

function buildPackageIndex(packages) {
    const byId = new Map();
    packages.forEach(p => {
        if (!p || !p.id) return;
        byId.set(p.id, p);
    });

    const childrenByParent = new Map();
    packages.forEach(p => {
        if (!p || !p.id) return;
        const parentId = p.parent ?? null;
        if (!childrenByParent.has(parentId)) childrenByParent.set(parentId, []);
        childrenByParent.get(parentId).push(p);
    });

    for (const [parentId, children] of childrenByParent.entries()) {
        children.sort((a, b) => (a.fqn ?? a.id ?? "").localeCompare((b.fqn ?? b.id ?? ""), "ja"));
        childrenByParent.set(parentId, children);
    }

    return { byId, childrenByParent };
}

function groupClassesByPackage(classes) {
    const map = new Map();
    classes.forEach(c => {
        if (!c || !c.id) return;
        const pkgId = c.package ?? null;
        if (!map.has(pkgId)) map.set(pkgId, []);
        map.get(pkgId).push(c);
    });
    for (const [pkgId, items] of map.entries()) {
        items.sort((a, b) => (a.fqn ?? a.id ?? "").localeCompare((b.fqn ?? b.id ?? ""), "ja"));
        map.set(pkgId, items);
    }
    return map;
}

function renderPackageTreeItem(pkg, index, classesByPackage, depth) {
    const label = pkg.label ?? pkg.fqn ?? pkg.id ?? "";
    const pkgId = pkg.id ?? "";
    const children = index.childrenByParent.get(pkgId) || [];
    const classItems = classesByPackage.get(pkgId) || [];

    const nestedChildren = [];
    if (children.length > 0) {
        nestedChildren.push(...children.map(child => renderPackageTreeItem(child, index, classesByPackage, depth + 1)));
    }
    if (classItems.length > 0) {
        nestedChildren.push(...classItems.map(c => createElement("li", {
            className: "in-page-sidebar__item",
            children: [
                createElement("a", {
                    className: "in-page-sidebar__link",
                    attributes: { href: "#" + (c.id ?? "") },
                    textContent: c.label ?? c.fqn ?? c.id ?? ""
                })
            ]
        })));
    }

    const link = createElement("a", {
        className: "in-page-sidebar__link",
        attributes: { href: "#" + pkgId },
        textContent: label
    });

    if (nestedChildren.length === 0) {
        return createElement("li", { className: "in-page-sidebar__item", children: [link] });
    }

    return createElement("li", {
        className: "in-page-sidebar__item",
        children: [
            link,
            createElement("ul", {
                className: "in-page-sidebar__links",
                style: { paddingLeft: `${Math.min(24, 12 + depth * 6)}px` },
                children: nestedChildren
            })
        ]
    });
}

function renderPackageTreeSection(packages, classes) {
    if (!Array.isArray(packages) || packages.length === 0) return null;

    const index = buildPackageIndex(packages);
    const classesByPackage = groupClassesByPackage(Array.isArray(classes) ? classes : []);
    const roots = index.childrenByParent.get(null) || [];
    if (roots.length === 0) return null;

    return createElement("section", {
        className: "in-page-sidebar__section",
        children: [
            createElement("p", { className: "in-page-sidebar__title", textContent: "パッケージ" }),
            createElement("ul", {
                className: "in-page-sidebar__links",
                children: roots.map(root => renderPackageTreeItem(root, index, classesByPackage, 0))
            })
        ]
    });
}

function renderSidebar(container, data) {
    if (!container) return;
    container.textContent = "";

    const packageTreeSection = renderPackageTreeSection(data.packages || [], data.classes || []);
    if (packageTreeSection) container.appendChild(packageTreeSection);

    if (!packageTreeSection) {
        renderNoData(container);
    }
}

function createField(label, value) {
    return createElement("div", {
        className: "outputs-item-field",
        children: [
            createElement("dt", { textContent: label }),
            createElement("dd", { textContent: value })
        ]
    });
}

function renderPackageCard(pkg) {
    const label = pkg.label ?? pkg.fqn ?? pkg.id ?? "";
    const fqn = pkg.fqn ?? pkg.id ?? "";
    const description = pkg.description ?? "";

    const children = [
        createElement("h3", { textContent: label }),
        createElement("p", { className: "fully-qualified-name", textContent: fqn }),
    ];
    if (description) {
        children.push(createElement("p", { className: "weak", textContent: description }));
    }
    return createElement("article", { id: pkg.id ?? "", className: "outputs-port-card", children });
}

function renderClassMemberSection(title, members, renderMember) {
    if (!Array.isArray(members) || members.length === 0) return null;
    return createElement("div", {
        className: "outputs-item-list",
        children: [
            createElement("p", { className: "outputs-persistence-title", textContent: title }),
            ...members.map(renderMember)
        ]
    });
}

function renderFieldItem(field) {
    const name = field.label ?? field.name ?? "";
    const type = field.type ?? "";
    const description = field.description ?? "";
    const meta = createElement("dl", {
        className: "outputs-item-meta",
        children: [
            createField("名前", name),
            ...(type ? [createField("型", type)] : []),
            ...(description ? [createField("説明", description)] : []),
        ]
    });
    return createElement("div", {
        className: "outputs-item",
        children: [
            createElement("h4", { textContent: name || "(no name)" }),
            meta
        ]
    });
}

function renderMethodItem(method) {
    const name = method.label ?? method.name ?? "";
    const returnType = method.returnType ?? "";
    const description = method.description ?? "";
    const parameters = Array.isArray(method.parameters)
        ? method.parameters.map(p => `${p.type ?? ""} ${p.name ?? ""}`.trim()).filter(Boolean).join(", ")
        : "";

    const metaChildren = [
        createField("名前", name),
        ...(returnType ? [createField("戻り値", returnType)] : []),
        ...(parameters ? [createField("引数", parameters)] : []),
        ...(description ? [createField("説明", description)] : []),
    ];

    return createElement("div", {
        className: "outputs-item",
        children: [
            createElement("h4", { textContent: name || "(no name)" }),
            createElement("dl", { className: "outputs-item-meta", children: metaChildren })
        ]
    });
}

function renderClassCard(clazz) {
    const label = clazz.label ?? clazz.fqn ?? clazz.id ?? "";
    const fqn = clazz.fqn ?? clazz.id ?? "";
    const description = clazz.description ?? "";

    const children = [
        createElement("h3", { textContent: label }),
        createElement("p", { className: "fully-qualified-name", textContent: fqn }),
    ];
    if (description) {
        children.push(createElement("p", { className: "weak", textContent: description }));
    }

    const fieldsSection = renderClassMemberSection("フィールド", clazz.fields, renderFieldItem);
    if (fieldsSection) children.push(fieldsSection);

    const methodsSection = renderClassMemberSection("メソッド", clazz.methods, renderMethodItem);
    if (methodsSection) children.push(methodsSection);

    return createElement("article", { id: clazz.id ?? "", className: "outputs-port-card", children });
}

function renderMain(container, data) {
    if (!container) return;
    container.textContent = "";

    const packages = Array.isArray(data.packages) ? data.packages : [];
    const classes = Array.isArray(data.classes) ? data.classes : [];
    if (packages.length === 0 && classes.length === 0) {
        renderNoData(container);
        return;
    }

    packages
        .slice()
        .sort((a, b) => (a.fqn ?? a.id ?? "").localeCompare((b.fqn ?? b.id ?? ""), "ja"))
        .forEach(pkg => container.appendChild(renderPackageCard(pkg)));

    classes
        .slice()
        .sort((a, b) => (a.fqn ?? a.id ?? "").localeCompare((b.fqn ?? b.id ?? ""), "ja"))
        .forEach(clazz => container.appendChild(renderClassCard(clazz)));
}

const DomainStaticApp = {
    init() {
        const bodyClassList = document.body?.classList;
        if (bodyClassList?.contains && !bodyClassList.contains("domain-static")) return;

        const data = getDomainData();
        renderSidebar(document.getElementById("domain-sidebar-list"), data);
        renderMain(document.getElementById("domain-main"), data);
    }
};

if (typeof window !== "undefined" && typeof document !== "undefined") {
    window.addEventListener("DOMContentLoaded", () => {
        DomainStaticApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = {
        DomainStaticApp,
        getDomainData,
        renderSidebar,
        renderMain,
        createElement,
        createSidebarSection,
        renderPackageTreeSection,
    };
}
