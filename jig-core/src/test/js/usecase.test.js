const assert = require('assert');
const { test, beforeEach } = require('node:test');
const path = require('path');
const { DocumentStub, EventStub, setGlossaryData } = require('./dom-stub.js');

const jigCommonJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/jig-common.js');
const jigMermaidDiagramJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/jig-mermaid-diagram.js');
const jigJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/jig.js');
const usecaseJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/usecase.js');

// モック用のデータ
const mockUsecaseData = {
    usecases: [
        {
            fqn: "com.example.ServiceA",
            fields: [
                { name: "field1", typeRef: { fqn: "java.lang.String"}, isDeprecated: false }
            ],
            staticMethods: [
                {
                    fqn: "com.example.ServiceA#staticMethod1()",
                    visibility: "PUBLIC",
                    declaration: "staticMethod1():void",
                    parameterTypeRefs: [],
                    returnTypeRef: { fqn: "void" }
                }
            ],
            methods: [
                {
                    fqn: "com.example.ServiceA#method1()",
                    visibility: "PUBLIC",
                    parameterTypeRefs: [],
                    returnTypeRef: { fqn: "void" },
                    declaration: "method1():void",
                    isDeprecated: false,
                    callMethods: ["com.example.ServiceA#otherMethod()"]
                },
                {
                    fqn: "com.example.ServiceA#otherMethod()",
                    visibility: "PUBLIC",
                    parameterTypeRefs: [],
                    returnTypeRef: { fqn: "void" },
                    declaration: "otherMethod():void",
                    isDeprecated: false,
                    callMethods: []
                }
            ]
        }
    ]
};

test.describe('UsecaseApp', () => {
    let doc;
    let UsecaseApp;
    let buildSequenceDiagram;
    let buildSequenceDiagramCode;

    beforeEach(() => {
        doc = new DocumentStub();
        global.document = doc;
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };
        delete globalThis.inboundData;
        // IntersectionObserver は設定しない → lazyRender が即時コールバック

        // チェックボックス要素を事前登録
        ['show-fields', 'show-static-methods', 'show-diagrams', 'show-details', 'show-descriptions', 'show-declarations', 'show-diagram-internal-methods', 'show-diagram-outbound-ports'].forEach(id => {
            const el = doc.createElement('input');
            el.id = id;
            el.checked = true;
        });
        // show-diagram-domain-types はデフォルト未チェック
        const domainTypesCheckbox = doc.createElement('input');
        domainTypesCheckbox.id = 'show-diagram-domain-types';
        domainTypesCheckbox.checked = false;
        // コンテナ要素を事前登録
        ['usecase-sidebar-list', 'usecase-list'].forEach(id => {
            const el = doc.createElement('div');
            el.id = id;
        });

        delete require.cache[jigCommonJsPath];
        delete require.cache[jigMermaidDiagramJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];
        require(jigCommonJsPath);
        require(jigMermaidDiagramJsPath);
        require(jigJsPath);

        // Mermaid の複雑なDOM操作を回避するためにオーバーライド
        globalThis.Jig.mermaid.renderWithControls = (container, source, {direction = 'LR'} = {}) => {
            const code = (typeof source === 'function') ? source(direction) : source;
            const pre = doc.createElement('pre');
            pre.className = 'mermaid';
            pre.textContent = code;
            container.appendChild(pre);
        };

        ({ UsecaseApp, buildSequenceDiagram, buildSequenceDiagramCode } = require(usecaseJsPath));
    });

    test('init should render data from globalThis.usecaseData', () => {
        setGlossaryData( {
            "com.example.ServiceA": { title: "ServiceA", description: "Description of ServiceA" },
            "com.example.ServiceA#staticMethod1()": { title: "staticMethod1", description: "Description of staticMethod1" },
            "com.example.ServiceA#method1()": { title: "method1", description: "Description of method1" },
            "com.example.ServiceA#otherMethod()": { title: "otherMethod", description: "" }
        });
        globalThis.usecaseData = mockUsecaseData;
        UsecaseApp.init();

        const sidebar = document.getElementById('usecase-sidebar-list');
        assert.strictEqual(sidebar.children.length, 1);
        assert.strictEqual(sidebar.querySelector('p').textContent, 'ユースケース');
        const sidebarLinks = sidebar.querySelectorAll('a');
        assert.strictEqual(sidebarLinks[0].textContent, 'ServiceA');
        assert.ok(sidebarLinks[1].classList.contains('in-page-sidebar__link--sub'));
        assert.strictEqual(sidebarLinks[1].textContent, 'method1');
        assert.strictEqual(sidebarLinks[2].textContent, 'otherMethod');

        const mainList = document.getElementById('usecase-list');
        assert.strictEqual(mainList.children.length, 1);
        const serviceSection = mainList.children[0];
        assert.strictEqual(serviceSection.querySelector('h3 a').id, globalThis.Jig.fqnToId("type", 'com.example.ServiceA'));
        assert.strictEqual(serviceSection.querySelector('h3 a').textContent, 'ServiceA');
        assert.strictEqual(serviceSection.querySelector('.declaration').textContent, 'com.example.ServiceA');
        assert.strictEqual(serviceSection.querySelector('.markdown').innerHTML, 'Description of ServiceA');

        const fieldsSection = serviceSection.querySelector('section.methods-section');
        assert.ok(fieldsSection);
        assert.strictEqual(fieldsSection.querySelector('.method-name').textContent, 'field1');

        const staticMethodsSection = serviceSection.querySelector('section.static-methods');
        assert.ok(staticMethodsSection);
        assert.strictEqual(staticMethodsSection.querySelector('h4').textContent, 'staticメソッド');
        assert.strictEqual(staticMethodsSection.querySelector('.method-name').textContent, 'staticMethod1');

        const methodSection = serviceSection.querySelector('article.jig-card--item');
        assert.ok(methodSection);
        assert.strictEqual(methodSection.querySelector('h4').id, globalThis.Jig.fqnToId("method", 'com.example.ServiceA#method1()'));
        assert.strictEqual(methodSection.querySelector('h4').textContent, 'method1');
        assert.strictEqual(methodSection.querySelector('.declaration').textContent, 'ServiceA#method1()');

        const diagramContainer = methodSection.querySelector('.diagram-container');
        assert.ok(diagramContainer);
        const tabs = diagramContainer.querySelector('.diagram-tabs');
        assert.ok(tabs);
        assert.strictEqual(tabs.children.length, 2);
        assert.strictEqual(tabs.children[0].textContent, 'ユースケース図');
        assert.strictEqual(tabs.children[1].textContent, 'シーケンス図');

        const mermaidPres = methodSection.querySelectorAll('.mermaid');
        assert.strictEqual(mermaidPres.length, 2);
        const graphCode = mermaidPres[0].textContent;
        assert.ok(graphCode.includes('graph LR'));
        assert.ok(graphCode.includes('subgraph'), 'ユースケース図にsubgraphが含まれること');
        assert.ok(graphCode.includes('ServiceA'), 'subgraphにクラス名が含まれること');
        assert.ok(graphCode.includes('direction LR'), 'subgraphにdirection LRが含まれること');
        assert.ok(graphCode.includes('classDef'), 'Theme classDefが含まれるべき');
        assert.ok(mermaidPres[1].textContent.includes('sequenceDiagram'));

        const description = methodSection.querySelector('.description');
        assert.strictEqual(description.innerHTML, 'Description of method1');
    });

    test('クラス単位の図がクラスヘッダー直下にレンダリングされる', () => {
        setGlossaryData( {
            "com.example.ServiceA": { title: "ServiceA" },
            "com.example.ServiceA#method1()": { title: "method1" },
            "com.example.ServiceA#otherMethod()": { title: "otherMethod" }
        });
        globalThis.usecaseData = mockUsecaseData;
        UsecaseApp.init();

        const mainList = document.getElementById('usecase-list');
        const serviceSection = mainList.children[0];
        
        // クラス単位のダイアグラムコンテナがあること
        const classDiagram = serviceSection.querySelector('.diagram-container.class-diagram');
        assert.ok(classDiagram);
        
        // Mermaidのプレ要素があること
        const mermaidPre = classDiagram.querySelector('.mermaid');
        assert.ok(mermaidPre);
        
        const code = mermaidPre.textContent;
        assert.ok(code.includes('graph LR'));
        // 内部メソッド間の関連があること
        assert.ok(code.includes('->'));
        // クラス単位の図にはsubgraphが含まれない（単純なグラフ）
        assert.ok(!code.includes('subgraph'));
    });

    test('renderUsecaseList should handle empty data', () => {
        globalThis.usecaseData = { usecases: [] };
        UsecaseApp.init();

        const mainList = document.getElementById('usecase-list');
        assert.strictEqual(mainList.textContent, 'データなし');
    });

    test('domain-data.jsがある場合にリゾルバーがdomain.html#fqnリンクを設定する', () => {
        globalThis.domainData = {
            types: [
                {fqn: 'com.example.Order', isDeprecated: false}
            ]
        };
        globalThis.usecaseData = { usecases: [] };
        UsecaseApp.init();

        const resolver = globalThis.Jig.dom.typeLinkResolver;
        assert.ok(resolver, 'リゾルバーが設定されていること');

        const resolved = resolver('com.example.Order');
        assert.strictEqual(resolved.href, 'domain.html#' + globalThis.Jig.fqnToId("domain", 'com.example.Order'));
        assert.strictEqual(resolved.className, undefined);

        delete globalThis.domainData;
    });

    test('domain-data.jsがない場合、リゾルバーは設定されない', () => {
        delete globalThis.domainData;
        globalThis.usecaseData = { usecases: [] };
        UsecaseApp.init();

        assert.strictEqual(globalThis.Jig.dom.typeLinkResolver, null);
    });

    test('deprecatedなdomain型はdeprecatedクラスを返す', () => {
        globalThis.domainData = {
            types: [
                {fqn: 'com.example.OldClass', isDeprecated: true}
            ]
        };
        globalThis.usecaseData = { usecases: [] };
        UsecaseApp.init();

        const resolved = globalThis.Jig.dom.typeLinkResolver('com.example.OldClass');
        assert.strictEqual(resolved.href, 'domain.html#' + globalThis.Jig.fqnToId("domain", 'com.example.OldClass'));
        assert.strictEqual(resolved.className, 'deprecated');

        delete globalThis.domainData;
    });

    test('domain型でない場合、リゾルバーはnullを返す', () => {
        globalThis.domainData = {
            types: []
        };
        globalThis.usecaseData = { usecases: [] };
        UsecaseApp.init();

        const resolver = globalThis.Jig.dom.typeLinkResolver;
        assert.ok(resolver);
        const resolved = resolver('java.lang.String');
        assert.strictEqual(resolved, null);

        delete globalThis.domainData;
    });

    test('initControls should toggle body classes', () => {
        globalThis.usecaseData = mockUsecaseData;
        UsecaseApp.init();

        const showFields = document.getElementById('show-fields');
        const showStaticMethods = document.getElementById('show-static-methods');
        const showDiagrams = document.getElementById('show-diagrams');
        const showDetails = document.getElementById('show-details');
        const showDescriptions = document.getElementById('show-descriptions');
        const showDeclarations = document.getElementById('show-declarations');

        // Initial state
        assert.strictEqual(showFields.checked, true);
        assert.strictEqual(document.body.classList.contains('hide-usecase-fields'), false);

        // Toggle fields
        showFields.checked = false;
        showFields.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-fields'), true);

        // Toggle static methods
        showStaticMethods.checked = false;
        showStaticMethods.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-static-methods'), true);

        // Toggle diagrams
        showDiagrams.checked = false;
        showDiagrams.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-diagrams'), true);

        // Toggle details
        showDetails.checked = false;
        showDetails.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-details'), true);

        // Toggle descriptions
        showDescriptions.checked = false;
        showDescriptions.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-descriptions'), true);

        // Toggle declarations
        showDeclarations.checked = false;
        showDeclarations.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-declarations'), true);
    });

    test('シーケンス図タブを選択した状態で再レンダリングしてもシーケンス図が維持される', () => {
        globalThis.usecaseData = mockUsecaseData;
        setGlossaryData( {
            "com.example.ServiceA": { title: "ServiceA" },
            "com.example.ServiceA#method1()": { title: "method1" },
            "com.example.ServiceA#otherMethod()": { title: "otherMethod" }
        });
        UsecaseApp.init();

        const methodFqn = "com.example.ServiceA#method1()";
        const methodSection = document.getElementById(globalThis.Jig.fqnToId("method", methodFqn)).parentElement;
        const sequenceBtn = methodSection.querySelectorAll('.diagram-tabs button')[1];

        // シーケンス図タブをクリック
        sequenceBtn.dispatchEvent(new window.Event('click'));

        // 状態が 'sequence' になっていることを確認
        assert.strictEqual(UsecaseApp.state.selectedTabs.get(methodFqn), 'sequence');

        // 再レンダリング（チェックボックス変更をシミュレート）
        const showDiagramInternalMethods = document.getElementById('show-diagram-internal-methods');
        showDiagramInternalMethods.checked = false;
        showDiagramInternalMethods.dispatchEvent(new window.Event('change'));

        // 再レンダリング後の要素を取得
        const newMethodSection = document.getElementById(globalThis.Jig.fqnToId("method", methodFqn)).parentElement;
        const newSequenceBtn = newMethodSection.querySelectorAll('.diagram-tabs button')[1];
        const newSequencePanel = newMethodSection.querySelectorAll('.diagram-panel')[1];

        // シーケンス図タブが active で、パネルが hidden でないことを確認
        assert.ok(newSequenceBtn.classList.contains('active'));
        assert.ok(!newSequencePanel.classList.contains('hidden'));
    });

    test('inbound呼び出し元はクラスノード化されinbound.htmlへのリンクが付与される', () => {
        globalThis.usecaseData = mockUsecaseData;
        setGlossaryData( {
            "com.example.ServiceA": { title: "ServiceA" },
            "com.example.ServiceA#method1()": { title: "method1", description: "Description of method1" },
            "com.example.ServiceA#otherMethod()": { title: "otherMethod" },
            "web.Ctrl": { title: "Ctrl" }
        });
        globalThis.inboundData = {
            controllers: [
                {
                    relations: [
                        {from: 'web.Ctrl#entry()', to: 'com.example.ServiceA#method1()'}
                    ]
                }
            ]
        };

        const internalCheckbox = document.getElementById('show-diagram-internal-methods');
        internalCheckbox.checked = false;

        UsecaseApp.init();

        const methodSection = document.getElementById(globalThis.Jig.fqnToId("method", 'com.example.ServiceA#method1()')).parentElement;
        assert.ok(methodSection);
        const mermaidPre = methodSection.querySelector('.mermaid');
        assert.ok(mermaidPre);
        const code = mermaidPre.textContent;
        assert.ok(code.includes('click'));
        assert.ok(code.includes('./inbound.html#' + globalThis.Jig.fqnToId("adapter", 'web.Ctrl')));
        assert.ok(code.includes('click'));
        assert.ok(code.includes('#' + globalThis.Jig.fqnToId("method", 'com.example.ServiceA#method1()')));
    });

    test('引数にドメインモデル型を持つメソッドの図にドメインモデルノードと引数→メソッドエッジが追加される', () => {
        document.getElementById('show-diagram-domain-types').checked = true;
        globalThis.domainData = {
            types: [{ fqn: 'com.example.Order', isDeprecated: false }]
        };
        const usecaseDataWithDomainParam = {
            usecases: [{
                fqn: "com.example.ServiceA",
                fields: [],
                staticMethods: [],
                methods: [{
                    fqn: "com.example.ServiceA#method1(Order)",
                    visibility: "PUBLIC",
                    parameterTypeRefs: [{ fqn: "com.example.Order" }],
                    returnTypeRef: { fqn: "void" },
                    declaration: "method1(Order):void",
                    isDeprecated: false,
                    callMethods: []
                }]
            }]
        };
        setGlossaryData({
            "com.example.ServiceA": { title: "ServiceA" },
            "com.example.ServiceA#method1(Order)": { title: "method1" },
            "com.example.Order": { title: "Order" }
        });
        globalThis.usecaseData = usecaseDataWithDomainParam;
        UsecaseApp.init();

        const methodId = globalThis.Jig.fqnToId("method", 'com.example.ServiceA#method1(Order)');
        const methodSection = document.getElementById(methodId).parentElement;
        const mermaidPre = methodSection.querySelector('.mermaid');
        assert.ok(mermaidPre, 'Mermaid図が生成されること');
        const code = mermaidPre.textContent;

        const orderNodeId = globalThis.Jig.fqnToId("node", 'com.example.Order');
        const methodNodeId = globalThis.Jig.fqnToId("node", 'com.example.ServiceA#method1(Order)');
        assert.ok(code.includes(orderNodeId), 'ドメインモデルのノードが含まれること');
        assert.ok(code.includes(`${orderNodeId} --> ${methodNodeId}`), '引数→メソッドのエッジが含まれること');
        assert.ok(code.includes('./domain.html#' + globalThis.Jig.fqnToId("domain", 'com.example.Order')), 'domain.htmlへのリンクが含まれること');

        delete globalThis.domainData;
    });

    test('戻り値にドメインモデル型を持つメソッドの図にドメインモデルノードとメソッド→戻り値エッジが追加される', () => {
        document.getElementById('show-diagram-domain-types').checked = true;
        globalThis.domainData = {
            types: [{ fqn: 'com.example.Order', isDeprecated: false }]
        };
        const usecaseDataWithDomainReturn = {
            usecases: [{
                fqn: "com.example.ServiceA",
                fields: [],
                staticMethods: [],
                methods: [{
                    fqn: "com.example.ServiceA#findOrder()",
                    visibility: "PUBLIC",
                    parameterTypeRefs: [],
                    returnTypeRef: { fqn: "com.example.Order" },
                    declaration: "findOrder():Order",
                    isDeprecated: false,
                    callMethods: []
                }]
            }]
        };
        setGlossaryData({
            "com.example.ServiceA": { title: "ServiceA" },
            "com.example.ServiceA#findOrder()": { title: "findOrder" },
            "com.example.Order": { title: "Order" }
        });
        globalThis.usecaseData = usecaseDataWithDomainReturn;
        UsecaseApp.init();

        const methodId = globalThis.Jig.fqnToId("method", 'com.example.ServiceA#findOrder()');
        const methodSection = document.getElementById(methodId).parentElement;
        const mermaidPre = methodSection.querySelector('.mermaid');
        assert.ok(mermaidPre, 'Mermaid図が生成されること');
        const code = mermaidPre.textContent;

        const orderNodeId = globalThis.Jig.fqnToId("node", 'com.example.Order');
        const methodNodeId = globalThis.Jig.fqnToId("node", 'com.example.ServiceA#findOrder()');
        assert.ok(code.includes(orderNodeId), 'ドメインモデルのノードが含まれること');
        assert.ok(code.includes(`${methodNodeId} --> ${orderNodeId}`), 'メソッド→戻り値のエッジが含まれること');

        delete globalThis.domainData;
    });

    test('show-diagram-domain-typesがOFFの場合、ドメインモデルノードは表示されない', () => {
        // デフォルトは unchecked
        globalThis.domainData = {
            types: [{ fqn: 'com.example.Order', isDeprecated: false }]
        };
        const usecaseDataWithDomain = {
            usecases: [{
                fqn: "com.example.ServiceA",
                fields: [],
                staticMethods: [],
                methods: [{
                    fqn: "com.example.ServiceA#findOrder()",
                    visibility: "PUBLIC",
                    parameterTypeRefs: [],
                    returnTypeRef: { fqn: "com.example.Order" },
                    declaration: "findOrder():Order",
                    isDeprecated: false,
                    callMethods: []
                }]
            }]
        };
        setGlossaryData({
            "com.example.ServiceA": { title: "ServiceA" },
            "com.example.ServiceA#findOrder()": { title: "findOrder" },
            "com.example.Order": { title: "Order" }
        });
        globalThis.usecaseData = usecaseDataWithDomain;
        UsecaseApp.init();

        const methodId = globalThis.Jig.fqnToId("method", 'com.example.ServiceA#findOrder()');
        const methodSection = document.getElementById(methodId)?.parentElement;
        // ドメインノードのみでエッジがなければ図自体が生成されないか、生成されてもドメインノードを含まない
        const mermaidPre = methodSection?.querySelector('.mermaid');
        if (mermaidPre) {
            const orderNodeId = globalThis.Jig.fqnToId("node", 'com.example.Order');
            assert.ok(!mermaidPre.textContent.includes(orderNodeId), 'チェックOFFではドメインノードが含まれないこと');
        }

        delete globalThis.domainData;
    });

    test('domainDataがない場合でもドメインモデルノードが追加されず正常動作する', () => {
        delete globalThis.domainData;
        setGlossaryData({
            "com.example.ServiceA": { title: "ServiceA" },
            "com.example.ServiceA#method1()": { title: "method1" },
            "com.example.ServiceA#otherMethod()": { title: "otherMethod" }
        });
        globalThis.usecaseData = mockUsecaseData;
        UsecaseApp.init();

        const methodId = globalThis.Jig.fqnToId("method", 'com.example.ServiceA#method1()');
        const methodSection = document.getElementById(methodId).parentElement;
        const mermaidPres = methodSection.querySelectorAll('.mermaid');
        assert.ok(mermaidPres.length > 0, 'Mermaid図は生成されること');
        // ドメインモデルノードは含まれない（エラーなし）
        const graphCode = mermaidPres[0].textContent;
        assert.ok(graphCode.includes('graph LR'), '正常なグラフが生成されること');
    });
});

test.describe('buildSequenceDiagram', () => {
    let buildSequenceDiagram;

    beforeEach(() => {
        delete require.cache[jigCommonJsPath];
        delete require.cache[jigMermaidDiagramJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];
        delete globalThis.inboundData;

        global.document = new DocumentStub();
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };

        require(jigCommonJsPath);
        require(jigMermaidDiagramJsPath);
        require(jigJsPath);
        ({ buildSequenceDiagram } = require(usecaseJsPath));
    });

    test('callMethodsが空の場合はcallsが空', () => {
        const rootMethod = { fqn: 'com.example.ServiceA#method1()', callMethods: [] };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

        const result = buildSequenceDiagram(rootMethod, methodMap);

        assert.strictEqual(result.calls.length, 0);
        assert.strictEqual(result.participants.length, 1);
        assert.strictEqual(result.participants[0].label, 'method1');
        assert.strictEqual(result.participants[0].kind, "usecase");
    });

    test('ユースケース内メソッドへの呼び出しはメソッド単位のパーティシパント', () => {
        const otherMethod = { fqn: 'com.example.ServiceA#otherMethod()', callMethods: [] };
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.ServiceA#otherMethod()']
        };
        const methodMap = new Map([
            ['com.example.ServiceA#method1()', rootMethod],
            ['com.example.ServiceA#otherMethod()', otherMethod]
        ]);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.participants.length, 2);
        assert.strictEqual(result.participants[0].label, 'method1');
        assert.strictEqual(result.participants[0].kind, "usecase");
        assert.strictEqual(result.participants[1].label, 'otherMethod');
        assert.strictEqual(result.participants[1].kind, "usecase");
        assert.strictEqual(result.calls.length, 1);
        assert.strictEqual(result.calls[0].label, '');
    });

    test('ユースケース外メソッドへの呼び出しはoutboundOperationSetにある場合だけクラス単位のパーティシパント', () => {
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.RepositoryB#save(com.example.Entity)']
        };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);
        const outboundOperationSet = new Set(['com.example.RepositoryB#save(com.example.Entity)']);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.participants.length, 2);
        assert.strictEqual(result.participants[1].label, 'RepositoryB');
        assert.strictEqual(result.participants[1].kind, 'outbound');
        assert.strictEqual(result.calls.length, 1);
        assert.strictEqual(result.calls[0].label, 'save');
    });

    test('outboundOperationSetにない外部呼び出しは無視される', () => {
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.RepositoryB#save()']
        };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.participants.length, 1);
        assert.strictEqual(result.calls.length, 0);
    });

    test('内部と外部への呼び出しが混在する場合も両方適切に処理', () => {
        const otherMethod = { fqn: 'com.example.ServiceA#otherMethod()', callMethods: [] };
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: [
                'com.example.ServiceA#otherMethod()',
                'com.example.RepositoryB#save()'
            ]
        };
        const methodMap = new Map([
            ['com.example.ServiceA#method1()', rootMethod],
            ['com.example.ServiceA#otherMethod()', otherMethod]
        ]);
        const outboundOperationSet = new Set(['com.example.RepositoryB#save()']);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.participants.length, 3);
        assert.strictEqual(result.calls.length, 2);
        assert.strictEqual(result.calls[0].label, '');
        assert.strictEqual(result.calls[1].label, 'save');
    });

    test('再帰的に内部メソッドを処理する', () => {
        const deepMethod = { fqn: 'com.example.ServiceA#deepMethod()', callMethods: [] };
        const middleMethod = {
            fqn: 'com.example.ServiceA#middleMethod()',
            callMethods: ['com.example.ServiceA#deepMethod()']
        };
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.ServiceA#middleMethod()']
        };
        const methodMap = new Map([
            ['com.example.ServiceA#method1()', rootMethod],
            ['com.example.ServiceA#middleMethod()', middleMethod],
            ['com.example.ServiceA#deepMethod()', deepMethod]
        ]);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.participants.length, 3);
        assert.strictEqual(result.calls.length, 2);
    });

    test('循環参照があっても無限ループしない', () => {
        const methodB = {
            fqn: 'com.example.ServiceA#methodB()',
            callMethods: ['com.example.ServiceA#methodA()']
        };
        const methodA = {
            fqn: 'com.example.ServiceA#methodA()',
            callMethods: ['com.example.ServiceA#methodB()']
        };
        const methodMap = new Map([
            ['com.example.ServiceA#methodA()', methodA],
            ['com.example.ServiceA#methodB()', methodB]
        ]);

        const result = buildSequenceDiagram(methodA, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.participants.length, 2);
        // methodA->methodB と methodB->methodA の2呼び出し
        assert.strictEqual(result.calls.length, 2);
    });

    test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドはパーティシパントとして追加されず呼び出しがインライン化される', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase' };
        const methodB = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#C()'], kind: 'method' };
        const methodC = { fqn: 'pkg.Cls#C()', callMethods: [], kind: 'usecase' };
        const methodMap = new Map([
            ['pkg.Cls#A()', {...rootMethod, kind: 'usecase'}],
            ['pkg.Cls#B()', {...methodB, kind: 'method'}],
            ['pkg.Cls#C()', {...methodC, kind: 'usecase'}]
        ]);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        // A と C だけがパーティシパントとして残る
        assert.strictEqual(result.participants.length, 2);
        assert.ok(result.participants.find(p => p.id.includes('_A_')));
        assert.ok(result.participants.find(p => p.id.includes('_C_')));
        // コールは A -> C になる
        assert.strictEqual(result.calls.length, 1);
        assert.ok(result.calls[0].from.includes('_A_'));
        assert.ok(result.calls[0].to.includes('_C_'));
    });

    test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドを介した外部呼び出しもインライン化される(シーケンス図)', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase' };
        const methodB = { fqn: 'pkg.Cls#B()', callMethods: ['ext.Cls#method()'], kind: 'method' };
        const methodMap = new Map([
            ['pkg.Cls#A()', {...rootMethod, kind: 'usecase'}],
            ['pkg.Cls#B()', {...methodB, kind: 'method'}]
        ]);
        const outboundOperationSet = new Set(['ext.Cls#method()']);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.participants.length, 2);
        assert.ok(result.participants.find(p => p.kind === 'outbound'));
        assert.strictEqual(result.calls.length, 1);
        assert.ok(result.calls[0].from.includes('_A_'));
    });

    test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドの循環参照があっても無限ループしない(シーケンス図)', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase' };
        const methodB = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#C()'], kind: 'method' };
        const methodC = { fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#B()', 'pkg.Cls#D()'], kind: 'method' };
        const methodD = { fqn: 'pkg.Cls#D()', callMethods: [], kind: 'usecase' };
        const methodMap = new Map([
            ['pkg.Cls#A()', {...rootMethod, kind: 'usecase'}],
            ['pkg.Cls#B()', {...methodB, kind: 'method'}],
            ['pkg.Cls#C()', {...methodC, kind: 'method'}],
            ['pkg.Cls#D()', {...methodD, kind: 'usecase'}]
        ]);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.participants.length, 2);
        assert.strictEqual(result.calls.length, 1);
        assert.ok(result.calls[0].from.includes('_A_'));
        assert.ok(result.calls[0].to.includes('_D_'));
    });

    test('showDiagramOutboundPortsがfalseの場合、外部ポートはパーティシパントとして追加されない', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: ['ext.Cls#method()'], kind: 'usecase' };
        const methodMap = new Map([['pkg.Cls#A()', {...rootMethod, kind: 'usecase'}]]);
        const outboundOperationSet = new Set(['ext.Cls#method()']);

        const result = buildSequenceDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: false
        });

        assert.strictEqual(result.participants.length, 1);
        assert.strictEqual(result.calls.length, 0);
    });
});

test.describe('buildSequenceDiagramCode', () => {
    let buildSequenceDiagramCode;

    beforeEach(() => {
        delete require.cache[jigCommonJsPath];
        delete require.cache[jigMermaidDiagramJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];

        global.document = new DocumentStub();
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };

        require(jigCommonJsPath);
        require(jigMermaidDiagramJsPath);
        require(jigJsPath);
        ({ buildSequenceDiagramCode } = require(usecaseJsPath));
    });

    test('callsが空の場合はnullを返す', () => {
        const sequence = { participants: [{id: 'node-a', label: 'methodA', isExternal: false}], calls: [] };
        assert.strictEqual(buildSequenceDiagramCode(sequence), null);
    });

    test('外部パーティシパントはbox LightGrayに入り内部はその外に出力される', () => {
        const sequence = {
            participants: [
                {id: 'node-a', label: 'methodA', kind: "usecase"},
                {id: 'node-b', label: 'ClassB', kind: 'outbound'}
            ],
            calls: [
                {from: 'node-a', to: 'node-b', label: 'save'}
            ]
        };
        const code = buildSequenceDiagramCode(sequence);

        assert.ok(code.startsWith('sequenceDiagram\n'));
        assert.ok(code.includes('box outbounds'));
        assert.ok(code.includes('participant node-b as ClassB'));
        assert.ok(code.includes('end'));
        assert.ok(code.includes('participant node-a as methodA'));
        assert.ok(code.includes('node-a->>node-b: save'));
        // 内部パーティシパントはboxの前にある
        const boxEnd = code.indexOf('box outbounds');
        const internalIdx = code.indexOf('participant node-a as methodA');
        assert.ok(internalIdx < boxEnd);
    });

    test('外部パーティシパントがない場合はboxなしで生成する', () => {
        const sequence = {
            participants: [
                {id: 'node-a', label: 'methodA', isExternal: false},
                {id: 'node-b', label: 'methodB', isExternal: false}
            ],
            calls: [
                {from: 'node-a', to: 'node-b', label: ''}
            ]
        };
        const code = buildSequenceDiagramCode(sequence);

        assert.ok(code.startsWith('sequenceDiagram\n'));
        assert.ok(!code.includes('box'));
        assert.ok(code.includes('participant node-a as methodA'));
        assert.ok(code.includes('participant node-b as methodB'));
    });
});

test.describe('buildOutboundOperationSet', () => {
    let buildOutboundOperationSet;

    beforeEach(() => {
        delete require.cache[jigCommonJsPath];
        delete require.cache[jigMermaidDiagramJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];

        global.document = new DocumentStub();
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };

        require(jigCommonJsPath);
        require(jigMermaidDiagramJsPath);
        require(jigJsPath);
        ({ buildOutboundOperationSet } = require(usecaseJsPath));
    });

    test('nullの場合は空Setを返す', () => {
        assert.strictEqual(buildOutboundOperationSet(null).size, 0);
    });

    test('undefinedの場合は空Setを返す', () => {
        assert.strictEqual(buildOutboundOperationSet(undefined).size, 0);
    });

    test('outboundPortsがない場合は空Setを返す', () => {
        assert.strictEqual(buildOutboundOperationSet({}).size, 0);
    });

    test('outboundPortsのoperationsのfqnをSetに収集する', () => {
        const outboundData = {
            outboundPorts: [
                {
                    fqn: 'com.example.RepositoryB',
                    operations: [
                        { fqn: 'com.example.RepositoryB#save()', signature: 'save()' },
                        { fqn: 'com.example.RepositoryB#find()', signature: 'find()' }
                    ]
                },
                {
                    fqn: 'com.example.ExternalApi',
                    operations: [
                        { fqn: 'com.example.ExternalApi#call()', signature: 'call()' }
                    ]
                }
            ]
        };
        const set = buildOutboundOperationSet(outboundData);
        assert.strictEqual(set.size, 3);
        assert.ok(set.has('com.example.RepositoryB#save()'));
        assert.ok(set.has('com.example.RepositoryB#find()'));
        assert.ok(set.has('com.example.ExternalApi#call()'));
    });
});

test.describe('buildUsecaseDiagram', () => {
    let buildUsecaseDiagram;

    beforeEach(() => {
        delete require.cache[jigCommonJsPath];
        delete require.cache[jigMermaidDiagramJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];

        global.document = new DocumentStub();
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };

        require(jigCommonJsPath);
        require(jigMermaidDiagramJsPath);
        require(jigJsPath);
        ({ buildUsecaseDiagram } = require(usecaseJsPath));
    });

    test('outboundOperationSetが空の場合、外部ノードは追加されない', () => {
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.RepositoryB#save()']
        };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.nodes.length, 1);
        assert.strictEqual(result.edges.length, 0);
    });

    test('outboundOperationSetに含まれる外部呼び出しはクラスノードとしてexternal:trueで追加される', () => {
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.RepositoryB#save()']
        };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);
        const outboundOperationSet = new Set(['com.example.RepositoryB#save()']);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.nodes.length, 2);
        assert.strictEqual(result.edges.length, 1);
        const externalNode = result.nodes.find(n => n.fqn === 'com.example.RepositoryB');
        assert.ok(externalNode);
        assert.strictEqual(externalNode.kind, 'outbound');
        assert.strictEqual(result.edges[0].to, 'com.example.RepositoryB');
    });

    test('outboundOperationSetに含まれない外部呼び出しは追加されない', () => {
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.OtherService#doWork()', 'com.example.RepositoryB#save()']
        };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);
        const outboundOperationSet = new Set(['com.example.RepositoryB#save()']);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.nodes.length, 2);
        const nodes = result.nodes.map(n => n.fqn);
        assert.ok(nodes.includes('com.example.RepositoryB'));
        assert.ok(!nodes.includes('com.example.OtherService'));
    });

    test('内部ノードはexternal:falseで追加される', () => {
        const otherMethod = {
            fqn: 'com.example.ServiceA#otherMethod()',
            callMethods: [],
            kind: "usecase"
        };
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.ServiceA#otherMethod()'],
            kind: "usecase"
        };
        const methodMap = new Map([
            ['com.example.ServiceA#method1()', rootMethod],
            ['com.example.ServiceA#otherMethod()', otherMethod]
        ]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.nodes.length, 2);
        result.nodes.forEach(n => assert.strictEqual(n.kind, "usecase"));
    });

    test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドはノードとして追加されず呼び出しがインライン化される', () => {
        const rootMethod = { fqn: 'A', callMethods: ['B'], kind: 'usecase' };
        const methodB = { fqn: 'B', callMethods: ['C'], kind: 'method' };
        const methodC = { fqn: 'C', callMethods: [], kind: 'usecase' };
        const methodMap = new Map([['A', rootMethod], ['B', methodB], ['C', methodC]]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        // A と C だけがノードとして残る
        assert.strictEqual(result.nodes.length, 2);
        assert.ok(result.nodes.find(n => n.fqn === 'A'));
        assert.ok(result.nodes.find(n => n.fqn === 'C'));
        // エッジは A -> C になる
        assert.strictEqual(result.edges.length, 1);
        assert.strictEqual(result.edges[0].from, 'A');
        assert.strictEqual(result.edges[0].to, 'C');
    });

    test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドを介した外部呼び出しもインライン化される', () => {
        const rootMethod = { fqn: 'A', callMethods: ['B'], kind: 'usecase' };
        const methodB = { fqn: 'B', callMethods: ['ext#method()'], kind: 'method' };
        const methodMap = new Map([['A', rootMethod], ['B', methodB]]);
        const outboundOperationSet = new Set(['ext#method()']);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.nodes.length, 2);
        assert.ok(result.nodes.find(n => n.fqn === 'ext'));
        assert.strictEqual(result.edges.length, 1);
        assert.strictEqual(result.edges[0].from, 'A');
        assert.strictEqual(result.edges[0].to, 'ext');
    });

    test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドの循環参照があっても無限ループしない', () => {
        const rootMethod = { fqn: 'A', callMethods: ['B'], kind: 'usecase' };
        const methodB = { fqn: 'B', callMethods: ['C'], kind: 'method' };
        const methodC = { fqn: 'C', callMethods: ['B', 'D'], kind: 'method' };
        const methodD = { fqn: 'D', callMethods: [], kind: 'usecase' };
        const methodMap = new Map([['A', rootMethod], ['B', methodB], ['C', methodC], ['D', methodD]]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        assert.strictEqual(result.nodes.length, 2);
        assert.ok(result.nodes.find(n => n.fqn === 'A'));
        assert.ok(result.nodes.find(n => n.fqn === 'D'));
        assert.strictEqual(result.edges.length, 1);
        assert.strictEqual(result.edges[0].from, 'A');
        assert.strictEqual(result.edges[0].to, 'D');
    });

    test('showDiagramOutboundPortsがfalseの場合、外部ポートはノードとして追加されない', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: ['ext.Cls#method()'], kind: 'usecase' };
        const methodMap = new Map([['pkg.Cls#A()', rootMethod]]);
        const outboundOperationSet = new Set(['ext.Cls#method()']);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: false
        });

        assert.strictEqual(result.nodes.length, 1);
        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
        assert.strictEqual(result.edges.length, 0);
    });

    test('直接の呼び出し元(usecase)は caller -> root のエッジで追加される', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase' };
        const callerMethod = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'usecase' };
        const methodMap = new Map([
            ['pkg.Cls#A()', rootMethod],
            ['pkg.Cls#B()', callerMethod]
        ]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
        assert.strictEqual(result.edges.length, 1);
        assert.deepStrictEqual(result.edges[0], {from: 'pkg.Cls#B()', to: 'pkg.Cls#A()'});
    });

    test('showDiagramInternalMethods=falseでは非ユースケース呼び出し元を遡ってユースケース呼び出し元を表示する', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase' };
        const directCaller = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'method' };
        const usecaseCaller = { fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#B()'], kind: 'usecase' };
        const methodMap = new Map([
            ['pkg.Cls#A()', rootMethod],
            ['pkg.Cls#B()', directCaller],
            ['pkg.Cls#C()', usecaseCaller]
        ]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#C()'));
        assert.ok(!result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
        assert.strictEqual(result.edges.length, 1);
        assert.ok(result.edges.find(e => e.from === 'pkg.Cls#C()' && e.to === 'pkg.Cls#A()'));
    });

    test('直接の呼び出し元が非ユースケースの場合、showDiagramInternalMethods=trueでは表示する', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase' };
        const callerMethod = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'method' };
        const methodMap = new Map([
            ['pkg.Cls#A()', rootMethod],
            ['pkg.Cls#B()', callerMethod]
        ]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
        assert.ok(result.edges.find(e => e.from === 'pkg.Cls#B()' && e.to === 'pkg.Cls#A()'));
    });

    test('showDiagramInternalMethods=trueでは呼び出し元は直接のみ表示する', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase' };
        const directCaller = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'usecase' };
        const indirectCaller = { fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#B()'], kind: 'usecase' };
        const methodMap = new Map([
            ['pkg.Cls#A()', rootMethod],
            ['pkg.Cls#B()', directCaller],
            ['pkg.Cls#C()', indirectCaller]
        ]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
        assert.ok(!result.nodes.find(n => n.fqn === 'pkg.Cls#C()'));
        assert.ok(result.edges.find(e => e.from === 'pkg.Cls#B()' && e.to === 'pkg.Cls#A()'));
        assert.ok(!result.edges.find(e => e.from === 'pkg.Cls#C()' && e.to === 'pkg.Cls#B()'));
    });

    test('直接呼び出し元表示を追加しても既存の外部呼び出し可視化は維持される', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: ['ext.Repo#save()'], kind: 'usecase' };
        const callerMethod = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'usecase' };
        const methodMap = new Map([
            ['pkg.Cls#A()', rootMethod],
            ['pkg.Cls#B()', callerMethod]
        ]);
        const outboundOperationSet = new Set(['ext.Repo#save()']);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet,
            showDiagramInternalMethods: true,
            showDiagramOutboundPorts: true
        });

        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
        assert.ok(result.nodes.find(n => n.fqn === 'ext.Repo'));
        assert.ok(result.edges.find(e => e.from === 'pkg.Cls#B()' && e.to === 'pkg.Cls#A()'));
        assert.ok(result.edges.find(e => e.from === 'pkg.Cls#A()' && e.to === 'ext.Repo'));
    });

    test('showDiagramInternalMethods=falseで複数経路から同一ユースケース呼び出し元に到達しても重複しない', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase' };
        const methodB = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'method' };
        const methodC = { fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#A()'], kind: 'method' };
        const usecaseCaller = { fqn: 'pkg.Cls#U()', callMethods: ['pkg.Cls#B()', 'pkg.Cls#C()'], kind: 'usecase' };
        const methodMap = new Map([
            ['pkg.Cls#A()', rootMethod],
            ['pkg.Cls#B()', methodB],
            ['pkg.Cls#C()', methodC],
            ['pkg.Cls#U()', usecaseCaller]
        ]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        const callerEdges = result.edges.filter(e => e.from === 'pkg.Cls#U()' && e.to === 'pkg.Cls#A()');
        assert.strictEqual(callerEdges.length, 1);
    });

    test('showDiagramInternalMethods=falseで逆方向循環があっても無限ループしない', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase' };
        const methodB = { fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()', 'pkg.Cls#C()'], kind: 'method' };
        const methodC = { fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#B()'], kind: 'method' };
        const usecaseCaller = { fqn: 'pkg.Cls#U()', callMethods: ['pkg.Cls#B()'], kind: 'usecase' };
        const methodMap = new Map([
            ['pkg.Cls#A()', rootMethod],
            ['pkg.Cls#B()', methodB],
            ['pkg.Cls#C()', methodC],
            ['pkg.Cls#U()', usecaseCaller]
        ]);

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#U()'));
        assert.ok(result.edges.find(e => e.from === 'pkg.Cls#U()' && e.to === 'pkg.Cls#A()'));
    });

    test('usecaseDataにないinbound側の直接呼び出し元も表示される', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase' };
        const methodMap = new Map([['pkg.Cls#A()', rootMethod]]);
        globalThis.inboundData = {
            controllers: [
                {
                    relations: [
                        {from: 'web.Ctrl#entry()', to: 'pkg.Cls#A()'},
                        {from: 'web.Ctrl#indirect()', to: 'web.Ctrl#entry()'}
                    ]
                }
            ]
        };

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
        assert.ok(result.nodes.find(n => n.fqn === 'web.Ctrl'));
        assert.ok(result.nodes.find(n => n.fqn === 'web.Ctrl').kind === 'inbound-class');
        assert.ok(result.edges.find(e => e.from === 'web.Ctrl' && e.to === 'pkg.Cls#A()'));
    });

    test('同一inboundクラスの複数メソッド呼び出しはクラスノード1つに集約される', () => {
        const rootMethod = { fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase' };
        const methodMap = new Map([['pkg.Cls#A()', rootMethod]]);
        globalThis.inboundData = {
            controllers: [
                {
                    relations: [
                        {from: 'web.Ctrl#entry()', to: 'pkg.Cls#A()'},
                        {from: 'web.Ctrl#entry2()', to: 'pkg.Cls#A()'}
                    ]
                }
            ]
        };

        const result = buildUsecaseDiagram(rootMethod, {
            methodMap,
            outboundOperationSet: new Set(),
            showDiagramInternalMethods: false,
            showDiagramOutboundPorts: true
        });

        const inboundNodes = result.nodes.filter(n => n.fqn === 'web.Ctrl');
        const inboundEdges = result.edges.filter(e => e.from === 'web.Ctrl' && e.to === 'pkg.Cls#A()');
        assert.strictEqual(inboundNodes.length, 1);
        assert.strictEqual(inboundEdges.length, 1);
    });
});
