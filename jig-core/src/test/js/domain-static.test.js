const test = require("node:test");
const assert = require("node:assert/strict");

const domainStatic = require("../../main/resources/templates/assets/domain-static.js");

class ClassList {
    constructor() {
        this.set = new Set();
    }
    contains(value) {
        return this.set.has(value);
    }
    add(value) {
        this.set.add(value);
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
        this.attributes = new Map();
        this.classList = new ClassList();
        this.parentNode = null;
        this.style = {};
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
        this.classList.set = new Set(String(value).split(" ").filter(Boolean));
    }

    get id() {
        return this.getAttribute("id");
    }

    set id(value) {
        this.setAttribute("id", value);
    }

    appendChild(child) {
        child.parentNode = this;
        this.children.push(child);
        return child;
    }

    setAttribute(name, value) {
        this.attributes.set(name, String(value));
        if (name === "id" && this.ownerDocument) {
            this.ownerDocument.elementsById.set(String(value), this);
        }
    }

    getAttribute(name) {
        return this.attributes.get(name) || null;
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.body = new Element("body", this);
        this.body.className = "domain-static outputs";
    }

    createElement(tagName) {
        return new Element(tagName, this);
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }
}

function collectElements(root, predicate) {
    const result = [];
    const stack = [root];
    while (stack.length > 0) {
        const node = stack.pop();
        if (!node) continue;
        if (predicate(node)) result.push(node);
        if (Array.isArray(node.children)) {
            for (const child of node.children) {
                stack.push(child);
            }
        }
    }
    return result;
}

function setupDocument(jsonText) {
    const doc = new DocumentStub();

    const sidebarList = doc.createElement("div");
    sidebarList.id = "domain-sidebar-list";

    const main = doc.createElement("section");
    main.id = "domain-main";

    const dataScript = doc.createElement("script");
    dataScript.id = "domain-data";
    dataScript.textContent = jsonText;

    global.document = doc;
    global.window = {};

    return { doc, sidebarList, main, dataScript };
}

function findFirst(root, predicate) {
    const items = collectElements(root, predicate);
    return items[0] || null;
}

function findListItemForHref(root, href) {
    return findFirst(root, el => el.tagName === "li" && collectElements(el, n => n.tagName === "a" && n.getAttribute("href") === href).length > 0);
}

test.describe("domain-static.js", () => {
    test("DomainStaticApp.init renders sidebar and main", () => {
        const json = JSON.stringify({
            packages: [
                { id: "com", label: "com", fqn: "com", description: "", parent: null },
                { id: "com.example", label: "example", fqn: "com.example", description: "example root package", parent: "com" },
                { id: "com.example.service", label: "service", fqn: "com.example.service", description: "service layer", parent: "com.example" },
                { id: "com.example.repository", label: "repository", fqn: "com.example.repository", description: "repository layer", parent: "com.example" },
            ],
            classes: [
                {
                    id: "com.example.service.UserService",
                    label: "UserService",
                    fqn: "com.example.service.UserService",
                    description: "user operations",
                    package: "com.example.service",
                    fields: [{ label: "repository", name: "repository", description: "user repository", type: "com.example.repository.UserRepository" }],
                    methods: [{ label: "findUser", name: "findUser", description: "find user", returnType: "com.example.domain.User", parameters: [{ name: "id", type: "java.lang.String" }] }]
                },
                {
                    id: "com.example.repository.UserRepository",
                    label: "UserRepository",
                    fqn: "com.example.repository.UserRepository",
                    description: "user persistence",
                    package: "com.example.repository",
                    fields: [],
                    methods: [{ label: "findById", name: "findById", description: "load user", returnType: "com.example.domain.User", parameters: [{ name: "id", type: "java.lang.String" }] }]
                }
            ]
        });

        const { doc } = setupDocument(json);

        domainStatic.DomainStaticApp.init();

        const sidebar = doc.getElementById("domain-sidebar-list");
        const main = doc.getElementById("domain-main");

        const sidebarLinks = collectElements(sidebar, el => el.tagName === "a");
        assert.equal(sidebarLinks.length, 6);
        const hrefs = new Set(sidebarLinks.map(a => a.getAttribute("href")));
        assert.ok(hrefs.has("#com"));
        assert.ok(hrefs.has("#com.example.service.UserService"));

        const exampleLi = findListItemForHref(sidebar, "#com.example");
        assert.ok(exampleLi);
        const serviceLinkInExample = findFirst(exampleLi, el => el.tagName === "a" && el.getAttribute("href") === "#com.example.service");
        assert.ok(serviceLinkInExample);

        const cards = collectElements(main, el => el.tagName === "article");
        assert.equal(cards.length, 6);

        const ids = new Set(cards.map(c => c.id));
        assert.ok(ids.has("com.example.service.UserService"));
        assert.ok(ids.has("com.example.repository"));
    });

    test("getDomainData returns empty arrays on invalid JSON", () => {
        setupDocument("{ this is not json }");
        const data = domainStatic.getDomainData();
        assert.deepEqual(data, { packages: [], classes: [] });
    });
});
