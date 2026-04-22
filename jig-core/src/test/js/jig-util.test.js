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
});
