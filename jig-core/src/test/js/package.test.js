const test = require('node:test');
const assert = require('node:assert/strict');
const {Element, DocumentStub, setGlossaryData} = require('./dom-stub.js');

// 依存モジュールを先にロードして Jig 名前空間をセットアップする
global.window = {
    location: {pathname: '', search: '', hash: ''},
    history: {
        replaceState: () => {
        }
    },
    addEventListener: () => {
    }
};
global.document = new DocumentStub();
require('../../main/resources/templates/assets/jig-util.js');
require('../../main/resources/templates/assets/jig-data.js');
require('../../main/resources/templates/assets/jig-glossary.js');
require('../../main/resources/templates/assets/jig-mermaid.js');
require('../../main/resources/templates/assets/jig-dom.js');

const PackageApp = require('../../main/resources/templates/assets/package.js');
const originalDom = {...PackageApp.dom};

let testContext;

function setupDocument() {
    const doc = new DocumentStub();
    doc.body.classList.add("package-relation");
    global.document = doc;
    return doc;
}

function setPackageData(data, context) {
    globalThis.packageData = data;
    context.packageRelationCache = null; // Reset cache
}

function setupDomMocks() {
    const methods = Object.keys(originalDom);
    methods.forEach(name => {
        if (typeof originalDom[name] !== 'function') return;
        test.mock.method(PackageApp.dom, name, test.mock.fn((...args) => originalDom[name](...args)));
    });
}

function createPackageFilterControls(doc) {
    const select = doc.createElement('select');
    select.id = 'package-filter-select';
    select.multiple = true;
    const clearButton = doc.createElement('button');
    clearButton.id = 'clear-package-filter';
    doc.elementsById.set('package-filter-select', select);
    doc.elementsById.set('clear-package-filter', clearButton);
    return {select, clearButton};
}

function createDepthSelect(doc) {
    const select = doc.createElement('select');
    select.id = 'package-depth-select';
    doc.elementsById.set('package-depth-select', select);
    return select;
}

function setupDiagramEnvironment(doc, context) {
    const container = doc.createElement('div');
    const diagram = doc.createElement('div');
    diagram.id = 'package-relation-diagram';
    container.appendChild(diagram);
    const mutual = doc.createElement('div');
    mutual.id = 'mutual-dependency-list';
    doc.elementsById.set('mutual-dependency-list', mutual);
    const transitiveReductionToggle = doc.createElement('input');
    transitiveReductionToggle.id = 'transitive-reduction-toggle';
    transitiveReductionToggle.type = 'checkbox';
    doc.elementsById.set('transitive-reduction-toggle', transitiveReductionToggle);

    globalThis.Jig.mermaid.renderWithControls = function () {
    };
    return diagram;
}

function setupExploreEnv(doc) {
    const exploreTable = doc.createElement('table');
    exploreTable.id = 'explore-package-table';
    doc.elementsById.set('explore-package-table', exploreTable);
    const exploreDiagram = doc.createElement('div');
    exploreDiagram.id = 'package-explore-diagram';
    doc.elementsById.set('package-explore-diagram', exploreDiagram);
    return {exploreTable, exploreDiagram};
}

test.describe('package.js', () => {
    test.beforeEach(() => {
        // Use the module's internal state for testing, resetting it before each test
        testContext = PackageApp.hierarchyState;
        testContext.packageRelationCache = null;
        testContext.diagramNodeIdToFqn = new Map();
        testContext.aggregationDepth = 0;
        testContext.packageFilterFqn = [];
        testContext.diagramDirection = 'TB';
        testContext.mutualDependencyDiagramDirection = 'LR';
        testContext.transitiveReductionEnabled = true;

        PackageApp.exploreState.exploreTargetPackages = [];
        PackageApp.exploreState.exploreCollapsedPackages = [];
        PackageApp.exploreState.exploreCallerMode = '1';
        PackageApp.exploreState.exploreCalleeMode = '1';
        PackageApp.exploreState.diagramNodeIdToFqn = new Map();
        PackageApp.exploreState.diagramDirection = 'TB';

        setupDomMocks();
    });

    test.describe('データ取得/整形', () => {
        test.describe('ロジック', () => {
            test('parsePackageRelationData: 配列/オブジェクトに対応する', () => {
                const arrayData = PackageApp.parsePackageRelationData([
                    {fqn: 'app.a', classCount: 1},
                ]);
                assert.equal(arrayData.packages.length, 1);
                assert.equal(arrayData.relations.length, 0);

                const objectData = PackageApp.parsePackageRelationData({
                    packages: [{fqn: 'app.b', classCount: 2}],
                    relations: [{from: 'app.b', to: 'app.c'}],
                });
                assert.equal(objectData.packages.length, 1);
                assert.equal(objectData.relations.length, 1);
            });

            test('getPackageRelationData: 配列/オブジェクトに対応する', () => {
                setupDocument();
                setPackageData([{fqn: 'app.a', classCount: 1}], testContext);

                const data = PackageApp.getPackageRelationData(testContext);

                assert.equal(data.packages.length, 1);
                assert.equal(data.relations.length, 0);
            });

            test('getMaxPackageDepth: 最大深さを返す', () => {
                setupDocument();
                globalThis.packageData = {
                    packages: [
                        {fqn: 'app.domain.a'},
                        {fqn: 'app.b'},
                        {fqn: 'app.domain.core.c'},
                    ],
                    relations: [],
                };

                assert.equal(PackageApp.getMaxPackageDepth(), 4);
            });
        });
    });


    test.describe('フィルタ', () => {
        test.describe('ロジック', () => {
            test('normalizePackageFilterValue: 空文字または空白のみの文字列は空の配列を返す', () => {
                assert.deepEqual(PackageApp.normalizePackageFilterValue(''), []);
                assert.deepEqual(PackageApp.normalizePackageFilterValue('   '), []);
            });

            test('normalizePackageFilterValue: 改行区切りの文字列を配列として返す', () => {
                assert.deepEqual(PackageApp.normalizePackageFilterValue('app.domain\napp.other'), ['app.domain', 'app.other']);
                assert.deepEqual(PackageApp.normalizePackageFilterValue('  app.domain  \n  app.other  '), ['app.domain', 'app.other']);
                assert.deepEqual(PackageApp.normalizePackageFilterValue('app.domain\n\napp.other'), ['app.domain', 'app.other']);
            });

            test('normalizeAggregationDepthValue: 数値化する', () => {
                assert.equal(PackageApp.normalizeAggregationDepthValue('2'), 2);
                assert.equal(PackageApp.normalizeAggregationDepthValue('0'), 0);
                assert.equal(PackageApp.normalizeAggregationDepthValue('abc'), 0);
            });

            test('findDefaultPackageFilterCandidate: domainPackageRootsがあれば配列を返す', () => {
                const candidate = PackageApp.findDefaultPackageFilterCandidate(['app.domain']);
                assert.deepEqual(candidate, ['app.domain']);
            });

            test('findDefaultPackageFilterCandidate: 複数のdomainPackageRootsを配列で返す', () => {
                const candidate = PackageApp.findDefaultPackageFilterCandidate(['com.a.domain', 'org.b.domain']);
                assert.deepEqual(candidate, ['com.a.domain', 'org.b.domain']);
            });

            test('findDefaultPackageFilterCandidate: domainPackageRootsが空ならnullを返す', () => {
                assert.equal(PackageApp.findDefaultPackageFilterCandidate([]), null);
                assert.equal(PackageApp.findDefaultPackageFilterCandidate(null), null);
            });

            test('getInitialAggregationDepth: domainPackageRootsが空ならば0を返す', () => {
                assert.equal(PackageApp.getInitialAggregationDepth([]), 0);
                assert.equal(PackageApp.getInitialAggregationDepth(null), 0);
            });

            test('getInitialAggregationDepth: 最も浅いパッケージの深さ+1を返す', () => {
                assert.equal(PackageApp.getInitialAggregationDepth(['com.example.domain']), 4); // 深さ3 → 4
                assert.equal(PackageApp.getInitialAggregationDepth(['com.example', 'com.example.domain']), 3); // 深さ2 → 3
            });

            test('getRelativeFqn: 親パッケージが存在する場合はその配下からの相対名を返す', () => {
                const fqnSet = new Set(['app', 'app.domain', 'app.other']);

                assert.deepEqual(PackageApp.getRelativeFqn('app.domain.model', fqnSet),
                    {ancestor: 'app.domain', relative: 'model'});
                assert.deepEqual(PackageApp.getRelativeFqn('app.domain', fqnSet),
                    {ancestor: 'app', relative: 'domain'}); // app があるので domain
                assert.deepEqual(PackageApp.getRelativeFqn('app.other.service', fqnSet),
                    {ancestor: 'app.other', relative: 'service'});
                assert.deepEqual(PackageApp.getRelativeFqn('app.another.service', fqnSet),
                    {ancestor: 'app', relative: 'another.service'});
                assert.deepEqual(PackageApp.getRelativeFqn('other.pkg', fqnSet),
                    {ancestor: undefined, relative: 'other.pkg'}); // 親がいない
            });

            test('buildPackageRowVisibility: パッケージフィルタのみを表示する', () => {
                const visibility = PackageApp.buildPackageRowVisibility(
                    ['app.domain', 'app.other'],
                    ['app.domain']
                );
                assert.deepEqual(visibility, [true, false]);
            });

            test('buildPackageRowVisibility: 複数パッケージフィルタのうちいずれかに一致するものを表示する', () => {
                const visibility = PackageApp.buildPackageRowVisibility(
                    ['app.domain.model', 'app.domain.service', 'app.other'],
                    ['app.domain.model', 'app.other']
                );
                assert.deepEqual(visibility, [true, false, true]);
            });

            const packages = [
                {fqn: 'app.a'},
                {fqn: 'app.b'},
                {fqn: 'app.c'},
                {fqn: 'lib.d'},
            ];
            const relations = [
                {from: 'app.a', to: 'app.b'},
                {from: 'app.b', to: 'app.c'},
                {from: 'app.c', to: 'lib.d'},
            ];

            test('collectRelatedSet: directモードは隣接のみを含める', () => {
                const aggregationDepth = 0;
                const relations = [
                    {from: 'app.domain.a', to: 'app.domain.b'},
                    {from: 'app.domain.b', to: 'app.domain.c'},
                ];

                const related = PackageApp.collectFocusSet('app.domain.a', relations, aggregationDepth, '0', '1');

                assert.deepEqual(Array.from(related).sort(), ['app.domain.a', 'app.domain.b']);
            });

            test('collectFocusSet: allモードは推移的に辿る', () => {
                const aggregationDepth = 0;
                const relations = [
                    {from: 'app.domain.a', to: 'app.domain.b'},
                    {from: 'app.domain.b', to: 'app.domain.c'},
                ];

                const focusSet = PackageApp.collectFocusSet('app.domain.a', relations, aggregationDepth, '-1', '-1');

                assert.deepEqual(
                    Array.from(focusSet).sort(),
                    ['app.domain.a', 'app.domain.b', 'app.domain.c']
                );
            });

            test('collectFocusSet: 依存元なし、依存先直接の場合', () => {
                const aggregationDepth = 0;
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.c', to: 'app.a'},
                    {from: 'app.a', to: 'app.d'},
                ];
                const focusSet = PackageApp.collectFocusSet('app.a', relations, aggregationDepth, '0', '1');
                assert.deepEqual(Array.from(focusSet).sort(), ['app.a', 'app.b', 'app.d']); // a -> b, a -> d (direct callees)
            });

            test('collectFocusSet: 依存元直接、依存先なしの場合', () => {
                const aggregationDepth = 0;
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.c', to: 'app.a'},
                    {from: 'app.d', to: 'app.a'},
                ];
                const focusSet = PackageApp.collectFocusSet('app.a', relations, aggregationDepth, '1', '0');
                assert.deepEqual(Array.from(focusSet).sort(), ['app.a', 'app.c', 'app.d']); // c -> a, d -> a (direct callers)
            });

            test('collectExploreNodeSets: 直接モードで起点の依存元・依存先を収集する', () => {
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.c', to: 'app.b'},
                    {from: 'app.b', to: 'app.d'},
                ];
                const {targetSet, callerSet, calleeSet} = PackageApp.collectExploreNodeSets(
                    ['app.b'], relations, '1', '1'
                );
                assert.deepEqual(Array.from(targetSet).sort(), ['app.b']);
                assert.deepEqual(Array.from(callerSet).sort(), ['app.a', 'app.c']);
                assert.deepEqual(Array.from(calleeSet).sort(), ['app.d']);
            });

            test('collectExploreNodeSets: 複数起点の場合は和集合で収集する', () => {
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.c', to: 'app.b'},
                    {from: 'app.b', to: 'app.d'},
                    {from: 'app.e', to: 'app.c'},
                    {from: 'app.c', to: 'app.f'},
                ];
                const {targetSet, callerSet, calleeSet} = PackageApp.collectExploreNodeSets(
                    ['app.b', 'app.c'], relations, '1', '1'
                );
                assert.deepEqual(Array.from(targetSet).sort(), ['app.b', 'app.c']);
                assert.deepEqual(Array.from(callerSet).sort(), ['app.a', 'app.e']); // targetに含まれるものは除外
                assert.deepEqual(Array.from(calleeSet).sort(), ['app.d', 'app.f']); // targetに含まれるものは除外
            });

            test('collectExploreNodeSets: 依存元なしの場合', () => {
                const relations = [{from: 'app.a', to: 'app.b'}];
                const {callerSet, calleeSet} = PackageApp.collectExploreNodeSets(
                    ['app.a'], relations, '0', '1'
                );
                assert.deepEqual(Array.from(callerSet).sort(), []);
                assert.deepEqual(Array.from(calleeSet).sort(), ['app.b']);
            });

            test('collectExploreNodeSets: 起点が空の場合は空セットを返す', () => {
                const relations = [{from: 'app.a', to: 'app.b'}];
                const {targetSet, callerSet, calleeSet} = PackageApp.collectExploreNodeSets(
                    [], relations, '1', '1'
                );
                assert.equal(targetSet.size, 0);
                assert.equal(callerSet.size, 0);
                assert.equal(calleeSet.size, 0);
            });

            test('buildVisibleDiagramRelations: パッケージフィルタを適用する', () => {
                const base = globalThis.Jig.mermaid.builder.buildVisibleDiagramRelations(packages, relations, [], {
                    packageFilterFqn: ['app'],
                    aggregationDepth: 0,
                    transitiveReductionEnabled: false
                });
                assert.deepEqual(Array.from(base.packageFqns).sort(), ['app.a', 'app.b', 'app.c']);
                assert.equal(base.uniqueRelations.length, 2);
            });

            test('buildVisibleDiagramElements: packageFilterは配下のみを表示する', () => {
                const {packageFqns} = PackageApp.buildVisibleDiagramElements(packages, relations, [], ['app'], 0, false);
                assert.deepEqual(Array.from(packageFqns).sort(), ['app.a', 'app.b', 'app.c']);
            });
        });

        test.describe('UI', () => {
            test('applyDefaultPackageFilterIfPresent: domainPackageRootsがあれば適用する', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [
                        {fqn: 'app.domain.core'},
                        {fqn: 'app.domain.sub'},
                    ],
                    relations: [],
                    domainPackageRoots: ['app.domain'],
                }, testContext);
                createDepthSelect(doc); // for renderHierarchyDiagramAndTable

                PackageApp.applyDefaultPackageFilterIfPresent(testContext);

                assert.deepEqual(testContext.packageFilterFqn, ['app.domain']);
            });

            test('applyDefaultPackageFilterIfPresent: domainPackageRootsがなければ適用しない', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [
                        {fqn: 'app.domain.core'},
                        {fqn: 'app.domain.sub'},
                    ],
                    relations: [],
                }, testContext);
                createDepthSelect(doc);

                PackageApp.applyDefaultPackageFilterIfPresent(testContext);

                assert.deepEqual(testContext.packageFilterFqn, []);
            });

            test('setupPackageFilterControl: 解除を扱う', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [{fqn: 'app.domain', classCount: 1}],
                    relations: [],
                }, testContext);
                createDepthSelect(doc);

                const {clearButton} = createPackageFilterControls(doc);
                testContext.packageFilterFqn = ['app.domain'];

                PackageApp.setupPackageFilterControl(testContext);

                clearButton.dispatchEvent({type: 'click'});
                assert.deepEqual(testContext.packageFilterFqn, []);
            });
        });
    });

    test.describe('テーブル', () => {
        test.describe('ロジック', () => {
            test('aggregatePackageData: depth=0はそのまま返す', () => {
                const packages = [{fqn: 'app.domain.a', classCount: 1}];
                const relations = [{from: 'app.domain.a', to: 'app.other.b'}];
                const result = PackageApp.aggregatePackageData(packages, relations, 0);
                assert.equal(result.packages, packages);
                assert.equal(result.relations, relations);
            });

            test('aggregatePackageData: depth指定でパッケージと関連を集約する', () => {
                const packages = [
                    {fqn: 'app.domain.a', classCount: 2},
                    {fqn: 'app.domain.b', classCount: 3},
                    {fqn: 'app.other.c', classCount: 1},
                ];
                const relations = [
                    {from: 'app.domain.a', to: 'app.other.c'},
                    {from: 'app.domain.b', to: 'app.other.c'}, // 集約後は重複
                    {from: 'app.domain.a', to: 'app.domain.b'}, // 集約後は自己ループ→除外
                ];

                const result = PackageApp.aggregatePackageData(packages, relations, 2);

                assert.equal(result.packages.length, 2);
                const domain = result.packages.find(p => p.fqn === 'app.domain');
                assert.equal(domain.classCount, 5);
                assert.equal(result.relations.length, 1);
                assert.equal(result.relations[0].from, 'app.domain');
                assert.equal(result.relations[0].to, 'app.other');
            });
        });

        test.describe('UI', () => {
            test('renderHierarchyPackageList: クラス数・関連数を表示する', () => {
                setGlossaryData({
                    'app.a': {title: 'A', simpleText: 'a', kind: 'パッケージ', description: ''},
                    'app.b': {title: 'B', simpleText: 'b', kind: 'パッケージ', description: ''},
                });
                const doc = setupDocument();
                setPackageData({
                    packages: [
                        {fqn: 'app.a', classCount: 3},
                        {fqn: 'app.b', classCount: 1},
                    ],
                    relations: [
                        {from: 'app.a', to: 'app.b'},
                    ],
                }, PackageApp.hierarchyState);
                const table = doc.createElement('table');
                table.id = 'hierarchy-package-table';
                doc.elementsById.set('hierarchy-package-table', table);

                PackageApp.renderHierarchyPackageList(PackageApp.hierarchyState);

                const tbody = table.querySelector('tbody');
                assert.ok(tbody, 'tbodyが生成されること');
                assert.equal(tbody.children.length, 2);

                const rowA = tbody.children.find(tr => tr.dataset.fqn === 'app.a');
                assert.equal(rowA.querySelector('td[data-count="class"]').textContent, '3');
                assert.equal(rowA.querySelector('td[data-count="incoming"]').textContent, '0');
                assert.equal(rowA.querySelector('td[data-count="outgoing"]').textContent, '1');

                const rowB = tbody.children.find(tr => tr.dataset.fqn === 'app.b');
                assert.equal(rowB.querySelector('td[data-count="class"]').textContent, '1');
                assert.equal(rowB.querySelector('td[data-count="incoming"]').textContent, '1');
                assert.equal(rowB.querySelector('td[data-count="outgoing"]').textContent, '0');
                delete globalThis.glossaryData;
            });

            test('renderHierarchyPackageList: クリックでパッケージフィルタをトグルする', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [
                        {fqn: 'app.a', classCount: 1},
                        {fqn: 'app.b', classCount: 1},
                    ],
                    relations: [],
                }, PackageApp.hierarchyState);
                createDepthSelect(doc);
                const table = doc.createElement('table');
                table.id = 'hierarchy-package-table';
                doc.elementsById.set('hierarchy-package-table', table);

                PackageApp.renderHierarchyPackageList(PackageApp.hierarchyState);

                const tbody = table.querySelector('tbody');
                const rowA = tbody.children.find(tr => tr.dataset.fqn === 'app.a');
                assert.ok(!rowA.classList.has('explore-target-selected'));

                // クリックで選択
                rowA.dispatchEvent({type: 'click'});
                assert.deepEqual(PackageApp.hierarchyState.packageFilterFqn, ['app.a']);
                assert.ok(rowA.classList.has('explore-target-selected'));

                // 再クリックで解除
                rowA.dispatchEvent({type: 'click'});
                assert.deepEqual(PackageApp.hierarchyState.packageFilterFqn, []);
                assert.ok(!rowA.classList.has('explore-target-selected'));
            });

            test('renderHierarchyPackageList: domainPackageRootsがpackagesにない場合は追加する', () => {
                const doc = setupDocument();
                setPackageData({
                    packages: [
                        {fqn: 'app.domain.core', classCount: 2},
                    ],
                    relations: [],
                    domainPackageRoots: ['app.domain'],
                }, PackageApp.hierarchyState);
                const table = doc.createElement('table');
                table.id = 'hierarchy-package-table';
                doc.elementsById.set('hierarchy-package-table', table);

                PackageApp.renderHierarchyPackageList(PackageApp.hierarchyState);

                const tbody = table.querySelector('tbody');
                assert.equal(tbody.children.length, 2);
                const fqns = tbody.children.map(tr => tr.dataset.fqn);
                assert.ok(fqns.includes('app.domain'), 'domainPackageRootが追加されること');
                assert.ok(fqns.includes('app.domain.core'));

                const domainRow = tbody.children.find(tr => tr.dataset.fqn === 'app.domain');
                assert.equal(domainRow.querySelector('td[data-count="class"]').textContent, '0');
            });

            test('renderExplorePackageList: クラス数・関連数を表示する', () => {
                setGlossaryData({
                    'app.a': {title: 'A', simpleText: 'a', kind: 'パッケージ', description: ''},
                    'app.b': {title: 'B', simpleText: 'b', kind: 'パッケージ', description: ''},
                });
                const doc = setupDocument();
                setPackageData({
                    packages: [
                        {fqn: 'app.a', classCount: 3},
                        {fqn: 'app.b', classCount: 1},
                    ],
                    relations: [
                        {from: 'app.a', to: 'app.b'},
                    ],
                }, PackageApp.hierarchyState);
                const {exploreTable: table} = setupExploreEnv(doc);

                PackageApp.renderExplorePackageList(PackageApp.exploreState);

                const tbody = table.querySelector('tbody');
                assert.ok(tbody, 'tbodyが生成されること');
                assert.equal(tbody.children.length, 2);

                const rowA = tbody.children.find(tr => tr.dataset.fqn === 'app.a');
                assert.equal(rowA.querySelector('td[data-count="class"]').textContent, '3');
                assert.equal(rowA.querySelector('td[data-count="incoming"]').textContent, '0');
                assert.equal(rowA.querySelector('td[data-count="outgoing"]').textContent, '1');

                const rowB = tbody.children.find(tr => tr.dataset.fqn === 'app.b');
                assert.equal(rowB.querySelector('td[data-count="class"]').textContent, '1');
                assert.equal(rowB.querySelector('td[data-count="incoming"]').textContent, '1');
                assert.equal(rowB.querySelector('td[data-count="outgoing"]').textContent, '0');
                delete globalThis.glossaryData;
            });

            test('renderExplorePackageList: 折りたたみ時にサブパッケージのカウントを合算する', () => {
                const doc = setupDocument();
                setPackageData({
                    packages: [
                        {fqn: 'app', classCount: 1},
                        {fqn: 'app.domain', classCount: 2},
                        {fqn: 'app.domain.sub', classCount: 3},
                    ],
                    relations: [
                        {from: 'app', to: 'app.domain'},
                        {from: 'app.domain', to: 'app.domain.sub'},
                    ],
                }, PackageApp.hierarchyState);
                PackageApp.exploreState.exploreCollapsedPackages = ['app.domain'];
                const {exploreTable: table} = setupExploreEnv(doc);

                PackageApp.renderExplorePackageList(PackageApp.exploreState);

                const tbody = table.querySelector('tbody');
                // app.domain は折りたたみ中 → classCount = 2 + 3 = 5
                const domainRow = tbody.children.find(tr => tr.dataset.fqn === 'app.domain');
                assert.equal(domainRow.querySelector('td[data-count="class"]').textContent, '5');    // classCount合算(2+3)
                assert.equal(domainRow.querySelector('td[data-count="incoming"]').textContent, '2'); // incomingCount合算(app→domain:1, domain→sub:1)
                assert.equal(domainRow.querySelector('td[data-count="outgoing"]').textContent, '1'); // outgoingCount合算(domain→sub:1, sub→:0)

                // app は折りたたんでいない → 自身のカウントのみ
                const appRow = tbody.children.find(tr => tr.dataset.fqn === 'app');
                assert.equal(appRow.querySelector('td[data-count="class"]').textContent, '1');
                assert.equal(appRow.querySelector('td[data-count="incoming"]').textContent, '0');
                assert.equal(appRow.querySelector('td[data-count="outgoing"]').textContent, '1');
            });
        });
    });

    test.describe('ダイアグラム', () => {
        test.describe('ロジック', () => {
            test('buildMutualDependencyItems: 相互依存の原因を整形する', () => {
                const items = PackageApp.buildMutualDependencyItems(
                    new Set(['app.alpha::app.beta']),
                    [
                        {from: 'app.alpha.A', to: 'app.beta.B'},
                        {from: 'app.beta.B', to: 'app.alpha.A'},
                    ],
                    0
                );

                assert.equal(items.length, 1);
                assert.equal(items[0].pairLabel, 'app.alpha <-> app.beta');
                assert.equal(items[0].causes.length, 2);
                assert.equal(items[0].causesForward.length, 1);  // app.alpha -> app.beta
                assert.equal(items[0].causesBackward.length, 1); // app.beta -> app.alpha
                assert.equal(items[0].stats[0].classCount, 1);   // app.alpha: A のみ（from/toで重複排除）
                assert.equal(items[0].stats[0].relationCount, 1);
                assert.equal(items[0].stats[1].classCount, 1);   // app.beta: B のみ（from/toで重複排除）
                assert.equal(items[0].stats[1].relationCount, 1);
            });

            test('buildMutualDependencyDiagramSource: 相互依存のMermaidソースを生成する', () => {
                const causes = [
                    'a.b.c.d.A -> a.b.x.y.B',
                    'a.b.x.y.B -> a.b.c.d.A',
                    'a.b.c.d.sub.C -> a.b.x.y.sub.D',
                    'a.b.x.y.sub.D -> a.b.c.d.sub.C'
                ];
                const {source} = PackageApp.buildMutualDependencyDiagramSource(causes, 'LR', 'a.b.c.d <-> a.b.x.y');
                const lines = source.split('\n').map(l => l.trim());
                assert.ok(lines.includes('graph LR;'));
                assert.ok(lines.includes('subgraph O0["d"]'));
                assert.ok(lines.includes('subgraph O1["y"]'));
                assert.ok(lines.some(l => l.match(/subgraph P\d+\["sub"\]/)));
                assert.ok(lines.includes('a_b_c_d_A["A"]'));
                assert.ok(lines.includes('a_b_x_y_B["B"]'));
                assert.ok(lines.includes('a_b_c_d_sub_C["C"]'));
                assert.ok(lines.includes('a_b_x_y_sub_D["D"]'));
                assert.ok(lines.includes('a_b_c_d_A --> a_b_x_y_B'));
                assert.ok(lines.includes('a_b_x_y_B --> a_b_c_d_A'));
                assert.ok(lines.includes('a_b_c_d_sub_C --> a_b_x_y_sub_D'));
            });

            test('buildMutualDependencyDiagramSource: 包含関係の相互依存では外側の子パッケージsubgraphを作らない', () => {
                const causes = [
                    'a.b.c.A -> a.b.c.d.B',
                    'a.b.c.d.B -> a.b.c.A',
                ];
                const {source} = PackageApp.buildMutualDependencyDiagramSource(causes, 'LR', 'a.b.c <-> a.b.c.d');
                const lines = source.split('\n').map(l => l.trim());
                assert.ok(lines.includes('subgraph O0["c"]'));
                assert.ok(!lines.includes('subgraph O1["d"]'));
            });

            test('buildMutualDependencyDiagramSource: 包含関係の相互依存でも子パッケージはネストsubgraphに配置する', () => {
                const causes = [
                    'org.dddjava.jig.JigExecutor -> org.dddjava.jig.adapter.JigDocumentGenerator',
                    'org.dddjava.jig.adapter.JigDocumentGenerator -> org.dddjava.jig.HandleResult',
                    'org.dddjava.jig.adapter.JigDocumentGenerator -> org.dddjava.jig.JigResult',
                    'org.dddjava.jig.adapter.JigResultData -> org.dddjava.jig.HandleResult',
                    'org.dddjava.jig.adapter.JigResultData -> org.dddjava.jig.JigResult',
                    'org.dddjava.jig.adapter.JigResultData -> org.dddjava.jig.JigResult$JigSummary',
                    'org.dddjava.jig.adapter.html.view.IndexView -> org.dddjava.jig.HandleResult',
                ];
                const {source} = PackageApp.buildMutualDependencyDiagramSource(
                    causes,
                    'LR',
                    'org.dddjava.jig <-> org.dddjava.jig.adapter'
                );
                const lines = source.split('\n').map(l => l.trim());
                assert.ok(lines.includes('subgraph O0["jig"]'));
                assert.ok(!lines.includes('subgraph O1["adapter"]'));
                assert.ok(lines.some(l => l.match(/subgraph P\d+\["adapter"\]/)));
                assert.ok(lines.some(l => l.match(/subgraph P\d+\["view"\]/)));
                assert.ok(lines.includes('org_dddjava_jig_adapter_JigDocumentGenerator["JigDocumentGenerator"]'));
                assert.ok(lines.includes('org_dddjava_jig_adapter_JigResultData["JigResultData"]'));
            });

            test('buildAggregationDepthOptions: 集約オプションを組み立てる', () => {
                const options = PackageApp.buildAggregationDepthOptions(2);

                assert.deepEqual(options, [
                    {value: '0', text: '集約なし'},
                    {value: '1', text: '深さ1'},
                    {value: '2', text: '深さ2'},
                ]);
            });

        });

        test.describe('UI', () => {
            test('renderMutualDependencyList: 相互依存と原因を一覧化する', () => {
                const doc = setupDocument();
                const container = new Element('div', doc);
                doc.elementsById.set('mutual-dependency-list', container);
                testContext.aggregationDepth = 0;

                PackageApp.renderMutualDependencyList(
                    new Set(['app.alpha::app.beta']),
                    [
                        {from: 'app.alpha.A', to: 'app.beta.B'},
                        {from: 'app.beta.B', to: 'app.alpha.A'},
                    ],
                    testContext.aggregationDepth,
                    testContext
                );

                assert.equal(container.style.display, '');
                // details要素のみ
                assert.equal(container.children.length, 1);
                const details = container.children[0];
                assert.equal(details.tagName, 'details');
                assert.ok(details.className.includes('jig-card--type'));
                assert.equal(details.children[0].tagName, 'summary');
                assert.equal(details.children[0].textContent, '相互依存分析');

                const tabSection = details.children[1];
                assert.ok(tabSection.className.includes('tab-content-section'));
                const tabsBar = tabSection.children[0];
                assert.equal(tabsBar.className, 'jig-tabs');
                // title + 4 tabs: 概要, クラス関連図, テキスト, シミュレーション
                assert.equal(tabsBar.children.length, 5);
                assert.equal(tabsBar.children[0].className, 'mutual-dependency-title');
                assert.equal(tabsBar.children[0].textContent, 'alpha <-> beta');
                assert.equal(tabsBar.children[1].textContent, '概要');
                assert.equal(tabsBar.children[2].textContent, 'クラス関連図');
                assert.equal(tabsBar.children[3].textContent, 'テキスト');
                assert.equal(tabsBar.children[4].textContent, 'シミュレーション');
                // 概要パネルにペアラベルとstatsテーブルが表示される
                const overviewPanel = tabSection.children[1];
                assert.equal(overviewPanel.children[0].textContent, 'app.alpha <-> app.beta');
                const statsTable = overviewPanel.children[1];
                assert.equal(statsTable.className, 'mutual-dependency-stats');
                const rows = statsTable.querySelector('tbody').children;
                assert.equal(rows.length, 2);
                assert.equal(rows[0].children[0].textContent, 'alpha'); // glossary title
                assert.equal(rows[1].children[0].textContent, 'beta');
                // テキストパネルにcausesが表示される
                const textPanel = tabSection.children[3];
                assert.equal(textPanel.children[0].className, 'causes');
            });

            test('renderHierarchyDiagram: 相互依存を含めて描画する', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [
                        {fqn: 'app.a', classCount: 1},
                        {fqn: 'app.b', classCount: 1},
                    ],
                    relations: [
                        {from: 'app.a', to: 'app.b'},
                        {from: 'app.b', to: 'app.a'},
                    ],
                }, testContext);

                PackageApp.renderHierarchyDiagram(testContext);

                const diagram = doc.getElementById('package-relation-diagram');
                assert.equal(diagram._textContent.includes('graph'), true);
                assert.equal(diagram._textContent.includes('<-->'), true);
                const mutual = doc.getElementById('mutual-dependency-list');
                assert.equal(mutual.children.length > 0, true);
            });

            test('registerHierarchyDiagramClickHandler: クリックでフィルタへ切り替える', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({packages: [{fqn: 'app.example', classCount: 1}], relations: []}, testContext);
                doc.selectorsAll.set('#package-table tbody tr', []);
                createPackageFilterControls(doc);
                createDepthSelect(doc);
                testContext.diagramNodeIdToFqn = new Map([['P1', 'app.example']]);

                PackageApp.registerHierarchyDiagramClickHandler(testContext);

                global.window[PackageApp.HIERARCHY_DIAGRAM_CLICK_HANDLER_NAME]('P1');

                assert.deepEqual(testContext.packageFilterFqn, ['app.example']);
            });

            test('resetPackageFilterButton: リセットボタンでデフォルトフィルタに戻す', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [{fqn: 'app.domain.core', classCount: 1}],
                    relations: [],
                    domainPackageRoots: ['app.domain'],
                }, testContext);
                createDepthSelect(doc);

                const resetButton = doc.createElement('button');
                resetButton.id = 'reset-package-filter';
                doc.elementsById.set('reset-package-filter', resetButton);
                testContext.packageFilterFqn = [];

                PackageApp.setupPackageFilterControl(testContext);
                resetButton.dispatchEvent({type: 'click'});

                assert.deepEqual(testContext.packageFilterFqn, ['app.domain']);
                testContext.packageFilterFqn = [];
            });

            test('setupTransitiveReductionControl: UIをセットアップする', () => {
                const doc = setupDocument();

                // renderHierarchyDiagramAndTableの副作用をチェックするための準備
                setupDiagramEnvironment(doc, testContext);
                setPackageData({packages: [{fqn: 'a'}], relations: []}, testContext);
                const depthSelect = createDepthSelect(doc);
                const dummyOption = doc.createElement('option');
                dummyOption.id = 'dummy-option-for-test';
                depthSelect.appendChild(dummyOption);
                doc.selectorsAll.set('#package-table tbody tr', []);


                PackageApp.setupTransitiveReductionControl(testContext);

                const checkbox = doc.getElementById('transitive-reduction-toggle');
                assert.ok(checkbox, 'checkbox should be created');
                assert.equal(checkbox.checked, true);
                assert.equal(testContext.transitiveReductionEnabled, true);

                // changeイベントを発火させる
                checkbox.checked = false;
                checkbox.dispatchEvent({type: 'change'});

                assert.equal(testContext.transitiveReductionEnabled, false);
            });
        });
    });

    test.describe('filterByPackageFilter', () => {
        test('フィルタありの場合パッケージと関連を絞り込む', () => {
            const packages = [{fqn: 'app.a'}, {fqn: 'app.b'}, {fqn: 'lib.c'}];
            const relations = [{from: 'app.a', to: 'app.b'}, {from: 'app.a', to: 'lib.c'}];
            const result = PackageApp.filterByPackageFilter(packages, relations, ['app']);
            assert.equal(result.packages.length, 2);
            assert.equal(result.relations.length, 1);
            assert.equal(result.relations[0].from, 'app.a');
            assert.equal(result.relations[0].to, 'app.b');
        });
    });

    test.describe('collectExploreNodeSets - 推移的モード', () => {
        test("callerMode='-1'で推移的に依存元を収集する", () => {
            const relations = [
                {from: 'app.a', to: 'app.b'},
                {from: 'app.c', to: 'app.a'},
                {from: 'app.d', to: 'app.c'},
            ];
            const {callerSet} = PackageApp.collectExploreNodeSets(['app.b'], relations, '-1', '0');
            assert.ok(callerSet.has('app.a'));
            assert.ok(callerSet.has('app.c'));
            assert.ok(callerSet.has('app.d'));
        });

        test("calleeMode='-1'で推移的に依存先を収集する", () => {
            const relations = [
                {from: 'app.a', to: 'app.b'},
                {from: 'app.b', to: 'app.c'},
                {from: 'app.c', to: 'app.d'},
            ];
            const {calleeSet} = PackageApp.collectExploreNodeSets(['app.a'], relations, '0', '-1');
            assert.ok(calleeSet.has('app.b'));
            assert.ok(calleeSet.has('app.c'));
            assert.ok(calleeSet.has('app.d'));
        });
    });

    test.describe('registerExploreDiagramClickHandler', () => {
        test('ノードクリックで探索対象パッケージを追加して再描画する', () => {
            const doc = setupDocument();
            setupExploreEnv(doc);
            setPackageData({packages: [{fqn: 'app.example', classCount: 1}], relations: []}, PackageApp.exploreState);
            PackageApp.exploreState.diagramNodeIdToFqn = new Map([['P1', 'app.example']]);

            PackageApp.registerExploreDiagramClickHandler(PackageApp.exploreState);
            global.window[PackageApp.EXPLORE_DIAGRAM_CLICK_HANDLER_NAME]('P1');

            assert.deepEqual(PackageApp.exploreState.exploreTargetPackages, ['app.example']);
        });

        test('既に選択済みのノードは追加しない', () => {
            PackageApp.exploreState.exploreTargetPackages = ['app.example'];
            PackageApp.exploreState.diagramNodeIdToFqn = new Map([['P1', 'app.example']]);

            PackageApp.registerExploreDiagramClickHandler(PackageApp.exploreState);
            global.window[PackageApp.EXPLORE_DIAGRAM_CLICK_HANDLER_NAME]('P1');

            assert.deepEqual(PackageApp.exploreState.exploreTargetPackages, ['app.example']);
        });

        test('存在しないノードIDのクリックは無視する', () => {
            PackageApp.exploreState.diagramNodeIdToFqn = new Map();
            PackageApp.registerExploreDiagramClickHandler(PackageApp.exploreState);
            global.window[PackageApp.EXPLORE_DIAGRAM_CLICK_HANDLER_NAME]('NonExistent');
            assert.deepEqual(PackageApp.exploreState.exploreTargetPackages, []);
        });
    });

    test.describe('renderExploreDiagram', () => {
        test('diagram要素がなければsyncStateToURLを呼んで終了する', () => {
            const doc = setupDocument();
            setPackageData({packages: [], relations: []}, PackageApp.exploreState);
            PackageApp.renderExploreDiagram(PackageApp.exploreState);
        });

        test('対象パッケージが空ならプレースホルダーを表示する', () => {
            const doc = setupDocument();
            const {exploreDiagram: diagram} = setupExploreEnv(doc);
            setPackageData({packages: [], relations: []}, PackageApp.exploreState);

            PackageApp.renderExploreDiagram(PackageApp.exploreState);

            assert.ok(diagram.innerHTML.includes('placeholder-text'), `プレースホルダーが表示される: ${diagram.innerHTML}`);
        });

        test('対象パッケージがあれば図を描画する', () => {
            const doc = setupDocument();
            const {exploreDiagram: diagram} = setupExploreEnv(doc);
            setPackageData({
                packages: [{fqn: 'app.a', classCount: 1}, {fqn: 'app.b', classCount: 1}],
                relations: [{from: 'app.a', to: 'app.b'}]
            }, PackageApp.exploreState);
            PackageApp.exploreState.exploreTargetPackages = ['app.a'];

            PackageApp.renderExploreDiagram(PackageApp.exploreState);

            assert.ok(diagram._textContent.length > 0 || diagram.innerHTML.length > 0, "図のコンテンツが設定される");
        });
    });

    test.describe('setupExploreControl', () => {
        function setupExploreControlElements(doc) {
            const clearButton = doc.createElement('button');
            clearButton.id = 'explore-clear-selection';
            doc.elementsById.set('explore-clear-selection', clearButton);

            const callerRadio0 = doc.createElement('input');
            callerRadio0.setAttribute('type', 'radio');
            callerRadio0.setAttribute('name', 'explore-caller-mode');
            callerRadio0.value = '0';
            const callerRadio1 = doc.createElement('input');
            callerRadio1.setAttribute('type', 'radio');
            callerRadio1.setAttribute('name', 'explore-caller-mode');
            callerRadio1.value = '1';

            const calleeRadio0 = doc.createElement('input');
            calleeRadio0.setAttribute('type', 'radio');
            calleeRadio0.setAttribute('name', 'explore-callee-mode');
            calleeRadio0.value = '0';
            const calleeRadio1 = doc.createElement('input');
            calleeRadio1.setAttribute('type', 'radio');
            calleeRadio1.setAttribute('name', 'explore-callee-mode');
            calleeRadio1.value = '1';

            doc.selectorsAll.set('input[name="explore-caller-mode"]', [callerRadio0, callerRadio1]);
            doc.selectorsAll.set('input[name="explore-callee-mode"]', [calleeRadio0, calleeRadio1]);

            return {clearButton, callerRadio0, callerRadio1, calleeRadio0, calleeRadio1};
        }

        test('クリアボタンで選択をリセットして再描画する', () => {
            const doc = setupDocument();
            setupExploreEnv(doc);
            setPackageData({packages: [{fqn: 'app.a', classCount: 1}], relations: []}, PackageApp.exploreState);

            const {clearButton} = setupExploreControlElements(doc);
            PackageApp.exploreState.exploreTargetPackages = ['app.a'];

            PackageApp.setupExploreControl(PackageApp.exploreState);
            clearButton.dispatchEvent({type: 'click'});

            assert.deepEqual(PackageApp.exploreState.exploreTargetPackages, []);
        });

        test('依存元ラジオボタン変更でexploreCallerModeを更新する', () => {
            const doc = setupDocument();
            setupExploreEnv(doc);
            setPackageData({packages: [], relations: []}, PackageApp.exploreState);
            const {callerRadio0} = setupExploreControlElements(doc);

            PackageApp.setupExploreControl(PackageApp.exploreState);
            callerRadio0.checked = true;
            callerRadio0.dispatchEvent({type: 'change'});

            assert.equal(PackageApp.exploreState.exploreCallerMode, '0');
        });

        test('依存先ラジオボタン変更でexploreCalleeModeを更新する', () => {
            const doc = setupDocument();
            setupExploreEnv(doc);
            setPackageData({packages: [], relations: []}, PackageApp.exploreState);
            const {calleeRadio0} = setupExploreControlElements(doc);

            PackageApp.setupExploreControl(PackageApp.exploreState);
            calleeRadio0.checked = true;
            calleeRadio0.dispatchEvent({type: 'change'});

            assert.equal(PackageApp.exploreState.exploreCalleeMode, '0');
        });
    });

    test.describe('setupAggregationDepthControl', () => {
        function setupDepthSelect(doc) {
            const select = doc.createElement('select');
            select.id = 'package-depth-select';
            doc.elementsById.set('package-depth-select', select);
            // select.options を children にフォールバックするパッチ
            Object.defineProperty(select, 'options', {get: () => select.children, configurable: true});
            return select;
        }

        test('深さ選択の変更でaggregationDepthを更新する', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({packages: [{fqn: 'app.a.b.c', classCount: 1}], relations: []}, testContext);
            const select = setupDepthSelect(doc);

            PackageApp.setupAggregationDepthControl(testContext);
            select.value = '2';
            select.dispatchEvent({type: 'change'});

            assert.equal(testContext.aggregationDepth, 2);
            testContext.aggregationDepth = 0;
        });

        test('upButtonクリックで集約深さを浅くする', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({packages: [{fqn: 'app.a.b.c', classCount: 1}], relations: []}, testContext);
            const select = setupDepthSelect(doc);
            const upButton = doc.createElement('button');
            upButton.id = 'depth-up-button';
            doc.elementsById.set('depth-up-button', upButton);
            const downButton = doc.createElement('button');
            downButton.id = 'depth-down-button';
            doc.elementsById.set('depth-down-button', downButton);

            PackageApp.setupAggregationDepthControl(testContext);
            // 深さ2を選択してから upButton で戻る
            select.value = '2';
            select.dispatchEvent({type: 'change'});
            assert.equal(testContext.aggregationDepth, 2);

            upButton.click();
            assert.equal(testContext.aggregationDepth, 1);
            testContext.aggregationDepth = 0;
        });

        test('downButtonクリックで集約深さを深くする', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({packages: [{fqn: 'app.a.b.c', classCount: 1}], relations: []}, testContext);
            const select = setupDepthSelect(doc);
            const upButton = doc.createElement('button');
            upButton.id = 'depth-up-button';
            doc.elementsById.set('depth-up-button', upButton);
            const downButton = doc.createElement('button');
            downButton.id = 'depth-down-button';
            doc.elementsById.set('depth-down-button', downButton);

            PackageApp.setupAggregationDepthControl(testContext);
            downButton.click();

            assert.equal(testContext.aggregationDepth, 1);
            testContext.aggregationDepth = 0;
        });
    });

    test.describe('setupTabControl', () => {
        test('タブクリックでアクティブクラスを切り替えコールバックを呼ぶ', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({packages: [], relations: []}, testContext);
            createDepthSelect(doc);

            const tabButton = doc.createElement('button');
            tabButton.className = 'tab-button';
            tabButton.setAttribute('data-tab', 'hierarchy');
            const tabsContainer = doc.createElement('div');
            tabsContainer.className = 'package-mode-tabs';
            tabsContainer.appendChild(tabButton);
            doc.body.appendChild(tabsContainer);

            const panel = doc.createElement('div');
            panel.id = 'panel-hierarchy';
            panel.className = 'package-tab-panel';
            doc.elementsById.set('panel-hierarchy', panel);

            doc.selectorsAll.set('.package-mode-tabs .tab-button', [tabButton]);
            doc.selectorsAll.set('.package-tab-panel', [panel]);

            let calledWith = null;
            PackageApp.setupTabControl((tabName) => { calledWith = tabName; });

            tabButton.click();

            assert.equal(calledWith, 'hierarchy');
            assert.ok(tabButton.classList.has('is-active'));
        });
    });

    test.describe('renderHierarchyPackageList - collapse/filter', () => {
        test('折りたたみトグルで子パッケージを非表示にする', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({
                packages: [{fqn: 'app', classCount: 1}, {fqn: 'app.sub', classCount: 2}],
                relations: []
            }, testContext);
            createDepthSelect(doc);
            const table = doc.createElement('table');
            table.id = 'hierarchy-package-table';
            doc.elementsById.set('hierarchy-package-table', table);
            testContext.hierarchyCollapsedPackages = [];

            PackageApp.renderHierarchyPackageList(testContext);

            const tbody = table.querySelector('tbody');
            const appRow = tbody.children.find(tr => tr.dataset.fqn === 'app');
            const toggleBtn = appRow?.querySelector('.explore-collapse-toggle');
            assert.ok(toggleBtn, 'toggle button should exist');
            toggleBtn.dispatchEvent({type: 'click', stopPropagation: () => {}});
            assert.ok(testContext.hierarchyCollapsedPackages.includes('app'));
        });

        test('フィルター入力でFQNに一致する行のみ表示する', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({
                packages: [{fqn: 'app.domain', classCount: 1}, {fqn: 'lib.util', classCount: 1}],
                relations: []
            }, testContext);
            createDepthSelect(doc);
            const table = doc.createElement('table');
            table.id = 'hierarchy-package-table';
            doc.elementsById.set('hierarchy-package-table', table);
            const filterInput = doc.createElement('input');
            filterInput.id = 'hierarchy-list-filter';
            doc.elementsById.set('hierarchy-list-filter', filterInput);

            PackageApp.renderHierarchyPackageList(testContext);

            filterInput.value = 'domain';
            filterInput.dispatchEvent({type: 'input'});

            const tbody = table.querySelector('tbody');
            const domainRow = tbody.children.find(tr => tr.dataset.fqn === 'app.domain');
            const libRow = tbody.children.find(tr => tr.dataset.fqn === 'lib.util');
            assert.ok(!domainRow.classList.has('hidden'), 'domainが表示される');
            assert.ok(libRow.classList.has('hidden'), 'lib.utilが非表示になる');
        });
    });

    test.describe('renderExplorePackageList - onRowClick', () => {
        test('行クリックで探索対象に追加する', () => {
            const doc = setupDocument();
            const {exploreTable: table} = setupExploreEnv(doc);
            setPackageData({packages: [{fqn: 'app.a', classCount: 1}], relations: []}, PackageApp.exploreState);

            PackageApp.renderExplorePackageList(PackageApp.exploreState);

            const tbody = table.querySelector('tbody');
            const row = tbody?.children.find(tr => tr.dataset.fqn === 'app.a');
            assert.ok(row, 'app.a の行が存在すること');
            row.dispatchEvent({type: 'click'});
            assert.deepEqual(PackageApp.exploreState.exploreTargetPackages, ['app.a']);

            // 再クリックで解除
            row.dispatchEvent({type: 'click'});
            assert.deepEqual(PackageApp.exploreState.exploreTargetPackages, []);
        });
    });

});
