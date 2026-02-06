const test = require('node:test');
const assert = require('node:assert/strict');

const pkg = require('../../main/resources/templates/assets/package.js');

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

function setPackageData(doc, data, context) {
    const dataElement = new Element('script', doc);
    dataElement.textContent = JSON.stringify(data);
    doc.elementsById.set('package-data', dataElement);
    context.packageSummaryCache = null;
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
                const doc = setupDocument();
                setPackageData(doc, [{fqn: 'app.a', name: 'A', classCount: 1, description: ''}], testContext);

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
                setPackageData(doc, {
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
            testContext.aggregationDepth = 1;
            testContext.relatedFilterMode = 'all';
            const packages = [
                {fqn: 'app.domain.a'},
                {fqn: 'app.domain.b'},
                {fqn: 'app.other.c'},
            ];
            const relations = [
                {from: 'app.domain.a', to: 'app.domain.b'},
                {from: 'app.domain.b', to: 'app.other.c'},
            ];

            const stats = pkg.buildAggregationStatsForRelated(packages, relations, 'app.domain.a', 1, testContext);
            const depth1 = stats.get(1);

            assert.equal(depth1.packageCount, 1);
            assert.equal(depth1.relationCount, 0);
        });

        test('buildAggregationStatsForFilters: directモードの複合集計', () => {
            testContext.aggregationDepth = 0;
            testContext.relatedFilterMode = 'direct';
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
                testContext
            );
            const depth0 = stats.get(0);

            assert.equal(depth0.packageCount, 2);
            assert.equal(depth0.relationCount, 1);
        });

        test('buildAggregationStatsForFilters: allモードの複合集計', () => {
            testContext.aggregationDepth = 0;
            testContext.relatedFilterMode = 'all';
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
                testContext
            );
            const depth0 = stats.get(0);

            assert.equal(depth0.packageCount, 3);
            assert.equal(depth0.relationCount, 2);
        });
    });

    test.describe('フィルタ', () => {
        test.describe('テーブル', () => {
            test('applyPackageFilterToTable: 行の表示/非表示を切り替える', () => {
                const doc = setupDocument();
                const rows = buildPackageRows(doc, ['app.domain', 'app.other']);

                pkg.applyPackageFilterToTable('app.domain', testContext);

                assert.equal(rows[0].classList.contains('hidden'), false);
                assert.equal(rows[1].classList.contains('hidden'), true);
            });

            test('applyRelatedFilterToTable: 未指定ならパッケージフィルタのみ', () => {
                const doc = setupDocument();
                const rows = buildPackageRows(doc, ['app.domain', 'app.other']);
                testContext.packageFilterFqn = 'app.domain';

                pkg.applyRelatedFilterToTable(null, testContext);

                assert.equal(rows[0].classList.contains('hidden'), false);
                assert.equal(rows[1].classList.contains('hidden'), true);
            });

            test('applyRelatedFilterToTable: 関係する行のみ表示', () => {
                const doc = setupDocument();
                setPackageData(doc, {
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
        });
    });

    test.describe('描画', () => {
        test.describe('UI表示', () => {
            test('renderRelatedFilterTarget: 対象表示を更新する', () => {
                const doc = setupDocument();
                const target = new Element('span');
                doc.elementsById.set('related-filter-target', target);

                testContext.relatedFilterFqn = null;
                pkg.renderRelatedFilterTarget(testContext);
                assert.equal(target.textContent, '未選択');

                testContext.relatedFilterFqn = 'app.domain';
                pkg.renderRelatedFilterTarget(testContext);
                assert.equal(target.textContent, 'app.domain');
            });

            test('updateAggregationDepthOptions: 選択肢を更新する', () => {
                const doc = setupDocument();
                const select = new Element('select');
                doc.elementsById.set('package-depth-select', select);
                setPackageData(doc, {
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
        });

        test.describe('一覧/補助', () => {
            test('renderPackageTable: 行とカウントを描画する', () => {
                const doc = setupDocument();
                setPackageData(doc, {
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

            test('getOrCreateDiagramErrorBox: エラーボックスを作成/再利用する', () => {
                const doc = setupDocument();
                const container = new Element('div', doc);
                const diagram = new Element('div', doc);
                container.appendChild(diagram);

                const first = pkg.getOrCreateDiagramErrorBox(diagram, testContext);
                const second = pkg.getOrCreateDiagramErrorBox(diagram, testContext);

                assert.equal(first, second);
                assert.equal(first.id, 'package-diagram-error');
                assert.equal(container.children[0], first);
            });

            test('showDiagramErrorMessage/hideDiagramErrorMessage: 表示を切り替える', () => {
                const doc = setupDocument();
                const container = new Element('div', doc);
                const diagram = new Element('div', doc);
                container.appendChild(diagram);
                testContext.diagramElement = diagram;

                const errors = withConsoleErrorCapture(() => {
                    pkg.showDiagramErrorMessage('test-error-message', false, null, null, testContext);
                });
                const errorBox = doc.getElementById('package-diagram-error');
                const messageNode = doc.getElementById('package-diagram-error-message');

                assert.equal(errorBox.style.display, '');
                assert.equal(diagram.style.display, 'none');
                assert.equal(messageNode.textContent, 'test-error-message');
                assert.equal(errors.some(line => line.includes('test-error-message')), true);

                pkg.hideDiagramErrorMessage(diagram, testContext);
                assert.equal(errorBox.style.display, 'none');
                assert.equal(diagram.style.display, '');
            });

            test('renderDiagramSvg: Mermaid描画を実行する', () => {
                const doc = setupDocument();
                const container = new Element('div', doc);
                const diagram = new Element('div', doc);
                container.appendChild(diagram);
                testContext.diagramElement = diagram;

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

                assert.equal(diagram.textContent, 'graph TD');
                assert.equal(runCalled, true);
            });
        });

        test.describe('既定フィルタ', () => {
            test('applyDefaultPackageFilterIfPresent: ドメインがあれば適用', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData(doc, {
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

            test('applyDefaultPackageFilterIfPresent: 入力済みなら適用しない', () => {
                const doc = setupDocument();
                setPackageData(doc, {
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
        });

        test.describe('描画', () => {
            test('renderPackageDiagram: 相互依存を含む描画', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData(doc, {
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
                setPackageData(doc, {
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
                setupDiagramEnvironment(doc, testContext);
                const packages = [];
                const relations = [];
                for (let i = 0; i < 501; i += 1) {
                    const from = `app.p${i}`;
                    const to = `app.p${i + 1}`;
                    packages.push({fqn: from, name: from, classCount: 1});
                    packages.push({fqn: to, name: to, classCount: 1});
                    relations.push({from, to});
                }
                setPackageData(doc, {packages, relations}, testContext);

                const errors = withConsoleErrorCapture(() => {
                    pkg.renderPackageDiagram(testContext, null, null);
                });

                const errorBox = doc.getElementById('package-diagram-error');
                assert.equal(errorBox.style.display, '');
                assert.equal(errors.some(line => line.includes('関連数が多すぎるため描画を省略しました。')), true);
            });

            test('mermaid.parseError: エラー内容を表示', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData(doc, {
                    packages: [{fqn: 'app.a', name: 'A', classCount: 1}],
                    relations: [],
                }, testContext);
                pkg.renderPackageDiagram(testContext, null, null);

                // Mermaidはパース失敗時のみ呼ばれるため、テストでは直接呼び出す。
                const errors = withConsoleErrorCapture(() => {
                    global.mermaid.parseError(
                        {message: 'Edge limit exceeded'},
                        {line: 10, loc: 2}
                    );
                });

                const messageNode = doc.getElementById('package-diagram-error-message');
                assert.equal(messageNode.textContent.includes('Mermaid parse error:'), true);
                assert.equal(messageNode.textContent.includes('Line: 10 Column: 2'), true);
                assert.equal(errors.some(line => line.includes('Mermaid parse error:')), true);
                assert.equal(errors.some(line => line.includes('Edge limit exceeded')), true);
                assert.equal(errors.some(line => line.includes('Mermaid error location: 10 2')), true);
            });

            test('renderDiagramAndTable: 描画とフィルタ適用を行う', () => {
                const doc = setupDocument();
                setupDiagramEnvironment(doc, testContext);
                setPackageData(doc, {
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
            setPackageData(doc, {
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
            setPackageData(doc, {
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
            setPackageData(doc, {
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
            setPackageData(doc, {
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
            setPackageData(doc, {
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
            setPackageData(doc, {packages: [{fqn: 'a'}], relations: []}, testContext);
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