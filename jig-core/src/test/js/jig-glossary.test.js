const test = require('node:test');
const assert = require('node:assert/strict');
const {setGlossaryData} = require('./dom-stub.js');

require('../../main/resources/templates/assets/jig-util.js');
require('../../main/resources/templates/assets/jig-data.js');
const sut = require('../../main/resources/templates/assets/jig-glossary.js');

// ----- getTypeTerm -----

test.describe('jig-glossary.js', () => {
    test.describe("getTypeTerm", () => {
        test.afterEach(() => {
            delete globalThis.glossaryData;
        });

        test("glossaryに登録されている場合はterm全体を返す", () => {
            setGlossaryData({
                "com.example.MyClass": {title: "マイクラス", description: "説明文"}
            });
            const term = sut.getTypeTerm("com.example.MyClass");
            assert.equal(term.title, "マイクラス");
            assert.equal(term.description, "説明文");
        });

        test("glossaryに登録されていない場合、単純名をtitleとして返す", () => {
            setGlossaryData({});
            const term = sut.getTypeTerm("java.lang.String");
            assert.equal(term.title, "String");
            assert.equal(term.description, "");
        });

        test("単純名がない場合、fqn全体をtitleとして返す", () => {
            setGlossaryData({});
            const term = sut.getTypeTerm("(default)");
            assert.equal(term.title, "(default)");
            assert.equal(term.description, "");
        });
    });

// ----- getMethodTerm -----

    test.describe("getMethodTerm", () => {
        test.afterEach(() => {
            delete globalThis.glossaryData;
        });

        test("glossaryに登録されている場合はterm全体を返す", () => {
            setGlossaryData({
                "com.example.Foo#bar(java.lang.String)": {title: "文字列で保存", description: "説明"}
            });
            const term = sut.getMethodTerm("com.example.Foo#bar(java.lang.String)");
            assert.equal(term.title, "文字列で保存");
            assert.equal(term.description, "説明");
        });

        test("引数を単純名に変換して再検索する", () => {
            setGlossaryData({
                "com.example.Foo#bar(String)": {title: "文字列版", description: ""}
            });
            const term = sut.getMethodTerm("com.example.Foo#bar(java.lang.String)");
            assert.equal(term.title, "文字列版");
        });

        test("登録なしの場合、メソッド名と引数単純名を返す", () => {
            setGlossaryData({});
            const term = sut.getMethodTerm("hoge.fuga.Class#save(java.lang.String)");
            assert.equal(term.title, "save(String)");
            assert.equal(term.description, "");
        });

        test("引数なしメソッドの場合", () => {
            setGlossaryData({});
            const term = sut.getMethodTerm("hoge.fuga.Class#list()");
            assert.equal(term.title, "list()");
        });

        test("複数引数の場合、カンマ区切りで表示", () => {
            setGlossaryData({});
            const term = sut.getMethodTerm("hoge.fuga.Class#save(com.example.User,java.lang.Long)");
            assert.equal(term.title, "save(User,Long)");
        });

        test("空のfqnの場合", () => {
            setGlossaryData({});
            assert.throws(
                () => {
                    sut.getMethodTerm("");
                },
                Error
            );
        });

        test("fallbackNameOnly=trueの場合、メソッド名のみ返す", () => {
            setGlossaryData({});
            const term = sut.getMethodTerm("hoge.fuga.Class#save(java.lang.String)", true);
            assert.deepEqual(term, {
                title: "save",
                simpleText: "save",
                kind: "メソッド",
                description: "",
                shortDeclaration: "Class#save(String)"
            });
        });
    });

// ----- methodSimpleName -----

    test.describe("methodSimpleName", () => {
        test("空文字の場合は空文字を返す", () => {
            assert.equal(sut.methodSimpleName(""), "");
        });

        test("nullishの場合は空文字を返す", () => {
            assert.equal(sut.methodSimpleName(null), "");
        });

        test("#がない場合はtypeSimpleNameと同じ結果を返す", () => {
            assert.equal(sut.methodSimpleName("com.example.MyClass"), "MyClass");
        });

        test("#はあるが(がない場合はメソッド名を返す", () => {
            assert.equal(sut.methodSimpleName("com.example.MyClass#myMethod"), "myMethod");
        });

        test("#と(がある場合はメソッド名のみ返す", () => {
            assert.equal(sut.methodSimpleName("com.example.MyClass#myMethod(java.lang.String)"), "myMethod");
        });
    });

// ----- getPackageTerm -----

    test.describe("getPackageTerm", () => {
        test.afterEach(() => {
            delete globalThis.glossaryData;
        });

        test("glossaryに登録されている場合はterm全体を返す", () => {
            setGlossaryData({
                "com.example.domain": {title: "ドメイン", description: "ドメイン層"}
            });
            const term = sut.getPackageTerm("com.example.domain");
            assert.equal(term.title, "ドメイン");
            assert.equal(term.description, "ドメイン層");
        });

        test("登録なしの場合、パッケージの単純名をtitleとして返す", () => {
            setGlossaryData({});
            const term = sut.getPackageTerm("com.example.domain");
            assert.equal(term.title, "domain");
            assert.equal(term.description, "");
        });
    });

// ----- getFieldTerm -----

    test.describe("getFieldTerm", () => {
        test.afterEach(() => {
            delete globalThis.glossaryData;
        });

        test("glossaryに登録されている場合はterm全体を返す", () => {
            setGlossaryData({
                "com.example.MyClass#myField": {title: "マイフィールド", description: "フィールドの説明"}
            });
            const term = sut.getFieldTerm("com.example.MyClass#myField");
            assert.equal(term.title, "マイフィールド");
            assert.equal(term.description, "フィールドの説明");
        });

        test("登録なしの場合、#以降をtitleとして返す", () => {
            setGlossaryData({});
            const term = sut.getFieldTerm("com.example.MyClass#myField");
            assert.equal(term.title, "myField");
            assert.equal(term.description, "");
        });
    });

});
