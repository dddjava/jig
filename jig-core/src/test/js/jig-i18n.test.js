const test = require('node:test');
const assert = require('node:assert/strict');
const {JSDOM} = require('jsdom');

test.describe('jig-i18n.js', () => {
    let Jig;

    function setupDom(bodyHtml, {lang = 'ja', titleHtml = '<title data-i18n>インサイト</title>'} = {}) {
        const dom = new JSDOM(`<!DOCTYPE html><html lang="${lang}"><head>${titleHtml}</head><body>${bodyHtml}</body></html>`);
        global.window = dom.window;
        global.document = dom.window.document;
        global.NodeFilter = dom.window.NodeFilter;
    }

    function loadI18n() {
        require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        return require('../../main/resources/templates/assets/jig-i18n.js');
    }

    test.beforeEach(() => {
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-util.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-data.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-i18n.js')];
        delete globalThis.Jig;
        delete globalThis.navigationData;
    });

    test.afterEach(() => {
        delete global.window;
        delete global.document;
        delete global.NodeFilter;
    });

    test('locale=ja のときは翻訳しない', () => {
        setupDom('<h1 data-i18n>インサイト</h1>');
        Jig = loadI18n();
        globalThis.navigationData = {locale: 'ja', links: []};

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, 'インサイト');
        assert.equal(document.title, 'インサイト');
    });

    test('data-i18n を持つ要素のみ翻訳する', () => {
        setupDom('<h1 data-i18n>インサイト</h1><p>インサイト</p>');
        Jig = loadI18n();
        globalThis.navigationData = {locale: 'en', links: []};

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, 'Insight');
        assert.equal(document.querySelector('p').textContent, 'インサイト');
        assert.equal(document.title, 'Insight');
        assert.equal(document.documentElement.lang, 'en');
    });

    test('data-i18n="key" で明示キーを指定できる', () => {
        Jig = (() => {
            // dictionaries に明示キーを追加して試す
            setupDom('<button data-i18n="custom.label">なにか</button>');
            const i18n = loadI18n();
            i18n.dictionaries.en['custom.label'] = 'Custom Label';
            return i18n;
        })();
        globalThis.navigationData = {locale: 'en', links: []};

        Jig.apply();

        assert.equal(document.querySelector('button').textContent, 'Custom Label');
    });

    test('辞書に無いキーはそのまま', () => {
        setupDom('<h1 data-i18n>未知の単語</h1>');
        Jig = loadI18n();
        globalThis.navigationData = {locale: 'en', links: []};

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, '未知の単語');
    });

    test('locale 未指定なら <html lang> をフォールバックに使う', () => {
        setupDom('<h1 data-i18n>インサイト</h1>', {lang: 'en'});
        Jig = loadI18n();

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, 'Insight');
    });

    test('data-i18n のない要素は同じ語でも放置する', () => {
        setupDom('<p>入力</p><li>出力</li><dd>フィールド</dd>');
        Jig = loadI18n();
        globalThis.navigationData = {locale: 'en', links: []};

        Jig.apply();

        assert.equal(document.querySelector('p').textContent, '入力');
        assert.equal(document.querySelector('li').textContent, '出力');
        assert.equal(document.querySelector('dd').textContent, 'フィールド');
    });
});
