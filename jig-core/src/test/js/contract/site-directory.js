/**
 * Java 側の Contract テストが出力する生成サイトの場所と、その読み取り。
 *
 * 出力先は jig-core/build.gradle の contractSiteRoot と ShowcaseSiteContractTest が決めている。
 * ここはそれと対になる JS 側の唯一の参照点で、個々のテストではパスを組み立てない。
 */
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const SITE_DIRECTORY = path.resolve(__dirname, '../../../../build/contract-site/showcase');

/**
 * 生成サイトが無ければ、原因と対処が分かる形で止める。
 */
function assertSiteGenerated() {
    assert.ok(
        fs.existsSync(SITE_DIRECTORY),
        `生成サイトがありません: ${SITE_DIRECTORY} / 先に ./gradlew :jig-core:contractTest を実行してください`);
}

/**
 * データJSはグローバルへ代入する形式なので、ブラウザと同じ意味論で評価して結果を取り出す。
 */
function loadData(fileName) {
    const source = fs.readFileSync(path.join(SITE_DIRECTORY, 'data', fileName), 'utf-8');
    const container = {};
    new Function('globalThis', source)(container);
    return container;
}

module.exports = {SITE_DIRECTORY, assertSiteGenerated, loadData};
