const test = require('node:test');
const assert = require('node:assert/strict');

const pkg = require('../../main/resources/templates/assets/package.js');

test('collectRelatedSet direct mode keeps only neighbors', () => {
    pkg.setAggregationDepth(0);
    pkg.setRelatedFilterMode('direct');
    const relations = [
        {from: 'app.domain.a', to: 'app.domain.b'},
        {from: 'app.domain.b', to: 'app.domain.c'},
    ];

    const related = pkg.collectRelatedSet('app.domain.a', relations);

    assert.deepEqual(Array.from(related).sort(), ['app.domain.a', 'app.domain.b']);
});

test('collectRelatedSet all mode walks transitive dependencies', () => {
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

test('buildAggregationStatsForPackageFilter counts only filtered packages', () => {
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

test('buildAggregationStatsForRelated respects aggregation depth', () => {
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
