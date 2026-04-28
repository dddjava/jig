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
                const table = doc.createElement('table');
                table.id = 'explore-package-table';
                doc.elementsById.set('explore-package-table', table);

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
                const table = doc.createElement('table');
                table.id = 'explore-package-table';
                doc.elementsById.set('explore-package-table', table);

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

                PackageApp.exploreState.exploreCollapsedPackages = [];
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
                assert.equal(container.children.length, 2);
                // heading, list(ul)
                assert.equal(container.children[0].tagName, 'h3');
                assert.equal(container.children[1].tagName, 'ul');

                const li = container.children[1].children[0];
                // li has a tab section
                const tabSection = li.children[0];
                const tabsBar = tabSection.children[0];
                assert.equal(tabsBar.className, 'jig-tabs');
                // 3 tabs: 概要, ダイアグラム, テキスト
                assert.equal(tabsBar.children.length, 3);
                assert.equal(tabsBar.children[0].textContent, '概要');
                assert.equal(tabsBar.children[1].textContent, 'ダイアグラム');
                assert.equal(tabsBar.children[2].textContent, 'テキスト');
                // 概要パネルにペアラベルが表示される
                const overviewPanel = tabSection.children[1];
                assert.equal(overviewPanel.children[0].textContent, 'app.alpha <-> app.beta');
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
});
