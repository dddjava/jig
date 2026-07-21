const test = require('node:test');
const assert = require('node:assert/strict');
const {JSDOM} = require('jsdom');

const ASSET_MODULES = ['jig-util.js', 'jig-data.js', 'jig-i18n.js', 'jig-dom.js', 'jig-bootstrap.js'];

function modulePath(name) {
    return require.resolve(`../../main/resources/templates/assets/${name}`);
}

function reloadJigModules() {
    ASSET_MODULES.forEach(m => delete require.cache[modulePath(m)]);
    delete require.cache[modulePath('index.js')];
    delete globalThis.Jig;
    ASSET_MODULES.forEach(m => require(modulePath(m)));

    globalThis.Jig.mermaid = {
        createPackageLevelDiagram: () => null,
        render: {renderWithControls: () => {}},
        nav: {domainTypeUrl: () => null},
    };

    return require(modulePath('index.js'));
}

function setupDom(html = '<!DOCTYPE html><html><body></body></html>') {
    const dom = new JSDOM(html);
    global.window = dom.window;
    global.document = dom.window.document;
    global.location = dom.window.location;
    // jig-i18n.js の言語切り替えイベントを document.dispatchEvent に渡せるようにする
    global.CustomEvent = dom.window.CustomEvent;
    return dom;
}

test.describe('index.js', () => {
    let IndexApp;

    test.beforeEach(() => {
        setupDom();
        IndexApp = reloadJigModules();
    });

    test.describe('updateRelativeTime', () => {
        test('要素が存在しない場合は何もしない', () => {
            assert.doesNotThrow(() => IndexApp.updateRelativeTime());
        });

        test('data-jig-timestamp が無ければ書き換えない', () => {
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.textContent = '2026-01-01T00:00:00Z';
            document.body.appendChild(el);

            IndexApp.updateRelativeTime();

            assert.equal(el.textContent, '2026-01-01T00:00:00Z');
        });

        test('不正な日時文字列は書き換えない', () => {
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.setAttribute('data-jig-timestamp', 'not-a-date');
            el.textContent = 'invalid';
            document.body.appendChild(el);

            IndexApp.updateRelativeTime();

            assert.equal(el.textContent, 'invalid');
        });

        test('数日前の日時は「n日前」と表示される', () => {
            const timestamp = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000 - 1000);
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.setAttribute('data-jig-timestamp', timestamp.toISOString());
            el.textContent = timestamp.toISOString();
            document.body.appendChild(el);

            IndexApp.updateRelativeTime();

            assert.match(el.textContent, /\(3日前\)$/);
        });

        test('数時間前の日時は「n時間前」と表示される', () => {
            const timestamp = new Date(Date.now() - 2 * 60 * 60 * 1000 - 1000);
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.setAttribute('data-jig-timestamp', timestamp.toISOString());
            el.textContent = timestamp.toISOString();
            document.body.appendChild(el);

            IndexApp.updateRelativeTime();

            assert.match(el.textContent, /\(2時間前\)$/);
        });

        test('数分前の日時は「n分前」と表示される', () => {
            const timestamp = new Date(Date.now() - 5 * 60 * 1000 - 1000);
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.setAttribute('data-jig-timestamp', timestamp.toISOString());
            el.textContent = timestamp.toISOString();
            document.body.appendChild(el);

            IndexApp.updateRelativeTime();

            assert.match(el.textContent, /\(5分前\)$/);
        });

        test('1分未満は「たった今」と表示される', () => {
            const timestamp = new Date(Date.now() - 500);
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.setAttribute('data-jig-timestamp', timestamp.toISOString());
            el.textContent = timestamp.toISOString();
            document.body.appendChild(el);

            IndexApp.updateRelativeTime();

            assert.match(el.textContent, /\(たった今\)$/);
        });

        test('再実行しても相対時刻表記が重複しない', () => {
            const timestamp = new Date(Date.now() - 5 * 60 * 1000 - 1000);
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.setAttribute('data-jig-timestamp', timestamp.toISOString());
            el.textContent = timestamp.toISOString();
            document.body.appendChild(el);

            IndexApp.updateRelativeTime();
            IndexApp.updateRelativeTime();

            const occurrences = el.textContent.split('分前').length - 1;
            assert.equal(occurrences, 1);
        });

        test('英語に切り替えると相対時刻も英語で表示される', () => {
            const timestamp = new Date(Date.now() - 3 * 24 * 60 * 60 * 1000 - 1000);
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.setAttribute('data-jig-timestamp', timestamp.toISOString());
            el.textContent = timestamp.toISOString();
            document.body.appendChild(el);

            globalThis.Jig.i18n.setLanguage('en');
            IndexApp.updateRelativeTime();

            assert.match(el.textContent, /\(3 days ago\)$/);
        });

        test('言語切り替えイベントで相対時刻を描き直す', () => {
            const timestamp = new Date(Date.now() - 500);
            const el = document.createElement('span');
            el.id = 'jig-timestamp';
            el.setAttribute('data-jig-timestamp', timestamp.toISOString());
            el.textContent = timestamp.toISOString();
            document.body.appendChild(el);

            IndexApp.init();
            assert.match(el.textContent, /\(たった今\)$/);

            globalThis.Jig.i18n.setLanguage('en');

            assert.match(el.textContent, /\(just now\)$/);
        });
    });

    test.describe('getPackageData', () => {
        test('Jig.data.package.get() の値を返す', () => {
            globalThis.packageData = {packages: [], relations: [], domainPackageRoots: []};
            assert.deepEqual(IndexApp.getPackageData(), globalThis.packageData);
            delete globalThis.packageData;
        });
    });

    test.describe('init', () => {
        test('summary/document-links/package-diagram が無くても例外を投げない', () => {
            assert.doesNotThrow(() => IndexApp.init());
        });

        test('document-links の ul にリンク一覧を描画する', () => {
            const container = document.createElement('div');
            container.id = 'document-links';
            const ul = document.createElement('ul');
            container.appendChild(ul);
            document.body.appendChild(container);

            globalThis.navigationData = {
                links: [{label: 'ラベル1', href: 'a.html'}, {label: 'ラベル2', href: 'b.html'}]
            };

            IndexApp.init();

            assert.equal(ul.querySelectorAll('li').length, 2);
            assert.equal(ul.querySelectorAll('a')[0].getAttribute('href'), 'a.html');

            delete globalThis.navigationData;
        });

        test('git 情報から Source 行を組み立てる', () => {
            const sourceEl = document.createElement('div');
            sourceEl.id = 'jig-source';
            document.body.appendChild(sourceEl);

            globalThis.summaryData = {
                git: {
                    remote: {baseUrl: 'https://example.com/repo', displayName: 'example/repo', commitUrl: 'https://example.com/repo/commit/abc'},
                    shortHash: 'abc1234',
                }
            };

            IndexApp.init();

            const link = sourceEl.querySelector('a');
            assert.ok(link);
            assert.equal(link.getAttribute('href'), 'https://example.com/repo');
            assert.match(sourceEl.textContent, /abc1234/);

            delete globalThis.summaryData;
        });

        test('package-diagram とデータが揃っているとパッケージ図を描画する', () => {
            const container = document.createElement('div');
            container.id = 'package-diagram';
            document.body.appendChild(container);

            globalThis.packageData = {
                packages: [{fqn: 'com.example.domain'}],
                relations: [],
                domainPackageRoots: ['com.example.domain'],
            };

            let calls = 0;
            globalThis.Jig.mermaid.createPackageLevelDiagram = () => {
                calls++;
                return true;
            };

            IndexApp.init();

            assert.ok(calls > 0);
            assert.equal(container.querySelectorAll('.mermaid-diagram').length, 2);

            delete globalThis.packageData;
        });
    });
});
