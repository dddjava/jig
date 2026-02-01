const test = require('node:test');
const assert = require('node:assert/strict');

const listOutput = require('../../main/resources/templates/assets/list-output.js');

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

test.describe('list-output.js CSV', () => {
    test('CSV値はクォートし、改行とダブルクォートを処理する', () => {
        const value = '"a"\r\nline';

        const escaped = listOutput.escapeCsvValue(value);

        assert.equal(escaped, '"""a""\nline"');
    });

    test('CSVにヘッダーと行を出力する', () => {
        const items = [
            {
                packageName: 'com.example',
                typeName: 'ExampleController',
                methodSignature: 'getExample()',
                returnType: 'Example',
                typeLabel: '例',
                usingFieldTypes: '[ExampleRepository]',
                cyclomaticComplexity: 2,
                path: 'GET /example',
            },
        ];

        const csv = listOutput.buildControllerCsv(items);

        assert.equal(
            csv,
            '"パッケージ名","クラス名","メソッドシグネチャ","メソッド戻り値の型","クラス別名","使用しているフィールドの型","循環的複雑度","パス"\r\n' +
                '"com.example","ExampleController","getExample()","Example","例","[ExampleRepository]","2","GET /example"'
        );
    });
});

test.describe('list-output.js データ読み込み', () => {
    test('list-dataから一覧を取得する', () => {
        const doc = setupDocument();
        const dataElement = new Element('script');
        dataElement.textContent = JSON.stringify({
            controllers: [{typeName: 'ExampleController'}],
        });
        doc.elementsById.set('list-data', dataElement);

        const items = listOutput.getListData();

        assert.equal(items.length, 1);
        assert.equal(items[0].typeName, 'ExampleController');
    });
});
