const OutboundApp = (() => {
    const Jig = globalThis.Jig;

    const DEFAULT_VISIBILITY = {
        port: true, operation: true,
        adapter: true, execution: true,
        accessor: false, accessorMethod: false,
        target: true,
        externalAccessor: false, externalAccessorMethod: false,
        externalType: true, externalTypeMethod: true,
        direction: 'LR',
        crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
    };

    const state = {
        visibility: null,
        activeTab: 'outbound',
        data: null,
        grouped: null,
        persistenceGrouped: null,
        externalGrouped: null
    };

    function loadData() {
        return Jig.data.outbound.get() || {
            outboundPorts: [],
            outboundAdapters: [],
            persistenceAccessors: [],
            otherExternalAccessors: [],
            targets: [],
            links: {
                operationToExecution: [],
                executionToPersistenceAccessor: [],
                executionToOtherExternalAccessor: []
            }
        };
    }

    function buildModel(data) {
        const grouped = groupOperationsByOutboundPort(data);
        const allOperations = grouped.flatMap(group =>
            group.operations.map(operation => ({...operation, outboundPort: group.outboundPort})));

        const operationExternalGrouped = groupOperationsByExternalType(allOperations);
        const directExternalGrouped = groupDirectExternalAccessors(data);
        const externalGroupedMap = new Map();
        operationExternalGrouped.forEach(g => externalGroupedMap.set(g.externalType.fqn, {...g, directAccessors: []}));
        directExternalGrouped.forEach(g => {
            if (externalGroupedMap.has(g.externalType.fqn)) {
                externalGroupedMap.get(g.externalType.fqn).directAccessors = g.directAccessors;
            } else {
                externalGroupedMap.set(g.externalType.fqn, {...g, operations: []});
            }
        });
        const externalGrouped = Array.from(externalGroupedMap.values())
            .sort((a, b) => Jig.glossary.getTypeTerm(a.externalType.fqn).title.localeCompare(Jig.glossary.getTypeTerm(b.externalType.fqn).title, "ja"));

        return {
            grouped,
            persistenceGrouped: groupOperationsByPersistenceTarget(allOperations),
            externalGrouped
        };
    }

    function groupOperationsByOutboundPort(data) {
        const executionByFqn = new Map();
        data.outboundAdapters.forEach(adapter => {
            adapter.executions.forEach(exec => {
                executionByFqn.set(exec.fqn, {exec, adapter});
            });
        });

        const methodById = new Map();
        data.persistenceAccessors.forEach(accessor => {
            accessor.methods.forEach(method => {
                methodById.set(method.id, {...method, group: accessor.fqn});
            });
        });

        const executionByOperation = new Map();
        data.links.operationToExecution.forEach(link => {
            executionByOperation.set(link.operation, link.execution);
        });

        const accessorsByExecution = new Map();
        data.links.executionToPersistenceAccessor.forEach(link => {
            if (!accessorsByExecution.has(link.execution)) {
                accessorsByExecution.set(link.execution, []);
            }
            accessorsByExecution.get(link.execution).push(link.accessor);
        });

        const externalAccessorByFqn = new Map();
        data.otherExternalAccessors.forEach(a => externalAccessorByFqn.set(a.fqn, a));

        const externalAccessorsByExecution = new Map();
        data.links.executionToOtherExternalAccessor.forEach(link => {
            if (!externalAccessorsByExecution.has(link.execution))
                externalAccessorsByExecution.set(link.execution, new Map());
            const accessorMethods = externalAccessorsByExecution.get(link.execution);
            if (!accessorMethods.has(link.accessor))
                accessorMethods.set(link.accessor, new Set());
            accessorMethods.get(link.accessor).add(link.method);
        });

        return data.outboundPorts.map(port => {
            const operations = port.operations.flatMap(op => {
                const execFqn = executionByOperation.get(op.fqn);
                if (!execFqn) return [];
                const execEntry = executionByFqn.get(execFqn);
                const accessorIds = accessorsByExecution.get(execFqn) || [];
                const persistenceAccessors = accessorIds.map(id => methodById.get(id)).filter(Boolean);
                const executionAccessorMethods = externalAccessorsByExecution.get(execFqn) || new Map();
                const externalAccessors = Array.from(executionAccessorMethods.entries()).flatMap(([fqn, methodNames]) => {
                    const accessor = externalAccessorByFqn.get(fqn);
                    if (!accessor) return [];
                    return [{...accessor, operations: accessor.operations.filter(m => methodNames.has(m.fqn.split('#')[1]?.split('(')[0]))}];
                });
                return [{
                    outboundPortOperation: op,
                    outboundAdapter: execEntry?.adapter ?? null,
                    outboundAdapterExecution: execEntry?.exec ?? null,
                    persistenceAccessors,
                    externalAccessors
                }];
            }).sort((a, b) => {
                const left = Jig.glossary.getMethodTerm(a.outboundPortOperation.fqn).title;
                const right = Jig.glossary.getMethodTerm(b.outboundPortOperation.fqn).title;
                return left.localeCompare(right, "ja");
            });
            return {outboundPort: port, operations};
        }).filter(group => group.operations.length > 0)
            .sort((a, b) => {
                const left = Jig.glossary.getTypeTerm(a.outboundPort.fqn).title;
                const right = Jig.glossary.getTypeTerm(b.outboundPort.fqn).title;
                return left.localeCompare(right, "ja");
            });
    }

    function groupOperationsByPersistenceTarget(operations) {
        const map = new Map();
        operations.forEach(operation => {
            operation.persistenceAccessors.forEach(op => {
                Object.keys(op.targetOperationTypes).forEach(persistenceTarget => {
                    if (!map.has(persistenceTarget)) {
                        map.set(persistenceTarget, {
                            persistenceTarget: persistenceTarget,
                            operations: [],
                        });
                    }
                    const group = map.get(persistenceTarget);
                    if (!group.operations.includes(operation)) {
                        group.operations.push(operation);
                    }
                });
            });
        });
        return Array.from(map.values()).map(group => {
            group.operations.sort((a, b) => {
                const left = Jig.glossary.getTypeTerm(a.outboundPort.fqn).title;
                const right = Jig.glossary.getTypeTerm(b.outboundPort.fqn).title;
                return left.localeCompare(right, "ja");
            });
            return group;
        }).sort((a, b) => {
            return a.persistenceTarget.localeCompare(b.persistenceTarget, "ja");
        });
    }

    function groupOperationsByExternalType(operations) {
        const map = new Map();
        operations.forEach(operation => {
            operation.externalAccessors.forEach(accessor => {
                accessor.operations.forEach(accMethod => {
                    accMethod.externals.forEach(ext => {
                        if (!map.has(ext.fqn)) map.set(ext.fqn, {externalType: {fqn: ext.fqn}, operations: []});
                        const group = map.get(ext.fqn);
                        if (!group.operations.includes(operation)) group.operations.push(operation);
                    });
                });
            });
        });
        return Array.from(map.values())
            .sort((a, b) => Jig.glossary.getTypeTerm(a.externalType.fqn).title.localeCompare(Jig.glossary.getTypeTerm(b.externalType.fqn).title, "ja"));
    }

    function groupDirectExternalAccessors(data) {
        const map = new Map();
        data.otherExternalAccessors.forEach(accessor => {
            accessor.operations.forEach(method => {
                method.externals.forEach(ext => {
                    if (!map.has(ext.fqn)) {
                        map.set(ext.fqn, {externalType: {fqn: ext.fqn}, directAccessors: []});
                    }
                    const group = map.get(ext.fqn);
                    let existing = group.directAccessors.find(a => a.fqn === accessor.fqn);
                    if (!existing) {
                        existing = {fqn: accessor.fqn, operations: []};
                        group.directAccessors.push(existing);
                    }
                    if (!existing.operations.find(m => m.fqn === method.fqn)) {
                        existing.operations.push({
                            ...method,
                            externals: method.externals.filter(e => e.fqn === ext.fqn)
                        });
                    }
                });
            });
        });
        return Array.from(map.values());
    }

    function formatPersistenceAccessors(persistenceAccessors) {
        if (!Array.isArray(persistenceAccessors) || persistenceAccessors.length === 0) {
            return ["なし"];
        }
        return persistenceAccessors
            .map(operation => {
                const operationTypes = Object.entries(operation.targetOperationTypes)
                    .map(([persistenceTarget, operationType]) => `${operationType}:${persistenceTarget}`)
                    .join(", ")
                return `${operation.id} [${operationTypes}]`.trim();
            });
    }

    function toCrudChar(operationType) {
        const type = (operationType || "").toUpperCase();
        if (type === "SELECT") return "R";
        if (type === "INSERT") return "C";
        if (type === "UPDATE") return "U";
        if (type === "DELETE") return "D";
        return "";
    }

    function isCrudVisible(operationType, visibility = state.visibility || DEFAULT_VISIBILITY) {
        switch ((operationType || "").toUpperCase()) {
            case 'INSERT':
                return visibility.crudCreate !== false;
            case 'SELECT':
                return visibility.crudRead !== false;
            case 'UPDATE':
                return visibility.crudUpdate !== false;
            case 'DELETE':
                return visibility.crudDelete !== false;
            default:
                return true;
        }
    }

    function collectAllTargets(grouped) {
        const targetsSet = new Set();
        grouped.forEach(group => {
            group.operations.forEach(operation => {
                operation.persistenceAccessors.forEach(op => {
                    Object.keys(op.targetOperationTypes).forEach(persistenceTarget => targetsSet.add(persistenceTarget));
                });
            });
        });
        return Array.from(targetsSet).sort();
    }

    function render() {
        updateTabs(state.activeTab);
        renderAllPanels();
    }

    function updateTabs(activeTab) {
        document.querySelectorAll('.outbound-tab-list .tab-button').forEach(btn => {
            btn.classList.toggle('is-active', btn.getAttribute('data-tab') === activeTab);
        });
        document.querySelectorAll('.outbound-tab-panel').forEach(panel => {
            panel.classList.toggle('is-active', panel.id === `${activeTab}-tab-panel`);
        });
    }

    function renderAllPanels() {
        const {visibility, data, grouped, persistenceGrouped, externalGrouped} = state;
        if (!data) return;

        renderPersistenceList(persistenceGrouped, visibility);
        renderExternalList(externalGrouped, visibility);
        renderOutboundList(grouped, visibility);
        renderCrudTable(grouped);
    }

    function renderOutboundList(grouped, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const container = document.getElementById("outbound-port-list");
        const sidebar = document.getElementById("outbound-sidebar-list");
        if (!container) return;
        container.innerHTML = "";
        if (sidebar) sidebar.innerHTML = "";

        grouped.forEach(group => {
            const portMermaidCode = generatePortMermaidCode(group, visibility);
            if (!portMermaidCode) return;
            const portFqnValue = group.outboundPort.fqn;
            const portId = Jig.util.fqnToId("port", portFqnValue);
            const portLabel = Jig.glossary.getTypeTerm(portFqnValue).title;

            const portCard = Jig.dom.card.type({
                id: portId,
                title: portLabel,
                fqn: portFqnValue,
            });

            if (visibility.adapter) {
                const adapterLabels = Array.from(new Set(group.operations.map(operation => {
                    const fqn = operation.outboundAdapter?.fqn ?? "";
                    const label = Jig.glossary.getTypeTerm(fqn).title;
                    return label + (label !== fqn ? ` (${fqn})` : "");
                })));
                if (adapterLabels.length > 0) {
                    portCard.appendChild(Jig.dom.createElement("p", {
                        className: "weak",
                        textContent: "Implementation: " + adapterLabels.join(", ")
                    }));
                }
            }

            portCard.appendChild(Jig.dom.createElement("p", {
                className: "weak",
                textContent: `${group.operations.length} operations`
            }));

            Jig.mermaid.diagram.createAndRegister(portCard, (container) => {
                const currentVisibility = readVisibility();
                const generator = (dir) => generatePortMermaidCode(group, {...currentVisibility, direction: dir});
                if (generator(currentVisibility.direction)) {
                    renderMermaid(generator, container, {direction: currentVisibility.direction});
                }
            }, {className: "mermaid-diagram port-diagram"});

            const itemList = Jig.dom.createElement("div", {className: "outbound-operation-list"});
            group.operations.forEach(operation => {
                const mermaidContainer = Jig.dom.createElement("div", {className: "mermaid-diagram"});
                const operationWithPort = {...operation, outboundPort: group.outboundPort};
                Jig.mermaid.diagram.register(mermaidContainer, () => {
                    const currentVisibility = readVisibility();
                    const generator = (dir) => generateOperationMermaidCode(operationWithPort, {
                        ...currentVisibility,
                        direction: dir
                    });
                    if (generator(currentVisibility.direction)) {
                        renderMermaid(generator, mermaidContainer, {direction: currentVisibility.direction});
                    }
                });

                const operationItem = Jig.dom.card.item({tagName: "article", extraClass: "outbound-operation-item"});
                operationItem.appendChild(Jig.dom.type.methodItem(operation.outboundPortOperation));
                operationItem.appendChild(mermaidContainer);
                operationItem.appendChild(Jig.dom.createElement("p", {className: "outbound-persistence-detail-title", textContent: "永続化操作"}));
                operationItem.appendChild(Jig.dom.createElement("ul", {
                    className: "outbound-persistence-detail-list",
                    children: formatPersistenceAccessors(operation.persistenceAccessors).map(text => Jig.dom.createElement("li", {textContent: text}))
                }));
                itemList.appendChild(operationItem);
            });
            const itemListDetails = Jig.dom.createElement("details", {});
            const itemListSummary = Jig.dom.createElement("summary", {
                className: "outbound-operation-list-summary",
                textContent: `操作別詳細 (${group.operations.length}件)`
            });
            itemListDetails.appendChild(itemListSummary);
            itemListDetails.appendChild(itemList);
            portCard.appendChild(itemListDetails);

            container.appendChild(portCard);
        });

        Jig.dom.sidebar.renderSection(sidebar, "出力ポート", grouped.map(group => ({
            id: Jig.util.fqnToId("port", group.outboundPort.fqn),
            label: Jig.glossary.getTypeTerm(group.outboundPort.fqn).title
        })));

        if (grouped.length === 0) renderNoData(container);
    }

    function renderPersistenceList(grouped, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const container = document.getElementById("outbound-persistence-list");
        const sidebar = document.getElementById("persistence-sidebar-list");
        if (!container) return;
        container.innerHTML = "";
        if (sidebar) sidebar.innerHTML = "";

        grouped.forEach(group => {
            const persistenceMermaidCode = generatePersistenceMermaidCode(group, visibility);
            if (!persistenceMermaidCode) return;
            const targetId = Jig.util.fqnToId("persistence", group.persistenceTarget);

            const persistenceMermaidContainer = Jig.dom.createElement("div", {className: "mermaid-diagram port-diagram"});
            Jig.mermaid.diagram.register(persistenceMermaidContainer, () => {
                const currentVisibility = readVisibility();
                const generator = (dir) => generatePersistenceMermaidCode(group, {...currentVisibility, direction: dir});
                if (generator(currentVisibility.direction)) {
                    renderMermaid(generator, persistenceMermaidContainer, {direction: currentVisibility.direction});
                }
            });

            const persistenceCard = Jig.dom.card.type({
                id: targetId,
                title: group.persistenceTarget,
            });
            persistenceCard.appendChild(persistenceMermaidContainer);
            container.appendChild(persistenceCard);
        });

        Jig.dom.sidebar.renderSection(sidebar, "永続化操作対象", grouped.map(group => ({
            id: Jig.util.fqnToId("persistence", group.persistenceTarget),
            label: group.persistenceTarget
        })));

        if (grouped.length === 0) renderNoData(container);
    }

    function renderExternalList(grouped, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const container = document.getElementById("outbound-external-list");
        const sidebar = document.getElementById("external-sidebar-list");
        if (!container) return;
        container.innerHTML = "";
        if (sidebar) sidebar.innerHTML = "";

        grouped.forEach(group => {
            const externalMermaidCode = generateExternalTypeMermaidCode(group, visibility);
            if (!externalMermaidCode) return;
            const externalFqn = group.externalType.fqn;
            const externalId = Jig.util.fqnToId("external", externalFqn);
            const externalLabel = Jig.glossary.getTypeTerm(externalFqn).title;

            const externalMermaidContainer = Jig.dom.createElement("div", {className: "mermaid-diagram port-diagram"});
            Jig.mermaid.diagram.register(externalMermaidContainer, () => {
                const currentVisibility = readVisibility();
                const generator = (dir) => generateExternalTypeMermaidCode(group, {...currentVisibility, direction: dir});
                if (generator(currentVisibility.direction)) {
                    renderMermaid(generator, externalMermaidContainer, {direction: currentVisibility.direction});
                }
            });

            const externalCard = Jig.dom.card.type({
                id: externalId,
                title: externalLabel,
                fqn: externalFqn,
            });
            externalCard.appendChild(externalMermaidContainer);
            container.appendChild(externalCard);
        });

        Jig.dom.sidebar.renderSection(sidebar, "外部型", grouped.map(group => ({
            id: Jig.util.fqnToId("external", group.externalType.fqn),
            label: Jig.glossary.getTypeTerm(group.externalType.fqn).title
        })));

        if (grouped.length === 0) renderNoData(container);
    }

    function renderCrudTable(grouped) {
        const container = document.getElementById("outbound-crud-panel");
        if (!container) return;
        container.innerHTML = "";

        const allPersistenceTargets = collectAllTargets(grouped);
        if (allPersistenceTargets.length === 0) {
            container.textContent = "永続化操作なし";
            return;
        }

        const headerRow = Jig.dom.createElement("tr", {
            children: [
                Jig.dom.createElement("th", {textContent: "出力ポート / 操作"}),
                ...allPersistenceTargets.map(persistenceTarget => Jig.dom.createElement("th", {
                    id: `crud-target-${persistenceTarget}`,
                    textContent: persistenceTarget
                }))
            ]
        });

        const tbody = Jig.dom.createElement("tbody");
        grouped.forEach(group => {
            const portId = Jig.util.fqnToId("port", group.outboundPort.fqn);
            const portRow = Jig.dom.createElement("tr", {
                className: "port-group-row",
                style: {cursor: "pointer"},
                children: [
                    Jig.dom.createElement("td", {
                        className: "port-group-cell",
                        children: [
                            document.createTextNode(Jig.glossary.getTypeTerm(group.outboundPort.fqn).title),
                            Jig.dom.createElement("span", {className: "weak", style: {marginLeft: "8px"}, textContent: `(${group.operations.length})`})
                        ]
                    }),
                    ...allPersistenceTargets.map(persistenceTarget => {
                        const cell = Jig.dom.createElement("td", {className: "crud-cell port-crud-cell"});
                        const cruds = new Set();
                        group.operations.forEach(operation => {
                            operation.persistenceAccessors.forEach(op => {
                                if (persistenceTarget in op.targetOperationTypes) {
                                    const crud = toCrudChar(op.targetOperationTypes[persistenceTarget]);
                                    if (crud) cruds.add(crud);
                                }
                            });
                        });
                        if (cruds.size > 0) cell.textContent = Array.from(cruds).sort().join("");
                        return cell;
                    })
                ]
            });
            tbody.appendChild(portRow);

            const opRows = group.operations.map(operation => {
                const row = Jig.dom.createElement("tr", {
                    className: `operation-row ${portId}`,
                    style: {display: "none"},
                    children: [
                        Jig.dom.createElement("td", {
                            className: "operation-cell",
                            textContent: Jig.glossary.getMethodTerm(operation.outboundPortOperation.fqn).title
                        }),
                        ...allPersistenceTargets.map(persistenceTarget => {
                            const cell = Jig.dom.createElement("td", {className: "crud-cell"});
                            const cruds = new Set();
                            operation.persistenceAccessors.forEach(op => {
                                if (persistenceTarget in op.targetOperationTypes) {
                                    const crud = toCrudChar(op.targetOperationTypes[persistenceTarget]);
                                    if (crud) cruds.add(crud);
                                }
                            });
                            if (cruds.size > 0) cell.textContent = Array.from(cruds).sort().join("");
                            return cell;
                        })
                    ]
                });
                tbody.appendChild(row);
                return row;
            });

            portRow.addEventListener("click", () => {
                const isHidden = opRows[0].style.display === "none";
                opRows.forEach(row => row.style.display = isHidden ? "table-row" : "none");
                portRow.classList.toggle("is-expanded", isHidden);
            });
        });

        container.appendChild(Jig.dom.createElement("table", {
            className: "zebra crud-table",
            children: [Jig.dom.createElement("thead", {children: [headerRow]}), tbody]
        }));
    }

    function renderNoData(container) {
        container.appendChild(Jig.dom.createElement("p", {className: "weak", textContent: "データなし"}));
    }

    function renderMermaid(diagramFn, container, options = {}) {
        if (!diagramFn || !container) return;
        container.innerHTML = "";
        Jig.mermaid.render.renderWithControls(container, diagramFn, options);
    }

    function generatePortMermaidCode(group, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const builder = new Jig.mermaid.Builder();
        builder.applyThemeClassDefs();
        const portFqn = group.outboundPort.fqn;
        const portLabel = Jig.glossary.getTypeTerm(portFqn).title;

        const contexts = {
            portSubgraphs: new Map(),
            adapterSubgraphs: new Map(),
            accessorSubgraphs: new Map(),
            accessorNodes: new Map(),
            persistenceTargetNodes: new Map(),
            extAccessorNodes: new Map(),
            extAccessorSubgraphs: new Map(),
            extTypeNodes: new Map()
        };

        group.operations.forEach((operation) => {
            const portOpName = Jig.glossary.getMethodTerm(operation.outboundPortOperation.fqn).title;
            const portOpFqn = operation.outboundPortOperation.fqn;

            const adapterFqn = operation.outboundAdapter?.fqn;
            const adapterLabel = Jig.glossary.getTypeTerm(operation.outboundAdapter?.fqn).title;
            const executionName = Jig.glossary.getMethodTerm(operation.outboundAdapterExecution?.fqn).title;
            const executionFqn = operation.outboundAdapterExecution?.fqn;

            let lastNodeId = addPortNode(builder, contexts.portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility);
            lastNodeId = addAdapterNode(builder, lastNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, contexts.adapterSubgraphs);

            operation.persistenceAccessors.forEach(op => {
                const currentNode = addAccessorNode(builder, lastNodeId, op, visibility, contexts.accessorSubgraphs, contexts.accessorNodes);
                if (visibility.target) {
                    addPersistenceTargetEdges(builder, currentNode, op, contexts.persistenceTargetNodes, visibility);
                }
            });

            operation.externalAccessors.forEach(accessor => {
                addExternalAccessorNode(builder, lastNodeId, accessor, visibility, contexts.extAccessorNodes, contexts.extAccessorSubgraphs, contexts.extTypeNodes);
            });
        });

        if (builder.isEmpty()) return null;
        return builder.build(visibility.direction);
    }

    function generateOperationMermaidCode(operation, visibility = state.visibility || DEFAULT_VISIBILITY) {
        return generatePortMermaidCode({outboundPort: operation.outboundPort, operations: [operation]}, visibility);
    }

    function generatePersistenceMermaidCode(group, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const builder = new Jig.mermaid.Builder();
        builder.applyThemeClassDefs();
        const persistenceTarget = group.persistenceTarget;

        const contexts = {
            portSubgraphs: new Map(), adapterSubgraphs: new Map(),
            accessorSubgraphs: new Map(), accessorNodes: new Map(),
            persistenceTargetNodes: new Map()
        };

        group.operations.forEach((operation) => {
            const props = extractOperationProps(operation);
            operation.persistenceAccessors
                .filter(op => persistenceTarget in op.targetOperationTypes)
                .filter(op => isCrudVisible(op.targetOperationTypes[persistenceTarget], visibility))
                .forEach(op => {
                    let currentNode = addPortNode(builder, contexts.portSubgraphs, props.portFqn, props.portLabel, props.portOpFqn, props.portOpName, visibility);
                    currentNode = addAdapterNode(builder, currentNode, props.adapterFqn, props.adapterLabel, props.executionFqn, props.executionName, visibility, contexts.adapterSubgraphs);
                    currentNode = addAccessorNode(builder, currentNode, op, visibility, contexts.accessorSubgraphs, contexts.accessorNodes);

                    if (visibility.target) {
                        addPersistenceTargetEdges(builder, currentNode, {
                            targetOperationTypes: {[persistenceTarget]: op.targetOperationTypes[persistenceTarget]}
                        }, contexts.persistenceTargetNodes, visibility);
                    }
                });
        });

        if (builder.isEmpty()) return null;
        return builder.build(visibility.direction);
    }

    function generateExternalTypeMermaidCode(group, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const builder = new Jig.mermaid.Builder();
        builder.applyThemeClassDefs();
        const externalType = group.externalType;

        const contexts = {
            portSubgraphs: new Map(), adapterSubgraphs: new Map(),
            extAccessorNodes: new Map(), extAccessorSubgraphs: new Map(),
            extTypeNodes: new Map()
        };

        const filterToExternalType = accessor => ({
            ...accessor,
            operations: accessor.operations
                .map(m => ({...m, externals: m.externals.filter(ext => ext.fqn === externalType.fqn)}))
                .filter(m => m.externals.length > 0)
        });

        group.operations.forEach(operation => {
            const relevantAccessors = operation.externalAccessors.filter(accessor =>
                accessor.operations.some(accMethod => accMethod.externals.some(ext => ext.fqn === externalType.fqn)));

            const props = extractOperationProps(operation);

            relevantAccessors.forEach(accessor => {
                let currentNode = addPortNode(builder, contexts.portSubgraphs, props.portFqn, props.portLabel, props.portOpFqn, props.portOpName, visibility);
                currentNode = addAdapterNode(builder, currentNode, props.adapterFqn, props.adapterLabel, props.executionFqn, props.executionName, visibility, contexts.adapterSubgraphs);
                addExternalAccessorNode(builder, currentNode, filterToExternalType(accessor), visibility, contexts.extAccessorNodes, contexts.extAccessorSubgraphs, contexts.extTypeNodes);
            });
        });

        (group.directAccessors || []).forEach(accessor => {
            addExternalAccessorNode(builder, null, filterToExternalType(accessor), visibility, contexts.extAccessorNodes, contexts.extAccessorSubgraphs, contexts.extTypeNodes);
        });

        if (builder.isEmpty()) return null;
        return builder.build(visibility.direction);
    }

    function extractOperationProps(operation) {
        return {
            portFqn: operation.outboundPort.fqn,
            portLabel: Jig.glossary.getTypeTerm(operation.outboundPort.fqn).title,
            portOpName: Jig.glossary.getMethodTerm(operation.outboundPortOperation.fqn).title,
            portOpFqn: operation.outboundPortOperation.fqn,
            adapterFqn: operation.outboundAdapter?.fqn,
            adapterLabel: Jig.glossary.getTypeTerm(operation.outboundAdapter?.fqn).title,
            executionName: Jig.glossary.getMethodTerm(operation.outboundAdapterExecution?.fqn).title,
            executionFqn: operation.outboundAdapterExecution?.fqn,
        };
    }

    function addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility) {
        if (!visibility.port) return null;
        if (visibility.operation) {
            const portOpId = Jig.util.fqnToId("portOp", portOpFqn);
            builder.addNodeToSubgraph(builder.ensureSubgraph(portSubgraphs, portFqn, portLabel), portOpId, portOpName, 'method');
            builder.addClass(portOpId, "outbound");
            return portOpId;
        } else {
            const portNodeId = Jig.util.fqnToId("port", portFqn);
            builder.addNode(portNodeId, portLabel, 'class');
            builder.addClass(portNodeId, "outbound");
            return portNodeId;
        }
    }

    function addAdapterNode(builder, sourceNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs) {
        if (!visibility.adapter) return sourceNodeId;
        if (visibility.execution) {
            const sg = builder.ensureSubgraph(adapterSubgraphs, adapterFqn, adapterLabel);
            const executionId = Jig.util.fqnToId("exec", executionFqn);
            builder.addNodeToSubgraph(sg, executionId, executionName, 'method');
            if (sourceNodeId) builder.addEdge(sourceNodeId, executionId);
            return executionId;
        } else {
            const adapterNodeId = Jig.util.fqnToId("adapter", adapterFqn);
            builder.addNode(adapterNodeId, adapterLabel, 'class');
            if (sourceNodeId) builder.addEdge(sourceNodeId, adapterNodeId);
            return adapterNodeId;
        }
    }

    function addAccessorNode(builder, sourceNodeId, op, visibility, accessorSubgraphs, accessorNodes) {
        const groupId = op.group;
        if (!visibility.accessor || !groupId) return sourceNodeId;

        const groupLabel = Jig.glossary.getTypeTerm(groupId).title;
        if (visibility.accessorMethod) {
            const opNodeId = Jig.util.fqnToId("op", op.id);
            builder.addNodeToSubgraph(builder.ensureSubgraph(accessorSubgraphs, groupId, groupLabel), opNodeId, op.id.split('.').pop(), 'method');
            if (sourceNodeId) builder.addEdge(sourceNodeId, opNodeId);
            return opNodeId;
        } else {
            const accessorNodeId = Jig.util.fqnToId("accessor", groupId);
            if (!accessorNodes.has(groupId)) {
                accessorNodes.set(groupId, accessorNodeId);
                builder.addNode(accessorNodeId, groupLabel, 'class');
            }
            if (sourceNodeId) builder.addEdge(sourceNodeId, accessorNodes.get(groupId));
            return accessorNodes.get(groupId);
        }
    }

    function addPersistenceTargetEdges(builder, sourceNodeId, op, persistenceTargetNodes, visibility) {
        Object.entries(op.targetOperationTypes).forEach(([persistenceTarget, operationType]) => {
            if (!isCrudVisible(operationType, visibility)) return;
            if (!persistenceTargetNodes.has(persistenceTarget)) {
                persistenceTargetNodes.set(persistenceTarget, `Target_${persistenceTargetNodes.size}`);
                builder.addNode(persistenceTargetNodes.get(persistenceTarget), persistenceTarget, 'database');
            }
            const edgeLabel = visibility.externalTypeMethod ? operationType : undefined;
            if (sourceNodeId) builder.addEdge(sourceNodeId, persistenceTargetNodes.get(persistenceTarget), edgeLabel);
        });
    }

    function addExternalAccessorNode(builder, sourceNodeId, accessor, visibility, extAccessorNodes, extAccessorSubgraphs, extTypeNodes) {
        const addExternal = (fromNodeId, ext) => {
            if (!visibility.externalType) return;
            if (!extTypeNodes.has(ext.fqn)) {
                extTypeNodes.set(ext.fqn, `ExtType_${extTypeNodes.size}`);
                builder.addNode(extTypeNodes.get(ext.fqn), Jig.glossary.getTypeTerm(ext.fqn).title, 'external');
            }
            const edgeLabel = visibility.externalTypeMethod ? ext.method : undefined;
            if (fromNodeId) builder.addEdge(fromNodeId, extTypeNodes.get(ext.fqn), edgeLabel);
        };

        if (!visibility.externalAccessor) {
            if (visibility.externalType) {
                const uniqueExternals = new Map();
                accessor.operations.forEach(accMethod => accMethod.externals.forEach(ext => uniqueExternals.set(ext.fqn, ext)));
                uniqueExternals.forEach(ext => addExternal(sourceNodeId, ext));
            }
            return sourceNodeId;
        }

        const accessorLabel = Jig.glossary.getTypeTerm(accessor.fqn).title;
        if (visibility.externalAccessorMethod) {
            const sg = builder.ensureSubgraph(extAccessorSubgraphs, accessor.fqn, accessorLabel);
            accessor.operations.forEach(accMethod => {
                const accMethodNodeId = Jig.util.fqnToId("accMethod", accMethod.fqn);
                builder.addNodeToSubgraph(sg, accMethodNodeId, Jig.glossary.getMethodTerm(accMethod.fqn).title, 'method');
                if (sourceNodeId) builder.addEdge(sourceNodeId, accMethodNodeId);
                accMethod.externals.forEach(ext => addExternal(accMethodNodeId, ext));
            });
            return null;
        } else {
            const nodeId = Jig.util.fqnToId("extAcc", accessor.fqn);
            if (!extAccessorNodes.has(accessor.fqn)) {
                extAccessorNodes.set(accessor.fqn, nodeId);
                builder.addNode(nodeId, accessorLabel, 'class');
            }
            if (sourceNodeId) builder.addEdge(sourceNodeId, extAccessorNodes.get(accessor.fqn));

            if (visibility.externalType) {
                const uniqueExternals = new Map();
                accessor.operations.forEach(accMethod => {
                    if (visibility.externalTypeMethod) {
                        accMethod.externals.forEach(ext => addExternal(extAccessorNodes.get(accessor.fqn), ext));
                    } else {
                        accMethod.externals.forEach(ext => uniqueExternals.set(ext.fqn, ext));
                    }
                });
                if (!visibility.externalTypeMethod) uniqueExternals.forEach(ext => addExternal(extAccessorNodes.get(accessor.fqn), ext));
            }
            return extAccessorNodes.get(accessor.fqn);
        }
    }

    function init() {
        if (typeof document === "undefined") return;
        if (!document.body.classList.contains("outbound-interface")) return;

        state.visibility = {...DEFAULT_VISIBILITY};
        state.data = loadData();

        const model = buildModel(state.data);
        state.grouped = model.grouped;
        state.persistenceGrouped = model.persistenceGrouped;
        state.externalGrouped = model.externalGrouped;

        bindEvents();
        render();
    }

    function setState(newState) {
        Object.assign(state, newState);
        if ('visibility' in newState) {
            renderAllPanels();
            Jig.mermaid.diagram.rerenderVisible();
        }
        if ('activeTab' in newState) {
            updateTabs(state.activeTab);
        }
    }

    function bindEvents() {
        const childRules = {
            "show-port": "show-operation",
            "show-adapter": "show-execution",
            "show-accessor": "show-accessor-method",
            "show-target": "show-external-type-method",
        };

        const updateChildDisabled = () => {
            Object.entries(childRules).forEach(([parentName, childName]) => {
                const parentEl = document.querySelector(`input[name="${parentName}"]`);
                const childEl = document.querySelector(`input[name="${childName}"]`);
                if (parentEl && childEl) childEl.disabled = !parentEl.checked;
            });
        };

        document.querySelectorAll('input[name^="show-"]').forEach(input => {
            input.addEventListener('change', () => {
                updateChildDisabled();
                setState({visibility: readVisibility()});
            });
        });

        updateChildDisabled();

        document.querySelectorAll('input[name="diagram-direction"]').forEach(input => {
            input.addEventListener('change', () => {
                setState({visibility: readVisibility()});
            });
        });

        document.querySelectorAll('.outbound-tab-list .tab-button').forEach(button => {
            button.addEventListener('click', () => {
                setState({activeTab: button.getAttribute('data-tab')});
            });
        });
    }

    function readVisibility() {
        const checked = (name) => {
            const el = document.querySelector(`input[name="${name}"]`);
            return el ? el.checked : false;
        };
        const directionEl = document.querySelector('input[name="diagram-direction"]:checked');
        return {
            port: checked("show-port"),
            operation: checked("show-operation"),
            adapter: checked("show-adapter"),
            execution: checked("show-execution"),
            accessor: checked("show-accessor"),
            accessorMethod: checked("show-accessor-method"),
            target: checked("show-target"),
            externalAccessor: checked("show-accessor"),
            externalAccessorMethod: checked("show-accessor-method"),
            externalType: checked("show-target"),
            externalTypeMethod: checked("show-external-type-method"),
            direction: directionEl ? directionEl.value : 'LR',
            crudCreate: checked("show-crud-c"),
            crudRead: checked("show-crud-r"),
            crudUpdate: checked("show-crud-u"),
            crudDelete: checked("show-crud-d"),
        };
    }

    return {
        init,
        groupOperationsByOutboundPort,
        groupOperationsByPersistenceTarget,
        groupOperationsByExternalType,
        groupDirectExternalAccessors,
        formatPersistenceAccessors,
        toCrudChar,
        renderOutboundList,
        renderPersistenceList,
        renderExternalList,
        renderCrudTable,
        generateOperationMermaidCode,
        generatePortMermaidCode,
        generatePersistenceMermaidCode,
        generateExternalTypeMermaidCode,
    };
})();

if (typeof document !== "undefined") {
    document.addEventListener("DOMContentLoaded", () => OutboundApp.init());
}

if (typeof module !== "undefined" && module.exports) {
    module.exports = OutboundApp;
}
