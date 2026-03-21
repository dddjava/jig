const assert = require('assert');
const { test, beforeEach } = require('node:test');
const { JSDOM } = require('jsdom');
const path = require('path');

const jigJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/jig.js');
const usecaseJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/usecase.js');

// モック用のデータ
const mockUsecaseData = {
    usecases: [
        {
            typeId: "com.example.ServiceA",
            label: "ServiceA",
            description: "Description of ServiceA",
            fields: [
                { name: "field1", typeHtml: "String", isDeprecated: false }
            ],
            staticMethods: [
                {
                    methodId: "staticMethod1",
                    label: "staticMethod1",
                    labelWithSymbol: "staticMethod1",
                    declaration: "staticMethod1():void",
                    returnTypeLink: '<span class="weak">void</span>',
                    argumentsLinks: [],
                    description: "Description of staticMethod1"
                }
            ],
            methods: [
                {
                    methodId: "method1",
                    label: "method1",
                    labelWithSymbol: "method1",
                    declaration: "method1():void",
                    returnTypeLink: '<span class="weak">void</span>',
                    argumentsLinks: [],
                    description: "Description of method1",
                    graph: {
                        nodes: [
                            { id: "n1", label: "method1", type: "usecase", highlight: true },
                            { id: "n2", label: "otherMethod", type: "normal", link: "otherMethod" }
                        ],
                        edges: [
                            { from: "n2", to: "n1" }
                        ]
                    }
                }
            ]
        }
    ]
};

test.describe('UsecaseApp', () => {
    let window;
    let document;
    let UsecaseApp;

    beforeEach(() => {
        const dom = new JSDOM(`
            <!DOCTYPE html>
            <html>
            <body class="usecase-summary">
                <main class="usecase">
                    <details class="controls">
                        <summary>表示設定</summary>
                        <div class="filter-controls">
                            <label><input type="checkbox" id="show-fields" checked> フィールド</label>
                            <label><input type="checkbox" id="show-static-methods" checked> staticメソッド</label>
                            <label><input type="checkbox" id="show-diagrams" checked> ダイアグラム</label>
                            <label><input type="checkbox" id="show-details" checked> 引数・戻り値</label>
                            <label><input type="checkbox" id="show-descriptions" checked> 説明</label>
                            <label><input type="checkbox" id="show-declarations" checked> 完全修飾名・宣言</label>
                        </div>
                    </details>
                    <div id="usecase-sidebar-list"></div>
                    <div id="usecase-list"></div>
                </main>
            </body>
            </html>
        `, { runScripts: "dangerously" });

        window = dom.window;
        document = window.document;
        global.window = window;
        global.document = document;
        const storage = {};
        global.localStorage = {
            getItem: (key) => storage[key] || null,
            setItem: (key, value) => storage[key] = String(value),
        };
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
        global.mermaid = { run: () => {} }; // mermaidのモック

        delete require.cache[jigJsPath];
        delete require.cache[usecaseJsPath];
        require(jigJsPath);
        ({ UsecaseApp } = require(usecaseJsPath));
    });

    test('init should render data from globalThis.usecaseData', () => {
        globalThis.usecaseData = mockUsecaseData;
        UsecaseApp.init();

        const sidebar = document.getElementById('usecase-sidebar-list');
        assert.strictEqual(sidebar.children.length, 1);
        assert.strictEqual(sidebar.querySelector('p').textContent, 'ユースケース');
        assert.strictEqual(sidebar.querySelector('a').textContent, 'ServiceA');

        const mainList = document.getElementById('usecase-list');
        assert.strictEqual(mainList.children.length, 1);
        const serviceSection = mainList.children[0];
        assert.strictEqual(serviceSection.querySelector('h3 a').id, 'com.example.ServiceA');
        assert.strictEqual(serviceSection.querySelector('h3 a').textContent, 'ServiceA');
        assert.strictEqual(serviceSection.querySelector('.fully-qualified-name').textContent, 'com.example.ServiceA');
        assert.strictEqual(serviceSection.querySelector('.markdown').innerHTML, 'Description of ServiceA');

        const fieldsTable = serviceSection.querySelector('table.fields');
        assert.ok(fieldsTable);
        assert.strictEqual(fieldsTable.querySelector('tbody td').textContent, 'field1');

        const staticMethodsTable = serviceSection.querySelector('table:not(.fields)');
        assert.ok(staticMethodsTable);
        assert.strictEqual(staticMethodsTable.querySelector('th').textContent, 'staticメソッド');
        assert.strictEqual(staticMethodsTable.querySelector('tbody td.method-name').textContent, 'staticMethod1');
        
        const methodSection = serviceSection.querySelector('.jig-card--item');
        assert.ok(methodSection);
        assert.strictEqual(methodSection.querySelector('h4').id, 'method1');
        assert.strictEqual(methodSection.querySelector('h4').textContent, 'method1');
        assert.strictEqual(methodSection.querySelector('.fully-qualified-name').textContent, 'method1():void');
        
        const mermaidPre = methodSection.querySelector('.mermaid');
        assert.ok(mermaidPre);
        assert.ok(mermaidPre.textContent.includes('graph LR'));
        assert.ok(mermaidPre.textContent.includes('style n1 font-weight:bold'));
        assert.ok(mermaidPre.textContent.includes('click n2 "#otherMethod"'));

        const description = methodSection.querySelector('.description');
        assert.strictEqual(description.innerHTML, 'Description of method1');
    });

    test('renderUsecaseList should handle empty data', () => {
        globalThis.usecaseData = { usecases: [] };
        UsecaseApp.init();
        
        const mainList = document.getElementById('usecase-list');
        assert.strictEqual(mainList.textContent, 'データなし');
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
