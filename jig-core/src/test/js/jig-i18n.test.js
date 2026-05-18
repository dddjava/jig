const test = require('node:test');
const assert = require('node:assert/strict');
const {JSDOM} = require('jsdom');

test.describe('jig-i18n.js', () => {
    let Jig;

    function setupDom(htmlBody, lang = 'ja') {
        const dom = new JSDOM(`<!DOCTYPE html><html lang="${lang}"><head><title>インサイト</title></head><body>${htmlBody}</body></html>`);
        global.window = dom.window;
        global.document = dom.window.document;
        global.NodeFilter = dom.window.NodeFilter;
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
        setupDom('<h1>インサイト</h1>', 'ja');
        require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        Jig = require('../../main/resources/templates/assets/jig-i18n.js');
        globalThis.navigationData = {locale: 'ja', links: []};

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, 'インサイト');
        assert.equal(document.title, 'インサイト');
    });

    test('locale=en のときは辞書に従って翻訳する', () => {
        setupDom('<h1>インサイト</h1><p>入力</p>', 'ja');
        require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        Jig = require('../../main/resources/templates/assets/jig-i18n.js');
        globalThis.navigationData = {locale: 'en', links: []};

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, 'Insight');
        assert.equal(document.querySelector('p').textContent, 'Input');
        assert.equal(document.title, 'Insight');
        assert.equal(document.documentElement.lang, 'en');
    });

    test('script/style 配下のテキストは翻訳しない', () => {
        setupDom('<script>const x = "インサイト";</script><h1>インサイト</h1>', 'ja');
        require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        Jig = require('../../main/resources/templates/assets/jig-i18n.js');
        globalThis.navigationData = {locale: 'en', links: []};

        Jig.apply();

        assert.match(document.querySelector('script').textContent, /インサイト/);
        assert.equal(document.querySelector('h1').textContent, 'Insight');
    });

    test('辞書に無い文字列はそのまま', () => {
        setupDom('<h1>未知の単語</h1>', 'ja');
        require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        Jig = require('../../main/resources/templates/assets/jig-i18n.js');
        globalThis.navigationData = {locale: 'en', links: []};

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, '未知の単語');
    });

    test('locale 未指定なら <html lang> をフォールバックに使う', () => {
        setupDom('<h1>インサイト</h1>', 'en');
        require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        Jig = require('../../main/resources/templates/assets/jig-i18n.js');
        // navigationData なし

        Jig.apply();

        assert.equal(document.querySelector('h1').textContent, 'Insight');
    });
});
