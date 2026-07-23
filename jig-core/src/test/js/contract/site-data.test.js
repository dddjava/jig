/**
 * JIG が生成したサイトと、それを読むブラウザ資産との境界の契約。
 *
 * 入力は showcase fixture を解析した生成物で、Java 側の ShowcaseSiteContractTest が出力する。
 * 先に `./gradlew :jig-core:contractTest` を実行しておく必要がある（npm run test:contract がまとめて行う）。
 */
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const SITE_DIRECTORY = path.resolve(__dirname, '../../../../build/contract-site/showcase');

/**
 * データJSはグローバルへ代入する形式なので、ブラウザと同じ意味論で評価して結果を取り出す。
 */
function loadData(fileName) {
    const source = fs.readFileSync(path.join(SITE_DIRECTORY, 'data', fileName), 'utf-8');
    const container = {};
    new Function('globalThis', source)(container);
    return container;
}

function readHtml(fileName) {
    return fs.readFileSync(path.join(SITE_DIRECTORY, fileName), 'utf-8');
}

test.describe('生成サイトとブラウザ資産の境界', () => {

    test.before(() => {
        assert.ok(
            fs.existsSync(SITE_DIRECTORY),
            `生成サイトがありません: ${SITE_DIRECTORY} / 先に ./gradlew :jig-core:contractTest を実行してください`);
    });

    test.describe('データJS', () => {
        // ページのJSが参照するグローバル名。変わると読み込み側が壊れる
        const DATA_GLOBALS = [
            ['domain-data.js', 'domainData'],
            ['glossary-data.js', 'glossaryData'],
            ['inbound-data.js', 'inboundData'],
            ['outbound-data.js', 'outboundData'],
            ['usecase-data.js', 'usecaseData'],
            ['package-data.js', 'packageData'],
            ['insight-data.js', 'insightData'],
            ['list-output-data.js', 'listData'],
            ['library-dependency-data.js', 'libraryDependencyData'],
            ['navigation-data.js', 'navigationData'],
            ['type-relations-data.js', 'typeRelationsData'],
        ];

        DATA_GLOBALS.forEach(([fileName, globalName]) => {
            test(`${fileName} は globalThis.${globalName} を定義する`, () => {
                const container = loadData(fileName);

                assert.notEqual(container[globalName], undefined);
            });
        });

        test('ドメインモデルのデータは型とパッケージ根を持つ', () => {
            const {domainData} = loadData('domain-data.js');

            assert.ok(Array.isArray(domainData.types));
            assert.ok(domainData.types.length > 0);
            assert.ok(Array.isArray(domainData.domainPackageRoots));

            const order = domainData.types.find(type => type.fqn === 'showcase.domain.order.Order');
            assert.ok(order, 'ドメインの型が含まれていません');
            assert.ok(Array.isArray(order.fields));
            assert.ok(Array.isArray(order.methods));
        });

        test('用語集のデータはJavadoc由来の和名を持つ', () => {
            const {glossaryData} = loadData('glossary-data.js');

            const terms = Object.values(glossaryData.terms);
            assert.ok(terms.length > 0);
            assert.ok(JSON.stringify(terms).includes('注文番号'));
        });

        test('ナビゲーションのデータは各ページへのリンクを持つ', () => {
            const {navigationData} = loadData('navigation-data.js');

            assert.ok(Array.isArray(navigationData.links));
            navigationData.links.forEach(link => {
                assert.equal(typeof link.href, 'string');
                assert.ok(link.href.endsWith('.html'), `hrefがページを指していません: ${link.href}`);
            });
        });

        test('引用符と山括弧を含む説明でもJSとして壊れない', () => {
            const {glossaryData} = loadData('glossary-data.js');

            // fixture の Quantity に仕込んだ説明。評価できている時点でエスケープは成立している
            const serialized = JSON.stringify(glossaryData);
            assert.ok(serialized.includes('数量'), '説明が読み取れていません');
        });
    });

    test.describe('HTML', () => {
        const PAGES = [
            ['index.html', null],
            ['domain.html', 'domain-data.js'],
            ['usecase.html', 'usecase-data.js'],
            ['inbound.html', 'inbound-data.js'],
            ['outbound.html', 'outbound-data.js'],
            ['package.html', 'package-data.js'],
            ['glossary.html', 'glossary-data.js'],
            ['insight.html', 'insight-data.js'],
        ];

        PAGES.forEach(([page, dataFile]) => {
            test(`${page} は必要なデータとアセットを読み込む`, () => {
                const html = readHtml(page);

                assert.ok(html.includes('<html'), 'HTMLの体裁になっていません');
                assert.ok(html.includes('assets/'), 'アセットを参照していません');
                if (dataFile) {
                    assert.ok(html.includes(dataFile), `${dataFile} を読み込んでいません`);
                }
            });
        });

        test('外部ライブラリはバージョンを固定して読み込む', () => {
            const html = readHtml('domain.html');

            // 固定を外すと生成物の描画が外部の更新で変わる
            const cdnScripts = html.match(/https:\/\/cdn\.jsdelivr\.net\/npm\/[^"]+/g) ?? [];
            assert.ok(cdnScripts.length > 0, 'CDNスクリプトがありません');
            cdnScripts.forEach(url => {
                assert.match(url, /@\d+\.\d+\.\d+\//, `バージョンが固定されていません: ${url}`);
            });
        });
    });
});
