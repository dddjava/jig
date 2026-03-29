const test = require('node:test');
const assert = require('node:assert/strict');

const common = require('../../main/resources/templates/assets/jig-common.js');
const pkgDiagram = require('../../main/resources/templates/assets/package-diagram.js');

test.describe('package-diagram', () => {
    test.describe('FQN ユーティリティ', () => {
        test('getPackageFqnFromTypeFqn: 型FQNからパッケージFQNを返す', () => {
            assert.equal(pkgDiagram.getPackageFqnFromTypeFqn('com.example.domain.User'), 'com.example.domain');
            assert.equal(pkgDiagram.getPackageFqnFromTypeFqn('TopLevelClass'), '(default)');
            assert.equal(pkgDiagram.getPackageFqnFromTypeFqn(null), '(default)');
        });

        test('isWithinPackageFilters: パッケージフィルタのマッチ判定', () => {
            assert.equal(pkgDiagram.isWithinPackageFilters('com.example', ['com.example']), true);
            assert.equal(pkgDiagram.isWithinPackageFilters('com.example.domain', ['com.example']), true);
            assert.equal(pkgDiagram.isWithinPackageFilters('com.other', ['com.example']), false);
            assert.equal(pkgDiagram.isWithinPackageFilters('com.example', []), true);
        });

        test('getAggregatedFqn: 指定深さで集約する', () => {
            assert.equal(pkgDiagram.getAggregatedFqn('com.example.domain', 2), 'com.example');
            assert.equal(pkgDiagram.getAggregatedFqn('com.example.domain', 0), 'com.example.domain');
            assert.equal(pkgDiagram.getAggregatedFqn('(default)', 2), '(default)');
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
            const result = pkgDiagram.buildVisibleDiagramRelations(packages, relations, [], {packageFilterFqn: [], aggregationDepth: 0, transitiveReductionEnabled: false});
            assert.equal(result.packageFqns.size, 4);
            assert.equal(result.uniqueRelations.length, 3);
        });

        test('パッケージフィルタを適用する', () => {
            const result = pkgDiagram.buildVisibleDiagramRelations(packages, relations, [], {packageFilterFqn: ['app'], aggregationDepth: 0, transitiveReductionEnabled: false});
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
            const result = pkgDiagram.buildVisibleDiagramRelations(pkgs, directRelations, [], {packageFilterFqn: [], aggregationDepth: 0, transitiveReductionEnabled: true});
            assert.equal(result.uniqueRelations.length, 2);
            assert.ok(!result.uniqueRelations.some(r => r.from === 'a' && r.to === 'c'));
        });
    });

    test.describe('buildMermaidDiagramSource', () => {
        test('図ソースを生成する', () => {
            const visibleSet = new Set(['app.a', 'app.b']);
            const relations = [{from: 'app.a', to: 'app.b'}];
            const nameByFqn = new Map([['app.a', 'A'], ['app.b', 'B']]);
            const {source} = pkgDiagram.buildMermaidDiagramSource(visibleSet, relations, nameByFqn, 'TD', null);
            assert.ok(source.includes('graph TD'));
            assert.ok(source.includes('-->'));
        });

        test('clickHandlerName を指定するとクリック行を含む', () => {
            const visibleSet = new Set(['app.a']);
            const nameByFqn = new Map([['app.a', 'A']]);
            const {source} = pkgDiagram.buildMermaidDiagramSource(visibleSet, [], nameByFqn, 'TD', null, {clickHandlerName: 'myHandler'});
            assert.ok(source.includes('click'));
            assert.ok(source.includes('myHandler'));
        });

        test('clickHandlerName を省略するとクリック行を含まない', () => {
            const visibleSet = new Set(['app.a']);
            const nameByFqn = new Map([['app.a', 'A']]);
            const {source} = pkgDiagram.buildMermaidDiagramSource(visibleSet, [], nameByFqn, 'TD', null);
            assert.ok(!source.includes('click'));
        });

        test('focusedPackageFqn のノードを強調する', () => {
            const visibleSet = new Set(['app.a', 'app.b']);
            const nameByFqn = new Map([['app.a', 'A'], ['app.b', 'B']]);
            const {source} = pkgDiagram.buildMermaidDiagramSource(visibleSet, [], nameByFqn, 'TD', 'app.a');
            assert.ok(source.includes('style') && source.includes('font-weight:bold'));
        });
    });
});
