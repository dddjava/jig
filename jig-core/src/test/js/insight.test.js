const test = require('node:test');
const assert = require('node:assert/strict');
const {setupDom, teardownDom} = require('./jsdom-env');

const ASSET_MODULES = ['jig-util.js', 'jig-data.js', 'jig-glossary.js', 'jig-i18n.js', 'jig-dom.js', 'jig-bootstrap.js'];

function modulePath(name) {
    return require.resolve(`../../main/resources/templates/assets/${name}`);
}

function reloadInsightApp() {
    [...ASSET_MODULES, 'insight.js'].forEach(m => delete require.cache[modulePath(m)]);
    delete globalThis.Jig;
    ASSET_MODULES.forEach(m => require(modulePath(m)));
    return require(modulePath('insight.js'));
}

const PAGE_HTML = `<!DOCTYPE html><html><body class="insight"><main>
<button id="cancel-zoom" class="hidden"></button>
<nav id="insight-section-nav">
  <a href="#section-package"></a><a href="#section-type"></a><a href="#section-method"></a>
</nav>
<h2 id="section-package"><span id="package-count">0</span></h2>
<table id="package-insight-list"><thead></thead><tbody></tbody></table>
<h2 id="section-type"><span id="type-count">0</span></h2>
<table id="type-insight-list"><thead></thead><tbody></tbody></table>
<h2 id="section-method"><span id="method-count">0</span></h2>
<table id="method-insight-list"><thead></thead><tbody></tbody></table>
</main></body></html>`;

function method(fqn, metrics = {}) {
    const typeFqn = fqn.slice(0, fqn.indexOf('#'));
    return {
        fqn, typeFqn, packageFqn: typeFqn.slice(0, typeFqn.lastIndexOf('.')),
        cyclomaticComplexity: 1, numberOfUsingTypes: 0, numberOfUsingMethods: 0, numberOfUsingFields: 0,
        numberOfUsingOwnFields: 0, numberOfUsingOwnMethods: 0, size: 1,
        ...metrics,
    };
}

function type(fqn, metrics = {}) {
    return {
        fqn, packageFqn: fqn.slice(0, fqn.lastIndexOf('.')),
        numberOfMethods: 1, numberOfUsingTypes: 0, numberOfUsedByTypes: 0, instability: 0, lcom: 0,
        cyclomaticComplexity: 1, size: 1,
        ...metrics,
    };
}

function pkg(fqn, metrics = {}) {
    return {fqn, numberOfTypes: 1, numberOfMethods: 1, numberOfUsingTypes: 0, cyclomaticComplexity: 1, size: 1, ...metrics};
}

function rowFqns(tableId) {
    return [...document.querySelectorAll(`#${tableId} tbody tr`)].map(row => row.dataset.fqn);
}

function clickHeader(tableId, sortKey) {
    document.querySelector(`#${tableId} button.insight-sort[data-sort-key="${sortKey}"]`).click();
}

test.describe('insight.js', () => {
    let InsightApp;

    test.beforeEach(() => {
        setupDom(PAGE_HTML);
        InsightApp = reloadInsightApp();
        globalThis.glossaryData = {terms: {}};
    });

    test.afterEach(() => {
        delete globalThis.insightData;
        delete globalThis.glossaryData;
        teardownDom();
    });

    test.describe('データ読み込み', () => {
        test('insightDataがない場合はデータ読み込みエラーを表示する', () => {
            InsightApp.init();

            const error = document.querySelector('main .jig-data-error');
            assert.ok(error, 'jig-data-error が表示されること');
            assert.ok(error.textContent.includes('insight-data.js'));
        });

        test('0件のセクションは見出し・テーブル・ナビゲーションリンクごと取り除く', () => {
            globalThis.insightData = {packages: [], types: [], methods: [method('app.Type#run()')]};

            InsightApp.init();

            assert.equal(document.getElementById('section-package'), null);
            assert.equal(document.getElementById('package-insight-list'), null);
            assert.ok(document.getElementById('method-insight-list'));
            assert.equal(document.querySelectorAll('#insight-section-nav a').length, 1);
            assert.equal(document.getElementById('method-count').textContent, '1');
        });
    });

    test.describe('parseMethod', () => {
        test('引数を単純名にし、ネストしたクラス名はパッケージからの相対名にする', () => {
            const parsed = InsightApp.parseMethod(method('app.domain.Outer$Inner#find(java.lang.String,app.domain.Id)'));

            assert.deepEqual(parsed, {typeName: 'Outer$Inner', methodName: 'find', parameters: ['String', 'Id']});
        });

        test('引数なしは空配列', () => {
            assert.deepEqual(InsightApp.parseMethod(method('app.Type#run()')).parameters, []);
        });

        test('デフォルトパッケージのクラスはFQNをそのままクラス名にする', () => {
            const parsed = InsightApp.parseMethod({fqn: 'Type#run()', typeFqn: 'Type', packageFqn: '(default)'});

            assert.equal(parsed.typeName, 'Type');
        });
    });

    test.describe('名前セル', () => {
        test('メソッドはクラス名#メソッド名(引数の単純名)とパッケージを1セルに表示する', () => {
            globalThis.insightData = {packages: [], types: [], methods: [method('app.domain.Order#ship(java.lang.String,app.domain.Address)')]};

            InsightApp.init();

            const cell = document.querySelector('#method-insight-list tbody td.insight-name');
            assert.equal(cell.querySelector('.insight-name__main').textContent, 'Order#ship(String, Address)');
            assert.equal(cell.querySelector('.insight-name__package').textContent, 'app.domain');
            assert.equal(cell.title, 'app.domain.Order#ship(java.lang.String,app.domain.Address)', '完全修飾名はtitleで確認できること');
        });

        test('用語集に和名があれば併記し、なければ出さない', () => {
            globalThis.glossaryData = {terms: {'app.domain.Order#ship(java.lang.String)': {title: '出荷する'}}};
            globalThis.insightData = {
                packages: [], types: [],
                methods: [method('app.domain.Order#ship(java.lang.String)'), method('app.domain.Order#cancel()')],
            };

            InsightApp.init();

            const terms = [...document.querySelectorAll('#method-insight-list tbody tr')]
                .map(row => row.querySelector('.insight-name__term')?.textContent ?? null);
            assert.deepEqual(terms, ['出荷する', null]);
        });

        test('コンストラクタとstatic初期化はバッジで表示する', () => {
            globalThis.insightData = {packages: [], types: [], methods: [method('app.Type#<init>()'), method('app.Type#<clinit>()')]};

            InsightApp.init();

            const badges = [...document.querySelectorAll('#method-insight-list .insight-badge')].map(b => b.textContent);
            assert.deepEqual(badges, ['コンストラクタ', 'static初期化']);
        });

        test('クラスはパッケージからの相対名とパッケージを表示する', () => {
            globalThis.insightData = {packages: [], types: [type('app.domain.Outer$Inner')], methods: []};

            InsightApp.init();

            const cell = document.querySelector('#type-insight-list tbody td.insight-name');
            assert.equal(cell.querySelector('.insight-name__type').textContent, 'Outer$Inner');
            assert.equal(cell.querySelector('.insight-name__package').textContent, 'app.domain');
        });
    });

    test.describe('数値列', () => {
        test('メソッドの引数の数を列として表示する', () => {
            globalThis.insightData = {packages: [], types: [], methods: [method('app.Type#run(int,int,int)')]};

            InsightApp.init();

            const headers = [...document.querySelectorAll('#method-insight-list thead button.insight-sort')].map(b => b.dataset.sortKey);
            // 先頭はズームのセルで見出しボタンを持たないため1つずれる
            const paramsCell = document.querySelectorAll('#method-insight-list tbody td')[headers.indexOf('numberOfParameters') + 1];
            assert.equal(paramsCell.textContent, '3');
        });

        test('比率の列は小数2桁で表示する', () => {
            globalThis.insightData = {packages: [], types: [type('app.Type', {instability: 0.5})], methods: []};

            InsightApp.init();

            const cells = [...document.querySelectorAll('#type-insight-list tbody td.number')].map(td => td.textContent);
            assert.ok(cells.includes('0.50'));
        });

        test('バーの満幅は上位1%の値にし、外れ値1件に引きずられない', () => {
            const items = Array.from({length: 200}, (_, i) => ({v: i < 199 ? 10 : 1000}));

            const scales = InsightApp.computeBarScales([{key: 'v'}], items);

            assert.equal(scales.v, 10);
        });

        test('比率の列は1を満幅にする', () => {
            const scales = InsightApp.computeBarScales([{key: 'lcom', ratio: true}], [{lcom: 0.2}]);

            assert.equal(scales.lcom, 1);
        });
    });

    test.describe('ソート', () => {
        test.beforeEach(() => {
            globalThis.insightData = {
                packages: [], types: [],
                methods: [
                    method('app.A#a()', {cyclomaticComplexity: 2}),
                    method('app.B#b()', {cyclomaticComplexity: 10}),
                    method('app.C#c()', {cyclomaticComplexity: 5}),
                ],
            };
            InsightApp.init();
        });

        test('数値列は初回クリックで大きい順、再クリックで小さい順', () => {
            clickHeader('method-insight-list', 'cyclomaticComplexity');
            assert.deepEqual(rowFqns('method-insight-list'), ['app.B#b()', 'app.C#c()', 'app.A#a()']);

            clickHeader('method-insight-list', 'cyclomaticComplexity');
            assert.deepEqual(rowFqns('method-insight-list'), ['app.A#a()', 'app.C#c()', 'app.B#b()']);
        });

        test('名前列は初回クリックで昇順', () => {
            clickHeader('method-insight-list', 'cyclomaticComplexity');
            clickHeader('method-insight-list', 'fqn');

            assert.deepEqual(rowFqns('method-insight-list'), ['app.A#a()', 'app.B#b()', 'app.C#c()']);
        });

        test('並べ替え中の列を aria-sort で示す', () => {
            clickHeader('method-insight-list', 'cyclomaticComplexity');

            const sorted = document.querySelectorAll('#method-insight-list thead th[aria-sort]');
            assert.equal(sorted.length, 1);
            assert.equal(sorted[0].getAttribute('aria-sort'), 'descending');
        });
    });

    test.describe('ズーム', () => {
        test('ズームした状態は並べ替えても保たれる', () => {
            globalThis.insightData = {
                packages: [pkg('app.a'), pkg('app.b')],
                types: [type('app.a.A'), type('app.b.B')],
                methods: [method('app.a.A#x()', {size: 1}), method('app.b.B#y()', {size: 9})],
            };
            InsightApp.init();

            document.querySelector('#package-insight-list tbody tr[data-fqn="app.a"] i.zoom').click();
            clickHeader('method-insight-list', 'size');

            const visible = [...document.querySelectorAll('#method-insight-list tbody tr:not(.hidden-by-zoom)')].map(r => r.dataset.fqn);
            assert.deepEqual(visible, ['app.a.A#x()']);
        });
    });
});
