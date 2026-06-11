const InboundApp = (() => {
    const Jig = globalThis.Jig;

    const ADAPTER_ID_PREFIX = "adapter";
    const SIMPLIFIED_CLASS = 'entrypoint-section--simplified';

    const TYPE_CONFIG = [
        {type: 'HTTP_API',       label: 'リクエストハンドラ', headers: ['パス', 'メソッド', 'ハンドラ'],  filterPlaceholder: 'パスで絞り込み'},
        {type: 'QUEUE_LISTENER', label: 'メッセージリスナー', headers: ['購読先', 'ハンドラ'],            filterPlaceholder: '購読先で絞り込み'},
        {type: 'SCHEDULER',      label: 'スケジューラー',     headers: ['スケジュール', 'ハンドラ'],       filterPlaceholder: 'スケジュールで絞り込み'},
        {type: 'OTHER',          label: 'その他',             headers: ['パス', 'ハンドラ'],               filterPlaceholder: 'パスで絞り込み'},
    ];

    const INITIAL_STATE = {
        data: null,
        sidebarFilterText: '',
        displayType: 'all',
        simplified: false,
    };

    const state = {...INITIAL_STATE};

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

    function buildDiagramBuilder(data, builder, showPhysicalName = false) {
        const fqnToNodeId = (fqn) => Jig.util.fqnToId("n", fqn);
        const {type: typeLabel, method: mLabel} = Jig.glossary.makeLabels(showPhysicalName);

        data.entrypointGroups.forEach((eps, typeFqn) => {
            const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), typeLabel(typeFqn));
            eps.forEach(ep => {
                const nodeId = fqnToNodeId(ep.fqn);
                builder.addNodeToSubgraph(subgraph, nodeId, mLabel(ep.fqn), 'method');
                builder.addClass(nodeId, "inbound");
                builder.addTooltip(nodeId, ep.fqn);
            });
        });

        const pathNodeShapes = {HTTP_API: 'request', QUEUE_LISTENER: 'queue', SCHEDULER: 'scheduler'};
        data.controller.entrypoints.forEach(ep => {
            const pathNodeId = Jig.util.fqnToId("path", ep.fqn);
            builder.addNode(pathNodeId, ep.path, pathNodeShapes[ep.entrypointType]);
            builder.addEdge(pathNodeId, fqnToNodeId(ep.fqn), "", true);
        });

        data.usecaseGroups.forEach((methods, typeFqn) => {
            const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), typeLabel(typeFqn));
            methods.forEach(fqn => {
                const mId = fqnToNodeId(fqn);
                builder.addNodeToSubgraph(subgraph, mId, mLabel(fqn), 'method');
                builder.addClass(mId, "usecase");
                builder.addClick(mId, Jig.mermaid.nav.usecaseMethodUrl(fqn), fqn);
            });
        });

        data.methodGroups.forEach((methods, typeFqn) => {
            const subgraph = builder.startSubgraph(Jig.util.fqnToId("sg", typeFqn), typeLabel(typeFqn));
            methods.forEach(fqn => {
                const nodeId = fqnToNodeId(fqn);
                builder.addNodeToSubgraph(subgraph, nodeId, mLabel(fqn), 'method');
                builder.addClass(nodeId, "inactive");
                builder.addTooltip(nodeId, fqn);
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
        Object.assign(state, INITIAL_STATE);

        state.data = parseInboundData();
        if (!state.data) {
            return;
        }

        Jig.dom.sidebar.initCollapseBtn();
        Jig.dom.sidebar.initTextFilter('inbound-sidebar-filter', text => {
            state.sidebarFilterText = text;
            renderSidebar(filteredAdapters());
        });

        initDisplayTypeSettings();
        initSimplifiedSetting();
        initIoTypeLinkResolver();

        render();
    }

    function filteredAdapters() {
        const all = state.data.inboundAdapters || [];
        if (state.displayType === 'all') return all;
        return all.filter(c => c.entrypoints?.some(ep => ep.entrypointType === state.displayType));
    }

    function initDisplayTypeSettings() {
        const fieldset = document.getElementById('display-type-fieldset');
        if (!fieldset) return;

        const types = new Set(
            (state.data.inboundAdapters || []).flatMap(c => (c.entrypoints || []).map(ep => ep.entrypointType)).filter(Boolean)
        );

        if (types.size <= 1) {
            fieldset.style.display = 'none';
            return;
        }

        fieldset.innerHTML = "";
        fieldset.appendChild(Jig.dom.i18nText('legend', '表示種別'));
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
            fieldset.appendChild(Jig.dom.createElement('label', {
                children: [radio, document.createTextNode(' '), Jig.dom.i18nText('span', label)]
            }));
        });
    }

    function initSimplifiedSetting() {
        const panel = document.querySelector('.sidebar-settings-panel');
        if (!panel) return;

        const existing = document.getElementById('simplified-toggle');
        if (existing) {
            existing.checked = false;
            return;
        }

        const checkbox = Jig.dom.createElement('input', {
            attributes: {type: 'checkbox', id: 'simplified-toggle'}
        });
        checkbox.addEventListener('change', () => {
            state.simplified = checkbox.checked;
            renderMain(filteredAdapters());
        });
        panel.appendChild(Jig.dom.createElement('label', {
            attributes: {for: 'simplified-toggle'},
            children: [checkbox, document.createTextNode(' '), Jig.dom.i18nText('span', '簡略表示')]
        }));
    }

    function initIoTypeLinkResolver() {
        const ioTypeFqns = new Set((state.data.ioTypes || []).map(t => t.fqn));
        const baseResolver = Jig.data.createTypeLinkResolver();
        Jig.dom.type.setResolver(fqn => {
            if (ioTypeFqns.has(fqn)) return {href: '#' + Jig.util.fqnToId('io-type', fqn)};
            return baseResolver ? baseResolver(fqn) : null;
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

        Jig.dom.sidebar.renderSection(sidebar, null, [
            {id: "entrypoint-summary", label: "エントリーポイント一覧"},
            {id: "io-types", label: "入出力オブジェクト一覧"},
        ]);

        const filterText = state.sidebarFilterText.toLowerCase();

        const filteredAdapters = adapters.filter(adapter => {
            if (!filterText) return true;
            return Jig.glossary.getTypeTerm(adapter.fqn).title.toLowerCase().includes(filterText);
        });
        const byPackage = Jig.util.groupByPackageFqn(filteredAdapters, adapter => adapter.fqn);

        Jig.dom.sidebar.renderPackageGrouped(sidebar, byPackage, pkgAdapters =>
            pkgAdapters.map(adapter =>
                Jig.dom.createElement("li", {
                    className: "in-page-sidebar__item",
                    children: [
                        Jig.dom.createElement("a", {
                            className: "in-page-sidebar__link",
                            attributes: {href: "#" + Jig.util.fqnToId(ADAPTER_ID_PREFIX, adapter.fqn)},
                            textContent: Jig.glossary.getTypeTerm(adapter.fqn).title
                        })
                    ]
                })
            )
        );
    }

    function buildEntrypointItem(ep) {
        const methodTerm = Jig.glossary.getMethodTerm(ep.fqn, true);

        return Jig.dom.createElement("div", {
            className: "entrypoint-item",
            children: [
                Jig.dom.createElement("div", {
                    className: "entrypoint-item__header",
                    children: [
                        Jig.dom.createElement("span", {
                            className: "entrypoint-item__name" + (ep.isDeprecated ? " deprecated" : ""),
                            textContent: methodTerm.title
                        }),
                        Jig.dom.createElement("span", {
                            className: "entrypoint-item__path",
                            textContent: ep.path || ''
                        })
                    ]
                }),
                Jig.dom.type.methodIOSection(ep.parameters || [], ep.returnTypeRef)
            ]
        });
    }

    function splitHttpPath(path) {
        const spaceIdx = (path || '').indexOf(' ');
        return spaceIdx !== -1
            ? [path.slice(0, spaceIdx), path.slice(spaceIdx + 1)]
            : ['', path || ''];
    }

    function methodLinkCell(fqn, cardId) {
        return Jig.dom.createElement("td", {
            children: [Jig.dom.createElement("a", {
                textContent: Jig.glossary.getMethodTerm(fqn, true).title,
                attributes: {href: '#' + cardId}
            })]
        });
    }

    function buildGroupedSubSection(title, headers, groups, tableClass = '', filterPlaceholder = '絞り込み') {
        const table = Jig.dom.createElement("table", {
            className: ["entrypoint-summary", tableClass].filter(Boolean).join(" "),
            children: [
                Jig.dom.createElement("thead", {
                    children: [Jig.dom.createElement("tr", {
                        children: headers.map(h => Jig.dom.i18nText("th", h))
                    })]
                })
            ]
        });

        const groupEntries = [];

        groups.forEach(({adapterFqn, cardId, classPath, rows}) => {
            const dataRows = rows.map(cells => Jig.dom.createElement("tr", {children: cells}));
            const tbody = Jig.dom.createElement("tbody");

            const toggleBtn = Jig.dom.createElement("button", {
                className: "controller-group-toggle",
                attributes: {"aria-expanded": "true", "aria-label": Jig.i18n.t("折りたたむ")}
            });
            toggleBtn.addEventListener("click", () => {
                const collapsing = toggleBtn.getAttribute("aria-expanded") === "true";
                toggleBtn.setAttribute("aria-expanded", String(!collapsing));
                toggleBtn.setAttribute("aria-label", Jig.i18n.t(collapsing ? "展開" : "折りたたむ"));
                dataRows.forEach(tr => tr.classList.toggle("hidden", collapsing));
            });

            const controllerLabel = Jig.glossary.getTypeTerm(adapterFqn).title;
            const headerChildren = [
                Jig.dom.createElement("a", {
                    textContent: controllerLabel,
                    attributes: {href: '#' + cardId}
                }),
            ];
            if (classPath) {
                headerChildren.push(Jig.dom.createElement("span", {
                    className: "controller-group-path",
                    textContent: classPath
                }));
            }
            headerChildren.push(Jig.dom.createElement("span", {
                className: "controller-group-count",
                textContent: rows.length
            }));
            headerChildren.push(toggleBtn);
            const headerRow = Jig.dom.createElement("tr", {
                className: "controller-group-header",
                children: [Jig.dom.createElement("td", {
                    attributes: {colspan: String(headers.length)},
                    children: [Jig.dom.createElement("div", {
                        className: "controller-group-header__inner",
                        children: headerChildren
                    })]
                })]
            });

            tbody.appendChild(headerRow);
            dataRows.forEach(tr => tbody.appendChild(tr));
            table.appendChild(tbody);
            groupEntries.push({headerRow, dataRows, toggleBtn});
        });

        const totalRows = groupEntries.reduce((sum, {dataRows}) => sum + dataRows.length, 0);
        const autoCollapse = totalRows > 10;
        if (autoCollapse) {
            for (const {dataRows, toggleBtn} of groupEntries) {
                dataRows.forEach(tr => tr.classList.add("hidden"));
                toggleBtn.setAttribute("aria-expanded", "false");
                toggleBtn.setAttribute("aria-label", Jig.i18n.t("展開"));
            }
        }

        const filterInput = Jig.dom.createElement("input", {
            className: "entrypoint-filter",
            attributes: {type: "search", placeholder: filterPlaceholder, autocomplete: "off", "data-i18n-attr": "placeholder"}
        });
        filterInput.addEventListener('input', () => {
            const text = filterInput.value.toLowerCase();
            for (const {headerRow, dataRows} of groupEntries) {
                let anyVisible = false;
                for (const tr of dataRows) {
                    const path = (tr.children[0].textContent || '').toLowerCase();
                    const hidden = text && !path.includes(text);
                    tr.style.display = hidden ? 'none' : '';
                    if (!hidden) anyVisible = true;
                }
                headerRow.style.display = text && !anyVisible ? 'none' : '';
            }
        });

        const card = Jig.dom.card.item({title});

        if (groups.length > 1) {
            const allToggleBtn = Jig.dom.createElement("button", {
                className: "controller-group-toggle-all",
                attributes: {"aria-expanded": String(!autoCollapse)},
                children: [Jig.dom.i18nText("span", autoCollapse ? "全て展開" : "全て折りたたむ")]
            });
            allToggleBtn.addEventListener("click", () => {
                const collapsing = allToggleBtn.getAttribute("aria-expanded") === "true";
                allToggleBtn.setAttribute("aria-expanded", String(!collapsing));
                const newKey = collapsing ? "全て展開" : "全て折りたたむ";
                const span = allToggleBtn.querySelector("span");
                span.textContent = Jig.i18n.t(newKey);
                span.setAttribute("data-i18n", newKey);
                for (const {toggleBtn, dataRows} of groupEntries) {
                    toggleBtn.setAttribute("aria-expanded", String(!collapsing));
                    toggleBtn.setAttribute("aria-label", Jig.i18n.t(collapsing ? "展開" : "折りたたむ"));
                    dataRows.forEach(tr => tr.classList.toggle("hidden", collapsing));
                }
            });
            card.appendChild(Jig.dom.createElement("div", {
                className: "entrypoint-filter-row",
                children: [filterInput, allToggleBtn]
            }));
        } else {
            card.appendChild(filterInput);
        }

        card.appendChild(table);
        return card;
    }

    function renderSummaryTable(adapters) {
        const typeRows = {};
        TYPE_CONFIG.forEach(({type}) => { typeRows[type] = []; });
        adapters.forEach(adapter => {
            const cardId = Jig.util.fqnToId(ADAPTER_ID_PREFIX, adapter.fqn);
            const classPath = adapter.classPath || '';
            (adapter.entrypoints || []).forEach(ep => {
                if (typeRows[ep.entrypointType]) typeRows[ep.entrypointType].push({ep, cardId, classPath, adapterFqn: adapter.fqn});
            });
        });

        const subSections = [];

        const {label: httpLabel, headers: httpHeaders, filterPlaceholder: httpFilterPlaceholder} = TYPE_CONFIG.find(c => c.type === 'HTTP_API');
        if (typeRows.HTTP_API.length > 0) {
            const sorted = [...typeRows.HTTP_API].sort((a, b) => {
                const [, pathA] = splitHttpPath(a.ep.path);
                const [, pathB] = splitHttpPath(b.ep.path);
                return (a.classPath + pathA).localeCompare(b.classPath + pathB);
            });

            const groupMap = new Map();
            sorted.forEach(({ep, cardId, classPath, adapterFqn}) => {
                if (!groupMap.has(adapterFqn)) {
                    groupMap.set(adapterFqn, {adapterFqn, cardId, classPath, rows: []});
                }
                const [method, path] = splitHttpPath(ep.path);
                groupMap.get(adapterFqn).rows.push(
                    [Jig.dom.createCell(classPath + path), Jig.dom.createCell(method), methodLinkCell(ep.fqn, cardId)]
                );
            });

            subSections.push(buildGroupedSubSection(httpLabel, httpHeaders, [...groupMap.values()], 'entrypoint-summary--http', httpFilterPlaceholder));
        }

        for (const {type, label, headers, filterPlaceholder} of TYPE_CONFIG.filter(c => c.type !== 'HTTP_API')) {
            if (typeRows[type].length === 0) continue;
            const groupMap = new Map();
            typeRows[type].forEach(({ep, cardId, classPath, adapterFqn}) => {
                if (!groupMap.has(adapterFqn)) {
                    groupMap.set(adapterFqn, {adapterFqn, cardId, classPath, rows: []});
                }
                groupMap.get(adapterFqn).rows.push(
                    [Jig.dom.createCell(ep.path || ''), methodLinkCell(ep.fqn, cardId)]
                );
            });
            subSections.push(buildGroupedSubSection(label, headers, [...groupMap.values()], '', filterPlaceholder));
        }

        if (subSections.length === 0) return null;

        const section = Jig.dom.card.type({id: "entrypoint-summary", title: "エントリーポイント一覧", extraClass: "entrypoint-summary-section"});
        subSections.forEach(s => section.appendChild(s));
        return section;
    }

    function renderMain(adapters) {
        const container = document.getElementById("inbound-list");
        if (!container) return;
        container.innerHTML = "";
        const usecaseData = Jig.data.usecase.get();

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
                fqn: adapter.fqn,
                titleSuffix: Jig.glossary.sourceLink(adapter.fqn)
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

            if (adapter.entrypoints && adapter.entrypoints.length > 0) {
                const epSection = Jig.dom.card.item({title: "エントリーポイント", extraClass: "entrypoint-section"});

                if (state.simplified) {
                    epSection.classList.add(SIMPLIFIED_CLASS);
                }

                const btnSpan = Jig.dom.i18nText('span', '簡略表示');
                const toggleBtn = Jig.dom.createElement('button', {
                    className: 'simplified-toggle-btn',
                    attributes: {'aria-pressed': String(state.simplified)},
                    children: [btnSpan]
                });
                toggleBtn.addEventListener('click', () => {
                    epSection.classList.toggle(SIMPLIFIED_CLASS);
                    const isNowSimplified = epSection.classList.contains(SIMPLIFIED_CLASS);
                    toggleBtn.setAttribute('aria-pressed', String(isNowSimplified));
                });
                epSection.querySelector('h4').appendChild(toggleBtn);

                adapter.entrypoints.forEach(ep => epSection.appendChild(buildEntrypointItem(ep)));
                jigCard.appendChild(epSection);
            }

            Jig.mermaid.diagram.createAndRegister(jigCard, (mmdContainer) => {
                const diagramGenerator = (dir, opts) => {
                    const data = prepareDiagramData(adapter, usecaseData);
                    const builder = Jig.mermaid.createBuilder();
                    buildDiagramBuilder(data, builder, opts?.showPhysicalName);
                    return builder.build(dir);
                };
                Jig.mermaid.render.renderWithControls(mmdContainer, diagramGenerator, {direction: 'LR', enableLabelToggle: true});
            });

            container.appendChild(jigCard);
        });

        const ioTypesSection = renderIoTypesSection(state.data.ioTypes || [], state.data.rootIoTypeFqns || []);
        if (ioTypesSection) container.appendChild(ioTypesSection);
    }

    function renderIoTypesSection(ioTypes, rootIoTypeFqns) {
        if (ioTypes.length === 0) return null;

        const ioTypeMap = new Map(ioTypes.map(t => [t.fqn, t]));
        const roots = rootIoTypeFqns.filter(fqn => ioTypeMap.has(fqn));
        if (roots.length === 0) return null;

        const section = Jig.dom.card.type({id: "io-types", title: "入出力オブジェクト一覧", extraClass: "io-types-section"});

        roots.forEach(rootFqn => {
            const typeTerm = Jig.glossary.getTypeTerm(rootFqn);
            const card = Jig.dom.card.type({
                id: Jig.util.fqnToId('io-type', rootFqn),
                title: typeTerm.title,
                fqn: rootFqn,
                titleSuffix: Jig.glossary.sourceLink(rootFqn),
                extraClass: 'io-type-card',
            });

            const {panels, section: tabSection} = Jig.dom.tab.buildSection(
                [{id: 'fields', label: 'フィールド'}, {id: 'diagram', label: 'クラス図'}],
                {className: "jig-card-section tab-content-section tab-io-section"}
            );
            card.appendChild(tabSection);

            // フィールド展開タブ: カード内のフィールド型はすぐ下にネスト展開があるためリンク不要
            const savedResolver = Jig.dom.type.getResolver();
            Jig.dom.type.setResolver(null);
            try {
                appendIoTypeExpanded(panels['fields'], rootFqn, ioTypeMap, new Set([rootFqn]));
            } finally {
                Jig.dom.type.setResolver(savedResolver);
            }

            // クラス図タブ
            Jig.mermaid.diagram.createAndRegister(panels['diagram'], (mmdContainer) => {
                Jig.mermaid.render.renderWithControls(mmdContainer, (dir, opts) => buildIoTypeClassDiagramCode(rootFqn, ioTypeMap, dir, opts?.showPhysicalName), {direction: 'TB', enableLabelToggle: true});
            });

            section.appendChild(card);
        });

        return section;
    }

    function appendIoTypeExpanded(container, fqn, ioTypeMap, visitedInBranch) {
        const ioType = ioTypeMap.get(fqn);
        if (!ioType) return;

        // フィールドごとに「フィールド行 → そのネスト型展開」を逐次処理する
        (ioType.fields || []).forEach(field => {
            container.appendChild(Jig.dom.type.fieldItem(field));

            collectIoFqnsFromTypeRef(field.typeRef, ioTypeMap).forEach(nestedFqn => {
                if (visitedInBranch.has(nestedFqn)) return;
                visitedInBranch.add(nestedFqn);

                const nestedSection = Jig.dom.createElement('div', {className: 'io-type-nested'});
                nestedSection.appendChild(Jig.dom.createElement('span', {
                    className: 'io-type-nested-label',
                    textContent: Jig.glossary.getTypeTerm(nestedFqn).title,
                }));
                appendIoTypeExpanded(nestedSection, nestedFqn, ioTypeMap, visitedInBranch);
                container.appendChild(nestedSection);
            });
        });
    }

    function collectIoFqnsFromTypeRef(typeRef, ioTypeMap) {
        return Jig.util.collectTypeRefFqns(typeRef).filter(fqn => ioTypeMap.has(fqn));
    }

    function buildIoTypeClassDiagramCode(rootFqn, ioTypeMap, dir = 'LR', showPhysicalName = false) {
        const {type: typeLabel} = Jig.glossary.makeLabels(showPhysicalName);
        const builder = new Jig.mermaid.ClassDiagramBuilder();
        const visited = new Set();

        function typeRefToLabel(typeRef) {
            if (!typeRef) return '';
            const name = typeLabel(typeRef.fqn);
            if (!typeRef.typeArgumentRefs || typeRef.typeArgumentRefs.length === 0) return name;
            return `${name}~${typeRef.typeArgumentRefs.map(a => typeRefToLabel(a)).join(', ')}~`;
        }

        function traverse(fqn) {
            if (visited.has(fqn)) return;
            visited.add(fqn);
            const ioType = ioTypeMap.get(fqn);
            if (!ioType) return;

            const classId = Jig.util.fqnToId('io', fqn);
            builder.addClass(classId, typeLabel(fqn));

            (ioType.fields || []).forEach(field => {
                builder.addField(classId, typeRefToLabel(field.typeRef), field.name || '');
                collectIoFqnsFromTypeRef(field.typeRef, ioTypeMap).forEach(nestedFqn => {
                    const nestedClassId = Jig.util.fqnToId('io', nestedFqn);
                    traverse(nestedFqn);
                    builder.addEdge(classId, nestedClassId, 'association');
                });
            });
        }

        traverse(rootFqn);
        return builder.build(dir);
    }

    return {
        init,
        parseInboundData,
        render,
        renderSidebar,
        renderSummaryTable,
        renderMain,
        renderIoTypesSection,
    };
})();

Jig.bootstrap.register("inbound-interface", InboundApp.init);

if (typeof module !== "undefined" && module.exports) {
    module.exports = InboundApp;
}
