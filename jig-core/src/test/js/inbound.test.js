const assert = require('assert');
const { test, beforeEach } = require('node:test');
const { JSDOM } = require('jsdom');
const path = require('path');

const jigCommonJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/jig-common.js');
const jigJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/jig.js');
const inboundJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/inbound.js');

// モック用のデータ
const mockInboundData = {
    controllers: [
        {
            fqn: "com.example.ControllerA",
            classPath: "/api",
            relations: [
                { from: "com.example.ControllerA#method1()", to: "com.example.ServiceA#serviceMethod()" }
            ],
            entrypoints: [
                {
                    fqn: "com.example.ControllerA#method1()",
                    visibility: "PUBLIC",
                    parameterTypeRefs: [],
                    returnTypeRef: { fqn: "void" },
                    isDeprecated: false,
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
                { fqn: "com.example.ServiceA#serviceMethod()" }
            ]
        }
    ]
};

const mockGlossaryData = {
    "com.example.ControllerA": { title: "ControllerA", description: "Description of ControllerA", kind: "クラス" },
    "com.example.ControllerA#method1()": { title: "method1", simpleText: "method1", kind: "メソッド", description: "" },
    "com.example.ServiceA": { title: "ServiceA", description: "", kind: "クラス" },
    "com.example.ServiceA#serviceMethod()": { title: "serviceMethod", simpleText: "serviceMethod", kind: "メソッド", description: "" }
};

test.describe('InboundApp', () => {
    let window;
    let document;
    let InboundApp;

    beforeEach(() => {
        const dom = new JSDOM(`
            <!DOCTYPE html>
            <html>
            <body>
                <div id="inbound-sidebar-list"></div>
                <div id="inbound-list"></div>
            </body>
            </html>
        `, { runScripts: "dangerously" });

        window = dom.window;
        document = window.document;
        global.window = window;
        global.document = document;
        global.IntersectionObserver = class {
            constructor(callback) {
                this.callback = callback;
            }
            observe(element) {
                this.callback([{ isIntersecting: true, target: element }]);
            }
            unobserve() {}
        };
        global.marked = { parse: (text) => text }; // markedのモック
        global.mermaid = { initialize: () => {}, run: () => {} }; // mermaidのモック

        delete require.cache[jigCommonJsPath];
        delete require.cache[jigJsPath];
        delete require.cache[inboundJsPath];
        require(jigCommonJsPath);
        require(jigJsPath);
        ({ InboundApp } = require(inboundJsPath));
    });

    test('init should render data from globalThis.inboundData', () => {
        globalThis.inboundData = mockInboundData;
        globalThis.glossaryData = mockGlossaryData;
        globalThis.usecaseData = mockUsecaseData;
        InboundApp.init();

        const sidebar = document.getElementById('inbound-sidebar-list');
        assert.strictEqual(sidebar.children.length, 1);
        assert.strictEqual(sidebar.querySelector('p').textContent, 'コントローラー');
        assert.strictEqual(sidebar.querySelector('a').textContent, 'ControllerA');

        const mainList = document.getElementById('inbound-list');
        assert.strictEqual(mainList.children.length, 1);
        const controllerSection = mainList.children[0];
        assert.strictEqual(controllerSection.id, 'com.example.ControllerA');
        assert.strictEqual(controllerSection.querySelector('h3 a').textContent, 'ControllerA');
        assert.strictEqual(controllerSection.querySelector('.fully-qualified-name').textContent, 'com.example.ControllerA');
        assert.strictEqual(controllerSection.querySelector('.class-path').textContent, '/api');
        assert.strictEqual(controllerSection.querySelector('.markdown').innerHTML, 'Description of ControllerA');

        // エントリーポイント一覧（createMethodsList）
        const methodsSection = controllerSection.querySelector('.methods-section');
        assert.ok(methodsSection);
        const methodItems = methodsSection.querySelectorAll('.method-item');
        assert.strictEqual(methodItems.length, 1);
        assert.ok(methodItems[0].querySelector('.method-name').textContent.includes('method1'));

        // 個別カード（jig-card--item）は廃止
        assert.strictEqual(controllerSection.querySelector('article.jig-card--item'), null);

        // コントローラー単位の統合ダイアグラム
        const mermaidPre = controllerSection.querySelector('.mermaid');
        assert.ok(mermaidPre);
        assert.ok(mermaidPre.textContent.includes('subgraph')); // Mermaid code generated
        assert.ok(mermaidPre.textContent.includes('GET /method1')); // パスノードが含まれる
    });

    test('renderControllerList should handle empty data', () => {
        globalThis.inboundData = { controllers: [] };
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        assert.strictEqual(mainList.textContent, 'データなし');
    });

    test('adapter chain: 浅いノードから外部へのエッジは長くなる', () => {
        // method1 → internalMethod → serviceMethod (depth=2)
        // method2 → serviceMethod2 (depth=1, maxDepth=2 なので ---> になる)
        globalThis.inboundData = {
            controllers: [{
                fqn: "com.example.ControllerA",
                classPath: "/api",
                relations: [
                    { from: "com.example.ControllerA#method1()", to: "com.example.ControllerA#internalMethod()" },
                    { from: "com.example.ControllerA#internalMethod()", to: "com.example.ServiceA#serviceMethod()" },
                    { from: "com.example.ControllerA#method2()", to: "com.example.ServiceB#serviceMethod2()" }
                ],
                entrypoints: [
                    { fqn: "com.example.ControllerA#method1()", visibility: "PUBLIC", parameterTypeRefs: [], returnTypeRef: { fqn: "void" }, isDeprecated: false, path: "GET /method1" },
                    { fqn: "com.example.ControllerA#method2()", visibility: "PUBLIC", parameterTypeRefs: [], returnTypeRef: { fqn: "void" }, isDeprecated: false, path: "GET /method2" }
                ]
            }]
        };
        globalThis.glossaryData = {
            "com.example.ControllerA": { title: "ControllerA", description: "", kind: "クラス" },
            "com.example.ControllerA#method1()": { title: "method1", simpleText: "method1", kind: "メソッド", description: "" },
            "com.example.ControllerA#method2()": { title: "method2", simpleText: "method2", kind: "メソッド", description: "" },
            "com.example.ControllerA#internalMethod()": { title: "internalMethod", simpleText: "internalMethod", kind: "メソッド", description: "" },
            "com.example.ServiceA": { title: "ServiceA", description: "", kind: "クラス" },
            "com.example.ServiceA#serviceMethod()": { title: "serviceMethod", simpleText: "serviceMethod", kind: "メソッド", description: "" },
            "com.example.ServiceB": { title: "ServiceB", description: "", kind: "クラス" },
            "com.example.ServiceB#serviceMethod2()": { title: "serviceMethod2", simpleText: "serviceMethod2", kind: "メソッド", description: "" }
        };
        globalThis.usecaseData = {
            usecases: [
                { fqn: "com.example.ServiceA", methods: [{ fqn: "com.example.ServiceA#serviceMethod()" }] },
                { fqn: "com.example.ServiceB", methods: [{ fqn: "com.example.ServiceB#serviceMethod2()" }] }
            ]
        };
        InboundApp.init();

        const mermaidPre = document.getElementById('inbound-list').children[0].querySelector('.mermaid');
        assert.ok(mermaidPre);
        const mermaidCode = mermaidPre.textContent;
        // method2 (depth=1, maxDepth=2) からのエッジは ---> になる
        assert.ok(mermaidCode.includes('--->'), `Expected ---> in: ${mermaidCode}`);
    });

    test('init should work without usecaseData', () => {
        globalThis.inboundData = mockInboundData;
        globalThis.glossaryData = mockGlossaryData;
        delete globalThis.usecaseData;
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        const mermaidPre = mainList.children[0].querySelector('.mermaid');
        assert.ok(mermaidPre);
        // entrypointおよびmethodのsubgraphが生成される
        assert.ok(mermaidPre.textContent.includes('subgraph'));
        // usecase.htmlへのclickリンクは生成されない
        assert.ok(!mermaidPre.textContent.includes('click'));
        assert.ok(mermaidPre.textContent.includes('GET /method1'));
    });
});
