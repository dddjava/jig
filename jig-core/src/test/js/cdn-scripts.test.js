/**
 * 生成HTMLが読み込むCDNスクリプトのバージョン契約。
 *
 * package.json の依存はテストだけが使い、実際にブラウザで動くのは cdn-scripts.html の
 * 固定バージョン。dependabot は package.json しか更新しないため、両者は放っておくとずれる。
 */
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const REPOSITORY_ROOT = path.resolve(__dirname, '../../../..');
const CDN_SCRIPTS_HTML = path.join(REPOSITORY_ROOT, 'jig-core/src/main/resources/templates/partials/cdn-scripts.html');

// <script src="https://cdn.jsdelivr.net/npm/<name>@<version>/..."> から name と version を取る
const CDN_SCRIPT_PATTERN = /https:\/\/cdn\.jsdelivr\.net\/npm\/(@?[^@/]+(?:\/[^@/]+)?)@([^/]+)\//g;

function cdnScripts() {
    const html = fs.readFileSync(CDN_SCRIPTS_HTML, 'utf-8');
    return Array.from(html.matchAll(CDN_SCRIPT_PATTERN)).map(([, name, version]) => ({name, version}));
}

function devDependencies() {
    const packageJson = JSON.parse(fs.readFileSync(path.join(REPOSITORY_ROOT, 'package.json'), 'utf-8'));
    return packageJson.devDependencies ?? {};
}

test.describe('cdn-scripts.html', () => {

    test('CDNスクリプトを1つ以上読み込む', () => {
        assert.ok(cdnScripts().length > 0, 'CDNスクリプトの抽出に失敗している');
    });

    test('バージョンを固定して読み込む', () => {
        cdnScripts().forEach(({name, version}) => {
            assert.match(version, /^\d+\.\d+\.\d+$/, `${name} のバージョン指定が固定されていない`);
        });
    });

    test('package.json に同名の依存があるものはバージョンが一致する', () => {
        const dependencies = devDependencies();
        const compared = cdnScripts().filter(({name}) => name in dependencies);

        // 比較対象が消えるとテストが素通りするため、対象があること自体を確認する
        assert.ok(compared.length > 0, 'package.json と突き合わせられるCDNスクリプトがない');

        compared.forEach(({name, version}) => {
            assert.equal(version, dependencies[name],
                `${name} が package.json(${dependencies[name]}) と cdn-scripts.html(${version}) でずれている。`
                + 'テストが検証しているバージョンと利用者のブラウザで動くバージョンを揃えること。');
        });
    });
});
