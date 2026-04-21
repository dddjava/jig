const test = require('node:test');
const assert = require('node:assert/strict');
const {DocumentStub, setGlossaryData} = require('./dom-stub.js');

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
            sidebar: {
                renderSection: (container, title, items) => {
                    if (!container) return;
                    const section = createSidebarSection(title, items);
                    if (section) container.appendChild(section);
                },
                initTextFilter: (inputId, onChange) => {
                    const input = document.getElementById(inputId);
                    if (!input) return;
                    input.addEventListener('input', () => onChange(input.value.trim()));
                }
            },
            type: {
                methodsList: createMethodsList
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
        assert.equal(sidebar.children.length, 1);
        assert.equal(sidebar.querySelector('p').textContent, 'リクエストハンドラ');
        assert.equal(sidebar.querySelector('a').textContent, 'ControllerA');

        const mainList = document.getElementById('inbound-list');
        assert.equal(mainList.children.length, 2); // サマリーセクション + コントローラーセクション

        // サマリーセクション（リクエストハンドラ）
        const summarySection = mainList.children[0];
        assert.ok(summarySection.classList.has('entrypoint-summary-section'));
        assert.equal(summarySection.querySelector('h3').textContent, 'リクエストハンドラ');
        const summaryTable = summarySection.querySelector('table.entrypoint-summary');
        assert.ok(summaryTable);
        const rows = summaryTable.querySelectorAll('tbody tr');
        assert.equal(rows.length, 1);
        const cells = rows[0].children;
        assert.equal(cells[0].textContent, '/method1'); // パスが先頭
        assert.equal(cells[1].textContent, 'GET');      // メソッドが2列目
        const link = cells[2].querySelector('a');
        assert.equal(link.textContent, 'com.example.ControllerA#method1()');
        assert.ok(link.getAttribute('href').startsWith('#'));

        const controllerSection = mainList.children[1];
        assert.equal(controllerSection.id, globalThis.Jig.util.fqnToId("adapter", 'com.example.ControllerA'));
        assert.equal(controllerSection.querySelector('h3 a').textContent, 'ControllerA');
        assert.equal(controllerSection.querySelector('.fully-qualified-name').textContent, 'com.example.ControllerA');
        assert.equal(controllerSection.querySelector('.class-path').textContent, '/api');
        assert.equal(controllerSection.querySelector('.markdown').innerHTML, 'Description of ControllerA');

        // エントリーポイント一覧（createMethodsList）
        const methodsSection = controllerSection.querySelector('.methods-section');
        assert.ok(methodsSection);
        const methodItems = methodsSection.querySelectorAll('.method-item');
        assert.equal(methodItems.length, 1);
        assert.ok(methodItems[0].querySelector('.method-name').textContent.includes('method1'));

        assert.equal(controllerSection.querySelector('article.jig-card--item'), null);

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
});
