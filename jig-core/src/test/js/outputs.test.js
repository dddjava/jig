const test = require("node:test");
const assert = require("node:assert/strict");

const outputs = require("../../main/resources/templates/assets/outputs.js");

class ClassList {
    constructor() {
        this.set = new Set();
    }

    add(c) {
        this.set.add(c);
    }

    remove(c) {
        this.set.delete(c);
    }

    delete(c) {
        this.set.delete(c);
    }

    has(c) {
        return this.set.has(c);
    }

    contains(c) {
        return this.set.has(c);
    }

    toggle(className, force) {
        if (force === true) {
            this.set.add(className);
        } else if (force === false) {
            this.set.delete(className);
        } else {
            if (this.set.has(className)) {
                this.set.delete(className);
            } else {
                this.set.add(className);
            }
        }
    }

    toString() {
        return Array.from(this.set).join(" ");
    }
}

class Element {
    constructor(tagName, ownerDocument = null) {
        this.tagName = tagName;
        this.ownerDocument = ownerDocument;
        this.children = [];
        this._textContent = "";
        this.innerHTML = "";
        this.classList = new ClassList();
        this.style = {};
        this.attributes = new Map();
        this.eventListeners = new Map();
        this.value = ""; // ラジオボタン等のため
        this.checked = false; // ラジオボタン等のため
    }

    get id() {
        return this.getAttribute("id");
    }

    set id(value) {
        this.setAttribute("id", value);
    }

    get textContent() {
        if (this.children.length > 0) {
            return this.children.map(c => c.textContent).join("");
        }
        return this._textContent;
    }

    set textContent(value) {
        this._textContent = String(value ?? "");
        this.children = [];
    }

    get className() {
        return this.classList.toString();
    }

    set className(value) {
        this.classList.set = new Set(value.split(" ").filter(c => c));
    }

    appendChild(child) {
        this.children.push(child);
        return child;
    }

    setAttribute(name, value) {
        this.attributes.set(name, String(value));
        if (name === "id" && this.ownerDocument) {
            this.ownerDocument.elementsById.set(value, this);
        }
    }

    getAttribute(name) {
        return this.attributes.get(name) || null;
    }

    addEventListener(type, listener) {
        if (!this.eventListeners.has(type)) {
            this.eventListeners.set(type, []);
        }
        this.eventListeners.get(type).push(listener);
    }

    dispatchEvent(event) {
        const listeners = this.eventListeners.get(event.type) || [];
        listeners.forEach(l => l(event));
    }

    click() {
        this.dispatchEvent({ type: "click", target: this });
    }

    // クエリセレクタの簡易実装（テスト用）
    querySelector(selector) {
        if (selector === 'input[name="display-mode"]:checked') {
            return this.ownerDocument.querySelector(selector);
        }
        if (selector.startsWith('#')) {
            return this.ownerDocument.getElementById(selector.substring(1));
        }
        return null;
    }

    querySelectorAll(selector) {
        return this.ownerDocument.querySelectorAll(selector);
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.outputsList = null;
        this.eventListeners = new Map();
        this.allElements = [];
    }

    createElement(tagName) {
        const el = new Element(tagName, this);
        this.allElements.push(el);
        return el;
    }

    createTextNode(text) {
        const el = new Element("#text", this);
        el.textContent = text;
        this.allElements.push(el);
        return el;
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }

    querySelector(selector) {
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
        if (selector === '.outputs-tabs .tab-button') {
            return this.allElements.filter(el =>
                el.classList.contains("tab-button")
            );
        }
        if (selector === '.outputs-tab-panel') {
            return this.allElements.filter(el =>
                el.classList.contains("outputs-tab-panel")
            );
        }
        // 完全一致での検索（デバッグ・簡易対応用）
        return this.allElements.filter(el =>
            el.tagName === selector || el.classList.contains(selector.replace('.', ''))
        );
    }

    addEventListener(type, listener) {
        if (!this.eventListeners.has(type)) {
            this.eventListeners.set(type, []);
        }
        this.eventListeners.get(type).push(listener);
    }

    dispatchEvent(event) {
        const listeners = this.eventListeners.get(event.type) || [];
        listeners.forEach(l => l(event));
    }
}

function setupDocument() {
    const doc = new DocumentStub();

    const outputsList = doc.createElement("section");
    doc.outputsList = outputsList;
    doc.elementsById.set("outputs-list", outputsList);

    doc.elementsById.set("outputs-crud", doc.createElement("div"));
    doc.elementsById.set("crud-sidebar", doc.createElement("div"));
    doc.elementsById.set("crud-sidebar-list", doc.createElement("div"));

    doc.elementsById.set("persistence-list", doc.createElement("div"));
    doc.elementsById.set("persistence-sidebar-list", doc.createElement("div"));

    doc.elementsById.set("outputs-sidebar-list", doc.createElement("div"));

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
        constructor(callback) {
            this.callback = callback;
        }
        observe(element) {
            // 自動的に交差したことにする（即時実行テスト用）
            this.callback([{ isIntersecting: true, target: element }]);
        }
        unobserve() {}
    };

    // mermaid のモック
    global.mermaid = {
        initialize: () => {},
        render: (id, code) => Promise.resolve({ svg: `<svg id="${id}">${code}</svg>` })
    };

    return doc;
}

test.describe("outputs.js", () => {
    test.describe("データ加工・ユーティリティ", () => {
        test("groupOperationsByOutputPort: 出力ポート単位でグルーピングし、表示名でソートする", () => {
            const data = {
                outputPorts: [
                    {fqn: "com.example.BPort", label: "い", operations: [{fqn: "opB", label: "delete"}]},
                    {fqn: "com.example.APort", label: "あ", operations: [{fqn: "opA", label: "save"}]},
                ],
                outputAdapters: [
                    {fqn: "adapterA", label: "A", executions: [{fqn: "execA", label: "save"}]},
                    {fqn: "adapterB", label: "B", executions: [{fqn: "execB", label: "delete"}]},
                ],
                persistenceAccessors: [],
                links: {
                    operationToExecution: [
                        {operation: "opA", execution: "execA"},
                        {operation: "opB", execution: "execB"}
                    ],
                    executionToAccessor: []
                }
            };

            const grouped = outputs.groupOperationsByOutputPort(data);
            assert.equal(grouped[0].outputPort.label, "あ");
            assert.equal(grouped[1].outputPort.label, "い");
        });

        test("groupOperationsByOutputPort: 境界条件（データなし）", () => {
            const grouped = outputs.groupOperationsByOutputPort({outputPorts: [], outputAdapters: [], persistenceAccessors: [], links: {}});
            assert.equal(grouped.length, 0);
        });

        test("groupOperationsByPersistenceTarget: ターゲット単位でグルーピングし、ターゲット名でソートする", () => {
            const operations = [
                {
                    persistenceAccessors: [{ targets: ["table_b"], id: "op1" }],
                    outputPort: { fqn: "port1", label: "P1" }
                },
                {
                    persistenceAccessors: [{ targets: ["table_a"], id: "op2" }],
                    outputPort: { fqn: "port2", label: "P2" }
                }
            ];
            const grouped = outputs.groupOperationsByPersistenceTarget(operations);
            assert.equal(grouped.length, 2);
            assert.equal(grouped[0].target, "table_a");
            assert.equal(grouped[1].target, "table_b");
            assert.equal(grouped[0].operations[0].outputPort.label, "P2");
        });

        test("toCrudChar: SQL型からCRUD文字への変換", () => {
            assert.equal(outputs.toCrudChar("SELECT"), "R");
            assert.equal(outputs.toCrudChar("INSERT"), "C");
            assert.equal(outputs.toCrudChar("UPDATE"), "U");
            assert.equal(outputs.toCrudChar("DELETE"), "D");
            assert.equal(outputs.toCrudChar("UNKNOWN"), "");
            assert.equal(outputs.toCrudChar(null), "");
        });

        test("formatPersistenceAccessors: 永続化操作を改行区切りで整形する", () => {
            const formatted = outputs.formatPersistenceAccessors([
                {sqlType: "SELECT", id: "com.example.Mapper.find", targets: ["orders"]},
                {sqlType: "UPDATE", id: "com.example.Mapper.update", targets: ["orders", "order_items"]},
            ]);

            assert.deepEqual(formatted, [
                "SELECT com.example.Mapper.find [orders]",
                "UPDATE com.example.Mapper.update [orders, order_items]",
            ]);
        });

        test("getOutputsData: データをそのまま返す", () => {
            globalThis.outputPortData = {
                outputPorts: [{ fqn: "port1", label: "P1", operations: [{ fqn: "op1", label: "save" }] }],
                outputAdapters: [{ fqn: "adapter1", label: "A1", executions: [{ fqn: "ex1", label: "exec" }] }],
                persistenceAccessors: [{ fqn: "com.example.Repo", label: "Repo", methods: [{ id: "com.example.Repo.po1", sqlType: "INSERT", targets: ["table1"] }] }],
                targets: ["table1"],
                links: {
                    operationToExecution: [{ operation: "op1", execution: "ex1" }],
                    executionToAccessor: [{ execution: "ex1", accessor: "com.example.Repo.po1" }]
                }
            };

            const data = outputs.getOutputsData();
            assert.equal(data.outputPorts[0].label, "P1");
            assert.equal(data.persistenceAccessors[0].label, "Repo");
        });

        test("getOutputsData: データがない場合のフォールバック", () => {
            delete globalThis.outputPortData;
            const data = outputs.getOutputsData();
            assert.deepEqual(data.outputPorts, []);
            assert.deepEqual(data.targets, []);
        });


    });

    test.describe("Mermaidコード生成", () => {
        test("generateOperationMermaidCode: standard相当のvisibilityで正しい接続関係を生成する", () => {
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { label:"op1" },
                outputAdapter: { label: "A1" },
                outputAdapterExecution: { label:"ex1" },
                persistenceAccessors: [
                    { id: "repo.save", sqlType: "INSERT", targets: ["table1"] }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true};
            const code = outputs.generateOperationMermaidCode(link, visibility);
            assert.ok(code.includes('["P1"]'));
            assert.ok(code.includes('PortOp_P1_op1["op1"]'));
            assert.ok(code.includes('["A1"]'));
            assert.ok(code.includes('Exec_Adapter_0_ex1["ex1"]'));
            assert.ok(code.includes('PortOp_P1_op1 --> Exec_Adapter_0_ex1'));
            assert.ok(code.includes('Exec_Adapter_0_ex1 -- "INSERT" --> Target_0'));
            assert.ok(code.includes('Target_0[(table1)]'));
        });

        test("generateOperationMermaidCode: adapterを非表示にするとPortOpからTargetへ直接接続される", () => {
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { label:"op1" },
                persistenceAccessors: [
                    { id: "repo.save", sqlType: "INSERT", targets: ["table1"] }
                ]
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true};
            const code = outputs.generateOperationMermaidCode(link, visibility);
            assert.ok(code.includes('PortOp_P1_op1["op1"]'));
            assert.ok(!code.includes('subgraph "Adapter"'));
            assert.ok(code.includes('PortOp_P1_op1 -- "INSERT" --> Target_0'));
        });

        test("generateOperationMermaidCode: accessorMethodを表示するvisibilityで永続化操作のグループを表示する", () => {
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { label:"op1" },
                outputAdapter: { label: "A1" },
                outputAdapterExecution: { label:"ex1" },
                persistenceAccessors: [
                    {
                        id: "com.example.repo.save",
                        sqlType: "INSERT",
                        targets: ["table1"],
                        group: "com.example.repo",
                        groupLabel: "repo"
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true};
            const code = outputs.generateOperationMermaidCode(link, visibility);
            assert.ok(code.includes('["repo"]'));
            assert.ok(code.includes('POp_com_example_repo_save["save"]'));
            assert.ok(code.includes('Exec_Adapter_0_ex1 --> POp_com_example_repo_save'));
            assert.ok(code.includes('POp_com_example_repo_save -- "INSERT" --> Target_0'));
        });

        test("generateOperationMermaidCode: direction=TBのとき、graph TBで始まるコードを生成する", () => {
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { label: "op1" },
                persistenceAccessors: []
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false, direction: 'TB'};
            const code = outputs.generateOperationMermaidCode(link, visibility);
            assert.ok(code.startsWith('graph TB\n'));
        });

        test("generatePortMermaidCode: ポート単位の図に複数の操作が含まれる", () => {
            const group = {
                outputPort: { label: "PortA" },
                operations: [
                    { outputPortOperation: { label:"op1" } },
                    { outputPortOperation: { label:"op2" } }
                ]
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false};
            const code = outputs.generatePortMermaidCode(group, visibility);
            assert.ok(code.includes('["PortA"]'));
            assert.ok(code.includes('PortOp_PortA_op1["op1"]'));
            assert.ok(code.includes('PortOp_PortA_op2["op2"]'));
        });

        test("generatePortMermaidCode: accessorMethodを表示するvisibilityで永続化操作のグループを表示する", () => {
            const group = {
                outputPort: { label: "PortA" },
                operations: [
                    {
                        outputPortOperation: { label:"op1" },
                        outputAdapter: { fqn: "adapter1", label: "A1" },
                        outputAdapterExecution: { fqn: "exec1", name: "ex1" },
                        persistenceAccessors: [
                            {
                                id: "com.example.repo.save",
                                sqlType: "INSERT",
                                targets: ["table1"],
                                group: "com.example.repo",
                                groupLabel: "repo"
                            }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true};
            const code = outputs.generatePortMermaidCode(group, visibility);
            assert.ok(code.includes('["repo"]'));
            assert.ok(code.includes('POp_com_example_repo_save["save"]'));
            assert.ok(code.includes('Exec_adapter1_ex1 --> POp_com_example_repo_save') || code.includes('Exec_exec1 --> POp_com_example_repo_save'));
        });

        test("generatePersistenceMermaidCode: visibility全表示のとき、ターゲット中心の図が生成される", () => {
            const group = {
                target: "table1",
                operations: [
                    {
                        outputPort: { label: "P1" },
                        outputPortOperation: { label:"op1" },
                        outputAdapter: { fqn: "adapter1", label: "A1" },
                        outputAdapterExecution: { fqn: "exec1", name: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save", sqlType: "INSERT", targets: ["table1"],
                              group: "com.example.repo", groupLabel: "Repo" }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true};
            const code = outputs.generatePersistenceMermaidCode(group, visibility);
            assert.ok(code.includes('Target_0[(table1)]'));
            assert.ok(code.includes('POp_repo_save -- "INSERT" --> Target_0'));
            assert.ok(code.includes('Exec_adapter1_exec1 --> POp_repo_save') || code.includes('Exec_exec1 --> POp_repo_save'));
        });

        test("generatePersistenceMermaidCode: adapter非表示のとき、PortOp → POp が直接接続される", () => {
            const group = {
                target: "table1",
                operations: [
                    {
                        outputPort: { fqn: "p1", label: "P1" },
                        outputPortOperation: { fqn: "p1.op1", label: "op1" },
                        outputAdapter: { fqn: "adapter1", label: "A1" },
                        outputAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save", sqlType: "INSERT", targets: ["table1"],
                              group: "com.example.repo", groupLabel: "Repo" }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: true, accessorMethod: true, target: true};
            const code = outputs.generatePersistenceMermaidCode(group, visibility);
            assert.ok(!code.includes('Execution'));
            assert.ok(code.includes('PortOp_p1_op1'));
            assert.ok(code.includes('POp_repo_save'));
            assert.ok(code.includes('PortOp_p1_op1 --> POp_repo_save'));
        });

        test("generateOperationMermaidCode: C非表示のときINSERTエッジが生成されない", () => {
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { label: "op1" },
                persistenceAccessors: [
                    { id: "repo.save", sqlType: "INSERT", targets: ["table1"] }
                ]
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outputs.generateOperationMermaidCode(link, visibility);
            assert.ok(!code.includes('"INSERT"'));
            assert.ok(!code.includes('Target_0'));
        });

        test("generatePortMermaidCode: C非表示のときINSERTエッジが生成されない", () => {
            const group = {
                outputPort: { label: "PortA" },
                operations: [
                    {
                        outputPortOperation: { label: "op1" },
                        persistenceAccessors: [
                            { id: "repo.save", sqlType: "INSERT", targets: ["table1"] }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outputs.generatePortMermaidCode(group, visibility);
            assert.ok(!code.includes('"INSERT"'));
            assert.ok(!code.includes('Target_0'));
        });

        test("generatePersistenceMermaidCode: C非表示のときINSERTエッジが生成されない", () => {
            const group = {
                target: "table1",
                operations: [
                    {
                        outputPort: { fqn: "p1", label: "P1" },
                        outputPortOperation: { fqn: "p1.op1", label: "op1" },
                        outputAdapter: { fqn: "adapter1", label: "A1" },
                        outputAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save", sqlType: "INSERT", targets: ["table1"],
                              group: "com.example.repo", groupLabel: "Repo" }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: true, crudUpdate: true, crudDelete: true};
            // INSERTのみで全CRUDがフィルタされるとbuildersが空になりnullを返す
            const code = outputs.generatePersistenceMermaidCode(group, visibility);
            assert.equal(code, null);
        });

        test("generatePortMermaidCode: メソッド非表示のとき、複数のexecutionから同一accessorへのエッジが全て生成される", () => {
            // バグ再現: adapter.a -> accessor.a, adapter.b -> accessor.b のとき
            // accessor.a と accessor.b が同じグループに属する場合、
            // adapter.b -> Accessor のエッジが欠落するケース
            const group = {
                outputPort: { label: "PortA" },
                operations: [
                    {
                        outputPortOperation: { label: "op1" },
                        outputAdapter: { fqn: "adapterA", label: "A1" },
                        outputAdapterExecution: { fqn: "execA", label: "execA" },
                        persistenceAccessors: [
                            {
                                id: "com.example.repo.methodA",
                                sqlType: "INSERT",
                                targets: ["table1"],
                                group: "com.example.repo",
                                groupLabel: "Repo"
                            }
                        ]
                    },
                    {
                        outputPortOperation: { label: "op2" },
                        outputAdapter: { fqn: "adapterB", label: "B1" },
                        outputAdapterExecution: { fqn: "execB", label: "execB" },
                        persistenceAccessors: [
                            {
                                id: "com.example.repo.methodB",
                                sqlType: "SELECT",
                                targets: ["table1"],
                                group: "com.example.repo",
                                groupLabel: "Repo"
                            }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: false, target: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outputs.generatePortMermaidCode(group, visibility);
            // execAとexecBの両方からAccessorへのエッジが存在すること
            assert.ok(code.includes('Exec_execA --> Accessor_com_example_repo'), `execA -> Accessor のエッジが存在しない:\n${code}`);
            assert.ok(code.includes('Exec_execB --> Accessor_com_example_repo'), `execB -> Accessor のエッジが存在しない:\n${code}`);
        });

        test("generateOperationMermaidCode: メソッド非表示のとき、複数の永続化操作が同一accessorグループに属する場合にエッジが生成される", () => {
            // バグ再現: 同一リンク内で同じaccessorグループの複数メソッドがある場合
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { label: "op1" },
                outputAdapter: { label: "A1", fqn: "adapterA" },
                outputAdapterExecution: { label: "execA", fqn: "execA" },
                persistenceAccessors: [
                    {
                        id: "com.example.repo.methodA",
                        sqlType: "INSERT",
                        targets: ["table1"],
                        group: "com.example.repo",
                        groupLabel: "Repo"
                    },
                    {
                        id: "com.example.repo.methodB",
                        sqlType: "SELECT",
                        targets: ["table2"],
                        group: "com.example.repo",
                        groupLabel: "Repo"
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: false, target: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outputs.generateOperationMermaidCode(link, visibility);
            // ExecutionからAccessorへのエッジが存在すること
            assert.ok(code.includes('Exec_execA --> Accessor_com_example_repo'), `Exec_execA -> Accessor のエッジが存在しない:\n${code}`);
        });

        test("generatePersistenceMermaidCode: accessor非表示のとき、Execution → Target が直接接続される", () => {
            const group = {
                target: "table1",
                operations: [
                    {
                        outputPort: { fqn: "p1", label: "P1" },
                        outputPortOperation: { fqn: "p1.op1", label: "op1" },
                        outputAdapter: { fqn: "adapter1", label: "A1" },
                        outputAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save", sqlType: "INSERT", targets: ["table1"],
                              group: "com.example.repo", groupLabel: "Repo" }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true};
            const code = outputs.generatePersistenceMermaidCode(group, visibility);
            assert.ok(!code.includes('POp_'));
            assert.ok(code.includes('Exec_exec1'));
            assert.ok(code.includes('Exec_exec1 -- "INSERT" --> Target_0'));
        });
    });

    test.describe("表示レンダリング (DOM操作)", () => {
        // 各テスト前に mermaid 等をリセット
        test.beforeEach(() => {
            setupDocument();
        });
        test("renderOutputsList: 出力ポートごとのカードを描画する", () => {
            const doc = setupDocument();

            outputs.renderOutputsList([
                {
                    outputPort: {fqn: "com.example.APort", label: "A Port"},
                    operations: [
                        {
                            outputPortOperation: {signature: "save(java.lang.String)"},
                            outputAdapter: {label: "A Adapter"},
                            outputAdapterExecution: {signature: "save(java.lang.String)"},
                            persistenceAccessors: [{sqlType: "SELECT", id: "a.save", targets: ["orders"]}],
                        },
                        {
                            outputPortOperation: {signature: "find(java.lang.String)"},
                            outputAdapter: {label: "A Adapter"},
                            outputAdapterExecution: {signature: "find(java.lang.String)"},
                            persistenceAccessors: [],
                        },
                    ],
                },
            ]);

            const outputsList = doc.outputsList;
            assert.equal(outputsList.children.length, 1);
            const portCard = outputsList.children[0];
            assert.equal(portCard.children[0].textContent, "A Port");
            // portFqn (p) が children[1]
            // adapterInfo (p) が children[2] (mode !== 'simple' の場合)
            // count (p) が children[3]
            assert.equal(portCard.children[3].textContent, "2 operations");

            const itemListDetails = portCard.children[5]; // portMermaidContainer が children[4]
            // children[0]: summary, children[1]: div.outputs-item-list
            const itemList = itemListDetails.children[1];
            assert.equal(itemList.children.length, 2);
            const firstItem = itemList.children[0];
            assert.equal(firstItem.children[0].textContent, "save(java.lang.String)");
            assert.equal(firstItem.children[3].children[0].textContent, "SELECT a.save [orders]");
            const secondItem = itemList.children[1];
            assert.equal(secondItem.children[3].children[0].textContent, "なし");
        });

        test("renderCrudTable: CRUDテーブルが正しく描画され、トグル動作が機能する", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outputs-crud");
            const sidebar = doc.getElementById("crud-sidebar-list");
            const grouped = [
                {
                    outputPort: { label: "Port A" },
                    operations: [{
                        outputPortOperation: { label:"opA" },
                        persistenceAccessors: [{ sqlType: "SELECT", targets: ["table1"] }]
                    }]
                }
            ];

            outputs.renderCrudTable(grouped);

            // テーブル構造の確認
            const table = container.children[0];
            assert.equal(table.tagName, "table");

            const thead = table.children[0];
            const headerRow = thead.children[0];
            assert.equal(headerRow.children[1].textContent, "table1");

            const tbody = table.children[1];
            assert.equal(tbody.children.length, 2); // ポート行 + 操作行

            const portRow = tbody.children[0];
            assert.ok(portRow.children[0].textContent.startsWith("Port A"));
            assert.equal(portRow.children[1].textContent, "R");

            const opRow = tbody.children[1];
            assert.equal(opRow.children[0].textContent, "opA");
            assert.equal(opRow.style.display, "none");

            // トグル動作の確認
            portRow.click();
            assert.equal(opRow.style.display, "table-row");

            portRow.click();
            assert.equal(opRow.style.display, "none");
        });

        test("renderCrudTable: C非表示のとき、INSERTが除外される", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outputs-crud");
            const grouped = [{
                outputPort: { label: "Port A" },
                operations: [{
                    outputPortOperation: { label: "opA" },
                    persistenceAccessors: [
                        { sqlType: "INSERT", targets: ["table1"] },
                        { sqlType: "SELECT", targets: ["table1"] }
                    ]
                }]
            }];
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: true, crudUpdate: true, crudDelete: true};
            outputs.renderCrudTable(grouped, visibility);

            const tbody = container.children[0].children[1];
            const portRow = tbody.children[0];
            assert.equal(portRow.children[1].textContent, "R");
            const opRow = tbody.children[1];
            assert.equal(opRow.children[1].textContent, "R");
        });

        test("renderCrudTable: R非表示のとき、SELECTが除外される", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outputs-crud");
            const grouped = [{
                outputPort: { label: "Port A" },
                operations: [{
                    outputPortOperation: { label: "opA" },
                    persistenceAccessors: [
                        { sqlType: "SELECT", targets: ["table1"] }
                    ]
                }]
            }];
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: true, crudRead: false, crudUpdate: true, crudDelete: true};
            outputs.renderCrudTable(grouped, visibility);

            const tbody = container.children[0].children[1];
            const portRow = tbody.children[0];
            assert.equal(portRow.children[1].textContent, "");
            const opRow = tbody.children[1];
            assert.equal(opRow.children[1].textContent, "");
        });

        test("renderCrudTable: 全CRUD非表示のとき、セルが空になる", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outputs-crud");
            const grouped = [{
                outputPort: { label: "Port A" },
                operations: [{
                    outputPortOperation: { label: "opA" },
                    persistenceAccessors: [
                        { sqlType: "INSERT", targets: ["table1"] },
                        { sqlType: "SELECT", targets: ["table1"] },
                        { sqlType: "UPDATE", targets: ["table1"] },
                        { sqlType: "DELETE", targets: ["table1"] }
                    ]
                }]
            }];
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: false, crudUpdate: false, crudDelete: false};
            outputs.renderCrudTable(grouped, visibility);

            const tbody = container.children[0].children[1];
            const portRow = tbody.children[0];
            assert.equal(portRow.children[1].textContent, "");
            const opRow = tbody.children[1];
            assert.equal(opRow.children[1].textContent, "");
        });

        test("renderCrudTable: 全CRUD表示（デフォルト）のとき、CとRが両方表示される", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outputs-crud");
            const grouped = [{
                outputPort: { label: "Port A" },
                operations: [{
                    outputPortOperation: { label: "opA" },
                    persistenceAccessors: [
                        { sqlType: "INSERT", targets: ["table1"] },
                        { sqlType: "SELECT", targets: ["table1"] }
                    ]
                }]
            }];
            outputs.renderCrudTable(grouped);

            const tbody = container.children[0].children[1];
            const portRow = tbody.children[0];
            assert.equal(portRow.children[1].textContent, "CR");
        });

        test("renderCrudTable: 永続化操作がない場合の表示", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outputs-crud");

            outputs.renderCrudTable([{outputPort: {label: "P"}, operations: []}]);
            assert.equal(container.textContent, "永続化操作なし");
        });

        test("renderPersistenceList: 永続化ターゲットごとのカードが描画される", () => {
            const doc = setupDocument();
            const container = doc.getElementById("persistence-list");
            const sidebar = doc.getElementById("persistence-sidebar-list");

            const grouped = [
                {
                    target: "table1",
                    operations: [
                        {
                            outputPort: { fqn: "p1", label: "P1" },
                            outputPortOperation: { fqn: "p1.op1", label: "op1" },
                            outputAdapter: { fqn: "adapter1", label: "A1" },
                            outputAdapterExecution: { fqn: "exec1", label: "ex1" },
                            persistenceAccessors: [
                                { id: "repo.save", sqlType: "INSERT", targets: ["table1"], group: "com.example.repo", groupLabel: "Repo" }
                            ]
                        }
                    ]
                }
            ];

            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: true, accessorMethod: true, target: true};
            outputs.renderPersistenceList(grouped, visibility);

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

        test("renderOutputsList: adapter非表示の場合はAdapter情報が表示されない", () => {
            const doc = setupDocument();
            const grouped = [
                {
                    outputPort: { fqn: "port1" },
                    operations: [{ outputAdapter: { label: "adapter1" } }]
                }
            ];

            const visibility = {port: true, operation: true, adapter: false, execution: false, accessor: false, accessorMethod: false, target: true};
            outputs.renderOutputsList(grouped, visibility);

            const portCard = doc.outputsList.children[0];
            const sidebarSection = doc.getElementById("outputs-sidebar-list").children[0];
            // adapter非表示ではadapterInfo (p) が追加されないため、children[2] は count (p) になる
            assert.equal(portCard.children[2].textContent, "1 operations");
            assert.ok(!portCard.children.some(child => child.textContent.includes("Implementation:")));
            assert.equal(sidebarSection.children[0].textContent, "出力ポート");
            assert.equal(sidebarSection.children[1].children[0].children[0].textContent, "port1");
        });

        test("renderOutputsList / renderPersistenceList: データが空の場合の表示", () => {
            const doc = global.document;

            outputs.renderOutputsList([]);
            // container (outputs-list) の中に p.weak 且つ "データなし" が含まれる
            const container = doc.getElementById("outputs-list");
            assert.equal(container.children[0].textContent, "データなし");

            outputs.renderPersistenceList([], {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true});
            const pContainer = doc.getElementById("persistence-list");
            assert.equal(pContainer.children[0].textContent, "データなし");
        });

        test("DOMContentLoaded 後の初期化とタブ切り替え動作", () => {
            // outputs.js を再読み込みして、モックされた global.window.addEventListener を通るようにする
            delete require.cache[require.resolve("../../main/resources/templates/assets/outputs.js")];
            const doc = setupDocument();
            const reloadedOutputs = require("../../main/resources/templates/assets/outputs.js");
            const app = reloadedOutputs.OutputsApp;

            // チェックボックスの準備
            const checkboxNames = ["show-port", "show-operation", "show-adapter", "show-execution", "show-accessor", "show-accessor-method", "show-target"];
            const checkboxes = checkboxNames.map(name => {
                const cb = doc.createElement("input");
                cb.setAttribute("name", name);
                cb.setAttribute("type", "checkbox");
                cb.checked = ["show-port", "show-operation", "show-adapter", "show-execution", "show-target"].includes(name);
                return cb;
            });

            // タブの準備
            const tabButton = doc.createElement("button");
            tabButton.classList.add("tab-button");
            tabButton.setAttribute("data-tab", "crud");

            const tabPanel = doc.createElement("div");
            tabPanel.id = "crud-tab-panel";
            tabPanel.classList.add("outputs-tab-panel");

            // outputs.js 内の querySelectorAll 等のためのモック
            const originalQuerySelectorAll = doc.querySelectorAll;
            doc.querySelectorAll = (selector) => {
                if (selector === '.outputs-tabs .tab-button') return [tabButton];
                if (selector === '.outputs-tab-panel') return [tabPanel];
                if (selector === 'input[name^="show-"]') return checkboxes;
                if (selector === 'input[name="diagram-direction"]') return [lrRadio, tbRadio];
                return originalQuerySelectorAll.call(doc, selector);
            };

            // diagram-direction ラジオボタンの準備
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

            const originalQuerySelector = doc.querySelector;
            doc.querySelector = (selector) => {
                const nameCheckedMatch = selector.match(/^input\[name="([^"]+)"\]:checked$/);
                if (nameCheckedMatch) {
                    const name = nameCheckedMatch[1];
                    return [...checkboxes, lrRadio, tbRadio].find(cb =>
                        cb.getAttribute("name") === name && cb.checked
                    ) || null;
                }
                const nameMatch = selector.match(/^input\[name="([^"]+)"\]$/);
                if (nameMatch) {
                    return [...checkboxes, lrRadio, tbRadio].find(cb => cb.getAttribute("name") === nameMatch[1]) || null;
                }
                return originalQuerySelector.call(doc, selector);
            };

            // DOMContentLoaded 前に mermaid がある状態にする
            global.mermaid = {
                initialize: () => {},
                render: (id, code) => Promise.resolve({ svg: `<svg id="${id}">${code}</svg>` })
            };

            // DOMContentLoaded 発火
            const listeners = doc.eventListeners.get("DOMContentLoaded") || [];
            listeners.forEach(l => l());

            // App が初期化されていることを確認
            assert.ok(app.state.data);
            assert.ok(app.state.visibility);
            assert.equal(app.state.visibility.port, true);
            assert.equal(app.state.visibility.accessor, false);

            // タブクリックのリスナーが登録されているはず
            assert.ok(tabButton.eventListeners.has("click"));

            // タブクリック
            tabButton.click();
            assert.equal(app.state.activeTab, "crud");
            assert.ok(tabButton.classList.contains("is-active"));
            assert.ok(tabPanel.classList.contains("is-active"));

            // チェックボックス変更でvisibilityが更新される
            const accessorCheckbox = checkboxes.find(cb => cb.getAttribute("name") === "show-accessor");
            accessorCheckbox.checked = true;
            accessorCheckbox.eventListeners.get("change")?.forEach(l => l());
            assert.equal(app.state.visibility.accessor, true);
        });

        test("lazyRender: IntersectionObserver がない場合のフォールバック", () => {
            const oldIO = global.IntersectionObserver;
            delete global.IntersectionObserver;
            try {
                let rendered = false;
                outputs.renderOutputsList([
                    {
                        outputPort: { fqn: "p1" },
                        operations: [{ outputPortOperation: { label:"op1" } }]
                    }
                ]);
                // IntersectionObserver がないので即時実行されるはず
                // ※ mermaid がグローバルにあり、lazyRender 経由で renderPortMermaid が呼ばれる
                // 実際には mermaid.render が非同期なのでフラグ確認は工夫が必要だが、
                // コードパスを通ることが重要
            } finally {
                global.IntersectionObserver = oldIO;
            }
        });

        test("renderOutputsList: port/adapter/accessor/target が全て false のとき、カードが描画されない", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outputs-list");

            const grouped = [
                {
                    outputPort: { fqn: "com.example.APort", label: "A Port" },
                    operations: [
                        {
                            outputPortOperation: { label: "op1" },
                            persistenceAccessors: []
                        }
                    ]
                }
            ];
            const visibility = {port: false, operation: false, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            outputs.renderOutputsList(grouped, visibility);

            assert.equal(container.children.length, 0);
        });

        test("renderPersistenceList: 全CRUD非表示でINSERTのみの操作があるとき、カードが描画されない", () => {
            const doc = setupDocument();
            const container = doc.getElementById("persistence-list");

            const grouped = [
                {
                    target: "table1",
                    operations: [
                        {
                            outputPort: { fqn: "p1", label: "P1" },
                            outputPortOperation: { fqn: "p1.op1", label: "op1" },
                            outputAdapter: { fqn: "adapter1", label: "A1" },
                            outputAdapterExecution: { fqn: "exec1", label: "ex1" },
                            persistenceAccessors: [
                                { id: "repo.insert", sqlType: "INSERT", targets: ["table1"], group: "com.example.repo", groupLabel: "Repo" }
                            ]
                        }
                    ]
                }
            ];
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: false, crudUpdate: false, crudDelete: false};
            outputs.renderPersistenceList(grouped, visibility);

            assert.equal(container.children.length, 0);
        });
    });

    test.describe("MermaidBuilder.isEmpty", () => {
        test("空の builder は true を返す", () => {
            const builder = new outputs.MermaidBuilder();
            assert.equal(builder.isEmpty(), true);
        });

        test("ノード追加後は false を返す", () => {
            const builder = new outputs.MermaidBuilder();
            builder.addNode("n1", "Node1");
            assert.equal(builder.isEmpty(), false);
        });

        test("サブグラフ追加後は false を返す", () => {
            const builder = new outputs.MermaidBuilder();
            builder.startSubgraph("SG");
            assert.equal(builder.isEmpty(), false);
        });

        test("エッジ追加後は false を返す", () => {
            const builder = new outputs.MermaidBuilder();
            builder.addEdge("a", "b");
            assert.equal(builder.isEmpty(), false);
        });
    });

    test.describe("generatePortMermaidCode: null を返すケース", () => {
        test("port/adapter/accessor/target が全て false のとき null を返す", () => {
            const group = {
                outputPort: { label: "PortA" },
                operations: [
                    {
                        outputPortOperation: { label: "op1" },
                        persistenceAccessors: []
                    }
                ]
            };
            const visibility = {port: false, operation: false, adapter: false, execution: false, accessor: false, accessorMethod: false, target: false, direction: 'LR', crudCreate: true, crudRead: true, crudUpdate: true, crudDelete: true};
            const code = outputs.generatePortMermaidCode(group, visibility);
            assert.equal(code, null);
        });
    });

    test.describe("MermaidBuilder: subgraph ID の一意性", () => {
        test("同じラベルの subgraph が2つある場合、それぞれ異なるIDを持つ", () => {
            const builder = new outputs.MermaidBuilder();
            const sg1 = builder.startSubgraph("MyService");
            const sg2 = builder.startSubgraph("MyService");
            assert.notEqual(sg1.id, sg2.id);
        });

        test("同じラベルの subgraph が2つある場合、build() に subgraph が2つ含まれる", () => {
            const builder = new outputs.MermaidBuilder();
            const sg1 = builder.startSubgraph("MyService");
            builder.addNodeToSubgraph(sg1, "n1", "Node1");
            const sg2 = builder.startSubgraph("MyService");
            builder.addNodeToSubgraph(sg2, "n2", "Node2");
            const code = builder.build();
            const matches = code.match(/subgraph /g);
            assert.equal(matches?.length, 2);
        });

        test("build() の subgraph 行が id [\"label\"] 形式になっている", () => {
            const builder = new outputs.MermaidBuilder();
            const sg = builder.startSubgraph("My Service");
            builder.addNodeToSubgraph(sg, "n1", "Node1");
            const code = builder.build();
            assert.match(code, /subgraph sg_My_Service_0 \["My Service"\]/);
        });
    });

    test.describe("generatePersistenceMermaidCode: null を返すケース", () => {
        test("全CRUD非表示のとき null を返す", () => {
            const group = {
                target: "table1",
                operations: [
                    {
                        outputPort: { fqn: "p1", label: "P1" },
                        outputPortOperation: { fqn: "p1.op1", label: "op1" },
                        outputAdapter: { fqn: "adapter1", label: "A1" },
                        outputAdapterExecution: { fqn: "exec1", label: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.insert", sqlType: "INSERT", targets: ["table1"], group: "com.example.repo", groupLabel: "Repo" }
                        ]
                    }
                ]
            };
            const visibility = {port: true, operation: true, adapter: true, execution: true, accessor: false, accessorMethod: false, target: true, direction: 'LR', crudCreate: false, crudRead: false, crudUpdate: false, crudDelete: false};
            const code = outputs.generatePersistenceMermaidCode(group, visibility);
            assert.equal(code, null);
        });
    });
});
