const test = require('node:test');
const assert = require('node:assert/strict');
const {Element, DocumentStub} = require('./dom-stub.js');

function setupDocument() {
    const doc = new DocumentStub();
    global.document = doc;
    return doc;
}

// document グローバルを設定してから jig-dom.js を require する
setupDocument();
require('../../main/resources/templates/assets/jig-glossary.js');
require('../../main/resources/templates/assets/jig-dom.js');

const insight = require('../../main/resources/templates/assets/insight.js');

function buildInsightTables(doc) {
    const packageTbody = doc.createElement('tbody');
    const typeTbody = doc.createElement('tbody');
    const methodTbody = doc.createElement('tbody');

    doc.selectors.set('#package-insight-list tbody', packageTbody);
    doc.selectors.set('#type-insight-list tbody', typeTbody);
    doc.selectors.set('#method-insight-list tbody', methodTbody);

    return {packageTbody, typeTbody, methodTbody};
}

test.describe('insight.js', () => {

    // データ読み込みの処理をテスト
    test.describe('JSON読み込み', () => {

        test('データがない場合はnull', () => {
            setupDocument();
            global.insightData = undefined;

            assert.equal(insight.parseInsightData(), null);
        });

        test('insightDataから取得する', () => {
            setupDocument();
            global.insightData = {packages: [{fqn: 'app'}]};

            const result = insight.parseInsightData();

            assert.equal(result.packages[0].fqn, 'app');
        });

    });


    // UI要素の構築をテスト（表示フォーマット、セルのレンダリングなど）
    test.describe('表示部品', () => {

        test('件数表示は対象要素に文字列で書き込む', () => {

            const doc = setupDocument();

            const target = new Element('span');

            doc.elementsById.set('package-count', target);


            insight.setInsightCount('package-count', 12);


            assert.equal(target.textContent, '12');

        });


        test('テーブルセルはテキストとクラス名を設定する', () => {

            setupDocument();

            const cell = Jig.dom.createCell('Hello', 'number');


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


    // テーブルへのデータレンダリングをテスト（行・セルの追加など）
    test.describe('テーブル描画', () => {

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

                    numberOfUsedByTypes: 0,

                    instability: 1.0,

                    lcom: 0,

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

                    numberOfUsingOwnFields: 0,

                    numberOfUsingOwnMethods: 0,

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

    // 不正な入力や不足している要素でも例外が発生しないことを確認
    test.describe('エッジケース - 必要な要素がない場合', () => {

        test('テーブルの tbody が存在しない場合、各 render メソッドは安全に実行される（例外が発生しない）', () => {

            const doc = setupDocument();

            // 各テーブルの tbody を null に設定
            doc.selectors.set('#package-insight-list tbody', null);
            doc.selectors.set('#type-insight-list tbody', null);
            doc.selectors.set('#method-insight-list tbody', null);

            // 例外が発生しないことを確認
            const testData = {
                packages: [{
                    fqn: 'app',
                    label: 'App',
                    numberOfTypes: 1,
                    numberOfMethods: 1,
                    numberOfUsingTypes: 0,
                    cyclomaticComplexity: 1,
                    size: 1
                }],
                types: [{
                    fqn: 'app.Type',
                    packageFqn: 'app',
                    label: 'Type',
                    numberOfMethods: 1,
                    numberOfUsingTypes: 0,
                    numberOfUsedByTypes: 0,
                    instability: 0,
                    lcom: 0,
                    cyclomaticComplexity: 1,
                    size: 1
                }],
                methods: [{
                    fqn: 'app.Type#method',
                    packageFqn: 'app',
                    typeFqn: 'app.Type',
                    label: 'method',
                    cyclomaticComplexity: 1,
                    numberOfUsingTypes: 0,
                    numberOfUsingMethods: 0,
                    numberOfUsingFields: 0,
                    numberOfUsingOwnFields: 0,
                    numberOfUsingOwnMethods: 0,
                    size: 1
                }]
            };

            // 全てのrenderメソッドは安全に実行される
            assert.doesNotThrow(() => insight.renderPackageInsights(testData.packages));
            assert.doesNotThrow(() => insight.renderTypeInsights(testData.types));
            assert.doesNotThrow(() => insight.renderMethodInsights(testData.methods));
        });

    });

});
