/**
 * 生成 HTML の構造の契約。
 *
 * 文字列の一致ではなく DOM の必須構造で確認する。テンプレートの整形や属性の並びが
 * 変わっただけで壊れないようにするため。
 *
 * 入力は Java 側の ShowcaseSiteContractTest が出力する生成サイト。
 */
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {JSDOM} = require('jsdom');

const SITE_DIRECTORY = path.resolve(__dirname, '../../../../build/contract-site/showcase');

// ページと、そのページが読み込むデータJS
const PAGES = [
    ['index.html', 'navigation-data.js'],
    ['domain.html', 'domain-data.js'],
    ['usecase.html', 'usecase-data.js'],
    ['inbound.html', 'inbound-data.js'],
    ['outbound.html', 'outbound-data.js'],
    ['package.html', 'package-data.js'],
    ['glossary.html', 'glossary-data.js'],
    ['insight.html', 'insight-data.js'],
    ['list-output.html', 'list-output-data.js'],
    ['library-dependency.html', 'library-dependency-data.js'],
];

function parse(fileName) {
    const html = fs.readFileSync(path.join(SITE_DIRECTORY, fileName), 'utf-8');
    return new JSDOM(html).window.document;
}

function localReferences(document) {
    return [
        ...Array.from(document.querySelectorAll('link[href]')).map(el => el.getAttribute('href')),
        ...Array.from(document.querySelectorAll('script[src]')).map(el => el.getAttribute('src')),
    ].filter(url => !url.startsWith('http'));
}

test.describe('生成HTMLの構造', () => {

    test.before(() => {
        assert.ok(
            fs.existsSync(SITE_DIRECTORY),
            `生成サイトがありません: ${SITE_DIRECTORY} / 先に ./gradlew :jig-core:contractTest を実行してください`);
    });

    PAGES.forEach(([page, dataFile]) => {
        test.describe(page, () => {

            test('ロケールとページ種別が要素に現れる', () => {
                const document = parse(page);

                assert.equal(document.documentElement.getAttribute('lang'), 'ja');
                assert.ok(document.body.className.length > 0, 'bodyにページ種別のクラスがありません');
            });

            test('タイトルがある', () => {
                const title = parse(page).querySelector('title');

                assert.ok(title, 'titleがありません');
                assert.ok(title.textContent.trim().length > 0, 'titleが空です');
            });

            test('必要なデータとスクリプトを読み込む', () => {
                const document = parse(page);
                const scripts = Array.from(document.querySelectorAll('script[src]'))
                    .map(el => el.getAttribute('src'));

                assert.ok(scripts.some(src => src.includes(dataFile)), `${dataFile} を読み込んでいません`);
                assert.ok(scripts.some(src => src.includes('jig-bundle.js')), 'jig-bundle.js を読み込んでいません');
            });

            test('ローカル参照にはキャッシュバスティングのクエリが付く', () => {
                const document = parse(page);

                const references = localReferences(document);
                assert.ok(references.length > 0, 'ローカル参照がありません');
                references.forEach(url => {
                    assert.match(url, /\?v=[^&]+$/, `キャッシュバスティングがありません: ${url}`);
                });
            });

            test('外部ライブラリはバージョン固定で、クエリを付けない', () => {
                const document = parse(page);

                const externals = Array.from(document.querySelectorAll('script[src], link[href]'))
                    .map(el => el.getAttribute('src') ?? el.getAttribute('href'))
                    .filter(url => url.startsWith('http'));

                externals.forEach(url => {
                    assert.match(url, /@\d+\.\d+\.\d+\//, `バージョンが固定されていません: ${url}`);
                    assert.doesNotMatch(url, /\?v=/, `外部参照にキャッシュバスティングが付いています: ${url}`);
                });
            });
        });
    });

    test('ドキュメント名を表示するページのタイトルは翻訳対象になっている', () => {
        // index はタイトルが製品名なので翻訳対象外
        PAGES.filter(([page]) => page !== 'index.html').forEach(([page]) => {
            const title = parse(page).querySelector('title');

            assert.ok(title.hasAttribute('data-i18n'), `${page} のtitleがdata-i18nを持ちません`);
        });
    });

    test('index からすべてのページへ辿れる', () => {
        const {navigationData} = (() => {
            const source = fs.readFileSync(path.join(SITE_DIRECTORY, 'data', 'navigation-data.js'), 'utf-8');
            const container = {};
            new Function('globalThis', source)(container);
            return container;
        })();

        navigationData.links.forEach(link => {
            assert.ok(
                fs.existsSync(path.join(SITE_DIRECTORY, link.href)),
                `ナビゲーションのリンク先がありません: ${link.href}`);
        });
    });

    test('各ページから index へ戻れる', () => {
        PAGES.filter(([page]) => page !== 'index.html').forEach(([page]) => {
            const document = parse(page);
            const hrefs = Array.from(document.querySelectorAll('a[href]')).map(el => el.getAttribute('href'));

            assert.ok(hrefs.some(href => href.includes('index.html')), `${page} から index へ戻れません`);
        });
    });
});
