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
        global.CustomEvent = dom.window.CustomEvent;
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
        delete global.CustomEvent;
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
        setupDom('<button data-i18n="custom.label">なにか</button>');
        Jig = loadI18n();
        Jig.builtinDictionaries.en['custom.label'] = 'Custom Label';
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
        globalThis.navigationData = {};

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

    test('複数の data-i18n 要素が辞書から翻訳される', () => {
        setupDom('<h1 data-i18n>インサイト</h1><button data-i18n>入力</button>');
        Jig = loadI18n();
        globalThis.navigationData = {locale: 'en', links: []};

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, 'Insight');
        assert.equal(document.querySelector('button').textContent, 'Input');
    });

    test('setLanguage で切り替えられ、ja に戻すと原文が復元する', () => {
        setupDom('<h1 data-i18n>インサイト</h1><button data-i18n>入力</button>');
        Jig = loadI18n();
        globalThis.navigationData = {locale: 'ja', links: []};

        Jig.apply();
        assert.equal(document.querySelector('h1').textContent, 'インサイト');

        Jig.setLanguage('en');
        assert.equal(document.querySelector('h1').textContent, 'Insight');
        assert.equal(document.querySelector('button').textContent, 'Input');
        assert.equal(document.documentElement.lang, 'en');
        assert.equal(Jig.currentLanguage(), 'en');

        Jig.setLanguage('ja');
        assert.equal(document.querySelector('h1').textContent, 'インサイト');
        assert.equal(document.querySelector('button').textContent, '入力');
        assert.equal(document.documentElement.lang, 'ja');
        assert.equal(Jig.currentLanguage(), 'ja');
    });

    test('setLanguage は jig:locale-change イベントを発火する', () => {
        setupDom('<h1 data-i18n>インサイト</h1>');
        Jig = loadI18n();
        globalThis.navigationData = {locale: 'ja', links: []};

        const received = [];
        document.addEventListener('jig:locale-change', e => received.push(e.detail?.lang));

        Jig.setLanguage('en');
        Jig.setLanguage('ja');

        assert.deepEqual(received, ['en', 'ja']);
    });

    test('availableLanguages は ja と builtinDictionaries のキーから導出する', () => {
        setupDom('');
        Jig = loadI18n();

        const langs = Jig.availableLanguages();
        assert.ok(langs.includes('ja'));
        assert.ok(langs.includes('en'));
    });
});
