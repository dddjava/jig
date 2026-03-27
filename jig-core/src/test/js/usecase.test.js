const assert = require('assert');
const { test, beforeEach } = require('node:test');
const path = require('path');
const { DocumentStub, LocalStorageStub, EventStub } = require('./dom-stub.js');

const jigCommonJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/jig-common.js');
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
    let buildSequenceFromCallMethods;
    let buildSequenceDiagramCode;

    beforeEach(() => {
        doc = new DocumentStub();
        global.document = doc;
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.localStorage = new LocalStorageStub();
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };
        // IntersectionObserver は設定しない → lazyRender が即時コールバック

        // チェックボックス要素を事前登録
        ['show-fields', 'show-static-methods', 'show-diagrams', 'show-details', 'show-descriptions', 'show-declarations'].forEach(id => {
            const el = doc.createElement('input');
            el.id = id;
            el.checked = true;
        });
        // コンテナ要素を事前登録
        ['usecase-sidebar-list', 'usecase-list'].forEach(id => {
            const el = doc.createElement('div');
            el.id = id;
        });

        delete require.cache[jigCommonJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];
        require(jigCommonJsPath);
        require(jigJsPath);

        // Mermaid の複雑なDOM操作を回避するためにオーバーライド
        globalThis.Jig.mermaid.renderWithControls = (container, code) => {
            const pre = doc.createElement('pre');
            pre.className = 'mermaid';
            pre.textContent = code;
            container.appendChild(pre);
        };

        ({ UsecaseApp, buildSequenceFromCallMethods, buildSequenceDiagramCode } = require(usecaseJsPath));
    });

    test('init should render data from globalThis.usecaseData', () => {
        globalThis.glossaryData = {
            "com.example.ServiceA": { title: "ServiceA", description: "Description of ServiceA" },
            "com.example.ServiceA#staticMethod1()": { title: "staticMethod1", description: "Description of staticMethod1" },
            "com.example.ServiceA#method1()": { title: "method1", description: "Description of method1" },
            "com.example.ServiceA#otherMethod()": { title: "otherMethod", description: "" }
        };
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
        assert.strictEqual(serviceSection.querySelector('h3 a').id, 'com.example.ServiceA');
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
        assert.strictEqual(methodSection.querySelector('h4').id, 'com.example.ServiceA#method1()');
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
        assert.ok(mermaidPres[0].textContent.includes('graph LR'));
        assert.ok(mermaidPres[1].textContent.includes('sequenceDiagram'));

        const description = methodSection.querySelector('.description');
        assert.strictEqual(description.innerHTML, 'Description of method1');
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
        assert.strictEqual(resolved.href, 'domain.html#com.example.Order');
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
        assert.strictEqual(resolved.href, 'domain.html#com.example.OldClass');
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

    test('initControls should toggle body classes and save to localStorage', () => {
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
        assert.strictEqual(global.localStorage.getItem('jig-usecase-show-fields'), 'false');

        // Toggle static methods
        showStaticMethods.checked = false;
        showStaticMethods.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-static-methods'), true);
        assert.strictEqual(global.localStorage.getItem('jig-usecase-show-static-methods'), 'false');

        // Toggle diagrams
        showDiagrams.checked = false;
        showDiagrams.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-diagrams'), true);
        assert.strictEqual(global.localStorage.getItem('jig-usecase-show-diagrams'), 'false');

        // Toggle details
        showDetails.checked = false;
        showDetails.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-details'), true);
        assert.strictEqual(global.localStorage.getItem('jig-usecase-show-details'), 'false');

        // Toggle descriptions
        showDescriptions.checked = false;
        showDescriptions.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-descriptions'), true);
        assert.strictEqual(global.localStorage.getItem('jig-usecase-show-descriptions'), 'false');

        // Toggle declarations
        showDeclarations.checked = false;
        showDeclarations.dispatchEvent(new window.Event('change'));
        assert.strictEqual(document.body.classList.contains('hide-usecase-declarations'), true);
        assert.strictEqual(global.localStorage.getItem('jig-usecase-show-declarations'), 'false');
    });
});

test.describe('buildSequenceFromCallMethods', () => {
    let buildSequenceFromCallMethods;

    beforeEach(() => {
        delete require.cache[jigCommonJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];

        global.document = new DocumentStub();
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.localStorage = new LocalStorageStub();
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };

        require(jigCommonJsPath);
        require(jigJsPath);
        ({ buildSequenceFromCallMethods } = require(usecaseJsPath));
    });

    test('callMethodsが空の場合はcallsが空', () => {
        const rootMethod = { fqn: 'com.example.ServiceA#method1()', callMethods: [] };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

        const result = buildSequenceFromCallMethods(rootMethod, methodMap);

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

        const result = buildSequenceFromCallMethods(rootMethod, methodMap);

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

        const result = buildSequenceFromCallMethods(rootMethod, methodMap, outboundOperationSet);

        assert.strictEqual(result.participants.length, 2);
        assert.strictEqual(result.participants[1].label, 'RepositoryB');
        assert.strictEqual(result.participants[1].kind, "external");
        assert.strictEqual(result.calls.length, 1);
        assert.strictEqual(result.calls[0].label, 'save');
    });

    test('outboundOperationSetにない外部呼び出しは無視される', () => {
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.RepositoryB#save()']
        };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

        const result = buildSequenceFromCallMethods(rootMethod, methodMap);  // outboundOperationSet省略=空

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

        const result = buildSequenceFromCallMethods(rootMethod, methodMap, outboundOperationSet);

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

        const result = buildSequenceFromCallMethods(rootMethod, methodMap);

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

        const result = buildSequenceFromCallMethods(methodA, methodMap);

        assert.strictEqual(result.participants.length, 2);
        // methodA->methodB と methodB->methodA の2呼び出し
        assert.strictEqual(result.calls.length, 2);
    });
});

test.describe('buildSequenceDiagramCode', () => {
    let buildSequenceDiagramCode;

    beforeEach(() => {
        delete require.cache[jigCommonJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];

        global.document = new DocumentStub();
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.localStorage = new LocalStorageStub();
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };

        require(jigCommonJsPath);
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
                {id: 'node-b', label: 'ClassB', kind: "external"}
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
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];

        global.document = new DocumentStub();
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.localStorage = new LocalStorageStub();
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };

        require(jigCommonJsPath);
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

test.describe('buildGraphFromCallMethods', () => {
    let buildGraphFromCallMethods;

    beforeEach(() => {
        delete require.cache[jigCommonJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];

        global.document = new DocumentStub();
        global.window = { addEventListener: () => {}, Event: EventStub };
        global.localStorage = new LocalStorageStub();
        global.marked = { parse: (text) => text };
        global.mermaid = { initialize: () => {}, run: () => {} };

        require(jigCommonJsPath);
        require(jigJsPath);
        ({ buildGraphFromCallMethods } = require(usecaseJsPath));
    });

    test('outboundOperationSetが空の場合、外部ノードは追加されない', () => {
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.RepositoryB#save()']
        };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

        const result = buildGraphFromCallMethods(rootMethod, methodMap);

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

        const result = buildGraphFromCallMethods(rootMethod, methodMap, outboundOperationSet);

        assert.strictEqual(result.nodes.length, 2);
        assert.strictEqual(result.edges.length, 1);
        const externalNode = result.nodes.find(n => n.fqn === 'com.example.RepositoryB');
        assert.ok(externalNode);
        assert.strictEqual(externalNode.kind, "external");
        assert.strictEqual(result.edges[0].to, 'com.example.RepositoryB');
    });

    test('outboundOperationSetに含まれない外部呼び出しは追加されない', () => {
        const rootMethod = {
            fqn: 'com.example.ServiceA#method1()',
            callMethods: ['com.example.OtherService#doWork()', 'com.example.RepositoryB#save()']
        };
        const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);
        const outboundOperationSet = new Set(['com.example.RepositoryB#save()']);

        const result = buildGraphFromCallMethods(rootMethod, methodMap, outboundOperationSet);

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

        const result = buildGraphFromCallMethods(rootMethod, methodMap);

        assert.strictEqual(result.nodes.length, 2);
        result.nodes.forEach(n => assert.strictEqual(n.kind, "usecase"));
    });
});
