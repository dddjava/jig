const test = require("node:test");
const assert = require("node:assert/strict");

const outputs = require("../../main/resources/templates/assets/outputs.js");

class Element {
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this._textContent = "";
        this.innerHTML = "";
        this.attributes = {};
        this.style = {};
        this.classList = {
            set: new Set(),
            add: (c) => this.classList.set.add(c),
            delete: (c) => this.classList.set.delete(c),
            has: (c) => this.classList.set.has(c),
            toggle: (c, f) => this.toggle(c, f),
            contains: (c) => this.classList.set.has(c)
        };
        this.eventListeners = new Map();
    }

    get textContent() {
        return this._textContent;
    }

    set textContent(value) {
        this._textContent = String(value ?? "");
    }

    get className() {
        return Array.from(this.classList.set).join(" ");
    }

    set className(value) {
        this.classList.set = new Set(value.split(" ").filter(c => c));
    }

    appendChild(child) {
        this.children.push(child);
        return child;
    }

    setAttribute(name, value) {
        this.attributes[name] = value;
    }

    getAttribute(name) {
        return this.attributes[name] || null;
    }

    addEventListener(type, listener) {
        if (!this.eventListeners.has(type)) {
            this.eventListeners.set(type, []);
        }
        this.eventListeners.get(type).push(listener);
    }

    click() {
        const listeners = this.eventListeners.get("click") || [];
        listeners.forEach(l => l({ type: "click", target: this }));
    }

    toggle(className, force) {
        if (force === true) {
            this.classList.set.add(className);
        } else if (force === false) {
            this.classList.set.delete(className);
        } else {
            if (this.classList.set.has(className)) {
                this.classList.set.delete(className);
            } else {
                this.classList.set.add(className);
            }
        }
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.outputsList = null;
    }

    createElement(tagName) {
        return new Element(tagName);
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }

    querySelector(selector) {
        return null;
    }
}

function setupDocument() {
    const doc = new DocumentStub();
    
    const outputsList = new Element("section");
    doc.outputsList = outputsList;
    doc.elementsById.set("outputs-list", outputsList);

    doc.elementsById.set("outputs-crud", new Element("div"));
    doc.elementsById.set("crud-sidebar", new Element("div"));
    doc.elementsById.set("crud-sidebar-list", new Element("div"));

    doc.elementsById.set("persistence-list", new Element("div"));
    doc.elementsById.set("persistence-sidebar-list", new Element("div"));

    doc.elementsById.set("outputs-sidebar-list", new Element("div"));

    global.document = doc;
    return doc;
}

test.describe("outputs.js", () => {
    test("groupLinksByOutputPort: 出力ポート単位でグルーピングし、表示名でソートする", () => {
        const links = [
            {
                outputPort: {fqn: "com.example.BPort", label: "B Port"},
                outputPortOperation: {name: "delete"},
            },
            {
                outputPort: {fqn: "com.example.APort", label: "A Port"},
                outputPortOperation: {name: "save"},
            },
            {
                outputPort: {fqn: "com.example.APort", label: "A Port"},
                outputPortOperation: {name: "find"},
            },
        ];

        const grouped = outputs.groupLinksByOutputPort(links);

        assert.equal(grouped.length, 2);
        // A Port (A) が B Port (B) より先に来る
        assert.equal(grouped[0].outputPort.label, "A Port");
        // 操作名は find (f) が save (s) より先に来る
        assert.equal(grouped[0].links[0].outputPortOperation.name, "find");
        assert.equal(grouped[0].links[1].outputPortOperation.name, "save");

        assert.equal(grouped[1].outputPort.label, "B Port");
    });

    test("groupLinksByOutputPort: 境界条件（空のリスト）", () => {
        const grouped = outputs.groupLinksByOutputPort([]);
        assert.equal(grouped.length, 0);
    });

    test("groupLinksByPersistenceTarget: ターゲット単位でグルーピングし、ターゲット名でソートする", () => {
        const links = [
            {
                persistenceOperations: [{ targets: ["table_b"], id: "op1" }],
                outputPort: { fqn: "port1", label: "P1" }
            },
            {
                persistenceOperations: [{ targets: ["table_a"], id: "op2" }],
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

    test("formatPersistenceOperations: 永続化操作を改行区切りで整形する", () => {
        const formatted = outputs.formatPersistenceOperations([
            {sqlType: "SELECT", id: "com.example.Mapper.find", targets: ["orders"]},
            {sqlType: "UPDATE", id: "com.example.Mapper.update", targets: ["orders", "order_items"]},
        ]);

        assert.deepEqual(formatted, [
            "SELECT com.example.Mapper.find [orders]",
            "UPDATE com.example.Mapper.update [orders, order_items]",
        ]);
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
                        persistenceOperations: [{sqlType: "SELECT", id: "a.save", targets: ["orders"]}],
                    },
                    {
                        outputPortOperation: {signature: "find(java.lang.String)"},
                        outputAdapter: {label: "A Adapter"},
                        outputAdapterExecution: {signature: "find(java.lang.String)"},
                        persistenceOperations: [],
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
        const links = [
            {
                outputPort: { label: "Port A" },
                outputPortOperation: { name: "opA" },
                persistenceOperations: [{ sqlType: "SELECT", targets: ["table1"] }]
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
        // 360行目付近: portCell.textContent = label
        // 367行目付近: portCell.appendChild(countSpan)
        // Elementの実装では appendChild しても _textContent は変わらない。
        // _textContent は「直下のテキスト」ではなく「全てのテキスト」を期待してしまっている？
        // 現状の Element 実装では textContent は _textContent を返すだけ。
        assert.equal(portRow.children[0].textContent, "Port A");
        assert.equal(portRow.children[0].children[0].textContent, "(1)");
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
        assert.equal(card.id, "persistence-table1");
        assert.equal(card.children[0].textContent, "table1");

        // サイドバーの確認
        assert.notEqual(sidebar.children.length, 0);
        const sidebarList = sidebar.children[0];
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
        // simple モードでは adapterInfo (p) が追加されないため、children[2] は count (p) になる
        assert.equal(portCard.children[2].textContent, "1 operations");
        assert.ok(!portCard.children.some(child => child.textContent.includes("Implementation:")));
    });
});
