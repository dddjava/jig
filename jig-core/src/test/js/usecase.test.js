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
                    visibility: "PRIVATE",
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

        ({ UsecaseApp } = require(usecaseJsPath));
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
        assert.strictEqual(sidebar.querySelector('a').textContent, 'ServiceA');

        const mainList = document.getElementById('usecase-list');
        assert.strictEqual(mainList.children.length, 1);
        const serviceSection = mainList.children[0];
        assert.strictEqual(serviceSection.querySelector('h3 a').id, 'com.example.ServiceA');
        assert.strictEqual(serviceSection.querySelector('h3 a').textContent, 'ServiceA');
        assert.strictEqual(serviceSection.querySelector('.fully-qualified-name').textContent, 'com.example.ServiceA');
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
        assert.strictEqual(methodSection.querySelector('.fully-qualified-name').textContent, 'method1():void');

        const mermaidPre = methodSection.querySelector('.mermaid');
        assert.ok(mermaidPre);
        assert.ok(mermaidPre.textContent.includes('graph LR'));

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
