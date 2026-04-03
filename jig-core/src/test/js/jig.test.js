const test = require('node:test');
const assert = require('node:assert/strict');

class Element {
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this.textContent = '';
        this.className = '';
        this.style = {};
        this._classSet = new Set();
        this.classList = {
            add: (name) => {
                this._classSet.add(name);
                this.className = Array.from(this._classSet).join(' ');
            },
            remove: (name) => {
                this._classSet.delete(name);
                this.className = Array.from(this._classSet).join(' ');
            },
            contains: (name) => this._classSet.has(name),
        };
    }

    appendChild(child) {
        this.children.push(child);
        return child;
    }

    addEventListener() {
    }
}

function createDocument() {
    return {
        body: {
            classList: {
                contains: () => false,
            },
        },
        addEventListener() {
        },
        getElementsByClassName() {
            return [];
        },
        createElement(tagName) {
            return new Element(tagName);
        },
    };
}

function setupGlobals() {
    global.window = {
        addEventListener() {
        },
        setTimeout: global.setTimeout,
    };
    global.document = createDocument();
}

// DOM setup needed for DOM-dependent tests (renderTooLargeDiagram, flashButtonLabel)
setupGlobals();
const jig = require('../../main/resources/templates/assets/jig-dom.js');
const jigMermaid = require('../../main/resources/templates/assets/jig-mermaid.js');

function resetDocument() {
    global.document = createDocument();
}

test.describe('jig-mermaid.js - DOM dependent functions', () => {
    test('renderTooLargeDiagramは案内表示を追加する', () => {
        resetDocument();

        const diagram = new Element('div');
        diagram.textContent = 'graph TD; A-->B;';

        jigMermaid.renderTooLargeDiagram(diagram, diagram.textContent);

        assert.equal(diagram.classList.contains('too-large'), true);
        assert.equal(diagram.textContent, '');
        assert.equal(diagram.children.length, 1);

        const container = diagram.children[0];
        assert.equal(container.className, 'mermaid-too-large');
        assert.equal(container.children.length, 2);

        const message = container.children[0];
        assert.equal(message.tagName, 'p');
        assert.equal(message.className, 'mermaid-too-large__message');
        assert.equal(message.textContent, '図の内容が大きすぎるため描画を省略しました。');

        const actions = container.children[1];
        assert.equal(actions.className, 'mermaid-too-large__actions');
        assert.equal(actions.children.length, 2);
        assert.equal(actions.children[0].tagName, 'button');
        assert.equal(actions.children[0].textContent, '上限を上げて描画する');
        assert.equal(actions.children[1].tagName, 'button');
        assert.equal(actions.children[1].textContent, '図の内容をコピー');
    });

    test('flashButtonLabelはラベルを戻す', () => {
        const button = new Element('button');
        button.textContent = '元のラベル';

        let timerCallback = null;
        const originalSetTimeout = global.window.setTimeout;
        global.window.setTimeout = (callback) => {
            timerCallback = callback;
        };

        try {
            jigMermaid.flashButtonLabel(button, '更新');
            assert.equal(button.textContent, '更新');
            assert.equal(typeof timerCallback, 'function');
            timerCallback();
            assert.equal(button.textContent, '元のラベル');
        } finally {
            global.window.setTimeout = originalSetTimeout;
        }
    });

    test.describe('isTooLarge', () => {
        test('閾値を超えるとtrueを返す', () => {
            const max = 'a'.repeat(50000);
            const over = 'a'.repeat(50001);

            assert.equal(jigMermaid.isTooLarge(max), false);
            assert.equal(jigMermaid.isTooLarge(over), true);
        });
    });

});

test.describe('typeLinkResolver', () => {
    test('デフォルトではnullに設定されている', () => {
        assert.equal(globalThis.Jig.dom.type.getResolver(), null);
    });
});
// Pure function tests have been moved to jig-glossary.test.js
