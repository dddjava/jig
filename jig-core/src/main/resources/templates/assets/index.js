const IndexApp = (() => {
    const Jig = globalThis.Jig;

    /**
     * @returns {PackageData}
     */
    function getPackageData() {
        return Jig.data.package.get();
    }

    function renderPackageDiagram(packageDiagramContainer, allPackages, allPackageRelations, packageRoot, titleLabel) {
        const domainPackageDiagram = Jig.dom.createElement("div", {className: "mermaid-diagram"});
        packageDiagramContainer.appendChild(domainPackageDiagram);

        domainPackageDiagram.innerHTML = "";
        const generator = (dir, opts) => Jig.mermaid.createPackageLevelDiagram(
            {fqn: packageRoot},
            allPackages, allPackageRelations,
            {
                transitiveReductionEnabled: true,
                diagramDirection: dir,
                showPhysicalName: opts?.showPhysicalName
            }
        );

        if (generator("TB")) {
            // ダイアグラムが出力されない場合もあるので、タイトル行は表示するときだけ追加する
            packageDiagramContainer.insertBefore(Jig.dom.createElement("h3", {textContent: titleLabel}), domainPackageDiagram);
            Jig.mermaid.render.renderWithControls(domainPackageDiagram, generator, {direction: "TB", enableLabelToggle: true});
        }
    }

    function renderDocumentLinks() {
        const container = document.getElementById("document-links");
        if (!container) return;

        const ul = container.querySelector("ul");
        if (!ul) return;
        ul.innerHTML = "";

        const links = Jig.data.navigation.getLinks();
        links.forEach(link => {
            const li = Jig.dom.createElement("li", {
                children: [
                    Jig.dom.createElement("a", {
                        attributes: {href: link.href},
                        textContent: link.label
                    })
                ]
            });
            ul.appendChild(li);
        });
    }

    function init() {
        renderDocumentLinks();

        const packageDiagramContainer = document.getElementById("package-diagram");
        if (!packageDiagramContainer) throw new Error("package-diagram container is not defined");

        const packageData = getPackageData();
        const allPackages = packageData.packages;
        const allPackageRelations = packageData.relations;

        packageData.domainPackageRoots.forEach(packageRoot => {
            renderPackageDiagram(
                packageDiagramContainer,
                allPackages, allPackageRelations,
                packageRoot,
                "ドメインパッケージ: " + packageRoot
            );
        });

        const commonRoot = Jig.util.getCommonPrefix(allPackages.map(pkg => pkg.fqn));
        renderPackageDiagram(
            packageDiagramContainer,
            allPackages, allPackageRelations,
            commonRoot,
            "最上位パッケージ: " + commonRoot
        );

        updateRelativeTime();
    }

    function updateRelativeTime() {
        const element = document.getElementById("jig-timestamp");
        if (!element) return;

        const timestampStr = element.getAttribute("data-jig-timestamp");
        if (!timestampStr) return;

        const timestamp = new Date(timestampStr);
        if (isNaN(timestamp.getTime())) return;

        const now = new Date();
        const diffMs = now - timestamp;
        const diffSec = Math.floor(diffMs / 1000);
        const diffMin = Math.floor(diffSec / 60);
        const diffHour = Math.floor(diffMin / 60);
        const diffDay = Math.floor(diffHour / 24);

        let relativeTime = "";
        if (diffDay > 0) {
            relativeTime = `${diffDay}日前`;
        } else if (diffHour > 0) {
            relativeTime = `${diffHour}時間前`;
        } else if (diffMin > 0) {
            relativeTime = `${diffMin}分前`;
        } else {
            relativeTime = "たった今";
        }

        element.textContent = `${element.textContent.split(' (')[0]} (${relativeTime})`;
    }

    return {
        init,
        getPackageData,
        renderPackageDiagram,
        updateRelativeTime
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        IndexApp.init();
    });
}
