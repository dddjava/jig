const test = require('node:test');
const assert = require('node:assert/strict');

// Pure functions - no DOM setup needed
const jigCommon = require('../../main/resources/templates/assets/jig-common.js');

// ----- estimateEdgeCount -----

test.describe('estimateEdgeCount', () => {
    test('Mermaid矢印を数える', () => {
        assert.equal(jigCommon.estimateEdgeCount('A --> B'), 1);
        assert.equal(jigCommon.estimateEdgeCount('A --> B\nB --> C'), 2);
        assert.equal(jigCommon.estimateEdgeCount('A <--> B'), 1);  // bidirectional
        assert.equal(jigCommon.estimateEdgeCount(''), 0);
        assert.equal(jigCommon.estimateEdgeCount(null), 0);
    });
});

// ----- fqnToId -----

test.describe("fqnToId", () => {
    test("プレフィックスを付けてIDを生成する", () => {
        const id = jigCommon.fqnToId("port", "com.example.MyPort");
        assert.match(id, /^port_com_exampl_[a-z0-9]+$/);
    });

    test("異なるfqnなら異なるIDを生成する", () => {
        const id1 = jigCommon.fqnToId("persistence", "my_table");
        const id2 = jigCommon.fqnToId("persistence", "another_table");
        assert.notEqual(id1, id2);
    });

    test("同じfqnなら同じIDを生成する（一意性）", () => {
        const id1 = jigCommon.fqnToId("op", "com.example.Port#save(java.lang.String)");
        const id2 = jigCommon.fqnToId("op", "com.example.Port#save(java.lang.String)");
        assert.equal(id1, id2);
    });

    test("マルチバイト文字でも正しくハッシュ化される", () => {
        const id1 = jigCommon.fqnToId("persistence", "テーブル1");
        const id2 = jigCommon.fqnToId("persistence", "テーブル2");
        assert.notEqual(id1, id2);
        assert.match(id1, /^persistence_[\w-]+_[a-z0-9]+$/);
    });
});

// ----- getTypeTerm -----

test.describe("getTypeTerm", () => {
    test("glossaryに登録されている場合はterm全体を返す", () => {
        globalThis.glossaryData = {
            "com.example.MyClass": {title: "マイクラス", description: "説明文"}
        };
        const term = jigCommon.getTypeTerm("com.example.MyClass");
        assert.equal(term.title, "マイクラス");
        assert.equal(term.description, "説明文");
        delete globalThis.glossaryData;
    });

    test("glossaryに登録されていない場合、単純名をtitleとして返す", () => {
        globalThis.glossaryData = {};
        const term = jigCommon.getTypeTerm("java.lang.String");
        assert.equal(term.title, "String");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });

    test("単純名がない場合、fqn全体をtitleとして返す", () => {
        globalThis.glossaryData = {};
        const term = jigCommon.getTypeTerm("(default)");
        assert.equal(term.title, "(default)");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });
});

// ----- getMethodTerm -----

test.describe("getMethodTerm", () => {
    test("glossaryに登録されている場合はterm全体を返す", () => {
        globalThis.glossaryData = {
            "com.example.Foo#bar(java.lang.String)": {title: "文字列で保存", description: "説明"}
        };
        const term = jigCommon.getMethodTerm("com.example.Foo#bar(java.lang.String)");
        assert.equal(term.title, "文字列で保存");
        assert.equal(term.description, "説明");
        delete globalThis.glossaryData;
    });

    test("引数を単純名に変換して再検索する", () => {
        globalThis.glossaryData = {
            "com.example.Foo#bar(String)": {title: "文字列版", description: ""}
        };
        const term = jigCommon.getMethodTerm("com.example.Foo#bar(java.lang.String)");
        assert.equal(term.title, "文字列版");
        delete globalThis.glossaryData;
    });

    test("登録なしの場合、メソッド名と引数単純名を返す", () => {
        globalThis.glossaryData = {};
        const term = jigCommon.getMethodTerm("hoge.fuga.Class#save(java.lang.String)");
        assert.equal(term.title, "save(String)");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });

    test("引数なしメソッドの場合", () => {
        globalThis.glossaryData = {};
        const term = jigCommon.getMethodTerm("hoge.fuga.Class#list()");
        assert.equal(term.title, "list()");
        delete globalThis.glossaryData;
    });

    test("複数引数の場合、カンマ区切りで表示", () => {
        globalThis.glossaryData = {};
        const term = jigCommon.getMethodTerm("hoge.fuga.Class#save(com.example.User, java.lang.Long)");
        assert.equal(term.title, "save(User, Long)");
        delete globalThis.glossaryData;
    });

    test("空のfqnの場合", () => {
        globalThis.glossaryData = {};
        const term = jigCommon.getMethodTerm("");
        assert.equal(term.title, "");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });
});

// ----- detectStronglyConnectedComponents -----

test.describe('detectStronglyConnectedComponents', () => {
    test('循環を検出する', () => {
        const graph = new Map([
            ['a', ['b']],
            ['b', ['c']],
            ['c', ['a', 'd']],
            ['d', ['e']],
            ['e', ['f']],
            ['f', ['d']],
        ]);
        const sccs = jigCommon.detectStronglyConnectedComponents(graph);
        const sortedSccs = sccs.map(scc => scc.sort()).sort((a, b) => a[0].localeCompare(b[0]));
        assert.deepEqual(sortedSccs, [['a', 'b', 'c'], ['d', 'e', 'f']]);
    });
});

// ----- transitiveReduction -----

test.describe('transitiveReduction', () => {
    test('単純な推移関係を簡約する', () => {
        const relations = [
            {from: 'a', to: 'b'},
            {from: 'b', to: 'c'},
            {from: 'a', to: 'c'},
        ];
        const result = jigCommon.transitiveReduction(relations);
        assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'b>c']);
    });

    test('循環参照は対象外とする', () => {
        const relations = [
            {from: 'a', to: 'b'},
            {from: 'b', to: 'a'},
            {from: 'a', to: 'c'},
        ];
        const result = jigCommon.transitiveReduction(relations);
        assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'a>c', 'b>a']);
    });

    test('循環ではないが簡約対象でもない', () => {
        const relations = [
            {from: 'a', to: 'b'},
            {from: 'c', to: 'd'},
        ];
        const result = jigCommon.transitiveReduction(relations);
        assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'c>d']);
    });

    test('循環からの関係は簡約対象にしない', () => {
        const relations = [
            {from: 'a', to: 'b'},
            {from: 'b', to: 'a'}, // cycle
            {from: 'b', to: 'c'},
            {from: 'a', to: 'c'},
        ];
        const result = jigCommon.transitiveReduction(relations);
        assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'a>c', 'b>a', 'b>c']);
    });
});
