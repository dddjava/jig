const assert = require('assert');
const { test, beforeEach } = require('node:test');
const { JSDOM } = require('jsdom');
const path = require('path');

const jigJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/jig.js');
const entrypointJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/entrypoint.js');

// モック用のデータ
const mockEntrypointData = {
    controllers: [
        {
            fqn: "com.example.ControllerA",
            label: "ControllerA",
            description: "Description of ControllerA",
            entrypoints: [
                {
                    methodId: "method1",
                    label: "method1",
                    path: "GET /api/method1",
                    graph: {
                        nodes: [
                            { id: "n1", label: "method1", type: "entrypoint" },
                            { id: "n2", label: "GET /api/method1", type: "path" }
                        ],
                        edges: [
                            { from: "n2", to: "n1", label: "", style: "dotted" }
                        ],
                        serviceGroups: [
                            {
                                typeId: "com.example.ServiceA",
                                label: "ServiceA",
                                methods: [
                                    { id: "s1", label: "serviceMethod", link: "serviceMethod" }
                                ]
                            }
                        ]
                    }
                }
            ]
        }
    ]
};

test.describe('EntrypointApp', () => {
    let window;
    let document;
    let EntrypointApp;

    beforeEach(() => {
        const dom = new JSDOM(`
            <!DOCTYPE html>
            <html>
            <body>
                <div id="entrypoint-sidebar-list"></div>
                <div id="entrypoint-list"></div>
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

        delete require.cache[jigJsPath];
        delete require.cache[entrypointJsPath];
        require(jigJsPath);
        ({ EntrypointApp } = require(entrypointJsPath));
    });

    test('init should render data from globalThis.entrypointData', () => {
        globalThis.entrypointData = mockEntrypointData;
        EntrypointApp.init();

        const sidebar = document.getElementById('entrypoint-sidebar-list');
        assert.strictEqual(sidebar.children.length, 1);
        assert.strictEqual(sidebar.querySelector('p').textContent, 'コントローラー');
        assert.strictEqual(sidebar.querySelector('a').textContent, 'ControllerA');

        const mainList = document.getElementById('entrypoint-list');
        assert.strictEqual(mainList.children.length, 1);
        const controllerSection = mainList.children[0];
        assert.strictEqual(controllerSection.id, 'com.example.ControllerA');
        assert.strictEqual(controllerSection.querySelector('h3 a').textContent, 'ControllerA');
        assert.strictEqual(controllerSection.querySelector('.fully-qualified-name').textContent, 'com.example.ControllerA');
        assert.strictEqual(controllerSection.querySelector('.markdown').innerHTML, 'Description of ControllerA');
        
        const entrypointSection = controllerSection.querySelector('.jig-card--item');
        assert.ok(entrypointSection);
        assert.strictEqual(entrypointSection.querySelector('h4').textContent, 'method1');
        assert.strictEqual(entrypointSection.querySelector('.fully-qualified-name').textContent, 'GET /api/method1');
        
        const mermaidPre = entrypointSection.querySelector('.mermaid');
        assert.ok(mermaidPre);
        assert.ok(mermaidPre.textContent.includes('subgraph')); // Mermaid code generated
    });

    test('renderControllerList should handle empty data', () => {
        globalThis.entrypointData = { controllers: [] };
        EntrypointApp.init();
        
        const mainList = document.getElementById('entrypoint-list');
        assert.strictEqual(mainList.textContent, 'データなし');
    });
});
