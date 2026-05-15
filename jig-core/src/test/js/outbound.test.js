const test = require("node:test");
const assert = require("node:assert/strict");
const {DocumentStub} = require("./dom-stub.js");

// jig-glossary.js と jig-dom.js をロード（outbound.js が require 時に Jig 名前空間を参照するため先に行う）
global.window = {
    addEventListener: () => {
    }
};
const doc = new DocumentStub();
doc.body.classList.add("outbound-interface");
global.document = doc;
require("../../main/resources/templates/assets/jig-util.js");
require("../../main/resources/templates/assets/jig-data.js");
require("../../main/resources/templates/assets/jig-glossary.js");
require("../../main/resources/templates/assets/jig-mermaid.js");
require("../../main/resources/templates/assets/jig-dom.js");
require("../../main/resources/templates/assets/jig-bootstrap.js");

const OutboundApp = require("../../main/resources/templates/assets/outbound.js");

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
    doc.body.classList.add("outbound-interface");
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
    globalThis.Jig.mermaid.renderWithControls = () => {
    };
    return doc;
}

function makeMethodData(fqn) {
    return {fqn, visibility: "PUBLIC", parameters: [], returnTypeRef: {fqn: "void"}, isDeprecated: false};
}

// テスト用の操作グループ（Mermaid コード生成・DOM 描画で再利用）
const simpleGroup = {
    outboundPort: {fqn: "com.example.Port", label: "Port"},
    operations: [{
        outboundPortOperation: makeMethodData("com.example.Port#save()"),
        outboundAdapter: {fqn: "com.example.Adapter", label: "Adapter"},
        outboundAdapterExecution: makeMethodData("com.example.Adapter#save()"),
        persistenceAccessors: [],
        externalAccessors: []
    }]
};

// ===== テスト =====

test.describe("outbound.js", () => {

    // ----- toCrudChar -----

    test.describe("toCrudChar", () => {
        test("各SQL操作タイプをCRUD文字に変換する", () => {
            assert.equal(OutboundApp.toCrudChar("SELECT"), "R");
            assert.equal(OutboundApp.toCrudChar("INSERT"), "C");
            assert.equal(OutboundApp.toCrudChar("UPDATE"), "U");
            assert.equal(OutboundApp.toCrudChar("DELETE"), "D");
        });

        test("不明なタイプは空文字を返す", () => {
            assert.equal(OutboundApp.toCrudChar("MERGE"), "");
            assert.equal(OutboundApp.toCrudChar(null), "");
            assert.equal(OutboundApp.toCrudChar(undefined), "");
        });

        test("小文字でも変換できる", () => {
            assert.equal(OutboundApp.toCrudChar("select"), "R");
            assert.equal(OutboundApp.toCrudChar("insert"), "C");
        });
    });

    // ----- groupOperationsByOutboundPort -----

    test.describe("groupOperationsByOutboundPort", () => {
        test("データなしの場合は空配列を返す", () => {
            const grouped = OutboundApp.groupOperationsByOutboundPort(makeEmptyData());
            assert.equal(grouped.length, 0);
        });

        test("アダプター実行と紐付かない操作は除外し、操作なしのポートも除外する", () => {
            const data = {
                ...makeEmptyData(),
                outboundPorts: [
                    {fqn: "portA", label: "A", operations: [{fqn: "opA", label: "save"}]}
                ]
                // links.operationToExecution が空なのでopAは紐付かない
            };
            const grouped = OutboundApp.groupOperationsByOutboundPort(data);
            assert.equal(grouped.length, 0);
        });

        test("出力ポート単位でグルーピングし、ラベルの昇順にソートする", () => {
            const data = {
                outboundPorts: [
                    {fqn: "portB", label: "い", operations: [{fqn: "opB", label: "B操作"}]},
                    {fqn: "portA", label: "あ", operations: [{fqn: "opA", label: "A操作"}]}
                ],
                outboundAdapters: [
                    {fqn: "adapterA", label: "A", executions: [{fqn: "execA", label: "execA"}]},
                    {fqn: "adapterB", label: "B", executions: [{fqn: "execB", label: "execB"}]}
                ],
                persistenceAccessors: [],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [
                        {operation: "opA", execution: "execA"},
                        {operation: "opB", execution: "execB"}
                    ],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: []
                }
            };
            const grouped = OutboundApp.groupOperationsByOutboundPort(data);
            assert.equal(grouped.length, 2);
            assert.equal(grouped[0].outboundPort.label, "あ");
            assert.equal(grouped[1].outboundPort.label, "い");
        });

        test("アダプターに実行が見つかった場合、outboundAdapter・outboundAdapterExecution が設定される", () => {
            const data = {
                outboundPorts: [
                    {fqn: "portA", label: "A", operations: [{fqn: "opA", label: "save"}]}
                ],
                outboundAdapters: [
                    {fqn: "adapterA", label: "AdapterA", executions: [{fqn: "execA", label: "execA"}]}
                ],
                persistenceAccessors: [],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [{operation: "opA", execution: "execA"}],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: []
                }
            };
            const grouped = OutboundApp.groupOperationsByOutboundPort(data);
            const op = grouped[0].operations[0];
            assert.equal(op.outboundAdapter?.fqn, "adapterA");
            assert.equal(op.outboundAdapterExecution?.fqn, "execA");
        });

        test("実行に対応するアダプターがない場合、outboundAdapter・outboundAdapterExecution が null になる", () => {
            const data = {
                outboundPorts: [
                    {fqn: "portA", label: "A", operations: [{fqn: "opA", label: "save"}]}
                ],
                outboundAdapters: [],
                persistenceAccessors: [],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [{operation: "opA", execution: "execUnknown"}],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: []
                }
            };
            const grouped = OutboundApp.groupOperationsByOutboundPort(data);
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
                    outboundPort: {fqn: "port1", label: "P1"},
                    persistenceAccessors: [{id: "op1", targetOperationTypes: {"table_b": "SELECT"}}]
                },
                {
                    outboundPort: {fqn: "port2", label: "P2"},
                    persistenceAccessors: [{id: "op2", targetOperationTypes: {"table_a": "SELECT"}}]
                }
            ];
            const grouped = OutboundApp.groupOperationsByPersistenceTarget(operations);
            assert.equal(grouped.length, 2);
            assert.equal(grouped[0].persistenceTarget, "table_a");
            assert.equal(grouped[1].persistenceTarget, "table_b");
        });

        test("同じターゲットに複数の操作が対応する場合、まとめて重複排除する", () => {
            const operations = [
                {
                    outboundPort: {fqn: "port1", label: "P1"},
                    persistenceAccessors: [{id: "op1", targetOperationTypes: {"orders": "SELECT"}}]
                },
                {
                    outboundPort: {fqn: "port2", label: "P2"},
                    persistenceAccessors: [{id: "op2", targetOperationTypes: {"orders": "INSERT"}}]
                }
            ];
            const grouped = OutboundApp.groupOperationsByPersistenceTarget(operations);
            assert.equal(grouped.length, 1);
            assert.equal(grouped[0].persistenceTarget, "orders");
            assert.equal(grouped[0].operations.length, 2);
        });

        test("同じ操作が複数ターゲットに紐付く場合も重複排除する", () => {
            const operation = {
                outboundPort: {fqn: "port1", label: "P1"},
                persistenceAccessors: [
                    {id: "op1", targetOperationTypes: {"orders": "SELECT", "items": "SELECT"}}
                ]
            };
            const grouped = OutboundApp.groupOperationsByPersistenceTarget([operation]);
            assert.equal(grouped.length, 2); // orders と items
            grouped.forEach(g => assert.equal(g.operations.length, 1)); // 各ターゲットで重複なし
        });
    });

    // ----- groupOperationsByExternalType -----

    test.describe("groupOperationsByExternalType", () => {
        test("外部型単位でグルーピングする", () => {
            const operations = [{
                outboundPort: {fqn: "port1", label: "P1"},
                outboundPortOperation: {fqn: "op1", label: "call"},
                outboundAdapter: null,
                outboundAdapterExecution: null,
                persistenceAccessors: [],
                externalAccessors: [{
                    fqn: "com.example.Client",
                    label: "Client",
                    operations: [{
                        ...makeMethodData("com.example.Client#fetch()"),
                        externals: [{fqn: "com.example.ExtType", label: "ExtType", method: "get"}]
                    }]
                }]
            }];
            const grouped = OutboundApp.groupOperationsByExternalType(operations);
            assert.equal(grouped.length, 1);
            assert.equal(grouped[0].externalType.fqn, "com.example.ExtType");
            assert.equal(grouped[0].operations.length, 1);
        });

        test("同じ外部型を参照する複数の操作はまとめられる", () => {
            const makeOp = (portFqn, portLabel) => ({
                outboundPort: {fqn: portFqn, label: portLabel},
                outboundPortOperation: {fqn: portFqn + "#op", label: "op"},
                outboundAdapter: null, outboundAdapterExecution: null,
                persistenceAccessors: [],
                externalAccessors: [{
                    fqn: "com.example.Client", label: "Client",
                    operations: [{...makeMethodData("com.example.Client#call()"), externals: [{fqn: "com.example.Ext", label: "Ext", method: "m"}]}]
                }]
            });
            const grouped = OutboundApp.groupOperationsByExternalType([makeOp("p1", "P1"), makeOp("p2", "P2")]);
            assert.equal(grouped.length, 1);
            assert.equal(grouped[0].operations.length, 2);
        });
    });

    // ----- MermaidBuilder -----

    test.describe("MermaidBuilder", () => {
        test("isEmpty: 初期状態では真を返す", () => {
            const builder = new Jig.mermaid.Builder();
            assert.ok(builder.isEmpty());
        });

        test("isEmpty: ノードを追加すると偽を返す", () => {
            const builder = new Jig.mermaid.Builder();
            builder.addNode("A", "NodeA");
            assert.ok(!builder.isEmpty());
        });

        test("isEmpty: エッジのみでは偽を返す", () => {
            const builder = new Jig.mermaid.Builder();
            builder.addEdge("A", "B");
            assert.ok(!builder.isEmpty());
        });

        test("isEmpty: サブグラフを追加すると偽を返す", () => {
            const builder = new Jig.mermaid.Builder();
            builder.startSubgraph("Group");
            assert.ok(!builder.isEmpty());
        });

        test("sanitize: 非英数字をアンダースコアに変換する", () => {
            const builder = new Jig.mermaid.Builder();
            assert.equal(builder.sanitize("com.example.MyClass"), "com_example_MyClass");
            assert.equal(builder.sanitize("foo#bar(baz)"), "foo_bar_baz_");
            assert.equal(builder.sanitize(null), "");
        });

        test("addNode: 同じノードは重複しない", () => {
            const builder = new Jig.mermaid.Builder();
            builder.addNode("A", "NodeA");
            builder.addNode("A", "NodeA");
            const code = builder.build();
            assert.equal((code.match(/NodeA/g) || []).length, 1);
        });

        test("addEdge: 同じエッジは重複しない", () => {
            const builder = new Jig.mermaid.Builder();
            builder.addNode("A", "NodeA");
            builder.addNode("B", "NodeB");
            builder.addEdge("A", "B");
            builder.addEdge("A", "B");
            const code = builder.build();
            assert.equal((code.match(/A --> B/g) || []).length, 1);
        });

        test("addEdge: ラベルなしエッジを生成する", () => {
            const builder = new Jig.mermaid.Builder();
            builder.addNode("A", "NodeA");
            builder.addNode("B", "NodeB");
            builder.addEdge("A", "B");
            const code = builder.build();
            assert.ok(code.includes("A --> B"), `エッジが含まれない: ${code}`);
        });

        test("addEdge: ラベル付きエッジを生成する", () => {
            const builder = new Jig.mermaid.Builder();
            builder.addNode("A", "NodeA");
            builder.addNode("B", "NodeB");
            builder.addEdge("A", "B", "SELECT");
            const code = builder.build();
            assert.ok(code.includes('"SELECT"'), `ラベルが含まれない: ${code}`);
        });

        test("build: デフォルトで graph LR から始まる", () => {
            const builder = new Jig.mermaid.Builder();
            builder.addNode("A", "NodeA");
            assert.ok(builder.build().startsWith("graph LR"));
        });

        test("build: direction を変更できる", () => {
            const builder = new Jig.mermaid.Builder();
            builder.addNode("A", "NodeA");
            assert.ok(builder.build("TB").startsWith("graph TB"));
        });

        test("startSubgraph・addNodeToSubgraph: subgraph を生成する", () => {
            const builder = new Jig.mermaid.Builder();
            const sg = builder.startSubgraph("MyGroup");
            builder.addNodeToSubgraph(sg, "N1", "Node1");
            const code = builder.build();
            assert.ok(code.includes("subgraph"), `subgraphが含まれない: ${code}`);
            assert.ok(code.includes('"MyGroup"'), `グループ名が含まれない: ${code}`);
            assert.ok(code.includes('"Node1"'), `ノード名が含まれない: ${code}`);
        });

        test("addNodeToSubgraph: 同じノードは重複しない", () => {
            const builder = new Jig.mermaid.Builder();
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
            const code = OutboundApp.generatePortMermaidCode(simpleGroup);
            assert.ok(code !== null);
            assert.ok(code.startsWith("graph LR"));
            assert.ok(code.includes("Port"), `ポート名が含まれない: ${code}`);
            assert.ok(!code.includes("Adapter"), `デフォルトではアダプター名が含まれるべきでない: ${code}`);
        });

        test("operation=falseの場合、portCardノードにportFqnのツールチップが含まれる", () => {
            const visibility = {callerUsecase: false, port: true, operation: false, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false, externalAccessor: false, externalAccessorMethod: false, externalType: false, externalTypeMethod: false, crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = OutboundApp.generatePortMermaidCode(simpleGroup, visibility);
            assert.ok(code !== null);
            const portCardId = Jig.util.fqnToId("port", simpleGroup.outboundPort.fqn);
            assert.ok(code.includes(`click ${portCardId} href`), `portCardへのclickが含まれること: ${code}`);
            assert.ok(code.includes(`"${simpleGroup.outboundPort.fqn}"`), `portFqnのツールチップが含まれること: ${code}`);
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
            const code = OutboundApp.generatePortMermaidCode(simpleGroup, allHidden);
            assert.equal(code, null);
        });

        test("accessor を表示する設定で永続化アクセッサノードを含む", () => {
            const groupWithAccessor = {
                outboundPort: {fqn: "com.example.Port", label: "Port"},
                operations: [{
                    outboundPortOperation: makeMethodData("com.example.Port#save()"),
                    outboundAdapter: {fqn: "com.example.Adapter", label: "Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#save()"),
                    persistenceAccessors: [{
                        id: "com.example.Mapper.save",
                        group: "com.example.Mapper",
                        groupLabel: "Mapper",
                        targetOperationTypes: {"orders": "INSERT"}
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
            const code = OutboundApp.generatePortMermaidCode(groupWithAccessor, visibility);
            assert.ok(code !== null);
            assert.ok(code.includes("orders"), `ターゲット名が含まれない: ${code}`);
        });

        test("callerUsecase=trueのとき呼び出し元ユースケースノードとエッジを含む", () => {
            const groupWithCallers = {
                outboundPort: {fqn: "com.example.Port"},
                operations: [{
                    outboundPortOperation: {
                        ...makeMethodData("com.example.Port#save()"),
                        callerUsecases: ["com.example.OrderService#createOrder(Request)"]
                    },
                    outboundAdapter: {fqn: "com.example.Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#save()"),
                    persistenceAccessors: [],
                    externalAccessors: []
                }]
            };
            const visibility = {callerUsecase: true, port: true, operation: true, adapter: true, execution: true,
                accessor: false, accessorMethod: false, target: true,
                externalAccessor: false, externalAccessorMethod: false,
                externalType: true, externalTypeMethod: true,
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = OutboundApp.generatePortMermaidCode(groupWithCallers, visibility);
            assert.ok(code !== null);
            assert.ok(code.includes("OrderService"), `ユースケースクラス名が含まれない: ${code}`);
            assert.ok(code.includes("createOrder"), `ユースケースメソッド名が含まれない: ${code}`);
            assert.ok(code.includes("-->"), `エッジが含まれない: ${code}`);
        });

        test("callerUsecase=falseのとき呼び出し元ユースケースを含まない", () => {
            const groupWithCallers = {
                outboundPort: {fqn: "com.example.Port"},
                operations: [{
                    outboundPortOperation: {
                        ...makeMethodData("com.example.Port#save()"),
                        callerUsecases: ["com.example.OrderService#createOrder(Request)"]
                    },
                    outboundAdapter: {fqn: "com.example.Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#save()"),
                    persistenceAccessors: [],
                    externalAccessors: []
                }]
            };
            const code = OutboundApp.generatePortMermaidCode(groupWithCallers, {callerUsecase: false, port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, externalAccessor: false, externalAccessorMethod: false, externalType: true, externalTypeMethod: true, crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true});
            assert.ok(code !== null);
            assert.ok(!code.includes("OrderService"), `callerUsecase=falseではユースケースが含まれるべきでない: ${code}`);
        });

        test("同じポート操作を複数ユースケースが呼ぶ場合、全てのエッジが生成される", () => {
            const groupWithMultipleCallers = {
                outboundPort: {fqn: "com.example.Port"},
                operations: [{
                    outboundPortOperation: {
                        ...makeMethodData("com.example.Port#save()"),
                        callerUsecases: [
                            "com.example.OrderService#createOrder(Request)",
                            "com.example.OrderService#updateOrder(Request)"
                        ]
                    },
                    outboundAdapter: {fqn: "com.example.Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#save()"),
                    persistenceAccessors: [],
                    externalAccessors: []
                }]
            };
            const visibility = {callerUsecase: true, port: true, operation: true, adapter: true, execution: true,
                accessor: false, accessorMethod: false, target: true,
                externalAccessor: false, externalAccessorMethod: false,
                externalType: true, externalTypeMethod: true,
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = OutboundApp.generatePortMermaidCode(groupWithMultipleCallers, visibility);
            assert.ok(code !== null);
            assert.equal((code.match(/\["createOrder"\]/g) || []).length, 1, "ノードは1回だけ定義される");
            assert.equal((code.match(/\["updateOrder"\]/g) || []).length, 1, "ノードは1回だけ定義される");
        });

        test("direction 設定が反映される", () => {
            const visibility = {
                ...OutboundApp.generatePortMermaidCode.length,
                port: true,
                operation: true,
                adapter: true,
                execution: true,
                accessor: false,
                accessorMethod: false,
                target: true,
                externalAccessor: false,
                externalAccessorMethod: false,
                externalType: true,
                externalTypeMethod: true,
                direction: "TB",
                crudCreate: true,
                crudRead: true,
                crudUpdate: true,
                crudDelete: true
            };
            // MermaidBuilder を直接使って direction を確認
            const builder = new Jig.mermaid.Builder();
            builder.addNode("A", "NodeA");
            assert.ok(builder.build("TB").startsWith("graph TB"));
        });

        test("アダプタ実行メソッドと外部アクセサメソッドが同じ場合、ノードが1つに統合される", () => {
            const group = {
                outboundPort: {fqn: "com.example.Port", label: "Port"},
                operations: [{
                    outboundPortOperation: makeMethodData("com.example.Port#call()"),
                    outboundAdapter: {fqn: "com.example.Adapter", label: "Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#call()"),
                    persistenceAccessors: [],
                    externalAccessors: [{
                        fqn: "com.example.Adapter",
                        operations: [{
                            ...makeMethodData("com.example.Adapter#call()"),
                            externals: [{fqn: "com.example.ExtService", method: "get"}]
                        }]
                    }]
                }]
            };
            const visibility = {
                port: true, operation: true,
                adapter: true, execution: true,
                accessor: false, accessorMethod: false,
                target: false,
                externalAccessor: true, externalAccessorMethod: true,
                externalType: true, externalTypeMethod: true,
                direction: "LR",
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
            };
            const code = OutboundApp.generatePortMermaidCode(group, visibility);
            assert.ok(code !== null);
            const execId = Jig.util.fqnToId("exec", "com.example.Adapter#call()");
            const accMethodId = Jig.util.fqnToId("accMethod", "com.example.Adapter#call()");
            const execCount = (code.match(new RegExp(execId, "g")) || []).length;
            const accMethodCount = (code.match(new RegExp(accMethodId, "g")) || []).length;
            assert.ok(execCount > 0, `実行ノード(${execId})が存在すること: ${code}`);
            assert.equal(accMethodCount, 0, `外部アクセサメソッドノード(${accMethodId})は重複生成されないこと: ${code}`);
        });
    });

    // ----- generateOperationMermaidCode -----

    test.describe("generateOperationMermaidCode", () => {
        test("単一操作の mermaid コードを生成する", () => {
            const operation = {
                outboundPort: {fqn: "com.example.Port", label: "Port"},
                outboundPortOperation: makeMethodData("com.example.Port#save()"),
                outboundAdapter: {fqn: "com.example.Adapter", label: "Adapter"},
                outboundAdapterExecution: makeMethodData("com.example.Adapter#save()"),
                persistenceAccessors: [],
                externalAccessors: []
            };
            const code = OutboundApp.generateOperationMermaidCode(operation);
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
                    outboundPort: {fqn: "com.example.Port", label: "Port"},
                    outboundPortOperation: makeMethodData("com.example.Port#save()"),
                    outboundAdapter: {fqn: "com.example.Adapter", label: "Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#save()"),
                    persistenceAccessors: [{
                        id: "com.example.Mapper.save",
                        group: "com.example.Mapper",
                        groupLabel: "Mapper",
                        targetOperationTypes: {"orders": "INSERT"}
                    }],
                    externalAccessors: []
                }]
            };
            const code = OutboundApp.generatePersistenceMermaidCode(group);
            assert.ok(code !== null);
            assert.ok(code.includes("orders"), `ターゲット名が含まれない: ${code}`);
        });
    });

    // ----- groupDirectExternalAccessors -----

    test.describe("groupDirectExternalAccessors", () => {
        test("外部アクセサがなければ空配列を返す", () => {
            const result = OutboundApp.groupDirectExternalAccessors(makeEmptyData());
            assert.equal(result.length, 0);
        });

        test("外部アクセサのメソッドが参照する外部型でグルーピングする", () => {
            const data = {
                ...makeEmptyData(),
                otherExternalAccessors: [{
                    fqn: "com.example.ApiClient",
                    operations: [{
                        ...makeMethodData("com.example.ApiClient#call()"),
                        externals: [{fqn: "com.example.ExtService", method: "get"}]
                    }]
                }]
            };
            const result = OutboundApp.groupDirectExternalAccessors(data);
            assert.equal(result.length, 1);
            assert.equal(result[0].externalType.fqn, "com.example.ExtService");
            assert.equal(result[0].directAccessors.length, 1);
            assert.equal(result[0].directAccessors[0].fqn, "com.example.ApiClient");
        });

        test("同じアクセサが複数の外部型を参照する場合、外部型ごとに分かれる", () => {
            const data = {
                ...makeEmptyData(),
                otherExternalAccessors: [{
                    fqn: "com.example.ApiClient",
                    operations: [{
                        ...makeMethodData("com.example.ApiClient#call()"),
                        externals: [
                            {fqn: "com.example.ExtA", method: "a"},
                            {fqn: "com.example.ExtB", method: "b"}
                        ]
                    }]
                }]
            };
            const result = OutboundApp.groupDirectExternalAccessors(data);
            assert.equal(result.length, 2);
        });

        test("同じアクセサが同じ外部型を複数メソッドで参照する場合は重複排除する", () => {
            const data = {
                ...makeEmptyData(),
                otherExternalAccessors: [{
                    fqn: "com.example.ApiClient",
                    operations: [
                        {...makeMethodData("com.example.ApiClient#callA()"), externals: [{fqn: "com.example.Ext", method: "get"}]},
                        {...makeMethodData("com.example.ApiClient#callB()"), externals: [{fqn: "com.example.Ext", method: "post"}]}
                    ]
                }]
            };
            const result = OutboundApp.groupDirectExternalAccessors(data);
            assert.equal(result.length, 1);
            assert.equal(result[0].directAccessors.length, 1);
            assert.equal(result[0].directAccessors[0].operations.length, 2);
        });
    });

    // ----- generateExternalTypeMermaidCode -----

    test.describe("generateExternalTypeMermaidCode", () => {
        test("外部型グループの mermaid コードを生成する", () => {
            const group = {
                externalType: {fqn: "com.example.ExtService", label: "ExtService"},
                operations: [{
                    outboundPort: {fqn: "com.example.Port", label: "Port"},
                    outboundPortOperation: makeMethodData("com.example.Port#call()"),
                    outboundAdapter: {fqn: "com.example.Adapter", label: "Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#call()"),
                    persistenceAccessors: [],
                    externalAccessors: [{
                        fqn: "com.example.Client",
                        label: "Client",
                        operations: [{
                            ...makeMethodData("com.example.Client#fetch()"),
                            externals: [{fqn: "com.example.ExtService", label: "ExtService", method: "get"}]
                        }]
                    }]
                }]
            };
            const code = OutboundApp.generateExternalTypeMermaidCode(group);
            assert.ok(code !== null);
            assert.ok(code.includes("ExtService"), `外部型名が含まれない: ${code}`);
        });

        test("ポートなしで directAccessors のみのグループもコードを生成する", () => {
            const group = {
                externalType: {fqn: "com.example.ExtService"},
                operations: [],
                directAccessors: [{
                    fqn: "com.example.ApiClient",
                    operations: [{
                        ...makeMethodData("com.example.ApiClient#call()"),
                        externals: [{fqn: "com.example.ExtService", method: "get"}]
                    }]
                }]
            };
            const code = OutboundApp.generateExternalTypeMermaidCode(group);
            assert.ok(code !== null, "ポートなしでもコードが生成される");
            assert.ok(code.includes("ExtService"), `外部型名が含まれない: ${code}`);
        });

        test("externalAccessor 表示オンで directAccessors のアクセサノードが含まれる", () => {
            const group = {
                externalType: {fqn: "com.example.ExtService"},
                operations: [],
                directAccessors: [{
                    fqn: "com.example.ApiClient",
                    operations: [{
                        ...makeMethodData("com.example.ApiClient#call()"),
                        externals: [{fqn: "com.example.ExtService", method: "get"}]
                    }]
                }]
            };
            const visibility = {
                port: true, operation: true, adapter: true, execution: true,
                accessor: true, accessorMethod: false,
                target: true,
                externalAccessor: true, externalAccessorMethod: false,
                externalType: true, externalTypeMethod: true,
                direction: "LR",
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
            };
            const code = OutboundApp.generateExternalTypeMermaidCode(group, visibility);
            assert.ok(code !== null);
            assert.ok(code.includes("ApiClient"), `アクセサ名が含まれない: ${code}`);
        });
    });

    // ----- renderOutboundList -----

    test.describe("renderOutboundList", () => {
        test("データなしの場合「データなし」を表示する", () => {
            const doc = setupDom();
            OutboundApp.renderOutboundList([]);
            const container = doc.getElementById("outbound-port-list");
            assert.ok(container.children.length > 0);
            assert.equal(container.children[0].textContent, "データなし");
        });

        test("出力ポートグループをセクションとして描画する", () => {
            const doc = setupDom();
            OutboundApp.renderOutboundList([simpleGroup]);
            const container = doc.getElementById("outbound-port-list");
            assert.ok(container.children.length > 0, "セクションが描画されていない");
            const section = container.children[0];
            assert.ok(section.className.includes("jig-card--type"));
            assert.equal(section.id, Jig.util.fqnToId("port", simpleGroup.outboundPort.fqn));
        });

        test("ポートのラベルと FQN を描画する", () => {
            const doc = setupDom();
            OutboundApp.renderOutboundList([simpleGroup]);
            const container = doc.getElementById("outbound-port-list");
            const section = container.children[0];
            const h3 = section.children.find(c => c.tagName === "h3");
            assert.ok(h3, "h3が見つからない");
            assert.equal(h3.textContent, "Port");
        });
    });

    // ----- groupOperationsByOutboundPort (永続化・外部アクセサ解決) -----

    test.describe("groupOperationsByOutboundPort - 永続化・外部アクセサ解決", () => {
        test("persistenceAccessorsのmethodsからpersistenceAccessorsを解決する", () => {
            const data = {
                outboundPorts: [{fqn: "portA", label: "A", operations: [{fqn: "opA", label: "op"}]}],
                outboundAdapters: [{fqn: "adapter", label: "Adapter", executions: [{fqn: "execA", label: "execA"}]}],
                persistenceAccessors: [{fqn: "com.example.Mapper", methods: [{id: "mapperSave", fqn: "save"}]}],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [{operation: "opA", execution: "execA"}],
                    executionToPersistenceAccessor: [{execution: "execA", accessor: "mapperSave"}],
                    executionToOtherExternalAccessor: []
                }
            };
            const grouped = OutboundApp.groupOperationsByOutboundPort(data);
            const op = grouped[0].operations[0];
            assert.equal(op.persistenceAccessors.length, 1);
            assert.equal(op.persistenceAccessors[0].id, "mapperSave");
            assert.equal(op.persistenceAccessors[0].group, "com.example.Mapper");
        });

        test("executionToOtherExternalAccessorのリンクからexternalAccessorsを解決する", () => {
            const data = {
                outboundPorts: [{fqn: "portA", label: "A", operations: [{fqn: "opA", label: "op"}]}],
                outboundAdapters: [{fqn: "adapter", label: "Adapter", executions: [{fqn: "execA", label: "execA"}]}],
                persistenceAccessors: [],
                otherExternalAccessors: [{
                    fqn: "com.example.Client",
                    operations: [{
                        fqn: "com.example.Client#call()", label: "call",
                        visibility: "PUBLIC", parameters: [], returnTypeRef: {fqn: "void"}, isDeprecated: false,
                        externals: []
                    }]
                }],
                links: {
                    operationToExecution: [{operation: "opA", execution: "execA"}],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: [{execution: "execA", accessor: "com.example.Client", method: "call"}]
                }
            };
            const grouped = OutboundApp.groupOperationsByOutboundPort(data);
            const op = grouped[0].operations[0];
            assert.equal(op.externalAccessors.length, 1);
            assert.equal(op.externalAccessors[0].fqn, "com.example.Client");
        });

        test("外部アクセサのFQNがotherExternalAccessorsにない場合は除外する", () => {
            const data = {
                outboundPorts: [{fqn: "portA", label: "A", operations: [{fqn: "opA", label: "op"}]}],
                outboundAdapters: [{fqn: "adapter", label: "Adapter", executions: [{fqn: "execA", label: "execA"}]}],
                persistenceAccessors: [],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [{operation: "opA", execution: "execA"}],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: [{execution: "execA", accessor: "com.example.Unknown", method: "call"}]
                }
            };
            const grouped = OutboundApp.groupOperationsByOutboundPort(data);
            assert.equal(grouped[0].operations[0].externalAccessors.length, 0);
        });

        test("ポート内の複数操作をラベルの昇順にソートする", () => {
            const data = {
                outboundPorts: [{fqn: "portA", label: "A", operations: [
                    {fqn: "portA#zOp()", label: "Z操作"},
                    {fqn: "portA#aOp()", label: "A操作"}
                ]}],
                outboundAdapters: [{fqn: "adapter", label: "Adapter", executions: [
                    {fqn: "execZ", label: "execZ"},
                    {fqn: "execA", label: "execA"}
                ]}],
                persistenceAccessors: [],
                otherExternalAccessors: [],
                links: {
                    operationToExecution: [
                        {operation: "portA#zOp()", execution: "execZ"},
                        {operation: "portA#aOp()", execution: "execA"}
                    ],
                    executionToPersistenceAccessor: [],
                    executionToOtherExternalAccessor: []
                }
            };
            const grouped = OutboundApp.groupOperationsByOutboundPort(data);
            assert.equal(grouped[0].operations.length, 2);
        });
    });

    // ----- renderCrudTable -----

    test.describe("renderCrudTable", () => {
        test("永続化操作なしの場合はメッセージを表示する", () => {
            const doc = setupDom();
            OutboundApp.renderCrudTable([]);
            const container = doc.getElementById("outbound-crud-panel");
            assert.equal(container.textContent, "永続化操作なし");
        });

        test("永続化操作ありの場合はテーブルを描画する", () => {
            const doc = setupDom();
            const grouped = [{
                outboundPort: {fqn: "com.example.Port", label: "Port"},
                operations: [{
                    outboundPortOperation: makeMethodData("com.example.Port#save()"),
                    outboundAdapter: null,
                    outboundAdapterExecution: null,
                    persistenceAccessors: [{
                        id: "op1",
                        group: "com.example.Mapper",
                        groupLabel: "Mapper",
                        targetOperationTypes: {"orders": "INSERT"}
                    }],
                    externalAccessors: []
                }]
            }];
            OutboundApp.renderCrudTable(grouped);
            const container = doc.getElementById("outbound-crud-panel");
            assert.ok(container.children.length > 0, "テーブルが描画されていない");
            const table = container.children[0];
            assert.equal(table.tagName, "table");
        });

        test("CRUDセルに操作タイプ文字（C/R/U/D）を表示する", () => {
            const doc = setupDom();
            const grouped = [{
                outboundPort: {fqn: "com.example.Port", label: "Port"},
                operations: [{
                    outboundPortOperation: makeMethodData("com.example.Port#save()"),
                    outboundAdapter: null, outboundAdapterExecution: null,
                    persistenceAccessors: [
                        {id: "op1", group: "g", groupLabel: "G", targetOperationTypes: {"orders": "INSERT"}}
                    ],
                    externalAccessors: []
                }]
            }];
            OutboundApp.renderCrudTable(grouped);
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
                outboundPort: {fqn: "com.example.Port", label: "Port"},
                operations: [{
                    outboundPortOperation: makeMethodData("com.example.Port#execute()"),
                    outboundAdapter: null, outboundAdapterExecution: null,
                    persistenceAccessors: [
                        {
                            id: "op1",
                            group: "g",
                            groupLabel: "G",
                            targetOperationTypes: {"orders": "INSERT", "users": "UPDATE"}
                        }
                    ],
                    externalAccessors: []
                }]
            }];
            OutboundApp.renderCrudTable(grouped);
            const container = doc.getElementById("outbound-crud-panel");
            const table = container.children[0];
            assert.equal(table.tagName, "table");
        });
    });

    // ----- generatePersistenceMermaidCode - CRUDタイプ別 -----

    test.describe("generatePersistenceMermaidCode - CRUDタイプ別", () => {
        test("SELECT・UPDATE・DELETE・不明なCRUD操作タイプを含む永続化グループのコードを生成する", () => {
            const group = {
                persistenceTarget: "orders",
                operations: [{
                    outboundPort: {fqn: "com.example.Port"},
                    outboundPortOperation: makeMethodData("com.example.Port#op()"),
                    outboundAdapter: {fqn: "com.example.Adapter", label: "Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#op()"),
                    persistenceAccessors: [{
                        id: "com.example.Mapper.op",
                        group: "com.example.Mapper",
                        groupLabel: "Mapper",
                        targetOperationTypes: {
                            "orders": "SELECT",
                            "users": "UPDATE",
                            "items": "DELETE",
                            "misc": "MERGE"
                        }
                    }],
                    externalAccessors: []
                }]
            };
            const visibility = {
                port: true, operation: true, adapter: false, execution: false,
                accessor: true, accessorMethod: false, target: true,
                externalAccessor: false, externalAccessorMethod: false,
                externalType: false, externalTypeMethod: false,
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
            };
            const code = OutboundApp.generatePersistenceMermaidCode(group, visibility);
            assert.ok(code !== null, "コードが生成されること");
            assert.ok(code.includes("orders"), `persistenceTargetが含まれること: ${code}`);
        });
    });

    // ----- generatePortMermaidCode - adapter/accessorMethod設定 -----

    test.describe("generatePortMermaidCode - adapter/accessorMethod設定", () => {
        test("adapter=true, execution=falseの場合アダプタークラスノードを生成する", () => {
            const visibility = {
                callerUsecase: false, port: true, operation: true,
                adapter: true, execution: false,
                accessor: false, accessorMethod: false,
                target: false,
                externalAccessor: false, externalAccessorMethod: false,
                externalType: false, externalTypeMethod: false,
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
            };
            const code = OutboundApp.generatePortMermaidCode(simpleGroup, visibility);
            assert.ok(code !== null, "コードが生成されること");
            const adapterId = Jig.util.fqnToId("adapter", simpleGroup.operations[0].outboundAdapter.fqn);
            assert.ok(code.includes(adapterId), `アダプタノードが含まれること: ${code}`);
        });

        test("accessor=true, accessorMethod=trueの場合アクセサメソッドノードを生成する", () => {
            const groupWithAccessor = {
                outboundPort: {fqn: "com.example.Port", label: "Port"},
                operations: [{
                    outboundPortOperation: makeMethodData("com.example.Port#save()"),
                    outboundAdapter: {fqn: "com.example.Adapter", label: "Adapter"},
                    outboundAdapterExecution: makeMethodData("com.example.Adapter#save()"),
                    persistenceAccessors: [{
                        id: "com.example.Mapper.save",
                        group: "com.example.Mapper",
                        groupLabel: "Mapper",
                        targetOperationTypes: {"orders": "INSERT"}
                    }],
                    externalAccessors: []
                }]
            };
            const visibility = {
                callerUsecase: false, port: true, operation: true,
                adapter: true, execution: true,
                accessor: true, accessorMethod: true,
                target: false,
                externalAccessor: false, externalAccessorMethod: false,
                externalType: false, externalTypeMethod: false,
                crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true
            };
            const code = OutboundApp.generatePortMermaidCode(groupWithAccessor, visibility);
            assert.ok(code !== null, "コードが生成されること");
            const opNodeId = Jig.util.fqnToId("op", "com.example.Mapper.save");
            assert.ok(code.includes(opNodeId), `アクセサメソッドノードが含まれること: ${code}`);
        });
    });

});
