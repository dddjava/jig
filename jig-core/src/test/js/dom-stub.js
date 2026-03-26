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
        this.innerHTML = "";
        this.classList = new ClassList();
        this.style = {};
        this.attributes = new Map();
        this.eventListeners = new Map();
        this.value = "";
        this._checked = false;
    }

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

    get className() { return this.classList.toString(); }
    set className(value) { this.classList.set = new Set(value.split(" ").filter(c => c)); }

    appendChild(child) {
        this.children.push(child);
        return child;
    }

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
        return null;
    }

    querySelectorAll(selector) {
        if (this.ownerDocument) return this.ownerDocument.querySelectorAll(selector, this);
        return [];
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.elementsByName = new Map();
        this.eventListeners = new Map();
        this.allElements = [];
        this.body = new Element("body", this);
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
        if (selector.startsWith('#')) return this.getElementById(selector.substring(1));
        const root = contextElement || this.body;
        const parts = selector.trim().split(/\s+/);
        return findFirstByParts(root, parts);
    }

    querySelectorAll(selector, contextElement = null) {
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

class LocalStorageStub {
    constructor() { this.storage = {}; }
    getItem(key) { return Object.prototype.hasOwnProperty.call(this.storage, key) ? this.storage[key] : null; }
    setItem(key, value) { this.storage[key] = String(value); }
    removeItem(key) { delete this.storage[key]; }
}

class EventStub {
    constructor(type) { this.type = type; }
}

module.exports = { Element, DocumentStub, LocalStorageStub, EventStub };
