const test = require('node:test');
const assert = require('node:assert/strict');

const pkg = require('../../main/resources/templates/assets/package.js');

test.describe('package.js 関連フィルタ', () => {
    test('directモードは隣接のみを含める', () => {
        pkg.setAggregationDepth(0);
        pkg.setRelatedFilterMode('direct');
        const relations = [
            {from: 'app.domain.a', to: 'app.domain.b'},
            {from: 'app.domain.b', to: 'app.domain.c'},
        ];

        const related = pkg.collectRelatedSet('app.domain.a', relations);

        assert.deepEqual(Array.from(related).sort(), ['app.domain.a', 'app.domain.b']);
    });

    test('allモードは推移的に辿る', () => {
        pkg.setAggregationDepth(0);
        pkg.setRelatedFilterMode('all');
        const relations = [
            {from: 'app.domain.a', to: 'app.domain.b'},
            {from: 'app.domain.b', to: 'app.domain.c'},
        ];

        const related = pkg.collectRelatedSet('app.domain.a', relations);

        assert.deepEqual(
            Array.from(related).sort(),
            ['app.domain.a', 'app.domain.b', 'app.domain.c']
        );
    });
});

test.describe('package.js 集計', () => {
    test('パッケージフィルタで対象のみ数える', () => {
        pkg.setAggregationDepth(0);
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

    test('関連フィルタは集計深さを反映する', () => {
        pkg.setAggregationDepth(1);
        pkg.setRelatedFilterMode('all');
        const packages = [
            {fqn: 'app.domain.a'},
            {fqn: 'app.domain.b'},
            {fqn: 'app.other.c'},
        ];
        const relations = [
            {from: 'app.domain.a', to: 'app.domain.b'},
            {from: 'app.domain.b', to: 'app.other.c'},
        ];

        const stats = pkg.buildAggregationStatsForRelated(packages, relations, 'app.domain.a', 1);
        const depth1 = stats.get(1);

        assert.equal(depth1.packageCount, 1);
        assert.equal(depth1.relationCount, 0);
    });

    test('directモードの複合フィルタ集計', () => {
        pkg.setAggregationDepth(0);
        pkg.setRelatedFilterMode('direct');
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
            0
        );
        const depth0 = stats.get(0);

        assert.equal(depth0.packageCount, 2);
        assert.equal(depth0.relationCount, 1);
    });

    test('allモードの複合フィルタ集計', () => {
        pkg.setAggregationDepth(0);
        pkg.setRelatedFilterMode('all');
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
            0
        );
        const depth0 = stats.get(0);

        assert.equal(depth0.packageCount, 3);
        assert.equal(depth0.relationCount, 2);
    });
});
