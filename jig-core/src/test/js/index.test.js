const test = require('node:test');
const assert = require('node:assert/strict');

require('../../main/resources/templates/assets/jig-glossary.js');
require('../../main/resources/templates/assets/jig-dom.js');
require('../../main/resources/templates/assets/jig-mermaid.js');

// Mock DOM environment
function setupGlobals() {
    global.window = {
        addEventListener() {},
        setTimeout: global.setTimeout,
    };
    global.document = {
        body: {
            classList: {
                contains: () => false,
            },
        },
        addEventListener() {},
        getElementById(id) {
            if (id === 'package-diagram') {
                return {
                    appendChild: () => {},
                    insertBefore: () => {},
                };
            }
            if (id === 'document-links') {
                return {
                    querySelector: () => ({
                        innerHTML: '',
                        appendChild: () => {},
                    }),
                };
            }
            return null;
        },
        getElementsByClassName() {
            return [];
        },
        createElement(tagName) {
            return {
                tagName,
                children: [],
                textContent: '',
                className: '',
                style: {},
                innerHTML: '',
                classList: {
                    add: () => {},
                    remove: () => {},
                    contains: () => false,
                },
                appendChild: function(child) {
                    this.children.push(child);
                    return child;
                },
                insertBefore: function(child) {
                    this.children.push(child);
                    return child;
                },
                querySelector: () => null,
                addEventListener: () => {},
                getAttribute: () => null,
                setAttribute: () => {},
                removeAttribute: () => {},
            };
        },
    };
}

test.describe('index.js', () => {
    test.beforeEach(() => {
        setupGlobals();
        globalThis.packageData = undefined;
        globalThis.navigationData = undefined;
    });

    test('修正確認: Jig.mermaid.render.renderWithControls が関数として存在すること', () => {
        assert.ok(globalThis.Jig.mermaid, 'Jig.mermaid が定義されている');
        assert.ok(globalThis.Jig.mermaid.render, 'Jig.mermaid.render が定義されている');
        assert.equal(
            typeof globalThis.Jig.mermaid.render.renderWithControls,
            'function',
            'Jig.mermaid.render.renderWithControls は関数である'
        );
    });

    test('修正確認: Jig.util.getCommonPrefix が関数として存在すること', () => {
        assert.ok(globalThis.Jig.util, 'Jig.util が定義されている');
        assert.equal(
            typeof globalThis.Jig.util.getCommonPrefix,
            'function',
            'Jig.util.getCommonPrefix は関数である'
        );
    });

    test('Jig.util.getCommonPrefix が期待値を返すこと', () => {
        const fqns = ['com.example.foo.Bar', 'com.example.foo.Baz', 'com.example.qux.Quux'];
        const result = globalThis.Jig.util.getCommonPrefix(fqns);

        assert.ok(result, 'getCommonPrefix は値を返す');
        assert.equal(typeof result, 'string', 'getCommonPrefix は文字列を返す');
    });

    test('index.js が正しい API を使用していることを確認（ソースコード検証）', () => {
        const fs = require('fs');
        const path = require('path');
        const indexJsPath = path.join(__dirname, '../../main/resources/templates/assets/index.js');
        const content = fs.readFileSync(indexJsPath, 'utf-8');

        // renderWithControls が Jig.mermaid.render.renderWithControls を使用していることを確認
        assert.ok(
            content.includes('Jig.mermaid.render.renderWithControls'),
            'index.js は Jig.mermaid.render.renderWithControls を使用すべき'
        );

        // getCommonPrefix が Jig.util.getCommonPrefix を使用していることを確認
        assert.ok(
            content.includes('Jig.util.getCommonPrefix'),
            'index.js は Jig.util.getCommonPrefix を使用すべき'
        );

        // 古い API が使用されていないことを確認
        assert.ok(
            !content.includes('Jig.packageDiagram.getCommonPrefix'),
            'index.js は Jig.packageDiagram.getCommonPrefix を使用してはいけない'
        );

        assert.ok(
            !content.includes('Jig.mermaid.renderWithControls') || content.includes('Jig.mermaid.render.renderWithControls'),
            'index.js は Jig.mermaid.renderWithControls（.render 抜き）を使用してはいけない'
        );
    });
});
