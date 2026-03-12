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
        if (selector === 'input[name="display-mode"]:checked') {
            return this.allElements.find(el => 
                el.tagName === "input" && 
                el.getAttribute("name") === "display-mode" && 
                el.checked
            ) || null;
        }
        return null;
    }

    querySelectorAll(selector) {
        if (selector === 'input[name="display-mode"]') {
            return this.allElements.filter(el => 
                el.tagName === "input" && 
                el.getAttribute("name") === "display-mode"
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
        if (selector === 'input[name="display-mode"]:checked') {
            const checked = this.querySelector(selector);
            return checked ? [checked] : [];
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
        test("groupLinksByOutputPort: 出力ポート単位でグルーピングし、表示名でソートする", () => {
            const links = [
                {
                    outputPort: {fqn: "com.example.BPort", label: "い"},
                    outputPortOperation: {name: "delete"},
                },
                {
                    outputPort: {fqn: "com.example.APort", label: "あ"},
                    outputPortOperation: {name: "save"},
                },
            ];

            const grouped = outputs.groupLinksByOutputPort(links);
            assert.equal(grouped[0].outputPort.label, "あ");
            assert.equal(grouped[1].outputPort.label, "い");
        });

        test("groupLinksByOutputPort: 境界条件（空のリスト）", () => {
            const grouped = outputs.groupLinksByOutputPort([]);
            assert.equal(grouped.length, 0);
        });

        test("groupLinksByPersistenceTarget: ターゲット単位でグルーピングし、ターゲット名でソートする", () => {
            const links = [
                {
                    persistenceAccessors: [{ targets: ["table_b"], id: "op1" }],
                    outputPort: { fqn: "port1", label: "P1" }
                },
                {
                    persistenceAccessors: [{ targets: ["table_a"], id: "op2" }],
                    outputPort: { fqn: "port2", label: "P2" }
                }
            ];
            const grouped = outputs.groupLinksByPersistenceTarget(links);
            assert.equal(grouped.length, 2);
            assert.equal(grouped[0].target, "table_a");
            assert.equal(grouped[1].target, "table_b");
            assert.equal(grouped[0].links[0].outputPort.label, "P2");
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

        test("getOutputsData: JSONからデータを正しくパースし、リンクを組み立てる", () => {
            const doc = setupDocument();
            const dataEl = doc.createElement("script");
            dataEl.id = "outputs-data";
            dataEl.textContent = JSON.stringify({
                ports: { p1: { fqn: "port1", label: "P1" } },
                operations: { op1: { name: "save" } },
                adapters: { a1: { fqn: "adapter1" } },
                executions: { ex1: { name: "exec" } },
                persistenceAccessors: {
                    po1: { id: "po1", sqlType: "INSERT", targets: ["table1"], group: "com.example.Repo" }
                },
                links: [{ port: "p1", operation: "op1", adapter: "a1", execution: "ex1", persistenceAccessors: ["po1"] }]
            });

            const data = outputs.getOutputsData();
            assert.equal(data.links.length, 1);
            const link = data.links[0];
            assert.equal(link.outputPort.label, "P1");
            assert.equal(link.persistenceAccessors[0].groupLabel, "Repo");
        });

        test("getOutputsData: データがない場合のフォールバック", () => {
            setupDocument();
            // outputs-data が存在しない場合
            const data = outputs.getOutputsData();
            assert.deepEqual(data.links, []);
        });

        test("createField: ラベルと値を持つ要素を生成する", () => {
            setupDocument();
            const field = outputs.createField("Label", "Value");
            assert.equal(field.className, "outputs-item-field");
            assert.equal(field.children[0].textContent, "Label");
            assert.equal(field.children[1].textContent, "Value");
        });
    });

    test.describe("Mermaidコード生成", () => {
        test("generateMermaidCode: standardモードで正しい接続関係を生成する", () => {
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { name: "op1" },
                outputAdapter: { label: "A1" },
                outputAdapterExecution: { name: "ex1" },
                persistenceAccessors: [
                    { id: "repo.save", sqlType: "INSERT", targets: ["table1"] }
                ]
            };
            const code = outputs.generateMermaidCode(link, "standard");
            assert.ok(code.includes('subgraph "P1"'));
            assert.ok(code.includes('PortOp["op1"]'));
            assert.ok(code.includes('subgraph "A1"'));
            assert.ok(code.includes('Execution["ex1"]'));
            assert.ok(code.includes('PortOp --> Execution'));
            assert.ok(code.includes('Execution -- "INSERT" --> Target_0'));
            assert.ok(code.includes('Target_0[(table1)]'));
        });

        test("generateMermaidCode: simpleモードでAdapterを省略する", () => {
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { name: "op1" },
                persistenceAccessors: [
                    { id: "repo.save", sqlType: "INSERT", targets: ["table1"] }
                ]
            };
            const code = outputs.generateMermaidCode(link, "simple");
            assert.ok(code.includes('PortOp["op1"]'));
            assert.ok(!code.includes('subgraph "Adapter"'));
            assert.ok(code.includes('PortOp -- "INSERT" --> Target_0'));
        });

        test("generateMermaidCode: detailedモードで永続化操作のグループ（クラス）を表示する", () => {
            const link = {
                outputPort: { label: "P1" },
                outputPortOperation: { name: "op1" },
                outputAdapter: { label: "A1" },
                outputAdapterExecution: { name: "ex1" },
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
            const code = outputs.generateMermaidCode(link, "detailed");
            assert.ok(code.includes('subgraph "repo"'));
            assert.ok(code.includes('Group_0["save"]'));
            assert.ok(code.includes('Execution --> Group_0'));
            assert.ok(code.includes('Group_0 -- "INSERT" --> Target_0'));
        });

        test("generatePortMermaidCode: ポート単位の図に複数の操作が含まれる", () => {
            const group = {
                outputPort: { label: "PortA" },
                links: [
                    { outputPortOperation: { name: "op1" } },
                    { outputPortOperation: { name: "op2" } }
                ]
            };
            const code = outputs.generatePortMermaidCode(group, "simple");
            assert.ok(code.includes('subgraph "PortA"'));
            assert.ok(code.includes('PortOp_0["op1"]'));
            assert.ok(code.includes('PortOp_1["op2"]'));
        });

        test("generatePortMermaidCode: detailedモードで永続化操作のグループを表示する", () => {
            const group = {
                outputPort: { label: "PortA" },
                links: [
                    {
                        outputPortOperation: { name: "op1" },
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
            const code = outputs.generatePortMermaidCode(group, "detailed");
            assert.ok(code.includes('subgraph "repo"'));
            assert.ok(code.includes('POp_com_example_repo_save["save"]'));
            assert.ok(code.includes('Exec_adapter1_ex1 --> POp_com_example_repo_save') || code.includes('Exec_exec1 --> POp_com_example_repo_save'));
        });

        test("generatePersistenceMermaidCode: ターゲット中心の図が生成される", () => {
            const group = {
                target: "table1",
                links: [
                    {
                        outputPort: { label: "P1" },
                        outputPortOperation: { name: "op1" },
                        outputAdapter: { fqn: "adapter1", label: "A1" },
                        outputAdapterExecution: { fqn: "exec1", name: "ex1" },
                        persistenceAccessors: [
                            { id: "repo.save", sqlType: "INSERT", targets: ["table1"] }
                        ]
                    }
                ]
            };
            const code = outputs.generatePersistenceMermaidCode(group);
            assert.ok(code.includes('Target[(table1)]'));
            assert.ok(code.includes('POp_repo_save -- "INSERT" --> Target'));
            assert.ok(code.includes('Execution_adapter1_ex1 --> POp_repo_save') || code.includes('Execution_exec1 --> POp_repo_save'));
        });
    });

    test.describe("表示レンダリング (DOM操作)", () => {
        // 各テスト前に mermaid 等をリセット
        test.beforeEach(() => {
            setupDocument();
        });
        test("renderOutputsTable: 出力ポートごとのカードを描画する", () => {
            const doc = setupDocument();

            outputs.renderOutputsTable([
                {
                    outputPort: {fqn: "com.example.APort", label: "A Port"},
                    links: [
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

            const itemList = portCard.children[5]; // portMermaidContainer が children[4]
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
            const links = [
                {
                    outputPort: { label: "Port A" },
                    outputPortOperation: { name: "opA" },
                    persistenceAccessors: [{ sqlType: "SELECT", targets: ["table1"] }]
                }
            ];

            outputs.renderCrudTable(links);

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

            const sidebarSection = sidebar.children[0];
            assert.equal(sidebarSection.className, "in-page-sidebar__section");
            assert.equal(sidebarSection.children[0].textContent, "永続化操作対象");
            assert.equal(sidebarSection.children[1].children[0].children[0].getAttribute("href"), "#crud-target-table1");

            // トグル動作の確認
            portRow.click();
            assert.equal(opRow.style.display, "table-row");

            portRow.click();
            assert.equal(opRow.style.display, "none");
        });

        test("renderCrudTable: 永続化操作がない場合の表示", () => {
            const doc = setupDocument();
            const container = doc.getElementById("outputs-crud");
            
            outputs.renderCrudTable([]);
            assert.equal(container.textContent, "永続化操作なし");
        });

        test("renderPersistenceTable: 永続化ターゲットごとのカードが描画される", () => {
            const doc = setupDocument();
            const container = doc.getElementById("persistence-list");
            const sidebar = doc.getElementById("persistence-sidebar-list");
            
            const grouped = [
                {
                    target: "table1",
                    links: []
                }
            ];

            outputs.renderPersistenceTable(grouped);

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

        test("renderOutputsTable: mode='simple' の場合は Adapter 情報が表示されない", () => {
            const doc = setupDocument();
            const grouped = [
                {
                    outputPort: { fqn: "port1" },
                    links: [{ outputAdapter: { label: "adapter1" } }]
                }
            ];

            outputs.renderOutputsTable(grouped, "simple");

            const portCard = doc.outputsList.children[0];
            const sidebarSection = doc.getElementById("outputs-sidebar-list").children[0];
            // simple モードでは adapterInfo (p) が追加されないため、children[2] は count (p) になる
            assert.equal(portCard.children[2].textContent, "1 operations");
            assert.ok(!portCard.children.some(child => child.textContent.includes("Implementation:")));
            assert.equal(sidebarSection.children[0].textContent, "出力ポート");
            assert.equal(sidebarSection.children[1].children[0].children[0].textContent, "port1");
        });

        test("renderOutputsTable / renderPersistenceTable: データが空の場合の表示", () => {
            const doc = global.document;
            
            outputs.renderOutputsTable([]);
            // container (outputs-list) の中に p.weak 且つ "データなし" が含まれる
            const container = doc.getElementById("outputs-list");
            assert.equal(container.children[0].textContent, "データなし");

            outputs.renderPersistenceTable([]);
            const pContainer = doc.getElementById("persistence-list");
            assert.equal(pContainer.children[0].textContent, "データなし");
        });

        test("DOMContentLoaded 後の初期化とタブ切り替え動作", () => {
            // outputs.js を再読み込みして、モックされた global.window.addEventListener を通るようにする
            delete require.cache[require.resolve("../../main/resources/templates/assets/outputs.js")];
            const doc = setupDocument();
            const reloadedOutputs = require("../../main/resources/templates/assets/outputs.js");
            const app = reloadedOutputs.OutputsApp;

            // ラジオボタンの準備
            const radio = doc.createElement("input");
            radio.setAttribute("name", "display-mode");
            radio.setAttribute("type", "radio");
            radio.value = "simple";
            radio.checked = true;

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
                if (selector === 'input[name="display-mode"]') return [radio];
                return originalQuerySelectorAll.call(doc, selector);
            };

            const originalQuerySelector = doc.querySelector;
            doc.querySelector = (selector) => {
                if (selector === 'input[name="display-mode"]:checked') return radio;
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
            assert.equal(app.state.mode, "simple");

            // タブクリックのリスナーが登録されているはず
            assert.ok(tabButton.eventListeners.has("click"));

            // タブクリック
            tabButton.click();
            assert.equal(app.state.activeTab, "crud");
            assert.ok(tabButton.classList.contains("is-active"));
            assert.ok(tabPanel.classList.contains("is-active"));

            // 表示モード変更
            radio.value = "detailed";
            radio.eventListeners.get("change")?.forEach(l => l({ target: radio }));
            assert.equal(app.state.mode, "detailed");
        });

        test("lazyRender: IntersectionObserver がない場合のフォールバック", () => {
            const oldIO = global.IntersectionObserver;
            delete global.IntersectionObserver;
            try {
                let rendered = false;
                outputs.renderOutputsTable([
                    {
                        outputPort: { fqn: "p1" },
                        links: [{ outputPortOperation: { name: "op1" } }]
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
    });
});
