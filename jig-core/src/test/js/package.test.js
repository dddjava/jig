const test = require('node:test');
const assert = require('node:assert/strict');

const pkg = require('../../main/resources/templates/assets/package.js');

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
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this.textContent = '';
        this.className = '';
        this.classList = new ClassList();
        this.style = {};
        this.attributes = new Map();
        this.parentNode = null;
        this.value = '';
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
    }

    appendChild(child) {
        child.parentNode = this;
        this.children.push(child);
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
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.selectors = new Map();
        this.selectorsAll = new Map();
    }

    createElement(tagName) {
        return new Element(tagName);
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

function setPackageData(doc, data) {
    const dataElement = new Element('script');
    dataElement.textContent = JSON.stringify(data);
    doc.elementsById.set('package-data', dataElement);
    pkg.resetPackageSummaryCache();
}

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

test.describe('package.js データ取得', () => {
    test('パッケージデータは配列/オブジェクトの両方に対応する', () => {
        const doc = setupDocument();
        setPackageData(doc, [{fqn: 'app.a', name: 'A', classCount: 1, description: ''}]);

        const data = pkg.getPackageSummaryData();

        assert.equal(data.packages.length, 1);
        assert.equal(data.relations.length, 0);
    });

    test('パッケージ深さを取得する', () => {
        assert.equal(pkg.getPackageDepth(''), 0);
        assert.equal(pkg.getPackageDepth('(default)'), 0);
        assert.equal(pkg.getPackageDepth('app.domain'), 2);
    });

    test('最大深さを計算する', () => {
        const doc = setupDocument();
        setPackageData(doc, {
            packages: [
                {fqn: 'app.domain.a'},
                {fqn: 'app.b'},
                {fqn: 'app.domain.core.c'},
            ],
            relations: [],
        });

        assert.equal(pkg.getMaxPackageDepth(), 4);
    });

    test('共通プレフィックスの深さを計算する', () => {
        assert.equal(pkg.getCommonPrefixDepth([]), 0);
        assert.equal(pkg.getCommonPrefixDepth(['app.domain.a', 'app.domain.b']), 2);
        assert.equal(pkg.getCommonPrefixDepth(['app', 'lib.tool']), 0);
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

test.describe('package.js テーブルフィルタ', () => {
    test('パッケージフィルタで行の表示/非表示を切り替える', () => {
        const doc = setupDocument();
        const rows = buildPackageRows(doc, ['app.domain', 'app.other']);

        pkg.applyPackageFilterToTable('app.domain');

        assert.equal(rows[0].classList.contains('hidden'), false);
        assert.equal(rows[1].classList.contains('hidden'), true);
    });

    test('関連フィルタが未指定ならパッケージフィルタだけ適用する', () => {
        const doc = setupDocument();
        const rows = buildPackageRows(doc, ['app.domain', 'app.other']);
        pkg.setPackageFilterFqn('app.domain');

        pkg.applyRelatedFilterToTable(null);

        assert.equal(rows[0].classList.contains('hidden'), false);
        assert.equal(rows[1].classList.contains('hidden'), true);
    });

    test('関連フィルタで関係する行のみ表示する', () => {
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
        });
        const rows = buildPackageRows(doc, ['app.a', 'app.b', 'app.c']);
        pkg.setAggregationDepth(0);
        pkg.setRelatedFilterMode('direct');
        pkg.setPackageFilterFqn(null);

        pkg.applyRelatedFilterToTable('app.a');

        assert.equal(rows[0].classList.contains('hidden'), false);
        assert.equal(rows[1].classList.contains('hidden'), false);
        assert.equal(rows[2].classList.contains('hidden'), true);
    });
});

test.describe('package.js UI表示', () => {
    test('関連フィルタの対象表示を更新する', () => {
        const doc = setupDocument();
        const target = new Element('span');
        doc.elementsById.set('related-filter-target', target);

        pkg.setRelatedFilterFqn(null);
        pkg.renderRelatedFilterTarget();
        assert.equal(target.textContent, '未選択');

        pkg.setRelatedFilterFqn('app.domain');
        pkg.renderRelatedFilterTarget();
        assert.equal(target.textContent, 'app.domain');
    });

    test('集約深さの選択肢を更新する', () => {
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
        });
        pkg.setAggregationDepth(1);
        pkg.setPackageFilterFqn(null);
        pkg.setRelatedFilterFqn(null);

        pkg.updateAggregationDepthOptions(2);

        assert.equal(select.children.length >= 2, true);
        assert.equal(select.children[0].textContent.includes('集約なし'), true);
        assert.equal(select.value, '1');
    });
});
