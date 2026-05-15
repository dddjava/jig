const test = require('node:test');
const assert = require('node:assert/strict');
const {DocumentStub, setGlossaryData} = require('./dom-stub.js');

global.window = {addEventListener: () => {}};
global.document = new DocumentStub();
require('../../main/resources/templates/assets/jig-util.js');
require('../../main/resources/templates/assets/jig-data.js');
require('../../main/resources/templates/assets/jig-glossary.js');
require('../../main/resources/templates/assets/jig-mermaid.js');
require('../../main/resources/templates/assets/jig-dom.js');
require('../../main/resources/templates/assets/jig-bootstrap.js');
const Jig = globalThis.Jig;

const DomainApp = require('../../main/resources/templates/assets/domain.js');
const {renderPackageNavItem, getDirectChildPackages, createRelationDiagram, createTypeRelationDiagram, createTypeClassDiagramSource, createPackageRelationDiagram, createPackageDirectRelationDiagram, buildPackages} = DomainApp;

function setupDomainData(domainPackageRoots, types) {
    globalThis.domainData = {domainPackageRoots, types};
    Jig.data.resetCache();
    const packages = buildPackages(domainPackageRoots, types);
    Jig.data.domain.setPackages(packages);
    const childrenMap = new Map(packages.map(p => [p.fqn, []]));
    packages.forEach(p => {
        const parent = p.fqn.substring(0, p.fqn.lastIndexOf('.'));
        if (childrenMap.has(parent)) childrenMap.get(parent).push(p);
    });
    Jig.data.domain.setChildPackagesMap(childrenMap);
}

test.describe('domain.js', () => {
    test.describe('renderPackageNavItem', () => {
        test('子が1つだけでタイプを持たないパッケージの場合、統合して表示する', () => {
            const comPkg = {fqn: 'com', types: []};
            const examplePkg = {fqn: 'com.example', types: [{fqn: 'com.example.MyClass'}]};
            const childPackagesMap = new Map([
                ['com', [examplePkg]],
                ['com.example', []]
            ]);
            const typesMap = new Map([
                ['com.example.MyClass', {fqn: 'com.example.MyClass', isDeprecated: false}]
            ]);

            const result = renderPackageNavItem(comPkg, childPackagesMap, typesMap);

            assert.equal(result.tagName, 'li');
            const summaryLink = result.children[0].children[0]; // div.in-page-sidebar__item-header > a
            assert.equal(summaryLink.tagName, 'a');
            assert.equal(summaryLink.children[1].textContent, 'com/example');
            assert.equal(summaryLink.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'com.example'));
        });

        test('タイプを持つパッケージを表示する', () => {
            const comPkg = {fqn: 'com', types: []};
            const examplePkg = {fqn: 'com.example', types: []};
            const deepPkg = {fqn: 'com.example.deep', types: [{fqn: 'com.example.deep.MyClass'}]};
            const childPackagesMap = new Map([
                ['com', [examplePkg]],
                ['com.example', [deepPkg]],
                ['com.example.deep', []]
            ]);
            const typesMap = new Map([
                ['com.example.deep.MyClass', {fqn: 'com.example.deep.MyClass', methods: [], isDeprecated: false}]
            ]);

            const result = renderPackageNavItem(comPkg, childPackagesMap, typesMap);

            assert.equal(result.tagName, 'li');
            const summaryLink = result.children[0].children[0]; // div.in-page-sidebar__item-header > a
            assert.equal(summaryLink.tagName, 'a');
            // com -> example -> deep で、deep がタイプを持つので統合が止まる
            assert.equal(summaryLink.children[1].textContent, 'com/example/deep');
            assert.equal(summaryLink.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'com.example.deep'));
        });

        test('複数段階のパッケージが1つずつ続く場合、全て統合して表示する', () => {
            const comPkg = {fqn: 'com', types: []};
            const examplePkg = {fqn: 'com.example', types: []};
            const subPkg = {fqn: 'com.example.sub', types: []};
            const deepPkg = {fqn: 'com.example.sub.deep', types: [{fqn: 'com.example.sub.deep.MyClass'}]};
            const childPackagesMap = new Map([
                ['com', [examplePkg]],
                ['com.example', [subPkg]],
                ['com.example.sub', [deepPkg]],
                ['com.example.sub.deep', []]
            ]);
            const typesMap = new Map([
                ['com.example.sub.deep.MyClass', {fqn: 'com.example.sub.deep.MyClass', methods: [], isDeprecated: false}]
            ]);

            const result = renderPackageNavItem(comPkg, childPackagesMap, typesMap);

            const summaryLink = result.children[0].children[0]; // div.in-page-sidebar__item-header > a
            assert.equal(summaryLink.children[1].textContent, 'com/example/sub/deep');
            assert.equal(summaryLink.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'com.example.sub.deep'));
        });

        test('子パッケージを持つパッケージを表示する', () => {
            const comPkg = {fqn: 'com', types: []};
            const examplePkg = {fqn: 'com.example', types: []};
            const sub1Pkg = {fqn: 'com.example.sub1', types: [{fqn: 'com.example.sub1.MyClass1'}]};
            const sub2Pkg = {fqn: 'com.example.sub2', types: [{fqn: 'com.example.sub2.MyClass2'}]};
            const childPackagesMap = new Map([
                ['com', [examplePkg]],
                ['com.example', [sub1Pkg, sub2Pkg]],
                ['com.example.sub1', []],
                ['com.example.sub2', []]
            ]);
            const typesMap = new Map([
                ['com.example.sub1.MyClass1', {fqn: 'com.example.sub1.MyClass1', isDeprecated: false}],
                ['com.example.sub2.MyClass2', {fqn: 'com.example.sub2.MyClass2', isDeprecated: false}]
            ]);

            const result = renderPackageNavItem(comPkg, childPackagesMap, typesMap);

            const summaryLink = result.children[0].children[0]; // div.in-page-sidebar__item-header > a
            // com -> example は統合（example は2つの子を持つので統合されない）
            assert.equal(summaryLink.children[1].textContent, 'com/example');
            assert.equal(summaryLink.attributes.get('href'), '#' + Jig.util.fqnToId("domain", 'com.example'));

            const childList = result.children[1]; // ul.in-page-sidebar__links
            const childPackageNames = Array.from(childList.children)
                .filter(child => child.attributes.has('data-kind-children'))
                .map(child => child.children[0].children[0].children[1].textContent);
            assert.ok(childPackageNames.includes('sub1'), 'example should have sub1 as child');
            assert.ok(childPackageNames.includes('sub2'), 'example should have sub2 as child');
        });
    });

    test.describe('getDirectChildPackages', () => {
        test('直下の子パッケージのみを返す', () => {
            const comPkg = {fqn: 'com', types: []};
            const examplePkg = {fqn: 'com.example', types: []};
            const subPkg = {fqn: 'com.example.sub', types: []};
            const deepPkg = {fqn: 'com.example.sub.deep', types: [{fqn: 'com.example.sub.deep.MyClass'}]};
            const childPackagesMap = new Map([
                ['com', [examplePkg]],
                ['com.example', [subPkg]],
                ['com.example.sub', [deepPkg]],
                ['com.example.sub.deep', []]
            ]);

            const result = getDirectChildPackages(comPkg, childPackagesMap);

            assert.equal(result.length, 1);
            assert.equal(result[0].fqn, 'com.example');
        });

        test('直下の子パッケージが複数の場合、全て返す', () => {
            const comPkg = {fqn: 'com', types: []};
            const examplePkg = {fqn: 'com.example', types: [{fqn: 'com.example.MyClass'}]};
            const utilPkg = {fqn: 'com.util', types: [{fqn: 'com.util.MyClass'}]};
            const childPackagesMap = new Map([
                ['com', [examplePkg, utilPkg]],
                ['com.example', []],
                ['com.util', []]
            ]);

            const result = getDirectChildPackages(comPkg, childPackagesMap);

            assert.equal(result.length, 2);
            assert.ok(result.some(p => p.fqn === 'com.example'));
            assert.ok(result.some(p => p.fqn === 'com.util'));
        });

        test('子パッケージがない場合は空配列を返す', () => {
            const comPkg = {fqn: 'com', types: [{fqn: 'com.MyClass'}]};
            const childPackagesMap = new Map([
                ['com', []]
            ]);

            const result = getDirectChildPackages(comPkg, childPackagesMap);

            assert.equal(result.length, 0);
        });
    });

    test.describe('createRelationDiagram', () => {
        test('関係図のMermaidソースを生成する', () => {
            const typesMap = new Map([
                ['org.example.A', {fqn: 'org.example.A', isDeprecated: false}],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false}],
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
        });

        test('タイプを持たない空パッケージはnullを返す', () => {
            const pkg = {fqn: 'app', types: []};
            const result = createRelationDiagram(pkg, [], new Map());
            assert.equal(result, null);
        });

        test('外部向きエッジ長を深さに応じて調整する', () => {
            const typesMap = new Map([
                ['org.example.A', {fqn: 'org.example.A', isDeprecated: false}],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false}],
                ['org.other.X', {fqn: 'org.other.X', isDeprecated: false}],
                ['org.third.Y', {fqn: 'org.third.Y', isDeprecated: false}],
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
        });
    });

    test.describe('createTypeRelationDiagram', () => {
        test('クラスの関連図を生成する（出力・入力両方）', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};
            const typeC = {fqn: 'org.example.C', isDeprecated: false};

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
            assert.ok(result.includes(`click ${idA} href "#${domainIdA}"`), 'Aへのクリックリンクが含まれること');
            assert.ok(result.includes(`style ${idA} font-weight:bold`), '自身（A）が強調表示されること');
            const sgId = Jig.util.fqnToId("sg", 'org.example');
            assert.ok(result.includes(`subgraph ${sgId}`), 'パッケージのサブグラフが含まれること');
        });

        test('関連がない場合は null を返す', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};

            const typesMap = new Map([['org.example.A', typeA]]);
            const typeRelations = [];

            const result = createTypeRelationDiagram(typeA, typeRelations, typesMap);

            assert.equal(result, null);
        });

        test('showOutgoing=false の場合、出力方向の関連は含まれない', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};
            const typeC = {fqn: 'org.example.C', isDeprecated: false};
            const typesMap = new Map([
                ['org.example.A', typeA],
                ['org.example.B', typeB],
                ['org.example.C', typeC],
            ]);
            const typeRelations = [
                {from: 'org.example.A', to: 'org.example.B'},
                {from: 'org.example.C', to: 'org.example.A'},
            ];

            const result = createTypeRelationDiagram(typeA, typeRelations, typesMap, 'TB', {showOutgoing: false, showIncoming: true});

            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            const idC = Jig.util.fqnToId("n", 'org.example.C');
            assert.ok(result, '図が生成されること');
            assert.ok(!result.includes(idB), 'B（関連先）は含まれないこと');
            assert.ok(result.includes(`${idC} --> ${idA}`), 'C→A の関連（関連元）は含まれること');
        });

        test('showIncoming=false の場合、入力方向の関連は含まれない', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};
            const typeC = {fqn: 'org.example.C', isDeprecated: false};
            const typesMap = new Map([
                ['org.example.A', typeA],
                ['org.example.B', typeB],
                ['org.example.C', typeC],
            ]);
            const typeRelations = [
                {from: 'org.example.A', to: 'org.example.B'},
                {from: 'org.example.C', to: 'org.example.A'},
            ];

            const result = createTypeRelationDiagram(typeA, typeRelations, typesMap, 'TB', {showOutgoing: true, showIncoming: false});

            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            const idC = Jig.util.fqnToId("n", 'org.example.C');
            assert.ok(result, '図が生成されること');
            assert.ok(result.includes(`${idA} --> ${idB}`), 'A→B の関連（関連先）は含まれること');
            assert.ok(!result.includes(idC), 'C（関連元）は含まれないこと');
        });

        test('showOutgoing=false かつ showIncoming=false の場合は対象クラス単体の図を返す', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};
            const typesMap = new Map([
                ['org.example.A', typeA],
                ['org.example.B', typeB],
            ]);
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B'}];

            const result = createTypeRelationDiagram(typeA, typeRelations, typesMap, 'TB', {showOutgoing: false, showIncoming: false});

            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result, '図が生成されること');
            assert.ok(result.includes(idA), '対象クラス（A）は含まれること');
            assert.ok(!result.includes(idB), '関連クラス（B）は含まれないこと');
        });

        test('subgraph外向きエッジは深さに応じて長さが変わる', () => {
            const typeA = {fqn: 'org.example.A', isDeprecated: false};
            const typeB = {fqn: 'org.example.B', isDeprecated: false};
            const typeX = {fqn: 'org.other.X', isDeprecated: false};

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
        });
    });

    test.describe('createPackageRelationDiagram', () => {
        test('子パッケージ間に関連がある場合、ダイアグラムが生成される', () => {
            const packages = [
                {fqn: 'org.example', types: []},
                {fqn: 'org.example.model', types: [{fqn: 'org.example.model.TypeA'}]},
                {fqn: 'org.example.service', types: [{fqn: 'org.example.service.TypeB'}]},
            ];
            const parentPkg = packages[0];
            const allPackageRelations = [{from: 'org.example.model', to: 'org.example.service'}];

            const result = createPackageRelationDiagram(parentPkg, packages, allPackageRelations);

            assert.ok(result !== null, 'ダイアグラムが生成されること');
            assert.ok(result.includes('graph'), 'Mermaidグラフが含まれること');
        });

        test('子パッケージが存在しない場合はnullを返す', () => {
            const packages = [
                {fqn: 'org.example', types: [{fqn: 'org.example.TypeA'}]},
            ];
            const parentPkg = packages[0];

            const result = createPackageRelationDiagram(parentPkg, packages, []);

            assert.equal(result, null, 'ダイアグラムが生成されないこと');
        });

        test('子パッケージ間に関連がない場合はnullを返す', () => {
            const packages = [
                {fqn: 'org.example', types: []},
                {fqn: 'org.example.model', types: [{fqn: 'org.example.model.TypeA'}]},
                {fqn: 'org.example.service', types: [{fqn: 'org.example.service.TypeB'}]},
            ];
            const parentPkg = packages[0];

            const result = createPackageRelationDiagram(parentPkg, packages, []);

            assert.equal(result, null, 'ダイアグラムが生成されないこと');
        });
    });

    test.describe('createPackageDirectRelationDiagram', () => {
        test('対象パッケージが他パッケージへ直接依存している場合、ダイアグラムが生成される', () => {
            const modelPkg = {fqn: 'org.example.model', types: [{fqn: 'org.example.model.TypeA'}]};
            const allPackageRelations = [{from: 'org.example.model', to: 'org.external'}];

            const result = createPackageDirectRelationDiagram(modelPkg, allPackageRelations);

            assert.ok(result !== null, 'ダイアグラムが生成されること');
            assert.ok(result.includes('graph'), 'Mermaidグラフが含まれること');
        });

        test('対象パッケージに関連がない場合はnullを返す', () => {
            const parentPkg = {fqn: 'org.example', types: [{fqn: 'org.example.TypeA'}]};

            const result = createPackageDirectRelationDiagram(parentPkg, []);

            assert.equal(result, null, 'ダイアグラムが生成されないこと');
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

            assert.equal(main.children.length, 1, "main に 1 つの子要素があること");
            assert.equal(main.children[0].tagName, 'p', "子要素が <p> タグであること");
            assert.ok(main.children[0].className.includes('jig-data-error'), "class に jig-data-error が含まれること");
            assert.ok(main.children[0].textContent.includes('domain-data.js'), "エラーメッセージに data ファイル名が含まれること");
        });

        test('glossaryData が undefined の場合、警告を表示してレンダリング続行する', () => {
            delete globalThis.glossaryData;
            globalThis.typeRelationsData = {relations: []};
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
            assert.ok(warning.textContent.includes('glossary-data.js'), "警告メッセージに glossary-data.js が含まれること");

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
        });

        test('typeRelationsData が undefined の場合、警告を表示してレンダリング続行する', () => {
            delete globalThis.typeRelationsData;
            setGlossaryData({});
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

    test.describe('renderPackages', () => {
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
            globalThis.typeRelationsData = {
                relations: [
                    {from: 'org.example.User', to: 'org.other.Service'},
                ]
            };

            DomainApp.init();

            assert.ok(main.children.length > 0, "main に要素が追加されること");

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
        });
    });

    test.describe('createTypeClassDiagramSource', () => {
        function makeTypesMap(fqns) {
            return new Map(fqns.map(fqn => [fqn, {fqn, isDeprecated: false, fields: [], methods: [], staticMethods: []}]));
        }

        test('kinds に「フィールド型」を含む場合 --> を使用する', () => {
            const typesMap = makeTypesMap(['org.example.A', 'org.example.B']);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['フィールド型']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idA} --> ${idB}`), `association エッジが含まれること: ${result}`);
        });

        test('kinds に「継承クラス」を含む場合 ..|> を使用する', () => {
            const typesMap = makeTypesMap(['org.example.A', 'org.example.B']);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['継承クラス']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idA} ..|> ${idB}`), `realization エッジが含まれること: ${result}`);
        });

        test('kinds に「実装インタフェース」を含む場合 --|> を使用する', () => {
            const typesMap = makeTypesMap(['org.example.A', 'org.example.B']);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['実装インタフェース']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idB} <|-- ${idA}`), `inheritance エッジが含まれること: ${result}`);
        });

        test('kinds がそれ以外の場合 ..> を使用する', () => {
            const typesMap = makeTypesMap(['org.example.A', 'org.example.B']);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['メソッド引数']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idA} ..> ${idB}`), `dependency エッジが含まれること: ${result}`);
        });

        test('kinds に「フィールド型引数」を含む場合 --> を使用する', () => {
            const typesMap = makeTypesMap(['org.example.A', 'org.example.B']);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['フィールド型引数']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idA} --> ${idB}`), `フィールド型引数も association エッジであること: ${result}`);
        });

        test('kinds がない場合 ..> を使用する', () => {
            const typesMap = makeTypesMap(['org.example.A', 'org.example.B']);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B'}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idA} ..> ${idB}`), `dependency エッジが含まれること: ${result}`);
        });

        test('継承クラスはフィールド型より優先される', () => {
            const typesMap = makeTypesMap(['org.example.A', 'org.example.B']);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['フィールド型', '継承クラス']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            const idA = Jig.util.fqnToId("n", 'org.example.A');
            const idB = Jig.util.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idA} ..|> ${idB}`), `継承クラスが優先されること: ${result}`);
        });

        test('フィールドのジェネリクス型が ~ 構文で表示される', () => {
            const typesMap = new Map([
                ['org.example.A', {
                    fqn: 'org.example.A', isDeprecated: false,
                    fields: [{name: 'items', typeRef: {fqn: 'java.util.List', typeArgumentRefs: [{fqn: 'org.example.B'}]}}],
                    methods: [], staticMethods: []
                }],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false, fields: [], methods: [], staticMethods: []}],
            ]);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['フィールド型']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            assert.ok(result.includes('List~B~'), `フィールドにジェネリクスが含まれること: ${result}`);
        });

        test('メソッド戻り値のジェネリクス型が ~ 構文で表示される', () => {
            const typesMap = new Map([
                ['org.example.A', {
                    fqn: 'org.example.A', isDeprecated: false,
                    fields: [],
                    methods: [{
                        fqn: 'org.example.A#getItems()',
                        visibility: 'PUBLIC',
                        parameters: [],
                        returnTypeRef: {fqn: 'java.util.Optional', typeArgumentRefs: [{fqn: 'org.example.B'}]}
                    }],
                    staticMethods: []
                }],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false, fields: [], methods: [], staticMethods: []}],
            ]);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['メソッド戻り値']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            assert.ok(result.includes('Optional~B~'), `メソッド戻り値にジェネリクスが含まれること: ${result}`);
        });

        test('メソッドパラメータのジェネリクス型が ~ 構文で表示される', () => {
            const typesMap = new Map([
                ['org.example.A', {
                    fqn: 'org.example.A', isDeprecated: false,
                    fields: [],
                    methods: [{
                        fqn: 'org.example.A#process()',
                        visibility: 'PUBLIC',
                        parameters: [{typeRef: {fqn: 'java.util.List', typeArgumentRefs: [{fqn: 'org.example.B'}]}}],
                        returnTypeRef: {fqn: 'void'}
                    }],
                    staticMethods: []
                }],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false, fields: [], methods: [], staticMethods: []}],
            ]);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['メソッド引数']}];
            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap);
            assert.ok(result.includes('List~B~'), `メソッドパラメータにジェネリクスが含まれること: ${result}`);
        });

        test('showFields=false の場合、フィールドは含まれない', () => {
            const typesMap = new Map([
                ['org.example.A', {
                    fqn: 'org.example.A', isDeprecated: false,
                    fields: [{name: 'value', typeRef: {fqn: 'java.lang.String'}}],
                    methods: [], staticMethods: []
                }],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false, fields: [], methods: [], staticMethods: []}],
            ]);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B', kinds: ['フィールド型']}];

            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap, 'TB', {showFields: false});

            assert.ok(result, '図が生成されること');
            assert.ok(!result.includes('value'), 'フィールドが含まれないこと');
        });

        test('showMethods=false の場合、メソッドは含まれない', () => {
            const typesMap = new Map([
                ['org.example.A', {
                    fqn: 'org.example.A', isDeprecated: false,
                    fields: [],
                    methods: [{fqn: 'org.example.A#doSomething()', visibility: 'PUBLIC', parameters: [], returnTypeRef: {fqn: 'void'}}],
                    staticMethods: []
                }],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false, fields: [], methods: [], staticMethods: []}],
            ]);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B'}];

            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap, 'TB', {showMethods: false});

            assert.ok(result, '図が生成されること');
            assert.ok(!result.includes('doSomething'), 'メソッドが含まれないこと');
        });

        test('maxVisibility=PUBLIC の場合、PUBLIC メソッドのみ表示される', () => {
            const typesMap = new Map([
                ['org.example.A', {
                    fqn: 'org.example.A', isDeprecated: false,
                    fields: [],
                    methods: [
                        {fqn: 'org.example.A#pub()', visibility: 'PUBLIC', parameters: [], returnTypeRef: {fqn: 'void'}},
                        {fqn: 'org.example.A#prot()', visibility: 'PROTECTED', parameters: [], returnTypeRef: {fqn: 'void'}},
                        {fqn: 'org.example.A#pkg()', visibility: 'PACKAGE', parameters: [], returnTypeRef: {fqn: 'void'}},
                        {fqn: 'org.example.A#priv()', visibility: 'PRIVATE', parameters: [], returnTypeRef: {fqn: 'void'}},
                    ],
                    staticMethods: []
                }],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false, fields: [], methods: [], staticMethods: []}],
            ]);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B'}];

            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap, 'TB', {maxVisibility: 'PUBLIC'});

            assert.ok(result.includes('pub'), 'PUBLIC メソッドは含まれること');
            assert.ok(!result.includes('prot'), 'PROTECTED メソッドは含まれないこと');
            assert.ok(!result.includes('pkg'), 'PACKAGE メソッドは含まれないこと');
            assert.ok(!result.includes('priv'), 'PRIVATE メソッドは含まれないこと');
        });

        test('maxVisibility=PROTECTED の場合、PUBLIC と PROTECTED メソッドが表示される', () => {
            const typesMap = new Map([
                ['org.example.A', {
                    fqn: 'org.example.A', isDeprecated: false,
                    fields: [],
                    methods: [
                        {fqn: 'org.example.A#pub()', visibility: 'PUBLIC', parameters: [], returnTypeRef: {fqn: 'void'}},
                        {fqn: 'org.example.A#prot()', visibility: 'PROTECTED', parameters: [], returnTypeRef: {fqn: 'void'}},
                        {fqn: 'org.example.A#priv()', visibility: 'PRIVATE', parameters: [], returnTypeRef: {fqn: 'void'}},
                    ],
                    staticMethods: []
                }],
                ['org.example.B', {fqn: 'org.example.B', isDeprecated: false, fields: [], methods: [], staticMethods: []}],
            ]);
            const typeA = typesMap.get('org.example.A');
            const typeRelations = [{from: 'org.example.A', to: 'org.example.B'}];

            const result = createTypeClassDiagramSource(typeA, typeRelations, typesMap, 'TB', {maxVisibility: 'PROTECTED'});

            assert.ok(result.includes('pub'), 'PUBLIC メソッドは含まれること');
            assert.ok(result.includes('prot'), 'PROTECTED メソッドは含まれること');
            assert.ok(!result.includes('priv'), 'PRIVATE メソッドは含まれないこと');
        });
    });

    test.describe('サイドバーテキストフィルター', () => {
        function setupFilterTest() {
            const types = [
                {fqn: 'org.example.FooClass', isDeprecated: false, kind: '不明', fields: [], methods: [], staticMethods: []},
                {fqn: 'org.example.BarClass', isDeprecated: false, kind: '不明', fields: [], methods: [], staticMethods: []},
            ];
            setupDomainData(['org.example'], types);
            setGlossaryData({
                'org.example': {title: 'example'},
                'org.example.FooClass': {title: 'FooClass'},
                'org.example.BarClass': {title: 'BarClass'},
            });
            globalThis.typeRelationsData = {relations: []};

            const doc = new DocumentStub();
            doc.body.classList.add("domain-model");
            global.document = doc;

            const main = doc.createElement("div");
            doc.elementsById.set("domain-main", main);

            const sidebarList = doc.createElement("div");
            doc.elementsById.set("domain-sidebar-list", sidebarList);

            const sidebar = doc.createElement("div");
            sidebar.setAttribute("id", "domain-sidebar");
            sidebar.appendChild(sidebarList);
            doc.elementsById.set("domain-sidebar", sidebar);

            const filterInput = doc.createElement("input");
            filterInput.setAttribute("id", "domain-sidebar-filter");
            doc.elementsById.set("domain-sidebar-filter", filterInput);

            DomainApp.init();

            return {sidebar, filterInput};
        }

        test('フィルターテキストに一致する型アイテムは表示、一致しない型アイテムは非表示になる', () => {
            const {sidebar, filterInput} = setupFilterTest();

            filterInput.value = 'FooClass';
            filterInput.dispatchEvent({type: 'input'});

            const typeItems = [...sidebar.querySelectorAll('div[data-kind]')];
            const fooLi = typeItems.find(div => div.querySelector('a')?.textContent.includes('FooClass'))?.closest('li');
            const barLi = typeItems.find(div => div.querySelector('a')?.textContent.includes('BarClass'))?.closest('li');

            assert.ok(fooLi, 'FooClass の li が存在すること');
            assert.ok(barLi, 'BarClass の li が存在すること');
            assert.equal(fooLi.style.display, '', 'FooClass は表示されること');
            assert.equal(barLi.style.display, 'none', 'BarClass は非表示になること');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
        });

        test('フィルターをクリアすると全アイテムが再表示される', () => {
            const {sidebar, filterInput} = setupFilterTest();

            filterInput.value = 'FooClass';
            filterInput.dispatchEvent({type: 'input'});
            filterInput.value = '';
            filterInput.dispatchEvent({type: 'input'});

            const typeItems = [...sidebar.querySelectorAll('div[data-kind]')];
            typeItems.forEach(div => {
                assert.notEqual(div.closest('li').style.display, 'none', `クリア後に ${div.querySelector('a')?.textContent} が表示されること`);
            });

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
        });

        test('何にもマッチしないテキスト後にクリアすると全アイテムが再表示される', () => {
            const {sidebar, filterInput} = setupFilterTest();

            filterInput.value = 'ZZZNOMATCH';
            filterInput.dispatchEvent({type: 'input'});
            filterInput.value = '';
            filterInput.dispatchEvent({type: 'input'});

            const typeItems = [...sidebar.querySelectorAll('div[data-kind]')];
            assert.ok(typeItems.length > 0, 'アイテムが存在すること');
            typeItems.forEach(div => {
                assert.notEqual(div.closest('li').style.display, 'none', `クリア後に ${div.querySelector('a')?.textContent} が表示されること`);
            });

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
        });

        test('パッケージ名がマッチすればクラスが一致しなくてもパッケージは表示される', () => {
            const {sidebar, filterInput} = setupFilterTest();

            filterInput.value = 'example';
            filterInput.dispatchEvent({type: 'input'});

            const packageItems = [...sidebar.querySelectorAll('[data-kind-children]')];
            assert.ok(packageItems.length > 0, 'パッケージアイテムが存在すること');
            const visiblePackage = packageItems.find(item => item.style.display !== 'none');
            assert.ok(visiblePackage, 'パッケージ名にマッチするパッケージが表示されること');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
        });
    });
});
