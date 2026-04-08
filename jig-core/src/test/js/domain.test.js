const test = require('node:test');
const assert = require('node:assert/strict');
const {DocumentStub, setGlossaryData} = require('./dom-stub.js');

// jig-glossary.js と jig-dom.js をロード（window・document のスタブが必要）
global.window = global.window || {
    addEventListener: () => {
    }
};
global.document = new DocumentStub();
require('../../main/resources/templates/assets/jig-util.js');
require('../../main/resources/templates/assets/jig-glossary.js');
require('../../main/resources/templates/assets/jig-mermaid.js');
require('../../main/resources/templates/assets/jig-dom.js');
const Jig = globalThis.Jig;

const DomainApp = require('../../main/resources/templates/assets/domain.js');
const {renderPackageNavItem, getDirectChildPackages, createRelationDiagram, createTypeRelationDiagram, createPackageRelationDiagram, createPackageDirectRelationDiagram, buildPackages} = DomainApp;

// ヘルパー関数：buildPackages で構築したパッケージから _childPackagesMap を設定
function setupDomainData(domainPackageRoots, types) {
    const packages = buildPackages(domainPackageRoots, types);
    globalThis.domainData = {domainPackageRoots, types};
    globalThis.domainData._typesMap = new Map(types.map(t => [t.fqn, t]));
    globalThis.domainData._packages = packages;
    const childrenMap = new Map(packages.map(p => [p.fqn, []]));
    packages.forEach(p => {
        const parent = p.fqn.substring(0, p.fqn.lastIndexOf('.'));
        if (childrenMap.has(parent)) childrenMap.get(parent).push(p);
    });
    globalThis.domainData._childPackagesMap = childrenMap;
}

test.describe('domain.js', () => {
    // ドメイン型のリンク解決をテスト（型から HTML リンクへの変換）
    test.describe('typeLinkResolver（DomainApp.init() で登録）', () => {
        test('domain型に対して、ページ内リンクのhrefを返す', () => {
            const domainType = {
                fqn: 'org.example.Account',
                isDeprecated: false,
                fields: [],
                methods: [],
                staticMethods: []
            };
            setupDomainData(['org.example'], [domainType]);
            setGlossaryData({'org.example.Account': {title: '口座', description: ''}});
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            const resolved = Jig.dom.type.getResolver()('org.example.Account');
            assert.equal(resolved.href, '#' + Jig.util.fqnToId("domain", 'org.example.Account'));
            assert.equal(resolved.className, undefined);

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            Jig.dom.type.clearResolver();
        });

        test('deprecatedなdomain型に対して、deprecatedクラスを返す', () => {
            const domainType = {
                fqn: 'org.example.OldClass',
                isDeprecated: true,
                fields: [],
                methods: [],
                staticMethods: []
            };
            setupDomainData(['org.example'], [domainType]);
            setGlossaryData({});
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            const resolved = Jig.dom.type.getResolver()('org.example.OldClass');
            assert.equal(resolved.href, '#' + Jig.util.fqnToId("domain", 'org.example.OldClass'));
            assert.equal(resolved.className, 'deprecated');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            Jig.dom.type.clearResolver();
        });

        test('domain型でない場合、weakクラスと単純名を返す（hrefなし）', () => {
            setupDomainData([], []);
            setGlossaryData({});
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            const resolved = Jig.dom.type.getResolver()('java.lang.String');
            assert.equal(resolved.href, undefined);
            assert.equal(resolved.className, 'weak');
            assert.equal(resolved.text, 'String');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            Jig.dom.type.clearResolver();
        });

        test('リゾルバー経由でdomain型はリンク付き要素になる', () => {
            const domainType = {
                fqn: 'org.example.User',
                isDeprecated: false,
                fields: [],
                methods: [],
                staticMethods: []
            };
            setupDomainData(['org.example'], [domainType]);
            setGlossaryData({'org.example.User': {title: 'ユーザー', description: ''}});
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            const result = Jig.dom.type.elementForRef({fqn: 'org.example.User'}, 'my-class');
            assert.equal(result.tagName, 'a');
            assert.equal(result.className, 'my-class');
            assert.equal(result.textContent, 'ユーザー');
            assert.equal(result.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'org.example.User'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            Jig.dom.type.clearResolver();
        });

        test('リゾルバー経由でdomain型でない場合はweak spanになる', () => {
            setupDomainData([], []);
            setGlossaryData({});
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            const result = Jig.dom.type.elementForRef({fqn: 'java.lang.String'}, 'my-class');
            assert.equal(result.tagName, 'span');
            assert.equal(result.textContent, 'String');
            assert.ok(result.className.includes('weak'));
            assert.ok(result.className.includes('my-class'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            Jig.dom.type.clearResolver();
        });

        test('型引数がある場合、spanで型と型引数を組み立てる', () => {
            const domainTypes = [
                {fqn: 'java.util.List', isDeprecated: false, fields: [], methods: [], staticMethods: []},
                {fqn: 'org.example.Item', isDeprecated: false, fields: [], methods: [], staticMethods: []}
            ];
            setupDomainData([], domainTypes);
            setGlossaryData({'java.util.List': {title: 'List', description: ''}});
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            const typeRef = {
                fqn: 'java.util.List',
                typeArgumentRefs: [{fqn: 'org.example.Item'}]
            };
            const result = Jig.dom.type.elementForRef(typeRef, 'generic-type');

            assert.equal(result.tagName, 'span');
            assert.equal(result.className, 'generic-type');
            assert.ok(result.children.length > 0);

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            Jig.dom.type.clearResolver();
        });

        test('配列型（Hoge[]）はベース型のリンクを解決して[]を付け直す', () => {
            const domainType = {
                fqn: 'org.example.Item',
                isDeprecated: false,
                fields: [],
                methods: [],
                staticMethods: []
            };
            setupDomainData(['org.example'], [domainType]);
            setGlossaryData({'org.example.Item': {title: 'アイテム', description: ''}});
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            const result = Jig.dom.type.elementForRef({fqn: 'org.example.Item[]'});
            assert.equal(result.tagName, 'a');
            assert.equal(result.textContent, 'アイテム[]');
            assert.equal(result.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'org.example.Item'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            Jig.dom.type.clearResolver();
        });

        test('多次元配列型（Hoge[][]）もベース型のリンクを解決して[][]を付け直す', () => {
            const domainType = {
                fqn: 'org.example.Item',
                isDeprecated: false,
                fields: [],
                methods: [],
                staticMethods: []
            };
            setupDomainData(['org.example'], [domainType]);
            setGlossaryData({'org.example.Item': {title: 'アイテム', description: ''}});
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            const result = Jig.dom.type.elementForRef({fqn: 'org.example.Item[][]'});
            assert.equal(result.tagName, 'a');
            assert.equal(result.textContent, 'アイテム[][]');
            assert.equal(result.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'org.example.Item'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            Jig.dom.type.clearResolver();
        });
    });

    // パッケージナビゲーション要素のレンダリングをテスト（パッケージツリーの表示形式）
    test.describe('renderPackageNavItem', () => {
        test('子が1つだけでタイプを持たないパッケージの場合、統合して表示する', () => {
            const types = [{
                fqn: 'com.example.MyClass'
            }];

            setupDomainData(['com'], types);
            setGlossaryData({
                'com': {title: 'com'},
                'com.example': {title: 'example'}
            });

            const packages = globalThis.domainData._packages;
            const comPkg = packages.find(p => p.fqn === 'com');
            const result = renderPackageNavItem(comPkg);

            assert.equal(result.tagName, 'details');
            const summaryLink = result.children[0].children[0];
            assert.equal(summaryLink.tagName, 'a');
            assert.equal(summaryLink.textContent, 'com/example');
            assert.equal(summaryLink.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'com.example'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('タイプを持つパッケージを表示する', () => {
            const types = [{fqn: 'com.example.deep.MyClass', methods: []}];

            setupDomainData(['com'], types);
            setGlossaryData({
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.deep': {title: 'deep'},
                'com.example.deep.MyClass': {title: 'MyClass'}
            });

            const packages = globalThis.domainData._packages;
            const comPkg = packages.find(p => p.fqn === 'com');
            const result = renderPackageNavItem(comPkg);

            assert.equal(result.tagName, 'details');
            const summaryLink = result.children[0].children[0];
            assert.equal(summaryLink.tagName, 'a');
            // com -> example -> deep で、deep がタイプを持つので統合が止まる
            assert.equal(summaryLink.textContent, 'com/example/deep');
            assert.equal(summaryLink.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'com.example.deep'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('複数段階のパッケージが1つずつ続く場合、全て統合して表示する', () => {
            const types = [{
                fqn: 'com.example.sub.deep.MyClass',
                methods: []
            }];

            setupDomainData(['com'], types);
            setGlossaryData({
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.sub': {title: 'sub'},
                'com.example.sub.deep': {title: 'deep'},
                'com.example.sub.deep.MyClass': {title: 'MyClass'}
            });

            const packages = globalThis.domainData._packages;
            const comPkg = packages.find(p => p.fqn === 'com');
            const result = renderPackageNavItem(comPkg);

            const summaryLink = result.children[0].children[0];
            // com -> example -> sub -> deep と続くので、sub がタイプを持つまで統合
            assert.equal(summaryLink.textContent, 'com/example/sub/deep');
            assert.equal(summaryLink.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'com.example.sub.deep'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('子パッケージを持つパッケージを表示する', () => {
            const types = [
                {fqn: 'com.example.sub1.MyClass1'},
                {fqn: 'com.example.sub2.MyClass2'}
            ];

            setupDomainData(['com'], types);
            setGlossaryData({
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.sub1': {title: 'sub1'},
                'com.example.sub2': {title: 'sub2'}
            });

            const packages = globalThis.domainData._packages;
            const comPkg = packages.find(p => p.fqn === 'com');
            const result = renderPackageNavItem(comPkg);

            const summaryLink = result.children[0].children[0];
            // com -> example は統合（example は1つだけの子を持つから）
            assert.equal(summaryLink.textContent, 'com/example');
            assert.equal(summaryLink.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'com.example'));

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

    // パッケージツリー操作をテスト（親パッケージから直下の子パッケージのフィルタリング）
    test.describe('getDirectChildPackages', () => {
        test('直下の子パッケージのみを返す', () => {
            const types = [{fqn: 'com.example.sub.deep.MyClass'}];
            setupDomainData(['com'], types);

            const packages = globalThis.domainData._packages;
            const comPkg = packages.find(p => p.fqn === 'com');
            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 1);
            assert.equal(result[0].fqn, 'com.example');

            delete globalThis.domainData;
        });

        test('直下の子パッケージが複数の場合、全て返す', () => {
            const types = [
                {fqn: 'com.example.MyClass'},
                {fqn: 'com.util.MyClass'}
            ];
            setupDomainData(['com'], types);

            const packages = globalThis.domainData._packages;
            const comPkg = packages.find(p => p.fqn === 'com');
            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 2);
            assert.ok(result.some(p => p.fqn === 'com.example'));
            assert.ok(result.some(p => p.fqn === 'com.util'));

            delete globalThis.domainData;
        });

        test('子パッケージがない場合は空配列を返す', () => {
            const types = [{fqn: 'com.MyClass'}];
            setupDomainData(['com'], types);

            const packages = globalThis.domainData._packages;
            const comPkg = packages.find(p => p.fqn === 'com');
            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 0);

            delete globalThis.domainData;
        });
    });

    // パッケージ関連図の Mermaid ソース生成をテスト
    test.describe('createRelationDiagram', () => {
        test('関係図のMermaidソースを生成する（fqnToMermaidIdが正常に動作すること）', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};

            setGlossaryData({
                'org.example.A': {title: 'A'},
                'org.example.B': {title: 'B'},
                'org.example': {title: 'example'}
            });

            const typesMap = new Map([
                ['org.example.A', typeA],
                ['org.example.B', typeB],
            ]);
            const typeRelations = [
                {from: 'org.example.A', to: 'org.example.B'}
            ];

            const pkg = {fqn: 'org.example', types: [{fqn: 'org.example.A'}, {fqn: 'org.example.B'}]};
            const result = createRelationDiagram(pkg, typeRelations, typesMap);

            assert.ok(result.includes('graph TB'), 'デフォルトの向きが含まれていること');
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idA} --> ${idB}`), '関連が含まれていること');

            const sgId = Jig.util.fqnToId("sg", 'org.example');
            assert.ok(result.includes(`subgraph ${sgId} ["example"]`), 'subgraphにパッケージ名のラベルが含まれていること');

            delete globalThis.glossaryData;
        });
    });

    test.describe('createTypeRelationDiagram', () => {
        test('クラスの関連図を生成する（出力・入力両方）', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};
            const typeC = {fqn: 'org.example.C', isDeprecated: false};

            setGlossaryData({
                'org.example.A': {title: 'A'},
                'org.example.B': {title: 'B'},
                'org.example.C': {title: 'C'},
            });

            const typesMap = new Map([
                ['org.example.A', typeA],
                ['org.example.B', typeB],
                ['org.example.C', typeC],
            ]);
            const typeRelations = [
                {from: 'org.example.A', to: 'org.example.B'},
                {from: 'org.example.C', to: 'org.example.A'},
            ];

            const result = createTypeRelationDiagram(typeA, typeRelations, typesMap);

            assert.ok(result, '図が生成されること');
            assert.ok(result.includes('graph TB'), '方向が含まれること');
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            const idC = Jig.util.fqnToId("n", 'org.example.C');
            assert.ok(result.includes(`${idA} --> ${idB}`), 'A→B の関連が含まれること');
            assert.ok(result.includes(`${idC} --> ${idA}`), 'C→A の関連が含まれること');
            const domainIdA = Jig.util.fqnToId("domain", 'org.example.A');
            assert.ok(result.includes(`click ${idA} "#${domainIdA}"`), 'Aへのクリックリンクが含まれること');
            assert.ok(result.includes(`style ${idA} font-weight:bold`), '自身（A）が強調表示されること');
            const sgId = Jig.util.fqnToId("sg", 'org.example');
            assert.ok(result.includes(`subgraph ${sgId}`), 'パッケージのサブグラフが含まれること');

            delete globalThis.glossaryData;
        });

        test('関連がない場合は null を返す', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};

            setGlossaryData({'org.example.A': {title: 'A'}});

            const typesMap = new Map([['org.example.A', typeA]]);
            const typeRelations = [];

            const result = createTypeRelationDiagram(typeA, typeRelations, typesMap);

            assert.equal(result, null);

            delete globalThis.glossaryData;
        });

        test('subgraph外向きエッジは深さに応じて長さが変わる', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};
            const typeX = {fqn: 'org.other.X', isDeprecated: false};

            setGlossaryData({
                'org.example.A': {title: 'A'},
                'org.example.B': {title: 'B'},
                'org.other.X': {title: 'X'},
            });

            const typesMap = new Map([
                ['org.example.A', typeA],
                ['org.example.B', typeB],
                ['org.other.X', typeX],
            ]);
            const typeRelations = [
                {from: 'org.example.A', to: 'org.example.B'},
                {from: 'org.example.A', to: 'org.other.X'},
            ];

            const result = createTypeRelationDiagram(typeA, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            const idX = Jig.util.fqnToId("n", 'org.other.X');
            assert.ok(result.includes(`${idA} ---> ${idX}`), '浅いノードから外部へのエッジは長くなること');
            assert.ok(result.includes(`${idA} --> ${idB}`), 'subgraph内エッジは通常長であること');

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
            delete globalThis.glossaryData;
        });
    });

    test.describe('createPackageRelationDiagram', () => {
        test('子パッケージ間に関連がある場合、ダイアグラムが生成される', () => {
            const typeA = {fqn: 'org.example.model.TypeA', isDeprecated: false};
            const typeB = {fqn: 'org.example.service.TypeB', isDeprecated: false};
            setupDomainData(['org.example'], [typeA, typeB]);
            globalThis.typeRelationsData = {
                relations: [{from: 'org.example.model.TypeA', to: 'org.example.service.TypeB'}]
            };
            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.model': {title: 'model'},
                'org.example.service': {title: 'service'},
                'org.example.model.TypeA': {title: 'TypeA'},
                'org.example.service.TypeB': {title: 'TypeB'}
            });

            const packages = globalThis.domainData._packages;
            const parentPkg = packages.find(p => p.fqn === 'org.example');
            const allPackageRelations = [{from: 'org.example.model', to: 'org.example.service'}];

            const result = createPackageRelationDiagram(parentPkg, packages, allPackageRelations);

            assert.ok(result !== null, 'ダイアグラムが生成されること');
            assert.ok(result.includes('graph'), 'Mermaidグラフが含まれること');

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
            delete globalThis.glossaryData;
        });

        test('子パッケージが存在しない場合はnullを返す', () => {
            const typeA = {fqn: 'org.example.TypeA', isDeprecated: false};
            setupDomainData(['org.example'], [typeA]);
            globalThis.typeRelationsData = {relations: []};
            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.TypeA': {title: 'TypeA'}
            });

            const packages = globalThis.domainData._packages;
            const parentPkg = packages.find(p => p.fqn === 'org.example');
            const allPackageRelations = [];

            const result = createPackageRelationDiagram(parentPkg, packages, allPackageRelations);

            assert.equal(result, null, 'ダイアグラムが生成されないこと');

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
            delete globalThis.glossaryData;
        });

        test('子パッケージ間に関連がない場合はnullを返す', () => {
            const typeA = {fqn: 'org.example.model.TypeA', isDeprecated: false};
            const typeB = {fqn: 'org.example.service.TypeB', isDeprecated: false};
            setupDomainData(['org.example'], [typeA, typeB]);
            globalThis.typeRelationsData = {relations: []}; // 型関連なし
            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.model': {title: 'model'},
                'org.example.service': {title: 'service'},
                'org.example.model.TypeA': {title: 'TypeA'},
                'org.example.service.TypeB': {title: 'TypeB'}
            });

            const packages = globalThis.domainData._packages;
            const parentPkg = packages.find(p => p.fqn === 'org.example');
            const allPackageRelations = [];

            const result = createPackageRelationDiagram(parentPkg, packages, allPackageRelations);

            assert.equal(result, null, 'ダイアグラムが生成されないこと');

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
            delete globalThis.glossaryData;
        });
    });

    test.describe('createPackageDirectRelationDiagram', () => {
        test('対象パッケージが他パッケージへ直接依存している場合、ダイアグラムが生成される', () => {
            const typeA = {fqn: 'org.example.model.TypeA', isDeprecated: false};
            const typeB = {fqn: 'org.external.TypeB', isDeprecated: false};
            setupDomainData(['org.example'], [typeA, typeB]);
            globalThis.typeRelationsData = {
                relations: [{from: 'org.example.model.TypeA', to: 'org.external.TypeB'}]
            };
            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.model': {title: 'model'},
                'org.external': {title: 'external'},
                'org.example.model.TypeA': {title: 'TypeA'},
                'org.external.TypeB': {title: 'TypeB'}
            });

            const packages = globalThis.domainData._packages;
            const modelPkg = packages.find(p => p.fqn === 'org.example.model');
            const allPackageRelations = [{from: 'org.example.model', to: 'org.external'}];

            const result = createPackageDirectRelationDiagram(modelPkg, allPackageRelations);

            assert.ok(result !== null, 'ダイアグラムが生成されること');
            assert.ok(result.includes('graph'), 'Mermaidグラフが含まれること');

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
            delete globalThis.glossaryData;
        });

        test('対象パッケージに関連がない場合はnullを返す', () => {
            const typeA = {fqn: 'org.example.TypeA', isDeprecated: false};
            setupDomainData(['org.example'], [typeA]);
            globalThis.typeRelationsData = {relations: []};
            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.TypeA': {title: 'TypeA'}
            });

            const packages = globalThis.domainData._packages;
            const parentPkg = packages.find(p => p.fqn === 'org.example');
            const allPackageRelations = [];

            const result = createPackageDirectRelationDiagram(parentPkg, allPackageRelations);

            assert.equal(result, null, 'ダイアグラムが生成されないこと');

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
            delete globalThis.glossaryData;
        });
    });

    test.describe('DomainApp', () => {
        test('domainData が undefined の場合、#domain-main にエラーメッセージを表示する', () => {
            delete globalThis.domainData;
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;

            const main = doc.createElement("div");
            doc.elementsById.set("domain-main", main);

            DomainApp.init();

            // エラー要素が追加されているか確認
            assert.equal(main.children.length, 1, "main に 1 つの子要素があること");
            assert.equal(main.children[0].tagName, 'p', "子要素が <p> タグであること");
            assert.ok(main.children[0].className.includes('jig-data-error'), "class に jig-data-error が含まれること");
            assert.ok(main.children[0].textContent.includes('domain-data.js'), "エラーメッセージに data ファイル名が含まれること");
        });

        test('glossaryData が undefined の場合、警告を表示してレンダリング続行する', () => {
            delete globalThis.glossaryData;
            globalThis.typeRelationsData = {relations: []}; // 他の optional データは設定
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;

            const main = doc.createElement("div");
            doc.elementsById.set("domain-main", main);
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));

            // setupDomainData で domainData を初期化
            setupDomainData([], []);

            DomainApp.init();

            // 警告要素が追加されているか確認
            const warning = main.children.find(el => el.className && el.className.includes('jig-data-warning'));
            assert.ok(warning, "warning 要素が存在すること");
            assert.ok(warning.textContent.includes('glossary-data.js'), "警告メッセージに glossary-data.js が含まれること");

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
        });

        test('typeRelationsData が undefined の場合、警告を表示してレンダリング続行する', () => {
            delete globalThis.typeRelationsData;
            setGlossaryData({}); // 他の optional データは設定
            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;

            const main = doc.createElement("div");
            doc.elementsById.set("domain-main", main);
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));

            setupDomainData([], []);

            DomainApp.init();

            const warning = main.children.find(el => el.className && el.className.includes('jig-data-warning'));
            assert.ok(warning, "warning 要素が存在すること");
            assert.ok(warning.textContent.includes('type-relations-data.js'), "警告メッセージに type-relations-data.js が含まれること");

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });
    });

    test.describe('パッケージ処理（renderPackages）', () => {
        test('複数のパッケージを持つdomainDataでレンダリングされること', () => {
            const types = [
                {fqn: 'org.example.User', isDeprecated: false, fields: [], methods: [], staticMethods: []},
                {fqn: 'org.example.domain.Account', isDeprecated: false, fields: [], methods: [], staticMethods: []},
            ];
            setupDomainData(['org.example'], types);
            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.User': {title: 'User'},
                'org.example.domain': {title: 'domain'},
                'org.example.domain.Account': {title: 'Account'},
            });

            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;

            const main = doc.createElement("div");
            doc.elementsById.set("domain-main", main);
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            globalThis.typeRelationsData = {relations: []};

            DomainApp.init();

            // パッケージがレンダリングされていることを確認
            const packageSections = main.children.filter(el => el.className && el.className.includes('jig-card--type'));
            assert.ok(packageSections.length > 0, "パッケージセクションが存在すること");

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
        });

        test('パッケージ間の型関連からパッケージ関連が導出されること', () => {
            const types = [
                {fqn: 'org.example.User', isDeprecated: false, fields: [], methods: [], staticMethods: []},
                {fqn: 'org.other.Service', isDeprecated: false, fields: [], methods: [], staticMethods: []},
            ];
            setupDomainData(['org.example', 'org.other'], types);
            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.User': {title: 'User'},
                'org.other': {title: 'other'},
                'org.other.Service': {title: 'Service'},
            });

            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;

            const main = doc.createElement("div");
            doc.elementsById.set("domain-main", main);
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            // パッケージ間に型関連がある場合
            globalThis.typeRelationsData = {
                relations: [
                    {from: 'org.example.User', to: 'org.other.Service'},
                ]
            };

            DomainApp.init();

            // エラーが出ず正常に処理されることを確認
            assert.ok(main.children.length > 0, "main に要素が追加されること");

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
        });
    });

    test.describe('パッケージ関連図の処理', () => {
        test('createRelationDiagramは空パッケージでnullを返す', () => {
            const pkg = {fqn: 'app', types: []};  // type を持たないパッケージ
            const typesMap = new Map();
            const typeRelations = [];

            const result = createRelationDiagram(pkg, typeRelations, typesMap);
            assert.equal(result, null);
        });

        test('createRelationDiagram: 外部向きエッジ長を調整する', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};
            const typeX = {fqn: 'org.other.X', isDeprecated: false};
            const typeY = {fqn: 'org.third.Y', isDeprecated: false};

            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.A': {title: 'A'},
                'org.example.B': {title: 'B'},
                'org.other': {title: 'other'},
                'org.third': {title: 'third'},
            });

            const typesMap = new Map([
                ['org.example.A', typeA],
                ['org.example.B', typeB],
                ['org.other.X', typeX],
                ['org.third.Y', typeY],
            ]);
            const typeRelations = [
                {from: 'org.example.A', to: 'org.example.B'},
                {from: 'org.example.A', to: 'org.other.X'},
                {from: 'org.example.B', to: 'org.third.Y'},
            ];

            const pkg = {fqn: 'org.example', types: [{fqn: 'org.example.A'}, {fqn: 'org.example.B'}]};
            const result = createRelationDiagram(pkg, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            const idOther = Jig.util.fqnToId("n", 'org.other');
            const idThird = Jig.util.fqnToId("n", 'org.third');
            assert.ok(result.includes(`${idA} ---> ${idOther}`), '浅いノードから外部へのエッジは長くなること');
            assert.ok(result.includes(`${idB} --> ${idThird}`), '深いノードから外部へのエッジは短いこと');

            delete globalThis.glossaryData;
        });
    });
});
