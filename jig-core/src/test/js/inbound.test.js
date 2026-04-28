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

test.describe('InboundApp', () => {
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

    function createSidebarSection(title, items) {
        if (!items || items.length === 0) return null;
        return createElement('section', {
            className: 'in-page-sidebar__section',
            children: [
                createElement('p', {
                    className: 'in-page-sidebar__title',
                    textContent: title
                }),
                createElement('ul', {
                    className: 'in-page-sidebar__links',
                    children: items.map(({id, label}) => createElement('li', {
                        className: 'in-page-sidebar__item',
                        children: [
                            createElement('a', {
                                className: 'in-page-sidebar__link',
                                attributes: {href: '#' + id},
                                textContent: label
                            })
                        ]
                    }))
                })
            ]
        });
    }

    function createMethodsList(kind, methods) {
        if (!methods || methods.length === 0) return null;

        return createElement('section', {
            className: 'methods-section jig-card jig-card--item',
            children: [
                createElement('h4', {textContent: kind}),
                ...methods.map(method => createElement('div', {
                    className: 'method-item',
                    children: [
                        createElement('div', {
                            className: 'method-signature',
                            children: [
                                createElement('span', {
                                    className: 'method-name',
                                    textContent: globalThis.Jig.glossary.getMethodTerm(method.fqn, true).title
                                })
                            ]
                        })
                    ]
                }))
            ]
        });
    }

    test.beforeEach(() => {
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-util.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-data.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-glossary.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-mermaid.js')];
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
        // 表示設定パネル
        const settingsEl = doc.createElement('details');
        settingsEl.id = 'sidebar-settings';
        const fieldset = doc.createElement('fieldset');
        fieldset.id = 'display-type-fieldset';
        settingsEl.appendChild(fieldset);
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
        globalThis.Jig.dom = {
            createElement,
            createCell: (text, className) => createElement('td', {className: className || undefined, textContent: text}),
            parseMarkdown: (markdown) => String(markdown ?? ''),
            createMarkdownElement: (markdown) => createElement('div', {
                className: 'markdown',
                innerHTML: String(markdown ?? '')
            }),
            card: {
                item: ({id, title, tagName = "section", extraClass} = {}) => {
                    return createElement(tagName, {
                        id,
                        className: ["jig-card", "jig-card--item", extraClass].filter(Boolean).join(" "),
                        children: title !== undefined ? [createElement("h4", {textContent: title})] : []
                    });
                },
                type: ({id, title, fqn, kind, attributes, tagName = "section", extraClass} = {}) => {
                    const titleEl = typeof title === 'string' ? createElement("span", {textContent: title}) : title;
                    const h3Children = kind !== undefined ? [createElement("span", {className: "kind-badge"}), titleEl] : [titleEl];
                    const card = createElement(tagName, {
                        id,
                        className: ["jig-card", "jig-card--type", extraClass].filter(Boolean).join(" "),
                        attributes,
                        children: [createElement("h3", {children: h3Children})]
                    });
                    if (fqn != null) {
                        card.appendChild(typeof fqn === 'string'
                            ? createElement("div", {className: "fully-qualified-name", textContent: fqn})
                            : fqn);
                    }
                    return card;
                }
            },
            sidebar: {
                renderSection: (container, title, items) => {
                    if (!container) return;
                    const section = createSidebarSection(title, items);
                    if (section) container.appendChild(section);
                },
                initCollapseBtn: () => {},
                initTextFilter: (inputId, onChange) => {
                    const input = document.getElementById(inputId);
                    if (!input) return;
                    input.addEventListener('input', () => onChange(input.value.trim()));
                },
                createToggle: () => createElement('button', {className: 'in-page-sidebar__toggle'})
            },
            type: {
                methodsList: createMethodsList,
                refElement: (typeRef) => {
                    if (!typeRef) return createElement('span', {});
                    const text = typeRef.fqn.split('.').pop();
                    return createElement('span', {textContent: text});
                }
            }
        };
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
        assert.equal(sidebar.children.length, 2); // エントリーポイント一覧リンク + com.example パッケージ
        assert.equal(sidebar.children[0].querySelector('a').textContent, 'エントリーポイント一覧');
        assert.equal(sidebar.children[0].querySelector('a').getAttribute('href'), '#entrypoint-summary');
        assert.equal(sidebar.children[1].querySelector('p span').textContent, 'example'); // パッケージ名
        assert.equal(sidebar.children[1].querySelector('a').textContent, 'ControllerA');

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
        const rows = summaryTable.querySelectorAll('tbody tr');
        assert.equal(rows.length, 1);
        const cells = rows[0].children;
        assert.equal(cells[0].textContent, '/api/method1'); // クラスパス+メソッドパス
        assert.equal(cells[1].textContent, 'GET');
        const link = cells[2].querySelector('a');
        assert.equal(link.textContent, 'ControllerA method1');
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
        assert.ok(!mermaidPre.textContent.includes('click'));
        assert.ok(mermaidPre.textContent.includes('GET /method1'));
    });

    test('リクエストハンドラのフィルター入力でパス部分一致絞り込みができる', () => {
        globalThis.inboundData = {
            inboundAdapters: [{
                fqn: "com.example.ControllerA",
                classPath: "/api",
                relations: [],
                entrypoints: [
                    {fqn: "com.example.ControllerA#methodA()", entrypointType: "HTTP_API", path: "GET /users"},
                    {fqn: "com.example.ControllerA#methodB()", entrypointType: "HTTP_API", path: "GET /orders"}
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

        const tbody = document.getElementById('inbound-list').querySelector('table.entrypoint-summary tbody');
        assert.equal(tbody.children.length, 2);

        filterInput.value = 'user';
        filterInput.dispatchEvent(new EventStub('input'));

        // パス昇順ソート: /api/orders → /api/users
        assert.equal(tbody.children[0].style.display, 'none', '/api/orders はマッチしないので非表示');
        assert.equal(tbody.children[1].style.display, '', '/api/users はマッチするので表示');

        filterInput.value = '';
        filterInput.dispatchEvent(new EventStub('input'));
        assert.equal(tbody.children[0].style.display, '', 'クリアすると全行表示');
    });

    test('エントリーポイント種別が1種類以下の場合は表示設定パネルを非表示にする', () => {
        globalThis.inboundData = mockInboundData; // HTTP_APIのみ
        setGlossaryData(mockGlossaryData);
        InboundApp.init();

        const settingsEl = document.getElementById('sidebar-settings');
        assert.equal(settingsEl.style.display, 'none');
        assert.equal(document.querySelectorAll('input[name="display-type"]').length, 0);
    });

    test('表示設定ラジオボタンで絞り込むとサマリー・カード・サイドバーが絞り込まれる', () => {
        globalThis.inboundData = {
            inboundAdapters: [
                {
                    fqn: "com.example.HttpController",
                    classPath: "/api", relations: [],
                    entrypoints: [{fqn: "com.example.HttpController#get()", entrypointType: "HTTP_API", path: "GET /items"}]
                },
                {
                    fqn: "com.example.QueueListener",
                    classPath: "", relations: [],
                    entrypoints: [{fqn: "com.example.QueueListener#onMessage()", entrypointType: "QUEUE_LISTENER", path: "my-queue"}]
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
        const adapterLinks = Array.from(sidebar.querySelectorAll('a'))
            .filter(a => a.textContent !== 'エントリーポイント一覧');
        assert.deepEqual(adapterLinks.map(a => a.textContent), ['HttpController'], 'HttpControllerのみ表示');
    });
});
