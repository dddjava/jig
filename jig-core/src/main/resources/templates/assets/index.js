const IndexApp = (() => {
    const Jig = globalThis.Jig;

    /**
     * @returns {PackageData}
     */
    function getPackageData() {
        return Jig.data.package.get();
    }

    function renderPackageDiagram(packageDiagramContainer, allPackages, allPackageRelations, packageRoot, titleLabelKey, nodeClickUrlCallback) {
        const domainPackageDiagram = Jig.dom.createElement("div", {className: "mermaid-diagram"});
        packageDiagramContainer.appendChild(domainPackageDiagram);

        const generator = (dir, opts) => Jig.mermaid.createPackageLevelDiagram(
            {fqn: packageRoot},
            allPackages, allPackageRelations,
            {
                transitiveReductionEnabled: true,
                diagramDirection: dir,
                showPhysicalName: opts?.showPhysicalName,
                nodeClickUrlCallback
            }
        );

        if (generator("TB")) {
            // ダイアグラムが出力されない場合もあるので、タイトル行は表示するときだけ追加する
            const heading = Jig.dom.createElement("h3", {
                children: [
                    Jig.dom.i18nText("span", titleLabelKey),
                    document.createTextNode(": " + packageRoot)
                ]
            });
            packageDiagramContainer.insertBefore(heading, domainPackageDiagram);
            Jig.mermaid.render.renderWithControls(domainPackageDiagram, generator, {direction: "TB", enableLabelToggle: true});
        }
    }

    function renderSummary() {
        const sourceEl = document.getElementById("jig-source");
        if (sourceEl) {
            const git = Jig.data.summary.getGit();
            sourceEl.replaceChildren();
            if (git) {
                sourceEl.appendChild(document.createTextNode("Source: "));
                if (git.remote) {
                    const remote = git.remote;
                    if (remote.baseUrl) {
                        sourceEl.appendChild(Jig.dom.createElement("a", {
                            attributes: {href: remote.baseUrl},
                            textContent: remote.displayName || remote.baseUrl
                        }));
                    } else {
                        sourceEl.appendChild(Jig.dom.createElement("code", {textContent: remote.rawUrl}));
                    }
                }
                if (git.shortHash) {
                    if (git.remote) sourceEl.appendChild(document.createTextNode(" @ "));
                    const codeEl = Jig.dom.createElement("code", {textContent: git.shortHash});
                    const commitUrl = git.remote && git.remote.commitUrl;
                    if (commitUrl) {
                        sourceEl.appendChild(Jig.dom.createElement("a", {
                            attributes: {href: commitUrl},
                            children: [codeEl]
                        }));
                    } else {
                        sourceEl.appendChild(codeEl);
                    }
                }
            }
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
                    Jig.dom.i18nText("a", link.label, {attributes: {href: link.href}})
                ]
            });
            ul.appendChild(li);
        });
    }

    function init() {
        renderSummary();
        renderDocumentLinks();

        const packageDiagramContainer = document.getElementById("package-diagram");
        const packageData = packageDiagramContainer ? getPackageData() : null;

        if (packageDiagramContainer && packageData) {
            const allPackages = packageData.packages;
            const allPackageRelations = packageData.relations;

            packageData.domainPackageRoots.forEach(packageRoot => {
                renderPackageDiagram(
                    packageDiagramContainer,
                    allPackages, allPackageRelations,
                    packageRoot,
                    "ドメインパッケージ",
                    Jig.mermaid.nav.domainTypeUrl
                );
            });

            const commonRoot = Jig.util.getCommonPrefix(allPackages.map(pkg => pkg.fqn));
            renderPackageDiagram(
                packageDiagramContainer,
                allPackages, allPackageRelations,
                commonRoot,
                "最上位パッケージ"
            );
        }

        updateRelativeTime();
        // 相対時間は data-i18n を持たないため、言語切り替え時に自分で描き直す
        document.addEventListener("jig:locale-change", updateRelativeTime);
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

        // 数値を含むため data-i18n では扱えない。キーの {n} を経過数に置換する
        const relative = (key, count) => Jig.i18n.t(key).replace("{n}", count);
        let relativeTime;
        if (diffDay > 0) {
            relativeTime = relative("{n}日前", diffDay);
        } else if (diffHour > 0) {
            relativeTime = relative("{n}時間前", diffHour);
        } else if (diffMin > 0) {
            relativeTime = relative("{n}分前", diffMin);
        } else {
            relativeTime = Jig.i18n.t("たった今");
        }

        // 表示のベースは data 属性の値。再実行しても相対時間が積み重ならない
        element.textContent = `${timestampStr} (${relativeTime})`;
    }

    return {
        init,
        getPackageData,
        renderPackageDiagram,
        updateRelativeTime
    };
})();

Jig.bootstrap.register("index", IndexApp.init);

if (typeof module !== "undefined" && module.exports) {
    module.exports = IndexApp;
}
