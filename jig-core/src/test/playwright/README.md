# Playwrightでの見た目確認手順

`usecase.html` など Mermaid 図を含むテンプレートの変更を、実際のブラウザで確認するための手順。

## 1. ブラウザバイナリの準備

```bash
ls ~/.cache/ms-playwright || npx playwright install
```

## 2. playwright パッケージの用意

このリポジトリの `package.json` には `playwright` を依存に入れていない（JSテストは jsdom で完結するため）。
スクラッチパッド等、リポジトリ外の一時ディレクトリにローカルインストールして使う。

```bash
mkdir -p /tmp/pw-check && cd /tmp/pw-check
npm init -y >/dev/null
npm install --no-save playwright
```

`~/.cache/ms-playwright` にバイナリがキャッシュ済みなら再ダウンロードは走らない。

## 3. 自己解析結果（サンプルHTML一式）の生成

JIG は自分自身を解析対象にできる。リポジトリルートで:

```bash
# テンプレート/JS/CSSだけでなくJavaも変更した場合はjarを作り直す
./gradlew :jig-cli:bootJar

# リポジトリルートを解析してプロジェクトルート直下の ./build/jig に出力する
java -jar jig-cli/build/libs/jig-cli.jar
```

`./build/jig/` に `usecase.html` 等と `assets/`（JS/CSS）、`data/`（解析結果JSON）一式が生成される。

**テンプレートHTML自体は変えず `assets/*.js` `assets/*.css` だけを変更した場合**は、jarの再ビルド・再実行をせずに該当ファイルを直接コピーするほうが速い（`build/` は `.gitignore` 対象なのでコピーしても差分は汚さない）。

```bash
cp jig-core/src/main/resources/templates/assets/usecase.js build/jig/assets/usecase.js
```

ただし `jig-*.js`（jig-util / jig-i18n 等）を変更した場合、ページが読み込むのはビルド時に結合される `jig-bundle.js` なので個別コピーでは反映されない。手動結合はビルド定義との乖離リスクがあるためせず、素直に `bootJar` → 実行をやり直すこと。

`templates/*.html` 自体（`{{...}}` プレースホルダ）を変更した場合は、プレースホルダ展開が必要なため上記コピーでは反映されない。素直に `bootJar` → 実行をやり直す。

## 4. 簡易サーバーで配信

```bash
python3 -m http.server 8791 --directory build/jig &
```

## 5. Playwrightで操作・スクリーンショット

`node -e "..."` でも動くが、複雑な操作は一時スクリプトに書いて `node` で実行するほうが扱いやすい。

```js
const { chromium } = require('playwright'); // /tmp/pw-check 側の node_modules を使う
(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1600, height: 1200 } });
  page.on('pageerror', err => console.log('PAGEERROR:', err.message));
  await page.goto('http://localhost:8791/usecase.html');
  await page.waitForTimeout(1500); // 初回描画・mermaid.run() の完了待ち

  const el = page.locator('.package-diagram').first(); // class-diagram / package-diagram など
  await el.scrollIntoViewIfNeeded();
  await el.screenshot({ path: '/tmp/pw-check/out.png' });

  await browser.close();
})();
```

**注意**: `jig-core/src/test/playwright` からの相対パスで実行する場合、`require('playwright')` はモジュール解決できない。`/tmp/pw-check` など playwright を `npm install` したディレクトリで `node` を実行するか、`NODE_PATH` でそのディレクトリを指定すること。

## 図のコンテキストメニュー（右クリックメニュー）を操作する

Mermaid図の右上の「⋮」ボタンから開くカスタムメニュー。**`button: 'right'` の右クリックはブラウザ標準メニューが開くだけで、カスタムメニュー項目はDOM上に存在してもテキスト取得はできてもクリックできない（非表示扱い）。** 必ずボタン経由で開くこと。

```js
await el.hover(); // ボタンはhover時にのみ表示される
await el.locator('.mermaid-menu-button').click();
await page.waitForTimeout(300);
await el.locator('.mermaid-menu-item', { hasText: 'メソッド単位' }).click();
await page.waitForTimeout(500); // 再描画待ち
```

## 既知の癖

- locator の `hasText` は部分一致。タブ名などで「クラス関連図」を指定すると「パッケージ内クラス関連図」にも一致する。完全一致には正規表現（`{ hasText: /^クラス関連図$/ }`）を使うこと。
- `fullPage: true` のスクリーンショットは `position: sticky` 要素（サイドバー等）をスクロール合成時に重複描画することがある。sticky要素の検証はビューポート単位のスクロールで行うこと（`~/.claude/CLAUDE.md` にも同様の注記あり）。
- 図の存在確認には `.diagram-container` の派生クラス（`.class-diagram` `.package-diagram` 等、`usecase.js` の `Jig.dom.createElement("div", {className: "jig-card-section diagram-container ..."})` 呼び出しを参照）を使うと的確に絞り込める。
- 自己解析結果（`build/jig`）のクラス数は少ないため、「特定の条件（1クラスのみのパッケージ等）を満たすデータ」がサンプルに存在しないことがある。無理に実データで揃えようとせず、次項のjsdom比較かPlaywright側でモックデータ（`globalThis.usecaseData = {...}` を差し込んでから `UsecaseApp.init()`）を使うほうが早い。

## ブラウザを起動せずMermaidソースをテキスト比較する（軽量版）

見た目の一致・差分を「スクリーンショットの目視」ではなく「生成されたMermaidソース文字列の diff」で確認したい場合、jsdom + 対象JSファイルの直接requireで十分なことが多い。ブラウザ起動より高速。

```js
const { JSDOM } = require('jsdom'); // jig-core/src/test/js で使っているのと同じ依存。プロジェクトルートで実行すること
const dom = new JSDOM('<!DOCTYPE html><html><body></body></html>');
global.document = dom.window.document;
global.window = dom.window;
global.marked = { parse: t => t };
global.DOMPurify = { sanitize: h => h };

// usecase.test.js と同じ順序でrequireする。jig-bootstrap.js を忘れると
// 「Cannot read properties of undefined (reading 'register')」でusecase.js末尾のグローバル登録が落ちる
require('./jig-core/src/main/resources/templates/assets/jig-util.js');
require('./jig-core/src/main/resources/templates/assets/jig-data.js');
require('./jig-core/src/main/resources/templates/assets/jig-glossary.js');
require('./jig-core/src/main/resources/templates/assets/jig-mermaid.js');
require('./jig-core/src/main/resources/templates/assets/jig-dom.js');
require('./jig-core/src/main/resources/templates/assets/jig-bootstrap.js');
const UsecaseApp = require('./jig-core/src/main/resources/templates/assets/usecase.js');

// 以降、UsecaseApp.createXxxDiagramGenerator(...) を呼んで文字列比較する
```

**注意**: このスクリプトを `/tmp` や scratchpad に置いて `node` で実行すると `jsdom` が解決できず `MODULE_NOT_FOUND` になる（`node_modules` はプロジェクトルートにしかない）。一時ファイルとして書く場合もリポジトリルート直下に置いて実行し、確認が終わったら削除すること。
