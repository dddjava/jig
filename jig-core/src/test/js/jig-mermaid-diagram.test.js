const test = require('node:test');
const assert = require('node:assert/strict');

const common = require('../../main/resources/templates/assets/jig-common.js');
const jigMermaid = require('../../main/resources/templates/assets/jig-mermaid.js');

test.describe('package-diagram', () => {
    test.describe('FQN ユーティリティ', () => {
        test('getPackageFqnFromTypeFqn: 型FQNからパッケージFQNを返す', () => {
            assert.equal(jigMermaid.getPackageFqnFromTypeFqn('com.example.domain.User'), 'com.example.domain');
            assert.equal(jigMermaid.getPackageFqnFromTypeFqn('TopLevelClass'), '(default)');
            assert.equal(jigMermaid.getPackageFqnFromTypeFqn(null), '(default)');
        });

        test('isWithinPackageFilters: パッケージフィルタのマッチ判定', () => {
            assert.equal(jigMermaid.isWithinPackageFilters('com.example', ['com.example']), true);
            assert.equal(jigMermaid.isWithinPackageFilters('com.example.domain', ['com.example']), true);
            assert.equal(jigMermaid.isWithinPackageFilters('com.other', ['com.example']), false);
            assert.equal(jigMermaid.isWithinPackageFilters('com.example', []), true);
        });

        test('getAggregatedFqn: 指定深さで集約する', () => {
            assert.equal(jigMermaid.getAggregatedFqn('com.example.domain', 2), 'com.example');
            assert.equal(jigMermaid.getAggregatedFqn('com.example.domain', 0), 'com.example.domain');
            assert.equal(jigMermaid.getAggregatedFqn('(default)', 2), '(default)');
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
            const result = jigMermaid.buildVisibleDiagramRelations(packages, relations, [], {packageFilterFqn: [], aggregationDepth: 0, transitiveReductionEnabled: false});
            assert.equal(result.packageFqns.size, 4);
            assert.equal(result.uniqueRelations.length, 3);
        });

        test('パッケージフィルタを適用する', () => {
            const result = jigMermaid.buildVisibleDiagramRelations(packages, relations, [], {packageFilterFqn: ['app'], aggregationDepth: 0, transitiveReductionEnabled: false});
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
            const result = jigMermaid.buildVisibleDiagramRelations(pkgs, directRelations, [], {packageFilterFqn: [], aggregationDepth: 0, transitiveReductionEnabled: true});
            assert.equal(result.uniqueRelations.length, 2);
            assert.ok(!result.uniqueRelations.some(r => r.from === 'a' && r.to === 'c'));
        });
    });

    test.describe('buildMermaidDiagramSource', () => {
        test('図ソースを生成する', () => {
            const visibleSet = new Set(['app.a', 'app.b']);
            const relations = [{from: 'app.a', to: 'app.b'}];
            const {source} = jigMermaid.buildMermaidDiagramSource(visibleSet, relations, {diagramDirection: 'TD', focusedPackageFqn: null});
            assert.ok(source.includes('graph TD'));
            assert.ok(source.includes('-->'));
        });

        test('clickHandlerName を指定するとクリック行を含む', () => {
            const visibleSet = new Set(['app.a']);
            const {source} = jigMermaid.buildMermaidDiagramSource(visibleSet, [], {diagramDirection: 'TD', focusedPackageFqn: null, clickHandlerName: 'myHandler'});
            assert.ok(source.includes('click'));
            assert.ok(source.includes('myHandler'));
        });

        test('clickHandlerName を省略するとクリック行を含まない', () => {
            const visibleSet = new Set(['app.a']);
            const {source} = jigMermaid.buildMermaidDiagramSource(visibleSet, [], {diagramDirection: 'TD', focusedPackageFqn: null});
            assert.ok(!source.includes('click'));
        });

        test('focusedPackageFqn のノードを強調する', () => {
            const visibleSet = new Set(['app.a', 'app.b']);
            const {source} = jigMermaid.buildMermaidDiagramSource(visibleSet, [], {diagramDirection: 'TD', focusedPackageFqn: 'app.a'});
            assert.ok(source.includes('style') && source.includes('font-weight:bold'));
        });

        test('focusedPackageFqn がノードに存在しない場合は強調スタイルを出力しない', () => {
            const visibleSet = new Set(['app.a', 'app.b']);
            const {source} = jigMermaid.buildMermaidDiagramSource(visibleSet, [], {diagramDirection: 'TD', focusedPackageFqn: 'app.not-in-diagram'});
            assert.ok(!source.includes('undefined'), '"undefined" がMermaidソースに含まれないこと');
        });

        test('subgraph外向きエッジは長さを調整する', () => {
            const visibleSet = new Set(['app.a', 'app.b', 'lib.x']);
            const relations = [
                {from: 'app.a', to: 'app.b'},
                {from: 'app.a', to: 'lib.x'},
                {from: 'app.b', to: 'lib.x'},
            ];
            const {source} = jigMermaid.buildMermaidDiagramSource(visibleSet, relations, {diagramDirection: 'TD', focusedPackageFqn: null});
            assert.ok(source.includes('--->'), '浅いノードから外部へのエッジは長くなること');
            assert.ok(source.includes('-->'), '標準長のエッジも含まれること');
        });
    });
});
