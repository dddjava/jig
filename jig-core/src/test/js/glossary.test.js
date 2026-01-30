const test = require('node:test');
const assert = require('node:assert/strict');

const glossary = require('../../main/resources/templates/assets/glossary.js');

class Element {
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this.textContent = '';
        this.innerHTML = '';
        this.className = '';
        this.parentElement = null;
    }

    appendChild(child) {
        child.parentElement = this;
        this.children.push(child);
        return child;
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
    }

    createElement(tagName) {
        return new Element(tagName);
    }

    createDocumentFragment() {
        return new Element('fragment');
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }
}

function setupDocument() {
    const doc = new DocumentStub();
    global.document = doc;
    return doc;
}

test('getFilteredTerms respects kind, description, and keyword filters', () => {
    const terms = [
        {title: 'Account', description: 'desc', kind: 'クラス'},
        {title: 'Order', description: '', kind: 'クラス'},
        {title: 'Repo', description: 'data', kind: 'パッケージ'},
        {title: 'Submit', description: 'action', kind: 'メソッド'},
    ];
    const controls = {
        showEmptyDescription: {checked: false},
        showPackage: {checked: false},
        showClass: {checked: true},
        showMethod: {checked: true},
        showField: {checked: true},
        searchInput: {value: 'acc'},
    };

    const result = glossary.getFilteredTerms(terms, controls);

    assert.deepEqual(result, [terms[0]]);
});

test('escapeCsvValue wraps, escapes quotes, and normalizes newlines', () => {
    const value = '"a"\r\nline';

    const escaped = glossary.escapeCsvValue(value);

    assert.equal(escaped, '"""a""\nline"');
});

test('buildGlossaryCsv creates header and rows', () => {
    const terms = [
        {simpleText: 'Account', title: '口座', description: 'desc', kind: 'クラス', fqn: 'app.Account'},
    ];

    const csv = glossary.buildGlossaryCsv(terms);

    assert.equal(
        csv,
        '"用語（英名）","用語","説明","種類","識別子"\r\n' +
            '"Account","口座","desc","クラス","app.Account"'
    );
});

test('getGlossaryData reads terms from glossary-data element', () => {
    const doc = setupDocument();
    const dataElement = new Element('script');
    dataElement.textContent = JSON.stringify({terms: [{title: 'Account'}]});
    doc.elementsById.set('glossary-data', dataElement);

    const terms = glossary.getGlossaryData();

    assert.equal(terms.length, 1);
    assert.equal(terms[0].title, 'Account');
});

test('renderGlossaryTerms builds a term list', () => {
    const doc = setupDocument();
    const list = new Element('div');
    list.innerHTML = 'existing';
    doc.elementsById.set('term-list', list);

    glossary.renderGlossaryTerms([
        {title: 'Account', simpleText: 'Account', fqn: 'app.Account', kind: 'クラス', description: 'desc'},
        {title: 'Order', simpleText: 'Order', fqn: 'app.Order', kind: 'クラス', description: ''},
    ]);

    assert.equal(list.innerHTML, '');
    assert.equal(list.children.length, 1);
    assert.equal(list.children[0].children.length, 2);
});
