const test = require('node:test');
const assert = require('node:assert/strict');

const PackageDiagramModule = require('../../main/resources/templates/assets/jig-mermaid.js').PackageDiagramModule;
require('../../main/resources/templates/assets/jig-glossary.js');
const PackageApp = require("../../main/resources/templates/assets/package");

test.describe('jig-mermaid.js', () => {
    test.describe('PackageDiagramModule', () => {
        test.describe('FQN ユーティリティ', () => {
            test('getPackageFqnFromTypeFqn: 型FQNからパッケージFQNを返す', () => {
                assert.equal(PackageDiagramModule.getPackageFqnFromTypeFqn('com.example.domain.User'), 'com.example.domain');
                assert.equal(PackageDiagramModule.getPackageFqnFromTypeFqn('TopLevelClass'), '(default)');
                assert.equal(PackageDiagramModule.getPackageFqnFromTypeFqn(null), '(default)');
            });

            test('isWithinPackageFilters: パッケージフィルタのマッチ判定', () => {
                assert.equal(PackageDiagramModule.isWithinPackageFilters('com.example', ['com.example']), true);
                assert.equal(PackageDiagramModule.isWithinPackageFilters('com.example.domain', ['com.example']), true);
                assert.equal(PackageDiagramModule.isWithinPackageFilters('com.other', ['com.example']), false);
                assert.equal(PackageDiagramModule.isWithinPackageFilters('com.example', []), true);
            });

            test('getAggregatedFqn: 指定深さで集約する', () => {
                assert.equal(PackageDiagramModule.getAggregatedFqn('com.example.domain', 2), 'com.example');
                assert.equal(PackageDiagramModule.getAggregatedFqn('com.example.domain', 0), 'com.example.domain');
                assert.equal(PackageDiagramModule.getAggregatedFqn('(default)', 2), '(default)');
            });
        });

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
                const result = PackageDiagramModule.buildVisibleDiagramRelations(packages, relations, [], {
                    packageFilterFqn: [],
                    aggregationDepth: 0,
                    transitiveReductionEnabled: false
                });
                assert.equal(result.packageFqns.size, 4);
                assert.equal(result.uniqueRelations.length, 3);
            });

            test('パッケージフィルタを適用する', () => {
                const result = PackageDiagramModule.buildVisibleDiagramRelations(packages, relations, [], {
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
                const result = PackageDiagramModule.buildVisibleDiagramRelations(pkgs, directRelations, [], {
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
                const {source} = PackageDiagramModule.buildMermaidDiagramSource(visibleSet, relations, {
                    diagramDirection: 'TD',
                    focusedPackageFqn: null
                });
                assert.ok(source.includes('graph TD'));
                assert.ok(source.includes('-->'));
            });

            test('clickHandlerName を指定するとクリック行を含む', () => {
                const visibleSet = new Set(['app.a']);
                const {source} = PackageDiagramModule.buildMermaidDiagramSource(visibleSet, [], {
                    diagramDirection: 'TD',
                    focusedPackageFqn: null,
                    clickHandlerName: 'myHandler'
                });
                assert.ok(source.includes('click'));
                assert.ok(source.includes('myHandler'));
            });

            test('clickHandlerName を省略するとクリック行を含まない', () => {
                const visibleSet = new Set(['app.a']);
                const {source} = PackageDiagramModule.buildMermaidDiagramSource(visibleSet, [], {
                    diagramDirection: 'TD',
                    focusedPackageFqn: null
                });
                assert.ok(!source.includes('click'));
            });

            test('focusedPackageFqn のノードを強調する', () => {
                const visibleSet = new Set(['app.a', 'app.b']);
                const {source} = PackageDiagramModule.buildMermaidDiagramSource(visibleSet, [], {
                    diagramDirection: 'TD',
                    focusedPackageFqn: 'app.a'
                });
                assert.ok(source.includes('style') && source.includes('font-weight:bold'));
            });

            test('focusedPackageFqn がノードに存在しない場合は強調スタイルを出力しない', () => {
                const visibleSet = new Set(['app.a', 'app.b']);
                const {source} = PackageDiagramModule.buildMermaidDiagramSource(visibleSet, [], {
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
                const {source} = PackageDiagramModule.buildMermaidDiagramSource(visibleSet, relations, {
                    diagramDirection: 'TD',
                    focusedPackageFqn: null
                });
                assert.ok(source.includes('--->'), '浅いノードから外部へのエッジは長くなること');
                assert.ok(source.includes('-->'), '標準長のエッジも含まれること');
            });
        });

        test('buildDiagramEdgeLines: 相互依存の双方向リンクを生成する', () => {
            const {ensureNodeId} = PackageDiagramModule.buildDiagramNodeMaps(new Set(['a', 'b']));
            const result = PackageDiagramModule.buildDiagramEdgeLines(
                [{from: 'a', to: 'b'}, {from: 'b', to: 'a'}],
                ensureNodeId
            );
            assert.equal(result.edgeLines.some(line => line.includes('<-->')), true);
            assert.equal(result.linkStyles.length, 1);
        });

        test('buildDiagramNodeLabel: サブグラフ配下のラベルを短縮する', () => {
            const label = PackageDiagramModule.buildDiagramNodeLabel(
                'com.example.domain.model',
                'com.example.domain.model',
                'com.example.domain'
            );
            assert.equal(label, 'model');
        });

        test('buildDiagramSubgraphLabel: 親サブグラフ配下ならプレフィックスを省略する', () => {
            const label = PackageDiagramModule.buildDiagramSubgraphLabel('com.example.domain', 'com.example');
            assert.equal(label, 'domain');
        });

        test('buildDiagramNodeTooltip: FQNを返す', () => {
            assert.equal(PackageDiagramModule.buildDiagramNodeTooltip('com.example.domain'), 'com.example.domain');
            assert.equal(PackageDiagramModule.buildDiagramNodeTooltip(null), '');
        });

        test('buildDiagramGroupTree: 共通プレフィックスでグループ化する', () => {
            const visibleFqns = ['com.example.a', 'com.example.b'];
            const nodeIdByFqn = new Map([
                ['com.example.a', 'P0'],
                ['com.example.b', 'P1'],
            ]);

            const rootGroup = PackageDiagramModule.buildDiagramGroupTree(visibleFqns, nodeIdByFqn);

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

            const lines = PackageDiagramModule.buildSubgraphLines(rootGroup, addNodeLines, text => text);

            assert.equal(lines.some(line => line.includes('node ROOT')), true);
            assert.equal(lines.some(line => line.includes('node P0')), true);
            assert.equal(lines.some(line => line.includes('subgraph') && line.includes('["com.example"]')), true);
            assert.equal(lines.some(line => line.includes('subgraph') && line.includes('["domain"]')), true);
        });

        test('buildDiagramNodeLines: クリックハンドラ名を埋め込む', () => {
            const visibleSet = new Set(['app.a']);
            const {nodeIdByFqn, nodeIdToFqn, nodeLabelById} = PackageDiagramModule.buildDiagramNodeMaps(visibleSet);
            const nodeLines = PackageDiagramModule.buildDiagramNodeLines(
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
            const {nodeIdByFqn, nodeIdToFqn, nodeLabelById} = PackageDiagramModule.buildDiagramNodeMaps(visibleSet);
            const nodeLines = PackageDiagramModule.buildDiagramNodeLines(
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
    });
});
