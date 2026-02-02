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
                usingFieldTypes: ['ExampleRepository', 'AnotherType'],
                cyclomaticComplexity: 2,
                path: 'GET /example',
            },
        ];

        const csv = listOutput.buildControllerCsv(items);

        assert.equal(
            csv,
            '"パッケージ名","クラス名","メソッドシグネチャ","メソッド戻り値の型","クラス別名","使用しているフィールドの型","循環的複雑度","パス"\r\n' +
                '"com.example","ExampleController","getExample()","Example","例","ExampleRepository\nAnotherType","2","GET /example"'
        );
    });

    test('SERVICEのCSVにヘッダーと行を出力する', () => {
        const items = [
            {
                packageName: 'com.example',
                typeName: 'ExampleService',
                methodSignature: 'handle()',
                returnType: 'Example',
                eventHandler: true,
                typeLabel: '例',
                methodLabel: '取得',
                returnTypeLabel: '例',
                parameterTypeLabels: ['Param'],
                usingFieldTypes: ['ExampleRepository'],
                cyclomaticComplexity: 3,
                usingServiceMethods: ['other():Example'],
                usingRepositoryMethods: ['find()'],
                useNull: false,
                useStream: true,
            },
        ];

        const csv = listOutput.buildServiceCsv(items);

        assert.equal(
            csv,
            '"パッケージ名","クラス名","メソッドシグネチャ","メソッド戻り値の型","イベントハンドラ","クラス別名","メソッド別名","メソッド戻り値の型の別名","メソッド引数の型の別名","使用しているフィールドの型","循環的複雑度","使用しているサービスのメソッド","使用しているリポジトリのメソッド","null使用","stream使用"\r\n' +
                '"com.example","ExampleService","handle()","Example","◯","例","取得","例","Param","ExampleRepository","3","other():Example","find()","","◯"'
        );
    });

    test('REPOSITORYのCSVにヘッダーと行を出力する', () => {
        const items = [
            {
                packageName: 'com.example',
                typeName: 'ExampleRepository',
                methodSignature: 'find()',
                returnType: 'Example',
                typeLabel: '例',
                returnTypeLabel: '例',
                parameterTypeLabels: [],
                cyclomaticComplexity: 1,
                insertTables: ['EXAMPLE'],
                selectTables: ['EXAMPLE'],
                updateTables: [],
                deleteTables: [],
                callerTypeCount: 1,
                callerMethodCount: 2,
            },
        ];

        const csv = listOutput.buildRepositoryCsv(items);

        assert.equal(
            csv,
            '"パッケージ名","クラス名","メソッドシグネチャ","メソッド戻り値の型","クラス別名","メソッド戻り値の型の別名","メソッド引数の型の別名","循環的複雑度","INSERT","SELECT","UPDATE","DELETE","関連元クラス数","関連元メソッド数"\r\n' +
                '"com.example","ExampleRepository","find()","Example","例","例","","1","EXAMPLE","EXAMPLE","","","1","2"'
        );
    });
});

test.describe('list-output.js データ読み込み', () => {
    test('list-dataから一覧を取得する', () => {
        const doc = setupDocument();
        const dataElement = new Element('script');
        dataElement.textContent = JSON.stringify({
            applications: {
                controllers: [{typeName: 'ExampleController'}],
            },
            businessRules: {
                packages: [{packageName: 'com.example'}],
            },
        });
        doc.elementsById.set('list-data', dataElement);

        const data = listOutput.getListData();

        assert.equal(data.applications.controllers.length, 1);
        assert.equal(data.applications.controllers[0].typeName, 'ExampleController');
        assert.equal(data.businessRules.packages.length, 1);
    });
});

test.describe('list-output.js 表示用整形', () => {
    test('使用フィールド型を改行で連結する', () => {
        const formatted = listOutput.formatFieldTypes(['A', 'B']);

        assert.equal(formatted, 'A\nB');
    });
});
