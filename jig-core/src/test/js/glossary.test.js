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

test.describe('glossary.js', () => {
    test.describe('絞り込み', () => {
        test('種類・説明有無・検索語でフィルタする', () => {
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
    });

    test.describe('CSV', () => {
        test('CSV値はクォートし、改行とダブルクォートを処理する', () => {
            const value = '"a"\r\nline';

            const escaped = glossary.escapeCsvValue(value);

            assert.equal(escaped, '"""a""\nline"');
        });

        test('CSVにヘッダーと行を出力する', () => {
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
    });

    test.describe('データ読み込み', () => {
        test('glossary-dataから用語一覧を取得する', () => {
            const doc = setupDocument();
            const dataElement = new Element('script');
            dataElement.textContent = JSON.stringify({terms: [{title: 'Account'}]});
            doc.elementsById.set('glossary-data', dataElement);

            const terms = glossary.getGlossaryData();

            assert.equal(terms.length, 1);
            assert.equal(terms[0].title, 'Account');
        });
    });

    test.describe('描画', () => {
        test('用語一覧をDOMに描画する', () => {
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
    });
});
