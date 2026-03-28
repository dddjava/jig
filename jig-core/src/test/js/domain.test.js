const test = require('node:test');
const assert = require('node:assert/strict');
const { Element, DocumentStub, setGlossaryData } = require('./dom-stub.js');

// jig-common.js と jig.js をロード（window・document のスタブが必要）
global.window = global.window || { addEventListener: () => {} };
global.document = new DocumentStub();
require('../../main/resources/templates/assets/jig-common.js');
require('../../main/resources/templates/assets/jig.js');

const { DomainApp, renderPackageNavItem, getDirectChildPackages, createRelationDiagram } = require('../../main/resources/templates/assets/domain.js');

// ヘルパー関数：_typesMap と _childPackagesMap を設定
function setupDomainData(packages, types) {
    globalThis.domainData = { packages, types };
    globalThis.domainData._typesMap = new Map(types.map(t => [t.fqn, t]));
    globalThis.domainData._childPackagesMap = new Map(
        packages.map(p => [p.fqn, packages.filter(q => {
            const prefix = p.fqn + ".";
            return q.fqn.startsWith(prefix) && q.fqn.indexOf(".", prefix.length) === -1;
        })])
    );
}

test.describe('domain.js', () => {
    test.describe('typeLinkResolver（DomainApp.init() で登録）', () => {
        test('domain型に対して、ページ内リンクのhrefを返す', () => {
            const domainType = {fqn: 'org.example.Account', isDeprecated: false, fields: [], methods: [], staticMethods: []};
            setupDomainData([], [domainType]);
            setGlossaryData({'org.example.Account': {title: '口座', description: ''}});
            const doc = new DocumentStub();
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = { relations: [] };

            DomainApp.init();

            const resolved = globalThis.Jig.dom.typeLinkResolver('org.example.Account');
            assert.equal(resolved.href, '#' + globalThis.Jig.fqnToId("domain", 'org.example.Account'));
            assert.equal(resolved.className, undefined);

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            globalThis.Jig.dom.typeLinkResolver = null;
        });

        test('deprecatedなdomain型に対して、deprecatedクラスを返す', () => {
            const domainType = {fqn: 'org.example.OldClass', isDeprecated: true, fields: [], methods: [], staticMethods: []};
            setupDomainData([], [domainType]);
            setGlossaryData({});
            const doc = new DocumentStub();
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = { relations: [] };

            DomainApp.init();

            const resolved = globalThis.Jig.dom.typeLinkResolver('org.example.OldClass');
            assert.equal(resolved.href, '#' + globalThis.Jig.fqnToId("domain", 'org.example.OldClass'));
            assert.equal(resolved.className, 'deprecated');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            globalThis.Jig.dom.typeLinkResolver = null;
        });

        test('domain型でない場合、weakクラスと単純名を返す（hrefなし）', () => {
            setupDomainData([], []);
            setGlossaryData({});
            const doc = new DocumentStub();
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = { relations: [] };

            DomainApp.init();

            const resolved = globalThis.Jig.dom.typeLinkResolver('java.lang.String');
            assert.equal(resolved.href, undefined);
            assert.equal(resolved.className, 'weak');
            assert.equal(resolved.text, 'String');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            globalThis.Jig.dom.typeLinkResolver = null;
        });

        test('リゾルバー経由でdomain型はリンク付き要素になる', () => {
            const domainType = {fqn: 'org.example.User', isDeprecated: false, fields: [], methods: [], staticMethods: []};
            setupDomainData([], [domainType]);
            setGlossaryData({'org.example.User': {title: 'ユーザー', description: ''}});
            const doc = new DocumentStub();
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = { relations: [] };

            DomainApp.init();

            const result = globalThis.Jig.dom.createElementForTypeRef({fqn: 'org.example.User'}, 'my-class');
            assert.equal(result.tagName, 'a');
            assert.equal(result.className, 'my-class');
            assert.equal(result.textContent, 'ユーザー');
            assert.equal(result.attributes.get('href'), '#' + globalThis.Jig.fqnToId("domain", 'org.example.User'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            globalThis.Jig.dom.typeLinkResolver = null;
        });

        test('リゾルバー経由でdomain型でない場合はweak spanになる', () => {
            setupDomainData([], []);
            setGlossaryData({});
            const doc = new DocumentStub();
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = { relations: [] };

            DomainApp.init();

            const result = globalThis.Jig.dom.createElementForTypeRef({fqn: 'java.lang.String'}, 'my-class');
            assert.equal(result.tagName, 'span');
            assert.equal(result.textContent, 'String');
            assert.ok(result.className.includes('weak'));
            assert.ok(result.className.includes('my-class'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            globalThis.Jig.dom.typeLinkResolver = null;
        });

        test('型引数がある場合、spanで型と型引数を組み立てる', () => {
            const domainTypes = [
                {fqn: 'java.util.List', isDeprecated: false, fields: [], methods: [], staticMethods: []},
                {fqn: 'org.example.Item', isDeprecated: false, fields: [], methods: [], staticMethods: []}
            ];
            setupDomainData([], domainTypes);
            setGlossaryData({'java.util.List': {title: 'List', description: ''}});
            const doc = new DocumentStub();
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = { relations: [] };

            DomainApp.init();

            const typeRef = {
                fqn: 'java.util.List',
                typeArgumentRefs: [{fqn: 'org.example.Item'}]
            };
            const result = globalThis.Jig.dom.createElementForTypeRef(typeRef, 'generic-type');

            assert.equal(result.tagName, 'span');
            assert.equal(result.className, 'generic-type');
            assert.ok(result.children.length > 0);

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            globalThis.Jig.dom.typeLinkResolver = null;
        });

        test('配列型（Hoge[]）はベース型のリンクを解決して[]を付け直す', () => {
            const domainType = {fqn: 'org.example.Item', isDeprecated: false, fields: [], methods: [], staticMethods: []};
            setupDomainData([], [domainType]);
            setGlossaryData({'org.example.Item': {title: 'アイテム', description: ''}});
            const doc = new DocumentStub();
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = { relations: [] };

            DomainApp.init();

            const result = globalThis.Jig.dom.createElementForTypeRef({fqn: 'org.example.Item[]'});
            assert.equal(result.tagName, 'a');
            assert.equal(result.textContent, 'アイテム[]');
            assert.equal(result.attributes.get('href'), '#' + globalThis.Jig.fqnToId("domain", 'org.example.Item'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            globalThis.Jig.dom.typeLinkResolver = null;
        });

        test('多次元配列型（Hoge[][]）もベース型のリンクを解決して[][]を付け直す', () => {
            const domainType = {fqn: 'org.example.Item', isDeprecated: false, fields: [], methods: [], staticMethods: []};
            setupDomainData([], [domainType]);
            setGlossaryData({'org.example.Item': {title: 'アイテム', description: ''}});
            const doc = new DocumentStub();
            global.document = doc;
            doc.elementsById.set("domain-sidebar-list", doc.createElement("div"));
            doc.elementsById.set("domain-main", doc.createElement("div"));
            globalThis.typeRelationsData = { relations: [] };

            DomainApp.init();

            const result = globalThis.Jig.dom.createElementForTypeRef({fqn: 'org.example.Item[][]'});
            assert.equal(result.tagName, 'a');
            assert.equal(result.textContent, 'アイテム[][]');
            assert.equal(result.attributes.get('href'), '#' + globalThis.Jig.fqnToId("domain", 'org.example.Item'));

            delete globalThis.domainData;
            delete globalThis.glossaryData;
            delete globalThis.typeRelationsData;
            globalThis.Jig.dom.typeLinkResolver = null;
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

            setupDomainData([comPkg, examplePkg], []);
            setGlossaryData({
                'com': {title: 'com'},
                'com.example': {title: 'example'}
            });

            const result = renderPackageNavItem(comPkg);

            assert.equal(result.tagName, 'details');
            const summaryLink = result.children[0].children[0];
            assert.equal(summaryLink.tagName, 'a');
            assert.equal(summaryLink.textContent, 'com/example');
            assert.equal(summaryLink.attributes.get('href'), '#' + globalThis.Jig.fqnToId("domain", 'com.example'));

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

            setupDomainData([comPkg, examplePkg, deepPkg], [{fqn: 'com.example.deep.MyClass', methods: []}]);
            setGlossaryData({
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.deep': {title: 'deep'},
                'com.example.deep.MyClass': {title: 'MyClass'}
            });

            const result = renderPackageNavItem(comPkg);

            assert.equal(result.tagName, 'details');
            const summaryLink = result.children[0].children[0];
            assert.equal(summaryLink.tagName, 'a');
            // com -> example -> deep で、deep がタイプを持つので統合が止まる
            assert.equal(summaryLink.textContent, 'com/example/deep');
            assert.equal(summaryLink.attributes.get('href'), '#' + globalThis.Jig.fqnToId("domain", 'com.example.deep'));

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

            setupDomainData([comPkg, examplePkg, subPkg, deepPkg], [{fqn: 'com.example.sub.deep.MyClass', methods: []}]);
            setGlossaryData({
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.sub': {title: 'sub'},
                'com.example.sub.deep': {title: 'deep'},
                'com.example.sub.deep.MyClass': {title: 'MyClass'}
            });

            const result = renderPackageNavItem(comPkg);

            const summaryLink = result.children[0].children[0];
            // com -> example -> sub -> deep と続くので、sub がタイプを持つまで統合
            assert.equal(summaryLink.textContent, 'com/example/sub/deep');
            assert.equal(summaryLink.attributes.get('href'), '#' + globalThis.Jig.fqnToId("domain", 'com.example.sub.deep'));

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

            setupDomainData([comPkg, examplePkg, sub1Pkg, sub2Pkg], []);
            setGlossaryData({
                'com': {title: 'com'},
                'com.example': {title: 'example'},
                'com.example.sub1': {title: 'sub1'},
                'com.example.sub2': {title: 'sub2'}
            });

            const result = renderPackageNavItem(comPkg);

            const summaryLink = result.children[0].children[0];
            // com -> example は統合（example は1つだけの子を持つから）
            assert.equal(summaryLink.textContent, 'com/example');
            assert.equal(summaryLink.attributes.get('href'), '#' + globalThis.Jig.fqnToId("domain", 'com.example'));

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

            setupDomainData([comPkg, examplePkg, subPkg, deepPkg], []);

            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 1);
            assert.equal(result[0].fqn, 'com.example');

            delete globalThis.domainData;
        });

        test('直下の子パッケージが複数の場合、全て返す', () => {
            const comPkg = {fqn: 'com'};
            const examplePkg = {fqn: 'com.example'};
            const utilPkg = {fqn: 'com.util'};

            setupDomainData([comPkg, examplePkg, utilPkg], []);

            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 2);
            assert.ok(result.some(p => p.fqn === 'com.example'));
            assert.ok(result.some(p => p.fqn === 'com.util'));

            delete globalThis.domainData;
        });

        test('子パッケージがない場合は空配列を返す', () => {
            const comPkg = {fqn: 'com'};

            setupDomainData([comPkg], []);

            const result = getDirectChildPackages(comPkg);

            assert.equal(result.length, 0);

            delete globalThis.domainData;
        });
    });

    test.describe('createRelationDiagram', () => {
        test('関係図のMermaidソースを生成する（fqnToMermaidIdが正常に動作すること）', () => {
            const pkg = { fqn: 'org.example', types: [{ fqn: 'org.example.A' }, { fqn: 'org.example.B' }] };
            const typeA = { fqn: 'org.example.A', isDeprecated: false };
            const typeB = { fqn: 'org.example.B', isDeprecated: false };
            setupDomainData([pkg], [typeA, typeB]);
            globalThis.typeRelationsData = {
                relations: [
                    { from: 'org.example.A', to: 'org.example.B' }
                ]
            };
            setGlossaryData({
                'org.example.A': { title: 'A' },
                'org.example.B': { title: 'B' },
                'org.example': { title: 'example' }
            });

            const result = createRelationDiagram(pkg);

            assert.ok(result.includes('graph TB'), 'デフォルトの向きが含まれていること');
            const idA = globalThis.Jig.fqnToId("n", 'org.example.A');
            const idB = globalThis.Jig.fqnToId("n", 'org.example.B');
            assert.ok(result.includes(`${idA} --> ${idB}`), '関連が含まれていること');

            const sgId = globalThis.Jig.fqnToId("sg", 'org.example');
            assert.ok(result.includes(`subgraph ${sgId} ["example"]`), 'subgraphにパッケージ名のラベルが含まれていること');

            delete globalThis.domainData;
            delete globalThis.typeRelationsData;
            delete globalThis.glossaryData;
        });
    });

    test.describe('DomainApp', () => {
        test('domainData が undefined の場合、#domain-main にエラーメッセージを表示する', () => {
            delete globalThis.domainData;
            const doc = new DocumentStub();
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
            globalThis.typeRelationsData = { relations: [] }; // 他の optional データは設定
            const doc = new DocumentStub();
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
});
