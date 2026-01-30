const test = require('node:test');
const assert = require('node:assert/strict');

const insight = require('../../main/resources/templates/assets/insight.js');

class Element {
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this.textContent = '';
        this.className = '';
        this.dataset = {};
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
        this.selectors = new Map();
    }

    createElement(tagName) {
        return new Element(tagName);
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }

    querySelector(selector) {
        return this.selectors.get(selector) || null;
    }
}

function setupDocument() {
    const doc = new DocumentStub();
    global.document = doc;
    return doc;
}

function buildInsightTables(doc) {
    const packageTbody = new Element('tbody');
    const typeTbody = new Element('tbody');
    const methodTbody = new Element('tbody');

    doc.selectors.set('#package-insight-list tbody', packageTbody);
    doc.selectors.set('#type-insight-list tbody', typeTbody);
    doc.selectors.set('#method-insight-list tbody', methodTbody);

    return {packageTbody, typeTbody, methodTbody};
}

test.describe('insight.js JSON読み込み', () => {
    test('データ要素がない場合はnull', () => {
        setupDocument();

        assert.equal(insight.parseInsightData(), null);
    });

    test('insight-dataからJSONを取得する', () => {
        const doc = setupDocument();
        const dataElement = new Element('script');
        dataElement.textContent = JSON.stringify({packages: [{fqn: 'app'}]});
        doc.elementsById.set('insight-data', dataElement);

        const result = insight.parseInsightData();

        assert.equal(result.packages[0].fqn, 'app');
    });

    test('不正なJSONの場合はnullを返す', () => {
        const doc = setupDocument();
        const dataElement = new Element('script');
        dataElement.textContent = '{bad json';
        doc.elementsById.set('insight-data', dataElement);

        let logged = false;
        const originalError = console.error;
        console.error = () => {
            logged = true;
        };

        try {
            assert.equal(insight.parseInsightData(), null);
            assert.equal(logged, true);
        } finally {
            console.error = originalError;
        }
    });
});

test.describe('insight.js 表示部品', () => {
    test('件数表示は対象要素に文字列で書き込む', () => {
        const doc = setupDocument();
        const target = new Element('span');
        doc.elementsById.set('package-count', target);

        insight.setInsightCount('package-count', 12);

        assert.equal(target.textContent, '12');
    });

    test('テーブルセルはテキストとクラス名を設定する', () => {
        setupDocument();
        const cell = insight.createCell('Hello', 'number');

        assert.equal(cell.tagName, 'td');
        assert.equal(cell.textContent, 'Hello');
        assert.equal(cell.className, 'number');
    });

    test('ズームセルはアイコンを追加する', () => {
        setupDocument();
        const cell = insight.createZoomCell();

        assert.equal(cell.tagName, 'td');
        assert.equal(cell.children.length, 1);
        assert.equal(cell.children[0].tagName, 'i');
        assert.equal(cell.children[0].className, 'zoom');
    });
});

test.describe('insight.js テーブル描画', () => {
    test('パッケージ一覧を描画する', () => {
        const doc = setupDocument();
        const {packageTbody} = buildInsightTables(doc);

        insight.renderPackageInsights([
            {
                fqn: 'app',
                label: 'App',
                numberOfTypes: 3,
                numberOfMethods: 5,
                numberOfUsingTypes: 2,
                cyclomaticComplexity: 9,
                size: 11,
            },
        ]);

        assert.equal(packageTbody.children.length, 1);
        const row = packageTbody.children[0];
        assert.equal(row.dataset.fqn, 'app');
        assert.equal(row.children[1].textContent, 'app');
        assert.equal(row.children[2].textContent, 'App');
    });

    test('型一覧を描画する', () => {
        const doc = setupDocument();
        const {typeTbody} = buildInsightTables(doc);

        insight.renderTypeInsights([
            {
                fqn: 'app.Type',
                packageFqn: 'app',
                label: 'Type',
                numberOfMethods: 1,
                numberOfUsingTypes: 4,
                cyclomaticComplexity: 2,
                size: 5,
            },
        ]);

        assert.equal(typeTbody.children.length, 1);
        const row = typeTbody.children[0];
        assert.equal(row.dataset.fqn, 'app.Type');
        assert.equal(row.dataset.packageFqn, 'app');
    });

    test('メソッド一覧を描画する', () => {
        const doc = setupDocument();
        const {methodTbody} = buildInsightTables(doc);

        insight.renderMethodInsights([
            {
                fqn: 'app.Type#method',
                packageFqn: 'app',
                typeFqn: 'app.Type',
                label: 'method',
                cyclomaticComplexity: 1,
                numberOfUsingTypes: 1,
                numberOfUsingMethods: 2,
                numberOfUsingFields: 0,
                size: 3,
            },
        ]);

        assert.equal(methodTbody.children.length, 1);
        const row = methodTbody.children[0];
        assert.equal(row.dataset.fqn, 'app.Type#method');
        assert.equal(row.dataset.packageFqn, 'app');
        assert.equal(row.dataset.typeFqn, 'app.Type');
    });
});
