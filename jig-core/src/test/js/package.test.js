const test = require('node:test');
const assert = require('node:assert/strict');

const pkg = require('../../main/resources/templates/assets/package.js');
const originalDom = {...pkg.dom};

// Creates a fresh deep copy of the initial context for each test
function createInitialContext() {
    return JSON.parse(JSON.stringify(pkg.packageContext));
}

let testContext;

class ClassList {
    constructor() {
        this.values = new Set();
    }

    add(value) {
        this.values.add(value);
    }

    remove(value) {
        this.values.delete(value);
    }

    toggle(value, force) {
        if (force === undefined) {
            if (this.values.has(value)) {
                this.values.delete(value);
                return false;
            }
            this.values.add(value);
            return true;
        }
        if (force) {
            this.values.add(value);
        } else {
            this.values.delete(value);
        }
        return force;
    }

    contains(value) {
        return this.values.has(value);
    }
}

class Element {
    constructor(tagName, ownerDocument = null) {
        this.tagName = tagName;
        this.children = [];
        this.textContent = '';
        this.className = '';
        this.classList = new ClassList();
        this.style = {};
        this.attributes = new Map();
        this.parentNode = null;
        this.ownerDocument = ownerDocument;
        this.value = '';
        this.eventListeners = new Map();
        let elementId = '';
        let inner = '';
        Object.defineProperty(this, 'innerHTML', {
            get() {
                return inner;
            },
            set(value) {
                inner = value;
                if (value === '') {
                    this.children = [];
                }
            },
        });
        Object.defineProperty(this, 'id', {
            get() {
                return elementId;
            },
            set(value) {
                elementId = value;
                if (this.ownerDocument) {
                    this.ownerDocument.elementsById.set(value, this);
                }
            },
        });
    }

    appendChild(child) {
        child.parentNode = this;
        this.children.push(child);
        return child;
    }

    insertBefore(child, referenceNode) {
        child.parentNode = this;
        if (!referenceNode) {
            this.children.push(child);
            return child;
        }
        const index = this.children.indexOf(referenceNode);
        if (index === -1) {
            this.children.push(child);
            return child;
        }
        this.children.splice(index, 0, child);
        return child;
    }

    setAttribute(name, value) {
        this.attributes.set(name, String(value));
    }

    removeAttribute(name) {
        this.attributes.delete(name);
    }

    querySelector(selector) {
        if (selector === 'td.fqn') {
            return this.children.find(
                child => child.tagName === 'td' && child.className.split(' ').includes('fqn')
            ) || null;
        }
        return null;
    }

    addEventListener(eventName, handler) {
        this.eventListeners.set(eventName, handler);
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.selectors = new Map();
        this.selectorsAll = new Map();
    }

    createElement(tagName) {
        return new Element(tagName, this);
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }

    querySelector(selector) {
        return this.selectors.get(selector) || null;
    }

    querySelectorAll(selector) {
        return this.selectorsAll.get(selector) || [];
    }
}

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
    const mockDataContent = JSON.stringify(data);
    const doc = global.document;
    if (doc && doc.elementsById) {
        const dataElement = new Element('script', doc);
        dataElement.textContent = mockDataContent;
        doc.elementsById.set('package-data', dataElement);
    } else {
        const mockDataElement = { textContent: mockDataContent };
        test.mock.method(pkg.dom, 'getPackageDataScript', test.mock.fn(() => mockDataElement));
        test.mock.method(pkg.dom, 'getNodeTextContent', test.mock.fn((el) => el.textContent));
    }

    context.packageSummaryCache = null; // Reset cache
}

function setupDomMocks() {
    const methods = Object.keys(originalDom);
    methods.forEach(name => {
        if (typeof originalDom[name] !== 'function') return;
        test.mock.method(pkg.dom, name, test.mock.fn((...args) => originalDom[name](...args)));
    });
}

function withConsoleErrorCapture(callback) {
    const errors = [];
    const originalError = console.error;
    console.error = (...args) => {
        errors.push(args.map(arg => String(arg)).join(' '));
    };
    try {
        callback();
    } finally {
        console.error = originalError;
    }
    return errors;
}

function createPackageFilterControls(doc) {
    const input = doc.createElement('input');
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
    context.diagramElement = diagram;
    const mutual = doc.createElement('div');
    mutual.id = 'mutual-dependency-list';
    doc.elementsById.set('mutual-dependency-list', mutual);
    global.window = {
        mermaid: {
            initialize() {
            },
            run() {
            },
        },
    };
    global.mermaid = global.window.mermaid;
    return diagram;
}

test.describe('package.js', () => {
    test.beforeEach(() => {
        // Reset the context for each test to ensure isolation
        testContext = {
            packageSummaryCache: null,
            diagramNodeIdToFqn: new Map(),
            aggregationDepth: 0,
            diagramElement: null,
            pendingDiagramRender: null,
            lastDiagramSource: '',
            lastDiagramEdgeCount: 0,
            DEFAULT_MAX_EDGES: 500,
            packageFilterFqn: null,
            relatedFilterMode: 'direct',
            relatedFilterFqn: null,
            diagramDirection: 'TD',
            transitiveReductionEnabled: true,
        };
        setupDomMocks();
    });

    test.describe('データ/ヘルパー', () => {
        test.describe('collectRelatedSet', () => {
            test('directモード: 隣接のみを含める', () => {
                const aggregationDepth = 0;
                const relatedFilterMode = 'direct';
                const relations = [
                    {from: 'app.domain.a', to: 'app.domain.b'},
                    {from: 'app.domain.b', to: 'app.domain.c'},
                ];

                const related = pkg.collectRelatedSet('app.domain.a', relations, aggregationDepth, relatedFilterMode);

                assert.deepEqual(Array.from(related).sort(), ['app.domain.a', 'app.domain.b']);
            });

            test('allモード: 推移的に辿る', () => {
                const aggregationDepth = 0;
                const relatedFilterMode = 'all';
                const relations = [
                    {from: 'app.domain.a', to: 'app.domain.b'},
                    {from: 'app.domain.b', to: 'app.domain.c'},
                ];

                const related = pkg.collectRelatedSet('app.domain.a', relations, aggregationDepth, relatedFilterMode);

                assert.deepEqual(
                    Array.from(related).sort(),
                    ['app.domain.a', 'app.domain.b', 'app.domain.c']
                );
            });
        });

        test.describe('データ取得', () => {
            test('getPackageSummaryData: 配列/オブジェクト両対応', () => {
                setupDocument();
                setPackageData([{fqn: 'app.a', name: 'A', classCount: 1, description: ''}], testContext);

                const data = pkg.getPackageSummaryData(testContext);

                assert.equal(data.packages.length, 1);
                assert.equal(data.relations.length, 0);
            });

            test('parsePackageSummaryData: 配列/オブジェクト両対応', () => {
                const arrayData = pkg.parsePackageSummaryData(JSON.stringify([
                    {fqn: 'app.a', name: 'A', classCount: 1, description: ''},
                ]));
                assert.equal(arrayData.packages.length, 1);
                assert.equal(arrayData.relations.length, 0);

                const objectData = pkg.parsePackageSummaryData(JSON.stringify({
                    packages: [{fqn: 'app.b', name: 'B', classCount: 2, description: ''}],
                    relations: [{from: 'app.b', to: 'app.c'}],
                }));
                assert.equal(objectData.packages.length, 1);
                assert.equal(objectData.relations.length, 1);
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
                assert.equal(pkg.getCommonPrefixDepth([]), 0);
                assert.equal(pkg.getCommonPrefixDepth(['app.domain.a', 'app.domain.b']), 2);
                assert.equal(pkg.getCommonPrefixDepth(['app', 'lib.tool']), 0);
            });
        });
    });

    test.describe('集計', () => {
        test('buildAggregationStatsForPackageFilter: 対象のみ数える', () => {
            testContext.aggregationDepth = 0;
            const packages = [
                {fqn: 'app.domain.a'},
                {fqn: 'app.domain.b'},
                {fqn: 'app.other.c'},
            ];
            const relations = [
                {from: 'app.domain.a', to: 'app.domain.b'},
                {from: 'app.other.c', to: 'app.domain.a'},
            ];

            const stats = pkg.buildAggregationStatsForPackageFilter(packages, relations, 'app.domain', 0);
            const depth0 = stats.get(0);

            assert.equal(depth0.packageCount, 2);
            assert.equal(depth0.relationCount, 1);
        });

        test('buildAggregationStatsForRelated: 集計深さを反映する', () => {
            const aggregationDepth = 1;
            const relatedFilterMode = 'all';
            const packages = [
                {fqn: 'app.domain.a'},
                {fqn: 'app.domain.b'},
                {fqn: 'app.other.c'},
            ];
            const relations = [
                {from: 'app.domain.a', to: 'app.domain.b'},
                {from: 'app.domain.b', to: 'app.other.c'},
            ];

            const stats = pkg.buildAggregationStatsForRelated(packages, relations, 'app.domain.a', 1, aggregationDepth, relatedFilterMode);
            const depth1 = stats.get(1);

            assert.equal(depth1.packageCount, 1);
            assert.equal(depth1.relationCount, 0);
        });

        test('buildAggregationStatsForFilters: directモードの複合集計', () => {
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
                'app.domain',
                'app.domain.a',
                0,
                0,
                'direct'
            );
            const depth0 = stats.get(0);

            assert.equal(depth0.packageCount, 2);
            assert.equal(depth0.relationCount, 1);
        });

        test('buildAggregationStatsForFilters: allモードの複合集計', () => {
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
                'app.domain',
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

    test.describe('フィルタ', () => {
        test.describe('getVisibleDiagramElements', () => {
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

            test('packageFilter: 指定パッケージ配下のみ表示', () => {
                const {visibleSet} = pkg.getVisibleDiagramElements(packages, relations, [], 'app', null, 0, 'direct', false);
                assert.deepEqual(Array.from(visibleSet).sort(), ['app.a', 'app.b', 'app.c']);
            });

            test('relatedFilter(direct): 指定パッケージの隣接のみ表示', () => {
                const {visibleSet} = pkg.getVisibleDiagramElements(packages, relations, [], null, 'app.b', 0, 'direct', false);
                assert.deepEqual(Array.from(visibleSet).sort(), ['app.a', 'app.b', 'app.c']);
            });

            test('relatedFilter(all): 指定パッケージから到達可能なものすべて表示', () => {
                const {visibleSet} = pkg.getVisibleDiagramElements(packages, relations, [], null, 'app.a', 0, 'all', false);
                assert.deepEqual(Array.from(visibleSet).sort(), ['app.a', 'app.b', 'app.c', 'lib.d']);
            });

            test('buildFilteredDiagramRelations: パッケージフィルタを適用する', () => {
                const base = pkg.buildFilteredDiagramRelations(packages, relations, [], 'app', 0, false);
                assert.deepEqual(Array.from(base.visibleSet).sort(), ['app.a', 'app.b', 'app.c']);
                assert.equal(base.uniqueRelations.length, 2);
            });

            test('applyRelatedFilterToDiagramRelations: relatedSetで絞り込む', () => {
                const base = pkg.buildFilteredDiagramRelations(packages, relations, [], null, 0, false);
                const filtered = pkg.applyRelatedFilterToDiagramRelations(
                    base.uniqueRelations,
                    base.visibleSet,
                    'app.b',
                    0,
                    'direct'
                );
                assert.deepEqual(Array.from(filtered.visibleSet).sort(), ['app.a', 'app.b', 'app.c']);
                assert.equal(filtered.uniqueRelations.length, 2);
            });

            test('buildDiagramEdgeLines: 相互依存で双方向リンクを生成', () => {
                const {ensureNodeId} = pkg.buildDiagramNodeMaps(new Set(['a', 'b']), new Map());
                const result = pkg.buildDiagramEdgeLines(
                    [{from: 'a', to: 'b'}, {from: 'b', to: 'a'}],
                    ensureNodeId
                );
                assert.equal(result.edgeLines.some(line => line.includes('<-->')), true);
                assert.equal(result.linkStyles.length, 1);
            });

            test('buildDiagramGroupTree: 共通プレフィックスでグループ化する', () => {
                const visibleFqns = ['com.example.a', 'com.example.b'];
                const nodeIdByFqn = new Map([
                    ['com.example.a', 'P0'],
                    ['com.example.b', 'P1'],
                ]);

                const rootGroup = pkg.buildDiagramGroupTree(visibleFqns, nodeIdByFqn);

                assert.equal(rootGroup.children.has('com.example'), true);
            });

            test('buildSubgraphLines: サブグラフ行を生成する', () => {
                const rootGroup = {
                    key: '',
                    nodes: [],
                    children: new Map([
                        ['com.example', {key: 'com.example', nodes: ['P0', 'P1'], children: new Map()}],
                    ]),
                };
                const addNodeLines = (lines, nodeId) => {
                    lines.push(`node ${nodeId}`);
                };

                const lines = pkg.buildSubgraphLines(rootGroup, addNodeLines, text => text);

                assert.equal(lines.some(line => line.includes('node P0')), true);
            });

            test('buildDiagramNodeLabel: サブグラフ配下のラベルを短縮する', () => {
                const label = pkg.buildDiagramNodeLabel(
                    'com.example.domain.model',
                    'com.example.domain.model',
                    'com.example.domain'
                );
                assert.equal(label, 'model');
            });

            test('buildDiagramNodeTooltip: FQNを返す', () => {
                assert.equal(pkg.buildDiagramNodeTooltip('com.example.domain'), 'com.example.domain');
                assert.equal(pkg.buildDiagramNodeTooltip(null), '');
            });
        });

        test.describe('テーブル', () => {
            test('applyPackageFilterToTable: 行の表示/非表示を切り替える', () => {
                const doc = setupDocument();
                const rows = buildPackageRows(doc, ['app.domain', 'app.other']);

                pkg.applyPackageFilterToTable('app.domain', testContext);

                assert.equal(rows[0].classList.contains('hidden'), false);
                assert.equal(rows[1].classList.contains('hidden'), true);
            });

            test('buildPackageRowVisibility: パッケージフィルタのみ', () => {
                const visibility = pkg.buildPackageRowVisibility(
                    ['app.domain', 'app.other'],
                    'app.domain'
                );
                assert.deepEqual(visibility, [true, false]);
            });

            test('applyRelatedFilterToTable: 未指定ならパッケージフィルタのみ', () => {
                const doc = setupDocument();
                setPackageData({packages: [], relations: []}, testContext);
                const rows = buildPackageRows(doc, ['app.domain', 'app.other']);
                testContext.packageFilterFqn = 'app.domain';

                pkg.applyRelatedFilterToTable(null, testContext);

                assert.equal(rows[0].classList.contains('hidden'), false);
                assert.equal(rows[1].classList.contains('hidden'), true);
            });

            test('applyRelatedFilterToTable: 関係する行のみ表示', () => {
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
                testContext.relatedFilterMode = 'direct';
                testContext.packageFilterFqn = null;

                pkg.applyRelatedFilterToTable('app.a', testContext);

                assert.equal(rows[0].classList.contains('hidden'), false);
                assert.equal(rows[1].classList.contains('hidden'), false);
                assert.equal(rows[2].classList.contains('hidden'), true);
            });

            test('buildRelatedRowVisibility: 関連フィルタ未指定はパッケージフィルタのみ', () => {
                const rowFqns = ['app.domain', 'app.other'];
                const visibility = pkg.buildRelatedRowVisibility(
                    rowFqns,
                    [],
                    'app.domain',
                    0,
                    'direct',
                    null
                );
                assert.deepEqual(visibility, [true, false]);
            });

            test('buildRelatedRowVisibility: 関係する行のみ表示', () => {
                const rowFqns = ['app.a', 'app.b', 'app.c'];
                const relations = [{from: 'app.a', to: 'app.b'}];
                const visibility = pkg.buildRelatedRowVisibility(
                    rowFqns,
                    relations,
                    null,
                    0,
                    'direct',
                    'app.a'
                );
                assert.deepEqual(visibility, [true, true, false]);
            });
        });
    });

    test.describe('描画', () => {
        test.describe('UI表示', () => {
            test('renderRelatedFilterTarget: 対象表示を更新する', () => {
                const mockTarget = { textContent: '' };
                const getRelatedFilterTargetMock = test.mock.fn(() => mockTarget);
                const setRelatedFilterTargetTextMock = test.mock.fn((element, text) => { element.textContent = text; });

                test.mock.method(pkg.dom, 'getRelatedFilterTarget', getRelatedFilterTargetMock);
                test.mock.method(pkg.dom, 'setRelatedFilterTargetText', setRelatedFilterTargetTextMock);

                testContext.relatedFilterFqn = null;
                pkg.renderRelatedFilterTarget(testContext);
                assert.equal(mockTarget.textContent, '未選択');
                assert.equal(setRelatedFilterTargetTextMock.mock.calls.length, 1);
                assert.deepEqual(setRelatedFilterTargetTextMock.mock.calls[0].arguments, [mockTarget, '未選択']);

                testContext.relatedFilterFqn = 'app.domain';
                pkg.renderRelatedFilterTarget(testContext);
                assert.equal(mockTarget.textContent, 'app.domain');
                assert.equal(setRelatedFilterTargetTextMock.mock.calls.length, 2);
                assert.deepEqual(setRelatedFilterTargetTextMock.mock.calls[1].arguments, [mockTarget, 'app.domain']);
            });

            test('updateAggregationDepthOptions: 選択肢を更新する', () => {
                const doc = setupDocument();
                const select = new Element('select');
                doc.elementsById.set('package-depth-select', select);
                setPackageData({
                    packages: [
                        {fqn: 'app.domain'},
                        {fqn: 'lib.core'},
                    ],
                    relations: [
                        {from: 'app.domain', to: 'lib.core'},
                    ],
                }, testContext);
                testContext.aggregationDepth = 1;
                testContext.packageFilterFqn = null;
                testContext.relatedFilterFqn = null;

                pkg.updateAggregationDepthOptions(2, testContext);

                assert.equal(select.children.length >= 2, true);
                assert.equal(select.children[0].textContent.includes('集約なし'), true);
                assert.equal(select.value, '1');
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

            test('renderAggregationDepthOptions: セレクトを更新する', () => {
                const doc = setupDocument();
                const select = new Element('select');
                doc.elementsById.set('package-depth-select', select);
                const options = [
                    {value: '0', text: '集約なし（P2 / R1）'},
                    {value: '1', text: '深さ1（P1 / R1）'},
                ];

                pkg.renderAggregationDepthOptions(select, options, 1, 2);

                assert.equal(select.children.length, 2);
                assert.equal(select.children[0].textContent, '集約なし（P2 / R1）');
                assert.equal(select.value, '1');
            });
        });

        test.describe('一覧/補助', () => {
            test('renderPackageTable: 行とカウントを描画する', () => {
                const doc = setupDocument();
                setPackageData({
                    packages: [
                        {fqn: 'app.a', name: 'A', classCount: 2},
                        {fqn: 'app.b', name: 'B', classCount: 1},
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
                assert.equal(tbody.children[0].children[4].textContent, '2');
                assert.equal(tbody.children[0].children[5].textContent, '0');
                assert.equal(tbody.children[0].children[6].textContent, '2');
            });

            test('buildPackageTableRowSpecs: 行データを整形する', () => {
                const rows = [
                    {fqn: 'app.a', name: 'A', classCount: 2, incomingCount: 0, outgoingCount: 1},
                ];

                const specs = pkg.buildPackageTableRowSpecs(rows);

                assert.deepEqual(specs, [{
                    fqn: 'app.a',
                    name: 'A',
                    classCount: 2,
                    incomingCount: 0,
                    outgoingCount: 1,
                }]);
            });

            test('buildPackageTableActionSpecs: ボタン文言を返す', () => {
                const specs = pkg.buildPackageTableActionSpecs();

                assert.equal(specs.filter.ariaLabel, 'このパッケージで絞り込み');
                assert.equal(specs.filter.screenReaderText, '絞り込み');
                assert.equal(specs.related.ariaLabel, '関連のみ表示');
                assert.equal(specs.related.screenReaderText, '関連のみ表示');
            });

            test('getOrCreateDiagramErrorBox: エラーボックスを作成/再利用する', () => {
                const diagram = { parentNode: { insertBefore: test.mock.fn() } }; // Minimal mock for diagram

                const createdErrorBox = { id: 'package-diagram-error', appendChild: test.mock.fn() };
                const createDiagramErrorBoxMock = test.mock.fn(() => createdErrorBox);

                // --- First call: Error box does not exist, should be created ---
                const getDiagramErrorBoxMockInitialNull = test.mock.fn(() => null);
                test.mock.method(pkg.dom, 'getDiagramErrorBox', getDiagramErrorBoxMockInitialNull);
                test.mock.method(pkg.dom, 'createDiagramErrorBox', createDiagramErrorBoxMock);

                const firstErrorBox = pkg.getOrCreateDiagramErrorBox(diagram);

                assert.equal(getDiagramErrorBoxMockInitialNull.mock.calls.length, 1, 'getDiagramErrorBox should be called once initially');
                assert.equal(createDiagramErrorBoxMock.mock.calls.length, 1, 'createDiagramErrorBox should be called once');
                assert.deepEqual(createDiagramErrorBoxMock.mock.calls[0].arguments, [diagram], 'createDiagramErrorBox called with diagram');
                assert.equal(firstErrorBox, createdErrorBox, 'First call returns newly created error box');

                // --- Second call: Error box already exists ---
                const getDiagramErrorBoxMockReturningCreated = test.mock.fn(() => createdErrorBox); // Returns the *same* instance
                // Re-mock getDiagramErrorBox to return the existing one.
                // It will override the previous mock, so the first mock will not be called again.
                test.mock.method(pkg.dom, 'getDiagramErrorBox', getDiagramErrorBoxMockReturningCreated);

                const secondErrorBox = pkg.getOrCreateDiagramErrorBox(diagram);

                assert.equal(getDiagramErrorBoxMockReturningCreated.mock.calls.length, 1, 'getDiagramErrorBox should be called once more for existing');
                // The createDiagramErrorBoxMock.mock.calls.length should still be 1 from the first call.
                assert.equal(createDiagramErrorBoxMock.mock.calls.length, 1, 'createDiagramErrorBox should not be called again');
                assert.equal(secondErrorBox, createdErrorBox, 'Second call returns existing error box');
                assert.equal(firstErrorBox, secondErrorBox, 'Both calls should return the same instance when reused');
            });

            test('showDiagramErrorMessage/hideDiagramErrorMessage: 表示を切り替える', () => {
                const diagramMock = { style: { display: '' } }; // Mock for context.diagramElement
                const errorBoxMock = { style: { display: 'none' } }; // Mock for the error box element
                const messageNodeMock = { textContent: '' }; // Mock for the message node
                const actionNodeMock = { style: { display: 'none' }, onclick: null }; // Mock for the action node

                // Mock dom helpers
                test.mock.method(pkg.dom, 'getDiagramErrorBox', test.mock.fn(() => errorBoxMock));
                test.mock.method(pkg.dom, 'createDiagramErrorBox', test.mock.fn(() => errorBoxMock)); // getOrCreate will call this if not found
                test.mock.method(pkg.dom, 'getDiagramErrorMessageNode', test.mock.fn(() => messageNodeMock));
                test.mock.method(pkg.dom, 'getDiagramErrorActionNode', test.mock.fn(() => actionNodeMock));
                test.mock.method(pkg.dom, 'setNodeTextContent', test.mock.fn((el, text) => { el.textContent = text; }));
                test.mock.method(pkg.dom, 'setNodeDisplay', test.mock.fn((el, display) => { el.style.display = display; }));
                test.mock.method(pkg.dom, 'setNodeOnClick', test.mock.fn((el, handler) => { el.onclick = handler; }));
                test.mock.method(pkg.dom, 'setDiagramElementDisplay', test.mock.fn((el, display) => { el.style.display = display; }));

                testContext.diagramElement = diagramMock;

                const errors = withConsoleErrorCapture(() => {
                    pkg.showDiagramErrorMessage('test-error-message', false, null, null, testContext);
                });

                assert.equal(errorBoxMock.style.display, '', 'errorBox should be displayed');
                assert.equal(diagramMock.style.display, 'none', 'diagram should be hidden');
                assert.equal(messageNodeMock.textContent, 'test-error-message', 'messageNode content should be set');
                assert.equal(errors.some(line => line.includes('test-error-message')), true, 'console.error should be called');

                pkg.hideDiagramErrorMessage(diagramMock); // Pass diagramMock directly as it's used
                assert.equal(errorBoxMock.style.display, 'none', 'errorBox should be hidden');
                assert.equal(diagramMock.style.display, '', 'diagram should be displayed');
                assert.equal(messageNodeMock.textContent, '', 'messageNode content should be cleared');
                assert.equal(actionNodeMock.style.display, 'none', 'actionNode should be hidden');
                assert.equal(actionNodeMock.onclick, null, 'actionNode onclick should be cleared');
            });

            test('renderDiagramSvg: Mermaid描画を実行する', () => {
                const diagramMock = {
                    removeAttribute: test.mock.fn(),
                    textContent: '',
                    style: { display: '' } // Needs to be there for dom.setDiagramElementDisplay
                };
                testContext.diagramElement = diagramMock;

                // Mock dom helpers used by hideDiagramErrorMessage and renderDiagramSvg itself
                test.mock.method(pkg.dom, 'getDiagramErrorBox', test.mock.fn(() => ({ style: {} }))); // Mock return for errorBox
                test.mock.method(pkg.dom, 'createDiagramErrorBox', test.mock.fn(() => ({ style: {} })));
                test.mock.method(pkg.dom, 'getDiagramErrorMessageNode', test.mock.fn(() => ({ textContent: '' })));
                test.mock.method(pkg.dom, 'getDiagramErrorActionNode', test.mock.fn(() => ({ style: {}, onclick: null })));
                test.mock.method(pkg.dom, 'setNodeTextContent', test.mock.fn((el, text) => { el.textContent = text; }));
                test.mock.method(pkg.dom, 'setNodeDisplay', test.mock.fn((el, display) => { el.style.display = display; }));
                test.mock.method(pkg.dom, 'setNodeOnClick', test.mock.fn((el, handler) => { el.onclick = handler; }));
                test.mock.method(pkg.dom, 'setDiagramElementDisplay', test.mock.fn((el, display) => { el.style.display = display; }));
                test.mock.method(pkg.dom, 'setDiagramContent', test.mock.fn((el, content) => { el.textContent = content; }));
                test.mock.method(pkg.dom, 'removeDiagramAttribute', test.mock.fn((el, attr) => { /* no-op */ }));


                let runCalled = false;
                global.window = {
                    mermaid: {
                        initialize() {
                        },
                        run() {
                            runCalled = true;
                        },
                    },
                };
                global.mermaid = global.window.mermaid;

                pkg.renderDiagramSvg('graph TD', 100, testContext);

                assert.equal(diagramMock.textContent, 'graph TD'); // Check through the dom helper mock
                assert.equal(pkg.dom.removeDiagramAttribute.mock.calls.length, 1);
                assert.deepEqual(pkg.dom.removeDiagramAttribute.mock.calls[0].arguments, [diagramMock, 'data-processed']);
                assert.equal(pkg.dom.setDiagramContent.mock.calls.length, 1);
                assert.deepEqual(pkg.dom.setDiagramContent.mock.calls[0].arguments, [diagramMock, 'graph TD']);
                assert.equal(runCalled, true);
            });
        });

        test.describe('既定フィルタ', () => {
            test('applyDefaultPackageFilterIfPresent: ドメインがあれば適用', () => {
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
                const {input} = createPackageFilterControls(doc);
                createDepthSelect(doc); // for renderDiagramAndTable

                const applied = pkg.applyDefaultPackageFilterIfPresent(testContext);

                assert.equal(applied, true);
                assert.equal(testContext.packageFilterFqn, 'app.domain');
                assert.equal(input.value, 'app.domain');
            });

            test('findDefaultPackageFilterCandidate: ドメイン候補を返す', () => {
                const candidate = pkg.findDefaultPackageFilterCandidate([
                    {fqn: 'app.domain.core'},
                    {fqn: 'app.domain.sub'},
                ]);
                assert.equal(candidate, 'app.domain');
            });

            test('applyDefaultPackageFilterIfPresent: 入力済みなら適用しない', () => {
                const doc = setupDocument();
                setPackageData({
                    packages: [{fqn: 'app.domain.core'}],
                    relations: [],
                }, testContext);
                const input = doc.createElement('input');
                input.id = 'package-filter-input';
                input.value = 'app';
                doc.elementsById.set('package-filter-input', input);

                const applied = pkg.applyDefaultPackageFilterIfPresent(testContext);

                assert.equal(applied, false);
            });

            test('normalizePackageFilterValue: 空文字はnull', () => {
                assert.equal(pkg.normalizePackageFilterValue(''), null);
                assert.equal(pkg.normalizePackageFilterValue('   '), null);
                assert.equal(pkg.normalizePackageFilterValue('app.domain'), 'app.domain');
            });

            test('normalizeAggregationDepthValue: 数値化する', () => {
                assert.equal(pkg.normalizeAggregationDepthValue('2'), 2);
                assert.equal(pkg.normalizeAggregationDepthValue('0'), 0);
                assert.equal(pkg.normalizeAggregationDepthValue('abc'), 0);
            });
        });
    });

    test.describe('ダイアグラム', () => {
        test.describe('相互依存', () => {
            test('renderMutualDependencyList: なしの場合は非表示', () => {
                const doc = setupDocument();
                const container = new Element('div', doc);
                doc.elementsById.set('mutual-dependency-list', container);

                pkg.renderMutualDependencyList(new Set(), [], testContext);

                assert.equal(container.style.display, 'none');
                assert.equal(container.innerHTML, '');
            });

    test('renderMutualDependencyList: 相互依存と原因を一覧化', () => {
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
            testContext
        );

                assert.equal(container.style.display, '');
        assert.equal(container.children.length, 1);
        assert.equal(container.children[0].tagName, 'details');
                assert.equal(container.children[0].children[1].tagName, 'ul');
            });

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
        });

        test.describe('描画', () => {
            test('renderPackageDiagram: 相互依存を含む描画', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [
                        {fqn: 'app.a', name: 'A', classCount: 1},
                        {fqn: 'app.b', name: 'B', classCount: 1},
                    ],
                    relations: [
                        {from: 'app.a', to: 'app.b'},
                        {from: 'app.b', to: 'app.a'},
                    ],
                }, testContext);

                pkg.renderPackageDiagram(testContext, null, null);

                const diagram = doc.getElementById('package-relation-diagram');
                assert.equal(diagram.textContent.includes('graph'), true);
                assert.equal(diagram.textContent.includes('<-->'), true);
                const mutual = doc.getElementById('mutual-dependency-list');
                assert.equal(mutual.children.length > 0, true);
            });

            test('renderPackageDiagram: サブグラフ内のFQNノードラベルが省略される', () => {
                const doc = setupDocument();
                const diagram = setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [
                        {fqn: 'com.example', name: 'example', classCount: 1},
                        {fqn: 'com.example.domain', name: 'domain', classCount: 1},
                        {fqn: 'com.example.domain.model', classCount: 1}, // No 'name' property
                        {fqn: 'com.example.domain.repository', name: 'repository', classCount: 1},
                        {fqn: 'com.example.service', name: 'service', classCount: 1},
                    ],
                    relations: [
                        {from: 'com.example.domain.model', to: 'com.example.domain.repository'},
                        {from: 'com.example.service', to: 'com.example.domain'},
                    ],
                }, testContext);

                testContext.aggregationDepth = 0; // Set to no aggregation to ensure full hierarchy is built
                pkg.renderPackageDiagram(testContext, null, null);

                const diagramContent = diagram.textContent;

                // Match subgraph creation for com.example.domain (using regex for groupId)
                assert.ok(/subgraph G\d+\["com\.example\.domain"]/.test(diagramContent), 'Expected subgraph for com.example.domain');

                // Check for nodes inside this subgraph:
                // com.example.domain.model (FQN, should be shortened to 'model')
                assert.ok(/P\d+\["model"]/.test(diagramContent), 'Expected "com.example.domain.model" to be shortened to "model"');
                // com.example.domain.repository (has 'name', should remain 'repository')
                assert.ok(/P\d+\["repository"]/.test(diagramContent), 'Expected "com.example.domain.repository" to remain "repository"');

                // Verify that 'com.example.service' is also present and correctly labeled
                assert.ok(/P\d+\["service"]/.test(diagramContent), 'Expected "com.example.service" to be labeled "service"');
            });
        });

        test.describe('分岐/エラー', () => {
            test('renderPackageDiagram: エッジ数超過で保留/エラー表示', () => {
                const doc = setupDocument();
                // setupDiagramEnvironmentはtestContext.diagramElementを設定する。
                // そのdiagramElementがdomヘルパーによって操作されることをモックする。
                const diagramMock = setupDiagramEnvironment(doc, testContext); // testContext.diagramElementも設定される

                // Mock dom helpers used by showDiagramErrorMessage internally
                const errorBoxMock = { style: { display: 'none' } };
                const messageNodeMock = { textContent: '' };
                const actionNodeMock = { style: { display: 'none' }, onclick: null };
                test.mock.method(pkg.dom, 'getDiagramErrorBox', test.mock.fn(() => errorBoxMock));
                test.mock.method(pkg.dom, 'createDiagramErrorBox', test.mock.fn(() => errorBoxMock)); // called by getOrCreate
                test.mock.method(pkg.dom, 'getDiagramErrorMessageNode', test.mock.fn(() => messageNodeMock));
                test.mock.method(pkg.dom, 'getDiagramErrorActionNode', test.mock.fn(() => actionNodeMock));
                test.mock.method(pkg.dom, 'setNodeTextContent', test.mock.fn((el, text) => { el.textContent = text; }));
                test.mock.method(pkg.dom, 'setNodeDisplay', test.mock.fn((el, display) => { el.style.display = display; }));
                test.mock.method(pkg.dom, 'setNodeOnClick', test.mock.fn((el, handler) => { el.onclick = handler; }));
                test.mock.method(pkg.dom, 'setDiagramElementDisplay', test.mock.fn((el, display) => { el.style.display = display; }));

                // Data setup (same as before)
                const packages = [];
                const relations = [];
                for (let i = 0; i < 501; i += 1) {
                    const from = `app.p${i}`;
                    const to = `app.p${i + 1}`;
                    packages.push({fqn: from, name: from, classCount: 1});
                    packages.push({fqn: to, name: to, classCount: 1});
                    relations.push({from, to});
                }
                setPackageData({packages, relations}, testContext);

                const errors = withConsoleErrorCapture(() => {
                    pkg.renderPackageDiagram(testContext, null, null);
                });

                assert.equal(errorBoxMock.style.display, '', 'errorBox should be displayed by showDiagramErrorMessage'); // Check display set by showDiagramErrorMessage
                assert.equal(diagramMock.style.display, 'none', 'diagram should be hidden by showDiagramErrorMessage'); // Check display set by showDiagramErrorMessage
                assert.equal(errors.some(line => line.includes('関連数が多すぎるため描画を省略しました。')), true);
                assert.ok(actionNodeMock.onclick, 'actionNode should have onclick handler');
                assert.equal(actionNodeMock.style.display, '', 'actionNode should be displayed');
            });

            test('mermaid.parseError: エラー内容を表示', () => {
                const doc = setupDocument();
                const diagramMock = setupDiagramEnvironment(doc, testContext); // testContext.diagramElement is set here
                setPackageData({
                    packages: [{fqn: 'app.a', name: 'A', classCount: 1}],
                    relations: [],
                }, testContext);
                pkg.renderPackageDiagram(testContext, null, null); // This prepares the diagram and sets mermaid.parseError

                // Mock dom helpers used by showDiagramErrorMessage internally
                const errorBoxMock = { style: { display: 'none' } };
                const messageNodeMock = { textContent: '' };
                const actionNodeMock = { style: { display: 'none' }, onclick: null };
                test.mock.method(pkg.dom, 'getDiagramErrorBox', test.mock.fn(() => errorBoxMock));
                test.mock.method(pkg.dom, 'createDiagramErrorBox', test.mock.fn(() => errorBoxMock)); // called by getOrCreate
                test.mock.method(pkg.dom, 'getDiagramErrorMessageNode', test.mock.fn(() => messageNodeMock));
                test.mock.method(pkg.dom, 'getDiagramErrorActionNode', test.mock.fn(() => actionNodeMock));
                test.mock.method(pkg.dom, 'setNodeTextContent', test.mock.fn((el, text) => { el.textContent = text; }));
                test.mock.method(pkg.dom, 'setNodeDisplay', test.mock.fn((el, display) => { el.style.display = display; }));
                test.mock.method(pkg.dom, 'setNodeOnClick', test.mock.fn((el, handler) => { el.onclick = handler; }));
                test.mock.method(pkg.dom, 'setDiagramElementDisplay', test.mock.fn((el, display) => { el.style.display = display; }));
                
                // Mermaidはパース失敗時のみ呼ばれるため、テストでは直接呼び出す。
                const errors = withConsoleErrorCapture(() => {
                    global.mermaid.parseError(
                        {message: 'Mermaid parse error details'}, // Use a more specific message
                        {line: 10, loc: 2}
                    );
                });

                assert.equal(messageNodeMock.textContent.includes('Mermaid parse error:'), true);
                assert.equal(messageNodeMock.textContent.includes('Line: 10 Column: 2'), true);
                assert.equal(errors.some(line => line.includes('Mermaid parse error:')), true);
                assert.equal(errors.some(line => line.includes('Mermaid parse error details')), true); // Check for specific error message
                assert.equal(errors.some(line => line.includes('Mermaid error location: 10 2')), true);
            });

            test('renderDiagramAndTable: 描画とフィルタ適用を行う', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData({
                    packages: [
                        {fqn: 'app.a', name: 'A', classCount: 1},
                        {fqn: 'app.b', name: 'B', classCount: 1},
                    ],
                    relations: [
                        {from: 'app.a', to: 'app.b'},
                    ],
                }, testContext);
                const rows = buildPackageRows(doc, ['app.a', 'app.b']);
                doc.selectors.set('#package-table tbody', doc.createElement('tbody'));
                const select = doc.createElement('select');
                select.id = 'package-depth-select';
                doc.elementsById.set('package-depth-select', select);
                testContext.relatedFilterMode = 'direct';
                testContext.relatedFilterFqn = 'app.a';
                testContext.packageFilterFqn = null;
                testContext.aggregationDepth = 0;

                pkg.renderDiagramAndTable(testContext);

                assert.equal(rows[1].classList.contains('hidden'), false);
                assert.equal(select.children.length > 0, true);
            });
        });
    });

    test.describe('UI制御', () => {
        test('setupPackageFilterControls: 適用/解除をハンドリング', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({
                packages: [{fqn: 'app.domain', name: 'Domain', classCount: 1}],
                relations: [],
            }, testContext);
            doc.selectorsAll.set('#package-table tbody tr', []);
            createDepthSelect(doc);

            const {input, applyButton, clearButton} = createPackageFilterControls(doc);

            pkg.setupPackageFilterControls(testContext);

            input.value = 'app.domain';
            applyButton.eventListeners.get('click')();
            assert.equal(testContext.packageFilterFqn, 'app.domain');

            clearButton.eventListeners.get('click')();
            assert.equal(testContext.packageFilterFqn, null);
            assert.equal(input.value, '');
        });

        test('setupPackageFilterControls: Enterキーで適用', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({
                packages: [{fqn: 'app.domain', name: 'Domain', classCount: 1}],
                relations: [],
            }, testContext);
            doc.selectorsAll.set('#package-table tbody tr', []);
            createDepthSelect(doc);
            const {input} = createPackageFilterControls(doc);

            pkg.setupPackageFilterControls(testContext);

            let prevented = false;
            input.value = 'app.domain';
            input.eventListeners.get('keydown')({
                key: 'Enter',
                preventDefault() {
                    prevented = true;
                },
            });

            assert.equal(prevented, true);
            assert.equal(testContext.packageFilterFqn, 'app.domain');
        });

        test('setupAggregationDepthControl: 変更を反映する', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({
                packages: [
                    {fqn: 'app.domain.a'},
                    {fqn: 'app.domain.b'},
                ],
                relations: [],
            }, testContext);
            doc.selectorsAll.set('#package-table tbody tr', []);
            const select = createDepthSelect(doc);

            testContext.aggregationDepth = 0;
            pkg.setupAggregationDepthControl(testContext);

            select.value = '1';
            select.eventListeners.get('change')();
            assert.equal(testContext.aggregationDepth, 1);
        });

        test('setupRelatedFilterControls: モード変更を反映', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({
                packages: [
                    {fqn: 'app.a'},
                    {fqn: 'app.b'},
                    {fqn: 'app.c'},
                ],
                relations: [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.b', to: 'app.c'},
                ],
            }, testContext);
            createDepthSelect(doc);
            const {select, clearButton} = createRelatedFilterControls(doc);
            const input = doc.createElement('input');
            input.id = 'package-filter-input';
            doc.elementsById.set('package-filter-input', input);

            testContext.aggregationDepth = 0;
            testContext.relatedFilterMode = 'direct';
            testContext.relatedFilterFqn = 'app.a';
            pkg.setupRelatedFilterControls(testContext);

            select.value = 'all';
            select.eventListeners.get('change')();
            assert.equal(testContext.relatedFilterMode, 'all');


            clearButton.eventListeners.get('click')();
            assert.equal(testContext.relatedFilterFqn, null);
        });

        test('setupDiagramDirectionControls: 向きを切り替える', () => {
            const doc = setupDocument();
            setupDiagramEnvironment(doc, testContext);
            setPackageData({
                packages: [{fqn: 'app.a'}],
                relations: [],
            }, testContext);
            createDepthSelect(doc);
            doc.selectorsAll.set('#package-table tbody tr', []);
            const td = doc.createElement('input');
            td.value = 'TD';
            const lr = doc.createElement('input');
            lr.value = 'LR';
            doc.selectorsAll.set('input[name=\"diagram-direction\"]', [td, lr]);

            pkg.setupDiagramDirectionControls(testContext);

            lr.checked = true;
            lr.eventListeners.get('change')();
            assert.equal(testContext.diagramDirection, 'LR');
        });
    });

    test.describe('推移簡約', () => {
        test('detectStronglyConnectedComponents: 循環を検出する', () => {
            const graph = new Map([
                ['a', ['b']],
                ['b', ['c']],
                ['c', ['a', 'd']],
                ['d', ['e']],
                ['e', ['f']],
                ['f', ['d']],
            ]);
            const sccs = pkg.detectStronglyConnectedComponents(graph);
            const sortedSccs = sccs.map(scc => scc.sort()).sort((a, b) => a[0].localeCompare(b[0]));
            assert.deepEqual(sortedSccs, [['a', 'b', 'c'], ['d', 'e', 'f']]);
        });

        test('transitiveReduction: 単純な推移関係を簡約する', () => {
            const relations = [
                {from: 'a', to: 'b'},
                {from: 'b', to: 'c'},
                {from: 'a', to: 'c'},
            ];
            const result = pkg.transitiveReduction(relations);
            assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'b>c']);
        });

        test('transitiveReduction: 循環参照は対象外とする', () => {
            const relations = [
                {from: 'a', to: 'b'},
                {from: 'b', to: 'a'},
                {from: 'a', to: 'c'},
            ];
            const result = pkg.transitiveReduction(relations);
            assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'a>c', 'b>a']);
        });

        test('transitiveReduction: 循環ではないが簡約対象でもない', () => {
            const relations = [
                {from: 'a', to: 'b'},
                {from: 'c', to: 'd'},
            ];
            const result = pkg.transitiveReduction(relations);
            assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'c>d']);
        });

        test('transitiveReduction: 循環からの関係は簡約対象にしない', () => {
            const relations = [
                {from: 'a', to: 'b'},
                {from: 'b', to: 'a'}, // cycle
                {from: 'b', to: 'c'},
                {from: 'a', to: 'c'},
            ];
            const result = pkg.transitiveReduction(relations);
            assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'a>c', 'b>a', 'b>c']);
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
            checkbox.eventListeners.get('change')();

            assert.equal(testContext.transitiveReductionEnabled, false);
        });
    });
});
