const InboundApp = (() => {
    const Jig = globalThis.Jig;

    const ADAPTER_ID_PREFIX = "adapter";

    const TYPE_CONFIG = [
        {type: 'HTTP_API',       label: 'リクエストハンドラ'},
        {type: 'QUEUE_LISTENER', label: 'メッセージリスナー'},
        {type: 'SCHEDULER',      label: 'スケジューラー'},
        {type: 'OTHER',          label: 'その他'},
    ];

    const state = {
        data: null,
        sidebarFilterText: '',
        displayType: 'all',
    };

    function prepareDiagramData(controller, usecaseData) {
        const entrypointFqns = new Set(controller.entrypoints.map(ep => ep.fqn));

        const usecaseMethodToType = new Map();
        if (usecaseData) {
            usecaseData.usecases.forEach(uc => {
                (uc.methods || []).forEach(m => usecaseMethodToType.set(m.fqn, uc.fqn));
            });
        }

        const allFqns = new Set(controller.entrypoints.map(ep => ep.fqn));
        controller.relations.forEach(edge => {
            allFqns.add(edge.from);
            allFqns.add(edge.to);
        });

        const entrypointGroups = new Map();
        controller.entrypoints.forEach(ep => {
            const typeFqn = ep.fqn.split('#')[0];
            Jig.util.pushToMap(entrypointGroups, typeFqn, ep);
        });

        const usecaseGroups = new Map();
        allFqns.forEach(fqn => {
            if (!entrypointFqns.has(fqn) && usecaseMethodToType.has(fqn)) {
                const typeFqn = usecaseMethodToType.get(fqn);
                Jig.util.pushToMap(usecaseGroups, typeFqn, fqn);
            }
        });

        const methodGroups = new Map();
        allFqns.forEach(fqn => {
            if (!entrypointFqns.has(fqn) && !usecaseMethodToType.has(fqn)) {
                const typeFqn = fqn.split('#')[0];
                Jig.util.pushToMap(methodGroups, typeFqn, fqn);
            }
        });

        const adapterFqns = new Set([...entrypointFqns]);
        allFqns.forEach(fqn => {
            if (!entrypointFqns.has(fqn) && !usecaseMethodToType.has(fqn)) {
                adapterFqns.add(fqn);
            }
        });

        const {edgeLengthByKey} = Jig.mermaid.graph.computeOutboundEdgeLengths({
            nodesInSubgraph: adapterFqns,
            edges: controller.relations
        });

        return {
            controller,
            entrypointFqns,
            usecaseMethodToType,
            allFqns,
            entrypointGroups,
            usecaseGroups,
            methodGroups,
            adapterFqns,
            edgeLengthByKey
        };
    }

    function buildDiagramBuilder(data, builder) {
        const fqnToNodeId = (fqn) => Jig.util.fqnToId("n", fqn);
        builder.applyThemeClassDefs();

        data.entrypointGroups.forEach((eps, typeFqn) => {
            const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), Jig.glossary.getTypeTerm(typeFqn).title);
            eps.forEach(ep => {
                const nodeId = fqnToNodeId(ep.fqn);
                builder.addNodeToSubgraph(subgraph, nodeId, Jig.glossary.getMethodTerm(ep.fqn, true).title, 'method');
                builder.addClass(nodeId, "inbound");
            });
        });

        const pathNodeShapes = {HTTP_API: 'request', QUEUE_LISTENER: 'queue', SCHEDULER: 'scheduler'};
        data.controller.entrypoints.forEach(ep => {
            const pathNodeId = Jig.util.fqnToId("path", ep.fqn);
            builder.addNode(pathNodeId, ep.path, pathNodeShapes[ep.entrypointType]);
            builder.addEdge(pathNodeId, fqnToNodeId(ep.fqn), "", true);
        });

        data.usecaseGroups.forEach((methods, typeFqn) => {
            const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), Jig.glossary.getTypeTerm(typeFqn).title);
            methods.forEach(fqn => {
                const mId = fqnToNodeId(fqn);
                builder.addNodeToSubgraph(subgraph, mId, Jig.glossary.getMethodTerm(fqn, true).title, 'method');
                builder.addClass(mId, "usecase");
                builder.addClick(mId, `./usecase.html#${Jig.util.fqnToId("method", fqn)}`);
            });
        });

        data.methodGroups.forEach((methods, typeFqn) => {
            const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), Jig.glossary.getTypeTerm(typeFqn).title);
            methods.forEach(fqn => {
                const nodeId = fqnToNodeId(fqn);
                builder.addNodeToSubgraph(subgraph, nodeId, Jig.glossary.getMethodTerm(fqn, true).title, 'method');
                builder.addClass(nodeId, "inactive");
            });
        });

        data.controller.relations.forEach(edge => {
            const edgeLength = data.edgeLengthByKey.get(`${edge.from}::${edge.to}`) || 1;
            builder.addEdge(fqnToNodeId(edge.from), fqnToNodeId(edge.to), "", false, edgeLength);
        });
    }

    function parseInboundData() {
        return Jig.data.inbound.get();
    }

    function init() {
        // モジュールキャッシュを再ロードしなくても状態がリセットされるよう明示的にクリア
        state.data = null;
        state.sidebarFilterText = '';
        state.displayType = 'all';

        state.data = parseInboundData();
        if (!state.data) {
            return;
        }

        Jig.dom.sidebar.initTextFilter('inbound-sidebar-filter', text => {
            state.sidebarFilterText = text;
            renderSidebar(filteredAdapters());
        });

        initDisplayTypeSettings();

        render();
    }

    function filteredAdapters() {
        const all = state.data.inboundAdapters || [];
        if (state.displayType === 'all') return all;
        return all.filter(c => c.entrypoints?.some(ep => ep.entrypointType === state.displayType));
    }

    function initDisplayTypeSettings() {
        const settingsEl = document.getElementById('sidebar-settings');
        const fieldset = document.getElementById('display-type-fieldset');
        if (!settingsEl || !fieldset) return;

        const types = new Set(
            (state.data.inboundAdapters || []).flatMap(c => (c.entrypoints || []).map(ep => ep.entrypointType)).filter(Boolean)
        );

        if (types.size <= 1) {
            settingsEl.style.display = 'none';
            return;
        }

        fieldset.innerHTML = "";
        const options = [
            {value: 'all', label: 'すべて'},
            ...TYPE_CONFIG.filter(c => types.has(c.type)).map(c => ({value: c.type, label: c.label})),
        ];

        options.forEach(({value, label}, idx) => {
            const radio = Jig.dom.createElement('input', {
                attributes: {type: 'radio', name: 'display-type', value}
            });
            if (idx === 0) radio.checked = true;
            radio.addEventListener('change', () => {
                if (radio.checked) {
                    state.displayType = value;
                    render();
                }
            });
            fieldset.appendChild(Jig.dom.createElement('label', {children: [radio, ' ' + label]}));
        });
    }

    function render() {
        const adapters = filteredAdapters();
        renderSidebar(adapters);
        renderMain(adapters);
    }

    function renderSidebar(adapters) {
        const sidebar = document.getElementById("inbound-sidebar-list");
        if (!sidebar) return;
        sidebar.innerHTML = "";

        Jig.dom.sidebar.renderSection(sidebar, null, [{id: "entrypoint-summary", label: "エントリーポイント一覧"}]);

        const filterText = state.sidebarFilterText.toLowerCase();

        for (const {type, label} of TYPE_CONFIG) {
            const items = adapters
                .filter(c => c.entrypoints?.some(ep => ep.entrypointType === type))
                .flatMap(c => {
                    const title = Jig.glossary.getTypeTerm(c.fqn).title;
                    if (filterText && !title.toLowerCase().includes(filterText)) return [];
                    return [{id: Jig.util.fqnToId(ADAPTER_ID_PREFIX,c.fqn), label: title}];
                });
            if (items.length === 0) continue;
            Jig.dom.sidebar.renderSection(sidebar, label, items);
        }
    }

    function splitHttpPath(path) {
        const spaceIdx = (path || '').indexOf(' ');
        return spaceIdx !== -1
            ? [path.slice(0, spaceIdx), path.slice(spaceIdx + 1)]
            : ['', path || ''];
    }

    function entrypointLabel(fqn) {
        const typeFqn = fqn.split('#')[0];
        return Jig.glossary.getTypeTerm(typeFqn).title + ' ' + Jig.glossary.getMethodTerm(fqn, true).title;
    }

    function linkCell(fqn, cardId) {
        return Jig.dom.createElement("td", {
            children: [Jig.dom.createElement("a", {
                textContent: entrypointLabel(fqn),
                attributes: {href: '#' + cardId}
            })]
        });
    }

    function buildTypeSubSection(title, headers, rows) {
        return Jig.dom.createElement("section", {
            className: "jig-card jig-card--item",
            children: [
                Jig.dom.createElement("h4", {textContent: title}),
                Jig.dom.createElement("table", {
                    className: "entrypoint-summary",
                    children: [
                        Jig.dom.createElement("thead", {
                            children: [Jig.dom.createElement("tr", {
                                children: headers.map(h => Jig.dom.createElement("th", {textContent: h}))
                            })]
                        }),
                        Jig.dom.createElement("tbody", {
                            children: rows.map(cells => Jig.dom.createElement("tr", {children: cells}))
                        })
                    ]
                })
            ]
        });
    }

    function buildHttpSubSection(rows) {
        const tbody = Jig.dom.createElement("tbody", {
            children: rows.map(cells => Jig.dom.createElement("tr", {children: cells}))
        });

        const filterInput = Jig.dom.createElement("input", {
            className: "entrypoint-filter",
            attributes: {type: "search", placeholder: "パスで絞り込み", autocomplete: "off"}
        });
        filterInput.addEventListener('input', () => {
            const text = filterInput.value.toLowerCase();
            for (const tr of tbody.children) {
                const path = (tr.children[0]?.textContent || '').toLowerCase();
                tr.style.display = text && !path.includes(text) ? 'none' : '';
            }
        });

        return Jig.dom.createElement("section", {
            className: "jig-card jig-card--item",
            children: [
                Jig.dom.createElement("h4", {textContent: 'リクエストハンドラ'}),
                filterInput,
                Jig.dom.createElement("table", {
                    className: "entrypoint-summary entrypoint-summary--http",
                    children: [
                        Jig.dom.createElement("thead", {
                            children: [Jig.dom.createElement("tr", {
                                children: ['パス', 'メソッド', 'エントリーポイント'].map(h => Jig.dom.createElement("th", {textContent: h}))
                            })]
                        }),
                        tbody
                    ]
                })
            ]
        });
    }

    function renderSummaryTable(adapters) {
        const typeRows = {};
        TYPE_CONFIG.forEach(({type}) => { typeRows[type] = []; });
        adapters.forEach(adapter => {
            const cardId = Jig.util.fqnToId(ADAPTER_ID_PREFIX, adapter.fqn);
            const classPath = adapter.classPath || '';
            (adapter.entrypoints || []).forEach(ep => {
                if (typeRows[ep.entrypointType]) typeRows[ep.entrypointType].push({ep, cardId, classPath});
            });
        });

        const subSections = [];

        if (typeRows.HTTP_API.length > 0) {
            const sorted = [...typeRows.HTTP_API].sort((a, b) => {
                const [, pathA] = splitHttpPath(a.ep.path);
                const [, pathB] = splitHttpPath(b.ep.path);
                return (a.classPath + pathA).localeCompare(b.classPath + pathB);
            });
            subSections.push(buildHttpSubSection(
                sorted.map(({ep, cardId, classPath}) => {
                    const [method, path] = splitHttpPath(ep.path);
                    return [Jig.dom.createCell(classPath + path), Jig.dom.createCell(method), linkCell(ep.fqn, cardId)];
                })
            ));
        }

        for (const {type, label} of TYPE_CONFIG.filter(c => c.type !== 'HTTP_API')) {
            if (typeRows[type].length === 0) continue;
            subSections.push(buildTypeSubSection(label,
                ['パス', 'エントリーポイント'],
                typeRows[type].map(({ep, cardId}) => [
                    Jig.dom.createCell(ep.path || ''),
                    linkCell(ep.fqn, cardId)
                ])
            ));
        }

        if (subSections.length === 0) return null;

        return Jig.dom.createElement("section", {
            className: "jig-card jig-card--type entrypoint-summary-section",
            id: "entrypoint-summary",
            children: [
                Jig.dom.createElement("h3", {textContent: "エントリーポイント一覧"}),
                ...subSections
            ]
        });
    }

    function renderMain(adapters) {
        const container = document.getElementById("inbound-list");
        if (!container) return;
        container.innerHTML = "";

        if (!adapters || adapters.length === 0) {
            container.textContent = "データなし";
            return;
        }

        const summaryCard = renderSummaryTable(adapters);
        if (summaryCard) container.appendChild(summaryCard);

        adapters.forEach(adapter => {
            const typeTerm = Jig.glossary.getTypeTerm(adapter.fqn);

            const jigCard = Jig.dom.card.type({
                id: Jig.util.fqnToId(ADAPTER_ID_PREFIX, adapter.fqn),
                title: typeTerm.title,
                fqn: adapter.fqn
            });

            if (typeTerm.description) {
                jigCard.appendChild(Jig.dom.createMarkdownElement(typeTerm.description));
            }

            if (adapter.classPath) {
                jigCard.appendChild(Jig.dom.createElement("div", {
                    className: "class-path",
                    textContent: adapter.classPath
                }));
            }

            const methodsList = Jig.dom.type.methodsList("エントリーポイント", adapter.entrypoints);
            if (methodsList) jigCard.appendChild(methodsList);

            const diagramGenerator = (dir) => {
                const data = prepareDiagramData(adapter, Jig.data.usecase.get());
                const builder = new Jig.mermaid.Builder();
                buildDiagramBuilder(data, builder);
                return builder.build(dir);
            };
            Jig.mermaid.diagram.createAndRegister(jigCard, (mmdContainer) => {
                Jig.mermaid.render.renderWithControls(mmdContainer, diagramGenerator, {direction: 'LR'});
            });

            container.appendChild(jigCard);
        });
    }

    return {
        init,
        parseInboundData,
        render,
        renderSidebar,
        renderSummaryTable,
        renderMain,
    };
})();

if (typeof document !== 'undefined') {
    document.addEventListener("DOMContentLoaded", () => {
        InboundApp.init();
    });
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = InboundApp;
}
