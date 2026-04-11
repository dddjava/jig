const test = require('node:test');
const assert = require('node:assert/strict');
const {DocumentStub} = require('./dom-stub.js');

// 依存モジュールを先にロードして Jig 名前空間をセットアップする
global.window = {
    location: {
        pathname: '/package.html',
        search: '',
        hash: ''
    },
    history: {
        replaceState: (state, title, url) => {
            global.window.location.search = url.split('?')[1] || '';
        }
    },
    addEventListener: () => {}
};
global.document = new DocumentStub();
require('../../main/resources/templates/assets/jig-util.js');
require('../../main/resources/templates/assets/jig-data.js');
require('../../main/resources/templates/assets/jig-glossary.js');
require('../../main/resources/templates/assets/jig-mermaid.js');
require('../../main/resources/templates/assets/jig-dom.js');

const PackageApp = require('../../main/resources/templates/assets/package.js');

test.describe('package.js URL同期', () => {
    test.beforeEach(() => {
        // 状態をリセット
        PackageApp.hierarchyState.aggregationDepth = 0;
        PackageApp.hierarchyState.packageFilterFqn = [];
        PackageApp.hierarchyState.transitiveReductionEnabled = true;
        PackageApp.exploreState.exploreTargetPackages = [];
        PackageApp.exploreState.exploreCallerMode = '1';
        PackageApp.exploreState.exploreCalleeMode = '1';
        
        global.window.location.search = '';
        
        // DOMをリセット
        global.document = new DocumentStub();
        global.document.body.classList.add("package-relation");
    });

    test('syncStateToURL: 状態がデフォルトの場合はクエリなし', () => {
        PackageApp.syncStateToURL();
        assert.equal(global.window.location.search, '');
    });

    test('syncStateToURL: 階層探索の状態をURLに反映する', () => {
        PackageApp.hierarchyState.aggregationDepth = 2;
        PackageApp.hierarchyState.packageFilterFqn = ['app.a', 'app.b'];
        PackageApp.hierarchyState.transitiveReductionEnabled = false;
        
        PackageApp.syncStateToURL();
        
        const params = new URLSearchParams(global.window.location.search);
        assert.equal(params.get('depth'), '2');
        assert.deepEqual(params.getAll('filter'), ['app.a', 'app.b']);
        assert.equal(params.get('reduction'), 'false');
    });

    test('syncStateToURL: 関連探索の状態をURLに反映する', () => {
        // タブを関連探索にする
        const tabButton = global.document.createElement('button');
        tabButton.className = 'tab-button is-active';
        tabButton.dataset.tab = 'explore';
        global.document.selectors.set('.package-mode-tabs .tab-button.is-active', tabButton);

        PackageApp.exploreState.exploreTargetPackages = ['app.target'];
        PackageApp.exploreState.exploreCallerMode = '-1';
        PackageApp.exploreState.exploreCalleeMode = '0';
        
        PackageApp.syncStateToURL();
        
        const params = new URLSearchParams(global.window.location.search);
        assert.equal(params.get('tab'), 'explore');
        assert.deepEqual(params.getAll('target'), ['app.target']);
        assert.equal(params.get('caller'), '-1');
        assert.equal(params.get('callee'), '0');
    });

    test('loadStateFromURL: URLから状態を復元する', () => {
        global.window.location.search = '?depth=3&filter=app.x&reduction=false&target=app.y&caller=0&callee=-1';
        
        PackageApp.loadStateFromURL();
        
        assert.equal(PackageApp.hierarchyState.aggregationDepth, 3);
        assert.deepEqual(PackageApp.hierarchyState.packageFilterFqn, ['app.x']);
        assert.equal(PackageApp.hierarchyState.transitiveReductionEnabled, false);
        assert.deepEqual(PackageApp.exploreState.exploreTargetPackages, ['app.y']);
        assert.equal(PackageApp.exploreState.exploreCallerMode, '0');
        assert.equal(PackageApp.exploreState.exploreCalleeMode, '-1');
    });

    test('loadStateFromURL: タブの復元', () => {
        global.window.location.search = '?tab=explore';
        const exploreTab = global.document.createElement('button');
        exploreTab.dataset.tab = 'explore';
        const clickMock = test.mock.fn();
        exploreTab.click = clickMock;
        global.document.selectors.set('.package-mode-tabs .tab-button[data-tab="explore"]', exploreTab);
        
        PackageApp.loadStateFromURL();
        
        assert.equal(clickMock.mock.calls.length, 1);
    });
});
