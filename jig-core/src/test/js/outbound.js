const test = require("node:test");
const assert = require("node:assert/strict");
const { Element, DocumentStub } = require("./dom-stub.js");

const outbound = require("../../main/resources/templates/assets/outbound.js");

/**
 * outbound.js のテスト用に拡張された DocumentStub
 */
class OutboundDocumentStub extends DocumentStub {
    constructor() {
        super();
        this.outboundList = null;
    }

    querySelector(selector) {
        // IDセレクタ
        if (selector.startsWith('#')) return this.getElementById(selector.substring(1));

        // input[name="xxx"]:checked 形式のセレクタに対応
        const nameCheckedMatch = selector.match(/^input\[name="([^"]+)"\]:checked$/);
        if (nameCheckedMatch) {
            const name = nameCheckedMatch[1];
            return this.allElements.find(el =>
                el.tagName === "input" &&
                el.getAttribute("name") === name &&
                el.checked
            ) || null;
        }

        // input[name="show-xxx"] 形式のセレクタに対応
        const nameMatch = selector.match(/^input\[name="([^"]+)"\]$/);
        if (nameMatch) {
            const name = nameMatch[1];
            return this.allElements.find(el =>
                el.tagName === "input" &&
                el.getAttribute("name") === name
            ) || null;
        }

        return null;
    }

    querySelectorAll(selector) {
        if (selector === 'input[name^="show-"]') {
            return this.allElements.filter(el =>
                el.tagName === "input" &&
                (el.getAttribute("name") || "").startsWith("show-")
            );
        }
        if (selector === 'input[name="diagram-direction"]') {
            return this.allElements.filter(el =>
                el.tagName === "input" &&
                el.getAttribute("name") === "diagram-direction"
            );
        }
        if (selector === '.outbound-tab-list .tab-button') {
            return this.allElements.filter(el =>
                el.classList.contains("tab-button")
            );
        }
        if (selector === '.outbound-tab-panel') {
            return this.allElements.filter(el =>
                el.classList.contains("outbound-tab-panel")
            );
        }
        // 完全一致での検索（デバッグ・簡易対応用）
        return this.allElements.filter(el =>
            el.tagName === selector || el.classList.contains(selector.replace('.', ''))
        );
    }
}

/**
 * テスト用のDOM環境をセットアップする
 */
function setupDocument() {
    const doc = new OutboundDocumentStub();

    // 必要なコンテナ要素を作成
    const outboundList = doc.createElement("section");
    doc.outboundList = outboundList;
    outboundList.id = "outbound-port-list";
    doc.elementsById.set("outbound-port-list", outboundList);

    ["outbound-crud-panel", "crud-sidebar", "crud-sidebar-list", "outbound-persistence-list", "persistence-sidebar-list", "outbound-sidebar-list", "outbound-external-list", "external-sidebar-list"]
        .forEach(id => {
            const el = doc.createElement("div");
            el.id = id;
            doc.elementsById.set(id, el);
        });

    // diagram-direction ラジオボタンのモック
    const lrRadio = doc.createElement("input");
    lrRadio.setAttribute("name", "diagram-direction");
    lrRadio.setAttribute("type", "radio");
    lrRadio.setAttribute("value", "LR");
    lrRadio.checked = true;

    const tbRadio = doc.createElement("input");
    tbRadio.setAttribute("name", "diagram-direction");
    tbRadio.setAttribute("type", "radio");
    tbRadio.setAttribute("value", "TB");
    tbRadio.checked = false;

    global.document = doc;
    global.window = doc;

    // IntersectionObserver のモック
    global.IntersectionObserver = class {
        constructor(callback) { this.callback = callback; }
        observe(element) { this.callback([{ isIntersecting: true, target: element }]); }
        unobserve() {}
    };

    // mermaid のモック
    global.mermaid = {
        initialize: () => {},
        render: (id, code) => Promise.resolve({ svg: `<svg id="${id}">${code}</svg>` })
    };

    return doc;
}

test.describe("outbound.js", () => {
    test.describe("DOMユーティリティ", () => {
        test("fqnToId: FQNをHTML IDに変換する", () => {
            assert.equal(outbound.fqnToId("port", "com.example.MyPort"), "port-com-example-MyPort");
            assert.equal(outbound.fqnToId("persistence", "my_table"), "persistence-my-table");
            assert.equal(outbound.fqnToId("external", "com.example.Ext$Type"), "external-com-example-Ext-Type");
        });
    });

    test.describe("データ加工・ユーティリティ", () => {
        test("groupOperationsByOutboundPort: 出力ポート単位でグルーピングし、表示名でソートする", () => {
            const data = {
                outboundPorts: [
                    {fqn: "com.example.BPort", label: "い", operations: [{fqn: "opB", label: "delete"}]},
                    {fqn: "com.example.APort", label: "あ", operations: [{fqn: "opA", label: "save"}]},
                ],
                outboundAdapters: [
                    {fqn: "adapterA", label: "A", executions: [{fqn: "execA", label: "save"}]},
                    {fqn: "adapterB", label: "B", executions: [{fqn: "execB", label: "delete"}]},
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

            const grouped = outbound.groupOperationsByOutboundPort(data);
            assert.equal(grouped[0].outboundPort.label, "あ");
            assert.equal(grouped[1].outboundPort.label, "い");
        });

        test("groupOperationsByOutboundPort: 境界条件（データなし）", () => {
            const grouped = outbound.groupOperationsByOutboundPort({outboundPorts: [], outboundAdapters: [], persistenceAccessors: [], otherExternalAccessors: [], links: {operationToExecution: [], executionToPersistenceAccessor: [], executionToOtherExternalAccessor: []}});
            assert.equal(grouped.length, 0);
        });

        test("groupOperationsByPersistenceTarget: ターゲット単位でグルーピングし、ターゲット名でソートする", () => {
            const operations = [
                {
                    persistenceAccessors: [{ id: "op1", targetOperationTypes: {"table_b": "SELECT"} }],
                    outboundPort: { fqn: "port1", label: "P1" }
                },
                {
                    persistenceAccessors: [{ id: "op2", targetOperationTypes: {"table_a": "SELECT"} }],
                    outboundPort: { fqn: "port2", label: "P2" }
                }
            ];
            const grouped = outbound.groupOperationsByPersistenceTarget(operations);
            assert.equal(grouped.length, 2);
            assert.equal(grouped[0].persistenceTarget, "table_a");
            assert.equal(grouped[1].persistenceTarget, "table_b");
            assert.equal(grouped[0].operations[0].outboundPort.label, "P2");
        });

        test("toCrudChar: SQL型からCRUD文字への変換", () => {
            assert.equal(outbound.toCrudChar("SELECT"), "R");
            assert.equal(outbound.toCrudChar("INSERT"), "C");
            assert.equal(outbound.toCrudChar("UPDATE"), "U");
            assert.equal(outbound.toCrudChar("DELETE"), "D");
            assert.equal(outbound.toCrudChar("UNKNOWN"), "");
            assert.equal(outbound.toCrudChar(null), "");
        });

        test("formatPersistenceAccessors: 永続化操作を改行区切りで整形する", () => {
            const formatted = outbound.formatPersistenceAccessors([
                {id: "com.example.Mapper.find", targetOperationTypes: {"orders": "SELECT"}},
                {id: "com.example.Mapper.update", targetOperationTypes: {"orders": "UPDATE", "order_items": "UPDATE"}},
            ]);

            assert.deepEqual(formatted, [
                "com.example.Mapper.find [SELECT:orders]",
                "com.example.Mapper.update [UPDATE:orders, UPDATE:order_items]",
            ]);
        });

        test("getOutboundData: データをそのまま返す", () => {
            globalThis.outboundData = {
                outboundPorts: [{ fqn: "port1", label: "P1", operations: [{ fqn: "op1", label: "save" }] }],
                outboundAdapters: [{ fqn: "adapter1", label: "A1", executions: [{ fqn: "ex1", label: "exec" }] }],
                persistenceAccessors: [{ fqn: "com.example.Repo", label: "Repo", methods: [{ id: "com.example.Repo.po1", statementOperationType: "INSERT", targetOperationTypes: {"table1": "INSERT"} }] }],
                targets: ["table1"],
                links: {
                    operationToExecution: [{ operation: "op1", execution: "ex1" }],
                    executionToPersistenceAccessor: [{ execution: "ex1", accessor: "com.example.Repo.po1" }]
                }
            };

            const data = outbound.getOutboundData();
            assert.equal(data.outboundPorts[0].label, "P1");
            assert.equal(data.persistenceAccessors[0].label, "Repo");
        });

        test("getOutboundData: データがない場合のフォールバック", () => {
            delete globalThis.outboundData;
            const data = outbound.getOutboundData();
            assert.deepEqual(data.outboundPorts, []);
            assert.deepEqual(data.targets, []);
        });
    });

    test.describe("Mermaidコード生成", () => {
        test("generateOperationMermaidCode: standard相当のvisibilityで正しい接続関係を生成する", () => {
            const link = {
                outboundPort: { fqn: "P1", label: "P1" },
                outboundPortOperation: { fqn: "P1.op1", label:"op1" },
                outboundAdapter: { fqn: "com.example.A1", label: "A1" },
                outboundAdapterExecution: { fqn: "com.example.A1.ex1", label:"ex1" },
                persistenceAccessors: [
                    { id: "repo.save", targetOperationTypes: {"table1": "INSERT"} }
                ],
                externalAccessors: []
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, externalTypeMethod: true};
            const code = outbound.generateOperationMermaidCode(link, visibility);
            assert.ok(code.includes('["P1"]'));
            assert.ok(code.includes('PortOp_P1_op1["op1"]'));
            assert.ok(code.includes('["A1"]'));
            assert.ok(code.includes('Exec_com_example_A1_ex1["ex1"]'));
            assert.ok(code.includes('PortOp_P1_op1 --> Exec_com_example_A1_ex1'));
            assert.ok(code.includes('Exec_com_example_A1_ex1 -- "INSERT" --> Target_0'));
            assert.ok(code.includes('Target_0[("table1")]'));
        });

        test("generateOperationMermaidCode: adapterを非表示にするとPortOpからTargetへ直接接続される", () => {
            const link = {
                outboundPort: { fqn: "P1", label: "P1" },
                outboundPortOperation: { fqn: "P1.op1", label:"op1" },
                persistenceAccessors: [
                    { id: "repo.save", targetOperationTypes: {"table1": "INSERT"} }
                ],
                externalAccessors: []
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true, externalTypeMethod: true};
            const code = outbound.generateOperationMermaidCode(link, visibility);
            assert.ok(code.includes('PortOp_P1_op1["op1"]'));
            assert.ok(!code.includes('subgraph "Adapter"'));
            assert.ok(code.includes('PortOp_P1_op1 -- "INSERT" --> Target_0'));
        });

        test("generateOperationMermaidCode: accessorMethodを表示するvisibilityで永続化操作のグループを表示する", () => {
            const link = {
                outboundPort: { fqn: "P1", label: "P1" },
                outboundPortOperation: { fqn: "P1.op1", label:"op1" },
                outboundAdapter: { fqn: "com.example.A1", label: "A1" },
                outboundAdapterExecution: { fqn: "com.example.A1.ex1", label:"ex1" },
                persistenceAccessors: [
                    {
                        id: "com.example.repo.save",
                        group: "com.example.repo",
                        groupLabel: "repo",
                        targetOperationTypes: {"table1": "INSERT"}
                    }
                ],
                externalAccessors: []
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true, externalTypeMethod: true};
            const code = outbound.generateOperationMermaidCode(link, visibility);
            assert.ok(code.includes('["repo"]'));
            assert.ok(code.includes('POp_com_example_repo_save["save"]'));
            assert.ok(code.includes('Exec_com_example_A1_ex1 --> POp_com_example_repo_save'));
            assert.ok(code.includes('POp_com_example_repo_save -- "INSERT" --> Target_0'));
        });

        test("generateOperationMermaidCode: direction=TBのとき、graph TBで始まるコードを生成する", () => {
            const link = {
                outboundPort: { fqn: "P1", label: "P1" },
                outboundPortOperation: { fqn: "P1.op1", label: "op1" },
                persistenceAccessors: [],
                externalAccessors: []
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false, direction: 'TB'};
            const code = outbound.generateOperationMermaidCode(link, visibility);
            assert.ok(code.startsWith('graph TB\n'));
        });

        test("generatePortMermaidCode: ポート単位の図に複数の操作が含まれる", () => {
            const group = {
                outboundPort: { fqn: "PortA", label: "PortA" },
                operations: [
                    { outboundPortOperation: { fqn: "PortA.op1", label:"op1" }, persistenceAccessors: [], externalAccessors: [] },
                    { outboundPortOperation: { fqn: "PortA.op2", label:"op2" }, persistenceAccessors: [], externalAccessors: [] }
                ]
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false};
            const code = outbound.generatePortMermaidCode(group, visibility);
            assert.ok(code.includes('["PortA"]'));
            assert.ok(code.includes('PortOp_PortA_op1["op1"]'));
            assert.ok(code.includes('PortOp_PortA_op2["op2"]'));
        });

        test("generatePortMermaidCode: accessorMethodを表示するvisibilityで永続化操作のグループを表示する", () => {
            const group = {
                outboundPort: { fqn: "PortA", label: "PortA" },
                operations: [
                    {
                        outboundPortOperation: { fqn: "PortA.op1", label:"op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            {
                                id: "com.example.repo.save",
                                statementOperationType: "INSERT",
                                group: "com.example.repo",
                                groupLabel: "repo",
                                targetOperationTypes: {"table1": "INSERT"}
                            }
                        ],
                        externalAccessors: []
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true};
            const code = outbound.generatePortMermaidCode(group, visibility);
            assert.ok(code.includes('["repo"]'));
            assert.ok(code.includes('POp_com_example_repo_save["save"]'));
            assert.ok(code.includes('Exec_adapter1_ex1 --> POp_com_example_repo_save') || code.includes('Exec_exec1 --> POp_com_example_repo_save'));
        });

        test("generatePersistenceMermaidCode: visibility全表示のとき、ターゲット中心の図が生成される", () => {
            const group = {
                persistenceTarget: "table1",
                operations: [
                    {
                        outboundPort: { fqn: "P1", label: "P1" },
                        outboundPortOperation: { fqn: "P1.op1", label:"op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save",
                              group: "com.example.repo", groupLabel: "Repo", targetOperationTypes: {"table1": "INSERT"} }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true, externalTypeMethod: true};
            const code = outbound.generatePersistenceMermaidCode(group, visibility);
            assert.ok(code.includes('Target_0[("table1")]'));
            assert.ok(code.includes('POp_repo_save -- "INSERT" --> Target_0'));
            assert.ok(code.includes('Exec_adapter1_exec1 --> POp_repo_save') || code.includes('Exec_exec1 --> POp_repo_save'));
        });

        test("generatePersistenceMermaidCode: adapter非表示のとき、PortOp → POp が直接接続される", () => {
            const group = {
                persistenceTarget: "table1",
                operations: [
                    {
                        outboundPort: { fqn: "p1", label: "P1" },
                        outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save",
                              group: "com.example.repo", groupLabel: "Repo", targetOperationTypes: {"table1": "INSERT"} }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: true, accessorMethod: true, target: true};
            const code = outbound.generatePersistenceMermaidCode(group, visibility);
            assert.ok(!code.includes('Execution'));
            assert.ok(code.includes('PortOp_p1_op1'));
            assert.ok(code.includes('POp_repo_save'));
            assert.ok(code.includes('PortOp_p1_op1 --> POp_repo_save'));
        });

        test("generateOperationMermaidCode: C非表示のときINSERTエッジが生成されない", () => {
            const link = {
                outboundPort: { fqn: "P1", label: "P1" },
                outboundPortOperation: { fqn: "P1.op1", label: "op1" },
                persistenceAccessors: [
                    { id: "repo.save", targetOperationTypes: {"table1": "INSERT"} }
                ],
                externalAccessors: []
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generateOperationMermaidCode(link, visibility);
            assert.ok(!code.includes('"INSERT"'));
            assert.ok(!code.includes('Target_0'));
        });

        test("generateOperationMermaidCode: outboundPortのlabelをポートラベルとして使用する", () => {
            const operation = {
                outboundPort: { fqn: "com.example.MyPort", label: "MyPort" },
                outboundPortOperation: { fqn: "com.example.MyPort.doSomething", label: "doSomething" },
                persistenceAccessors: [],
                externalAccessors: []
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false};
            const code = outbound.generateOperationMermaidCode(operation, visibility);
            assert.ok(code.includes('["MyPort"]'), `ポートラベル "MyPort" が含まれない:\n${code}`);
        });

        test("generatePortMermaidCode: C非表示のときINSERTエッジが生成されない", () => {
            const group = {
                outboundPort: { fqn: "PortA", label: "PortA" },
                operations: [
                    {
                        outboundPortOperation: { fqn: "PortA.op1", label: "op1" },
                        persistenceAccessors: [
                            { id: "repo.save", targetOperationTypes: {"table1": "INSERT"} }
                        ],
                        externalAccessors: []
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generatePortMermaidCode(group, visibility);
            assert.ok(!code.includes('"INSERT"'));
            assert.ok(!code.includes('Target_0'));
        });

        test("generatePersistenceMermaidCode: C非表示のときINSERTエッジが生成されない", () => {
            const group = {
                persistenceTarget: "table1",
                operations: [
                    {
                        outboundPort: { fqn: "p1", label: "P1" },
                        outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save",
                              group: "com.example.repo", groupLabel: "Repo", targetOperationTypes: {"table1": "INSERT"} }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: true, crudUpdate: true, crudDelete: true};
            // INSERTのみで全CRUDがフィルタされるとbuildersが空になりnullを返す
            const code = outbound.generatePersistenceMermaidCode(group, visibility);
            assert.equal(code, null);
        });

        test("generatePortMermaidCode: メソッド非表示のとき、複数のexecutionから同一accessorへのエッジが全て生成される", () => {
            // バグ再現: adapter.a -> accessor.a, adapter.b -> accessor.b のとき
            // accessor.a と accessor.b が同じグループに属する場合、
            // adapter.b -> Accessor のエッジが欠落するケース
            const group = {
                outboundPort: { fqn: "PortA", label: "PortA" },
                operations: [
                    {
                        outboundPortOperation: { fqn: "PortA.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapterA", label: "A1" },
                        outboundAdapterExecution: { fqn: "execA", label: "execA" },
                        persistenceAccessors: [
                            {
                                id: "com.example.repo.methodA",
                                group: "com.example.repo",
                                groupLabel: "Repo",
                                targetOperationTypes: {"table1": "INSERT"}
                            }
                        ],
                        externalAccessors: []
                    },
                    {
                        outboundPortOperation: { fqn: "PortA.op2", label: "op2" },
                        outboundAdapter: { fqn: "adapterB", label: "B1" },
                        outboundAdapterExecution: { fqn: "execB", label: "execB" },
                        persistenceAccessors: [
                            {
                                id: "com.example.repo.methodB",
                                group: "com.example.repo",
                                groupLabel: "Repo",
                                targetOperationTypes: {"table1": "SELECT"}
                            }
                        ],
                        externalAccessors: []
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: false, target: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generatePortMermaidCode(group, visibility);
            // execAとexecBの両方からAccessorへのエッジが存在すること
            assert.ok(code.includes('Exec_execA --> Accessor_com_example_repo'));
            assert.ok(code.includes('Exec_execB --> Accessor_com_example_repo'));
        });

        test("generateOperationMermaidCode: メソッド非表示のとき、複数の永続化操作が同一accessorグループに属する場合にエッジが生成される", () => {
            // バグ再現: 同一リンク内で同じaccessorグループの複数メソッドがある場合
            const link = {
                outboundPort: { fqn: "P1", label: "P1" },
                outboundPortOperation: { fqn: "P1.op1", label: "op1" },
                outboundAdapter: { label: "A1", fqn: "adapterA" },
                outboundAdapterExecution: { label: "execA", fqn: "execA" },
                persistenceAccessors: [
                    {
                        id: "com.example.repo.methodA",
                        group: "com.example.repo",
                        groupLabel: "Repo",
                        targetOperationTypes: {"table1": "INSERT"}
                    },
                    {
                        id: "com.example.repo.methodB",
                        group: "com.example.repo",
                        groupLabel: "Repo",
                        targetOperationTypes: {"table2": "SELECT"}
                    }
                ],
                externalAccessors: []
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: false, target: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generateOperationMermaidCode(link, visibility);
            // ExecutionからAccessorへのエッジが存在すること
            assert.ok(code.includes('Exec_execA --> Accessor_com_example_repo'));
        });

        test("generatePortMermaidCode: 複数のexecutionが同一外部アクセッサの異なるメソッドを使う場合にCartesian productが生じない", () => {
            // バグ再現: e1→Y.m1, e2→Y.m2 のとき e1→m2, e2→m1 のエッジが生成されないことを確認
            const group = {
                outboundPort: { fqn: "PortA", label: "PortA" },
                operations: [
                    {
                        outboundPortOperation: { fqn: "PortA.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapterX", label: "X" },
                        outboundAdapterExecution: { fqn: "e1", label: "e1" },
                        persistenceAccessors: [],
                        externalAccessors: [
                            {
                                fqn: "com.example.ExternalY",
                                label: "Y",
                                methods: [{ name: "m1", externals: [] }]
                            }
                        ]
                    },
                    {
                        outboundPortOperation: { fqn: "PortA.op2", label: "op2" },
                        outboundAdapter: { fqn: "adapterX", label: "X" },
                        outboundAdapterExecution: { fqn: "e2", label: "e2" },
                        persistenceAccessors: [],
                        externalAccessors: [
                            {
                                fqn: "com.example.ExternalY",
                                label: "Y",
                                methods: [{ name: "m2", externals: [] }]
                            }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: false, externalAccessor: true, externalAccessorMethod: true, externalType: false, externalTypeMethod: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generatePortMermaidCode(group, visibility);
            // 外部アクセッサメソッドのノードIDは AccMethod_{sanitize(fqn + '_' + methodName)} の形式
            const m1NodeId = 'AccMethod_com_example_ExternalY_m1';
            const m2NodeId = 'AccMethod_com_example_ExternalY_m2';
            // e1→m1, e2→m2 のエッジが存在すること
            assert.ok(code.includes(`Exec_e1 --> ${m1NodeId}`), "e1からm1へのエッジが存在すること");
            assert.ok(code.includes(`Exec_e2 --> ${m2NodeId}`), "e2からm2へのエッジが存在すること");
            // Cartesian productが生じていないこと: e1→m2, e2→m1 のエッジが存在しない
            assert.ok(!code.includes(`Exec_e1 --> ${m2NodeId}`), "e1からm2へのエッジが存在しないこと");
            assert.ok(!code.includes(`Exec_e2 --> ${m1NodeId}`), "e2からm1へのエッジが存在しないこと");
        });

        test("generatePortMermaidCode: externalAccessor非表示のとき、Execution → ExternalType が直接接続される", () => {
            const group = {
                outboundPort: { fqn: "p1", label: "P1" },
                operations: [
                    {
                        outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [],
                        externalAccessors: [
                            {
                                fqn: "com.example.ExtAcc",
                                label: "ExtAcc",
                                methods: [
                                    {
                                        name: "call",
                                        externals: [{ fqn: "com.example.ExtType", label: "ExtType", method: "call" }]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: false, externalAccessor: false, externalAccessorMethod: false, externalType: true, externalTypeMethod: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generatePortMermaidCode(group, visibility);
            assert.ok(!code.includes('ExtAcc_'), "外部アクセッサノードが存在しないこと");
            assert.ok(code.includes('ExtType_0'), "外部型ノードが存在すること");
            assert.ok(code.includes('Exec_exec1 --> ExtType_0'), "Execution → ExternalType が直接接続されること");
        });

        test("generateExternalTypeMermaidCode: port/adapter/externalAccessor 非表示のとき、ExternalType ノードのみが表示され null ノードが生じない", () => {
            const group = {
                externalType: { fqn: "com.example.ExtType", label: "ExtType" },
                operations: [
                    {
                        outboundPort: { fqn: "p1", label: "P1" },
                        outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        externalAccessors: [
                            {
                                fqn: "com.example.ExtAcc",
                                label: "ExtAcc",
                                methods: [
                                    {
                                        name: "call",
                                        externals: [{ fqn: "com.example.ExtType", label: "ExtType", method: "call" }]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            };
            const visibility = {port: false, operation: false, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true, externalAccessor: false, externalAccessorMethod: false, externalType: true, externalTypeMethod: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generateExternalTypeMermaidCode(group, visibility);
            assert.ok(code !== null, "コードが生成されること");
            assert.ok(!code.includes('null'), "nullノードが存在しないこと");
            assert.ok(code.includes('ExtType_0'), "外部型ノードが存在すること");
        });

        test("generateExternalTypeMermaidCode: externalAccessor非表示のとき、Execution → ExternalType が直接接続される", () => {
            const group = {
                externalType: { fqn: "com.example.ExtType", label: "ExtType" },
                operations: [
                    {
                        outboundPort: { fqn: "p1", label: "P1" },
                        outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        externalAccessors: [
                            {
                                fqn: "com.example.ExtAcc",
                                label: "ExtAcc",
                                methods: [
                                    {
                                        name: "call",
                                        externals: [{ fqn: "com.example.ExtType", label: "ExtType", method: "call" }]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: false, externalAccessor: false, externalAccessorMethod: false, externalType: true, externalTypeMethod: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generateExternalTypeMermaidCode(group, visibility);
            assert.ok(!code.includes('ExtAcc_'), "外部アクセッサノードが存在しないこと");
            assert.ok(code.includes('ExtType_0'), "外部型ノードが存在すること");
            assert.ok(code.includes('Exec_exec1 --> ExtType_0'), "Execution → ExternalType が直接接続されること");
        });

        test("generateExternalTypeMermaidCode: 同じアクセッサが複数の外部型を持つとき、カード対象の外部型のみが表示される", () => {
            // accessorX -> externalA, accessorX -> externalB のとき、externalAカードにexternalBが表示されない
            const makeGroup = (targetFqn, targetLabel) => ({
                externalType: { fqn: targetFqn, label: targetLabel },
                operations: [
                    {
                        outboundPort: { fqn: "p1", label: "P1" },
                        outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        externalAccessors: [
                            {
                                fqn: "com.example.AccX",
                                label: "AccX",
                                methods: [
                                    {
                                        name: "callA",
                                        externals: [{ fqn: "com.example.ExtA", label: "ExtA", method: "methodA" }]
                                    },
                                    {
                                        name: "callB",
                                        externals: [{ fqn: "com.example.ExtB", label: "ExtB", method: "methodB" }]
                                    }
                                ]
                            }
                        ]
                    }
                ]
            });
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: false, externalAccessor: true, externalAccessorMethod: false, externalType: true, externalTypeMethod: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};

            const codeA = outbound.generateExternalTypeMermaidCode(makeGroup("com.example.ExtA", "ExtA"), visibility);
            assert.ok(codeA.includes('(("ExtA"))'), "externalAカードにExtAノードが表示されること");
            assert.ok(!codeA.includes('(("ExtB"))'), "externalAカードにExtBノードが表示されないこと");

            const codeB = outbound.generateExternalTypeMermaidCode(makeGroup("com.example.ExtB", "ExtB"), visibility);
            assert.ok(codeB.includes('(("ExtB"))'), "externalBカードにExtBノードが表示されること");
            assert.ok(!codeB.includes('(("ExtA"))'), "externalBカードにExtAノードが表示されないこと");
        });

        test("generatePersistenceMermaidCode: table2のみアクセスするアクセッサはtable1のカードに表示されない", () => {
            // バグ再現: isCrudVisible(undefined, ...) が true を返すことで、
            // 無関係なアクセッサが永続化カードに混入しラベルなしエッジが生じていた
            const group = {
                persistenceTarget: "table1",
                operations: [
                    {
                        outboundPort: { fqn: "p1", label: "P1" },
                        outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            // table1 へのアクセッサ
                            { id: "repo.save", group: "com.example.repo", groupLabel: "Repo", targetOperationTypes: {"table1": "INSERT"} },
                            // table2 のみへのアクセッサ（table1 とは無関係）
                            { id: "repo.find", group: "com.example.repo", groupLabel: "Repo", targetOperationTypes: {"table2": "SELECT"} }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true, externalTypeMethod: true, crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outbound.generatePersistenceMermaidCode(group, visibility);
            // table1 への INSERT エッジが存在すること
            assert.ok(code.includes('"INSERT"'), `INSERTエッジが存在すること:\n${code}`);
            // table2 のみのアクセッサによるラベルなしエッジが存在しないこと
            assert.ok(!code.includes('-- "" -->'), `ラベルなしエッジが存在しないこと:\n${code}`);
            assert.ok(!code.includes('-->  '), `不正なエッジが存在しないこと:\n${code}`);
            // repo.find (table2専用) のノードは存在しないこと
            assert.ok(!code.includes('POp_repo_find'), `table2専用アクセッサノードが存在しないこと:\n${code}`);
        });

        test("generatePersistenceMermaidCode: accessor非表示のとき、Execution → Target が直接接続される", () => {
            const group = {
                persistenceTarget: "table1",
                operations: [
                    {
                        outboundPort: { fqn: "p1", label: "P1" },
                        outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                        outboundAdapter: { fqn: "adapter1", label: "A1" },
                        outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save",
                              group: "com.example.repo", groupLabel: "Repo", targetOperationTypes: {"table1": "INSERT"} }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, externalTypeMethod: true};
            const code = outbound.generatePersistenceMermaidCode(group, visibility);
            assert.ok(!code.includes('POp_'));
            assert.ok(code.includes('Exec_exec1'));
            assert.ok(code.includes('Exec_exec1 -- "INSERT" --> Target_0'));
        });
    });

    test.describe("表示レンダリング (DOM操作)", () => {
        test.beforeEach(() => { setupDocument(); });

        test("renderOutboundList: 出力ポートごとのカードを描画する", () => {
            const doc = setupDocument();

            outbound.renderOutboundList([
                {
                    outboundPort: {fqn: "com.example.APort", label: "A Port"},
                    operations: [
                        {
                            outboundPortOperation: {fqn: "com.example.APort.save", label: "save(java.lang.String)", signature: "save(java.lang.String)"},
                            outboundAdapter: {fqn: "com.example.AAdapter", label: "A Adapter"},
                            outboundAdapterExecution: {fqn: "com.example.AAdapter.save", label: "save(java.lang.String)", signature: "save(java.lang.String)"},
                            persistenceAccessors: [{id: "a.save", targetOperationTypes: {"orders": "SELECT"}}],
                            externalAccessors: [],
                        },
                        {
                            outboundPortOperation: {fqn: "com.example.APort.find", label: "find(java.lang.String)", signature: "find(java.lang.String)"},
                            outboundAdapter: {fqn: "com.example.AAdapter", label: "A Adapter"},
                            outboundAdapterExecution: {fqn: "com.example.AAdapter.find", label: "find(java.lang.String)", signature: "find(java.lang.String)"},
                            persistenceAccessors: [],
                            externalAccessors: [],
                        },
                    ],
                },
            ]);

            const outboundList = doc.outboundList;
            assert.equal(outboundList.children.length, 1);
            const portCard = outboundList.children[0];
            assert.equal(portCard.children[0].textContent, "A Port");
            assert.equal(portCard.children[3].textContent, "2 operations");

            const itemList = portCard.children[5].children[1];
            assert.equal(itemList.children.length, 2);
            assert.equal(itemList.children[0].children[0].textContent, "save(java.lang.String)");
            assert.equal(itemList.children[0].children[3].children[0].textContent, "a.save [SELECT:orders]");
            assert.equal(itemList.children[1].children[3].children[0].textContent, "なし");
        });

        test("renderCrudTable: CRUDテーブルが正しく描画され、トグル動作が機能する", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outbound-crud-panel");
            const grouped = [
                {
                    outboundPort: { fqn: "Port_A", label: "Port A" },
                    operations: [{
                        outboundPortOperation: { fqn: "Port_A.opA", label:"opA" },
                        persistenceAccessors: [{ targetOperationTypes: {"table1": "SELECT"} }]
                    }]
                }
            ];

            outbound.renderCrudTable(grouped);

            const table = container.children[0];
            const tbody = table.children[1];
            const portRow = tbody.children[0];
            const opRow = tbody.children[1];

            assert.ok(portRow.children[0].textContent.startsWith("Port A"));
            assert.equal(portRow.children[1].textContent, "R");
            assert.equal(opRow.style.display, "none");

            portRow.click();
            assert.equal(opRow.style.display, "table-row");

            portRow.click();
            assert.equal(opRow.style.display, "none");
        });

        test("renderCrudTable: 全CRUD表示（デフォルト）のとき、CとRが両方表示される", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outbound-crud-panel");
            const grouped = [{
                outboundPort: { fqn: "Port_A", label: "Port A" },
                operations: [{
                    outboundPortOperation: { fqn: "Port_A.opA", label: "opA" },
                    persistenceAccessors: [
                        { targetOperationTypes: {"table1": "INSERT"} },
                        { targetOperationTypes: {"table1": "SELECT"} }
                    ]
                }]
            }];
            outbound.renderCrudTable(grouped);

            const tbody = container.children[0].children[1];
            const portRow = tbody.children[0];
            assert.equal(portRow.children[1].textContent, "CR");
        });

        test("renderCrudTable: 永続化操作がない場合の表示", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outbound-crud-panel");

            outbound.renderCrudTable([{outboundPort: {fqn: "P", label: "P"}, operations: []}]);
            assert.equal(container.textContent, "永続化操作なし");
        });

        test("renderPersistenceList: 永続化ターゲットごとのカードが描画される", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outbound-persistence-list");
            const sidebar = doc.getElementById("persistence-sidebar-list");

            const grouped = [
                {
                    persistenceTarget: "table1",
                    operations: [
                        {
                            outboundPort: { fqn: "p1", label: "P1" },
                            outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                            outboundAdapter: { fqn: "adapter1", label: "A1" },
                            outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                            persistenceAccessors: [
                                { id: "repo.save", group: "com.example.repo", groupLabel: "Repo", targetOperationTypes: {"table1": "INSERT"} }
                            ]
                        }
                    ]
                }
            ];

            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true};
            outbound.renderPersistenceList(grouped, visibility);

            assert.equal(container.children.length, 1);
            const card = container.children[0];
            assert.equal(card.tagName, "section");
            assert.equal(card.getAttribute("id"), "persistence-table1");
            assert.equal(card.children[0].textContent, "table1");

            // サイドバーの確認
            assert.notEqual(sidebar.children.length, 0);
            const sidebarSection = sidebar.children[0];
            assert.equal(sidebarSection.children[0].textContent, "永続化操作対象");
            const sidebarList = sidebarSection.children[1];
            const sidebarItem = sidebarList.children[0];
            const sidebarLink = sidebarItem.children[0];
            assert.equal(sidebarLink.getAttribute("href"), "#persistence-table1");
            assert.equal(sidebarLink.textContent, "table1");
        });

        test("renderOutboundList: adapter非表示の場合はAdapter情報が表示されない", () => {
            const doc = setupDocument();
            const grouped = [
                {
                    outboundPort: { fqn: "port1", label: "port1" },
                    operations: [{ outboundPortOperation: { fqn: "port1.op1", label: "op1" }, outboundAdapter: { label: "adapter1" }, persistenceAccessors: [], externalAccessors: [] }]
                }
            ];

            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true};
            outbound.renderOutboundList(grouped, visibility);

            const portCard = doc.outboundList.children[0];
            const sidebarSection = doc.getElementById("outbound-sidebar-list").children[0];
            // adapter非表示ではadapterInfo (p) が追加されないため、children[2] は count (p) になる
            assert.equal(portCard.children[2].textContent, "1 operations");
            assert.ok(!portCard.children.some(child => child.textContent.includes("Implementation:")));
            assert.equal(sidebarSection.children[0].textContent, "出力ポート");
            assert.equal(sidebarSection.children[1].children[0].children[0].textContent, "port1");
        });

        test("renderOutboundList / renderPersistenceList: データが空の場合の表示", () => {
            const doc = global.document;

            outbound.renderOutboundList([]);
            const container = doc.getElementById("outbound-port-list");
            assert.equal(container.children[0].textContent, "データなし");

            outbound.renderPersistenceList([], {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true});
            const pContainer = doc.getElementById("outbound-persistence-list");
            assert.equal(pContainer.children[0].textContent, "データなし");
        });

        test("DOMContentLoaded 後の初期化とタブ切り替え動作", () => {
            delete require.cache[require.resolve("../../main/resources/templates/assets/outbound.js")];
            const doc = setupDocument();
            const reloadedOutbound = require("../../main/resources/templates/assets/outbound.js");
            const app = reloadedOutbound.OutboundApp;

            const checkboxNames = ["show-port", "show-operation", "show-adapter", "show-execution", "show-accessor", "show-accessor-method", "show-target"];
            const checkboxes = checkboxNames.map(name => {
                const cb = doc.createElement("input");
                cb.setAttribute("name", name);
                cb.setAttribute("type", "checkbox");
                cb.checked = ["show-port", "show-operation", "show-adapter", "show-execution", "show-target"].includes(name);
                return cb;
            });

            const tabButton = doc.createElement("button");
            tabButton.classList.add("tab-button");
            tabButton.setAttribute("data-tab", "crud");

            const tabPanel = doc.createElement("div");
            tabPanel.id = "crud-tab-panel";
            tabPanel.classList.add("outbound-tab-panel");

            const originalQuerySelectorAll = doc.querySelectorAll;
            doc.querySelectorAll = (selector) => {
                if (selector === '.outbound-tab-list .tab-button') return [tabButton];
                if (selector === '.outbound-tab-panel') return [tabPanel];
                if (selector === 'input[name^="show-"]') return checkboxes;
                return originalQuerySelectorAll.call(doc, selector);
            };

            const originalQuerySelector = doc.querySelector;
            doc.querySelector = (selector) => {
                const nameCheckedMatch = selector.match(/^input\[name="([^"]+)"\]:checked$/);
                if (nameCheckedMatch) {
                    const name = nameCheckedMatch[1];
                    return checkboxes.find(cb =>
                        cb.getAttribute("name") === name && cb.checked
                    ) || null;
                }
                const nameMatch = selector.match(/^input\[name="([^"]+)"\]$/);
                if (nameMatch) {
                    return checkboxes.find(cb => cb.getAttribute("name") === nameMatch[1]) || null;
                }
                return originalQuerySelector.call(doc, selector);
            };

            const listeners = doc.eventListeners.get("DOMContentLoaded") || [];
            listeners.forEach(l => l());

            assert.ok(app.state.data);
            assert.equal(app.state.visibility.port, true);
            assert.equal(app.state.visibility.accessor, false);

            tabButton.click();
            assert.equal(app.state.activeTab, "crud");
            assert.ok(tabButton.classList.contains("is-active"));
            assert.ok(tabPanel.classList.contains("is-active"));

            const accessorMethodCheckbox = checkboxes.find(cb => cb.getAttribute("name") === "show-accessor-method");
            assert.ok(accessorMethodCheckbox.disabled, "show-accessor が未チェックなので show-accessor-method は disabled");

            const accessorCheckbox = checkboxes.find(cb => cb.getAttribute("name") === "show-accessor");
            accessorCheckbox.checked = true;
            accessorCheckbox.eventListeners.get("change")?.forEach(l => l());
            assert.equal(app.state.visibility.accessor, true);
            assert.ok(!accessorMethodCheckbox.disabled, "show-accessor がチェック後は show-accessor-method が disabled でない");
        });

        test("lazyRender: IntersectionObserver がない場合のフォールバック", () => {
            const oldIO = global.IntersectionObserver;
            delete global.IntersectionObserver;
            try {
                outbound.renderOutboundList([
                    {
                        outboundPort: { fqn: "p1" },
                        operations: [{ outboundPortOperation: { label:"op1" }, persistenceAccessors: [], externalAccessors: [] }]
                    }
                ]);
            } finally {
                global.IntersectionObserver = oldIO;
            }
        });

        test("renderOutboundList: port/adapter/accessor/target が全て false のとき、カードが描画されない", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outbound-port-list");

            const grouped = [
                {
                    outboundPort: { fqn: "com.example.APort", label: "A Port" },
                    operations: [
                        {
                            outboundPortOperation: { label: "op1" },
                            persistenceAccessors: [],
                            externalAccessors: []
                        }
                    ]
                }
            ];
            const visibility = {port: false, operation: false, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            outbound.renderOutboundList(grouped, visibility);

            assert.equal(container.children.length, 0);
        });

        test("renderPersistenceList: 全CRUD非表示でINSERTのみの操作があるとき、カードが描画されない", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outbound-persistence-list");

            const grouped = [
                {
                    persistenceTarget: "table1",
                    operations: [
                        {
                            outboundPort: { fqn: "p1", label: "P1" },
                            outboundPortOperation: { fqn: "p1.op1", label: "op1" },
                            outboundAdapter: { fqn: "adapter1", label: "A1" },
                            outboundAdapterExecution: { fqn: "exec1", label: "ex1" },
                            persistenceAccessors: [
                                { id: "repo.insert", group: "com.example.repo", groupLabel: "Repo", targetOperationTypes: {"table1": "INSERT"} }
                            ]
                        }
                    ]
                }
            ];
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: false, crudUpdate: false, crudDelete: false};
            outbound.renderPersistenceList(grouped, visibility);

            assert.equal(container.children.length, 0);
        });
    });

    test.describe("MermaidBuilder", () => {
        test("isEmpty: 初期状態は空", () => {
            const builder = new outbound.MermaidBuilder();
            assert.equal(builder.isEmpty(), true);
        });

        test("isEmpty: ノード追加後は空ではない", () => {
            const builder = new outbound.MermaidBuilder();
            builder.addNode("n1", "Node1");
            assert.equal(builder.isEmpty(), false);
        });

        test("subgraph ID の一意性: 同じラベルでも異なるIDを持つ", () => {
            const builder = new outbound.MermaidBuilder();
            const sg1 = builder.startSubgraph("MyService");
            const sg2 = builder.startSubgraph("MyService");
            assert.notEqual(sg1.id, sg2.id);
        });

        test("同じラベルの subgraph が2つある場合、build() に subgraph が2つ含まれる", () => {
            const builder = new outbound.MermaidBuilder();
            const sg1 = builder.startSubgraph("MyService");
            builder.addNodeToSubgraph(sg1, "n1", "Node1");
            const sg2 = builder.startSubgraph("MyService");
            builder.addNodeToSubgraph(sg2, "n2", "Node2");
            const code = builder.build();
            const matches = code.match(/subgraph /g);
            assert.equal(matches?.length, 2);
        });

        test("build() の subgraph 行が id [\"label\"] 形式になっている", () => {
            const builder = new outbound.MermaidBuilder();
            const sg = builder.startSubgraph("My Service");
            builder.addNodeToSubgraph(sg, "n1", "Node1");
            const code = builder.build();
            assert.match(code, /subgraph sg_My_Service_0 \["My Service"\]/);
        });
    });
});
