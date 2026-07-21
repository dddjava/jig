/**
 * jsdom で組み立てたブラウザ環境をグローバルに載せ降ろしするテスト用ヘルパーです。
 *
 * アセットのスクリプトは `document` や `CustomEvent` をグローバルとして参照するため、
 * テスト側で jsdom の window から写す必要があります。この写し取りが各テストファイルに
 * 散らばると、必要なグローバルが増えたときの追従漏れが起きるためここに集約します。
 *
 * ブラウザAPIのうちここで用意するものだけがテスト中に存在します。列に加えると
 * `typeof globalThis.xxx` で分岐しているコード（getComputedStyle / MutationObserver など）
 * の通る経路が変わるため、実際に必要になったタイミングで追加してください。
 */

const {JSDOM} = require('jsdom');

const WINDOW_GLOBALS = ['document', 'location', 'Event', 'CustomEvent', 'NodeFilter'];

const DEFAULT_HTML = '<!DOCTYPE html><html><body></body></html>';

/**
 * @param {string} [html] ページのHTML
 * @returns {JSDOM} 生成した jsdom。window 固有の値が必要な場合に使う
 */
function setupDom(html = DEFAULT_HTML) {
    const dom = new JSDOM(html);
    global.window = dom.window;
    WINDOW_GLOBALS.forEach(name => {
        global[name] = dom.window[name];
    });
    return dom;
}

function teardownDom() {
    delete global.window;
    WINDOW_GLOBALS.forEach(name => {
        delete global[name];
    });
}

module.exports = {setupDom, teardownDom};
