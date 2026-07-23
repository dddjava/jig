const test = require('node:test');
const assert = require('node:assert/strict');
const {setupDom, teardownDom} = require('./jsdom-env');

const ASSET_MODULES = ['jig-util.js', 'jig-data.js', 'jig-dom.js', 'jig-bootstrap.js'];

function modulePath(name) {
    return require.resolve(`../../main/resources/templates/assets/${name}`);
}

function reloadJigModules() {
    ASSET_MODULES.forEach(m => delete require.cache[modulePath(m)]);
    delete require.cache[modulePath('library-dependency.js')];
    delete globalThis.Jig;
    ASSET_MODULES.forEach(m => require(modulePath(m)));

    const clickHandlers = new Map();
    globalThis.Jig.mermaid = {
        render: {renderWithControls: () => {}},
        registerClickHandler: (name, handler) => clickHandlers.set(name, handler),
        getClickHandler: name => clickHandlers.get(name),
    };

    return require(modulePath('library-dependency.js'));
}

test.describe('library-dependency.js', () => {
    let App;

    test.beforeEach(() => {
        setupDom();
        App = reloadJigModules();
    });

    test.afterEach(teardownDom);

    test.describe('computeMaxDepth', () => {
        test('最も深いFQNの階層数を返す', () => {
            assert.equal(App.computeMaxDepth(['a.b.c', 'a.b', 'a.b.c.d']), 4);
        });

        test('空配列なら1を返す', () => {
            assert.equal(App.computeMaxDepth([]), 1);
        });

        test('undefinedでも例外を投げない', () => {
            assert.equal(App.computeMaxDepth(undefined), 1);
        });
    });

    test.describe('computeInitialDepth', () => {
        test('内部パッケージが1つ以下ならmaxDepthを返す', () => {
            assert.equal(App.computeInitialDepth(['a.b.c'], 3), 3);
            assert.equal(App.computeInitialDepth([], 3), 3);
        });

        test('集約後に複数パッケージへ分岐する最も浅い深さを返す', () => {
            // depth=1: a のみ（1種）、depth=2: a.b, a.c（2種）
            const depth = App.computeInitialDepth(['a.b.x', 'a.c.y'], 3);
            assert.equal(depth, 2);
        });

        test('どの深さでも分岐しない場合はmaxDepthを返す', () => {
            const depth = App.computeInitialDepth(['a.b.x', 'a.b.y'], 2);
            assert.equal(depth, 2);
        });
    });

    test.describe('nodeId / escape', () => {
        test('nodeId は英数字以外をアンダースコアへ変換する', () => {
            assert.equal(App.nodeId('com.example.Foo'), 'n_com_example_Foo');
        });

        test('nodeId は空文字・未定義でも例外を投げない', () => {
            assert.equal(App.nodeId(''), 'n_');
            assert.equal(App.nodeId(undefined), 'n_');
        });

        test('escape はダブルクォートをエスケープする', () => {
            assert.equal(App.escape('a"b"c'), 'a\\"b\\"c');
        });
    });

    test.describe('buildMermaidText', () => {
        const librariesById = new Map([
            ['lib1', {id: 'lib1', displayName: 'Lib One', isJavaStandard: false}],
            ['lib2', {id: 'lib2', displayName: 'Lib Two', isJavaStandard: true}],
        ]);
        const data = {
            libraries: [...librariesById.values()],
            relations: [
                {from: 'com.example.a.A', to: 'lib1'},
                {from: 'com.example.b.B', to: 'lib2'},
            ],
        };

        test('javaStandardを含めない場合はエッジ・ノードから除外する', () => {
            const text = App.buildMermaidText(data, librariesById, 2, false, new Set(), 'TB');
            assert.match(text, /Lib One/);
            assert.doesNotMatch(text, /Lib Two/);
        });

        test('javaStandardを含める場合は全ノードを描画する', () => {
            const text = App.buildMermaidText(data, librariesById, 2, true, new Set(), 'TB');
            assert.match(text, /Lib One/);
            assert.match(text, /Lib Two/);
        });

        test('選択中ライブラリがある場合はそれに関係するノードのみ表示する', () => {
            const text = App.buildMermaidText(data, librariesById, 2, true, new Set(['lib1']), 'TB');
            assert.match(text, /Lib One/);
            assert.doesNotMatch(text, /Lib Two/);
            assert.match(text, /classDef selectedLibrary/);
        });

        test('指定したdirectionがflowchart宣言に反映される', () => {
            const text = App.buildMermaidText(data, librariesById, 2, true, new Set(), 'LR');
            assert.match(text, /^flowchart LR/);
        });
    });

    test.describe('init 経由の toggleSelection', () => {
        function setupDiagram() {
            const diagram = document.createElement('pre');
            diagram.id = 'library-dependency-diagram';
            document.body.appendChild(diagram);
            return diagram;
        }

        test('データが無くても例外を投げない', () => {
            assert.doesNotThrow(() => App.init());
        });

        test('テーブル行クリックで選択がトグルされ is-selected が付与される', () => {
            setupDiagram();
            const tableBody = document.createElement('tbody');
            const table = document.createElement('table');
            table.id = 'library-table';
            table.appendChild(tableBody);
            document.body.appendChild(table);

            globalThis.libraryDependencyData = {
                internalPackages: ['com.example.a.A'],
                libraries: [{id: 'lib1', displayName: 'Lib One', isJavaStandard: false, samplePackages: [], usingClasses: []}],
                relations: [{from: 'com.example.a.A', to: 'lib1'}],
            };

            App.init();

            const row = tableBody.querySelector('tr');
            assert.ok(row);
            assert.equal(row.classList.contains('is-selected'), false);

            row.dispatchEvent(new window.Event('click', {bubbles: true}));
            assert.equal(row.classList.contains('is-selected'), true);

            row.dispatchEvent(new window.Event('click', {bubbles: true}));
            assert.equal(row.classList.contains('is-selected'), false);

            delete globalThis.libraryDependencyData;
        });

        test('clear-selection-button クリックで選択が全解除される', () => {
            setupDiagram();
            const tableBody = document.createElement('tbody');
            const table = document.createElement('table');
            table.id = 'library-table';
            table.appendChild(tableBody);
            document.body.appendChild(table);
            const clearButton = document.createElement('button');
            clearButton.id = 'clear-selection-button';
            document.body.appendChild(clearButton);

            globalThis.libraryDependencyData = {
                internalPackages: ['com.example.a.A'],
                libraries: [{id: 'lib1', displayName: 'Lib One', isJavaStandard: false, samplePackages: [], usingClasses: []}],
                relations: [{from: 'com.example.a.A', to: 'lib1'}],
            };

            App.init();
            const row = tableBody.querySelector('tr');
            row.dispatchEvent(new window.Event('click', {bubbles: true}));
            assert.equal(row.classList.contains('is-selected'), true);

            clearButton.dispatchEvent(new window.Event('click', {bubbles: true}));
            assert.equal(row.classList.contains('is-selected'), false);

            delete globalThis.libraryDependencyData;
        });
    });
});
