const test = require('node:test');
const assert = require('node:assert/strict');

require('../../main/resources/templates/assets/jig-util.js');
require('../../main/resources/templates/assets/jig-data.js');
require('../../main/resources/templates/assets/jig-glossary.js');

const mermaid = require('../../main/resources/templates/assets/jig-mermaid.js')

test.describe('jig-mermaid.js', () => {

    test.describe('builder', () => {
        const sut = mermaid.builder;


        test.describe('buildVisibleDiagramRelations', () => {
            const packages = [
                {fqn: 'app.a'},
                {fqn: 'app.b'},
                {fqn: 'app.c'},
                {fqn: 'lib.x'},
            ];
            const relations = [
                {from: 'app.a', to: 'app.b'},
                {from: 'app.b', to: 'app.c'},
                {from: 'app.c', to: 'lib.x'},
            ];

            test('フィルタなしで全パッケージを含む', () => {
                const result = sut.buildVisibleDiagramRelations(packages, relations, [], {
                    packageFilterFqn: [],
                    aggregationDepth: 0,
                    transitiveReductionEnabled: false
                });
                assert.equal(result.packageFqns.size, 4);
                assert.equal(result.uniqueRelations.length, 3);
            });

            test('パッケージフィルタを適用する', () => {
                const result = sut.buildVisibleDiagramRelations(packages, relations, [], {
                    packageFilterFqn: ['app'],
                    aggregationDepth: 0,
                    transitiveReductionEnabled: false
                });
                assert.deepEqual(Array.from(result.packageFqns).sort(), ['app.a', 'app.b', 'app.c']);
                assert.equal(result.uniqueRelations.length, 2);
            });

            test('推移的簡約を適用する', () => {
                const directRelations = [
                    {from: 'a', to: 'b'},
                    {from: 'b', to: 'c'},
                    {from: 'a', to: 'c'}, // 推移的
                ];
                const pkgs = [{fqn: 'a'}, {fqn: 'b'}, {fqn: 'c'}];
                const result = sut.buildVisibleDiagramRelations(pkgs, directRelations, [], {
                    packageFilterFqn: [],
                    aggregationDepth: 0,
                    transitiveReductionEnabled: true
                });
                assert.equal(result.uniqueRelations.length, 2);
                assert.ok(!result.uniqueRelations.some(r => r.from === 'a' && r.to === 'c'));
            });
        });

        test.describe('buildMermaidDiagramSource', () => {
            test('図ソースを生成する', () => {
                const visibleSet = new Set(['app.a', 'app.b']);
                const relations = [{from: 'app.a', to: 'app.b'}];
                const {source} = sut.buildMermaidDiagramSource(visibleSet, relations, {
                    diagramDirection: 'TD',
                    focusedPackageFqn: null
                });
                assert.ok(source.includes('graph TD'));
                assert.ok(source.includes('-->'));
            });

            test('clickHandlerName を指定するとクリック行を含む', () => {
                const visibleSet = new Set(['app.a']);
                const {source} = sut.buildMermaidDiagramSource(visibleSet, [], {
                    diagramDirection: 'TD',
                    focusedPackageFqn: null,
                    clickHandlerName: 'myHandler'
                });
                assert.ok(source.includes('click'));
                assert.ok(source.includes('myHandler'));
            });

            test('clickHandlerName を省略するとクリック行を含まない', () => {
                const visibleSet = new Set(['app.a']);
                const {source} = sut.buildMermaidDiagramSource(visibleSet, [], {
                    diagramDirection: 'TD',
                    focusedPackageFqn: null
                });
                assert.ok(!source.includes('click'));
            });

            test('focusedPackageFqn のノードを強調する', () => {
                const visibleSet = new Set(['app.a', 'app.b']);
                const {source} = sut.buildMermaidDiagramSource(visibleSet, [], {
                    diagramDirection: 'TD',
                    focusedPackageFqn: 'app.a'
                });
                assert.ok(source.includes('style') && source.includes('font-weight:bold'));
            });

            test('focusedPackageFqn がノードに存在しない場合は強調スタイルを出力しない', () => {
                const visibleSet = new Set(['app.a', 'app.b']);
                const {source} = sut.buildMermaidDiagramSource(visibleSet, [], {
                    diagramDirection: 'TD',
                    focusedPackageFqn: 'app.not-in-diagram'
                });
                assert.ok(!source.includes('undefined'), '"undefined" がMermaidソースに含まれないこと');
            });

            test('subgraph外向きエッジは長さを調整する', () => {
                const visibleSet = new Set(['app.a', 'app.b', 'lib.x']);
                const relations = [
                    {from: 'app.a', to: 'app.b'},
                    {from: 'app.a', to: 'lib.x'},
                    {from: 'app.b', to: 'lib.x'},
                ];
                const {source} = sut.buildMermaidDiagramSource(visibleSet, relations, {
                    diagramDirection: 'TD',
                    focusedPackageFqn: null
                });
                assert.ok(source.includes('--->'), '浅いノードから外部へのエッジは長くなること');
                assert.ok(source.includes('-->'), '標準長のエッジも含まれること');
            });
        });

        test('buildDiagramEdgeLines: 相互依存の双方向リンクを生成する', () => {
            const {ensureNodeId} = sut.buildDiagramNodeMaps(new Set(['a', 'b']));
            const result = sut.buildDiagramEdgeLines(
                [{from: 'a', to: 'b'}, {from: 'b', to: 'a'}],
                ensureNodeId
            );
            assert.equal(result.edgeLines.some(line => line.includes('<-->')), true);
            assert.equal(result.linkStyles.length, 1);
        });

        test('buildDiagramNodeLabel: サブグラフ配下のラベルを短縮する', () => {
            const label = sut.buildDiagramNodeLabel(
                'com.example.domain.model',
                'com.example.domain.model',
                'com.example.domain'
            );
            assert.equal(label, 'model');
        });

        test('buildDiagramSubgraphLabel: グロッサリー未登録の場合は単純名を返す', () => {
            const label = sut.buildDiagramSubgraphLabel('com.example.domain');
            assert.equal(label, 'domain');
        });

        test('buildDiagramNodeTooltip: FQNを返す', () => {
            assert.equal(sut.buildDiagramNodeTooltip('com.example.domain'), 'com.example.domain');
            assert.equal(sut.buildDiagramNodeTooltip(null), '');
        });

        test('buildDiagramGroupTree: 共通プレフィックスでグループ化する', () => {
            const visibleFqns = ['com.example.a', 'com.example.b'];
            const nodeIdByFqn = new Map([
                ['com.example.a', 'P0'],
                ['com.example.b', 'P1'],
            ]);

            const rootGroup = sut.buildDiagramGroupTree(visibleFqns, nodeIdByFqn);

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
                            ['com.example.domain', {
                                key: 'com.example.domain',
                                nodes: ['P1', 'P2'],
                                children: new Map()
                            }],
                        ]),
                    }],
                ]),
            };
            const addNodeLines = (lines, nodeId) => {
                lines.push(`node ${nodeId}`);
            };

            const lines = sut.buildSubgraphLines(rootGroup, addNodeLines, text => text);

            assert.equal(lines.some(line => line.includes('node ROOT')), true);
            assert.equal(lines.some(line => line.includes('node P0')), true);
            assert.equal(lines.some(line => line.includes('subgraph') && line.includes('["example"]')), true);
            assert.equal(lines.some(line => line.includes('subgraph') && line.includes('["domain"]')), true);
        });

        test('buildDiagramNodeLines: クリックハンドラ名を埋め込む', () => {
            const visibleSet = new Set(['app.a']);
            const {nodeIdByFqn, nodeIdToFqn, nodeLabelById} = sut.buildDiagramNodeMaps(visibleSet);
            const nodeLines = sut.buildDiagramNodeLines(
                visibleSet,
                nodeIdByFqn,
                {
                    nodeIdToFqn,
                    nodeLabelById,
                    escapeMermaidText: text => text,
                    clickHandlerName: 'test-handler-name',
                    parentFqnsWithRelations: new Set()
                }
            );
            const clickLine = nodeLines.find(line => line.startsWith('click '));
            assert.ok(clickLine);
            assert.equal(clickLine.includes('test-handler-name'), true);
        });

        test('buildDiagramNodeLines: nodeClickUrlCallbackでhrefクリックを埋め込む', () => {
            const visibleSet = new Set(['app.a']);
            const {nodeIdByFqn, nodeIdToFqn, nodeLabelById} = sut.buildDiagramNodeMaps(visibleSet);
            const nodeLines = sut.buildDiagramNodeLines(
                visibleSet,
                nodeIdByFqn,
                {
                    nodeIdToFqn,
                    nodeLabelById,
                    escapeMermaidText: text => text,
                    nodeClickUrlCallback: (fqn) => `#anchor-${fqn}`,
                    parentFqnsWithRelations: new Set()
                }
            );
            const clickLine = nodeLines.find(line => line.startsWith('click ') && line.includes('href'));
            assert.ok(clickLine, 'href クリック行があるはず');
            assert.ok(clickLine.includes('href "#anchor-app.a"'), `click ... href "..." の形式のはず: ${clickLine}`);
        });


        test.describe('MermaidBuilder.applyThemeClassDefs', () => {
            test('全テーマのclassDefを追加する', () => {
                const builder = new sut.MermaidBuilder();
                builder.applyThemeClassDefs();
                const code = builder.build();
                assert.ok(code.includes('classDef inbound'), 'inbound classDef should be present');
                assert.ok(code.includes('classDef usecase'), 'usecase classDef should be present');
                assert.ok(code.includes('classDef outbound'), 'outbound classDef should be present');
                assert.ok(code.includes('classDef inactive'), 'inactive classDef should be present');
            });

            test('nodeStyleDefsから色コードが正しく取得される', () => {
                assert.equal(sut.nodeStyleDefs.inbound, 'fill:#E8F0FE,stroke:#2E5C8A');
                assert.equal(sut.nodeStyleDefs.usecase, 'fill:#E6F8F0,stroke:#2D7A4A');
                assert.equal(sut.nodeStyleDefs.outbound, 'fill:#FFF0E6,stroke:#CC6600');
                assert.equal(sut.nodeStyleDefs.inactive, 'fill:#e0e0e0,stroke:#aaa');
            });
        });

// ----- getNodeDefinition & MermaidBuilder node shapes -----

        test.describe('Mermaid node shapes', () => {
            test('getNodeDefinition: default shape is class (square)', () => {
                const def = sut.getNodeDefinition('id1', 'label1');
                assert.equal(def, 'id1["label1"]');
            });

            test('getNodeDefinition: method shape is rounded', () => {
                const def = sut.getNodeDefinition('id1', 'label1', 'method');
                assert.equal(def, 'id1(["label1"])');
            });

            test('getNodeDefinition: package shape is stacked', () => {
                const def = sut.getNodeDefinition('id1', 'label1', 'package');
                assert.equal(def, 'id1@{shape: st-rect, label: "label1"}');
            });

            test('getNodeDefinition: database shape is cylindrical', () => {
                const def = sut.getNodeDefinition('id1', 'label1', 'database');
                assert.equal(def, 'id1[("label1")]');
            });

            test('getNodeDefinition: external shape is double circle', () => {
                const def = sut.getNodeDefinition('id1', 'label1', 'external');
                assert.equal(def, 'id1(("label1"))');
            });

            test('getNodeDefinition: fallback to raw shape string', () => {
                const def = sut.getNodeDefinition('id1', 'label1', '>"$LABEL"]');
                assert.equal(def, 'id1>"label1"]');
            });

            test('MermaidBuilder.addNode uses default shape', () => {
                const builder = new sut.MermaidBuilder();
                builder.addNode('id1', 'label1');
                const code = builder.build();
                assert.ok(code.includes('id1["label1"]'));
            });

            test('MermaidBuilder.addNodeToSubgraph uses specified shape', () => {
                const builder = new sut.MermaidBuilder();
                const sg = builder.startSubgraph('sg1');
                builder.addNodeToSubgraph(sg, 'id1', 'label1', 'method');
                const code = builder.build();
                assert.ok(code.includes('id1(["label1"])'));
            });
        });

    });

    test.describe('graph', () => {
        const sut = mermaid.graph;

// ----- detectStronglyConnectedComponents -----

        test.describe('detectStronglyConnectedComponents', () => {
            test('循環を検出する', () => {
                const graph = new Map([
                    ['a', ['b']],
                    ['b', ['c']],
                    ['c', ['a', 'd']],
                    ['d', ['e']],
                    ['e', ['f']],
                    ['f', ['d']],
                ]);
                const sccs = sut.detectStronglyConnectedComponents(graph);
                const sortedSccs = sccs.map(scc => scc.sort()).sort((a, b) => a[0].localeCompare(b[0]));
                assert.deepEqual(sortedSccs, [['a', 'b', 'c'], ['d', 'e', 'f']]);
            });
        });

// ----- transitiveReduction -----

        test.describe('transitiveReduction', () => {
            test('単純な推移関係を簡約する', () => {
                const relations = [
                    {from: 'a', to: 'b'},
                    {from: 'b', to: 'c'},
                    {from: 'a', to: 'c'},
                ];
                const result = sut.transitiveReduction(relations);
                assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'b>c']);
            });

            test('循環参照は対象外とする', () => {
                const relations = [
                    {from: 'a', to: 'b'},
                    {from: 'b', to: 'a'},
                    {from: 'a', to: 'c'},
                ];
                const result = sut.transitiveReduction(relations);
                assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'a>c', 'b>a']);
            });

            test('循環ではないが簡約対象でもない', () => {
                const relations = [
                    {from: 'a', to: 'b'},
                    {from: 'c', to: 'd'},
                ];
                const result = sut.transitiveReduction(relations);
                assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'c>d']);
            });

            test('循環からの関係は簡約対象にしない', () => {
                const relations = [
                    {from: 'a', to: 'b'},
                    {from: 'b', to: 'a'}, // cycle
                    {from: 'b', to: 'c'},
                    {from: 'a', to: 'c'},
                ];
                const result = sut.transitiveReduction(relations);
                assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'a>c', 'b>a', 'b>c']);
            });
        });

// ----- computeSubgraphDepthMap / computeOutboundEdgeLengths -----

        test.describe('computeSubgraphDepthMap', () => {
            test('DAGの最長パスで深さを計算する', () => {
                const {depthMap, maxDepth} = sut.computeSubgraphDepthMap({
                    nodesInSubgraph: ['A', 'B', 'C', 'D'],
                    edges: [
                        {from: 'A', to: 'B'},
                        {from: 'B', to: 'C'},
                        {from: 'A', to: 'D'},
                    ],
                });
                assert.equal(depthMap.get('A'), 1);
                assert.equal(depthMap.get('B'), 2);
                assert.equal(depthMap.get('C'), 3);
                assert.equal(depthMap.get('D'), 2);
                assert.equal(maxDepth, 3);
            });

            test('循環のみの場合は全ノードをdepth=1で開始する', () => {
                const {depthMap, maxDepth} = sut.computeSubgraphDepthMap({
                    nodesInSubgraph: ['A', 'B'],
                    edges: [
                        {from: 'A', to: 'B'},
                        {from: 'B', to: 'A'},
                    ],
                });
                assert.ok(depthMap.get('A') >= 1);
                assert.ok(depthMap.get('B') >= 1);
                assert.ok(maxDepth >= 1);
            });
        });

        test.describe('computeOutboundEdgeLengths', () => {
            test('浅いノードから外部へのエッジが長くなる', () => {
                const {edgeLengthByKey} = sut.computeOutboundEdgeLengths({
                    nodesInSubgraph: ['A', 'B', 'C'],
                    edges: [
                        {from: 'A', to: 'B'},
                        {from: 'B', to: 'C'},
                        {from: 'A', to: 'X'},
                        {from: 'C', to: 'Y'},
                    ],
                });
                assert.equal(edgeLengthByKey.get('A::X'), 3);
                assert.equal(edgeLengthByKey.get('C::Y'), 1);
                assert.equal(edgeLengthByKey.get('A::B'), 1);
            });
        });
    });

    test.describe('render(DOM操作を伴う）', () => {
        const sut = mermaid.render;

        test.describe('estimateEdgeCount', () => {
            test('Mermaid矢印を数える', () => {
                assert.equal(sut.estimateEdgeCount('A --> B'), 1);
                assert.equal(sut.estimateEdgeCount('A --> B\nB --> C'), 2);
                assert.equal(sut.estimateEdgeCount('A <--> B'), 1);  // bidirectional
                assert.equal(sut.estimateEdgeCount(''), 0);
                assert.equal(sut.estimateEdgeCount(null), 0);
            });
        });

        test.describe('isTooLarge', () => {
            test('閾値を超えるとtrueを返す', () => {
                const max = 'a'.repeat(50000);
                const over = 'a'.repeat(50001);

                assert.equal(sut.isTooLarge(max), false);
                assert.equal(sut.isTooLarge(over), true);
            });
        });

        class Element {
            constructor(tagName) {
                this.tagName = tagName;
                this.children = [];
                this.textContent = '';
                this.className = '';
                this.style = {};
                this._classSet = new Set();
                this.classList = {
                    add: (name) => {
                        this._classSet.add(name);
                        this.className = Array.from(this._classSet).join(' ');
                    },
                    remove: (name) => {
                        this._classSet.delete(name);
                        this.className = Array.from(this._classSet).join(' ');
                    },
                    contains: (name) => this._classSet.has(name),
                };
            }

            appendChild(child) {
                this.children.push(child);
                return child;
            }

            addEventListener() {
            }
        }

        function setupGlobals() {
            global.window = {
                addEventListener() {
                },
                setTimeout: global.setTimeout,
            };
            global.document = {
                body: {
                    classList: {
                        contains: () => false,
                    },
                },
                addEventListener() {
                },
                getElementsByClassName() {
                    return [];
                },
                createElement(tagName) {
                    return new Element(tagName);
                },
            };
        }

        test('renderTooLargeDiagramは案内表示を追加する', () => {
            setupGlobals();

            const diagram = new Element('div');
            diagram.textContent = 'graph TD; A-->B;';

            sut.renderTooLargeDiagram(diagram, diagram.textContent);

            assert.equal(diagram.classList.contains('too-large'), true);
            assert.equal(diagram.textContent, '');
            assert.equal(diagram.children.length, 1);

            const container = diagram.children[0];
            assert.equal(container.className, 'mermaid-too-large');
            assert.equal(container.children.length, 2);

            const message = container.children[0];
            assert.equal(message.tagName, 'p');
            assert.equal(message.className, 'mermaid-too-large__message');
            assert.equal(message.textContent, '図が大きいため表示を制限しています');

            const actions = container.children[1];
            assert.equal(actions.className, 'mermaid-too-large__actions');
            assert.equal(actions.children.length, 2);
            assert.equal(actions.children[0].tagName, 'button');
            assert.equal(actions.children[0].textContent, '上限を上げて描画する');
            assert.equal(actions.children[1].tagName, 'button');
            assert.equal(actions.children[1].textContent, 'テキストで表示');
        });

        test('renderTooLargeDiagramはmessageTextオプションで表示メッセージを変更できる', () => {
            setupGlobals();

            const diagram = new Element('div');
            sut.renderTooLargeDiagram(diagram, 'graph TD; A-->B;', {messageText: '関連数が多いため表示を制限しています（エッジ数: 42）'});

            const container = diagram.children[0];
            const message = container.children[0];
            assert.equal(message.textContent, '関連数が多いため表示を制限しています（エッジ数: 42）');
        });

        test('flashButtonLabelはラベルを戻す', () => {
            setupGlobals();

            const button = new Element('button');
            button.textContent = '元のラベル';

            let timerCallback = null;
            const originalSetTimeout = global.window.setTimeout;
            global.window.setTimeout = (callback) => {
                timerCallback = callback;
            };

            try {
                sut.flashButtonLabel(button, '更新');
                assert.equal(button.textContent, '更新');
                assert.equal(typeof timerCallback, 'function');
                timerCallback();
                assert.equal(button.textContent, '元のラベル');
            } finally {
                global.window.setTimeout = originalSetTimeout;
            }
        });
    });
});
