const OutboundApp = (() => {
    const Jig = globalThis.Jig;

    const DEFAULT_VISIBILITY = {
        callerUsecase: true,
        port: true, operation: true,
        adapter: false, execution: true,
        accessor: false, accessorMethod: true,
        target: true,
        externalAccessor: false, externalAccessorMethod: false,
        externalType: true, externalTypeMethod: true,
        crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
    };

    const INITIAL_STATE = {
        visibility: null,
        data: null,
        grouped: null,
        persistenceGrouped: null,
        externalGrouped: null
    };

    const state = {...INITIAL_STATE};

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
                const left = Jig.glossary.getMethodTerm(a.outboundPortOperation.fqn, true).title;
                const right = Jig.glossary.getMethodTerm(b.outboundPortOperation.fqn, true).title;
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
        const extMap = new Map();
        data.otherExternalAccessors.forEach(accessor => {
            accessor.operations.forEach(method => {
                method.externals.forEach(ext => {
                    if (!extMap.has(ext.fqn)) {
                        extMap.set(ext.fqn, {externalType: {fqn: ext.fqn}, accessorMap: new Map()});
                    }
                    const group = extMap.get(ext.fqn);
                    if (!group.accessorMap.has(accessor.fqn)) {
                        group.accessorMap.set(accessor.fqn, {fqn: accessor.fqn, operationMap: new Map()});
                    }
                    const accessorEntry = group.accessorMap.get(accessor.fqn);
                    if (!accessorEntry.operationMap.has(method.fqn)) {
                        accessorEntry.operationMap.set(method.fqn, {
                            ...method,
                            externals: method.externals.filter(e => e.fqn === ext.fqn)
                        });
                    }
                });
            });
        });
        return Array.from(extMap.values()).map(group => ({
            externalType: group.externalType,
            directAccessors: Array.from(group.accessorMap.values()).map(a => ({
                fqn: a.fqn,
                operations: Array.from(a.operationMap.values())
            }))
        }));
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

    function collectCrudChars(accessors, persistenceTarget) {
        const cruds = new Set();
        accessors.forEach(op => {
            if (persistenceTarget in op.targetOperationTypes) {
                const crud = toCrudChar(op.targetOperationTypes[persistenceTarget]);
                if (crud) cruds.add(crud);
            }
        });
        return Array.from(cruds).sort().join("");
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
                const generator = (dir, opts) => generatePortMermaidCode(group, {...currentVisibility, direction: dir, showPhysicalName: opts?.showPhysicalName});
                if (generator('LR')) {
                    Jig.mermaid.render.renderWithControls(container, generator, {direction: 'LR', enableLabelToggle: true});
                }
            }, {className: "mermaid-diagram"});

            const itemList = Jig.dom.createElement("div", {className: "outbound-operation-list"});
            group.operations.forEach(operation => {
                const operationWithPort = {...operation, outboundPort: group.outboundPort};

                const op = operation.outboundPortOperation;
                const opTerm = Jig.glossary.getMethodTerm(op.fqn);
                const operationItem = Jig.dom.card.item({tagName: "article", extraClass: "outbound-operation-item", id: Jig.util.fqnToId("portOp", op.fqn), title: opTerm.title});
                operationItem.appendChild(Jig.dom.createElement("div", {className: "declaration", textContent: opTerm.shortDeclaration}));
                operationItem.appendChild(Jig.dom.type.methodIOSection(op.parameters, op.returnTypeRef));
                Jig.mermaid.diagram.createAndRegister(operationItem, (container) => {
                    const currentVisibility = readVisibility();
                    const generator = (dir, opts) => generateOperationMermaidCode(operationWithPort, {...currentVisibility, direction: dir, showPhysicalName: opts?.showPhysicalName});
                    if (generator('LR')) {
                        Jig.mermaid.render.renderWithControls(container, generator, {direction: 'LR', enableLabelToggle: true});
                    }
                });
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

        if (sidebar && grouped.length > 0) {
            const byPackage = new Map();
            grouped.forEach(group => {
                const fqn = group.outboundPort.fqn;
                const dotIdx = fqn.lastIndexOf('.');
                const pkg = dotIdx === -1 ? '' : fqn.slice(0, dotIdx);
                Jig.util.pushToMap(byPackage, pkg, group);
            });

            const packageContainer = Jig.dom.createElement("div", {});
            Jig.dom.sidebar.renderPackageGrouped(packageContainer, byPackage, pkgGroups =>
                pkgGroups.map(group => {
                    const fqn = group.outboundPort.fqn;
                    return Jig.dom.createElement("li", {
                        className: "in-page-sidebar__item",
                        children: [Jig.dom.createElement("a", {
                            className: "in-page-sidebar__link",
                            attributes: {href: "#" + Jig.util.fqnToId("port", fqn)},
                            textContent: Jig.glossary.getTypeTerm(fqn).title
                        })]
                    });
                }),
                {titleClass: "in-page-sidebar__title--sub"}
            );

            const portTitle = Jig.dom.createElement("p", {
                className: "in-page-sidebar__title in-page-sidebar__title--collapsible",
                children: [
                    Jig.dom.createElement("span", {textContent: "出力ポート"}),
                    Jig.dom.sidebar.createToggle(packageContainer)
                ]
            });
            sidebar.appendChild(Jig.dom.createElement("section", {
                className: "in-page-sidebar__section",
                children: [portTitle, packageContainer]
            }));
        }

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

            const persistenceCard = Jig.dom.card.type({
                id: targetId,
                title: group.persistenceTarget,
            });
            Jig.mermaid.diagram.createAndRegister(persistenceCard, (container) => {
                const currentVisibility = readVisibility();
                const generator = (dir, opts) => generatePersistenceMermaidCode(group, {...currentVisibility, direction: dir, showPhysicalName: opts?.showPhysicalName});
                if (generator('LR')) {
                    Jig.mermaid.render.renderWithControls(container, generator, {direction: 'LR', enableLabelToggle: true});
                }
            });
            container.appendChild(persistenceCard);
        });

        Jig.dom.sidebar.renderSection(sidebar, "永続化操作対象", grouped.map(group => ({
            id: Jig.util.fqnToId("persistence", group.persistenceTarget),
            label: group.persistenceTarget
        })), {collapsible: true});

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

            const externalCard = Jig.dom.card.type({
                id: externalId,
                title: externalLabel,
                fqn: externalFqn,
            });
            Jig.mermaid.diagram.createAndRegister(externalCard, (container) => {
                const currentVisibility = readVisibility();
                const generator = (dir, opts) => generateExternalTypeMermaidCode(group, {...currentVisibility, direction: dir, showPhysicalName: opts?.showPhysicalName});
                if (generator('LR')) {
                    Jig.mermaid.render.renderWithControls(container, generator, {direction: 'LR', enableLabelToggle: true});
                }
            });
            container.appendChild(externalCard);
        });

        Jig.dom.sidebar.renderSection(sidebar, "外部型", grouped.map(group => ({
            id: Jig.util.fqnToId("external", group.externalType.fqn),
            label: Jig.glossary.getTypeTerm(group.externalType.fqn).title
        })), {collapsible: true});

        if (grouped.length === 0) renderNoData(container);
    }

    function renderCrudTable(grouped) {
        const container = document.getElementById("outbound-crud-panel");
        const sidebar = document.getElementById("crud-sidebar-list");
        if (!container) return;
        container.innerHTML = "";
        if (sidebar) sidebar.innerHTML = "";

        const allPersistenceTargets = collectAllTargets(grouped);
        if (allPersistenceTargets.length === 0) {
            container.textContent = "永続化操作なし";
            return;
        }

        if (sidebar) {
            sidebar.appendChild(Jig.dom.createElement("section", {
                className: "in-page-sidebar__section",
                children: [Jig.dom.createElement("p", {
                    className: "in-page-sidebar__title",
                    children: [Jig.dom.createElement("a", {
                        className: "in-page-sidebar__link",
                        attributes: {href: "#outbound-crud-panel"},
                        textContent: "永続化(CRUD)"
                    })]
                })]
            }));
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
                        const text = collectCrudChars(group.operations.flatMap(op => op.persistenceAccessors), persistenceTarget);
                        if (text) cell.textContent = text;
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
                            textContent: Jig.glossary.getMethodTerm(operation.outboundPortOperation.fqn, true).title
                        }),
                        ...allPersistenceTargets.map(persistenceTarget => {
                            const cell = Jig.dom.createElement("td", {className: "crud-cell"});
                            const text = collectCrudChars(operation.persistenceAccessors, persistenceTarget);
                            if (text) cell.textContent = text;
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

    function generatePortMermaidCode(group, visibility = state.visibility || DEFAULT_VISIBILITY) {
        const builder = new Jig.mermaid.Builder();
        builder.applyThemeClassDefs();
        const showPhysicalName = visibility.showPhysicalName ?? false;
        const {type: typeLabel} = Jig.glossary.makeLabels(showPhysicalName);
        const portFqn = group.outboundPort.fqn;
        const portLabel = typeLabel(portFqn);

        const contexts = {
            portSubgraphs: new Map(),
            adapterSubgraphs: new Map(),
            accessorSubgraphs: new Map(),
            accessorNodes: new Map(),
            persistenceTargetNodes: new Map(),
            extAccessorNodes: new Map(),
            extAccessorSubgraphs: new Map(),
            extTypeNodes: new Map(),
            usecaseSubgraphs: new Map(),
            usecaseNodes: new Map(),
            usecaseEdges: new Set(),
            methodFqnToNodeId: new Map()
        };

        group.operations.forEach((operation) => {
            const props = extractOperationProps({...operation, outboundPort: group.outboundPort}, showPhysicalName);
            const portOpFqn = props.portOpFqn;
            const portOpName = props.portOpName;
            const adapterFqn = props.adapterFqn;
            const adapterLabel = props.adapterLabel;
            const executionName = props.executionName;
            const executionFqn = props.executionFqn;

            let lastNodeId = addPortNode(builder, contexts.portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility);
            addCallerUsecaseNodes(builder, lastNodeId, portOpFqn, operation.outboundPortOperation.callerUsecases, visibility, contexts.usecaseSubgraphs, contexts.usecaseNodes, contexts.usecaseEdges);
            lastNodeId = addAdapterNode(builder, lastNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, contexts.adapterSubgraphs, contexts.methodFqnToNodeId);

            operation.persistenceAccessors.forEach(op => {
                const currentNode = addAccessorNode(builder, lastNodeId, op, visibility, contexts.accessorSubgraphs, contexts.accessorNodes);
                if (visibility.target) {
                    addPersistenceTargetEdges(builder, currentNode, op, contexts.persistenceTargetNodes, visibility);
                }
            });

            operation.externalAccessors.forEach(accessor => {
                addExternalAccessorNode(builder, lastNodeId, accessor, visibility, contexts.extAccessorNodes, contexts.extAccessorSubgraphs, contexts.extTypeNodes, contexts.methodFqnToNodeId);
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
        const showPhysicalName = visibility.showPhysicalName ?? false;
        const persistenceTarget = group.persistenceTarget;

        const contexts = {
            portSubgraphs: new Map(), adapterSubgraphs: new Map(),
            accessorSubgraphs: new Map(), accessorNodes: new Map(),
            persistenceTargetNodes: new Map(),
            usecaseSubgraphs: new Map(), usecaseNodes: new Map(), usecaseEdges: new Set()
        };

        group.operations.forEach((operation) => {
            const props = extractOperationProps(operation, showPhysicalName);
            operation.persistenceAccessors
                .filter(op => persistenceTarget in op.targetOperationTypes)
                .filter(op => isCrudVisible(op.targetOperationTypes[persistenceTarget], visibility))
                .forEach(op => {
                    let currentNode = addPortNode(builder, contexts.portSubgraphs, props.portFqn, props.portLabel, props.portOpFqn, props.portOpName, visibility);
                    addCallerUsecaseNodes(builder, currentNode, props.portOpFqn, operation.outboundPortOperation.callerUsecases, visibility, contexts.usecaseSubgraphs, contexts.usecaseNodes, contexts.usecaseEdges);
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
        const showPhysicalName = visibility.showPhysicalName ?? false;
        const externalType = group.externalType;

        const contexts = {
            portSubgraphs: new Map(), adapterSubgraphs: new Map(),
            extAccessorNodes: new Map(), extAccessorSubgraphs: new Map(),
            extTypeNodes: new Map(),
            usecaseSubgraphs: new Map(), usecaseNodes: new Map(), usecaseEdges: new Set(),
            methodFqnToNodeId: new Map()
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

            const props = extractOperationProps(operation, showPhysicalName);

            relevantAccessors.forEach(accessor => {
                let currentNode = addPortNode(builder, contexts.portSubgraphs, props.portFqn, props.portLabel, props.portOpFqn, props.portOpName, visibility);
                addCallerUsecaseNodes(builder, currentNode, props.portOpFqn, operation.outboundPortOperation.callerUsecases, visibility, contexts.usecaseSubgraphs, contexts.usecaseNodes, contexts.usecaseEdges);
                currentNode = addAdapterNode(builder, currentNode, props.adapterFqn, props.adapterLabel, props.executionFqn, props.executionName, visibility, contexts.adapterSubgraphs, contexts.methodFqnToNodeId);
                addExternalAccessorNode(builder, currentNode, filterToExternalType(accessor), visibility, contexts.extAccessorNodes, contexts.extAccessorSubgraphs, contexts.extTypeNodes, contexts.methodFqnToNodeId);
            });
        });

        (group.directAccessors || []).forEach(accessor => {
            addExternalAccessorNode(builder, null, filterToExternalType(accessor), visibility, contexts.extAccessorNodes, contexts.extAccessorSubgraphs, contexts.extTypeNodes, contexts.methodFqnToNodeId);
        });

        if (builder.isEmpty()) return null;
        return builder.build(visibility.direction);
    }

    function extractOperationProps(operation, showPhysicalName = false) {
        const typeLabel = (fqn) => showPhysicalName ? (fqn ? Jig.glossary.typeSimpleName(fqn) : '') : Jig.glossary.getTypeTerm(fqn).title;
        const {method: mLabel} = Jig.glossary.makeLabels(showPhysicalName);
        return {
            portFqn: operation.outboundPort.fqn,
            portLabel: typeLabel(operation.outboundPort.fqn),
            portOpName: mLabel(operation.outboundPortOperation.fqn),
            portOpFqn: operation.outboundPortOperation.fqn,
            adapterFqn: operation.outboundAdapter?.fqn,
            adapterLabel: typeLabel(operation.outboundAdapter?.fqn),
            executionName: mLabel(operation.outboundAdapterExecution?.fqn),
            executionFqn: operation.outboundAdapterExecution?.fqn,
        };
    }

    function addCallerUsecaseNodes(builder, portOpNodeId, portOpFqn, callerUsecases, visibility, usecaseSubgraphs, usecaseNodes, usecaseEdges) {
        if (!visibility.callerUsecase || !portOpNodeId || !callerUsecases?.length) return;
        const showPhysicalName = visibility.showPhysicalName ?? false;
        const {type: typeLabel, method: mLabel} = Jig.glossary.makeLabels(showPhysicalName);
        callerUsecases.forEach(usecaseFqn => {
            const edgeKey = usecaseFqn + '->' + portOpFqn;
            if (usecaseEdges.has(edgeKey)) return;
            usecaseEdges.add(edgeKey);
            if (!usecaseNodes.has(usecaseFqn)) {
                const nodeId = Jig.util.fqnToId("usecase", usecaseFqn);
                const hashIdx = usecaseFqn.indexOf('#');
                const classFqn = hashIdx !== -1 ? usecaseFqn.slice(0, hashIdx) : usecaseFqn;
                const sg = builder.ensureSubgraph(usecaseSubgraphs, classFqn, typeLabel(classFqn));
                builder.addNodeToSubgraph(sg, nodeId, mLabel(usecaseFqn), 'method');
                builder.addClass(nodeId, "usecase");
                builder.addClick(nodeId, Jig.mermaid.nav.usecaseMethodUrl(usecaseFqn), usecaseFqn);
                usecaseNodes.set(usecaseFqn, nodeId);
            }
            builder.addEdge(usecaseNodes.get(usecaseFqn), portOpNodeId);
        });
    }

    function addPortNode(builder, portSubgraphs, portFqn, portLabel, portOpFqn, portOpName, visibility) {
        if (!visibility.port) return null;
        const portCardId = Jig.util.fqnToId("port", portFqn);
        if (visibility.operation) {
            const portOpId = Jig.util.fqnToId("portOp", portOpFqn);
            builder.addNodeToSubgraph(builder.ensureSubgraph(portSubgraphs, portFqn, portLabel), portOpId, portOpName, 'method');
            builder.addClass(portOpId, "outbound");
            builder.addClick(portFqn, `#${portCardId}`, portFqn);
            builder.addClick(portOpId, `#${portOpId}`, portOpFqn);
            return portOpId;
        } else {
            builder.addNode(portCardId, portLabel, 'class');
            builder.addClass(portCardId, "outbound");
            builder.addClick(portCardId, `#${portCardId}`, portFqn);
            return portCardId;
        }
    }

    function addAdapterNode(builder, sourceNodeId, adapterFqn, adapterLabel, executionFqn, executionName, visibility, adapterSubgraphs, methodFqnToNodeId = null) {
        if (!visibility.adapter) return sourceNodeId;
        if (visibility.execution) {
            const sg = builder.ensureSubgraph(adapterSubgraphs, adapterFqn, adapterLabel);
            const executionId = Jig.util.fqnToId("exec", executionFqn);
            builder.addNodeToSubgraph(sg, executionId, executionName, 'method');
            builder.addTooltip(executionId, executionFqn);
            if (sourceNodeId) builder.addEdge(sourceNodeId, executionId);
            methodFqnToNodeId?.set(executionFqn, executionId);
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

        const showPhysicalName = visibility.showPhysicalName ?? false;
        const {type: typeLabel} = Jig.glossary.makeLabels(showPhysicalName);
        const groupLabel = typeLabel(groupId);
        if (visibility.accessorMethod) {
            const opNodeId = Jig.util.fqnToId("op", op.id);
            builder.addNodeToSubgraph(builder.ensureSubgraph(accessorSubgraphs, groupId, groupLabel), opNodeId, op.id.split('.').pop(), 'method');
            builder.addTooltip(opNodeId, op.id);
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
                const nodeId = persistenceTargetNodes.get(persistenceTarget);
                builder.addNode(nodeId, persistenceTarget, 'database');
                builder.addClick(nodeId, `#${Jig.util.fqnToId("persistence", persistenceTarget)}`);
            }
            const edgeLabel = visibility.externalTypeMethod ? operationType : undefined;
            if (sourceNodeId) builder.addEdge(sourceNodeId, persistenceTargetNodes.get(persistenceTarget), edgeLabel);
        });
    }

    function addExternalAccessorNode(builder, sourceNodeId, accessor, visibility, extAccessorNodes, extAccessorSubgraphs, extTypeNodes, methodFqnToNodeId = null) {
        const showPhysicalName = visibility.showPhysicalName ?? false;
        const {type: typeLabel, method: methodLabel} = Jig.glossary.makeLabels(showPhysicalName);
        const addExternal = (fromNodeId, ext) => {
            if (!visibility.externalType) return;
            if (!extTypeNodes.has(ext.fqn)) {
                extTypeNodes.set(ext.fqn, `ExtType_${extTypeNodes.size}`);
                const extLabel = typeLabel(ext.fqn);
                builder.addNode(extTypeNodes.get(ext.fqn), extLabel, 'external');
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

        const accessorLabel = typeLabel(accessor.fqn);
        if (visibility.externalAccessorMethod) {
            const sg = builder.ensureSubgraph(extAccessorSubgraphs, accessor.fqn, accessorLabel);
            accessor.operations.forEach(accMethod => {
                const existingNodeId = methodFqnToNodeId?.get(accMethod.fqn);
                if (existingNodeId !== undefined) {
                    if (sourceNodeId && sourceNodeId !== existingNodeId) builder.addEdge(sourceNodeId, existingNodeId);
                    accMethod.externals.forEach(ext => addExternal(existingNodeId, ext));
                    return;
                }
                const accMethodNodeId = Jig.util.fqnToId("accMethod", accMethod.fqn);
                const accMLabel = methodLabel(accMethod.fqn);
                builder.addNodeToSubgraph(sg, accMethodNodeId, accMLabel, 'method');
                builder.addTooltip(accMethodNodeId, accMethod.fqn);
                if (sourceNodeId) builder.addEdge(sourceNodeId, accMethodNodeId);
                methodFqnToNodeId?.set(accMethod.fqn, accMethodNodeId);
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

        Object.assign(state, INITIAL_STATE);
        state.visibility = {...DEFAULT_VISIBILITY};
        state.data = loadData();

        const model = buildModel(state.data);
        state.grouped = model.grouped;
        state.persistenceGrouped = model.persistenceGrouped;
        state.externalGrouped = model.externalGrouped;

        Jig.dom.sidebar.initCollapseBtn();
        bindEvents();
        renderAllPanels();
    }

    function setState(newState) {
        Object.assign(state, newState);
        if ('visibility' in newState) {
            renderAllPanels();
            Jig.mermaid.diagram.rerenderVisible();
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
    }

    function readVisibility() {
        const checked = (name) => {
            const el = document.querySelector(`input[name="${name}"]`);
            return el ? el.checked : false;
        };
        return {
            callerUsecase: checked("show-caller-usecase"),
            port: checked("show-port"),
            operation: checked("show-operation"),
            adapter: checked("show-adapter"),
            execution: checked("show-execution"),
            accessor: checked("show-accessor"),
            accessorMethod: checked("show-accessor-method"),
            target: checked("show-target"),
            externalAccessor: checked("show-accessor"),         // ポートリストと同じUI設定を共用
            externalAccessorMethod: checked("show-accessor-method"), // 同上
            externalType: checked("show-target"),               // 同上
            externalTypeMethod: checked("show-external-type-method"),
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
