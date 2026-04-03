const test = require('node:test');
const assert = require('node:assert/strict');
const { setGlossaryData } = require('./dom-stub.js');

// Pure functions - no DOM setup needed
const jigCommon = require('../../main/resources/templates/assets/jig-glossary.js');
// Mermaid utilities (moved from jig-glossary.js to jig-mermaid.js)
const jigMermaid = require('../../main/resources/templates/assets/jig-mermaid.js');

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
        assert.match(id, /^port_MyPort_exa_[a-z0-9]+$/);
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
        setGlossaryData( {
            "com.example.MyClass": {title: "マイクラス", description: "説明文"}
        });
        const term = jigCommon.getTypeTerm("com.example.MyClass");
        assert.equal(term.title, "マイクラス");
        assert.equal(term.description, "説明文");
        delete globalThis.glossaryData;
    });

    test("glossaryに登録されていない場合、単純名をtitleとして返す", () => {
        setGlossaryData( {});
        const term = jigCommon.getTypeTerm("java.lang.String");
        assert.equal(term.title, "String");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });

    test("単純名がない場合、fqn全体をtitleとして返す", () => {
        setGlossaryData( {});
        const term = jigCommon.getTypeTerm("(default)");
        assert.equal(term.title, "(default)");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });
});

// ----- getMethodTerm -----

test.describe("getMethodTerm", () => {
    test("glossaryに登録されている場合はterm全体を返す", () => {
        setGlossaryData( {
            "com.example.Foo#bar(java.lang.String)": {title: "文字列で保存", description: "説明"}
        });
        const term = jigCommon.getMethodTerm("com.example.Foo#bar(java.lang.String)");
        assert.equal(term.title, "文字列で保存");
        assert.equal(term.description, "説明");
        delete globalThis.glossaryData;
    });

    test("引数を単純名に変換して再検索する", () => {
        setGlossaryData( {
            "com.example.Foo#bar(String)": {title: "文字列版", description: ""}
        });
        const term = jigCommon.getMethodTerm("com.example.Foo#bar(java.lang.String)");
        assert.equal(term.title, "文字列版");
        delete globalThis.glossaryData;
    });

    test("登録なしの場合、メソッド名と引数単純名を返す", () => {
        setGlossaryData( {});
        const term = jigCommon.getMethodTerm("hoge.fuga.Class#save(java.lang.String)");
        assert.equal(term.title, "save(String)");
        assert.equal(term.description, "");
        delete globalThis.glossaryData;
    });

    test("引数なしメソッドの場合", () => {
        setGlossaryData( {});
        const term = jigCommon.getMethodTerm("hoge.fuga.Class#list()");
        assert.equal(term.title, "list()");
        delete globalThis.glossaryData;
    });

    test("複数引数の場合、カンマ区切りで表示", () => {
        setGlossaryData( {});
        const term = jigCommon.getMethodTerm("hoge.fuga.Class#save(com.example.User,java.lang.Long)");
        assert.equal(term.title, "save(User,Long)");
        delete globalThis.glossaryData;
    });

    test("空のfqnの場合", () => {
        setGlossaryData( {});
        assert.throws(
            () => {
                jigCommon.getMethodTerm("");
            },
            Error
        );
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
        const sccs = jigMermaid.detectStronglyConnectedComponents(graph);
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
        const result = jigMermaid.transitiveReduction(relations);
        assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'b>c']);
    });

    test('循環参照は対象外とする', () => {
        const relations = [
            {from: 'a', to: 'b'},
            {from: 'b', to: 'a'},
            {from: 'a', to: 'c'},
        ];
        const result = jigMermaid.transitiveReduction(relations);
        assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'a>c', 'b>a']);
    });

    test('循環ではないが簡約対象でもない', () => {
        const relations = [
            {from: 'a', to: 'b'},
            {from: 'c', to: 'd'},
        ];
        const result = jigMermaid.transitiveReduction(relations);
        assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'c>d']);
    });

    test('循環からの関係は簡約対象にしない', () => {
        const relations = [
            {from: 'a', to: 'b'},
            {from: 'b', to: 'a'}, // cycle
            {from: 'b', to: 'c'},
            {from: 'a', to: 'c'},
        ];
        const result = jigMermaid.transitiveReduction(relations);
        assert.deepEqual(result.map(r => `${r.from}>${r.to}`).sort(), ['a>b', 'a>c', 'b>a', 'b>c']);
    });
});

// ----- computeSubgraphDepthMap / computeOutboundEdgeLengths -----

test.describe('computeSubgraphDepthMap', () => {
    test('DAGの最長パスで深さを計算する', () => {
        const { depthMap, maxDepth } = jigMermaid.computeSubgraphDepthMap({
            nodesInSubgraph: ['A', 'B', 'C', 'D'],
            edges: [
                {from: 'A', to: 'B'},
                {from: 'B', to: 'C'},
                {from: 'A', to: 'D'},
            ],
        });
        assert.equal(depthMap.get('A'), 1);
        assert.equal(depthMap.get('B'), 2);
        assert.equal(depthMap.get('C'), 3);
        assert.equal(depthMap.get('D'), 2);
        assert.equal(maxDepth, 3);
    });

    test('循環のみの場合は全ノードをdepth=1で開始する', () => {
        const { depthMap, maxDepth } = jigMermaid.computeSubgraphDepthMap({
            nodesInSubgraph: ['A', 'B'],
            edges: [
                {from: 'A', to: 'B'},
                {from: 'B', to: 'A'},
            ],
        });
        assert.ok(depthMap.get('A') >= 1);
        assert.ok(depthMap.get('B') >= 1);
        assert.ok(maxDepth >= 1);
    });
});

test.describe('computeOutboundEdgeLengths', () => {
    test('浅いノードから外部へのエッジが長くなる', () => {
        const { edgeLengthByKey } = jigMermaid.computeOutboundEdgeLengths({
            nodesInSubgraph: ['A', 'B', 'C'],
            edges: [
                {from: 'A', to: 'B'},
                {from: 'B', to: 'C'},
                {from: 'A', to: 'X'},
                {from: 'C', to: 'Y'},
            ],
        });
        assert.equal(edgeLengthByKey.get('A::X'), 3);
        assert.equal(edgeLengthByKey.get('C::Y'), 1);
        assert.equal(edgeLengthByKey.get('A::B'), 1);
    });
});

// ----- MermaidBuilder.applyThemeClassDefs -----

test.describe('MermaidBuilder.applyThemeClassDefs', () => {
    test('全テーマのclassDefを追加する', () => {
        const builder = new jigMermaid.MermaidBuilder();
        builder.applyThemeClassDefs();
        const code = builder.build();
        assert.ok(code.includes('classDef inbound'), 'inbound classDef should be present');
        assert.ok(code.includes('classDef usecase'), 'usecase classDef should be present');
        assert.ok(code.includes('classDef outbound'), 'outbound classDef should be present');
        assert.ok(code.includes('classDef inactive'), 'inactive classDef should be present');
    });

    test('nodeStyleDefsから色コードが正しく取得される', () => {
        assert.equal(jigMermaid.nodeStyleDefs.inbound, 'fill:#E8F0FE,stroke:#2E5C8A');
        assert.equal(jigMermaid.nodeStyleDefs.usecase, 'fill:#E6F8F0,stroke:#2D7A4A');
        assert.equal(jigMermaid.nodeStyleDefs.outbound, 'fill:#FFF0E6,stroke:#CC6600');
        assert.equal(jigMermaid.nodeStyleDefs.inactive, 'fill:#e0e0e0,stroke:#aaa');
    });
});

// ----- getNodeDefinition & MermaidBuilder node shapes -----

test.describe('Mermaid node shapes', () => {
    test('getNodeDefinition: default shape is class (square)', () => {
        const def = jigMermaid.getNodeDefinition('id1', 'label1');
        assert.equal(def, 'id1["label1"]');
    });

    test('getNodeDefinition: method shape is rounded', () => {
        const def = jigMermaid.getNodeDefinition('id1', 'label1', 'method');
        assert.equal(def, 'id1(["label1"])');
    });

    test('getNodeDefinition: package shape is stacked', () => {
        const def = jigMermaid.getNodeDefinition('id1', 'label1', 'package');
        assert.equal(def, 'id1@{shape: st-rect, label: "label1"}');
    });

    test('getNodeDefinition: database shape is cylindrical', () => {
        const def = jigMermaid.getNodeDefinition('id1', 'label1', 'database');
        assert.equal(def, 'id1[("label1")]');
    });

    test('getNodeDefinition: external shape is double circle', () => {
        const def = jigMermaid.getNodeDefinition('id1', 'label1', 'external');
        assert.equal(def, 'id1(("label1"))');
    });

    test('getNodeDefinition: fallback to raw shape string', () => {
        const def = jigMermaid.getNodeDefinition('id1', 'label1', '>"$LABEL"]');
        assert.equal(def, 'id1>"label1"]');
    });

    test('MermaidBuilder.addNode uses default shape', () => {
        const builder = new jigMermaid.MermaidBuilder();
        builder.addNode('id1', 'label1');
        const code = builder.build();
        assert.ok(code.includes('id1["label1"]'));
    });

    test('MermaidBuilder.addNodeToSubgraph uses specified shape', () => {
        const builder = new jigMermaid.MermaidBuilder();
        const sg = builder.startSubgraph('sg1');
        builder.addNodeToSubgraph(sg, 'id1', 'label1', 'method');
        const code = builder.build();
        assert.ok(code.includes('id1(["label1"])'));
    });
});
