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
            entrypoints: [
                {
                    fqn: "com.example.ControllerA#method1()",
                    visibility: "PUBLIC",
                    parameterTypeRefs: [],
                    returnTypeRef: { fqn: "void" },
                    isDeprecated: false,
                    path: "GET /api/method1",
                    graph: {
                        nodes: [
                            { fqn: "com.example.ControllerA#method1()", type: "entrypoint" }
                        ],
                        edges: [],
                        serviceGroups: [
                            {
                                fqn: "com.example.ServiceA",
                                methods: [
                                    { fqn: "com.example.ServiceA#serviceMethod()" }
                                ]
                            }
                        ]
                    }
                }
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
        assert.strictEqual(controllerSection.querySelector('.markdown').innerHTML, 'Description of ControllerA');

        const inboundSection = controllerSection.querySelector('.jig-card--item');
        assert.ok(inboundSection);
        assert.strictEqual(inboundSection.querySelector('h4').textContent, 'method1');
        assert.strictEqual(inboundSection.querySelector('h4').id, 'com.example.ControllerA#method1()');
        assert.strictEqual(inboundSection.querySelector('.fully-qualified-name').textContent, 'GET /api/method1');

        const mermaidPre = inboundSection.querySelector('.mermaid');
        assert.ok(mermaidPre);
        assert.ok(mermaidPre.textContent.includes('subgraph')); // Mermaid code generated
    });

    test('renderControllerList should handle empty data', () => {
        globalThis.inboundData = { controllers: [] };
        InboundApp.init();

        const mainList = document.getElementById('inbound-list');
        assert.strictEqual(mainList.textContent, 'データなし');
    });
});
