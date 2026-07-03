const test = require('node:test');
const assert = require('node:assert/strict');
const {DocumentStub, EventStub, setGlossaryData} = require('./dom-stub.js');

// モック用のデータ
const mockInboundData = {
    inboundAdapters: [
        {
            fqn: "com.example.ControllerA",
            classPath: "/api",
            relations: [
                {from: "com.example.ControllerA#method1()", to: "com.example.ServiceA#serviceMethod()"}
            ],
            entrypoints: [
                {
                    fqn: "com.example.ControllerA#method1()",
                    visibility: "PUBLIC",
                    parameters: [],
                    returnTypeRef: {fqn: "void"},
                    isDeprecated: false,
                    entrypointType: "HTTP_API",
                    path: "GET /method1"
                }
            ]
        }
    ]
};

const mockUsecaseData = {
    usecases: [
        {
            fqn: "com.example.ServiceA",
            methods: [
                {fqn: "com.example.ServiceA#serviceMethod()"}
            ]
        }
    ]
};

const mockGlossaryData = {
    "com.example.ControllerA": {title: "ControllerA", description: "Description of ControllerA", kind: "クラス"},
    "com.example.ControllerA#method1()": {title: "method1", simpleText: "method1", kind: "メソッド", description: ""},
    "com.example.ServiceA": {title: "ServiceA", description: "", kind: "クラス"},
    "com.example.ServiceA#serviceMethod()": {
        title: "serviceMethod",
        simpleText: "serviceMethod",
        kind: "メソッド",
        description: ""
    }
};

test.describe('inbound.js', () => {
    let InboundApp;
    let doc;

    function createElement(tagName, options = {}) {
        const element = doc.createElement(tagName);
        if (options.className) element.className = options.className;
        if (options.id) element.id = options.id;
        if (options.textContent != null) element.textContent = options.textContent;
        if (options.innerHTML != null) element.innerHTML = options.innerHTML;
        if (options.attributes) {
            Object.entries(options.attributes).forEach(([name, value]) => {
                element.setAttribute(name, value);
            });
        }
        if (options.style) {
            Object.assign(element.style, options.style);
        }
        if (options.children) {
            options.children.forEach(child => {
                if (child) element.append(child);
            });
        }
        return element;
    }

    test.beforeEach(() => {
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-util.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-data.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-glossary.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-mermaid.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-dom.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-bootstrap.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/inbound.js')];

        doc = new DocumentStub();
        global.window = {
            addEventListener: () => {
            },
            innerHeight: 900
        };
        global.document = doc;
        ['inbound-sidebar-list', 'inbound-list'].forEach(id => {
            const el = doc.createElement('div');
            el.id = id;
            doc.body.appendChild(el);
        });
        // 表示設定パネル（inbound.htmlの実際の構造に合わせる）
        const settingsEl = doc.createElement('details');
        settingsEl.id = 'sidebar-settings';
        const panel = doc.createElement('div');
        panel.className = 'sidebar-settings-panel';
        const fieldset = doc.createElement('fieldset');
        fieldset.id = 'display-type-fieldset';
        panel.appendChild(fieldset);
        settingsEl.appendChild(panel);
        doc.body.appendChild(settingsEl);
        global.marked = {parse: (text) => text}; // markedのモック
        global.mermaid = {
            initialize: () => {
            }, run: () => {
            }
        }; // mermaidのモック

        // グローバルデータをクリア（テスト間での汚染防止）
        delete globalThis.inboundData;
        delete globalThis.usecaseData;
        delete globalThis.glossaryData;
        delete globalThis.Jig;

        require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        require('../../main/resources/templates/assets/jig-glossary.js');
        require('../../main/resources/templates/assets/jig-mermaid.js');
        require('../../main/resources/templates/assets/jig-dom.js');
        require('../../main/resources/templates/assets/jig-bootstrap.js');
        globalThis.Jig.i18n = {t: (key) => key};
        globalThis.Jig.mermaid.render.renderWithControls = (targetEl, source, {direction} = {}) => {
            const code = (typeof source === 'function') ? source(direction || 'LR') : source;
            const pre = createElement('pre', {
                className: 'mermaid',
                textContent: String(code ?? '')
            });
            targetEl.appendChild(pre);
        };
        InboundApp = require('../../main/resources/templates/assets/inbound.js');
    });

    test('init should render data from globalThis.inboundData', () => {
        globalThis.inboundData = mockInboundData;
        setGlossaryData(mockGlossaryData);
        globalThis.usecaseData = mockUsecaseData;
        InboundApp.init();

        const sidebar = document.getElementById('inbound-sidebar-list');
        assert.equal(sidebar.children.length, 2); // 固定リンクセクション + リクエストハンドラグループ
        assert.equal(sidebar.children[0].querySelector('a').textContent, 'エントリーポイント一覧');
        assert.equal(sidebar.children[0].querySelector('a').getAttribute('href'), '#entrypoint-summary');
        const group = sidebar.children[1];
        assert.ok(group.classList.has('in-page-sidebar__section--group'), 'エントリーポイント種別のグループセクション');
        assert.equal(group.querySelector('p span').textContent, 'リクエストハンドラ');
        // パッケージノードは省略・連結され、メインセクションへのリンクになる
        const packageLink = group.querySelector('.in-page-sidebar__package-link');
        assert.equal(packageLink.textContent, 'com.example');
        assert.ok(packageLink.getAttribute('href').startsWith('#'));
        assert.equal(group.querySelector('.in-page-sidebar__link').textContent, 'ControllerA');

        const mainList = document.getElementById('inbound-list');
        assert.equal(mainList.children.length, 2); // サマリーセクション + コントローラーセクション

        // サマリーカード（エントリーポイント一覧）
        const summaryCard = mainList.children[0];
        assert.ok(summaryCard.classList.has('entrypoint-summary-section'));
        assert.equal(summaryCard.querySelector('h3').textContent, 'エントリーポイント一覧');
        // リクエストハンドラのサブセクション
        assert.equal(summaryCard.querySelector('h4').textContent, 'リクエストハンドラ');
        const summaryTable = summaryCard.querySelector('table.entrypoint-summary');
        assert.ok(summaryTable);
        // Controller グループヘッダー行の確認
        const groupHeaders = summaryTable.querySelectorAll('tr.controller-group-header');
        assert.equal(groupHeaders.length, 1);
        assert.equal(groupHeaders[0].querySelector('a').textContent, 'ControllerA');
        assert.ok(groupHeaders[0].querySelector('a').getAttribute('href').startsWith('#'));
        assert.equal(groupHeaders[0].querySelector('.controller-group-path').textContent, '/api');
        assert.equal(groupHeaders[0].querySelector('.controller-group-count').textContent, '1');
        assert.ok(groupHeaders[0].querySelector('button.controller-group-toggle'));
        // データ行の確認（グループヘッダー行を除外）
        const rows = summaryTable.querySelectorAll('tbody tr:not(.controller-group-header)');
        assert.equal(rows.length, 1);
        const cells = rows[0].children;
        assert.equal(cells[0].textContent, '/api/method1'); // クラスパス+メソッドパス
        assert.equal(cells[1].textContent, 'GET');
        const link = cells[2].querySelector('a');
        assert.equal(link.textContent, 'method1'); // Controller名なしのメソッド名のみ
        assert.ok(link.getAttribute('href').startsWith('#'));

        const controllerSection = mainList.children[1];
        assert.equal(controllerSection.id, globalThis.Jig.util.fqnToId("adapter", 'com.example.ControllerA'));
        assert.equal(controllerSection.querySelector('h3 span').textContent, 'ControllerA');
        assert.equal(controllerSection.querySelector('.fully-qualified-name').textContent, 'com.example.ControllerA');
        assert.equal(controllerSection.querySelector('.class-path').textContent, '/api');
        assert.equal(controllerSection.querySelector('.markdown').innerHTML, 'Description of ControllerA');

        // エントリーポイント一覧（buildEntrypointItem）
        const epSection = controllerSection.querySelector('section.jig-card--item');
        assert.ok(epSection);
        const epItems = epSection.querySelectorAll('.entrypoint-item');
        assert.equal(epItems.length, 1);
        assert.ok(epItems[0].querySelector('.entrypoint-item__name').textContent.includes('method1'));
        assert.equal(epItems[0].querySelector('.entrypoint-item__path').textContent, 'GET /method1');
        assert.equal(epItems[0].querySelector('.entrypoint-item__empty').textContent, '-'); // 引数なし
        const ioDds = epItems[0].querySelectorAll('.entrypoint-item__io dd');
        assert.ok(ioDds[ioDds.length - 1]?.querySelector('span')?.textContent); // 出力あり

        // コントローラー単位の統合ダイアグラム
        const mermaidPre = controllerSection.querySelector('.mermaid');
        assert.ok(mermaidPre);
        const mermaidCode = mermaidPre.textContent;
        assert.ok(mermaidCode.includes('subgraph')); // Mermaid code generated
        assert.ok(mermaidCode.includes('GET /method1')); // パスノードが含まれる
        assert.ok(mermaidCode.includes('classDef'), 'Theme classDefが含まれるべき');
    });

    test('renderMain should handle empty data', () => {
        globalThis.inboundData = {inboundAdapters: []};
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        assert.equal(mainList.textContent, 'データなし');
    });

    test('adapter chain: 浅いノードから外部へのエッジは長くなる', () => {
        // method1 → internalMethod → serviceMethod (depth=2)
        // method2 → serviceMethod2 (depth=1, maxDepth=2 なので ---> になる)
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.ControllerA",
                classPath: "/api",
                relations: [
                    {from: "com.example.ControllerA#method1()", to: "com.example.ControllerA#internalMethod()"},
                    {from: "com.example.ControllerA#internalMethod()", to: "com.example.ServiceA#serviceMethod()"},
                    {from: "com.example.ControllerA#method2()", to: "com.example.ServiceB#serviceMethod2()"}
                ],
                entrypoints: [
                    {
                        fqn: "com.example.ControllerA#method1()",
                        visibility: "PUBLIC",
                        parameters: [],
                        returnTypeRef: {fqn: "void"},
                        isDeprecated: false,
                        entrypointType: "HTTP_API",
                        path: "GET /method1"
                    },
                    {
                        fqn: "com.example.ControllerA#method2()",
                        visibility: "PUBLIC",
                        parameters: [],
                        returnTypeRef: {fqn: "void"},
                        isDeprecated: false,
                        entrypointType: "HTTP_API",
                        path: "GET /method2"
                    }
                ]
            }]
        };
        setGlossaryData({
            "com.example.ControllerA": {title: "ControllerA", description: "", kind: "クラス"},
            "com.example.ControllerA#method1()": {
                title: "method1",
                simpleText: "method1",
                kind: "メソッド",
                description: ""
            },
            "com.example.ControllerA#method2()": {
                title: "method2",
                simpleText: "method2",
                kind: "メソッド",
                description: ""
            },
            "com.example.ControllerA#internalMethod()": {
                title: "internalMethod",
                simpleText: "internalMethod",
                kind: "メソッド",
                description: ""
            },
            "com.example.ServiceA": {title: "ServiceA", description: "", kind: "クラス"},
            "com.example.ServiceA#serviceMethod()": {
                title: "serviceMethod",
                simpleText: "serviceMethod",
                kind: "メソッド",
                description: ""
            },
            "com.example.ServiceB": {title: "ServiceB", description: "", kind: "クラス"},
            "com.example.ServiceB#serviceMethod2()": {
                title: "serviceMethod2",
                simpleText: "serviceMethod2",
                kind: "メソッド",
                description: ""
            }
        });
        globalThis.usecaseData = {
            usecases: [
                {fqn: "com.example.ServiceA", methods: [{fqn: "com.example.ServiceA#serviceMethod()"}]},
                {fqn: "com.example.ServiceB", methods: [{fqn: "com.example.ServiceB#serviceMethod2()"}]}
            ]
        };
        InboundApp.init();

        const mermaidPre = document.getElementById('inbound-list').children[1].querySelector('.mermaid');
        assert.ok(mermaidPre);
        const mermaidCode = mermaidPre.textContent;
        // method2 (depth=1, maxDepth=2) からのエッジは ---> になる
        assert.ok(mermaidCode.includes('--->'), `Expected ---> in: ${mermaidCode}`);
    });

    test('init should work without usecaseData', () => {
        globalThis.inboundData = mockInboundData;
        setGlossaryData(mockGlossaryData);
        delete globalThis.usecaseData;
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        const mermaidPre = mainList.children[1].querySelector('.mermaid');
        assert.ok(mermaidPre);
        // entrypointおよびmethodのsubgraphが生成される
        assert.ok(mermaidPre.textContent.includes('subgraph'));
        // usecase.htmlへのclickリンクは生成されない
        assert.ok(!mermaidPre.textContent.includes('usecase.html'));
        assert.ok(mermaidPre.textContent.includes('GET /method1'));
    });

    test('リクエストハンドラのフィルター入力でパス部分一致絞り込みができる', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.ControllerA",
                classPath: "/api",
                relations: [],
                entrypoints: [
                    {fqn: "com.example.ControllerA#methodA()", entrypointType: "HTTP_API", path: "GET /users", parameters: [], returnTypeRef: {fqn: "void"}},
                    {fqn: "com.example.ControllerA#methodB()", entrypointType: "HTTP_API", path: "GET /orders", parameters: [], returnTypeRef: {fqn: "void"}}
                ]
            }]
        };
        setGlossaryData({
            "com.example.ControllerA": {title: "ControllerA", description: "", kind: "クラス"},
            "com.example.ControllerA#methodA()": {title: "methodA", simpleText: "methodA", kind: "メソッド", description: ""},
            "com.example.ControllerA#methodB()": {title: "methodB", simpleText: "methodB", kind: "メソッド", description: ""}
        });
        InboundApp.init();

        const filterInput = document.getElementById('inbound-list').querySelector('input.entrypoint-filter');
        assert.ok(filterInput, 'フィルター入力欄が存在する');

        const summaryTable = document.getElementById('inbound-list').querySelector('table.entrypoint-summary');
        const dataRows = summaryTable.querySelectorAll('tbody tr:not(.controller-group-header)');
        assert.equal(dataRows.length, 2);

        filterInput.value = 'user';
        filterInput.dispatchEvent(new EventStub('input'));

        // パス昇順ソート: /api/orders → /api/users
        assert.equal(dataRows[0].style.display, 'none', '/api/orders はマッチしないので非表示');
        assert.equal(dataRows[1].style.display, '', '/api/users はマッチするので表示');

        filterInput.value = '';
        filterInput.dispatchEvent(new EventStub('input'));
        assert.equal(dataRows[0].style.display, '', 'クリアすると全行表示');
    });

    test('フィルターで全データ行が非表示になったグループのヘッダー行も非表示になる', () => {
        globalThis.inboundData = {
            inboundAdapters: [
                {
                    fqn: "com.example.OrderController",
                    classPath: "/orders",
                    relations: [],
                    entrypoints: [
                        {fqn: "com.example.OrderController#list()", entrypointType: "HTTP_API", path: "GET /", parameters: [], returnTypeRef: {fqn: "void"}}
                    ]
                },
                {
                    fqn: "com.example.UserController",
                    classPath: "/users",
                    relations: [],
                    entrypoints: [
                        {fqn: "com.example.UserController#list()", entrypointType: "HTTP_API", path: "GET /", parameters: [], returnTypeRef: {fqn: "void"}}
                    ]
                }
            ]
        };
        setGlossaryData({
            "com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"},
            "com.example.OrderController#list()": {title: "list", simpleText: "list", kind: "メソッド", description: ""},
            "com.example.UserController": {title: "UserController", description: "", kind: "クラス"},
            "com.example.UserController#list()": {title: "list", simpleText: "list", kind: "メソッド", description: ""}
        });
        InboundApp.init();

        const summaryTable = document.getElementById('inbound-list').querySelector('table.entrypoint-summary--http');
        const filterInput = document.getElementById('inbound-list').querySelector('input.entrypoint-filter');
        const groupHeaders = summaryTable.querySelectorAll('tr.controller-group-header');
        assert.equal(groupHeaders.length, 2);

        // /users にマッチ → OrderController の全データ行が非表示になる
        filterInput.value = 'users';
        filterInput.dispatchEvent(new EventStub('input'));

        // OrderController (/orders/) のヘッダーは非表示
        const orderHeader = Array.from(groupHeaders).find(h => h.querySelector('a').textContent === 'OrderController');
        assert.equal(orderHeader.style.display, 'none', '全行非表示のグループヘッダーも非表示');

        // UserController (/users/) のヘッダーは表示
        const userHeader = Array.from(groupHeaders).find(h => h.querySelector('a').textContent === 'UserController');
        assert.equal(userHeader.style.display, '', '一致行があるグループヘッダーは表示');

        // フィルタークリアで全ヘッダーが復元
        filterInput.value = '';
        filterInput.dispatchEvent(new EventStub('input'));
        assert.equal(orderHeader.style.display, '', 'クリアするとヘッダーも復元');
    });

    test('複数ControllerのHTTP_APIはController単位でグループ化される', () => {
        globalThis.inboundData = {
            inboundAdapters: [
                {
                    fqn: "com.example.OrderController",
                    classPath: "/orders",
                    relations: [],
                    entrypoints: [
                        {fqn: "com.example.OrderController#list()", entrypointType: "HTTP_API", path: "GET /", parameters: [], returnTypeRef: {fqn: "void"}},
                        {fqn: "com.example.OrderController#get()", entrypointType: "HTTP_API", path: "GET /{id}", parameters: [], returnTypeRef: {fqn: "void"}}
                    ]
                },
                {
                    fqn: "com.example.UserController",
                    classPath: "/users",
                    relations: [],
                    entrypoints: [
                        {fqn: "com.example.UserController#list()", entrypointType: "HTTP_API", path: "GET /", parameters: [], returnTypeRef: {fqn: "void"}}
                    ]
                }
            ]
        };
        setGlossaryData({
            "com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"},
            "com.example.OrderController#list()": {title: "list", simpleText: "list", kind: "メソッド", description: ""},
            "com.example.OrderController#get()": {title: "get", simpleText: "get", kind: "メソッド", description: ""},
            "com.example.UserController": {title: "UserController", description: "", kind: "クラス"},
            "com.example.UserController#list()": {title: "list", simpleText: "list", kind: "メソッド", description: ""}
        });
        InboundApp.init();

        const summaryTable = document.getElementById('inbound-list').querySelector('table.entrypoint-summary--http');
        assert.ok(summaryTable, 'テーブルが存在する');

        const groupHeaders = summaryTable.querySelectorAll('tr.controller-group-header');
        assert.equal(groupHeaders.length, 2, 'Controller数分のグループヘッダーが存在する');

        const headerLabels = Array.from(groupHeaders).map(h => h.querySelector('a').textContent);
        assert.ok(headerLabels.includes('OrderController'), 'OrderControllerのヘッダーが存在する');
        assert.ok(headerLabels.includes('UserController'), 'UserControllerのヘッダーが存在する');

        const dataRows = summaryTable.querySelectorAll('tbody tr:not(.controller-group-header)');
        assert.equal(dataRows.length, 3, 'データ行は全エントリーポイント数分');

        // tbody が2つ存在し、それぞれにデータ行がグループ化されている
        const tbodies = summaryTable.querySelectorAll('tbody');
        assert.equal(tbodies.length, 2, 'tbody がController数分存在する');
    });

    test('Controller グループのトグルボタンでデータ行を開閉できる', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.ControllerA",
                classPath: "/api",
                relations: [],
                entrypoints: [
                    {fqn: "com.example.ControllerA#methodA()", entrypointType: "HTTP_API", path: "GET /a", parameters: [], returnTypeRef: {fqn: "void"}},
                    {fqn: "com.example.ControllerA#methodB()", entrypointType: "HTTP_API", path: "GET /b", parameters: [], returnTypeRef: {fqn: "void"}}
                ]
            }]
        };
        setGlossaryData({
            "com.example.ControllerA": {title: "ControllerA", description: "", kind: "クラス"},
            "com.example.ControllerA#methodA()": {title: "methodA", simpleText: "methodA", kind: "メソッド", description: ""},
            "com.example.ControllerA#methodB()": {title: "methodB", simpleText: "methodB", kind: "メソッド", description: ""}
        });
        InboundApp.init();

        const summaryTable = document.getElementById('inbound-list').querySelector('table.entrypoint-summary--http');
        const toggleBtn = summaryTable.querySelector('.controller-group-toggle');
        assert.ok(toggleBtn, 'トグルボタンが存在する');
        assert.equal(toggleBtn.getAttribute('aria-expanded'), 'true', '初期状態は展開');

        const dataRows = summaryTable.querySelectorAll('tbody tr:not(.controller-group-header)');
        assert.equal(dataRows.length, 2);
        dataRows.forEach(tr => assert.ok(!tr.classList.has('hidden'), '初期状態はhiddenクラスなし'));

        // 折りたたむ
        toggleBtn.dispatchEvent(new EventStub('click'));
        assert.equal(toggleBtn.getAttribute('aria-expanded'), 'false', 'クリック後はaria-expanded=false');
        assert.equal(toggleBtn.getAttribute('aria-label'), '展開', 'ラベルが「展開」に変わる');
        dataRows.forEach(tr => assert.ok(tr.classList.has('hidden'), '折りたたみ後はhiddenクラスあり'));

        // 展開
        toggleBtn.dispatchEvent(new EventStub('click'));
        assert.equal(toggleBtn.getAttribute('aria-expanded'), 'true', '再クリックでaria-expanded=true');
        assert.equal(toggleBtn.getAttribute('aria-label'), '折りたたむ', 'ラベルが「折りたたむ」に戻る');
        dataRows.forEach(tr => assert.ok(!tr.classList.has('hidden'), '展開後はhiddenクラスなし'));
    });

    test('複数Controllerがある場合に一括開閉ボタンが表示される', () => {
        globalThis.inboundData = {
            inboundAdapters: [
                {
                    fqn: "com.example.OrderController",
                    classPath: "/orders", relations: [],
                    entrypoints: [{fqn: "com.example.OrderController#list()", entrypointType: "HTTP_API", path: "GET /", parameters: [], returnTypeRef: {fqn: "void"}}]
                },
                {
                    fqn: "com.example.UserController",
                    classPath: "/users", relations: [],
                    entrypoints: [{fqn: "com.example.UserController#list()", entrypointType: "HTTP_API", path: "GET /", parameters: [], returnTypeRef: {fqn: "void"}}]
                }
            ]
        };
        setGlossaryData({
            "com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"},
            "com.example.OrderController#list()": {title: "list", simpleText: "list", kind: "メソッド", description: ""},
            "com.example.UserController": {title: "UserController", description: "", kind: "クラス"},
            "com.example.UserController#list()": {title: "list", simpleText: "list", kind: "メソッド", description: ""}
        });
        InboundApp.init();

        const summaryCard = document.getElementById('inbound-list').querySelector('.entrypoint-summary-section');
        const allToggleBtn = summaryCard.querySelector('.controller-group-toggle-all');
        assert.ok(allToggleBtn, '一括開閉ボタンが存在する');
        assert.equal(allToggleBtn.getAttribute('aria-expanded'), 'true', '初期状態は展開');
        assert.equal(allToggleBtn.querySelector('span').textContent, '全て折りたたむ');

        const summaryTable = summaryCard.querySelector('table.entrypoint-summary--http');
        const dataRows = summaryTable.querySelectorAll('tbody tr:not(.controller-group-header)');
        const individualToggles = summaryTable.querySelectorAll('.controller-group-toggle');

        // 一括折りたたみ
        allToggleBtn.dispatchEvent(new EventStub('click'));
        assert.equal(allToggleBtn.getAttribute('aria-expanded'), 'false');
        assert.equal(allToggleBtn.querySelector('span').textContent, '全て展開');
        dataRows.forEach(tr => assert.ok(tr.classList.has('hidden'), '全データ行が折りたたまれる'));
        individualToggles.forEach(btn => assert.equal(btn.getAttribute('aria-expanded'), 'false', '個別トグルも同期される'));

        // 一括展開
        allToggleBtn.dispatchEvent(new EventStub('click'));
        assert.equal(allToggleBtn.getAttribute('aria-expanded'), 'true');
        assert.equal(allToggleBtn.querySelector('span').textContent, '全て折りたたむ');
        dataRows.forEach(tr => assert.ok(!tr.classList.has('hidden'), '全データ行が展開される'));
        individualToggles.forEach(btn => assert.equal(btn.getAttribute('aria-expanded'), 'true', '個別トグルも同期される'));
    });

    test('Controllerが1件の場合は一括開閉ボタンが表示されない', () => {
        globalThis.inboundData = mockInboundData;
        setGlossaryData(mockGlossaryData);
        InboundApp.init();

        const summaryCard = document.getElementById('inbound-list').querySelector('.entrypoint-summary-section');
        assert.ok(!summaryCard.querySelector('.controller-group-toggle-all'), '一括開閉ボタンは表示されない');
    });

    test('エントリーポイント数が10を超える場合は初期表示で折りたたまれる', () => {
        const entrypoints = Array.from({length: 11}, (_, i) => ({
            fqn: `com.example.ControllerA#method${i}()`,
            entrypointType: "HTTP_API",
            path: `GET /method${i}`,
            parameters: [],
            returnTypeRef: {fqn: "void"}
        }));
        globalThis.inboundData = {
            inboundAdapters: [{fqn: "com.example.ControllerA", classPath: "/api", relations: [], entrypoints}]
        };
        const glossary = {"com.example.ControllerA": {title: "ControllerA", description: "", kind: "クラス"}};
        entrypoints.forEach(ep => { glossary[ep.fqn] = {title: ep.fqn.split('#')[1], simpleText: ep.fqn.split('#')[1], kind: "メソッド", description: ""}; });
        setGlossaryData(glossary);
        InboundApp.init();

        const summaryTable = document.getElementById('inbound-list').querySelector('table.entrypoint-summary--http');
        const dataRows = summaryTable.querySelectorAll('tbody tr:not(.controller-group-header)');
        assert.equal(dataRows.length, 11);
        dataRows.forEach(tr => assert.ok(tr.classList.has('hidden'), '初期表示で折りたたまれている'));

        const toggleBtn = summaryTable.querySelector('.controller-group-toggle');
        assert.equal(toggleBtn.getAttribute('aria-expanded'), 'false', '初期状態は折りたたみ');
        assert.equal(toggleBtn.getAttribute('aria-label'), '展開');
    });

    test('エントリーポイント数が10以下の場合は初期表示で展開される', () => {
        const entrypoints = Array.from({length: 10}, (_, i) => ({
            fqn: `com.example.ControllerA#method${i}()`,
            entrypointType: "HTTP_API",
            path: `GET /method${i}`,
            parameters: [],
            returnTypeRef: {fqn: "void"}
        }));
        globalThis.inboundData = {
            inboundAdapters: [{fqn: "com.example.ControllerA", classPath: "/api", relations: [], entrypoints}]
        };
        const glossary = {"com.example.ControllerA": {title: "ControllerA", description: "", kind: "クラス"}};
        entrypoints.forEach(ep => { glossary[ep.fqn] = {title: ep.fqn.split('#')[1], simpleText: ep.fqn.split('#')[1], kind: "メソッド", description: ""}; });
        setGlossaryData(glossary);
        InboundApp.init();

        const summaryTable = document.getElementById('inbound-list').querySelector('table.entrypoint-summary--http');
        const dataRows = summaryTable.querySelectorAll('tbody tr:not(.controller-group-header)');
        dataRows.forEach(tr => assert.ok(!tr.classList.has('hidden'), '初期表示で展開されている'));

        const toggleBtn = summaryTable.querySelector('.controller-group-toggle');
        assert.equal(toggleBtn.getAttribute('aria-expanded'), 'true', '初期状態は展開');
    });

    test('エントリーポイント数が10を超える場合、複数グループある時の一括ボタンは「全て展開」になる', () => {
        const makeEntrypoints = (prefix, n) => Array.from({length: n}, (_, i) => ({
            fqn: `com.example.${prefix}#method${i}()`,
            entrypointType: "HTTP_API",
            path: `GET /${prefix.toLowerCase()}/${i}`,
            parameters: [],
            returnTypeRef: {fqn: "void"}
        }));
        const ep1 = makeEntrypoints("ControllerA", 6);
        const ep2 = makeEntrypoints("ControllerB", 6);
        globalThis.inboundData = {
            inboundAdapters: [
                {fqn: "com.example.ControllerA", classPath: "/a", relations: [], entrypoints: ep1},
                {fqn: "com.example.ControllerB", classPath: "/b", relations: [], entrypoints: ep2}
            ]
        };
        const glossary = {
            "com.example.ControllerA": {title: "ControllerA", description: "", kind: "クラス"},
            "com.example.ControllerB": {title: "ControllerB", description: "", kind: "クラス"}
        };
        [...ep1, ...ep2].forEach(ep => { glossary[ep.fqn] = {title: ep.fqn.split('#')[1], simpleText: ep.fqn.split('#')[1], kind: "メソッド", description: ""}; });
        setGlossaryData(glossary);
        InboundApp.init();

        const summaryCard = document.getElementById('inbound-list').querySelector('.entrypoint-summary-section');
        const allToggleBtn = summaryCard.querySelector('.controller-group-toggle-all');
        assert.equal(allToggleBtn.getAttribute('aria-expanded'), 'false', '初期状態は折りたたみ');
        assert.equal(allToggleBtn.querySelector('span').textContent, '全て展開');
    });

    test('QUEUE_LISTENERもController単位でグループ化される', () => {
        globalThis.inboundData = {
            inboundAdapters: [
                {
                    fqn: "com.example.OrderListener",
                    classPath: "",
                    relations: [],
                    entrypoints: [
                        {fqn: "com.example.OrderListener#onOrder()", entrypointType: "QUEUE_LISTENER", path: "order-queue", parameters: [], returnTypeRef: {fqn: "void"}},
                        {fqn: "com.example.OrderListener#onCancel()", entrypointType: "QUEUE_LISTENER", path: "cancel-queue", parameters: [], returnTypeRef: {fqn: "void"}}
                    ]
                },
                {
                    fqn: "com.example.UserListener",
                    classPath: "",
                    relations: [],
                    entrypoints: [
                        {fqn: "com.example.UserListener#onUser()", entrypointType: "QUEUE_LISTENER", path: "user-queue", parameters: [], returnTypeRef: {fqn: "void"}}
                    ]
                }
            ]
        };
        setGlossaryData({
            "com.example.OrderListener": {title: "OrderListener", description: "", kind: "クラス"},
            "com.example.OrderListener#onOrder()": {title: "onOrder", simpleText: "onOrder", kind: "メソッド", description: ""},
            "com.example.OrderListener#onCancel()": {title: "onCancel", simpleText: "onCancel", kind: "メソッド", description: ""},
            "com.example.UserListener": {title: "UserListener", description: "", kind: "クラス"},
            "com.example.UserListener#onUser()": {title: "onUser", simpleText: "onUser", kind: "メソッド", description: ""}
        });
        InboundApp.init();

        const summaryCard = document.getElementById('inbound-list').querySelector('.entrypoint-summary-section');
        // メッセージリスナーのサブセクション
        const listenerSection = Array.from(summaryCard.querySelectorAll('.jig-card--item'))
            .find(el => el.querySelector('h4')?.textContent === 'メッセージリスナー');
        assert.ok(listenerSection, 'メッセージリスナーセクションが存在する');

        const groupHeaders = listenerSection.querySelectorAll('tr.controller-group-header');
        assert.equal(groupHeaders.length, 2, 'Listener数分のグループヘッダーが存在する');

        const headerLabels = Array.from(groupHeaders).map(h => h.querySelector('a').textContent);
        assert.ok(headerLabels.includes('OrderListener'));
        assert.ok(headerLabels.includes('UserListener'));

        // エントリーポイント列はメソッド名のみ
        const dataRows = listenerSection.querySelectorAll('tbody tr:not(.controller-group-header)');
        assert.equal(dataRows.length, 3);
        assert.equal(dataRows[0].children[1].querySelector('a').textContent, 'onOrder');

        // 件数バッジ
        const orderHeader = Array.from(groupHeaders).find(h => h.querySelector('a').textContent === 'OrderListener');
        assert.equal(orderHeader.querySelector('.controller-group-count').textContent, '2');
    });

    test('簡略表示チェックボックスで入出力セクションを非表示にできる', () => {
        globalThis.inboundData = mockInboundData;
        setGlossaryData(mockGlossaryData);
        globalThis.usecaseData = mockUsecaseData;
        InboundApp.init();

        // 初期状態では入出力セクションが表示されている
        const mainList = document.getElementById('inbound-list');
        const epSection = mainList.querySelector('.entrypoint-section');
        assert.ok(epSection, '初期状態ではエントリーポイントセクションが存在する');
        assert.ok(!epSection.classList.has('entrypoint-section--simplified'), '初期状態では簡略表示クラスなし');
        assert.ok(mainList.querySelector('.entrypoint-item__io'), '初期状態では入出力セクションが存在する');

        // 簡略表示チェックボックスを取得してオン
        const checkbox = document.getElementById('simplified-toggle');
        assert.ok(checkbox, '簡略表示チェックボックスが存在する');
        checkbox._checked = true;
        checkbox.dispatchEvent(new EventStub('change'));

        const epSectionSimplified = mainList.querySelector('.entrypoint-section');
        assert.ok(epSectionSimplified.classList.has('entrypoint-section--simplified'), '簡略表示オン: epSectionに簡略表示クラスが付与される');
        assert.ok(mainList.querySelector('.entrypoint-item__name'), '簡略表示オン: 名称は表示されている');
        assert.ok(mainList.querySelector('.entrypoint-item__path'), '簡略表示オン: パスは表示されている');

        // チェックボックスをオフに戻す
        checkbox._checked = false;
        checkbox.dispatchEvent(new EventStub('change'));

        const epSectionFull = mainList.querySelector('.entrypoint-section');
        assert.ok(!epSectionFull.classList.has('entrypoint-section--simplified'), '簡略表示オフ: 簡略表示クラスが削除される');
    });

    test('各エントリーポイントカードの簡略表示トグルボタンでカード単位の表示切り替えができる', () => {
        globalThis.inboundData = mockInboundData;
        setGlossaryData(mockGlossaryData);
        globalThis.usecaseData = mockUsecaseData;
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        const epSection = mainList.querySelector('.entrypoint-section');
        assert.ok(epSection, 'エントリーポイントセクションが存在する');

        const toggleBtn = epSection.querySelector('.simplified-toggle-btn');
        assert.ok(toggleBtn, 'カード内にトグルボタンが存在する');
        assert.equal(toggleBtn.getAttribute('aria-pressed'), 'false', '初期状態はaria-pressed=false');

        // 簡略表示にする
        toggleBtn.dispatchEvent(new EventStub('click'));
        assert.equal(toggleBtn.getAttribute('aria-pressed'), 'true', 'クリック後はaria-pressed=true');
        assert.ok(epSection.classList.has('entrypoint-section--simplified'), 'epSectionに簡略表示クラスが付与される');

        // 詳細表示に戻す
        toggleBtn.dispatchEvent(new EventStub('click'));
        assert.equal(toggleBtn.getAttribute('aria-pressed'), 'false', '再クリックでaria-pressed=false');
        assert.ok(!epSection.classList.has('entrypoint-section--simplified'), 'epSectionの簡略表示クラスが削除される');
    });

    test('init再呼び出し時に簡略表示チェックボックスはリセットされリスナーは重複しない', () => {
        globalThis.inboundData = mockInboundData;
        setGlossaryData(mockGlossaryData);
        globalThis.usecaseData = mockUsecaseData;
        InboundApp.init();

        const checkbox = document.getElementById('simplified-toggle');
        assert.ok(checkbox);
        checkbox._checked = true;
        checkbox.dispatchEvent(new EventStub('change'));
        const epSectionSimplified = document.getElementById('inbound-list').querySelector('.entrypoint-section');
        assert.ok(epSectionSimplified.classList.has('entrypoint-section--simplified'), '簡略表示オン');

        // init再呼び出し
        InboundApp.init();

        assert.ok(document.getElementById('simplified-toggle'), 'チェックボックスは存在する');
        assert.equal(document.getElementById('simplified-toggle').checked, false, 'チェックはリセットされる');
        const epSectionAfter = document.getElementById('inbound-list').querySelector('.entrypoint-section');
        assert.ok(!epSectionAfter.classList.has('entrypoint-section--simplified'), '簡略表示クラスがリセットされる');
    });

    test('エントリーポイント種別が1種類以下の場合は表示設定パネルを非表示にする', () => {
        globalThis.inboundData = mockInboundData; // HTTP_APIのみ
        setGlossaryData(mockGlossaryData);
        InboundApp.init();

        const fieldset = document.getElementById('display-type-fieldset');
        assert.equal(fieldset.style.display, 'none');
        assert.equal(document.querySelectorAll('input[name="display-type"]').length, 0);
    });

    test('表示設定ラジオボタンで絞り込むとサマリー・カード・サイドバーが絞り込まれる', () => {
        globalThis.inboundData = {
            inboundAdapters: [
                {
                    fqn: "com.example.HttpController",
                    classPath: "/api", relations: [],
                    entrypoints: [{fqn: "com.example.HttpController#get()", entrypointType: "HTTP_API", path: "GET /items", parameters: [], returnTypeRef: {fqn: "void"}}]
                },
                {
                    fqn: "com.example.QueueListener",
                    classPath: "", relations: [],
                    entrypoints: [{fqn: "com.example.QueueListener#onMessage()", entrypointType: "QUEUE_LISTENER", path: "my-queue", parameters: [], returnTypeRef: {fqn: "void"}}]
                }
            ]
        };
        setGlossaryData({
            "com.example.HttpController": {title: "HttpController", description: "", kind: "クラス"},
            "com.example.HttpController#get()": {title: "get", simpleText: "get", kind: "メソッド", description: ""},
            "com.example.QueueListener": {title: "QueueListener", description: "", kind: "クラス"},
            "com.example.QueueListener#onMessage()": {title: "onMessage", simpleText: "onMessage", kind: "メソッド", description: ""}
        });
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        assert.equal(mainList.children.length, 3); // サマリー(2タイプ) + カード2枚

        // HTTP_API のみに絞り込む（動的生成されたラジオボタンを取得）
        const radios = document.querySelectorAll('input[name="display-type"]');
        const radioHttp = Array.from(radios).find(r => r.value === 'HTTP_API');
        assert.ok(radioHttp, 'HTTP_APIラジオボタンが動的生成されている');
        radioHttp._checked = true;
        radioHttp.dispatchEvent(new EventStub('change'));

        // サマリーカードは1セクション（リクエストハンドラのみ）
        const summaryCard = mainList.children[0];
        assert.equal(summaryCard.querySelectorAll('h4').length, 1);
        assert.equal(summaryCard.querySelector('h4').textContent, 'リクエストハンドラ');

        // カードは1枚（HttpController のみ）
        assert.equal(mainList.children.length, 2); // サマリー + HttpController カード

        // サイドバーには HttpController のリンクのみ表示
        const sidebar = document.getElementById('inbound-sidebar-list');
        const adapterLinks = Array.from(sidebar.querySelectorAll('a.in-page-sidebar__link'))
            .filter(a => !a.getAttribute('href').match(/^#(entrypoint-summary|io-types)$/));
        assert.deepEqual(adapterLinks.map(a => a.textContent), ['HttpController'], 'HttpControllerのみ表示');
        // グループもリクエストハンドラのみ表示
        const groupTitles = sidebar.querySelectorAll('.in-page-sidebar__section--group p span');
        assert.deepEqual(groupTitles.map(el => el.textContent), ['リクエストハンドラ']);
    });

    test('サイドバーはエントリーポイント種別ごとのグループに分かれ、複数種別を持つアダプターは両方に現れる', () => {
        globalThis.inboundData = {
            inboundAdapters: [
                {
                    fqn: "com.example.MixedAdapter",
                    classPath: "", relations: [],
                    entrypoints: [
                        {fqn: "com.example.MixedAdapter#get()", entrypointType: "HTTP_API", path: "GET /items", parameters: [], returnTypeRef: {fqn: "void"}},
                        {fqn: "com.example.MixedAdapter#onMessage()", entrypointType: "QUEUE_LISTENER", path: "my-queue", parameters: [], returnTypeRef: {fqn: "void"}}
                    ]
                },
                {
                    fqn: "com.example.Scheduler",
                    classPath: "", relations: [],
                    entrypoints: [{fqn: "com.example.Scheduler#run()", entrypointType: "SCHEDULER", path: "cron", parameters: [], returnTypeRef: {fqn: "void"}}]
                }
            ]
        };
        setGlossaryData({});
        InboundApp.init();

        const sidebar = document.getElementById('inbound-sidebar-list');
        const groups = sidebar.querySelectorAll('.in-page-sidebar__section--group');
        assert.deepEqual(
            groups.map(g => g.querySelector('p span').textContent),
            ['リクエストハンドラ', 'メッセージリスナー', 'スケジューラー']
        );
        assert.equal(groups[0].querySelector('.in-page-sidebar__link').textContent, 'MixedAdapter');
        assert.equal(groups[1].querySelector('.in-page-sidebar__link').textContent, 'MixedAdapter');
        assert.equal(groups[2].querySelector('.in-page-sidebar__link').textContent, 'Scheduler');
        // パッケージノードはグループ内の最初のアダプターカードへリンクする
        assert.equal(
            groups[2].querySelector('.in-page-sidebar__package-link').getAttribute('href'),
            '#' + Jig.util.fqnToId('adapter', 'com.example.Scheduler')
        );
    });

    test('ioTypesがある場合はルート型カードが描画されフィールドを展開する', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.OrderController",
                classPath: "/api", relations: [],
                entrypoints: [{
                    fqn: "com.example.OrderController#order()",
                    entrypointType: "HTTP_API",
                    path: "GET /order",
                    parameters: [],
                    returnTypeRef: {fqn: "com.example.OrderItem"}
                }]
            }],
            ioTypes: [
                {fqn: "com.example.OrderItem", fields: [{name: "id", typeRef: {fqn: "java.lang.Long"}, isDeprecated: false}], isDeprecated: false}
            ],
            rootIoTypeFqns: ["com.example.OrderItem"]
        };
        setGlossaryData({
            "com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"},
            "com.example.OrderController#order()": {title: "order", simpleText: "order", kind: "メソッド", description: ""},
            "com.example.OrderItem": {title: "OrderItem", description: "", kind: "クラス"}
        });
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        const ioTypesSection = mainList.querySelector('#io-types');
        assert.ok(ioTypesSection, '入出力オブジェクト一覧セクションが存在する');
        assert.equal(ioTypesSection.querySelector('h3 span').textContent, '入出力オブジェクト一覧');

        const orderItemCard = mainList.querySelector('#' + Jig.util.fqnToId('io-type', 'com.example.OrderItem'));
        assert.ok(orderItemCard, 'OrderItemのカードが存在する');
        assert.equal(orderItemCard.querySelector('h3 span').textContent, 'OrderItem');

        const fieldItem = orderItemCard.querySelector('.field-item');
        assert.ok(fieldItem, 'フィールドアイテムが存在する');
        assert.equal(fieldItem.textContent, 'id:Long');
    });

    test('deprecatedなルート型カードのタイトルにdeprecatedクラスが付く', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.OrderController",
                classPath: "/api", relations: [],
                entrypoints: [{
                    fqn: "com.example.OrderController#order()",
                    entrypointType: "HTTP_API",
                    path: "GET /order",
                    parameters: [],
                    returnTypeRef: {fqn: "com.example.OrderItem"}
                }]
            }],
            ioTypes: [
                {fqn: "com.example.OrderItem", fields: [{name: "id", typeRef: {fqn: "java.lang.Long"}, isDeprecated: false}], isDeprecated: true}
            ],
            rootIoTypeFqns: ["com.example.OrderItem"]
        };
        setGlossaryData({
            "com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"},
            "com.example.OrderController#order()": {title: "order", simpleText: "order", kind: "メソッド", description: ""},
            "com.example.OrderItem": {title: "OrderItem", description: "", kind: "クラス"}
        });
        InboundApp.init();

        const orderItemCard = document.getElementById('inbound-list')
            .querySelector('#' + Jig.util.fqnToId('io-type', 'com.example.OrderItem'));
        assert.ok(orderItemCard.querySelector('h3 span.deprecated'), 'タイトルspanにdeprecatedクラスが付く');
    });

    test('ネスト型をルートカード内に再帰展開する', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.OrderController",
                classPath: "/api", relations: [],
                entrypoints: [{
                    fqn: "com.example.OrderController#order()",
                    entrypointType: "HTTP_API",
                    path: "GET /order",
                    parameters: [],
                    returnTypeRef: {fqn: "com.example.OrderItem"}
                }]
            }],
            ioTypes: [
                {fqn: "com.example.OrderItem", fields: [{name: "id", typeRef: {fqn: "com.example.OrderId"}, isDeprecated: false}], isDeprecated: false},
                {fqn: "com.example.OrderId",   fields: [{name: "value", typeRef: {fqn: "java.lang.Long"}, isDeprecated: false}], isDeprecated: false}
            ],
            rootIoTypeFqns: ["com.example.OrderItem"]
        };
        setGlossaryData({
            "com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"},
            "com.example.OrderController#order()": {title: "order", simpleText: "order", kind: "メソッド", description: ""},
            "com.example.OrderItem": {title: "OrderItem", description: "", kind: "クラス"},
            "com.example.OrderId":   {title: "OrderId",   description: "", kind: "クラス"}
        });
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        // OrderItemはルートカードとして存在する
        const orderItemCard = mainList.querySelector('#' + Jig.util.fqnToId('io-type', 'com.example.OrderItem'));
        assert.ok(orderItemCard, 'OrderItemのルートカードが存在する');

        // OrderIdはOrderItemカード内にネストとして存在する
        const orderIdSection = orderItemCard.querySelector('.io-type-nested');
        assert.ok(orderIdSection, 'OrderIdのネストセクションがOrderItem内に存在する');
        assert.equal(orderIdSection.querySelector('.io-type-nested-label').textContent, 'OrderId');

        // OrderIdの内部にフィールドが展開される
        const nestedFieldItem = orderIdSection.querySelector('.field-item');
        assert.ok(nestedFieldItem, 'OrderIdのフィールドアイテムが存在する');
        assert.equal(nestedFieldItem.textContent, 'value:Long');

        // OrderIdは独立したトップレベルカードとして存在しない（ルートでないため）
        const orderIdTopLevel = mainList.querySelector('#io-types > #' + Jig.util.fqnToId('io-type', 'com.example.OrderId'));
        assert.equal(orderIdTopLevel, null, 'OrderIdはトップレベルには存在しない');

        // フィールド行の直後にネストセクションが来る（DOM順序の確認）
        const fieldsPanel = orderItemCard.querySelector('.jig-tab-panel');
        const panelChildren = Array.from(fieldsPanel.children);
        const idFieldIdx    = panelChildren.findIndex(el => el.classList.contains('field-item'));
        const nestedIdx     = panelChildren.findIndex(el => el.classList.contains('io-type-nested'));
        assert.ok(idFieldIdx !== -1 && nestedIdx !== -1, 'フィールドとネストセクションが存在する');
        assert.ok(nestedIdx === idFieldIdx + 1, 'ネストセクションはフィールド行の直後に配置される');
    });

    test('循環参照がある場合でも無限ループしない', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.Ctrl",
                classPath: "/api", relations: [],
                entrypoints: [{fqn: "com.example.Ctrl#a()", entrypointType: "HTTP_API", path: "GET /a", parameters: [], returnTypeRef: {fqn: "void"}}]
            }],
            ioTypes: [
                {fqn: "com.example.TypeA", fields: [{name: "b", typeRef: {fqn: "com.example.TypeB"}, isDeprecated: false}], isDeprecated: false},
                {fqn: "com.example.TypeB", fields: [{name: "a", typeRef: {fqn: "com.example.TypeA"}, isDeprecated: false}], isDeprecated: false}
            ],
            rootIoTypeFqns: ["com.example.TypeA"]
        };
        setGlossaryData({
            "com.example.Ctrl": {title: "Ctrl", description: "", kind: "クラス"},
            "com.example.Ctrl#a()": {title: "a", simpleText: "a", kind: "メソッド", description: ""},
            "com.example.TypeA": {title: "TypeA", description: "", kind: "クラス"},
            "com.example.TypeB": {title: "TypeB", description: "", kind: "クラス"}
        });

        assert.doesNotThrow(() => InboundApp.init(), '循環参照があっても例外が発生しない');

        const mainList = document.getElementById('inbound-list');
        const typeACard = mainList.querySelector('#' + Jig.util.fqnToId('io-type', 'com.example.TypeA'));
        assert.ok(typeACard, 'TypeAカードが存在する');
        // TypeBはTypeAの内部にネストされるが、TypeBからTypeAへの再展開はされない
        const typeBSection = typeACard.querySelector('.io-type-nested');
        assert.ok(typeBSection, 'TypeBのネストセクションが存在する');
        const nestedTypeAInB = typeBSection.querySelector('.io-type-nested');
        assert.equal(nestedTypeAInB, null, 'TypeB内にTypeAが再展開されない（無限ループ防止）');
    });

    test('入出力オブジェクトカードに使用するエントリーポイントへのリンクが表示される', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.OrderController",
                classPath: "/api", relations: [],
                entrypoints: [{
                    fqn: "com.example.OrderController#order()",
                    entrypointType: "HTTP_API",
                    path: "GET /order",
                    parameters: [],
                    returnTypeRef: {fqn: "com.example.OrderItem"}
                }]
            }],
            ioTypes: [
                {fqn: "com.example.OrderItem", fields: [{name: "id", typeRef: {fqn: "java.lang.Long"}, isDeprecated: false}], isDeprecated: false}
            ],
            rootIoTypeFqns: ["com.example.OrderItem"]
        };
        setGlossaryData({
            "com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"},
            "com.example.OrderController#order()": {title: "order", simpleText: "order", kind: "メソッド", description: ""},
            "com.example.OrderItem": {title: "OrderItem", description: "", kind: "クラス"}
        });
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        const orderItemCard = mainList.querySelector('#' + Jig.util.fqnToId('io-type', 'com.example.OrderItem'));
        assert.ok(orderItemCard, 'OrderItemのカードが存在する');

        const usagesDiv = orderItemCard.querySelector('.io-type-usages');
        assert.ok(usagesDiv, '使用するエントリーポイントセクションが存在する');

        const usageLinks = usagesDiv.querySelectorAll('a.io-type-usage-link');
        assert.equal(usageLinks.length, 1, '使用リンクが1件表示される');
        assert.equal(usageLinks[0].textContent, 'order', 'エントリーポイントのメソッド名が表示される');
        assert.equal(
            usageLinks[0].getAttribute('href'),
            '#' + Jig.util.fqnToId('adapter', 'com.example.OrderController'),
            'アダプターカードへのリンクになっている'
        );
    });

    test('ioTypesが空の場合は入出力オブジェクト一覧セクションが描画されない', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.OrderController",
                classPath: "/api", relations: [],
                entrypoints: [{
                    fqn: "com.example.OrderController#order()",
                    entrypointType: "HTTP_API",
                    path: "GET /order",
                    parameters: [],
                    returnTypeRef: {fqn: "java.lang.String"}
                }]
            }],
            ioTypes: [],
            rootIoTypeFqns: []
        };
        setGlossaryData({
            "com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"},
            "com.example.OrderController#order()": {title: "order", simpleText: "order", kind: "メソッド", description: ""}
        });
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        const ioTypesSection = mainList.querySelector('#io-types');
        assert.equal(ioTypesSection, null, '入出力オブジェクト一覧セクションは存在しない');
    });

    test('サイドバーに入出力オブジェクト一覧リンクが表示される', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.OrderController",
                classPath: "/api", relations: [],
                entrypoints: [{fqn: "com.example.OrderController#order()", entrypointType: "HTTP_API", path: "GET /order", parameters: [], returnTypeRef: {fqn: "void"}}]
            }],
            ioTypes: [],
            rootIoTypeFqns: []
        };
        setGlossaryData({"com.example.OrderController": {title: "OrderController", description: "", kind: "クラス"}, "com.example.OrderController#order()": {title: "order", simpleText: "order", kind: "メソッド", description: ""}});
        InboundApp.init();

        const sidebar = document.getElementById('inbound-sidebar-list');
        const links = Array.from(sidebar.querySelectorAll('a'));
        const ioLink = links.find(a => a.textContent === '入出力オブジェクト一覧');
        assert.ok(ioLink, '入出力オブジェクト一覧リンクが存在する');
        assert.equal(ioLink.getAttribute('href'), '#io-types');
    });
});
