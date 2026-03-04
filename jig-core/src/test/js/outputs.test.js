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
    }

    get textContent() {
        return this._textContent;
    }

    set textContent(value) {
        this._textContent = String(value ?? "");
    }

    appendChild(child) {
        this.children.push(child);
        return child;
    }

    setAttribute(name, value) {
        this.attributes[name] = value;
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
});
