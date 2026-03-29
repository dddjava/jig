const test = require('node:test');
const assert = require('node:assert/strict');
const { Element, DocumentStub, setGlossaryData } = require('./dom-stub.js');

const common = require('../../main/resources/templates/assets/jig-common.js');
const pkgDiagram = require('../../main/resources/templates/assets/package-diagram.js');

const pkg = require('../../main/resources/templates/assets/package.js');
const originalDom = {...pkg.dom};

let testContext;

function setupDocument() {
    const doc = new DocumentStub();
    global.document = doc;
    return doc;
}

function buildPackageRows(doc, fqns) {
    const rows = fqns.map(fqn => {
        const row = new Element('tr');
        const cell = new Element('td');
        cell.className = 'fqn';
        cell.textContent = fqn;
        row.appendChild(cell);
        return row;
    });
    doc.selectorsAll.set('#package-table tbody tr', rows);
    return rows;
}

function setPackageData(data, context) {
    globalThis.packageData = data;
    context.packageSummaryCache = null; // Reset cache
}

function setupDomMocks() {
    const methods = Object.keys(originalDom);
    methods.forEach(name => {
        if (typeof originalDom[name] !== 'function') return;
        test.mock.method(pkg.dom, name, test.mock.fn((...args) => originalDom[name](...args)));
    });
}

function createPackageFilterControls(doc) {
    const input = doc.createElement('textarea');
    input.id = 'package-filter-input';
    const applyButton = doc.createElement('button');
    applyButton.id = 'apply-package-filter';
    const clearButton = doc.createElement('button');
    clearButton.id = 'clear-package-filter';
    doc.elementsById.set('package-filter-input', input);
    doc.elementsById.set('apply-package-filter', applyButton);
    doc.elementsById.set('clear-package-filter', clearButton);
    return {input, applyButton, clearButton};
}

function createRelatedFilterControls(doc) {
    const select = doc.createElement('select');
    select.id = 'related-mode-select';
    const clearButton = doc.createElement('button');
    clearButton.id = 'clear-related-filter';
    doc.elementsById.set('related-mode-select', select);
    doc.elementsById.set('clear-related-filter', clearButton);
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

    globalThis.Jig.mermaid.renderWithControls = function() {};
    return diagram;
}

test.describe('package.js', () => {
    test.beforeEach(() => {
        // Reset the context for each test to ensure isolation
        testContext = {
            packageSummaryCache: null,
            diagramNodeIdToFqn: new Map(),
            aggregationDepth: 0,
            packageFilterFqn: [],
            focusCallerMode: '1',
            focusCalleeMode: '1',
            focusedPackageFqn: null,
            diagramDirection: 'TD',
            mutualDependencyDiagramDirection: 'LR',
            transitiveReductionEnabled: true,
        };
        setupDomMocks();
    });

    test.describe('データ取得/整形', () => {
        test.describe('ロジック', () => {
            test('parsePackageSummaryData: 配列/オブジェクトに対応する', () => {
                const arrayData = pkg.parsePackageSummaryData([
                    {fqn: 'app.a', classCount: 1},
                ]);
                assert.equal(arrayData.packages.length, 1);
                assert.equal(arrayData.relations.length, 0);

                const objectData = pkg.parsePackageSummaryData({
                    packages: [{fqn: 'app.b', classCount: 2}],
                    relations: [{from: 'app.b', to: 'app.c'}],
                });
                assert.equal(objectData.packages.length, 1);
                assert.equal(objectData.relations.length, 1);
            });

            test('getPackageSummaryData: 配列/オブジェクトに対応する', () => {
                setupDocument();
                setPackageData([{fqn: 'app.a', classCount: 1}], testContext);

                const data = pkg.getPackageSummaryData(testContext);

                assert.equal(data.packages.length, 1);
                assert.equal(data.relations.length, 0);
            });

            test('getPackageDepth: 深さを返す', () => {
                assert.equal(pkg.getPackageDepth(''), 0);
                assert.equal(pkg.getPackageDepth('(default)'), 0);
                assert.equal(pkg.getPackageDepth('app.domain'), 2);
            });

            test('getMaxPackageDepth: 最大深さを返す', () => {
                const doc = setupDocument();
                setPackageData({
                    packages: [
                        {fqn: 'app.domain.a'},
                        {fqn: 'app.b'},
                        {fqn: 'app.domain.core.c'},
                    ],
                    relations: [],
                }, testContext);

                assert.equal(pkg.getMaxPackageDepth(testContext), 4);
            });

            test('getCommonPrefixDepth: 共通プレフィックス深さを返す', () => {
                assert.equal(pkgDiagram.getCommonPrefixDepth([]), 0);
                assert.equal(pkgDiagram.getCommonPrefixDepth(['app.domain.a', 'app.domain.b']), 2);
                assert.equal(pkgDiagram.getCommonPrefixDepth(['app', 'lib.tool']), 0);
            });
        });
    });

    test.describe('集計', () => {
        test.describe('ロジック', () => {
            test('buildAggregationStatsForFilters: directモードの複合集計を行う', () => {
                const packages = [
                    {fqn: 'app.domain.a'},
                    {fqn: 'app.domain.b'},
                    {fqn: 'app.domain.c'},
                    {fqn: 'app.other.d'},
                ];
                const relations = [
                    {from: 'app.domain.a', to: 'app.domain.b'},
                    {from: 'app.domain.b', to: 'app.domain.c'},
                    {from: 'app.domain.c', to: 'app.other.d'},
                    {from: 'app.other.d', to: 'app.domain.a'},
                ];

                const stats = pkg.buildAggregationStatsForFilters(
                    packages,
                    relations,
                    ['app.domain'],
                    'app.domain.a',
                    0,
                    0,
                    '1', // relatedCallerFilterMode: direct
                    '1'  // relatedCalleeFilterMode: direct
                );
                const depth0 = stats.get(0);

                assert.equal(depth0.packageCount, 2);
                assert.equal(depth0.relationCount, 1);
            });

            test('buildAggregationStatsForFilters: allモードの複合集計を行う', () => {
                const packages = [
                    {fqn: 'app.domain.a'},
                    {fqn: 'app.domain.b'},
                    {fqn: 'app.domain.c'},
                    {fqn: 'app.other.d'},
                ];
                const relations = [
                    {from: 'app.domain.a', to: 'app.domain.b'},
                    {from: 'app.domain.b', to: 'app.domain.c'},
                    {from: 'app.domain.c', to: 'app.other.d'},
                    {from: 'app.other.d', to: 'app.domain.a'},
                ];

                const stats = pkg.buildAggregationStatsForFilters(
                    packages,
                    relations,
                    ['app.domain'],
                    'app.domain.a',
                    0,
                    0,
                    'all'
                );
                const depth0 = stats.get(0);

                assert.equal(depth0.packageCount, 3);
                assert.equal(depth0.relationCount, 2);
            });
        });
    });

    test.describe('フィルタ', () => {
        test.describe('ロジック', () => {
            test('normalizePackageFilterValue: 空文字または空白のみの文字列は空の配列を返す', () => {
                assert.deepEqual(pkg.normalizePackageFilterValue(''), []);
                assert.deepEqual(pkg.normalizePackageFilterValue('   '), []);
            });

            test('normalizePackageFilterValue: 改行区切りの文字列を配列として返す', () => {
                assert.deepEqual(pkg.normalizePackageFilterValue('app.domain\napp.other'), ['app.domain', 'app.other']);
                assert.deepEqual(pkg.normalizePackageFilterValue('  app.domain  \n  app.other  '), ['app.domain', 'app.other']);
                assert.deepEqual(pkg.normalizePackageFilterValue('app.domain\n\napp.other'), ['app.domain', 'app.other']);
            });

            test('normalizeAggregationDepthValue: 数値化する', () => {
                assert.equal(pkg.normalizeAggregationDepthValue('2'), 2);
                assert.equal(pkg.normalizeAggregationDepthValue('0'), 0);
                assert.equal(pkg.normalizeAggregationDepthValue('abc'), 0);
            });

            test('findDefaultPackageFilterCandidate: domainPackageRootsがあれば返す', () => {
                const candidate = pkg.findDefaultPackageFilterCandidate(['app.domain']);
                assert.equal(candidate, 'app.domain');
            });

            test('findDefaultPackageFilterCandidate: 複数のdomainPackageRootsを改行結合して返す', () => {
                const candidate = pkg.findDefaultPackageFilterCandidate(['com.a.domain', 'org.b.domain']);
                assert.equal(candidate, 'com.a.domain\norg.b.domain');
            });

            test('findDefaultPackageFilterCandidate: domainPackageRootsが空ならnullを返す', () => {
                assert.equal(pkg.findDefaultPackageFilterCandidate([]), null);
                assert.equal(pkg.findDefaultPackageFilterCandidate(null), null);
            });

            test('buildPackageRowVisibility: パッケージフィルタのみを表示する', () => {
                const visibility = pkg.buildPackageRowVisibility(
                    ['app.domain', 'app.other'],
                    ['app.domain']
                );
                assert.deepEqual(visibility, [true, false]);
            });

            test('buildPackageRowVisibility: 複数パッケージフィルタのうちいずれかに一致するものを表示する', () => {
                const visibility = pkg.buildPackageRowVisibility(
                    ['app.domain.model', 'app.domain.service', 'app.other'],
                    ['app.domain.model', 'app.other']
                );
                assert.deepEqual(visibility, [true, false, true]);
            });

            test('buildRelatedRowVisibility: 関連フィルタ未指定はパッケージフィルタのみを表示する', () => {
                const rowFqns = ['app.domain', 'app.other'];
                const visibility = pkg.buildFocusRowVisibility(
                    rowFqns,
                    [],
                    ['app.domain'],
                    0,
                    '1', // relatedCallerFilterMode
                    '0' // relatedCalleeFilterMode
                );
                assert.deepEqual(visibility, [true, false]);
            });

            test('buildRelatedRowVisibility: 関係する行のみ表示する', () => {
                const rowFqns = ['app.a', 'app.b', 'app.c'];
                const relations = [{from: 'app.a', to: 'app.b'}];
                const visibility = pkg.buildFocusRowVisibility(
                    rowFqns,
                    relations,
                    [],
                    0,
                    '0', // relatedCallerFilterMode
                    '1', // relatedCalleeFilterMode
                    'app.a'
                );
                assert.deepEqual(visibility, [true, true, false]);
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

                const related = pkg.collectFocusSet('app.domain.a', relations, aggregationDepth, '0', '1');

                assert.deepEqual(Array.from(related).sort(), ['app.domain.a', 'app.domain.b']);
            });

            test('collectFocusSet: allモードは推移的に辿る', () => {
                const aggregationDepth = 0;
                const relations = [
                    {from: 'app.domain.a', to: 'app.domain.b'},
                    {from: 'app.domain.b', to: 'app.domain.c'},
                ];

                const focusSet = pkg.collectFocusSet('app.domain.a', relations, aggregationDepth, '-1', '-1');

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
                const focusSet = pkg.collectFocusSet('app.a', relations, aggregationDepth, '0', '1');
                assert.deepEqual(Array.from(focusSet).sort(), ['app.a', 'app.b', 'app.d']); // a -> b, a -> d (direct callees)
            });

            test('collectFocusSet: 依存元直接、依存先なしの場合', () => {
                const aggregationDepth = 0;
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.c', to: 'app.a'},
                    {from: 'app.d', to: 'app.a'},
                ];
                const focusSet = pkg.collectFocusSet('app.a', relations, aggregationDepth, '1', '0');
                assert.deepEqual(Array.from(focusSet).sort(), ['app.a', 'app.c', 'app.d']); // c -> a, d -> a (direct callers)
            });

            test('buildVisibleDiagramRelations: パッケージフィルタを適用する', () => {
                const base = pkg.buildVisibleDiagramRelations(packages, relations, [], ['app'], 0, false);
                assert.deepEqual(Array.from(base.packageFqns).sort(), ['app.a', 'app.b', 'app.c']);
                assert.equal(base.uniqueRelations.length, 2);
            });

            test('filterFocusDiagramRelations: relatedSetで絞り込む', () => {
                const base = pkg.buildVisibleDiagramRelations(packages, relations, [], [], 0, false);
                const filtered = pkg.filterFocusDiagramRelations(
                    base.uniqueRelations,
                    base.packageFqns,
                    'app.b',
                    0,
                    '1', // focusCallerMode
                    '1'  // focusCalleeMode
                );
                assert.deepEqual(Array.from(filtered.packageFqns).sort(), ['app.a', 'app.b', 'app.c']);
                assert.equal(filtered.uniqueRelations.length, 2);
            });

            test('filterFocusDiagramRelations: 依存元なし、依存先なしの場合', () => {
                const packages = [
                    {fqn: 'app.a'}, {fqn: 'app.b'}, {fqn: 'app.c'}
                ];
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.b', to: 'app.c'}
                ];
                const base = pkg.buildVisibleDiagramRelations(packages, relations, [], [], 0, false);
                const filtered = pkg.filterFocusDiagramRelations(
                    base.uniqueRelations,
                    base.packageFqns,
                    'app.b',
                    0,
                    '0', // focusCallerMode: なし
                    '0'  // focusCalleeMode: なし
                );
                assert.deepEqual(Array.from(filtered.packageFqns).sort(), ['app.b']); // Only the focused package
                assert.equal(filtered.uniqueRelations.length, 0); // No relations
            });

            test('filterFocusDiagramRelations: 依存元すべて、依存先なしの場合', () => {
                const packages = [
                    {fqn: 'app.a'}, {fqn: 'app.b'}, {fqn: 'app.c'}
                ];
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.b', to: 'app.c'}
                ];
                const base = pkg.buildVisibleDiagramRelations(packages, relations, [], [], 0, false);
                const filtered = pkg.filterFocusDiagramRelations(
                    base.uniqueRelations,
                    base.packageFqns,
                    'app.b',
                    0,
                    '-1', // focusCallerMode: すべて
                    '0'  // focusCalleeMode: なし
                );
                assert.deepEqual(Array.from(filtered.packageFqns).sort(), ['app.a', 'app.b']); // a -> b (all callers)
                assert.deepEqual(filtered.uniqueRelations.map(r => `${r.from}>${r.to}`), ['app.a>app.b']);
            });

            test('filterFocusDiagramRelations: 依存元なし、依存先すべての場合', () => {
                const packages = [
                    {fqn: 'app.a'}, {fqn: 'app.b'}, {fqn: 'app.c'}
                ];
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.b', to: 'app.c'}
                ];
                const base = pkg.buildVisibleDiagramRelations(packages, relations, [], [], 0, false);
                const filtered = pkg.filterFocusDiagramRelations(
                    base.uniqueRelations,
                    base.packageFqns,
                    'app.b',
                    0,
                    '0', // focusCallerMode: なし
                    '-1'  // focusCalleeMode: すべて
                );
                assert.deepEqual(Array.from(filtered.packageFqns).sort(), ['app.b', 'app.c']); // b -> c (all callees)
                assert.deepEqual(filtered.uniqueRelations.map(r => `${r.from}>${r.to}`), ['app.b>app.c']);
            });

            test('filterFocusDiagramRelations: 依存元すべて、依存先直接の場合', () => {
                const packages = [
                    {fqn: 'app.a'}, {fqn: 'app.b'}, {fqn: 'app.c'}, {fqn: 'app.d'}
                ];
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.b', to: 'app.c'},
                    {from: 'app.d', to: 'app.b'},
                ];
                const base = pkg.buildVisibleDiagramRelations(packages, relations, [], [], 0, false);
                const filtered = pkg.filterFocusDiagramRelations(
                    base.uniqueRelations,
                    base.packageFqns,
                    'app.b', // focus target
                    0,       // aggregation depth
                    '-1',    // focusCallerMode: すべて
                    '1'      // focusCalleeMode: 直接
                );
                assert.deepEqual(Array.from(filtered.packageFqns).sort(), ['app.a', 'app.b', 'app.c', 'app.d']);
                assert.deepEqual(filtered.uniqueRelations.map(r => `${r.from}>${r.to}`).sort(), ['app.a>app.b', 'app.b>app.c', 'app.d>app.b']);
            });

            test('filterFocusDiagramRelations: 依存元直接、依存先すべての場合', () => {
                const packages = [
                    {fqn: 'app.a'}, {fqn: 'app.b'}, {fqn: 'app.c'}, {fqn: 'app.d'}
                ];
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.b', to: 'app.c'},
                    {from: 'app.c', to: 'app.d'},
                ];
                const base = pkg.buildVisibleDiagramRelations(packages, relations, [], [], 0, false);
                const filtered = pkg.filterFocusDiagramRelations(
                    base.uniqueRelations,
                    base.packageFqns,
                    'app.b', // focus target
                    0,       // aggregation depth
                    '1',     // focusCallerMode: 直接
                    '-1'     // focusCalleeMode: すべて
                );
                assert.deepEqual(Array.from(filtered.packageFqns).sort(), ['app.a', 'app.b', 'app.c', 'app.d']);
                assert.deepEqual(filtered.uniqueRelations.map(r => `${r.from}>${r.to}`).sort(), ['app.a>app.b', 'app.b>app.c', 'app.c>app.d']);
            });

            test('buildVisibleDiagramElements: packageFilterは配下のみを表示する', () => {
                const {packageFqns} = pkg.buildVisibleDiagramElements(packages, relations, [], ['app'], null, 0, '1', '1');
                assert.deepEqual(Array.from(packageFqns).sort(), ['app.a', 'app.b', 'app.c']);
            });

            test('buildVisibleDiagramElements: relatedFilter(direct)は隣接のみを表示する', () => {
                const {packageFqns} = pkg.buildVisibleDiagramElements(packages, relations, [], [], 'app.b', 0, '1', '1', false);
                assert.deepEqual(Array.from(packageFqns).sort(), ['app.a', 'app.b', 'app.c']);
            });

            test('buildVisibleDiagramElements: relatedFilter(all)は到達可能なものを表示する', () => {
                const {packageFqns} = pkg.buildVisibleDiagramElements(packages, relations, [], [], 'app.a', 0, '-1', '-1');
                assert.deepEqual(Array.from(packageFqns).sort(), ['app.a', 'app.b', 'app.c', 'lib.d']);
            });
        });

        test.describe('UI', () => {
            test('filterRelatedTableRows: 関係する行のみ表示する', () => {
                const doc = setupDocument();
                setPackageData({
                    packages: [
                        {fqn: 'app.a'},
                        {fqn: 'app.b'},
                        {fqn: 'app.c'},
                    ],
                    relations: [
                        {from: 'app.a', to: 'app.b'},
                    ],
                }, testContext);
                const rows = buildPackageRows(doc, ['app.a', 'app.b', 'app.c']);
                testContext.aggregationDepth = 0;
                testContext.relatedCallerFilterMode = '1';
                testContext.relatedCalleeFilterMode = '1';
                testContext.packageFilterFqn = [];

                pkg.filterFocusTableRows('app.a', testContext);

                assert.equal(rows[0].classList.contains('hidden'), false);
                assert.equal(rows[1].classList.contains('hidden'), false);
                assert.equal(rows[2].classList.contains('hidden'), true);
            });

            test('renderFocusLabel: 対象表示を更新する', () => {
                const mockTarget = { textContent: '' };
                const getFocusTargetMock = test.mock.fn(() => mockTarget);
                const setFocusTargetTextMock = test.mock.fn((element, text) => { element.textContent = text; });

                test.mock.method(pkg.dom, 'getFocusTarget', getFocusTargetMock);
                test.mock.method(pkg.dom, 'setFocusTargetText', setFocusTargetTextMock);

                testContext.focusedPackageFqn = null;
                pkg.renderFocusLabel(testContext);
                assert.equal(mockTarget.textContent, '未選択');
                assert.equal(setFocusTargetTextMock.mock.calls.length, 1);
                assert.deepEqual(setFocusTargetTextMock.mock.calls[0].arguments, [mockTarget, '未選択']);

                testContext.focusedPackageFqn = 'app.domain';
                pkg.renderFocusLabel(testContext);
                assert.equal(mockTarget.textContent, 'app.domain');
                assert.equal(setFocusTargetTextMock.mock.calls.length, 2);
                assert.deepEqual(setFocusTargetTextMock.mock.calls[1].arguments, [mockTarget, 'app.domain']);
            });

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
                doc.selectorsAll.set('#package-table tbody tr', []);
                const {input} = createPackageFilterControls(doc);
                createDepthSelect(doc); // for renderDiagramAndTable

                const applied = pkg.applyDefaultPackageFilterIfPresent(testContext);

                assert.equal(applied, true);
                assert.deepEqual(testContext.packageFilterFqn, ['app.domain']);
                assert.equal(input.value, 'app.domain');
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
                doc.selectorsAll.set('#package-table tbody tr', []);
                createPackageFilterControls(doc);
                createDepthSelect(doc);

                const applied = pkg.applyDefaultPackageFilterIfPresent(testContext);

                assert.equal(applied, false);
            });

            test('setupPackageFilterControl: 適用/解除を扱う', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [{fqn: 'app.domain', classCount: 1}],
                    relations: [],
                }, testContext);
                doc.selectorsAll.set('#package-table tbody tr', []);
                createDepthSelect(doc);

                const {input, applyButton, clearButton} = createPackageFilterControls(doc);

                pkg.setupPackageFilterControl(testContext);

                input.value = 'app.domain\napp.other';
                applyButton.dispatchEvent({ type: 'click' });
                assert.deepEqual(testContext.packageFilterFqn, ['app.domain', 'app.other']);

                clearButton.dispatchEvent({ type: 'click' });
                assert.deepEqual(testContext.packageFilterFqn, []);
                assert.equal(input.value, '');
            });
        });
    });

    test.describe('テーブル', () => {
        test.describe('ロジック', () => {
            test('buildPackageTableRowSpecs: 行データを整形する', () => {
                setGlossaryData( {'app.a': {title: 'A', simpleText: 'a', kind: 'パッケージ', description: ''}});
                const rows = [
                    {fqn: 'app.a', classCount: 2, incomingCount: 0, outgoingCount: 1},
                ];

                const specs = pkg.buildPackageTableRowSpecs(rows);

                assert.deepEqual(specs, [{
                    fqn: 'app.a',
                    name: 'A',
                    classCount: 2,
                    incomingCount: 0,
                    outgoingCount: 1,
                }]);
                delete globalThis.glossaryData;
            });

            test('buildPackageTableActionSpecs: ボタン文言を返す', () => {
                const specs = pkg.buildPackageTableActionSpecs();

                assert.equal(specs.filter.ariaLabel, 'このパッケージで絞り込み');
                assert.equal(specs.filter.screenReaderText, '絞り込み');
                assert.equal(specs.focus.ariaLabel, 'フォーカス');
                assert.equal(specs.focus.screenReaderText, 'フォーカス');
            });
        });

        test.describe('UI', () => {
            test('renderPackageTable: 行とカウントを描画する', () => {
                setGlossaryData( {
                    'app.a': {title: 'A', simpleText: 'a', kind: 'パッケージ', description: ''},
                    'app.b': {title: 'B', simpleText: 'b', kind: 'パッケージ', description: ''},
                });
                const doc = setupDocument();
                setPackageData({
                    packages: [
                        {fqn: 'app.a', classCount: 2},
                        {fqn: 'app.b', classCount: 1},
                    ],
                    relations: [
                        {from: 'app.a', to: 'app.b'},
                        {from: 'app.a', to: 'app.b'},
                    ],
                }, testContext);
                const tbody = new Element('tbody', doc);
                doc.selectors.set('#package-table tbody', tbody);

                pkg.renderPackageTable(testContext);

                assert.equal(tbody.children.length, 2);
                assert.equal(tbody.children[0].children[3].textContent, 'A');
                assert.equal(tbody.children[0].children[5].textContent, '2');
                assert.equal(tbody.children[0].children[6].textContent, '0');
                assert.equal(tbody.children[0].children[7].textContent, '2');
                delete globalThis.glossaryData;
            });
        });
    });

    test.describe('ダイアグラム', () => {
        test.describe('ロジック', () => {
            test('buildMutualDependencyItems: 相互依存の原因を整形する', () => {
                const items = pkg.buildMutualDependencyItems(
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
                const {source} = pkg.buildMutualDependencyDiagramSource(causes, 'LR', 'a.b.c.d <-> a.b.x.y');
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
                const {source} = pkg.buildMutualDependencyDiagramSource(causes, 'LR', 'a.b.c <-> a.b.c.d');
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
                const {source} = pkg.buildMutualDependencyDiagramSource(
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

            test('buildDiagramEdgeLines: 相互依存の双方向リンクを生成する', () => {
                const {ensureNodeId} = pkgDiagram.buildDiagramNodeMaps(new Set(['a', 'b']), new Map());
                const result = pkgDiagram.buildDiagramEdgeLines(
                    [{from: 'a', to: 'b'}, {from: 'b', to: 'a'}],
                    ensureNodeId
                );
                assert.equal(result.edgeLines.some(line => line.includes('<-->')), true);
                assert.equal(result.linkStyles.length, 1);
            });

            test('buildDiagramNodeLabel: サブグラフ配下のラベルを短縮する', () => {
                const label = pkgDiagram.buildDiagramNodeLabel(
                    'com.example.domain.model',
                    'com.example.domain.model',
                    'com.example.domain'
                );
                assert.equal(label, 'model');
            });

            test('buildDiagramSubgraphLabel: 親サブグラフ配下ならプレフィックスを省略する', () => {
                const label = pkgDiagram.buildDiagramSubgraphLabel('com.example.domain', 'com.example');
                assert.equal(label, 'domain');
            });

            test('buildDiagramNodeTooltip: FQNを返す', () => {
                assert.equal(pkgDiagram.buildDiagramNodeTooltip('com.example.domain'), 'com.example.domain');
                assert.equal(pkgDiagram.buildDiagramNodeTooltip(null), '');
            });

            test('buildDiagramGroupTree: 共通プレフィックスでグループ化する', () => {
                const visibleFqns = ['com.example.a', 'com.example.b'];
                const nodeIdByFqn = new Map([
                    ['com.example.a', 'P0'],
                    ['com.example.b', 'P1'],
                ]);

                const rootGroup = pkgDiagram.buildDiagramGroupTree(visibleFqns, nodeIdByFqn);

                assert.equal(rootGroup.children.has('com.example'), true);
            });

            test('buildSubgraphLines: サブグラフ行を生成する', () => {
                const rootGroup = {
                    key: '',
                    nodes: ['ROOT'],
                    children: new Map([
                        ['com.example', {
                            key: 'com.example',
                            nodes: ['P0'],
                            children: new Map([
                                ['com.example.domain', {key: 'com.example.domain', nodes: ['P1', 'P2'], children: new Map()}],
                            ]),
                        }],
                    ]),
                };
                const addNodeLines = (lines, nodeId) => {
                    lines.push(`node ${nodeId}`);
                };

                const lines = pkgDiagram.buildSubgraphLines(rootGroup, addNodeLines, text => text);

                assert.equal(lines.some(line => line.includes('node ROOT')), true);
                assert.equal(lines.some(line => line.includes('node P0')), true);
                assert.equal(lines.some(line => line.includes('subgraph') && line.includes('["com.example"]')), true);
                assert.equal(lines.some(line => line.includes('subgraph') && line.includes('["domain"]')), true);
            });

            test('buildAggregationDepthOptions: 集約オプションを組み立てる', () => {
                const stats = new Map([
                    [0, {packageCount: 2, relationCount: 1}],
                    [1, {packageCount: 1, relationCount: 1}],
                    [2, {packageCount: 1, relationCount: 0}],
                ]);

                const options = pkg.buildAggregationDepthOptions(stats, 2);

                assert.deepEqual(options, [
                    {value: '0', text: '集約なし（P2 / R1）'},
                    {value: '1', text: '深さ1（P1 / R1）'},
                ]);
            });

            test('buildDiagramNodeLines: クリックハンドラ名を埋め込む', () => {
                const visibleSet = new Set(['app.a']);
                const {nodeIdByFqn, nodeIdToFqn, nodeLabelById} = pkgDiagram.buildDiagramNodeMaps(visibleSet, new Map([['app.a', 'A']]));
                const nodeLines = pkgDiagram.buildDiagramNodeLines(
                    visibleSet,
                    nodeIdByFqn,
                    nodeIdToFqn,
                    nodeLabelById,
                    text => text,
                    pkg.DIAGRAM_CLICK_HANDLER_NAME
                );
                const clickLine = nodeLines.find(line => line.startsWith('click '));
                assert.ok(clickLine);
                assert.equal(clickLine.includes(pkg.DIAGRAM_CLICK_HANDLER_NAME), true);
            });
        });

        test.describe('UI', () => {
            test('renderMutualDependencyList: 相互依存と原因を一覧化する', () => {
                const doc = setupDocument();
                const container = new Element('div', doc);
                doc.elementsById.set('mutual-dependency-list', container);
                testContext.aggregationDepth = 0;

                pkg.renderMutualDependencyList(
                    new Set(['app.alpha::app.beta']),
                    [
                        {from: 'app.alpha.A', to: 'app.beta.B'},
                        {from: 'app.beta.B', to: 'app.alpha.A'},
                    ],
                    testContext.aggregationDepth,
                    testContext
                );

                assert.equal(container.style.display, '');
                assert.equal(container.children.length, 1);
                const details = container.children[0];
                assert.equal(details.tagName, 'details');
                // summary, settingsRow(div), list(ul)
                assert.equal(details.children[0].tagName, 'summary');
                assert.equal(details.children[1].className, 'control-row');
                assert.equal(details.children[2].tagName, 'ul');

                const li = details.children[2].children[0];
                assert.equal(li.children[0].className, 'pair');
                assert.equal(li.children[1].className, 'causes');
                assert.equal(li.children[2].className, 'diagram-button');
                assert.equal(li.children[3].className, 'mermaid mutual-dependency-diagram');
            });

            test('renderMutualDependencyList: 図の向きを変更するとcontextが更新される', () => {
                const doc = setupDocument();
                const container = new Element('div', doc);
                doc.elementsById.set('mutual-dependency-list', container);

                pkg.renderMutualDependencyList(
                    new Set(['a::b']),
                    [{from: 'a.A', to: 'b.B'}, {from: 'b.B', to: 'a.A'}],
                    0,
                    testContext
                );

                const details = container.children[0];
                const settingsRow = details.children[1];
                const tdRadio = settingsRow.children[1].children[0]; // 縦 TD
                const lrRadio = settingsRow.children[2].children[0]; // 横 LR

                assert.equal(testContext.mutualDependencyDiagramDirection, 'LR');
                assert.equal(lrRadio.checked, true);

                tdRadio.checked = true;
                tdRadio.dispatchEvent({ type: 'change' });

                assert.equal(testContext.mutualDependencyDiagramDirection, 'TD');
            });

            test('renderMutualDependencyDiagram: 描画後にボタンを非表示にする', () => {
                const doc = setupDocument();
                const itemNode = new Element('li', doc);
                const diagram = new Element('pre', doc);
                diagram.className = 'mutual-dependency-diagram';
                itemNode.appendChild(diagram);
                const button = new Element('button', doc);
                button.className = 'diagram-button';
                button.style = {}; // Initialize style object
                itemNode.appendChild(button);

                const renderWithControls = test.mock.fn(() => {});
                globalThis.Jig.mermaid.renderWithControls = renderWithControls;

                const item = { causes: ['a.A -> b.B'] };
                pkg.renderMutualDependencyDiagram(item, itemNode, testContext);

                assert.equal(button.style.display, 'none');
                assert.ok(diagram.style.display === 'block' || diagram.style.display === ''); // Mermaid rendering might change this
                assert.equal(renderWithControls.mock.calls.length, 1);
            });

            test('renderPackageDiagram: 相互依存を含めて描画する', () => {
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

                pkg.renderPackageDiagram(testContext, [], null);

                const diagram = doc.getElementById('package-relation-diagram');
                assert.equal(diagram.textContent.includes('graph'), true);
                assert.equal(diagram.textContent.includes('<-->'), true);
                const mutual = doc.getElementById('mutual-dependency-list');
                assert.equal(mutual.children.length > 0, true);
            });

            test('renderPackageDiagram: エッジ数超過でもrenderWithControlsにedgeCountを渡す', () => {
                const doc = setupDocument();
                const diagramMock = setupDiagramEnvironment(doc, testContext);
                const renderWithControls = test.mock.fn(() => {});

                // renderWithControlsへの委譲をキャプチャする
                let capturedRenderArgs = null;
                globalThis.Jig.mermaid.renderWithControls = (el, text, opts) => {
                    capturedRenderArgs = { el, text, opts };
                };

                const packages = [];
                const relations = [];
                for (let i = 0; i < 501; i += 1) {
                    const from = `app.p${i}`;
                    const to = `app.p${i + 1}`;
                    packages.push({fqn: from, classCount: 1});
                    packages.push({fqn: to, classCount: 1});
                    relations.push({from, to});
                }
                setPackageData({packages, relations}, testContext);

                pkg.renderPackageDiagram(testContext, [], null);

                assert.ok(capturedRenderArgs, 'renderWithControlsが呼ばれること');
                assert.equal(capturedRenderArgs.el, diagramMock, 'diagramが渡されること');
                assert.ok(capturedRenderArgs.opts.edgeCount > 500, `edgeCountが500超であること: ${capturedRenderArgs.opts.edgeCount}`);
            });

            test('registerDiagramClickHandler: クリックで関連フィルタへ切り替える', () => {
                global.window = {};
                testContext.diagramNodeIdToFqn = new Map([['P1', 'app.example']]);
                let called = null;
                const applyFocus = (fqn, context) => {
                    called = {fqn, context};
                };

                pkg.registerDiagramClickHandler(testContext, applyFocus);

                global.window[pkg.DIAGRAM_CLICK_HANDLER_NAME]('P1');

                assert.deepEqual(called, {fqn: 'app.example', context: testContext});
            });

            test('setupTransitiveReductionControl: UIをセットアップする', () => {
                const doc = setupDocument();
                const container = doc.createElement('div');
                const pp = doc.createElement('div');
                const input = doc.createElement('input');
                input.name = 'diagram-direction';
                pp.appendChild(input);
                container.appendChild(pp);
                doc.selectors.set('input[name="diagram-direction"]', input);
                input.parentNode = pp;
                pp.parentNode = container;

                // renderDiagramAndTableの副作用をチェックするための準備
                setupDiagramEnvironment(doc, testContext);
                setPackageData({packages: [{fqn: 'a'}], relations: []}, testContext);
                const depthSelect = createDepthSelect(doc);
                const dummyOption = doc.createElement('option');
                dummyOption.id = 'dummy-option-for-test';
                depthSelect.appendChild(dummyOption);
                doc.selectorsAll.set('#package-table tbody tr', []);


                pkg.setupTransitiveReductionControl(testContext);

                const checkbox = doc.getElementById('transitive-reduction-toggle');
                assert.ok(checkbox, 'checkbox should be created');
                assert.equal(checkbox.checked, true);
                assert.equal(testContext.transitiveReductionEnabled, true);

                // changeイベントを発火させる
                checkbox.checked = false;
                checkbox.dispatchEvent({ type: 'change' });

                assert.equal(testContext.transitiveReductionEnabled, false);
            });
        });
    });
});
