const test = require('node:test');
const assert = require('node:assert/strict');

const jigUtil = require('../../main/resources/templates/assets/jig-util.js');

test.describe('jig-util.js', () => {

    test.describe('collectTypeRefFqns', () => {
        const sut = jigUtil.collectTypeRefFqns;

        test('null を渡すと空配列を返す', () => {
            const result = sut(null);
            assert.deepEqual(result, []);
        });

        test('undefined を渡すと空配列を返す', () => {
            const result = sut(undefined);
            assert.deepEqual(result, []);
        });

        test('シンプルな型の FQN を返す', () => {
            const typeRef = {fqn: 'com.example.Order'};
            const result = sut(typeRef);
            assert.deepEqual(result, ['com.example.Order']);
        });

        test('コレクション型の場合、FQN と typeArgumentRefs の FQN を返す', () => {
            const typeRef = {
                fqn: 'java.util.List',
                typeArgumentRefs: [{fqn: 'com.example.Order'}]
            };
            const result = sut(typeRef);
            assert.deepEqual(result, ['java.util.List', 'com.example.Order']);
        });

        test('複数の typeArgumentRefs を処理する', () => {
            const typeRef = {
                fqn: 'java.util.Map',
                typeArgumentRefs: [
                    {fqn: 'java.lang.String'},
                    {fqn: 'com.example.Order'}
                ]
            };
            const result = sut(typeRef);
            assert.deepEqual(result, ['java.util.Map', 'java.lang.String', 'com.example.Order']);
        });

        test('ネストされたコレクション型を再帰的に処理する', () => {
            const typeRef = {
                fqn: 'java.util.List',
                typeArgumentRefs: [{
                    fqn: 'java.util.List',
                    typeArgumentRefs: [{fqn: 'com.example.Order'}]
                }]
            };
            const result = sut(typeRef);
            assert.deepEqual(result, ['java.util.List', 'java.util.List', 'com.example.Order']);
        });

        test('Map<String, List<Order>> のような複雑なネスト構造を処理する', () => {
            const typeRef = {
                fqn: 'java.util.Map',
                typeArgumentRefs: [
                    {fqn: 'java.lang.String'},
                    {
                        fqn: 'java.util.List',
                        typeArgumentRefs: [{fqn: 'com.example.Order'}]
                    }
                ]
            };
            const result = sut(typeRef);
            assert.deepEqual(result, ['java.util.Map', 'java.lang.String', 'java.util.List', 'com.example.Order']);
        });

        test('typeArgumentRefs が空配列の場合、元の FQN のみを返す', () => {
            const typeRef = {
                fqn: 'java.util.List',
                typeArgumentRefs: []
            };
            const result = sut(typeRef);
            assert.deepEqual(result, ['java.util.List']);
        });
    });

    test.describe('pushToMap', () => {
        const sut = jigUtil.pushToMap;

        test('新しいキーに値を追加すると配列が作られる', () => {
            const map = new Map();
            sut(map, 'key', 'value');
            assert.deepEqual(map.get('key'), ['value']);
        });

        test('既存のキーに値を追加すると配列に追記される', () => {
            const map = new Map();
            sut(map, 'key', 'first');
            sut(map, 'key', 'second');
            assert.deepEqual(map.get('key'), ['first', 'second']);
        });

        test('異なるキーは独立した配列を持つ', () => {
            const map = new Map();
            sut(map, 'a', 1);
            sut(map, 'b', 2);
            assert.deepEqual(map.get('a'), [1]);
            assert.deepEqual(map.get('b'), [2]);
        });
    });

    test.describe('addToSetMap', () => {
        const sut = jigUtil.addToSetMap;

        test('新しいキーに値を追加するとSetが作られる', () => {
            const map = new Map();
            sut(map, 'key', 'value');
            assert.deepStrictEqual(map.get('key'), new Set(['value']));
        });

        test('既存のキーに値を追加するとSetに追記される', () => {
            const map = new Map();
            sut(map, 'key', 'first');
            sut(map, 'key', 'second');
            assert.deepStrictEqual(map.get('key'), new Set(['first', 'second']));
        });

        test('同じ値を複数回追加しても重複しない', () => {
            const map = new Map();
            sut(map, 'key', 'value');
            sut(map, 'key', 'value');
            assert.deepStrictEqual(map.get('key'), new Set(['value']));
        });
    });

    test.describe('fqnToId', () => {

        test("プレフィックスを付けてIDを生成する", () => {
            const id = jigUtil.fqnToId("port", "com.example.MyPort");
            assert.match(id, /^port_MyPort_exa_[a-z0-9]+$/);
        });

        test("異なるfqnなら異なるIDを生成する", () => {
            const id1 = jigUtil.fqnToId("persistence", "my_table");
            const id2 = jigUtil.fqnToId("persistence", "another_table");
            assert.notEqual(id1, id2);
        });

        test("同じfqnなら同じIDを生成する（一意性）", () => {
            const id1 = jigUtil.fqnToId("op", "com.example.Port#save(java.lang.String)");
            const id2 = jigUtil.fqnToId("op", "com.example.Port#save(java.lang.String)");
            assert.equal(id1, id2);
        });

        test("マルチバイト文字でも正しくハッシュ化される", () => {
            const id1 = jigUtil.fqnToId("persistence", "テーブル1");
            const id2 = jigUtil.fqnToId("persistence", "テーブル2");
            assert.notEqual(id1, id2);
            assert.match(id1, /^persistence_[\w-]+_[a-z0-9]+$/);
        });
    });

    test('getPackageDepth: 深さを返す', () => {
        assert.equal(jigUtil.getPackageDepth(''), 0);
        assert.equal(jigUtil.getPackageDepth('(default)'), 0);
        assert.equal(jigUtil.getPackageDepth('app.domain'), 2);
    });

    test('getCommonPrefixDepth: 共通プレフィックス深さを返す', () => {
        assert.equal(jigUtil.getCommonPrefixDepth([]), 0);
        assert.equal(jigUtil.getCommonPrefixDepth(['app.domain.a', 'app.domain.b']), 2);
        assert.equal(jigUtil.getCommonPrefixDepth(['app', 'lib.tool']), 0);
    });

    test('getPackageFqnFromTypeFqn: 型FQNからパッケージFQNを返す', () => {
        assert.equal(jigUtil.getPackageFqnFromTypeFqn('com.example.domain.User'), 'com.example.domain');
        assert.equal(jigUtil.getPackageFqnFromTypeFqn('TopLevelClass'), '(default)');
        assert.equal(jigUtil.getPackageFqnFromTypeFqn(null), '(default)');
    });

    test('isWithinPackageFilters: パッケージフィルタのマッチ判定', () => {
        assert.equal(jigUtil.isWithinPackageFilters('com.example', ['com.example']), true);
        assert.equal(jigUtil.isWithinPackageFilters('com.example.domain', ['com.example']), true);
        assert.equal(jigUtil.isWithinPackageFilters('com.other', ['com.example']), false);
        assert.equal(jigUtil.isWithinPackageFilters('com.example', []), true);
    });

    test('getAggregatedFqn: 指定深さで集約する', () => {
        assert.equal(jigUtil.getAggregatedFqn('com.example.domain', 2), 'com.example');
        assert.equal(jigUtil.getAggregatedFqn('com.example.domain', 0), 'com.example.domain');
        assert.equal(jigUtil.getAggregatedFqn('(default)', 2), '(default)');
    });
});
