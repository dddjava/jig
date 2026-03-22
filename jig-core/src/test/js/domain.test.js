const test = require('node:test');
const assert = require('node:assert/strict');
const { Element, DocumentStub } = require('./dom-stub.js');

// jig.js の共通ユーティリティをロード（window・document のスタブが必要）
global.window = global.window || { addEventListener: () => {} };
global.document = new DocumentStub();
require('../../main/resources/templates/assets/jig.js');

const { getGlossaryMethodTerm, createTypeLink, createTypeRefLink, renderPackageNavItem, getDirectChildPackages } = require('../../main/resources/templates/assets/domain.js');

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

    test.describe('createTypeLink', () => {
        test('domain型が存在する場合、リンク（<a>タグ）を返す', () => {
            const domainType = {fqn: 'org.example.Account', methods: []};
            globalThis.domainData = {types: [domainType]};
            globalThis.glossaryData = {
                'org.example.Account': {title: '口座', description: ''}
            };

            const result = createTypeLink('org.example.Account', 'test-class');

            assert.equal(result.tagName, 'a');
            assert.equal(result.className, 'test-class');
            assert.equal(result.textContent, '口座');
            assert.equal(result.attributes.get('href'), '#org.example.Account');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('domain型が存在しない場合、spanを返す（weak クラス付き）', () => {
            globalThis.domainData = {types: []};
            globalThis.glossaryData = {};

            const result = createTypeLink('java.lang.String', 'test-class');

            assert.equal(result.tagName, 'span');
            assert.equal(result.textContent, 'String');
            assert.ok(result.className.includes('weak'));
            assert.ok(result.className.includes('test-class'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('classNameが指定されない場合、weakクラスのみ', () => {
            globalThis.domainData = {types: []};
            globalThis.glossaryData = {};

            const result = createTypeLink('java.lang.String');

            assert.equal(result.className, 'weak');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });
    });

    test.describe('createTypeRefLink', () => {
        test('型引数なしの場合、createTypeLinkの結果を返す', () => {
            const domainType = {fqn: 'org.example.User', methods: []};
            globalThis.domainData = {types: [domainType]};
            globalThis.glossaryData = {
                'org.example.User': {title: 'ユーザー', description: ''}
            };

            const typeRef = {fqn: 'org.example.User'};
            const result = createTypeRefLink(typeRef, 'arg-class');

            assert.equal(result.tagName, 'a');
            assert.equal(result.className, 'arg-class');
            assert.equal(result.textContent, 'ユーザー');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('型引数がある場合、spanで型と型引数を組み立てる', () => {
            const domainType = {fqn: 'java.util.List', methods: []};
            globalThis.domainData = {types: [domainType]};
            globalThis.glossaryData = {
                'java.util.List': {title: 'List', description: ''}
            };

            const typeRef = {
                fqn: 'java.util.List',
                typeArgumentRefs: [
                    {fqn: 'org.example.Item'}
                ]
            };
            const result = createTypeRefLink(typeRef, 'generic-type');

            assert.equal(result.tagName, 'span');
            assert.equal(result.className, 'generic-type');
            // 子要素に型と型引数が含まれる（最初の要素は createTypeLink の結果）
            assert.ok(result.children.length > 0);

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('ネストした型引数がある場合、再帰的に処理する', () => {
            const domainTypes = [
                {fqn: 'java.util.List', methods: []},
                {fqn: 'java.util.Map', methods: []}
            ];
            globalThis.domainData = {types: domainTypes};
            globalThis.glossaryData = {};

            const typeRef = {
                fqn: 'java.util.Map',
                typeArgumentRefs: [
                    {fqn: 'java.lang.String'},
                    {
                        fqn: 'java.util.List',
                        typeArgumentRefs: [{fqn: 'org.example.Item'}]
                    }
                ]
            };
            const result = createTypeRefLink(typeRef);

            assert.equal(result.tagName, 'span');
            // 子要素が複数（型と区切り文字と型引数）
            assert.ok(result.children.length > 2);

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });
    });

    test.describe('renderPackageNavItem', () => {
        test('子が1つだけでタイプを持たないパッケージの場合、統合して表示する', () => {
            const examplePkg = {
                fqn: 'com.example',
                types: []
            };
            const comPkg = {
                fqn: 'com',
                types: []
            };

            globalThis.domainData = {
                packages: [comPkg, examplePkg],
                types: []
            };
            globalThis.glossaryData = {
                'com': {title: 'com'},
                'com.example': {title: 'example'}
            };

            const result = renderPackageNavItem(comPkg);

            assert.equal(result.tagName, 'details');
            const summaryLink = result.children[0].children[0];
            assert.equal(summaryLink.tagName, 'a');
            assert.equal(summaryLink.textContent, 'com/example');
            assert.equal(summaryLink.attributes.get('href'), '#com.example');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('タイプを持つパッケージを表示する', () => {
            const deepPkg = {
                fqn: 'com.example.deep',
                types: [
                    {fqn: 'com.example.deep.MyClass'}
                ]
            };
            const examplePkg = {
                fqn: 'com.example',
                types: []
            };
            const comPkg = {
                fqn: 'com',
                types: []
            };

            globalThis.domainData = {
                packages: [comPkg, examplePkg, deepPkg],
                types: [{fqn: 'com.example.deep.MyClass', methods: []}]
            };
            globalThis.glossaryData = {
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.deep': {title: 'deep'},
                'com.example.deep.MyClass': {title: 'MyClass'}
            };

            const result = renderPackageNavItem(comPkg);

            assert.equal(result.tagName, 'details');
            const summaryLink = result.children[0].children[0];
            assert.equal(summaryLink.tagName, 'a');
            // com -> example -> deep で、deep がタイプを持つので統合が止まる
            assert.equal(summaryLink.textContent, 'com/example/deep');
            assert.equal(summaryLink.attributes.get('href'), '#com.example.deep');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('複数段階のパッケージが1つずつ続く場合、全て統合して表示する', () => {
            const deepPkg = {
                fqn: 'com.example.sub.deep',
                types: [
                    {fqn: 'com.example.sub.deep.MyClass'}
                ]
            };
            const subPkg = {
                fqn: 'com.example.sub',
                types: []
            };
            const examplePkg = {
                fqn: 'com.example',
                types: []
            };
            const comPkg = {
                fqn: 'com',
                types: []
            };

            globalThis.domainData = {
                packages: [comPkg, examplePkg, subPkg, deepPkg],
                types: [{fqn: 'com.example.sub.deep.MyClass', methods: []}]
            };
            globalThis.glossaryData = {
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.sub': {title: 'sub'},
                'com.example.sub.deep': {title: 'deep'},
                'com.example.sub.deep.MyClass': {title: 'MyClass'}
            };

            const result = renderPackageNavItem(comPkg);

            const summaryLink = result.children[0].children[0];
            // com -> example -> sub -> deep と続くので、sub がタイプを持つまで統合
            assert.equal(summaryLink.textContent, 'com/example/sub/deep');
            assert.equal(summaryLink.attributes.get('href'), '#com.example.sub.deep');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('子パッケージを持つパッケージを表示する', () => {
            const sub2Pkg = {
                fqn: 'com.example.sub2',
                types: []
            };
            const sub1Pkg = {
                fqn: 'com.example.sub1',
                types: []
            };
            const examplePkg = {
                fqn: 'com.example',
                types: []
            };
            const comPkg = {
                fqn: 'com',
                types: []
            };

            globalThis.domainData = {
                packages: [comPkg, examplePkg, sub1Pkg, sub2Pkg],
                types: []
            };
            globalThis.glossaryData = {
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.sub1': {title: 'sub1'},
                'com.example.sub2': {title: 'sub2'}
            };

            const result = renderPackageNavItem(comPkg);

            const summaryLink = result.children[0].children[0];
            // com -> example は統合（example は1つだけの子を持つから）
            assert.equal(summaryLink.textContent, 'com/example');
            assert.equal(summaryLink.attributes.get('href'), '#com.example');

            // example の直下には sub1 と sub2 があるはず
            const childPackageNames = Array.from(result.children)
                .filter(child => child.tagName === 'details')
                .map(child => child.children[0].children[0].textContent);
            assert.ok(childPackageNames.includes('sub1'), 'example should have sub1 as child');
            assert.ok(childPackageNames.includes('sub2'), 'example should have sub2 as child');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

    });

    test.describe('getDirectChildPackages', () => {
        test('直下の子パッケージのみを返す', () => {
            const comPkg = {fqn: 'com'};
            const examplePkg = {fqn: 'com.example'};
            const subPkg = {fqn: 'com.example.sub'};
            const deepPkg = {fqn: 'com.example.sub.deep'};

            globalThis.domainData = {
                packages: [comPkg, examplePkg, subPkg, deepPkg],
                types: []
            };

            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 1);
            assert.equal(result[0].fqn, 'com.example');

            delete globalThis.domainData;
        });

        test('直下の子パッケージが複数の場合、全て返す', () => {
            const comPkg = {fqn: 'com'};
            const examplePkg = {fqn: 'com.example'};
            const utilPkg = {fqn: 'com.util'};

            globalThis.domainData = {
                packages: [comPkg, examplePkg, utilPkg],
                types: []
            };

            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 2);
            assert.ok(result.some(p => p.fqn === 'com.example'));
            assert.ok(result.some(p => p.fqn === 'com.util'));

            delete globalThis.domainData;
        });

        test('子パッケージがない場合は空配列を返す', () => {
            const comPkg = {fqn: 'com'};

            globalThis.domainData = {
                packages: [comPkg],
                types: []
            };

            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 0);

            delete globalThis.domainData;
        });
    });
});
