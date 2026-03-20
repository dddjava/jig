const assert = require('assert');
const { test, beforeEach } = require('node:test');
const { JSDOM } = require('jsdom');
const fs = require('fs');
const path = require('path');

// テスト対象のファイルを読み込む
const usecaseJsPath = path.resolve(__dirname, '../../main/resources/templates/assets/usecase.js');
const { UsecaseApp } = require(usecaseJsPath);

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

    beforeEach(() => {
        const dom = new JSDOM(`
            <!DOCTYPE html>
            <html>
            <body>
                <div id="usecase-sidebar-list"></div>
                <div id="usecase-list"></div>
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
        global.mermaid = { run: () => {} }; // mermaidのモック
    });

    test('init should render data from globalThis.usecaseData', () => {
        global.globalThis = { usecaseData: mockUsecaseData };
        UsecaseApp.init();

        const sidebar = document.getElementById('usecase-sidebar-list');
        assert.strictEqual(sidebar.children.length, 1);
        assert.strictEqual(sidebar.querySelector('h3').textContent, 'ユースケース');
        assert.strictEqual(sidebar.querySelector('a').textContent, 'ServiceA');

        const mainList = document.getElementById('usecase-list');
        assert.strictEqual(mainList.children.length, 1);
        const serviceSection = mainList.children[0];
        assert.strictEqual(serviceSection.querySelector('h2 a').id, 'com.example.ServiceA');
        assert.strictEqual(serviceSection.querySelector('h2 a').textContent, 'ServiceA');
        assert.strictEqual(serviceSection.querySelector('.fully-qualified-name').textContent, 'com.example.ServiceA');
        assert.strictEqual(serviceSection.querySelector('.markdown').innerHTML, 'Description of ServiceA');

        const fieldsTable = serviceSection.querySelector('table.fields');
        assert.ok(fieldsTable);
        assert.strictEqual(fieldsTable.querySelector('tbody td').textContent, 'field1');

        const staticMethodsTable = serviceSection.querySelector('table:not(.fields)');
        assert.ok(staticMethodsTable);
        assert.strictEqual(staticMethodsTable.querySelector('th').textContent, 'staticメソッド');
        assert.strictEqual(staticMethodsTable.querySelector('tbody td.method-name').textContent, 'staticMethod1');
        
        const methodSection = serviceSection.querySelector('.method');
        assert.ok(methodSection);
        assert.strictEqual(methodSection.querySelector('h3').id, 'method1');
        assert.strictEqual(methodSection.querySelector('h3').textContent, 'method1');
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
        global.globalThis = { usecaseData: { usecases: [] } };
        UsecaseApp.init();
        
        const mainList = document.getElementById('usecase-list');
        assert.strictEqual(mainList.textContent, 'データなし');
    });
});
