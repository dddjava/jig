const test = require('node:test');
const assert = require('node:assert/strict');
const {setGlossaryData} = require('./dom-stub.js');

const sut = require('../../main/resources/templates/assets/jig-glossary.js');

test.describe("util", () => {
    const util = globalThis.Jig.util;

    test.describe("fqnToId", () => {

        test("プレフィックスを付けてIDを生成する", () => {
            const id = util.fqnToId("port", "com.example.MyPort");
            assert.match(id, /^port_MyPort_exa_[a-z0-9]+$/);
        });

        test("異なるfqnなら異なるIDを生成する", () => {
            const id1 = util.fqnToId("persistence", "my_table");
            const id2 = util.fqnToId("persistence", "another_table");
            assert.notEqual(id1, id2);
        });

        test("同じfqnなら同じIDを生成する（一意性）", () => {
            const id1 = util.fqnToId("op", "com.example.Port#save(java.lang.String)");
            const id2 = util.fqnToId("op", "com.example.Port#save(java.lang.String)");
            assert.equal(id1, id2);
        });

        test("マルチバイト文字でも正しくハッシュ化される", () => {
            const id1 = util.fqnToId("persistence", "テーブル1");
            const id2 = util.fqnToId("persistence", "テーブル2");
            assert.notEqual(id1, id2);
            assert.match(id1, /^persistence_[\w-]+_[a-z0-9]+$/);
        });
    });


    test('getPackageDepth: 深さを返す', () => {
        assert.equal(util.getPackageDepth(''), 0);
        assert.equal(util.getPackageDepth('(default)'), 0);
        assert.equal(util.getPackageDepth('app.domain'), 2);
    });

    test('getCommonPrefixDepth: 共通プレフィックス深さを返す', () => {
        assert.equal(util.getCommonPrefixDepth([]), 0);
        assert.equal(util.getCommonPrefixDepth(['app.domain.a', 'app.domain.b']), 2);
        assert.equal(util.getCommonPrefixDepth(['app', 'lib.tool']), 0);
    })

    test('getPackageFqnFromTypeFqn: 型FQNからパッケージFQNを返す', () => {
        assert.equal(util.getPackageFqnFromTypeFqn('com.example.domain.User'), 'com.example.domain');
        assert.equal(util.getPackageFqnFromTypeFqn('TopLevelClass'), '(default)');
        assert.equal(util.getPackageFqnFromTypeFqn(null), '(default)');
    });

    test('isWithinPackageFilters: パッケージフィルタのマッチ判定', () => {
        assert.equal(util.isWithinPackageFilters('com.example', ['com.example']), true);
        assert.equal(util.isWithinPackageFilters('com.example.domain', ['com.example']), true);
        assert.equal(util.isWithinPackageFilters('com.other', ['com.example']), false);
        assert.equal(util.isWithinPackageFilters('com.example', []), true);
    });

    test('getAggregatedFqn: 指定深さで集約する', () => {
        assert.equal(util.getAggregatedFqn('com.example.domain', 2), 'com.example');
        assert.equal(util.getAggregatedFqn('com.example.domain', 0), 'com.example.domain');
        assert.equal(util.getAggregatedFqn('(default)', 2), '(default)');
    });

});

// ----- getTypeTerm -----

test.describe("getTypeTerm", () => {
    test("glossaryに登録されている場合はterm全体を返す", () => {
        setGlossaryData({
            "com.example.MyClass": {title: "マイクラス", description: "説明文"}
        });
        const term = sut.getTypeTerm("com.example.MyClass");
        assert.equal(term.title, "マイクラス");
        assert.equal(term.description, "説明文");
        delete globalThis.glossaryData;
    });

    test("glossaryに登録されていない場合、単純名をtitleとして返す", () => {
        setGlossaryData({});
        const term = sut.getTypeTerm("java.lang.String");
        assert.equal(term.title, "String");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });

    test("単純名がない場合、fqn全体をtitleとして返す", () => {
        setGlossaryData({});
        const term = sut.getTypeTerm("(default)");
        assert.equal(term.title, "(default)");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });
});

// ----- getMethodTerm -----

test.describe("getMethodTerm", () => {
    test("glossaryに登録されている場合はterm全体を返す", () => {
        setGlossaryData({
            "com.example.Foo#bar(java.lang.String)": {title: "文字列で保存", description: "説明"}
        });
        const term = sut.getMethodTerm("com.example.Foo#bar(java.lang.String)");
        assert.equal(term.title, "文字列で保存");
        assert.equal(term.description, "説明");
        delete globalThis.glossaryData;
    });

    test("引数を単純名に変換して再検索する", () => {
        setGlossaryData({
            "com.example.Foo#bar(String)": {title: "文字列版", description: ""}
        });
        const term = sut.getMethodTerm("com.example.Foo#bar(java.lang.String)");
        assert.equal(term.title, "文字列版");
        delete globalThis.glossaryData;
    });

    test("登録なしの場合、メソッド名と引数単純名を返す", () => {
        setGlossaryData({});
        const term = sut.getMethodTerm("hoge.fuga.Class#save(java.lang.String)");
        assert.equal(term.title, "save(String)");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });

    test("引数なしメソッドの場合", () => {
        setGlossaryData({});
        const term = sut.getMethodTerm("hoge.fuga.Class#list()");
        assert.equal(term.title, "list()");
        delete globalThis.glossaryData;
    });

    test("複数引数の場合、カンマ区切りで表示", () => {
        setGlossaryData({});
        const term = sut.getMethodTerm("hoge.fuga.Class#save(com.example.User,java.lang.Long)");
        assert.equal(term.title, "save(User,Long)");
        delete globalThis.glossaryData;
    });

    test("空のfqnの場合", () => {
        setGlossaryData({});
        assert.throws(
            () => {
                sut.getMethodTerm("");
            },
            Error
        );
        delete globalThis.glossaryData;
    });
});
