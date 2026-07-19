const test = require('node:test');
const assert = require('node:assert/strict');
const {DocumentStub, EventStub, setGlossaryData} = require('./dom-stub.js');

// モジュールを事前にロード
require('../../main/resources/templates/assets/jig-util.js');
require('../../main/resources/templates/assets/jig-data.js');
require('../../main/resources/templates/assets/jig-glossary.js');
require('../../main/resources/templates/assets/jig-mermaid.js');
require('../../main/resources/templates/assets/jig-dom.js');
require('../../main/resources/templates/assets/jig-bootstrap.js');
const UsecaseApp = require('../../main/resources/templates/assets/usecase.js');

// ネストしたdescribeブロック内での require() 呼び出しに使用
// （キャッシュされたモジュールが返されるため、require.cache削除は不要）
const jigUtilJsPath = '../../main/resources/templates/assets/jig-util.js';
const jigCommonJsPath = '../../main/resources/templates/assets/jig-glossary.js';
const jigMermaidDiagramJsPath = '../../main/resources/templates/assets/jig-mermaid.js';
const jigJsPath = '../../main/resources/templates/assets/jig-dom.js';
const usecaseJsPath = '../../main/resources/templates/assets/usecase.js';

// モック用のデータ
const mockUsecaseAppData = {
    usecases: [
        {
            fqn: "com.example.ServiceA",
            fields: [
                {name: "field1", typeRef: {fqn: "java.lang.String"}, isDeprecated: false}
            ],
            staticMethods: [
                {
                    fqn: "com.example.ServiceA#staticMethod1()",
                    visibility: "PUBLIC",
                    declaration: "staticMethod1():void",
                    parameters: [],
                    returnTypeRef: {fqn: "void"}
                }
            ],
            methods: [
                {
                    fqn: "com.example.ServiceA#method1()",
                    visibility: "PUBLIC",
                    parameters: [],
                    returnTypeRef: {fqn: "void"},
                    declaration: "method1():void",
                    isDeprecated: false,
                    callMethods: ["com.example.ServiceA#otherMethod()"]
                },
                {
                    fqn: "com.example.ServiceA#otherMethod()",
                    visibility: "PUBLIC",
                    parameters: [],
                    returnTypeRef: {fqn: "void"},
                    declaration: "otherMethod():void",
                    isDeprecated: false,
                    callMethods: []
                }
            ]
        }
    ]
};

test.describe('usecase.js', () => {

    test.describe('UsecaseApp', () => {
        let doc;

        test.beforeEach(() => {
            doc = new DocumentStub();
            doc.body.classList.add("usecase-model");
            global.document = doc;
            global.window = {
                addEventListener: () => {
                }, Event: EventStub
            };
            global.marked = {parse: (text) => text};
            global.DOMPurify = {sanitize: (html) => html};
            global.mermaid = {
                initialize: () => {
                }, run: () => {
                }
            };

            // グローバルデータをクリア（テスト間での汚染防止）
            delete globalThis.inboundData;
            delete globalThis.domainData;
            delete globalThis.usecaseData;
            delete globalThis.outboundData;
            // IntersectionObserver は設定しない → lazyRender が即時コールバック

            // チェックボックス要素を事前登録
            ['show-members', 'show-diagrams', 'show-details', 'show-descriptions', 'show-declarations', 'show-diagram-callers', 'show-diagram-callees', 'show-diagram-internal-methods', 'show-diagram-inbound-classes', 'show-diagram-outbound-ports', 'show-diagram-arguments'].forEach(id => {
                const el = doc.createElement('input');
                el.id = id;
                el.checked = true;
            });
            // show-diagram-domain-types はデフォルト未チェック
            const domainTypesCheckbox = doc.createElement('input');
            domainTypesCheckbox.id = 'show-diagram-domain-types';
            domainTypesCheckbox.checked = false;
            // 表示対象ラジオボタン（デフォルトはすべて表示）
            const radioAll = doc.createElement('input');
            radioAll.id = 'display-target-all';
            radioAll.type = 'radio';
            radioAll.name = 'display-target';
            radioAll.checked = true;
            const radioHandlersOnly = doc.createElement('input');
            radioHandlersOnly.id = 'display-target-handlers-only';
            radioHandlersOnly.type = 'radio';
            radioHandlersOnly.name = 'display-target';
            radioHandlersOnly.checked = false;
            // コンテナ要素を事前登録
            ['usecase-sidebar-list', 'usecase-list'].forEach(id => {
                const el = doc.createElement('div');
                el.id = id;
            });

            // Mermaid の複雑なDOM操作を回避するためにオーバーライド
            globalThis.Jig.mermaid.renderWithControls = (container, source, {direction = 'LR'} = {}) => {
                const code = (typeof source === 'function') ? source(direction) : source;
                const pre = doc.createElement('pre');
                pre.className = 'mermaid';
                pre.textContent = code;
                container.appendChild(pre);
            };
        });

        test('init should render data from globalThis.usecaseData', () => {
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA", description: "Description of ServiceA"},
                "com.example.ServiceA#staticMethod1()": {
                    title: "staticMethod1",
                    description: "Description of staticMethod1"
                },
                "com.example.ServiceA#method1()": {title: "method1", description: "Description of method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod", description: ""}
            });
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const sidebar = document.getElementById('usecase-sidebar-list');
            // グループが「ユースケース」の1つしかないため見出しは表示せず、
            // パッケージ階層のulを直接並べる
            assert.equal(sidebar.children.length, 1);
            assert.equal(sidebar.children[0].tagName, 'ul');
            assert.equal(sidebar.querySelectorAll('.in-page-sidebar__section--group').length, 0);
            assert.equal(sidebar.querySelectorAll('.in-page-sidebar__title--group').length, 0);
            const sidebarLinks = sidebar.querySelectorAll('a');
            // 用語のないパッケージは省略され、メインのパッケージ見出しへのリンクになる
            assert.equal(sidebarLinks[0].textContent, 'example');
            assert.equal(sidebarLinks[0].getAttribute('href'), '#' + globalThis.Jig.util.fqnToId('package', 'com.example'));
            assert.equal(sidebarLinks[1].textContent, 'ServiceA');
            assert.ok(sidebarLinks[2].classList.contains('in-page-sidebar__link--sub'));
            assert.equal(sidebarLinks[2].textContent, 'method1');
            assert.equal(sidebarLinks[3].textContent, 'otherMethod');

            const mainList = document.getElementById('usecase-list');
            assert.equal(mainList.children.length, 2, 'パッケージ見出しとユースケースカードが並ぶ');
            const packageHeading = mainList.children[0];
            assert.equal(packageHeading.className, 'package-heading');
            assert.equal(packageHeading.id, globalThis.Jig.util.fqnToId('package', 'com.example'));
            assert.equal(packageHeading.querySelector('h2').textContent, 'example');
            assert.equal(packageHeading.querySelector('.fully-qualified-name').textContent, 'com.example');
            const serviceSection = document.getElementById(globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));
            assert.equal(serviceSection.querySelector('h3 span').textContent, 'ServiceA');
            assert.equal(serviceSection.querySelector('.fully-qualified-name').textContent, 'com.example.ServiceA');
            assert.equal(serviceSection.querySelector('.markdown').innerHTML, 'Description of ServiceA');

            const fieldsSection = serviceSection.querySelector('section.methods-section');
            assert.ok(fieldsSection);
            assert.equal(fieldsSection.querySelector('.field-name').textContent, 'field1');

            const staticMethodsSection = serviceSection.querySelector('section.static-methods');
            assert.ok(staticMethodsSection);
            assert.equal(staticMethodsSection.querySelector('.method-name').textContent, 'staticMethod1');

            const methodSection = serviceSection.querySelector('article.jig-card--item');
            assert.ok(methodSection);
            assert.equal(methodSection.id, globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#method1()'));
            assert.equal(methodSection.querySelector('h4').textContent, 'method1');
            assert.equal(methodSection.querySelector('.declaration').textContent, 'ServiceA#method1()');

            const diagramContainer = methodSection.querySelector('.tab-diagram-section');
            assert.ok(diagramContainer);
            const tabs = diagramContainer.querySelector('.jig-tabs');
            assert.ok(tabs);
            assert.equal(tabs.children.length, 2);
            assert.equal(tabs.children[0].textContent, 'ユースケース図');
            assert.equal(tabs.children[1].textContent, 'シーケンス図');

            const mermaidPres = methodSection.querySelectorAll('.mermaid');
            // renderWithControls が呼ばれる際に .mermaid 要素が生成される可能性があるため、
            // 最低でも 2 個（ユースケース図とシーケンス図）以上の要素が存在すること
            assert.ok(mermaidPres.length >= 2, `.mermaid 要素が最低2個存在すること（実際: ${mermaidPres.length}）`);

            // テキストコンテンツを持つ .mermaid 要素を探す
            const usecaseMermaid = Array.from(mermaidPres).find(el => el.textContent.includes('graph LR'));
            const sequenceMermaid = Array.from(mermaidPres).find(el => el.textContent.includes('sequenceDiagram'));

            assert.ok(usecaseMermaid, 'ユースケース図（graph LR）が存在すること');
            assert.ok(sequenceMermaid, 'シーケンス図（sequenceDiagram）が存在すること');

            const graphCode = usecaseMermaid.textContent;
            assert.ok(graphCode.includes('subgraph'), 'ユースケース図にsubgraphが含まれること');
            assert.ok(graphCode.includes('ServiceA'), 'subgraphにクラス名が含まれること');
            assert.ok(graphCode.includes('direction LR'), 'subgraphにdirection LRが含まれること');
            assert.ok(graphCode.includes('classDef'), 'Theme classDefが含まれるべき');

            const description = methodSection.querySelector('.description');
            assert.equal(description.querySelector('.markdown')?.innerHTML, 'Description of method1');
        });

        test('パッケージのdescriptionが存在する場合、パッケージ見出しに説明が表示される', () => {
            setGlossaryData({
                "com.example": {title: "サンプル", description: "サンプルパッケージの説明"},
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const mainList = document.getElementById('usecase-list');
            const packageHeading = mainList.children[0];
            assert.equal(packageHeading.querySelector('h2').textContent, 'サンプル');
            const pkgDescription = packageHeading.querySelector('.description .markdown');
            assert.ok(pkgDescription, 'パッケージの説明が表示されること');
            assert.equal(pkgDescription.innerHTML, 'サンプルパッケージの説明');
        });

        // issue #1141: クラスを直接含まないパッケージのpackage-infoが表示されない
        test('用語を持つがクラスを直接含まない中間パッケージにも見出しと説明が表示される', () => {
            const nestedUsecaseData = {
                usecases: [{
                    fqn: "com.example.sub.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [{
                        fqn: "com.example.sub.ServiceA#method1()",
                        visibility: "PUBLIC",
                        parameters: [],
                        returnTypeRef: {fqn: "void"},
                        declaration: "method1():void",
                        isDeprecated: false,
                        callMethods: []
                    }]
                }]
            };
            setGlossaryData({
                "com.example": {title: "サンプル", description: "中間パッケージの説明"},
                "com.example.sub.ServiceA": {title: "ServiceA"},
                "com.example.sub.ServiceA#method1()": {title: "method1"}
            });
            globalThis.usecaseData = nestedUsecaseData;
            UsecaseApp.init();

            const mainList = document.getElementById('usecase-list');
            assert.equal(mainList.children.length, 3, '中間パッケージ見出し・直属パッケージ見出し・カードが並ぶ');

            const intermediateHeading = mainList.children[0];
            assert.equal(intermediateHeading.id, globalThis.Jig.util.fqnToId('package', 'com.example'));
            assert.equal(intermediateHeading.querySelector('h2').textContent, 'サンプル');
            assert.equal(intermediateHeading.querySelector('.description .markdown').innerHTML, '中間パッケージの説明');

            const leafHeading = mainList.children[1];
            assert.equal(leafHeading.id, globalThis.Jig.util.fqnToId('package', 'com.example.sub'));

            // サイドバーの中間パッケージノードはメインの見出しへのリンクになる
            const sidebar = document.getElementById('usecase-sidebar-list');
            const intermediateLink = sidebar.querySelectorAll('a')
                .find(a => a.textContent === 'サンプル');
            assert.ok(intermediateLink, '中間パッケージがリンクとして表示される');
            assert.equal(intermediateLink.getAttribute('href'), '#' + globalThis.Jig.util.fqnToId('package', 'com.example'));
        });

        test('用語のない中間パッケージにはセクションが生成されない', () => {
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const mainList = document.getElementById('usecase-list');
            assert.equal(mainList.children.length, 2, '直属パッケージの見出しとカードのみ');
            assert.equal(mainList.children[0].id, globalThis.Jig.util.fqnToId('package', 'com.example'));
        });

        test('ハンドラのみ表示で子孫が全て非表示なら中間パッケージの見出しも表示されない', () => {
            // inboundDataなし = ハンドラなし
            setGlossaryData({
                "com.example": {title: "サンプル", description: "中間パッケージの説明"},
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;

            document.getElementById('display-target-handlers-only').checked = true;
            document.getElementById('display-target-all').checked = false;

            UsecaseApp.init();

            const mainList = document.getElementById('usecase-list');
            assert.equal(mainList.children.length, 0, '孤児の見出しが生成されない');
        });

        test('パッケージのdescriptionが空の場合、説明セクションは表示されない', () => {
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const mainList = document.getElementById('usecase-list');
            const packageHeading = mainList.children[0];
            assert.ok(!packageHeading.querySelector('.description'), 'descriptionセクションが存在しないこと');
        });

        test.describe('サイドバーテキストフィルター', () => {
            const filterTestData = {
                usecases: [
                    {
                        fqn: "com.example.pkga.ServiceA",
                        fields: [],
                        staticMethods: [],
                        methods: [
                            {
                                fqn: "com.example.pkga.ServiceA#method1()",
                                visibility: "PUBLIC",
                                parameters: [],
                                returnTypeRef: {fqn: "void"},
                                declaration: "method1():void",
                                isDeprecated: false,
                                callMethods: []
                            }
                        ]
                    },
                    {
                        fqn: "com.example.pkgb.ServiceB",
                        fields: [],
                        staticMethods: [],
                        methods: [
                            {
                                fqn: "com.example.pkgb.ServiceB#method2()",
                                visibility: "PUBLIC",
                                parameters: [],
                                returnTypeRef: {fqn: "void"},
                                declaration: "method2():void",
                                isDeprecated: false,
                                callMethods: []
                            }
                        ]
                    }
                ]
            };

            function setupFilterTest() {
                setGlossaryData({
                    "com.example.pkga": {title: "アルファ機能"},
                    "com.example.pkga.ServiceA": {title: "ServiceA"},
                    "com.example.pkga.ServiceA#method1()": {title: "method1"},
                    "com.example.pkgb": {title: "ベータ機能"},
                    "com.example.pkgb.ServiceB": {title: "ServiceB"},
                    "com.example.pkgb.ServiceB#method2()": {title: "method2"},
                });
                globalThis.usecaseData = filterTestData;

                const filterInput = document.createElement('input');
                filterInput.id = 'usecase-sidebar-filter';

                UsecaseApp.init();
                return filterInput;
            }

            test('所属パッケージ名で一致する場合、そのクラスの全メソッドがサイドバーに表示される', () => {
                const filterInput = setupFilterTest();

                filterInput.value = 'アルファ';
                filterInput.dispatchEvent({type: 'input'});

                const sidebar = document.getElementById('usecase-sidebar-list');
                const linkTexts = [...sidebar.querySelectorAll('a')].map(a => a.textContent);
                assert.ok(linkTexts.includes('ServiceA'), 'アルファ機能配下のServiceAが表示されること');
                assert.ok(linkTexts.includes('method1'), 'ServiceAのメソッドが表示されること');
                assert.ok(!linkTexts.includes('ServiceB'), 'ベータ機能配下のServiceBは表示されないこと');
            });

            test('パッケージ名に一致しない場合はクラス名・メソッド名の一致判定にフォールバックする', () => {
                const filterInput = setupFilterTest();

                filterInput.value = 'method2';
                filterInput.dispatchEvent({type: 'input'});

                const sidebar = document.getElementById('usecase-sidebar-list');
                const linkTexts = [...sidebar.querySelectorAll('a')].map(a => a.textContent);
                assert.ok(linkTexts.includes('ServiceB'), 'method2を持つServiceBが表示されること');
                assert.ok(!linkTexts.includes('ServiceA'), 'method2を持たないServiceAは表示されないこと');
            });
        });

        test('クラス単位の図では内部メソッド（非PUBLIC）は表示されない', () => {
            const usecaseDataWithInternal = {
                usecases: [{
                    fqn: "com.example.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [
                        {
                            fqn: "com.example.ServiceA#publicMethod()",
                            visibility: "PUBLIC",
                            parameters: [],
                            returnTypeRef: {fqn: "void"},
                            declaration: "publicMethod():void",
                            isDeprecated: false,
                            callMethods: ["com.example.ServiceA#internalHelper()"]
                        },
                        {
                            fqn: "com.example.ServiceA#internalHelper()",
                            visibility: "PRIVATE",
                            parameters: [],
                            returnTypeRef: {fqn: "void"},
                            declaration: "internalHelper():void",
                            isDeprecated: false,
                            callMethods: []
                        }
                    ]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#publicMethod()": {title: "publicMethod"},
                "com.example.ServiceA#internalHelper()": {title: "internalHelper"}
            });
            globalThis.usecaseData = usecaseDataWithInternal;
            UsecaseApp.init();

            const serviceSection = document.querySelector('#' + globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));
            const classDiagram = serviceSection.querySelector('.diagram-container.class-diagram');
            assert.ok(!classDiagram, '内部メソッドのみへのエッジがある場合、クラス図は生成されない');
        });

        // 図の有無判定はトグル状態に依存しない。現在のトグル状態で判定すると、OFFのトグルでしか
        // 関連が生まれない図はコンテナごと生成されず、後からONにしても出現できない
        test('ドメインモデルとの関連しかないクラスでも、表示設定OFFのままクラス図コンテナが生成される', () => {
            // show-diagram-domain-types はデフォルトOFF
            globalThis.domainData = {
                types: [{fqn: 'com.example.Order', isDeprecated: false}]
            };
            const usecaseDataDomainOnly = {
                usecases: [{
                    fqn: "com.example.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [{
                        fqn: "com.example.ServiceA#findOrder()",
                        visibility: "PUBLIC",
                        parameters: [],
                        returnTypeRef: {fqn: "com.example.Order"},
                        declaration: "findOrder():Order",
                        isDeprecated: false,
                        callMethods: []
                    }]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#findOrder()": {title: "findOrder"},
                "com.example.Order": {title: "Order"}
            });
            globalThis.usecaseData = usecaseDataDomainOnly;
            UsecaseApp.init();

            const serviceSection = document.getElementById(globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));
            const classDiagram = serviceSection.querySelector('.diagram-container.class-diagram');
            assert.ok(classDiagram, 'ドメインモデルOFFでもクラス図コンテナが生成されること');
            const code = classDiagram.querySelector('.mermaid').textContent;
            const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Order');
            assert.ok(!code.includes(orderNodeId), '描画自体は現在のトグル状態に従い、ドメインノードは含まれないこと');

            delete globalThis.domainData;
        });

        test('クラス単位の図がクラスヘッダー直下にレンダリングされる', () => {
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const serviceSection = document.querySelector('#' + globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));

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
            // クラス単位の図はクラス自身がsubgraphの枠になる
            const classSubgraphId = Jig.util.fqnToId("sg", 'com.example.ServiceA');
            assert.ok(code.includes(`subgraph ${classSubgraphId}`));
        });

        test('show-diagram-domain-typesがONの場合、クラス単位の図にドメインモデルノードが表示される', () => {
            document.getElementById('show-diagram-domain-types').checked = true;
            globalThis.domainData = {
                types: [{fqn: 'com.example.Order', isDeprecated: false}]
            };
            const usecaseDataWithDomain = {
                usecases: [{
                    fqn: "com.example.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [
                        {
                            fqn: "com.example.ServiceA#method1(Order)",
                            visibility: "PUBLIC",
                            parameters: [{name: "arg0", nameSource: "POSITIONAL", typeRef: {fqn: "com.example.Order"}}],
                            returnTypeRef: {fqn: "void"},
                            declaration: "method1(Order):void",
                            isDeprecated: false,
                            callMethods: []
                        },
                        {
                            fqn: "com.example.ServiceA#findOrder()",
                            visibility: "PUBLIC",
                            parameters: [],
                            returnTypeRef: {fqn: "com.example.Order"},
                            declaration: "findOrder():Order",
                            isDeprecated: false,
                            callMethods: []
                        }
                    ]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1(Order)": {title: "method1"},
                "com.example.ServiceA#findOrder()": {title: "findOrder"},
                "com.example.Order": {title: "Order"}
            });
            globalThis.usecaseData = usecaseDataWithDomain;
            UsecaseApp.init();

            const serviceSection = document.querySelector('#' + globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));
            const classDiagram = serviceSection.querySelector('.diagram-container.class-diagram');
            assert.ok(classDiagram, 'クラス図が生成されること');

            const mermaidPre = classDiagram.querySelector('.mermaid');
            assert.ok(mermaidPre);
            const code = mermaidPre.textContent;

            const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Order');
            const method1NodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#method1(Order)');
            const findOrderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#findOrder()');
            assert.ok(code.includes(orderNodeId), 'ドメインモデルのノードが含まれること');
            assert.ok(code.includes(`${orderNodeId} -.-> ${method1NodeId}`), '引数→メソッドのエッジが破線で含まれること');
            assert.ok(code.includes(`${findOrderNodeId} -.-> ${orderNodeId}`), 'メソッド→戻り値のエッジが破線で含まれること');
            assert.ok(code.includes('./domain.html#' + globalThis.Jig.util.fqnToId("domain", 'com.example.Order')), 'domain.htmlへのリンクが含まれること');
            assert.ok(code.includes('"com.example.Order"'), 'domain-typeノードにFQNのツールチップが含まれること');

            delete globalThis.domainData;
        });

        test('戻り値の型がコレクション型の場合、typeArgumentRefs内のドメイン型がノードに追加される', () => {
            document.getElementById('show-diagram-domain-types').checked = true;
            globalThis.domainData = {
                types: [{fqn: 'com.example.Order', isDeprecated: false}]
            };
            const usecaseDataWithGenerics = {
                usecases: [{
                    fqn: "com.example.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [
                        {
                            fqn: "com.example.ServiceA#findOrders()",
                            visibility: "PUBLIC",
                            parameters: [],
                            returnTypeRef: {
                                fqn: "java.util.List",
                                typeArgumentRefs: [{fqn: "com.example.Order"}]
                            },
                            declaration: "findOrders():List<Order>",
                            isDeprecated: false,
                            callMethods: []
                        }
                    ]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#findOrders()": {title: "findOrders"},
                "com.example.Order": {title: "Order"}
            });
            globalThis.usecaseData = usecaseDataWithGenerics;
            UsecaseApp.init();

            const serviceSection = document.getElementById(globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));
            const classDiagram = serviceSection.querySelector('.diagram-container.class-diagram');
            assert.ok(classDiagram, 'クラス図が生成されること');

            const mermaidPre = classDiagram.querySelector('.mermaid');
            const code = mermaidPre.textContent;

            const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Order');
            const findOrdersNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#findOrders()');
            assert.ok(code.includes(orderNodeId), 'typeArgumentRefsのドメイン型がノードとして含まれること');
            assert.ok(code.includes(`${findOrdersNodeId} -.-> ${orderNodeId}`), 'メソッド→ドメイン型のエッジが破線で含まれること');

            delete globalThis.domainData;
        });

        test('引数の型がコレクション型の場合、typeArgumentRefs内のドメイン型がノードに追加される', () => {
            document.getElementById('show-diagram-domain-types').checked = true;
            globalThis.domainData = {
                types: [{fqn: 'com.example.Order', isDeprecated: false}]
            };
            const usecaseDataWithGenerics = {
                usecases: [{
                    fqn: "com.example.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [
                        {
                            fqn: "com.example.ServiceA#saveOrders()",
                            visibility: "PUBLIC",
                            parameters: [{name: "arg0", nameSource: "POSITIONAL", typeRef: {
                                fqn: "java.util.List",
                                typeArgumentRefs: [{fqn: "com.example.Order"}]
                            }}],
                            returnTypeRef: {fqn: "void"},
                            declaration: "saveOrders(List<Order>):void",
                            isDeprecated: false,
                            callMethods: []
                        }
                    ]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#saveOrders()": {title: "saveOrders"},
                "com.example.Order": {title: "Order"}
            });
            globalThis.usecaseData = usecaseDataWithGenerics;
            UsecaseApp.init();

            const serviceSection = document.querySelector('#' + globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));
            const classDiagram = serviceSection.querySelector('.diagram-container.class-diagram');
            assert.ok(classDiagram, 'クラス図が生成されること');

            const mermaidPre = classDiagram.querySelector('.mermaid');
            const code = mermaidPre.textContent;

            const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Order');
            const saveOrdersNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#saveOrders()');
            assert.ok(code.includes(orderNodeId), 'typeArgumentRefsのドメイン型がノードとして含まれること');
            assert.ok(code.includes(`${orderNodeId} -.-> ${saveOrdersNodeId}`), 'ドメイン型→メソッドのエッジが破線で含まれること');

            delete globalThis.domainData;
        });

        test('クラス単位の図にinboundクラスのノードとエッジが追加される', () => {
            globalThis.inboundData = {
                inboundAdapters: [{
                    relations: [
                        {from: 'web.OrderCtrl#create()', to: 'com.example.ServiceA#method1()'},
                        {from: 'web.OrderCtrl#list()', to: 'com.example.ServiceA#otherMethod()'}
                    ]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"},
                "web.OrderCtrl": {title: "OrderCtrl"}
            });
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const serviceSection = document.querySelector('#' + globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));
            const classDiagram = serviceSection.querySelector('.diagram-container.class-diagram');
            assert.ok(classDiagram, 'クラス図が生成されること');

            const mermaidPre = classDiagram.querySelector('.mermaid');
            assert.ok(mermaidPre);
            const code = mermaidPre.textContent;

            const ctrlNodeId = globalThis.Jig.util.fqnToId("node", 'web.OrderCtrl');
            const method1NodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#method1()');
            const otherMethodNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#otherMethod()');
            assert.ok(code.includes(ctrlNodeId), 'inboundクラスのノードが含まれること');
            assert.ok(code.includes(`${ctrlNodeId} --> ${method1NodeId}`), 'inbound→method1のエッジが含まれること');
            assert.ok(code.includes(`${ctrlNodeId} --> ${otherMethodNodeId}`), 'inbound→otherMethodのエッジが含まれること');
            assert.ok(code.includes('./inbound.html#' + globalThis.Jig.util.fqnToId("adapter", 'web.OrderCtrl')), 'inbound.htmlへのリンクが含まれること');
            assert.ok(code.includes('"web.OrderCtrl"'), 'inbound-classノードにFQNのツールチップが含まれること');
        });

        test.describe('パッケージ単位のユースケース図', () => {
            const packageDiagramData = {
                usecases: [
                    {
                        fqn: "com.example.ServiceA",
                        fields: [],
                        staticMethods: [],
                        methods: [{
                            fqn: "com.example.ServiceA#method1()",
                            visibility: "PUBLIC",
                            parameters: [],
                            returnTypeRef: {fqn: "void"},
                            declaration: "method1():void",
                            isDeprecated: false,
                            callMethods: ["com.example.ServiceB#action()", "com.example.Repo#save()"]
                        }]
                    },
                    {
                        fqn: "com.example.ServiceB",
                        fields: [],
                        staticMethods: [],
                        methods: [{
                            fqn: "com.example.ServiceB#action()",
                            visibility: "PUBLIC",
                            parameters: [],
                            returnTypeRef: {fqn: "void"},
                            declaration: "action():void",
                            isDeprecated: false,
                            callMethods: []
                        }]
                    }
                ]
            };

            function setupPackageDiagramGlossary() {
                setGlossaryData({
                    "com.example.ServiceA": {title: "ServiceA"},
                    "com.example.ServiceA#method1()": {title: "method1"},
                    "com.example.ServiceB": {title: "ServiceB"},
                    "com.example.ServiceB#action()": {title: "action"},
                    "web.OrderCtrl": {title: "OrderCtrl"},
                    "com.example.Repo": {title: "Repo"}
                });
            }

            function getPackageDiagramCode() {
                const heading = document.getElementById(globalThis.Jig.util.fqnToId('package', 'com.example'));
                const diagram = heading?.querySelector('.package-diagram');
                return diagram?.querySelector('.mermaid')?.textContent ?? null;
            }

            test('パッケージ内のクラス間呼び出しがユースケースノードとエッジで表示される', () => {
                setupPackageDiagramGlossary();
                globalThis.usecaseData = packageDiagramData;
                UsecaseApp.init();

                const code = getPackageDiagramCode();
                assert.ok(code, 'パッケージ見出しにパッケージ図が生成されること');

                const serviceANodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA');
                const serviceBNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceB');
                assert.ok(code.includes(`${serviceANodeId}(["ServiceA"])`), 'クラスがユースケースの形状で表示されること');
                assert.ok(code.includes(`${serviceANodeId} --> ${serviceBNodeId}`), 'クラス間のエッジが含まれること');
                assert.ok(code.includes(`class ${serviceANodeId} usecase`), 'クラスノードにusecaseスタイルが付与されること');
                assert.ok(code.includes('#' + globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA')), 'クラスカードへのリンクが含まれること');
                const repoNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Repo');
                assert.ok(!code.includes(repoNodeId), 'outboundOperationSetにないパッケージ外クラスは表示されないこと');
            });

            test('クラス間の関連がないパッケージには図が生成されない', () => {
                setGlossaryData({
                    "com.example.ServiceA": {title: "ServiceA"},
                    "com.example.ServiceA#method1()": {title: "method1"},
                    "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
                });
                globalThis.usecaseData = mockUsecaseAppData;
                UsecaseApp.init();

                const heading = document.getElementById(globalThis.Jig.util.fqnToId('package', 'com.example'));
                assert.ok(heading, 'パッケージ見出しは生成されること');
                assert.ok(!heading.querySelector('.package-diagram'), 'クラス内呼び出しのみではパッケージ図が生成されないこと');
            });

            test('inboundクラスのノードとエッジが追加される', () => {
                globalThis.inboundData = {
                    inboundAdapters: [{
                        relations: [
                            {from: 'web.OrderCtrl#create()', to: 'com.example.ServiceB#action()'}
                        ]
                    }]
                };
                setupPackageDiagramGlossary();
                globalThis.usecaseData = packageDiagramData;
                UsecaseApp.init();

                const code = getPackageDiagramCode();
                assert.ok(code, 'パッケージ図が生成されること');

                const ctrlNodeId = globalThis.Jig.util.fqnToId("node", 'web.OrderCtrl');
                const serviceBNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceB');
                assert.ok(code.includes(`${ctrlNodeId} --> ${serviceBNodeId}`), 'inboundクラス→クラスのエッジが含まれること');
                assert.ok(code.includes('./inbound.html#' + globalThis.Jig.util.fqnToId("adapter", 'web.OrderCtrl')), 'inbound.htmlへのリンクが含まれること');
            });

            test('outboundクラスのノードとエッジが追加される', () => {
                globalThis.outboundData = {
                    outboundPorts: [{
                        fqn: 'com.example.Repo',
                        operations: [{fqn: 'com.example.Repo#save()'}]
                    }]
                };
                setupPackageDiagramGlossary();
                globalThis.usecaseData = packageDiagramData;
                UsecaseApp.init();

                const code = getPackageDiagramCode();
                assert.ok(code, 'パッケージ図が生成されること');

                const serviceANodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA');
                const repoNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Repo');
                assert.ok(code.includes(`${serviceANodeId} --> ${repoNodeId}`), 'クラス→outboundクラスのエッジが含まれること');
                assert.ok(code.includes('./outbound.html#' + globalThis.Jig.util.fqnToId("port", 'com.example.Repo')), 'outbound.htmlへのリンクが含まれること');
            });

            test('show-diagram-domain-typesがONの場合、公開メソッドの入出力ドメインモデルが表示される', () => {
                document.getElementById('show-diagram-domain-types').checked = true;
                globalThis.domainData = {
                    types: [{fqn: 'com.example.domain.Order', isDeprecated: false}]
                };
                const dataWithDomain = {
                    usecases: [
                        packageDiagramData.usecases[0],
                        {
                            fqn: "com.example.ServiceB",
                            fields: [],
                            staticMethods: [],
                            methods: [{
                                fqn: "com.example.ServiceB#action()",
                                visibility: "PUBLIC",
                                parameters: [],
                                returnTypeRef: {fqn: "com.example.domain.Order"},
                                declaration: "action():Order",
                                isDeprecated: false,
                                callMethods: []
                            }]
                        }
                    ]
                };
                setupPackageDiagramGlossary();
                globalThis.usecaseData = dataWithDomain;
                UsecaseApp.init();

                const code = getPackageDiagramCode();
                assert.ok(code, 'パッケージ図が生成されること');

                const serviceBNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceB');
                const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.domain.Order');
                assert.ok(code.includes(`${serviceBNodeId} -.-> ${orderNodeId}`), 'クラス→戻り値ドメインの破線エッジが含まれること');
                assert.ok(code.includes('./domain.html#' + globalThis.Jig.util.fqnToId("domain", 'com.example.domain.Order')), 'domain.htmlへのリンクが含まれること');

                delete globalThis.domainData;
            });

            test('ドメインモデルとの関連しかないパッケージでも、表示設定OFFのままパッケージ図コンテナが生成される', () => {
                // show-diagram-domain-types はデフォルトOFF
                globalThis.domainData = {
                    types: [{fqn: 'com.example.domain.Order', isDeprecated: false}]
                };
                const domainOnlyData = {
                    usecases: [
                        {
                            fqn: "com.example.ServiceA",
                            fields: [],
                            staticMethods: [],
                            methods: [{
                                fqn: "com.example.ServiceA#findOrder()",
                                visibility: "PUBLIC",
                                parameters: [],
                                returnTypeRef: {fqn: "com.example.domain.Order"},
                                declaration: "findOrder():Order",
                                isDeprecated: false,
                                callMethods: []
                            }]
                        }
                    ]
                };
                setupPackageDiagramGlossary();
                globalThis.usecaseData = domainOnlyData;
                UsecaseApp.init();

                const code = getPackageDiagramCode();
                assert.ok(code, 'ドメインモデルOFFでもパッケージ図コンテナが生成されること');
                const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.domain.Order');
                assert.ok(!code.includes(orderNodeId), '描画自体は現在のトグル状態に従い、ドメインノードは含まれないこと');

                delete globalThis.domainData;
            });
        });

        test('renderUsecaseAppList should handle empty data', () => {
            globalThis.usecaseData = {usecases: []};
            UsecaseApp.init();

            const mainList = document.getElementById('usecase-list');
            assert.equal(mainList.textContent, 'データなし');
        });

        test('initControls should toggle body classes', () => {
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const showMembers = document.getElementById('show-members');
            const showDiagrams = document.getElementById('show-diagrams');
            const showDetails = document.getElementById('show-details');
            const showDescriptions = document.getElementById('show-descriptions');
            const showDeclarations = document.getElementById('show-declarations');

            // Initial state
            assert.equal(showMembers.checked, true);
            assert.equal(document.body.classList.contains('hide-usecase-members'), false);

            // Toggle members
            showMembers.checked = false;
            showMembers.dispatchEvent(new window.Event('change'));
            assert.equal(document.body.classList.contains('hide-usecase-members'), true);

            // Toggle diagrams
            showDiagrams.checked = false;
            showDiagrams.dispatchEvent(new window.Event('change'));
            assert.equal(document.body.classList.contains('hide-usecase-diagrams'), true);

            // Toggle details
            showDetails.checked = false;
            showDetails.dispatchEvent(new window.Event('change'));
            assert.equal(document.body.classList.contains('hide-usecase-details'), true);

            // Toggle descriptions
            showDescriptions.checked = false;
            showDescriptions.dispatchEvent(new window.Event('change'));
            assert.equal(document.body.classList.contains('hide-usecase-descriptions'), true);

            // Toggle declarations
            showDeclarations.checked = false;
            showDeclarations.dispatchEvent(new window.Event('change'));
            assert.equal(document.body.classList.contains('hide-usecase-declarations'), true);
        });

        test('ハンドラのみ表示でinbound呼び出しのあるメソッドだけ表示される', () => {
            globalThis.inboundData = {
                inboundAdapters: [{
                    relations: [
                        {from: 'web.Ctrl#entry()', to: 'com.example.ServiceA#method1()'}
                    ]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;

            // ハンドラのみ表示を選択
            document.getElementById('display-target-handlers-only').checked = true;
            document.getElementById('display-target-all').checked = false;

            UsecaseApp.init();

            // サイドバーに method1 は表示され otherMethod は表示されない
            const sidebar = document.getElementById('usecase-sidebar-list');
            const sidebarLinks = sidebar.querySelectorAll('a');
            const linkTexts = sidebarLinks.map(a => a.textContent);
            assert.ok(linkTexts.includes('method1'), 'ハンドラのmethod1はサイドバーに表示される');
            assert.ok(!linkTexts.includes('otherMethod'), 'ハンドラでないotherMethodはサイドバーに表示されない');

            // メイン一覧に method1 の article は存在し otherMethod の article は存在しない
            const mainList = document.getElementById('usecase-list');
            const method1Id = globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#method1()');
            const otherMethodId = globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#otherMethod()');
            assert.ok(document.getElementById(method1Id), 'method1のarticleが存在する');
            assert.ok(!document.getElementById(otherMethodId), 'otherMethodのarticleは存在しない');

            // パッケージ見出しとカードが表示される（ハンドラを含むため）
            assert.equal(mainList.children.length, 2);

            // クラス単位の図にはハンドラ（method1）のみが含まれ、otherMethodは含まれない
            const serviceSection = document.querySelector('#' + globalThis.Jig.util.fqnToId("type", 'com.example.ServiceA'));
            const classDiagram = serviceSection.querySelector('.diagram-container.class-diagram');
            if (classDiagram) {
                const code = classDiagram.querySelector('.mermaid').textContent;
                const method1NodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#method1()');
                const otherMethodNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#otherMethod()');
                assert.ok(code.includes(method1NodeId), 'クラス図にmethod1が含まれる');
                assert.ok(!code.includes(otherMethodNodeId), 'クラス図にotherMethodは含まれない');
            }
        });

        test('ハンドラのみ表示でinbound呼び出しが一つもないクラスは非表示になる', () => {
            // inboundDataなし
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;

            document.getElementById('display-target-handlers-only').checked = true;
            document.getElementById('display-target-all').checked = false;

            UsecaseApp.init();

            // クラスセクションが表示されない
            const mainList = document.getElementById('usecase-list');
            assert.equal(mainList.children.length, 0, 'ハンドラなしのクラスは非表示');
            // サイドバーも空
            const sidebar = document.getElementById('usecase-sidebar-list');
            const sidebarLinks = sidebar.querySelectorAll('a');
            assert.equal(sidebarLinks.length, 0, 'サイドバーにリンクなし');
        });

        test('すべて表示（デフォルト）では全メソッドが表示される', () => {
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const method1Id = globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#method1()');
            const otherMethodId = globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#otherMethod()');
            assert.ok(document.getElementById(method1Id), 'method1が表示される');
            assert.ok(document.getElementById(otherMethodId), 'otherMethodも表示される');
        });

        test('シーケンス図タブを選択した状態で再レンダリングしてもシーケンス図が維持される', () => {
            globalThis.usecaseData = mockUsecaseAppData;
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            UsecaseApp.init();

            const methodFqn = "com.example.ServiceA#method1()";
            const methodElement = document.getElementById(globalThis.Jig.util.fqnToId("method", methodFqn));
            const sequenceBtn = methodElement.querySelectorAll('.jig-tabs button')[1];

            // シーケンス図タブをクリック
            sequenceBtn.dispatchEvent(new window.Event('click'));

            // 状態が 'sequence' になっていることを確認
            assert.equal(UsecaseApp.state.selectedTabs.get(methodFqn), 'sequence');

            // 再レンダリング（チェックボックス変更をシミュレート）
            const showDiagramInternalMethods = document.getElementById('show-diagram-internal-methods');
            showDiagramInternalMethods.checked = false;
            showDiagramInternalMethods.dispatchEvent(new window.Event('change'));

            // 再レンダリング後の要素を取得
            const newMethodElement = document.getElementById(globalThis.Jig.util.fqnToId("method", methodFqn));
            const newSequenceBtn = newMethodElement.querySelectorAll('.jig-tabs button')[1];
            const newSequencePanel = newMethodElement.querySelectorAll('.jig-tab-panel')[1];

            // シーケンス図タブが active で、パネルが hidden でないことを確認
            assert.ok(newSequenceBtn.classList.contains('active'));
            assert.ok(!newSequencePanel.classList.contains('hidden'));
        });

        test('inbound呼び出し元はクラスノード化されinbound.htmlへのリンクが付与される', () => {
            globalThis.usecaseData = mockUsecaseAppData;
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1", description: "Description of method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"},
                "web.Ctrl": {title: "Ctrl"}
            });
            globalThis.inboundData = {
                inboundAdapters: [
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

            const methodSection = document.getElementById(globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#method1()')).parentElement;
            assert.ok(methodSection);
            const mermaidPre = methodSection.querySelector('.mermaid');
            assert.ok(mermaidPre);
            const code = mermaidPre.textContent;
            assert.ok(code.includes('click'));
            assert.ok(code.includes('./inbound.html#' + globalThis.Jig.util.fqnToId("adapter", 'web.Ctrl')));
            assert.ok(code.includes('click'));
            assert.ok(code.includes('#' + globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#method1()')));
        });

        test('引数にドメインモデル型を持つメソッドの図にドメインモデルノードと引数→メソッドエッジが追加される', () => {
            document.getElementById('show-diagram-domain-types').checked = true;
            globalThis.domainData = {
                types: [{fqn: 'com.example.Order', isDeprecated: false}]
            };
            const usecaseDataWithDomainParam = {
                usecases: [{
                    fqn: "com.example.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [{
                        fqn: "com.example.ServiceA#method1(Order)",
                        visibility: "PUBLIC",
                        parameters: [{name: "arg0", nameSource: "POSITIONAL", typeRef: {fqn: "com.example.Order"}}],
                        returnTypeRef: {fqn: "void"},
                        declaration: "method1(Order):void",
                        isDeprecated: false,
                        callMethods: []
                    }]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1(Order)": {title: "method1"},
                "com.example.Order": {title: "Order"}
            });
            globalThis.usecaseData = usecaseDataWithDomainParam;
            UsecaseApp.init();

            const methodId = globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#method1(Order)');
            const methodSection = document.getElementById(methodId).parentElement;
            const mermaidPre = methodSection.querySelector('.mermaid');
            assert.ok(mermaidPre, 'Mermaid図が生成されること');
            const code = mermaidPre.textContent;

            const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Order');
            const methodNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#method1(Order)');
            assert.ok(code.includes(orderNodeId), 'ドメインモデルのノードが含まれること');
            assert.ok(code.includes(`${orderNodeId} -.-> ${methodNodeId}`), '引数→メソッドのエッジが破線で含まれること');
            assert.ok(code.includes('./domain.html#' + globalThis.Jig.util.fqnToId("domain", 'com.example.Order')), 'domain.htmlへのリンクが含まれること');
            assert.ok(code.includes('"com.example.Order"'), 'ユースケース図のdomain-typeノードにFQNのツールチップが含まれること');

            delete globalThis.domainData;
        });

        test('戻り値にドメインモデル型を持つメソッドの図にドメインモデルノードとメソッド→戻り値エッジが追加される', () => {
            document.getElementById('show-diagram-domain-types').checked = true;
            globalThis.domainData = {
                types: [{fqn: 'com.example.Order', isDeprecated: false}]
            };
            const usecaseDataWithDomainReturn = {
                usecases: [{
                    fqn: "com.example.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [{
                        fqn: "com.example.ServiceA#findOrder()",
                        visibility: "PUBLIC",
                        parameters: [],
                        returnTypeRef: {fqn: "com.example.Order"},
                        declaration: "findOrder():Order",
                        isDeprecated: false,
                        callMethods: []
                    }]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#findOrder()": {title: "findOrder"},
                "com.example.Order": {title: "Order"}
            });
            globalThis.usecaseData = usecaseDataWithDomainReturn;
            UsecaseApp.init();

            const methodId = globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#findOrder()');
            const methodSection = document.getElementById(methodId).parentElement;
            const mermaidPre = methodSection.querySelector('.mermaid');
            assert.ok(mermaidPre, 'Mermaid図が生成されること');
            const code = mermaidPre.textContent;

            const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Order');
            const methodNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.ServiceA#findOrder()');
            assert.ok(code.includes(orderNodeId), 'ドメインモデルのノードが含まれること');
            assert.ok(code.includes(`${methodNodeId} -.-> ${orderNodeId}`), 'メソッド→戻り値のエッジが破線で含まれること');

            delete globalThis.domainData;
        });

        test('show-diagram-domain-typesがOFFの場合、ドメインモデルノードは表示されない', () => {
            // デフォルトは unchecked
            globalThis.domainData = {
                types: [{fqn: 'com.example.Order', isDeprecated: false}]
            };
            const usecaseDataWithDomain = {
                usecases: [{
                    fqn: "com.example.ServiceA",
                    fields: [],
                    staticMethods: [],
                    methods: [{
                        fqn: "com.example.ServiceA#findOrder()",
                        visibility: "PUBLIC",
                        parameters: [],
                        returnTypeRef: {fqn: "com.example.Order"},
                        declaration: "findOrder():Order",
                        isDeprecated: false,
                        callMethods: []
                    }]
                }]
            };
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#findOrder()": {title: "findOrder"},
                "com.example.Order": {title: "Order"}
            });
            globalThis.usecaseData = usecaseDataWithDomain;
            UsecaseApp.init();

            const methodId = globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#findOrder()');
            const methodSection = document.getElementById(methodId);
            // ドメインノードのみでエッジがなければ図自体が生成されないか、生成されてもドメインノードを含まない
            const mermaidPre = methodSection?.querySelector('.mermaid');
            if (mermaidPre) {
                const orderNodeId = globalThis.Jig.util.fqnToId("node", 'com.example.Order');
                assert.ok(!mermaidPre.textContent.includes(orderNodeId), 'チェックOFFではドメインノードが含まれないこと');
            }

            delete globalThis.domainData;
        });

        test('domainDataがない場合でもドメインモデルノードが追加されず正常動作する', () => {
            delete globalThis.domainData;
            setGlossaryData({
                "com.example.ServiceA": {title: "ServiceA"},
                "com.example.ServiceA#method1()": {title: "method1"},
                "com.example.ServiceA#otherMethod()": {title: "otherMethod"}
            });
            globalThis.usecaseData = mockUsecaseAppData;
            UsecaseApp.init();

            const methodId = globalThis.Jig.util.fqnToId("method", 'com.example.ServiceA#method1()');
            const methodSection = document.getElementById(methodId).parentElement;
            const mermaidPres = methodSection.querySelectorAll('.mermaid');
            assert.ok(mermaidPres.length > 0, 'Mermaid図は生成されること');
            // ドメインモデルノードは含まれない（エラーなし）
            const graphCode = mermaidPres[0].textContent;
            assert.ok(graphCode.includes('graph LR'), '正常なグラフが生成されること');
        });
    });

    test.describe('SequenceDiagram', () => {
        let SequenceDiagram;

        test.describe('buildSequenceDiagram', () => {

            test.beforeEach(() => {
                delete globalThis.inboundData;

                const doc = new DocumentStub();
                doc.body.classList.add("usecase-model");
                global.document = doc;
                global.window = {
                    addEventListener: () => {
                    }, Event: EventStub
                };
                global.marked = {parse: (text) => text};
                global.DOMPurify = {sanitize: (html) => html};
                global.mermaid = {
                    initialize: () => {
                    }, run: () => {
                    }
                };

                require(jigCommonJsPath);
                require(jigMermaidDiagramJsPath);
                require(jigJsPath);
                SequenceDiagram = require(usecaseJsPath).SequenceDiagram;
            });

            test('callMethodsが空の場合はcallsが空', () => {
                const rootMethod = {fqn: 'com.example.ServiceA#method1()', callMethods: []};
                const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

                const result = SequenceDiagram.buildDiagram(rootMethod, methodMap);

                assert.equal(result.calls.length, 0);
                assert.equal(result.participants.length, 1);
                assert.equal(result.participants[0].label, 'method1');
                assert.equal(result.participants[0].kind, "usecase");
            });

            test('ユースケース内メソッドへの呼び出しはメソッド単位のパーティシパント', () => {
                const otherMethod = {fqn: 'com.example.ServiceA#otherMethod()', callMethods: []};
                const rootMethod = {
                    fqn: 'com.example.ServiceA#method1()',
                    callMethods: ['com.example.ServiceA#otherMethod()']
                };
                const methodMap = new Map([
                    ['com.example.ServiceA#method1()', rootMethod],
                    ['com.example.ServiceA#otherMethod()', otherMethod]
                ]);

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet: new Set(),
                    showDiagramInternalMethods: true,
                    showDiagramOutboundPorts: true
                });

                assert.equal(result.participants.length, 2);
                assert.equal(result.participants[0].label, 'method1');
                assert.equal(result.participants[0].kind, "usecase");
                assert.equal(result.participants[1].label, 'otherMethod');
                assert.equal(result.participants[1].kind, "usecase");
                assert.equal(result.calls.length, 1);
                assert.equal(result.calls[0].label, '');
            });

            test('ユースケース外メソッドへの呼び出しはoutboundOperationSetにある場合だけクラス単位のパーティシパント', () => {
                const rootMethod = {
                    fqn: 'com.example.ServiceA#method1()',
                    callMethods: ['com.example.RepositoryB#save(com.example.Entity)']
                };
                const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);
                const outboundOperationSet = new Set(['com.example.RepositoryB#save(com.example.Entity)']);

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet,
                    showDiagramInternalMethods: true,
                    showDiagramOutboundPorts: true
                });

                assert.equal(result.participants.length, 2);
                assert.equal(result.participants[1].label, 'RepositoryB');
                assert.equal(result.participants[1].kind, 'outbound');
                assert.equal(result.calls.length, 1);
                assert.equal(result.calls[0].label, 'save');
            });

            test('outboundOperationSetにない外部呼び出しは無視される', () => {
                const rootMethod = {
                    fqn: 'com.example.ServiceA#method1()',
                    callMethods: ['com.example.RepositoryB#save()']
                };
                const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet: new Set(),
                    showDiagramInternalMethods: true,
                    showDiagramOutboundPorts: true
                });

                assert.equal(result.participants.length, 1);
                assert.equal(result.calls.length, 0);
            });

            test('内部と外部への呼び出しが混在する場合も両方適切に処理', () => {
                const otherMethod = {fqn: 'com.example.ServiceA#otherMethod()', callMethods: []};
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

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet,
                    showDiagramInternalMethods: true,
                    showDiagramOutboundPorts: true
                });

                assert.equal(result.participants.length, 3);
                assert.equal(result.calls.length, 2);
                assert.equal(result.calls[0].label, '');
                assert.equal(result.calls[1].label, 'save');
            });

            test('再帰的に内部メソッドを処理する', () => {
                const deepMethod = {fqn: 'com.example.ServiceA#deepMethod()', callMethods: []};
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

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet: new Set(),
                    showDiagramInternalMethods: true,
                    showDiagramOutboundPorts: true
                });

                assert.equal(result.participants.length, 3);
                assert.equal(result.calls.length, 2);
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

                const result = SequenceDiagram.buildDiagram(methodA, {
                    methodMap,
                    outboundOperationSet: new Set(),
                    showDiagramInternalMethods: true,
                    showDiagramOutboundPorts: true
                });

                assert.equal(result.participants.length, 2);
                // methodA->methodB と methodB->methodA の2呼び出し
                assert.equal(result.calls.length, 2);
            });

            test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドはパーティシパントとして追加されず呼び出しがインライン化される', () => {
                const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
                const methodB = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#C()'], kind: 'method'};
                const methodC = {fqn: 'pkg.Cls#C()', callMethods: [], kind: 'usecase'};
                const methodMap = new Map([
                    ['pkg.Cls#A()', {...rootMethod, kind: 'usecase'}],
                    ['pkg.Cls#B()', {...methodB, kind: 'method'}],
                    ['pkg.Cls#C()', {...methodC, kind: 'usecase'}]
                ]);

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet: new Set(),
                    showDiagramInternalMethods: false,
                    showDiagramOutboundPorts: true
                });

                // A と C だけがパーティシパントとして残る
                assert.equal(result.participants.length, 2);
                assert.ok(result.participants.find(p => p.id.includes('_A_')));
                assert.ok(result.participants.find(p => p.id.includes('_C_')));
                // コールは A -> C になる
                assert.equal(result.calls.length, 1);
                assert.ok(result.calls[0].from.includes('_A_'));
                assert.ok(result.calls[0].to.includes('_C_'));
            });

            test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドを介した外部呼び出しもインライン化される(シーケンス図)', () => {
                const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
                const methodB = {fqn: 'pkg.Cls#B()', callMethods: ['ext.Cls#method()'], kind: 'method'};
                const methodMap = new Map([
                    ['pkg.Cls#A()', {...rootMethod, kind: 'usecase'}],
                    ['pkg.Cls#B()', {...methodB, kind: 'method'}]
                ]);
                const outboundOperationSet = new Set(['ext.Cls#method()']);

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet,
                    showDiagramInternalMethods: false,
                    showDiagramOutboundPorts: true
                });

                assert.equal(result.participants.length, 2);
                assert.ok(result.participants.find(p => p.kind === 'outbound'));
                assert.equal(result.calls.length, 1);
                assert.ok(result.calls[0].from.includes('_A_'));
            });

            test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドの循環参照があっても無限ループしない(シーケンス図)', () => {
                const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
                const methodB = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#C()'], kind: 'method'};
                const methodC = {fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#B()', 'pkg.Cls#D()'], kind: 'method'};
                const methodD = {fqn: 'pkg.Cls#D()', callMethods: [], kind: 'usecase'};
                const methodMap = new Map([
                    ['pkg.Cls#A()', {...rootMethod, kind: 'usecase'}],
                    ['pkg.Cls#B()', {...methodB, kind: 'method'}],
                    ['pkg.Cls#C()', {...methodC, kind: 'method'}],
                    ['pkg.Cls#D()', {...methodD, kind: 'usecase'}]
                ]);

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet: new Set(),
                    showDiagramInternalMethods: false,
                    showDiagramOutboundPorts: true
                });

                assert.equal(result.participants.length, 2);
                assert.equal(result.calls.length, 1);
                assert.ok(result.calls[0].from.includes('_A_'));
                assert.ok(result.calls[0].to.includes('_D_'));
            });

            test('showDiagramOutboundPortsがfalseの場合、外部ポートはパーティシパントとして追加されない', () => {
                const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['ext.Cls#method()'], kind: 'usecase'};
                const methodMap = new Map([['pkg.Cls#A()', {...rootMethod, kind: 'usecase'}]]);
                const outboundOperationSet = new Set(['ext.Cls#method()']);

                const result = SequenceDiagram.buildDiagram(rootMethod, {
                    methodMap,
                    outboundOperationSet,
                    showDiagramInternalMethods: true,
                    showDiagramOutboundPorts: false
                });

                assert.equal(result.participants.length, 1);
                assert.equal(result.calls.length, 0);
            });
        });

        test.describe('buildSequenceDiagramCode', () => {


            test.beforeEach(() => {

                const doc = new DocumentStub();
                doc.body.classList.add("usecase-model");
                global.document = doc;
                global.window = {
                    addEventListener: () => {
                    }, Event: EventStub
                };
                global.marked = {parse: (text) => text};
                global.DOMPurify = {sanitize: (html) => html};
                global.mermaid = {
                    initialize: () => {
                    }, run: () => {
                    }
                };
            });

            test('callsが空の場合はnullを返す', () => {
                const sequence = {participants: [{id: 'node-a', label: 'methodA', isExternal: false}], calls: []};
                assert.equal(SequenceDiagram.buildCode(sequence), null);
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
                const code = SequenceDiagram.buildCode(sequence);

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
                const code = SequenceDiagram.buildCode(sequence);

                assert.ok(code.startsWith('sequenceDiagram\n'));
                assert.ok(!code.includes('box'));
                assert.ok(code.includes('participant node-a as methodA'));
                assert.ok(code.includes('participant node-b as methodB'));
            });
        });
    });

    test.describe('buildOutboundOperationSet', () => {


        test.beforeEach(() => {
            const doc = new DocumentStub();
            doc.body.classList.add("usecase-model");
            global.document = doc;
            global.window = {
                addEventListener: () => {
                }, Event: EventStub
            };
            global.marked = {parse: (text) => text};
            global.DOMPurify = {sanitize: (html) => html};
            global.mermaid = {
                initialize: () => {
                }, run: () => {
                }
            };

        });

        test('nullの場合は空Setを返す', () => {
            assert.equal(UsecaseApp.buildOutboundOperationSet(null).size, 0);
        });

        test('undefinedの場合は空Setを返す', () => {
            assert.equal(UsecaseApp.buildOutboundOperationSet(undefined).size, 0);
        });

        test('outboundPortsがない場合は空Setを返す', () => {
            assert.equal(UsecaseApp.buildOutboundOperationSet({}).size, 0);
        });

        test('outboundPortsのoperationsのfqnをSetに収集する', () => {
            const outboundData = {
                outboundPorts: [
                    {
                        fqn: 'com.example.RepositoryB',
                        operations: [
                            {fqn: 'com.example.RepositoryB#save()', signature: 'save()'},
                            {fqn: 'com.example.RepositoryB#find()', signature: 'find()'}
                        ]
                    },
                    {
                        fqn: 'com.example.ExternalApi',
                        operations: [
                            {fqn: 'com.example.ExternalApi#call()', signature: 'call()'}
                        ]
                    }
                ]
            };
            const set = UsecaseApp.buildOutboundOperationSet(outboundData);
            assert.equal(set.size, 3);
            assert.ok(set.has('com.example.RepositoryB#save()'));
            assert.ok(set.has('com.example.RepositoryB#find()'));
            assert.ok(set.has('com.example.ExternalApi#call()'));
        });
    });

    test.describe('buildOutboundOperationParameterMap', () => {
        test('outboundPortsのoperationsのfqnからparametersを引けるMapを作る', () => {
            const outboundData = {
                outboundPorts: [
                    {
                        fqn: 'com.example.RepositoryB',
                        operations: [
                            {fqn: 'com.example.RepositoryB#save(Order)', parameters: [{name: 'arg0', nameSource: 'POSITIONAL', typeRef: {fqn: 'com.example.Order'}}]}
                        ]
                    }
                ]
            };
            const map = UsecaseApp.buildOutboundOperationParameterMap(outboundData);
            assert.equal(map.get('com.example.RepositoryB#save(Order)').length, 1);
            assert.equal(map.get('com.example.RepositoryB#save(Order)')[0].typeRef.fqn, 'com.example.Order');
        });

        test('outboundPortsがない場合は空Mapを返す', () => {
            assert.equal(UsecaseApp.buildOutboundOperationParameterMap(null).size, 0);
        });
    });

    test.describe('buildArgumentsLabel', () => {
        const typeLabel = fqn => fqn;

        test('パラメータが空の場合は空文字列を返す', () => {
            assert.equal(UsecaseApp.buildArgumentsLabel([], typeLabel), '');
            assert.equal(UsecaseApp.buildArgumentsLabel(null, typeLabel), '');
        });

        test('nameSourceがMETHOD_PARAMETERSの場合は "name: Type" 形式になる', () => {
            const parameters = [{name: 'order', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.Order'}}];
            assert.equal(UsecaseApp.buildArgumentsLabel(parameters, typeLabel), 'order: com.example.Order');
        });

        test('nameSourceがMETHOD_PARAMETERS以外の場合は型のみになる（実引数名が取得できていないため）', () => {
            const parameters = [{name: 'arg0', nameSource: 'POSITIONAL', typeRef: {fqn: 'com.example.Order'}}];
            assert.equal(UsecaseApp.buildArgumentsLabel(parameters, typeLabel), 'com.example.Order');
        });

        test('複数パラメータはカンマ区切りになる', () => {
            const parameters = [
                {name: 'order', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.Order'}},
                {name: 'arg1', nameSource: 'POSITIONAL', typeRef: {fqn: 'java.lang.String'}}
            ];
            assert.equal(UsecaseApp.buildArgumentsLabel(parameters, typeLabel), 'order: com.example.Order, java.lang.String');
        });

        test('typeLabelで用語名変換される', () => {
            const parameters = [{name: 'order', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.Order'}}];
            const label = UsecaseApp.buildArgumentsLabel(parameters, () => '注文');
            assert.equal(label, 'order: 注文');
        });
    });

    test.describe('buildUsecaseDiagram', () => {


        test.beforeEach(() => {
            const doc = new DocumentStub();
            doc.body.classList.add("usecase-model");
            global.document = doc;
            global.window = {
                addEventListener: () => {
                }, Event: EventStub
            };
            global.marked = {parse: (text) => text};
            global.DOMPurify = {sanitize: (html) => html};
            global.mermaid = {
                initialize: () => {
                }, run: () => {
                }
            };
        });

        test('outboundOperationSetが空の場合、外部ノードは追加されない', () => {
            const rootMethod = {
                fqn: 'com.example.ServiceA#method1()',
                callMethods: ['com.example.RepositoryB#save()']
            };
            const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true
            });

            assert.equal(result.nodes.length, 1);
            assert.equal(result.edges.length, 0);
        });

        test('outboundOperationSetに含まれる外部呼び出しはメソッドノードとして追加される', () => {
            const rootMethod = {
                fqn: 'com.example.ServiceA#method1()',
                callMethods: ['com.example.RepositoryB#save()']
            };
            const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);
            const outboundOperationSet = new Set(['com.example.RepositoryB#save()']);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet,
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true
            });

            assert.equal(result.nodes.length, 2);
            assert.equal(result.edges.length, 1);
            const externalNode = result.nodes.find(n => n.fqn === 'com.example.RepositoryB#save()');
            assert.ok(externalNode);
            assert.equal(externalNode.kind, 'outbound-method');
            assert.equal(result.edges[0].to, 'com.example.RepositoryB#save()');
        });

        test('outboundOperationSetに含まれない外部呼び出しは追加されない', () => {
            const rootMethod = {
                fqn: 'com.example.ServiceA#method1()',
                callMethods: ['com.example.OtherService#doWork()', 'com.example.RepositoryB#save()']
            };
            const methodMap = new Map([['com.example.ServiceA#method1()', rootMethod]]);
            const outboundOperationSet = new Set(['com.example.RepositoryB#save()']);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet,
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true
            });

            assert.equal(result.nodes.length, 2);
            const nodes = result.nodes.map(n => n.fqn);
            assert.ok(nodes.includes('com.example.RepositoryB#save()'));
            assert.ok(!nodes.includes('com.example.OtherService#doWork()'));
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

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true
            });

            assert.equal(result.nodes.length, 2);
            result.nodes.forEach(n => assert.equal(n.kind, "usecase"));
        });

        test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドはノードとして追加されず呼び出しがインライン化される', () => {
            const rootMethod = {fqn: 'A', callMethods: ['B'], kind: 'usecase'};
            const methodB = {fqn: 'B', callMethods: ['C'], kind: 'method'};
            const methodC = {fqn: 'C', callMethods: [], kind: 'usecase'};
            const methodMap = new Map([['A', rootMethod], ['B', methodB], ['C', methodC]]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            // A と C だけがノードとして残る
            assert.equal(result.nodes.length, 2);
            assert.ok(result.nodes.find(n => n.fqn === 'A'));
            assert.ok(result.nodes.find(n => n.fqn === 'C'));
            // エッジは A -> C になる
            assert.equal(result.edges.length, 1);
            assert.equal(result.edges[0].from, 'A');
            assert.equal(result.edges[0].to, 'C');
        });

        test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドを介した外部呼び出しもインライン化される', () => {
            const rootMethod = {fqn: 'A', callMethods: ['B'], kind: 'usecase'};
            const methodB = {fqn: 'B', callMethods: ['ext#method()'], kind: 'method'};
            const methodMap = new Map([['A', rootMethod], ['B', methodB]]);
            const outboundOperationSet = new Set(['ext#method()']);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet,
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            assert.equal(result.nodes.length, 2);
            assert.ok(result.nodes.find(n => n.fqn === 'ext#method()'));
            assert.equal(result.edges.length, 1);
            assert.equal(result.edges[0].from, 'A');
            assert.equal(result.edges[0].to, 'ext#method()');
        });

        test('showDiagramInternalMethodsがfalseの場合、非ユースケースメソッドの循環参照があっても無限ループしない', () => {
            const rootMethod = {fqn: 'A', callMethods: ['B'], kind: 'usecase'};
            const methodB = {fqn: 'B', callMethods: ['C'], kind: 'method'};
            const methodC = {fqn: 'C', callMethods: ['B', 'D'], kind: 'method'};
            const methodD = {fqn: 'D', callMethods: [], kind: 'usecase'};
            const methodMap = new Map([['A', rootMethod], ['B', methodB], ['C', methodC], ['D', methodD]]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            assert.equal(result.nodes.length, 2);
            assert.ok(result.nodes.find(n => n.fqn === 'A'));
            assert.ok(result.nodes.find(n => n.fqn === 'D'));
            assert.equal(result.edges.length, 1);
            assert.equal(result.edges[0].from, 'A');
            assert.equal(result.edges[0].to, 'D');
        });

        test('showDiagramOutboundPortsがfalseの場合、外部ポートはノードとして追加されない', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['ext.Cls#method()'], kind: 'usecase'};
            const methodMap = new Map([['pkg.Cls#A()', rootMethod]]);
            const outboundOperationSet = new Set(['ext.Cls#method()']);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet,
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: false
            });

            assert.equal(result.nodes.length, 1);
            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
            assert.equal(result.edges.length, 0);
        });

        test('showDiagramCallersがfalseの場合、呼び出し元はノードとして追加されない', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const callerMethod = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'usecase'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', callerMethod]
            ]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramCallers: false,
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true
            });

            assert.equal(result.nodes.length, 1);
            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
            assert.equal(result.edges.length, 0);
        });

        test('showDiagramCalleesがfalseの場合、呼び出し先はノードとして追加されない', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const calleeMethod = {fqn: 'pkg.Cls#B()', callMethods: [], kind: 'method'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', calleeMethod]
            ]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramCallees: false,
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true
            });

            assert.equal(result.nodes.length, 1);
            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
            assert.equal(result.edges.length, 0);
        });

        test('直接の呼び出し元(usecase)は caller -> root のエッジで追加される', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const callerMethod = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'usecase'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', callerMethod]
            ]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
            assert.equal(result.edges.length, 1);
            assert.deepStrictEqual(result.edges[0], {from: 'pkg.Cls#B()', to: 'pkg.Cls#A()'});
        });

        test('showDiagramInternalMethods=falseでは非ユースケース呼び出し元を遡ってユースケース呼び出し元を表示する', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const directCaller = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'method'};
            const usecaseCaller = {fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', directCaller],
                ['pkg.Cls#C()', usecaseCaller]
            ]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#C()'));
            assert.ok(!result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
            assert.equal(result.edges.length, 1);
            assert.ok(result.edges.find(e => e.from === 'pkg.Cls#C()' && e.to === 'pkg.Cls#A()'));
        });

        test('直接の呼び出し元が非ユースケースの場合、showDiagramInternalMethods=trueでは表示する', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const callerMethod = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'method'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', callerMethod]
            ]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
            assert.ok(result.edges.find(e => e.from === 'pkg.Cls#B()' && e.to === 'pkg.Cls#A()'));
        });

        test('showDiagramInternalMethods=trueでは呼び出し元は直接のみ表示する', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const directCaller = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'usecase'};
            const indirectCaller = {fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', directCaller],
                ['pkg.Cls#C()', indirectCaller]
            ]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
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
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['ext.Repo#save()'], kind: 'usecase'};
            const callerMethod = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'usecase'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', callerMethod]
            ]);
            const outboundOperationSet = new Set(['ext.Repo#save()']);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet,
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#B()'));
            assert.ok(result.nodes.find(n => n.fqn === 'ext.Repo#save()'));
            assert.ok(result.edges.find(e => e.from === 'pkg.Cls#B()' && e.to === 'pkg.Cls#A()'));
            assert.ok(result.edges.find(e => e.from === 'pkg.Cls#A()' && e.to === 'ext.Repo#save()'));
        });

        test('showDiagramInternalMethods=falseで複数経路から同一ユースケース呼び出し元に到達しても重複しない', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()'], kind: 'method'};
            const methodC = {fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#A()'], kind: 'method'};
            const usecaseCaller = {fqn: 'pkg.Cls#U()', callMethods: ['pkg.Cls#B()', 'pkg.Cls#C()'], kind: 'usecase'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', methodB],
                ['pkg.Cls#C()', methodC],
                ['pkg.Cls#U()', usecaseCaller]
            ]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            const callerEdges = result.edges.filter(e => e.from === 'pkg.Cls#U()' && e.to === 'pkg.Cls#A()');
            assert.equal(callerEdges.length, 1);
        });

        test('showDiagramInternalMethods=falseで逆方向循環があっても無限ループしない', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#A()', 'pkg.Cls#C()'], kind: 'method'};
            const methodC = {fqn: 'pkg.Cls#C()', callMethods: ['pkg.Cls#B()'], kind: 'method'};
            const usecaseCaller = {fqn: 'pkg.Cls#U()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const methodMap = new Map([
                ['pkg.Cls#A()', rootMethod],
                ['pkg.Cls#B()', methodB],
                ['pkg.Cls#C()', methodC],
                ['pkg.Cls#U()', usecaseCaller]
            ]);

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#U()'));
            assert.ok(result.edges.find(e => e.from === 'pkg.Cls#U()' && e.to === 'pkg.Cls#A()'));
        });

        test('usecaseDataにないinbound側の直接呼び出し元もメソッドとして表示される', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const methodMap = new Map([['pkg.Cls#A()', rootMethod]]);
            globalThis.inboundData = {
                inboundAdapters: [
                    {
                        relations: [
                            {from: 'web.Ctrl#entry()', to: 'pkg.Cls#A()'},
                            {from: 'web.Ctrl#indirect()', to: 'web.Ctrl#entry()'}
                        ]
                    }
                ]
            };

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'pkg.Cls#A()'));
            assert.ok(result.nodes.find(n => n.fqn === 'web.Ctrl#entry()'));
            assert.ok(result.nodes.find(n => n.fqn === 'web.Ctrl#entry()').kind === 'inbound-method');
            assert.ok(result.edges.find(e => e.from === 'web.Ctrl#entry()' && e.to === 'pkg.Cls#A()'));
        });

        test('同一inboundクラスの複数メソッド呼び出しはそれぞれメソッドノードとして表示される', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: [], kind: 'usecase'};
            const methodMap = new Map([['pkg.Cls#A()', rootMethod]]);
            globalThis.inboundData = {
                inboundAdapters: [
                    {
                        relations: [
                            {from: 'web.Ctrl#entry()', to: 'pkg.Cls#A()'},
                            {from: 'web.Ctrl#entry2()', to: 'pkg.Cls#A()'}
                        ]
                    }
                ]
            };

            const result = UsecaseApp.buildUsecaseDiagram(rootMethod, {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: false,
                showDiagramOutboundPorts: true
            });

            const inboundNodes = result.nodes.filter(n => n.kind === 'inbound-method');
            assert.equal(inboundNodes.length, 2);
            assert.ok(result.edges.find(e => e.from === 'web.Ctrl#entry()' && e.to === 'pkg.Cls#A()'));
            assert.ok(result.edges.find(e => e.from === 'web.Ctrl#entry2()' && e.to === 'pkg.Cls#A()'));
        });
    });

    test.describe('createUsecaseDiagramGenerator', () => {
        function buildContext(rootMethod, methodB, overrides = {}) {
            const methodMap = new Map([[rootMethod.fqn, rootMethod], [methodB.fqn, methodB]]);
            return {
                methodMap,
                reverseCallerMap: UsecaseApp.buildReverseCallerMap(methodMap),
                outboundOperationSet: new Set(),
                showDiagramCallers: true,
                showDiagramCallees: true,
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true,
                showDiagramDomainTypes: false,
                ...overrides
            };
        }

        test('メニュー項目はサイドバーの現在値をON/OFF状態(checked)として反映する（ラベルは固定名詞）', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: [], kind: 'method'};
            const context = buildContext(rootMethod, methodB);

            const generator = UsecaseApp.createUsecaseDiagramGenerator(rootMethod, () => context);
            const items = generator.buildExtraMenuItems(() => {});

            assert.deepEqual(items.map(i => i.label), [
                '呼び出し元',
                '呼び出し先',
                '内部メソッド',
                '出力インタフェース',
                'ドメインモデル',
                '引数'
            ]);
            assert.deepEqual(items.map(i => i.checked), [true, true, true, true, false, false]);
        });

        test('メニュー項目を選択するとこのダイアグラムだけ表示要素が切り替わる（サイドバー側の値は変更しない）', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: [], kind: 'method'};
            const methodBNodeId = globalThis.Jig.util.fqnToId("node", methodB.fqn);
            // サイドバー設定は「内部メソッド:表示」のまま
            const context = buildContext(rootMethod, methodB, {showDiagramInternalMethods: true});

            const generator = UsecaseApp.createUsecaseDiagramGenerator(rootMethod, () => context);

            // 内部メソッドを非表示に切り替える前は B ノードが含まれる
            const before = generator('LR', {});
            assert.ok(before.includes(methodBNodeId), '切替前はBノードを含む');

            let rerendered = false;
            const items = generator.buildExtraMenuItems(() => { rerendered = true; });
            const internalMethodsItem = items.find(i => i.label.startsWith('内部メソッド'));
            internalMethodsItem.onSelect();

            assert.equal(rerendered, true, '選択時に再描画コールバックが呼ばれる');
            assert.equal(context.showDiagramInternalMethods, true, 'サイドバー（グローバル）側の値は変更されない');

            // 切替後は B ノードが消える（このダイアグラムだけの上書き）
            const after = generator('LR', {});
            assert.ok(!after.includes(methodBNodeId), '切替後はBノードを含まない');

            // メニューを再度取得すると状態がトグルされていることが反映される
            const itemsAfter = generator.buildExtraMenuItems(() => {});
            assert.equal(itemsAfter.find(i => i.label.startsWith('内部メソッド')).checked, false);
        });

        test('同じmethodから複数回生成しても、それぞれ独立した上書き状態を持つ', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: [], kind: 'method'};
            const methodBNodeId = globalThis.Jig.util.fqnToId("node", methodB.fqn);
            const context = buildContext(rootMethod, methodB, {showDiagramInternalMethods: true});

            const generator1 = UsecaseApp.createUsecaseDiagramGenerator(rootMethod, () => context);
            const generator2 = UsecaseApp.createUsecaseDiagramGenerator(rootMethod, () => context);

            generator1.buildExtraMenuItems(() => {}).find(i => i.label.startsWith('内部メソッド')).onSelect();

            assert.ok(!generator1('LR', {}).includes(methodBNodeId), 'generator1はBノードを含まない');
            assert.ok(generator2('LR', {}).includes(methodBNodeId), 'generator2は影響を受けずBノードを含む');
        });

        test('既定（引数ON）では呼び出し先メソッドの引数がedgeラベルとして表示される', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B(Order)'], kind: 'usecase'};
            const methodB = {
                fqn: 'pkg.Cls#B(Order)', callMethods: [], kind: 'method',
                parameters: [{name: 'order', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.Order'}}]
            };
            const context = buildContext(rootMethod, methodB, {showDiagramArguments: true});

            const generator = UsecaseApp.createUsecaseDiagramGenerator(rootMethod, () => context);
            const source = generator('LR', {});

            assert.ok(source.includes('order: Order'), '引数ラベルを含む');
        });

        test('引数OFFに切り替えるとedgeラベルが表示されなくなる', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B(Order)'], kind: 'usecase'};
            const methodB = {
                fqn: 'pkg.Cls#B(Order)', callMethods: [], kind: 'method',
                parameters: [{name: 'order', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.Order'}}]
            };
            const context = buildContext(rootMethod, methodB, {showDiagramArguments: true});

            const generator = UsecaseApp.createUsecaseDiagramGenerator(rootMethod, () => context);
            const items = generator.buildExtraMenuItems(() => {});
            items.find(i => i.label === '引数').onSelect();

            const source = generator('LR', {});
            assert.ok(!source.includes('order: Order'), '引数OFFではラベルを含まない');
        });
    });

    test.describe('createSequenceDiagramGenerator', () => {
        function buildContext(rootMethod, methodB, methodC, overrides = {}) {
            const methodMap = new Map([[rootMethod.fqn, rootMethod], [methodB.fqn, methodB], [methodC.fqn, methodC]]);
            return {
                methodMap,
                outboundOperationSet: new Set(),
                showDiagramInternalMethods: true,
                showDiagramOutboundPorts: true,
                ...overrides
            };
        }

        test('メニュー項目は内部メソッド・出力インタフェース・引数の3つ（ドメインモデルは対象外）', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: [], kind: 'method'};
            const methodC = {fqn: 'pkg.Cls#C()', callMethods: [], kind: 'usecase'};
            const context = buildContext(rootMethod, methodB, methodC);

            const generator = UsecaseApp.createSequenceDiagramGenerator(rootMethod, () => context);
            const items = generator.buildExtraMenuItems(() => {});

            assert.deepEqual(items.map(i => i.label), ['内部メソッド', '出力インタフェース', '引数']);
            assert.deepEqual(items.map(i => i.checked), [true, true, false]);
        });

        test('メニュー項目を選択するとこのシーケンス図だけ表示要素が切り替わる（サイドバー側の値は変更しない）', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#B()'], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: ['pkg.Cls#C()'], kind: 'method'};
            const methodC = {fqn: 'pkg.Cls#C()', callMethods: [], kind: 'usecase'};
            const methodBNodeId = globalThis.Jig.util.fqnToId("node", methodB.fqn);
            const context = buildContext(rootMethod, methodB, methodC, {showDiagramInternalMethods: true});

            const generator = UsecaseApp.createSequenceDiagramGenerator(rootMethod, () => context);

            const before = generator();
            assert.ok(before.includes(methodBNodeId), '切替前はBノードを含む');

            let rerendered = false;
            const items = generator.buildExtraMenuItems(() => { rerendered = true; });
            items.find(i => i.label === '内部メソッド').onSelect();

            assert.equal(rerendered, true, '選択時に再描画コールバックが呼ばれる');
            assert.equal(context.showDiagramInternalMethods, true, 'サイドバー（グローバル）側の値は変更されない');

            const after = generator();
            assert.ok(!after.includes(methodBNodeId), '切替後はBが消え、A->Cの呼び出しにインライン化される');
        });

        test('既定（引数ON）では内部メソッド呼び出しに引数ラベルが付与される', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#C(Order)'], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: [], kind: 'method'};
            const methodC = {
                fqn: 'pkg.Cls#C(Order)', callMethods: [], kind: 'usecase',
                parameters: [{name: 'order', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.Order'}}]
            };
            const context = buildContext(rootMethod, methodB, methodC, {showDiagramArguments: true});

            const generator = UsecaseApp.createSequenceDiagramGenerator(rootMethod, () => context);
            const code = generator();

            assert.ok(code.includes(': order: Order'), '引数ラベルを含む');
        });

        test('既定（引数ON）では出力インタフェース呼び出しに "メソッド名(引数)" 形式のラベルが付与される', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['ext.Repo#save(Order)'], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: [], kind: 'method'};
            const methodC = {fqn: 'pkg.Cls#C()', callMethods: [], kind: 'usecase'};
            const context = buildContext(rootMethod, methodB, methodC, {
                showDiagramArguments: true,
                outboundOperationSet: new Set(['ext.Repo#save(Order)']),
                outboundOperationParameterMap: new Map([['ext.Repo#save(Order)', [{name: 'order', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.Order'}}]]])
            });

            const generator = UsecaseApp.createSequenceDiagramGenerator(rootMethod, () => context);
            const code = generator();

            assert.ok(code.includes(': save(order: Order)'), 'メソッド名と引数ラベルを含む');
        });

        test('引数OFFに切り替えると内部メソッド呼び出しのラベルが空になる', () => {
            const rootMethod = {fqn: 'pkg.Cls#A()', callMethods: ['pkg.Cls#C(Order)'], kind: 'usecase'};
            const methodB = {fqn: 'pkg.Cls#B()', callMethods: [], kind: 'method'};
            const methodC = {
                fqn: 'pkg.Cls#C(Order)', callMethods: [], kind: 'usecase',
                parameters: [{name: 'order', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.Order'}}]
            };
            const context = buildContext(rootMethod, methodB, methodC, {showDiagramArguments: true});

            const generator = UsecaseApp.createSequenceDiagramGenerator(rootMethod, () => context);
            const items = generator.buildExtraMenuItems(() => {});
            items.find(i => i.label === '引数').onSelect();

            const code = generator();
            assert.ok(!code.includes('order: Order'), '引数OFFではラベルを含まない');
        });
    });

    test.describe('buildClassGraph', () => {
        test('showDiagramOutboundPorts=trueかつoutboundOperationSetに含まれる呼び出しはoutbound-classノードとして追加される', () => {
            const usecase = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [
                    {fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: ['ext.Repo#save()']}
                ]
            };

            const result = UsecaseApp.buildClassGraph(usecase, null, {
                outboundOperationSet: new Set(['ext.Repo#save()']),
                showDiagramOutboundPorts: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'ext.Repo' && n.kind === 'outbound-class'));
            assert.ok(result.edges.find(e => e.from === 'pkg.ServiceA#method1()' && e.to === 'ext.Repo'));
        });

        test('showDiagramOutboundPorts=falseの場合、outbound-classノードは追加されない', () => {
            const usecase = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [
                    {fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: ['ext.Repo#save()']}
                ]
            };

            const result = UsecaseApp.buildClassGraph(usecase, null, {
                outboundOperationSet: new Set(['ext.Repo#save()']),
                showDiagramOutboundPorts: false
            });

            assert.ok(!result.nodes.find(n => n.kind === 'outbound-class'));
            assert.equal(result.edges.length, 0);
        });

        test('showDiagramInboundClasses=falseの場合、inbound-classノードは追加されない', () => {
            globalThis.inboundData = {
                inboundAdapters: [{relations: [{from: 'web.Ctrl#create()', to: 'pkg.ServiceA#method1()'}]}]
            };
            const usecase = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [
                    {fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: []}
                ]
            };

            const result = UsecaseApp.buildClassGraph(usecase, null, {
                outboundOperationSet: new Set(),
                showDiagramInboundClasses: false
            });

            assert.ok(!result.nodes.find(n => n.kind === 'inbound-class'));
            assert.equal(result.edges.length, 0);

            delete globalThis.inboundData;
        });

        test('showDiagramDomainTypesを指定しない場合はドメインモデルノードが追加されない（既定は非表示）', () => {
            globalThis.domainData = {types: [{fqn: 'com.example.Order', isDeprecated: false}]};
            const usecase = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [
                    {
                        fqn: 'pkg.ServiceA#method1(Order)',
                        visibility: 'PUBLIC',
                        callMethods: [],
                        parameters: [{name: 'arg0', nameSource: 'POSITIONAL', typeRef: {fqn: 'com.example.Order'}}],
                        returnTypeRef: {fqn: 'void'}
                    }
                ]
            };

            const result = UsecaseApp.buildClassGraph(usecase, null, {outboundOperationSet: new Set()});

            assert.ok(!result.nodes.find(n => n.kind === 'domain-type'));

            delete globalThis.domainData;
        });
    });

    test.describe('createClassDiagramGenerator', () => {
        function buildContext(overrides = {}) {
            return {
                outboundOperationSet: new Set(),
                showDiagramInboundClasses: true,
                showDiagramOutboundPorts: true,
                showDiagramDomainTypes: false,
                ...overrides
            };
        }

        test('メニュー項目は入力インタフェース・出力インタフェース・ドメインモデルの3つ', () => {
            const usecase = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: []}]
            };
            const context = buildContext();

            const generator = UsecaseApp.createClassDiagramGenerator(usecase, null, () => context);
            const items = generator.buildExtraMenuItems(() => {});

            assert.deepEqual(items.map(i => i.label), ['入力インタフェース', '出力インタフェース', 'ドメインモデル']);
            assert.deepEqual(items.map(i => i.checked), [true, true, false]);
        });

        test('メニュー項目を選択するとこの図だけ表示要素が切り替わる（サイドバー側の値は変更しない）', () => {
            globalThis.inboundData = {
                inboundAdapters: [{relations: [{from: 'web.Ctrl#create()', to: 'pkg.ServiceA#method1()'}]}]
            };
            const usecase = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: []}]
            };
            const ctrlNodeId = globalThis.Jig.util.fqnToId("node", 'web.Ctrl');
            const context = buildContext();

            const generator = UsecaseApp.createClassDiagramGenerator(usecase, null, () => context);

            const before = generator();
            assert.ok(before.includes(ctrlNodeId), '切替前はinboundクラスノードを含む');

            let rerendered = false;
            const items = generator.buildExtraMenuItems(() => { rerendered = true; });
            items.find(i => i.label === '入力インタフェース').onSelect();

            assert.equal(rerendered, true, '選択時に再描画コールバックが呼ばれる');
            assert.equal(context.showDiagramInboundClasses, true, 'サイドバー（グローバル）側の値は変更されない');

            const after = generator();
            assert.ok(!after.includes(ctrlNodeId), '切替後はinboundクラスノードが消える');

            delete globalThis.inboundData;
        });
    });

    test.describe('buildPackageMethodGraph', () => {
        test('公開メソッドがclassFqn付きのusecaseノードとして追加され、パッケージ内の別クラスへの呼び出しはメソッド単位のエッジになる', () => {
            const usecaseA = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: ['pkg.ServiceB#method2()']}]
            };
            const usecaseB = {
                fqn: 'pkg.ServiceB',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceB#method2()', visibility: 'PUBLIC', callMethods: []}]
            };

            const result = UsecaseApp.buildPackageMethodGraph([usecaseA, usecaseB], {outboundOperationSet: new Set()});

            const nodeA = result.nodes.find(n => n.fqn === 'pkg.ServiceA#method1()');
            const nodeB = result.nodes.find(n => n.fqn === 'pkg.ServiceB#method2()');
            assert.ok(nodeA && nodeA.kind === 'usecase' && nodeA.classFqn === 'pkg.ServiceA');
            assert.ok(nodeB && nodeB.kind === 'usecase' && nodeB.classFqn === 'pkg.ServiceB');
            assert.ok(result.edges.find(e => e.from === 'pkg.ServiceA#method1()' && e.to === 'pkg.ServiceB#method2()'));
        });

        test('同一クラス内のメソッド呼び出しもエッジとして追加される（クラス単位版との違い）', () => {
            const usecaseA = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [
                    {fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: ['pkg.ServiceA#method2()']},
                    {fqn: 'pkg.ServiceA#method2()', visibility: 'PUBLIC', callMethods: []}
                ]
            };

            const result = UsecaseApp.buildPackageMethodGraph([usecaseA], {outboundOperationSet: new Set()});

            assert.ok(result.edges.find(e => e.from === 'pkg.ServiceA#method1()' && e.to === 'pkg.ServiceA#method2()'));
        });

        test('showDiagramOutboundPorts=trueかつoutboundOperationSetに含まれる呼び出しはoutbound-classノードとして追加される', () => {
            const usecaseA = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: ['ext.Repo#save()']}]
            };

            const result = UsecaseApp.buildPackageMethodGraph([usecaseA], {
                outboundOperationSet: new Set(['ext.Repo#save()']),
                showDiagramOutboundPorts: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'ext.Repo' && n.kind === 'outbound-class'));
            assert.ok(result.edges.find(e => e.from === 'pkg.ServiceA#method1()' && e.to === 'ext.Repo'));
        });

        test('showDiagramInboundClasses=trueの場合、外部クラスからの呼び出しはinbound-classから対象メソッドへのエッジになる', () => {
            globalThis.inboundData = {
                inboundAdapters: [{relations: [{from: 'web.Ctrl#create()', to: 'pkg.ServiceA#method1()'}]}]
            };
            const usecaseA = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: []}]
            };

            const result = UsecaseApp.buildPackageMethodGraph([usecaseA], {
                outboundOperationSet: new Set(),
                showDiagramInboundClasses: true
            });

            assert.ok(result.nodes.find(n => n.fqn === 'web.Ctrl' && n.kind === 'inbound-class'));
            assert.ok(result.edges.find(e => e.from === 'web.Ctrl' && e.to === 'pkg.ServiceA#method1()'));

            delete globalThis.inboundData;
        });
    });

    test.describe('createPackageDiagramGenerator', () => {
        function buildContext(overrides = {}) {
            return {
                outboundOperationSet: new Set(),
                showDiagramInboundClasses: true,
                showDiagramOutboundPorts: true,
                showDiagramDomainTypes: false,
                ...overrides
            };
        }

        test('メニュー項目は入力インタフェース・出力インタフェース・ドメインモデル・メソッド単位の4つで、メソッド単位は既定OFF', () => {
            const usecaseA = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: []}]
            };
            const context = buildContext();

            const generator = UsecaseApp.createPackageDiagramGenerator([usecaseA], () => context);
            const items = generator.buildExtraMenuItems(() => {});

            assert.deepEqual(items.map(i => i.label), ['入力インタフェース', '出力インタフェース', 'ドメインモデル', 'メソッド単位']);
            assert.deepEqual(items.map(i => i.checked), [true, true, false, false]);
        });

        test('既定（メソッド単位OFF）ではクラス単位ノードで、クラス間のエッジのみ表示される', () => {
            const usecaseA = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: ['pkg.ServiceB#method2()']}]
            };
            const usecaseB = {
                fqn: 'pkg.ServiceB',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceB#method2()', visibility: 'PUBLIC', callMethods: []}]
            };
            const context = buildContext();
            const classANodeId = globalThis.Jig.util.fqnToId("node", 'pkg.ServiceA');
            const methodANodeId = globalThis.Jig.util.fqnToId("node", 'pkg.ServiceA#method1()');
            const packageSubgraphId = Jig.util.fqnToId("sg", 'pkg');

            const generator = UsecaseApp.createPackageDiagramGenerator([usecaseA, usecaseB], () => context);
            const source = generator('LR', {});

            assert.ok(source.includes(classANodeId), 'クラス単位ノードを含む');
            assert.ok(!source.includes(methodANodeId), 'メソッド単位ノードは含まない');
            assert.ok(source.includes(`subgraph ${packageSubgraphId}`), 'パッケージがsubgraphの枠になる');
        });

        test('メソッド単位をONにするとクラスをsubgraphの枠にしてメソッドノードが配置される', () => {
            const usecaseA = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: ['pkg.ServiceB#method2()']}]
            };
            const usecaseB = {
                fqn: 'pkg.ServiceB',
                fields: [],
                staticMethods: [],
                methods: [{fqn: 'pkg.ServiceB#method2()', visibility: 'PUBLIC', callMethods: []}]
            };
            const context = buildContext();
            const classASubgraphId = Jig.util.fqnToId("sg", 'pkg.ServiceA');
            const methodANodeId = globalThis.Jig.util.fqnToId("node", 'pkg.ServiceA#method1()');
            const methodBNodeId = globalThis.Jig.util.fqnToId("node", 'pkg.ServiceB#method2()');

            const generator = UsecaseApp.createPackageDiagramGenerator([usecaseA, usecaseB], () => context);

            let rerendered = false;
            const items = generator.buildExtraMenuItems(() => { rerendered = true; });
            items.find(i => i.label === 'メソッド単位').onSelect();

            assert.equal(rerendered, true, '選択時に再描画コールバックが呼ばれる');
            assert.equal(context.showDiagramMethodLevel, undefined, 'サイドバー（グローバル）側の値は変更されない');

            const source = generator('LR', {});
            assert.ok(source.includes(`subgraph ${classASubgraphId}`), 'クラスがsubgraphの枠になる');
            assert.ok(source.includes(methodANodeId) && source.includes(methodBNodeId), 'メソッド単位ノードを含む');
        });

        test('1クラスのみのパッケージでは、メソッド単位表示とクラス単位のユースケース図が同じソースになる', () => {
            const usecaseA = {
                fqn: 'pkg.ServiceA',
                fields: [],
                staticMethods: [],
                methods: [
                    {fqn: 'pkg.ServiceA#method1()', visibility: 'PUBLIC', callMethods: ['pkg.ServiceA#method2()']},
                    {fqn: 'pkg.ServiceA#method2()', visibility: 'PUBLIC', callMethods: []}
                ]
            };
            const context = buildContext();

            const packageGenerator = UsecaseApp.createPackageDiagramGenerator([usecaseA], () => context);
            packageGenerator.buildExtraMenuItems(() => {}).find(i => i.label === 'メソッド単位').onSelect();
            const packageSource = packageGenerator('LR', {});

            const classGenerator = UsecaseApp.createClassDiagramGenerator(usecaseA, null, () => context);
            const classSource = classGenerator('LR', {});

            assert.equal(packageSource, classSource);
        });
    });
});
