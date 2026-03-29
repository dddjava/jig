// 先に読んでおく
// jig.js : globalThis.Jig.dom
// jig-common.js : globalThis.Jig.mermaid
// package-diagram.js : globalThis.Jig.packageDiagram
// package-data.js : globalThis.packageData
// glossary-data.js : globalThis.glossaryData

/**
 * @typedef {Object} PackageData
 * @property {Package[]} packages
 * @property {Relation[]} relations
 * @property {string} domainPackageRoots
 */

/**
 * @type {PackageData}
 */
function getPackageData() {
    return globalThis.packageData;
}

function renderPackageDiagram(packageDiagramContainer, allPackages, allPackageRelations, packageRoot, titleLabel) {
    const createElement = globalThis.Jig.dom.createElement;
    const domainPackageDiagram = createElement("div", {className: "mermaid-diagram"});
    packageDiagramContainer.appendChild(domainPackageDiagram);
    globalThis.Jig.observe.lazyRender(domainPackageDiagram, () => {
        domainPackageDiagram.innerHTML = "";
        console.log("Rendering package diagram for " + packageRoot);
        const pkgDiagram = globalThis.Jig.packageDiagram.createPackageLevelDiagram(
            {fqn: packageRoot},
            allPackages, allPackageRelations,
            {
                transitiveReductionEnabled: true,
                diagramDirection: "TB"
            }
        );
        if (pkgDiagram) {
            // ダイアグラムが出力されない場合もあるので、タイトル行は表示するときだけ追加する
            packageDiagramContainer.insertBefore(createElement("h3", {textContent: titleLabel}), domainPackageDiagram);
            globalThis.Jig.mermaid.renderWithControls(domainPackageDiagram, pkgDiagram);
        }
    });
}

const IndexApp = {

    init() {
        const packageDiagramContainer = document.getElementById("package-diagram");
        if (!packageDiagramContainer) throw new Error("package-diagram container is not defined");

        const packageData = getPackageData()
        const allPackages = packageData.packages;
        const allPackageRelations = packageData.relations;

        packageData.domainPackageRoots.forEach(packageRoot => {
            renderPackageDiagram(
                packageDiagramContainer,
                allPackages, allPackageRelations,
                packageRoot,
                "ドメインパッケージ: " + packageRoot
            );
        })

        const commonRoot = globalThis.Jig.packageDiagram.getCommonPrefix(allPackages.map(pkg => pkg.fqn))
        renderPackageDiagram(
            packageDiagramContainer,
            allPackages, allPackageRelations,
            commonRoot,
            "最上位パッケージ: " + commonRoot
        );
    }
}

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        IndexApp.init();
    });
}

