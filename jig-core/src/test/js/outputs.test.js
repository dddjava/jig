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
        this.outputsTableBody = null;
    }

    createElement(tagName) {
        return new Element(tagName);
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }

    querySelector(selector) {
        if (selector === "#outputs-list tbody") {
            return this.outputsTableBody;
        }
        return null;
    }
}

function setupDocument() {
    const doc = new DocumentStub();
    global.document = doc;
    return doc;
}

test.describe("outputs.js", () => {
    test("groupLinksByOutputPort: 出力ポート単位でグルーピングする", () => {
        const links = [
            {
                outputPort: {fqn: "com.example.APort", label: "A Port"},
                outputPortOperation: {name: "save"},
            },
            {
                outputPort: {fqn: "com.example.APort", label: "A Port"},
                outputPortOperation: {name: "find"},
            },
            {
                outputPort: {fqn: "com.example.BPort", label: "B Port"},
                outputPortOperation: {name: "delete"},
            },
        ];

        const grouped = outputs.groupLinksByOutputPort(links);

        assert.equal(grouped.length, 2);
        assert.equal(grouped[0].outputPort.fqn, "com.example.APort");
        assert.equal(grouped[0].links.length, 2);
        assert.equal(grouped[1].outputPort.fqn, "com.example.BPort");
        assert.equal(grouped[1].links.length, 1);
    });

    test("formatPersistenceOperations: 永続化操作を改行区切りで整形する", () => {
        const formatted = outputs.formatPersistenceOperations([
            {sqlType: "SELECT", id: "com.example.Mapper.find", targets: ["orders"]},
            {sqlType: "UPDATE", id: "com.example.Mapper.update", targets: ["orders", "order_items"]},
        ]);

        assert.equal(
            formatted,
            "SELECT com.example.Mapper.find [orders]\nUPDATE com.example.Mapper.update [orders, order_items]"
        );
    });

    test("renderOutputsTable: 出力ポートごとのrowspan付きで描画する", () => {
        const doc = setupDocument();
        const tbody = new Element("tbody");
        doc.outputsTableBody = tbody;

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

        assert.equal(tbody.children.length, 2);
        assert.equal(tbody.children[0].children[0].textContent, "A Port");
        assert.equal(tbody.children[0].children[0].attributes.rowspan, "2");
        assert.equal(tbody.children[0].children[4].textContent, "SELECT a.save [orders]");
        assert.equal(tbody.children[1].children[3].textContent, "なし");
    });
});
