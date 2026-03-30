/**
 * JIGのJavaScriptテストで使用する、ブラウザのDOM APIをエミュレートするスタブクラス群です。
 * Node.js環境（ブラウザ不在）でDOM操作を伴うスクリプトをテストするために使用します。
 */

// ===== セレクタマッチングヘルパー =====

function matchesPart(el, part) {
    if (!part || part === '*') return true;
    // :not() の処理
    const notMatch = part.match(/^([^:]*):not\(([^)]+)\)$/);
    if (notMatch) {
        return matchesPart(el, notMatch[1] || '*') && !matchesPart(el, notMatch[2]);
    }
    // タグ.クラス1.クラス2 形式
    const dotParts = part.split('.');
    const tag = dotParts[0];
    const classes = dotParts.slice(1);
    if (tag && tag !== '*' && el.tagName && el.tagName.toLowerCase() !== tag.toLowerCase()) return false;
    for (const cls of classes) {
        if (!el.classList || !el.classList.has(cls)) return false;
    }
    return true;
}

function collectInSubtree(root, part, results) {
    for (const child of (root.children || [])) {
        if (matchesPart(child, part)) results.push(child);
        collectInSubtree(child, part, results);
    }
}

function findFirstByParts(root, parts) {
    if (parts.length === 0) return null;
    const [first, ...rest] = parts;
    const candidates = [];
    collectInSubtree(root, first, candidates);
    for (const candidate of candidates) {
        if (rest.length === 0) return candidate;
        const found = findFirstByParts(candidate, rest);
        if (found) return found;
    }
    return null;
}

function collectAllByParts(root, parts, results) {
    if (parts.length === 0) return;
    const [first, ...rest] = parts;
    const candidates = [];
    collectInSubtree(root, first, candidates);
    for (const candidate of candidates) {
        if (rest.length === 0) results.push(candidate);
        else collectAllByParts(candidate, rest, results);
    }
}
class ClassList {
    constructor() {
        this.set = new Set();
    }
    add(c) { this.set.add(c); }
    remove(c) { this.set.delete(c); }
    delete(c) { this.set.delete(c); }
    has(c) { return this.set.has(c); }
    contains(c) { return this.set.has(c); }
    toggle(className, force) {
        if (force === true) this.set.add(className);
        else if (force === false) this.set.delete(className);
        else if (this.set.has(className)) this.set.delete(className);
        else this.set.add(className);
    }
    toString() { return Array.from(this.set).join(" "); }
}

class Element {
    constructor(tagName, ownerDocument = null) {
        this.tagName = tagName;
        this.ownerDocument = ownerDocument;
        this.children = [];
        this._textContent = "";
        this._innerHTML = "";
        this.classList = new ClassList();
        this.style = {};
        this.attributes = new Map();
        this.eventListeners = new Map();
        this.value = "";
        this._checked = false;
        this.parentNode = null;
    }

    get parentElement() { return this.parentNode; }

    get id() { return this.getAttribute("id"); }
    set id(value) { this.setAttribute("id", value); }

    get name() { return this.getAttribute("name"); }
    set name(value) { this.setAttribute("name", value); }

    get type() { return this.getAttribute("type"); }
    set type(value) { this.setAttribute("type", value); }

    get href() { return this.getAttribute("href"); }
    set href(value) { this.setAttribute("href", value); }

    get checked() { return this._checked; }
    set checked(value) {
        this._checked = !!value;
        // ラジオボタンの場合、同じ名前を持つ他のラジオボタンのcheckedをfalseにする
        if (this.tagName === "input" && this.type === "radio" && this.name && this._checked && this.ownerDocument) {
            this.ownerDocument.allElements.forEach(el => {
                if (el !== this && el.tagName === "input" && el.type === "radio" && el.name === this.name) {
                    el._checked = false;
                }
            });
        }
    }

    get textContent() {
        if (this.children.length > 0) return this.children.map(c => c.textContent).join("");
        return this._textContent;
    }
    set textContent(value) {
        this._textContent = String(value ?? "");
        this.children = [];
    }

    get innerHTML() { return this._innerHTML; }
    set innerHTML(value) {
        this._innerHTML = value ?? "";
        if (value === "" || value === null || value === undefined) {
            this.children = [];
            this._textContent = "";
        }
    }

    get className() { return this.classList.toString(); }
    set className(value) { this.classList.set = new Set(value.split(" ").filter(c => c)); }

    appendChild(child) {
        if (child && typeof child === "object") child.parentNode = this;
        this.children.push(child);
        return child;
    }

    insertBefore(newNode, referenceNode) {
        if (newNode && typeof newNode === "object") newNode.parentNode = this;
        if (!referenceNode) { this.children.push(newNode); return newNode; }
        const idx = this.children.indexOf(referenceNode);
        if (idx === -1) this.children.push(newNode);
        else this.children.splice(idx, 0, newNode);
        return newNode;
    }

    removeAttribute(name) { this.attributes.delete(name); }

    append(...children) {
        children.forEach(child => {
            if (typeof child === "string") {
                if (this.ownerDocument) {
                    this.appendChild(this.ownerDocument.createTextNode(child));
                } else {
                    const textNode = new Element("#text");
                    textNode.textContent = child;
                    this.appendChild(textNode);
                }
            } else if (child) {
                this.appendChild(child);
            }
        });
    }

    setAttribute(name, value) {
        this.attributes.set(name, String(value));
        if (name === "id" && this.ownerDocument) {
            this.ownerDocument.elementsById.set(value, this);
        }
        if (name === "name" && this.ownerDocument) {
            if (!this.ownerDocument.elementsByName.has(value)) {
                this.ownerDocument.elementsByName.set(value, []);
            }
            this.ownerDocument.elementsByName.get(value).push(this);
        }
    }

    getAttribute(name) { return this.attributes.get(name) || null; }

    addEventListener(type, listener) {
        if (!this.eventListeners.has(type)) this.eventListeners.set(type, []);
        this.eventListeners.get(type).push(listener);
    }

    dispatchEvent(event) {
        const listeners = this.eventListeners.get(event.type) || [];
        listeners.forEach(l => l(event));
    }

    click() { this.dispatchEvent({ type: "click", target: this }); }

    querySelector(selector) {
        if (this.ownerDocument) return this.ownerDocument.querySelector(selector, this);
        const parts = selector.trim().split(/\s+/);
        return findFirstByParts(this, parts);
    }

    querySelectorAll(selector) {
        if (this.ownerDocument) return this.ownerDocument.querySelectorAll(selector, this);
        const parts = selector.trim().split(/\s+/);
        const results = [];
        collectAllByParts(this, parts, results);
        return results;
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.elementsByName = new Map();
        this.eventListeners = new Map();
        this.allElements = [];
        this.body = new Element("body", this);
        // テスト用に querySelector/querySelectorAll の結果を事前登録するMap
        this.selectors = new Map();
        this.selectorsAll = new Map();
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

    createDocumentFragment() {
        return new Element("fragment", this);
    }

    getElementById(id) { return this.elementsById.get(id) || null; }

    getElementsByClassName(className) {
        return this.allElements.filter(el => el.classList.contains(className));
    }

    querySelector(selector, contextElement = null) {
        if (!contextElement && this.selectors.has(selector)) return this.selectors.get(selector);
        if (selector.startsWith('#')) return this.getElementById(selector.substring(1));
        const root = contextElement || this.body;
        const parts = selector.trim().split(/\s+/);
        return findFirstByParts(root, parts);
    }

    querySelectorAll(selector, contextElement = null) {
        if (!contextElement && this.selectorsAll.has(selector)) return this.selectorsAll.get(selector);
        const root = contextElement || this.body;
        const parts = selector.trim().split(/\s+/);
        const results = [];
        collectAllByParts(root, parts, results);
        return results;
    }

    addEventListener(type, listener) {
        if (!this.eventListeners.has(type)) this.eventListeners.set(type, []);
        this.eventListeners.get(type).push(listener);
    }

    dispatchEvent(event) {
        const listeners = this.eventListeners.get(event.type) || [];
        listeners.forEach(l => l(event));
    }
}

class EventStub {
    constructor(type) { this.type = type; }
}

// ----- glossaryData ヘルパー -----
// 古いフォーマット（fqnキーマップ）と新しいフォーマット（wrapper形式）の両方に対応
// すべてのglossaryData設定をこれ経由で行い、テストの脆性を減らす
function setGlossaryData(data) {
    if (!data) {
        globalThis.glossaryData = { terms: {} };
        return;
    }

    // 空オブジェクトの場合
    if (!Array.isArray(data) && Object.keys(data).length === 0) {
        globalThis.glossaryData = { terms: {} };
        return;
    }

    // 配列形式（normalizeGlossaryDataの結果）をそのまま使用
    if (Array.isArray(data)) {
        globalThis.glossaryData = data;
        return;
    }

    // wrapper形式チェック：
    // - {terms: {...}, domainPackageRoots?: [...]} の形か
    // - {domainPackageRoots: [...]} のような形か
    const hasTerms = data.terms !== undefined && typeof data.terms === "object" && !Array.isArray(data.terms);
    const hasDomainPackageRoots = Array.isArray(data.domainPackageRoots);

    if (hasTerms || hasDomainPackageRoots || Object.keys(data).some(k => !k.includes('.'))) {
        // wrapper形式の可能性: プロパティ名にドット（FQN）がない場合はwrapper形式
        globalThis.glossaryData = data;
        return;
    }

    // 古いフォーマット（fqnキーマップ）を新しいwrapper形式に変換
    globalThis.glossaryData = { terms: data };
}

module.exports = { Element, DocumentStub, EventStub, setGlossaryData };
