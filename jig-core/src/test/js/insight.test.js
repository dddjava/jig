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

const PAGE_HTML = `<!DOCTYPE html><html><body><main>
<div id="insight-controls">
  <div id="insight-tabs"></div>
  <div id="insight-scope"></div>
  <input type="search" id="insight-search">
  <input type="checkbox" id="insight-hide-initializers">
  <p id="insight-status"></p>
</div>
<table id="package-insight-list" hidden><thead></thead><tbody></tbody></table>
<table id="type-insight-list" hidden><thead></thead><tbody></tbody></table>
<table id="method-insight-list" hidden><thead></thead><tbody></tbody></table>
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

function visibleFqns(tableId) {
    return [...document.querySelectorAll(`#${tableId} tbody tr:not([hidden])`)].map(row => row.dataset.fqn);
}

function clickTab(viewId) {
    document.querySelector(`#insight-tabs button[data-view-id="${viewId}"]`).click();
}

function tabCounts() {
    return Object.fromEntries([...document.querySelectorAll('#insight-tabs button')]
        .map(tab => [tab.dataset.viewId, tab.querySelector('.insight-tab__count').textContent]));
}

async function search(text) {
    const input = document.getElementById('insight-search');
    input.value = text;
    input.dispatchEvent(new Event('input'));
    // 入力は間引いてから反映される
    await new Promise(resolve => setTimeout(resolve, 200));
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

        test('insightDataがない場合は操作部品を残さない', () => {
            InsightApp.init();

            assert.equal(document.getElementById('insight-controls'), null);
        });

        test('0件の表はタブごと取り除き、最初のタブを表示する', () => {
            globalThis.insightData = {packages: [], types: [type('app.Type')], methods: [method('app.Type#run()')]};

            InsightApp.init();

            assert.equal(document.getElementById('package-insight-list'), null);
            assert.deepEqual(tabCounts(), {type: '1', method: '1'});
            assert.equal(document.getElementById('type-insight-list').hidden, false);
            assert.equal(document.getElementById('method-insight-list').hidden, true);
        });

        test('すべて0件なら空のドキュメントとして表示する', () => {
            globalThis.insightData = {packages: [], types: [], methods: []};

            InsightApp.init();

            assert.ok(document.querySelector('main .jig-empty'));
            assert.equal(document.getElementById('insight-controls'), null);
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
            const paramsCell = document.querySelectorAll('#method-insight-list tbody td')[headers.indexOf('numberOfParameters')];
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

    test.describe('タブ', () => {
        test('タブを押すとその表だけを表示する', () => {
            globalThis.insightData = {packages: [pkg('app')], types: [type('app.Type')], methods: [method('app.Type#run()')]};
            InsightApp.init();

            clickTab('method');

            assert.equal(document.getElementById('package-insight-list').hidden, true);
            assert.equal(document.getElementById('method-insight-list').hidden, false);
            assert.equal(document.querySelector('#insight-tabs [aria-selected="true"]').dataset.viewId, 'method');
        });
    });

    test.describe('範囲', () => {
        test.beforeEach(() => {
            globalThis.insightData = {
                packages: [pkg('app.a'), pkg('app.a.sub'), pkg('app.b')],
                types: [type('app.a.A'), type('app.a.A$Inner'), type('app.a.sub.S'), type('app.b.B')],
                methods: [method('app.a.A#x()'), method('app.a.A$Inner#y()'), method('app.a.sub.S#z()'), method('app.b.B#w()')],
            };
            InsightApp.init();
        });

        test('パッケージ名を押すと配下（サブパッケージ含む）のクラス表へ移る', () => {
            document.querySelector('#package-insight-list tr[data-fqn="app.a"] button.insight-link').click();

            assert.equal(document.getElementById('type-insight-list').hidden, false);
            assert.deepEqual(visibleFqns('type-insight-list'), ['app.a.A', 'app.a.A$Inner', 'app.a.sub.S']);
            assert.deepEqual(tabCounts(), {package: '2', type: '3', method: '3'});
        });

        test('クラス名を押すとそのクラス（ネストしたクラス含む）のメソッド表へ移る', () => {
            clickTab('type');
            document.querySelector('#type-insight-list tr[data-fqn="app.a.A"] button.insight-name__type').click();

            assert.equal(document.getElementById('method-insight-list').hidden, false);
            assert.deepEqual(visibleFqns('method-insight-list'), ['app.a.A#x()', 'app.a.A$Inner#y()']);
            clickTab('package');
            assert.deepEqual(visibleFqns('package-insight-list'), ['app.a'], 'パッケージ表はクラスの属するパッケージだけ');
        });

        test('メソッド表のパッケージ名を押すと表を移らず範囲だけ変わる', () => {
            clickTab('method');
            document.querySelector('#method-insight-list tr[data-fqn="app.b.B#w()"] button.insight-name__package').click();

            assert.equal(document.getElementById('method-insight-list').hidden, false);
            assert.deepEqual(visibleFqns('method-insight-list'), ['app.b.B#w()']);
        });

        test('パンくずに範囲を表示し、「すべて」で解除する', () => {
            clickTab('type');
            document.querySelector('#type-insight-list tr[data-fqn="app.a.A"] button.insight-name__type').click();

            const scopeText = document.getElementById('insight-scope').textContent;
            assert.ok(scopeText.includes('app.a') && scopeText.includes('A'), scopeText);

            document.querySelector('#insight-scope button[data-scope-clear]').click();

            assert.equal(visibleFqns('method-insight-list').length, 4);
        });

        test('件数は「表示 / 範囲内」で示す', () => {
            document.querySelector('#package-insight-list tr[data-fqn="app.b"] button.insight-link').click();

            assert.ok(document.getElementById('insight-status').textContent.endsWith('1 / 1'));
        });
    });

    test.describe('検索・除外', () => {
        test('空白区切りの語をすべて含む行だけを表示する', async () => {
            globalThis.insightData = {
                packages: [], types: [],
                methods: [method('app.order.Order#ship()'), method('app.order.Order#cancel()'), method('app.user.User#ship()')],
            };
            InsightApp.init();

            await search('ORDER ship');

            assert.deepEqual(visibleFqns('method-insight-list'), ['app.order.Order#ship()']);
            assert.ok(document.getElementById('insight-status').textContent.endsWith('1 / 3'));
        });

        test('用語集の和名でも検索できる', async () => {
            globalThis.glossaryData = {terms: {'app.Order#ship()': {title: '出荷する'}}};
            globalThis.insightData = {packages: [], types: [], methods: [method('app.Order#ship()'), method('app.Order#cancel()')]};
            InsightApp.init();

            await search('出荷');

            assert.deepEqual(visibleFqns('method-insight-list'), ['app.Order#ship()']);
        });

        test('コンストラクタ・static初期化は既定で表示し、チェックで除外する', () => {
            globalThis.insightData = {
                packages: [], types: [],
                methods: [method('app.Type#<init>()'), method('app.Type#<clinit>()'), method('app.Type#run()')],
            };
            InsightApp.init();
            assert.equal(visibleFqns('method-insight-list').length, 3);

            const checkbox = document.getElementById('insight-hide-initializers');
            checkbox.checked = true;
            checkbox.dispatchEvent(new Event('change'));

            assert.deepEqual(visibleFqns('method-insight-list'), ['app.Type#run()']);
        });

        test('絞り込んだ状態は並べ替えても保たれる', async () => {
            globalThis.insightData = {
                packages: [], types: [],
                methods: [method('app.A#x()', {size: 1}), method('app.B#y()', {size: 9}), method('app.A#z()', {size: 5})],
            };
            InsightApp.init();

            await search('app.A#');
            clickHeader('method-insight-list', 'size');

            assert.deepEqual(visibleFqns('method-insight-list'), ['app.A#z()', 'app.A#x()']);
        });
    });
});
