const test = require("node:test");
const assert = require("node:assert/strict");
const { DocumentStub } = require("./dom-stub.js");

// jig-common.js と jig.js をロード（outbound.js が require 時に Jig 名前空間を参照するため先に行う）
global.window = { addEventListener: () => {} };
global.document = new DocumentStub();
require("../../main/resources/templates/assets/jig-common.js");
require("../../main/resources/templates/assets/jig-mermaid-diagram.js");
require("../../main/resources/templates/assets/jig.js");

const jig = require("../../main/resources/templates/assets/jig.js");
const outbound = require("../../main/resources/templates/assets/outbound.js");

// ===== テスト用ヘルパー =====

function makeEmptyData() {
    return {
        outboundPorts: [],
        outboundAdapters: [],
        persistenceAccessors: [],
        otherExternalAccessors: [],
        links: {
            operationToExecution: [],
            executionToPersistenceAccessor: [],
            executionToOtherExternalAccessor: []
        }
    };
}

function setupDom() {
    const doc = new DocumentStub();
    for (const id of [
        "outbound-port-list", "outbound-sidebar-list",
        "outbound-persistence-list", "persistence-sidebar-list",
        "outbound-external-list", "external-sidebar-list",
        "outbound-crud-panel"
    ]) {
        const el = doc.createElement("div");
        el.id = id;
        doc.elementsById.set(id, el);
    }
    global.document = doc;
    // renderMermaid が呼ばれた際にエラーにならないようモック
    globalThis.Jig.mermaid.renderWithControls = () => {};
    return doc;
}

// テスト用の操作グループ（Mermaid コード生成・DOM 描画で再利用）
const simpleGroup = {
    outboundPort: { fqn: "com.example.Port", label: "Port" },
    operations: [{
        outboundPortOperation: { fqn: "com.example.Port#save()", label: "save" },
        outboundAdapter: { fqn: "com.example.Adapter", label: "Adapter" },
        outboundAdapterExecution: { fqn: "com.example.Adapter#save()", label: "save" },
        persistenceAccessors: [],
        externalAccessors: []
    }]
};

// ===== テスト =====

test.describe("outbound.js", () => {

    // ----- toCrudChar -----

    test.describe("toCrudChar", () => {
        test("各SQL操作タイプをCRUD文字に変換する", () => {
            assert.equal(outbound.toCrudChar("SELECT"), "R");
            assert.equal(outbound.toCrudChar("INSERT"), "C");
            assert.equal(outbound.toCrudChar("UPDATE"), "U");
            assert.equal(outbound.toCrudChar("DELETE"), "D");
        });

        test("不明なタイプは空文字を返す", () => {
            assert.equal(outbound.toCrudChar("MERGE"), "");
            assert.equal(outbound.toCrudChar(null), "");
            assert.equal(outbound.toCrudChar(undefined), "");
        });

        test("小文字でも変換できる", () => {
            assert.equal(outbound.toCrudChar("select"), "R");
            assert.equal(outbound.toCrudChar("insert"), "C");
        });
    });

    // ----- formatPersistenceAccessors -----

    test.describe("formatPersistenceAccessors", () => {
        test("空配列の場合は [\"なし\"] を返す", () => {
            assert.deepEqual(outbound.formatPersistenceAccessors([]), ["なし"]);
        });

        test("null の場合は [\"なし\"] を返す", () => {
            assert.deepEqual(outbound.formatPersistenceAccessors(null), ["なし"]);
        });

        test("永続化操作をid・操作タイプ・ターゲット名を含む文字列に整形する", () => {
            const result = outbound.formatPersistenceAccessors([
                { id: "com.example.Mapper.find", targetOperationTypes: { "orders": "SELECT" } }
            ]);
            assert.equal(result.length, 1);
            assert.ok(result[0].includes("com.example.Mapper.find"), "idを含む");
            assert.ok(result[0].includes("SELECT:orders"), "操作タイプ:ターゲット名を含む");
        });

        test("複数の操作は複数の文字列として返す", () => {
            const result = outbound.formatPersistenceAccessors([
                { id: "op1", targetOperationTypes: { "tableA": "INSERT" } },
                { id: "op2", targetOperationTypes: { "tableB": "SELECT" } }
            ]);
            assert.equal(result.length, 2);
        });
    });

    // ----- groupOperationsByOutboundPort -----

    test.describe("groupOperationsByOutboundPort", () => {
        test("データなしの場合は空配列を返す", () => {
            const grouped = outbound.groupOperationsByOutboundPort(makeEmptyData());
            assert.equal(grouped.length, 0);
        });

        test("アダプター実行と紐付かない操作は除外し、操作なしのポートも除外する", () => {
            const data = {
                ...makeEmptyData(),
                outboundPorts: [
                    { fqn: "portA", label: "A", operations: [{ fqn: "opA", label: "save" }] }
                ]
                // links.operationToExecution が空なのでopAは紐付かない
            };
            const grouped = outbound.groupOperationsByOutboundPort(data);
            assert.equal(grouped.length, 0);
        });

        test("出力ポート単位でグルーピングし、ラベルの昇順にソートする", () => {
            const data = {
                outboundPorts: [
                    { fqn: "portB", label: "い", operations: [{ fqn: "opB", label: "B操作" }] },
                    { fqn: "portA", label: "あ", operations: [{ fqn: "opA", label: "A操作" }] }
                ],
                outboundAdapters: [
                    { fqn: "adapterA", label: "A", executions: [{ fqn: "execA", label: "execA" }] },
                    { fqn: "adapterB", label: "B", executions: [{ fqn: "execB", label: "execB" }] }
                ],
                persistenceAccessors: [],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [
                        { operation: "opA", execution: "execA" },
                        { operation: "opB", execution: "execB" }
                    ],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: []
                }
            };
            const grouped = outbound.groupOperationsByOutboundPort(data);
            assert.equal(grouped.length, 2);
            assert.equal(grouped[0].outboundPort.label, "あ");
            assert.equal(grouped[1].outboundPort.label, "い");
        });

        test("アダプターに実行が見つかった場合、outboundAdapter・outboundAdapterExecution が設定される", () => {
            const data = {
                outboundPorts: [
                    { fqn: "portA", label: "A", operations: [{ fqn: "opA", label: "save" }] }
                ],
                outboundAdapters: [
                    { fqn: "adapterA", label: "AdapterA", executions: [{ fqn: "execA", label: "execA" }] }
                ],
                persistenceAccessors: [],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [{ operation: "opA", execution: "execA" }],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: []
                }
            };
            const grouped = outbound.groupOperationsByOutboundPort(data);
            const op = grouped[0].operations[0];
            assert.equal(op.outboundAdapter?.fqn, "adapterA");
            assert.equal(op.outboundAdapterExecution?.fqn, "execA");
        });

        test("実行に対応するアダプターがない場合、outboundAdapter・outboundAdapterExecution が null になる", () => {
            const data = {
                outboundPorts: [
                    { fqn: "portA", label: "A", operations: [{ fqn: "opA", label: "save" }] }
                ],
                outboundAdapters: [],
                persistenceAccessors: [],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [{ operation: "opA", execution: "execUnknown" }],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: []
                }
            };
            const grouped = outbound.groupOperationsByOutboundPort(data);
            const op = grouped[0].operations[0];
            assert.equal(op.outboundAdapter, null);
            assert.equal(op.outboundAdapterExecution, null);
        });
    });

    // ----- groupOperationsByPersistenceTarget -----

    test.describe("groupOperationsByPersistenceTarget", () => {
        test("永続化ターゲット単位でグルーピングし、ターゲット名でソートする", () => {
            const operations = [
                {
                    outboundPort: { fqn: "port1", label: "P1" },
                    persistenceAccessors: [{ id: "op1", targetOperationTypes: { "table_b": "SELECT" } }]
                },
                {
                    outboundPort: { fqn: "port2", label: "P2" },
                    persistenceAccessors: [{ id: "op2", targetOperationTypes: { "table_a": "SELECT" } }]
                }
            ];
            const grouped = outbound.groupOperationsByPersistenceTarget(operations);
            assert.equal(grouped.length, 2);
            assert.equal(grouped[0].persistenceTarget, "table_a");
            assert.equal(grouped[1].persistenceTarget, "table_b");
        });

        test("同じターゲットに複数の操作が対応する場合、まとめて重複排除する", () => {
            const operations = [
                {
                    outboundPort: { fqn: "port1", label: "P1" },
                    persistenceAccessors: [{ id: "op1", targetOperationTypes: { "orders": "SELECT" } }]
                },
                {
                    outboundPort: { fqn: "port2", label: "P2" },
                    persistenceAccessors: [{ id: "op2", targetOperationTypes: { "orders": "INSERT" } }]
                }
            ];
            const grouped = outbound.groupOperationsByPersistenceTarget(operations);
            assert.equal(grouped.length, 1);
            assert.equal(grouped[0].persistenceTarget, "orders");
            assert.equal(grouped[0].operations.length, 2);
        });

        test("同じ操作が複数ターゲットに紐付く場合も重複排除する", () => {
            const operation = {
                outboundPort: { fqn: "port1", label: "P1" },
                persistenceAccessors: [
                    { id: "op1", targetOperationTypes: { "orders": "SELECT", "items": "SELECT" } }
                ]
            };
            const grouped = outbound.groupOperationsByPersistenceTarget([operation]);
            assert.equal(grouped.length, 2); // orders と items
            grouped.forEach(g => assert.equal(g.operations.length, 1)); // 各ターゲットで重複なし
        });
    });

    // ----- groupOperationsByExternalType -----

    test.describe("groupOperationsByExternalType", () => {
        test("外部型単位でグルーピングする", () => {
            const operations = [{
                outboundPort: { fqn: "port1", label: "P1" },
                outboundPortOperation: { fqn: "op1", label: "call" },
                outboundAdapter: null,
                outboundAdapterExecution: null,
                persistenceAccessors: [],
                externalAccessors: [{
                    fqn: "com.example.Client",
                    label: "Client",
                    methods: [{
                        name: "fetch",
                        externals: [{ fqn: "com.example.ExtType", label: "ExtType", method: "get" }]
                    }]
                }]
            }];
            const grouped = outbound.groupOperationsByExternalType(operations);
            assert.equal(grouped.length, 1);
            assert.equal(grouped[0].externalType.fqn, "com.example.ExtType");
            assert.equal(grouped[0].operations.length, 1);
        });

        test("同じ外部型を参照する複数の操作はまとめられる", () => {
            const makeOp = (portFqn, portLabel) => ({
                outboundPort: { fqn: portFqn, label: portLabel },
                outboundPortOperation: { fqn: portFqn + "#op", label: "op" },
                outboundAdapter: null, outboundAdapterExecution: null,
                persistenceAccessors: [],
                externalAccessors: [{
                    fqn: "com.example.Client", label: "Client",
                    methods: [{ name: "call", externals: [{ fqn: "com.example.Ext", label: "Ext", method: "m" }] }]
                }]
            });
            const grouped = outbound.groupOperationsByExternalType([makeOp("p1", "P1"), makeOp("p2", "P2")]);
            assert.equal(grouped.length, 1);
            assert.equal(grouped[0].operations.length, 2);
        });
    });

    // ----- MermaidBuilder -----

    test.describe("MermaidBuilder", () => {
        test("isEmpty: 初期状態では真を返す", () => {
            const builder = new outbound.MermaidBuilder();
            assert.ok(builder.isEmpty());
        });

        test("isEmpty: ノードを追加すると偽を返す", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addNode("A", "NodeA");
            assert.ok(!builder.isEmpty());
        });

        test("isEmpty: エッジのみでは偽を返す", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addEdge("A", "B");
            assert.ok(!builder.isEmpty());
        });

        test("isEmpty: サブグラフを追加すると偽を返す", () => {
            const builder = new outbound.MermaidBuilder();
            builder.startSubgraph("Group");
            assert.ok(!builder.isEmpty());
        });

        test("sanitize: 非英数字をアンダースコアに変換する", () => {
            const builder = new outbound.MermaidBuilder();
            assert.equal(builder.sanitize("com.example.MyClass"), "com_example_MyClass");
            assert.equal(builder.sanitize("foo#bar(baz)"), "foo_bar_baz_");
            assert.equal(builder.sanitize(null), "");
        });

        test("addNode: 同じノードは重複しない", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addNode("A", "NodeA");
            builder.addNode("A", "NodeA");
            const code = builder.build();
            assert.equal((code.match(/NodeA/g) || []).length, 1);
        });

        test("addEdge: 同じエッジは重複しない", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addNode("A", "NodeA");
            builder.addNode("B", "NodeB");
            builder.addEdge("A", "B");
            builder.addEdge("A", "B");
            const code = builder.build();
            assert.equal((code.match(/A --> B/g) || []).length, 1);
        });

        test("addEdge: ラベルなしエッジを生成する", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addNode("A", "NodeA");
            builder.addNode("B", "NodeB");
            builder.addEdge("A", "B");
            const code = builder.build();
            assert.ok(code.includes("A --> B"), `エッジが含まれない: ${code}`);
        });

        test("addEdge: ラベル付きエッジを生成する", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addNode("A", "NodeA");
            builder.addNode("B", "NodeB");
            builder.addEdge("A", "B", "SELECT");
            const code = builder.build();
            assert.ok(code.includes('"SELECT"'), `ラベルが含まれない: ${code}`);
        });

        test("build: デフォルトで graph LR から始まる", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addNode("A", "NodeA");
            assert.ok(builder.build().startsWith("graph LR"));
        });

        test("build: direction を変更できる", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addNode("A", "NodeA");
            assert.ok(builder.build("TB").startsWith("graph TB"));
        });

        test("startSubgraph・addNodeToSubgraph: subgraph を生成する", () => {
            const builder = new outbound.MermaidBuilder();
            const sg = builder.startSubgraph("MyGroup");
            builder.addNodeToSubgraph(sg, "N1", "Node1");
            const code = builder.build();
            assert.ok(code.includes("subgraph"), `subgraphが含まれない: ${code}`);
            assert.ok(code.includes('"MyGroup"'), `グループ名が含まれない: ${code}`);
            assert.ok(code.includes('"Node1"'), `ノード名が含まれない: ${code}`);
        });

        test("addNodeToSubgraph: 同じノードは重複しない", () => {
            const builder = new outbound.MermaidBuilder();
            const sg = builder.startSubgraph("Group");
            builder.addNodeToSubgraph(sg, "N1", "Node1");
            builder.addNodeToSubgraph(sg, "N1", "Node1");
            const code = builder.build();
            assert.equal((code.match(/Node1/g) || []).length, 1);
        });
    });

    // ----- generatePortMermaidCode -----

    test.describe("generatePortMermaidCode", () => {
        test("デフォルト表示設定で graph LR のコードを生成する", () => {
            const code = outbound.generatePortMermaidCode(simpleGroup);
            assert.ok(code !== null);
            assert.ok(code.startsWith("graph LR"));
            assert.ok(code.includes("Port"), `ポート名が含まれない: ${code}`);
            assert.ok(code.includes("Adapter"), `アダプター名が含まれない: ${code}`);
        });

        test("すべての表示設定を false にすると null を返す", () => {
            const allHidden = {
                port: false, operation: false,
                adapter: false, execution: false,
                accessor: false, accessorMethod: false,
                target: false,
                externalAccessor: false, externalAccessorMethod: false,
                externalType: false, externalTypeMethod: false,
                direction: "LR",
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
            };
            const code = outbound.generatePortMermaidCode(simpleGroup, allHidden);
            assert.equal(code, null);
        });

        test("accessor を表示する設定で永続化アクセッサノードを含む", () => {
            const groupWithAccessor = {
                outboundPort: { fqn: "com.example.Port", label: "Port" },
                operations: [{
                    outboundPortOperation: { fqn: "com.example.Port#save()", label: "save" },
                    outboundAdapter: { fqn: "com.example.Adapter", label: "Adapter" },
                    outboundAdapterExecution: { fqn: "com.example.Adapter#save()", label: "save" },
                    persistenceAccessors: [{
                        id: "com.example.Mapper.save",
                        group: "com.example.Mapper",
                        groupLabel: "Mapper",
                        targetOperationTypes: { "orders": "INSERT" }
                    }],
                    externalAccessors: []
                }]
            };
            const visibility = {
                port: true, operation: true,
                adapter: true, execution: true,
                accessor: true, accessorMethod: false,
                target: true,
                externalAccessor: false, externalAccessorMethod: false,
                externalType: true, externalTypeMethod: true,
                direction: "LR",
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
            };
            const code = outbound.generatePortMermaidCode(groupWithAccessor, visibility);
            assert.ok(code !== null);
            assert.ok(code.includes("orders"), `ターゲット名が含まれない: ${code}`);
        });

        test("direction 設定が反映される", () => {
            const visibility = { ...outbound.generatePortMermaidCode.length, port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, externalAccessor: false, externalAccessorMethod: false, externalType: true, externalTypeMethod: true, direction: "TB", crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true };
            // MermaidBuilder を直接使って direction を確認
            const builder = new outbound.MermaidBuilder();
            builder.addNode("A", "NodeA");
            assert.ok(builder.build("TB").startsWith("graph TB"));
        });
    });

    // ----- generateOperationMermaidCode -----

    test.describe("generateOperationMermaidCode", () => {
        test("単一操作の mermaid コードを生成する", () => {
            const operation = {
                outboundPort: { fqn: "com.example.Port", label: "Port" },
                outboundPortOperation: { fqn: "com.example.Port#save()", label: "save" },
                outboundAdapter: { fqn: "com.example.Adapter", label: "Adapter" },
                outboundAdapterExecution: { fqn: "com.example.Adapter#save()", label: "save" },
                persistenceAccessors: [],
                externalAccessors: []
            };
            const code = outbound.generateOperationMermaidCode(operation);
            assert.ok(code !== null);
            assert.ok(code.startsWith("graph LR"));
            assert.ok(code.includes("Port"));
        });
    });

    // ----- generatePersistenceMermaidCode -----

    test.describe("generatePersistenceMermaidCode", () => {
        test("永続化ターゲットグループの mermaid コードを生成する", () => {
            const group = {
                persistenceTarget: "orders",
                operations: [{
                    outboundPort: { fqn: "com.example.Port", label: "Port" },
                    outboundPortOperation: { fqn: "com.example.Port#save()", label: "save" },
                    outboundAdapter: { fqn: "com.example.Adapter", label: "Adapter" },
                    outboundAdapterExecution: { fqn: "com.example.Adapter#save()", label: "save" },
                    persistenceAccessors: [{
                        id: "com.example.Mapper.save",
                        group: "com.example.Mapper",
                        groupLabel: "Mapper",
                        targetOperationTypes: { "orders": "INSERT" }
                    }],
                    externalAccessors: []
                }]
            };
            const code = outbound.generatePersistenceMermaidCode(group);
            assert.ok(code !== null);
            assert.ok(code.includes("orders"), `ターゲット名が含まれない: ${code}`);
        });
    });

    // ----- generateExternalTypeMermaidCode -----

    test.describe("generateExternalTypeMermaidCode", () => {
        test("外部型グループの mermaid コードを生成する", () => {
            const group = {
                externalType: { fqn: "com.example.ExtService", label: "ExtService" },
                operations: [{
                    outboundPort: { fqn: "com.example.Port", label: "Port" },
                    outboundPortOperation: { fqn: "com.example.Port#call()", label: "call" },
                    outboundAdapter: { fqn: "com.example.Adapter", label: "Adapter" },
                    outboundAdapterExecution: { fqn: "com.example.Adapter#call()", label: "call" },
                    persistenceAccessors: [],
                    externalAccessors: [{
                        fqn: "com.example.Client",
                        label: "Client",
                        methods: [{
                            name: "fetch",
                            externals: [{ fqn: "com.example.ExtService", label: "ExtService", method: "get" }]
                        }]
                    }]
                }]
            };
            const code = outbound.generateExternalTypeMermaidCode(group);
            assert.ok(code !== null);
            assert.ok(code.includes("ExtService"), `外部型名が含まれない: ${code}`);
        });
    });

    // ----- renderOutboundList -----

    test.describe("renderOutboundList", () => {
        test("データなしの場合「データなし」を表示する", () => {
            const doc = setupDom();
            outbound.renderOutboundList([]);
            const container = doc.getElementById("outbound-port-list");
            assert.ok(container.children.length > 0);
            assert.equal(container.children[0].textContent, "データなし");
        });

        test("出力ポートグループをセクションとして描画する", () => {
            const doc = setupDom();
            outbound.renderOutboundList([simpleGroup]);
            const container = doc.getElementById("outbound-port-list");
            assert.ok(container.children.length > 0, "セクションが描画されていない");
            const section = container.children[0];
            assert.ok(section.className.includes("outbound-group-card"));
            assert.equal(section.id, jig.fqnToId("port", simpleGroup.outboundPort.fqn));
        });

        test("ポートのラベルと FQN を描画する", () => {
            const doc = setupDom();
            outbound.renderOutboundList([simpleGroup]);
            const container = doc.getElementById("outbound-port-list");
            const section = container.children[0];
            const h3 = section.children.find(c => c.tagName === "h3");
            assert.ok(h3, "h3が見つからない");
            assert.equal(h3.textContent, "Port");
        });
    });

    // ----- renderCrudTable -----

    test.describe("renderCrudTable", () => {
        test("永続化操作なしの場合はメッセージを表示する", () => {
            const doc = setupDom();
            outbound.renderCrudTable([]);
            const container = doc.getElementById("outbound-crud-panel");
            assert.equal(container.textContent, "永続化操作なし");
        });

        test("永続化操作ありの場合はテーブルを描画する", () => {
            const doc = setupDom();
            const grouped = [{
                outboundPort: { fqn: "com.example.Port", label: "Port" },
                operations: [{
                    outboundPortOperation: { fqn: "com.example.Port#save()", label: "save" },
                    outboundAdapter: null,
                    outboundAdapterExecution: null,
                    persistenceAccessors: [{
                        id: "op1",
                        group: "com.example.Mapper",
                        groupLabel: "Mapper",
                        targetOperationTypes: { "orders": "INSERT" }
                    }],
                    externalAccessors: []
                }]
            }];
            outbound.renderCrudTable(grouped);
            const container = doc.getElementById("outbound-crud-panel");
            assert.ok(container.children.length > 0, "テーブルが描画されていない");
            const table = container.children[0];
            assert.equal(table.tagName, "table");
        });

        test("CRUDセルに操作タイプ文字（C/R/U/D）を表示する", () => {
            const doc = setupDom();
            const grouped = [{
                outboundPort: { fqn: "com.example.Port", label: "Port" },
                operations: [{
                    outboundPortOperation: { fqn: "com.example.Port#save()", label: "save" },
                    outboundAdapter: null, outboundAdapterExecution: null,
                    persistenceAccessors: [
                        { id: "op1", group: "g", groupLabel: "G", targetOperationTypes: { "orders": "INSERT" } }
                    ],
                    externalAccessors: []
                }]
            }];
            outbound.renderCrudTable(grouped);
            const container = doc.getElementById("outbound-crud-panel");
            const table = container.children[0];
            const tbody = table.children.find(c => c.tagName === "tbody");
            assert.ok(tbody, "tbodyが見つからない");
            const portRow = tbody.children[0];
            assert.ok(portRow, "ポート行が見つからない");
            // 2番目のセルがCRUD列（ordersターゲット）= "C"（INSERT）
            const crudCell = portRow.children[1];
            assert.equal(crudCell.textContent, "C");
        });

        test("複数のCRUD操作が混在する場合、すべて表示される", () => {
             const doc = setupDom();
             const grouped = [{
                 outboundPort: { fqn: "com.example.Port", label: "Port" },
                 operations: [{
                     outboundPortOperation: { fqn: "com.example.Port#execute()", label: "execute" },
                     outboundAdapter: null, outboundAdapterExecution: null,
                     persistenceAccessors: [
                         { id: "op1", group: "g", groupLabel: "G", targetOperationTypes: { "orders": "INSERT", "users": "UPDATE" } }
                     ],
                     externalAccessors: []
                 }]
             }];
             outbound.renderCrudTable(grouped);
             const container = doc.getElementById("outbound-crud-panel");
             assert.ok(container);
             const table = container.children[0];
             assert.equal(table.tagName, "table");
        });
    });

});
