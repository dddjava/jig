const test = require('node:test');
const assert = require('node:assert/strict');

// jig.js の共通ユーティリティをロード（window・document のスタブが必要）
global.window = global.window || { addEventListener: () => {} };
global.document = global.document || {
    addEventListener: () => {},
    getElementsByClassName: () => [],
    createElement: (tag) => ({ tagName: tag, children: [], className: '', textContent: '', style: {}, appendChild(c) { this.children.push(c); } }),
    body: { classList: { contains: () => false } },
};
require('../../main/resources/templates/assets/jig.js');

const { getGlossaryMethodTerm } = require('../../main/resources/templates/assets/domain.js');

test.describe('domain.js', () => {
    test.describe('getGlossaryMethodTerm', () => {
        test('fqn が glossaryData に完全一致する場合、その用語を返す', () => {
            const methodFqn = 'org.example.Account#create(java.lang.String)';
            const expectedTerm = {
                title: '口座作成',
                description: '口座エンティティを新規作成する'
            };

            globalThis.glossaryData = {
                [methodFqn]: expectedTerm
            };

            const method = {
                fqn: methodFqn,
                parameterTypeRefs: ['java.lang.String']
            };

            const result = getGlossaryMethodTerm(method);
            assert.deepEqual(result, expectedTerm);

            delete globalThis.glossaryData;
        });

        test('fqn が一致しないが、引数変換キーで一致する場合、その用語を返す', () => {
            const methodFqn = 'org.example.Account#create(java.lang.String,int)';
            // 引数変換キーの構築:
            const transformedKey = 'org.example.Account#create(String,int)';
            const expectedTerm = {
                title: 'create',
                description: 'Creates an account'
            };

            globalThis.glossaryData = {
                [transformedKey]: expectedTerm
            };

            const method = {
                fqn: methodFqn,
                parameterTypeRefs: [{fqn: 'java.lang.String'}, {fqn: 'int'}]
            };

            const result = getGlossaryMethodTerm(method);
            assert.deepEqual(result, expectedTerm);

            delete globalThis.glossaryData;
        });

        test('glossaryData に該当がない場合、メソッド名をtitle、空のdescriptionを返す', () => {
            globalThis.glossaryData = {};

            const method = {
                fqn: 'org.example.Account#create(java.lang.String)',
                parameterTypeRefs: [{fqn: 'java.lang.String'}]
            };

            const result = getGlossaryMethodTerm(method);
            assert.deepEqual(result, {
                title: 'create',
                description: ''
            });

            delete globalThis.glossaryData;
        });

        test('引数なしのメソッド（fqn完全一致）の場合、用語を返す', () => {
            const methodFqn = 'org.example.User#getName()';
            const expectedTerm = {
                title: '名前取得',
                description: 'ユーザーの名前を取得'
            };

            globalThis.glossaryData = {
                [methodFqn]: expectedTerm
            };

            const method = {
                fqn: methodFqn,
                parameterTypeRefs: []
            };

            const result = getGlossaryMethodTerm(method);
            assert.deepEqual(result, expectedTerm);

            delete globalThis.glossaryData;
        });

        test('引数なしのメソッド（glossaryData になし）の場合、メソッド名を返す', () => {
            globalThis.glossaryData = {};

            const method = {
                fqn: 'org.example.User#getName()',
                parameterTypeRefs: []
            };

            const result = getGlossaryMethodTerm(method);
            assert.deepEqual(result, {
                title: 'getName',
                description: ''
            });

            delete globalThis.glossaryData;
        });

        test('複雑なパッケージ構造のメソッド名を正しく抽出する', () => {
            globalThis.glossaryData = {};

            const method = {
                fqn: 'org.example.domain.user.service.UserApplicationService#createUser(org.example.domain.user.value.UserId,org.example.domain.user.value.UserName)',
                parameterTypeRefs: [
                    {fqn: 'org.example.domain.user.value.UserId'},
                    {fqn: 'org.example.domain.user.value.UserName'}
                ]
            };

            const result = getGlossaryMethodTerm(method);
            assert.deepEqual(result, {
                title: 'createUser',
                description: ''
            });

            delete globalThis.glossaryData;
        });
    });
});
