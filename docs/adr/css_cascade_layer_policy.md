# CSSカスケードレイヤー（@layer）の使用方針

## 状況 (Context)

`common.css` では「コンポーネントの基本スタイル」を「コンテキストに応じた上書き」（例: `.sidebar--collapsed` 配下での非表示化、`.tab-content-section` 配下のテーマ切り替え）が詳細度の差で上書きする構成になっています。
この構成は stylelint の `no-descending-specificity` に違反として検出され、実害のない意図的な上書きにも抑制コメントが必要になり、可読性を損なっていました（#1096 #1097）。

詳細度に頼らず優先順位を明示する手段としてカスケードレイヤー（`@layer`）を導入します。

## 前提の確認

- **ブラウザ対応**: `@layer` は Chrome 99 / Firefox 97 / Safari 15.4（2022年3月）以降で利用可能。`common.css` は既に `:has()`（Chrome 105 / Firefox 121 / Safari 15.4 以降）を使用しているため、`@layer` の導入で対応ブラウザ要件は変わらない。
- **stylelint の挙動**: stylelint 17.14.0 で実験により確認。`no-descending-specificity` は `@layer` ブロック内のルールを比較対象から除外する（比較ペアの片側だけがレイヤー内にある場合も違反にならない）。
- **カスケードの原則**: 通常宣言では「レイヤーに属さないスタイル」が「レイヤー内のスタイル」より常に優先される。レイヤー間は `@layer` 文の宣言順で後のレイヤーが優先される。

## 決定 (Decision)

`common.css` の先頭で 2 つのレイヤーを宣言します。

```css
@layer base, components;
```

- **`base`**: 要素セレクタによるサイト全体のデフォルト（`a`, `a:hover`）。クラス指定のコンポーネントに常に負ける。
- **`components`**: コンポーネントの基本スタイルのうち、コンテキストによる上書きが存在するもの。
- **レイヤーに属さないスタイル（既定）**: 上記以外はすべて非レイヤーのまま。コンテキスト上書き（`.sidebar--collapsed ...` 等）や状態スタイル（`:hover` 等）は非レイヤーに置くことで、詳細度に関係なくレイヤー内の基本スタイルに勝つ。

### レイヤーを 2 層に留める理由

- 「上書きする側」を `overrides` レイヤーに入れなくても、非レイヤーであれば必ずレイヤー内に勝つため、`overrides` レイヤーは冗長。
- ページ固有CSS（`glossary.css` 等）も非レイヤーのままでよい。`common.css` の `components` レイヤーより常に優先されるため、ページ側での上書きが詳細度に依存しなくなる（現時点で移行対象クラスへのページCSSからの言及はなく、挙動変化はない）。
- 必要になったときにレイヤーを追加するのは容易（`@layer` 文に追記するだけ）だが、使われないレイヤーの削除は影響調査が必要。最小構成から始める。

## ガイドライン (Guidelines)

1. `@layer` に入れるのは「意図的に上書きされる側」だけ。上書きする側・状態スタイルは非レイヤーに置く。
2. レイヤー内で `!important` を使用しない。`!important` 同士ではレイヤー優先順位が反転する（`base` の `!important` が非レイヤーの `!important` に勝つ）ため、混乱の元になる。既存の `!important`（`.deprecated`）は非レイヤーにあり、通常宣言に対しては従来どおりすべてに勝つ。
3. レイヤー化は同一コンポーネントのルール群単位で行う。プロパティが競合するルール（例: `.mermaid-*-button` の共通スタイルと個別の `font-size` 指定）を片方だけレイヤー化すると、非レイヤー側が意図せず勝つため、セットで移動する。
4. 別の DOM subtree 同士の偶然の詳細度一致（上書き関係がないもの）に `@layer` を使わない。カスケード上の意味がないレイヤー化は誤解を招くため、従来どおり理由付きの `stylelint-disable` コメントで抑制する。

## 移行対象の割り当て（#1098 時点の 22 件）

### `components` レイヤーへ移行（12件、抑制コメントを削除）

| 移行するルール（上書きされる側） | 上書きする側（非レイヤーのまま） |
|---|---|
| `.entrypoint-item__io` | `.entrypoint-section--simplified .entrypoint-item__io` |
| `.jig-tabs` | `.tab-content-section .jig-tabs` / `.tab-mutual-dependency .jig-tabs` |
| `.jig-tab` | `.tab-content-section .jig-tab` |
| `.jig-tab.active` | `.tab-content-section .jig-tab.active` |
| `.mermaid-diagram` | `body.hide-domain-diagrams ...` / `body.hide-usecase-diagrams ...` |
| `.in-page-sidebar__list` | `.sidebar--collapsed .in-page-sidebar__list` |
| `.in-page-sidebar__package-link` | `.in-page-sidebar__item-header > .in-page-sidebar__package-link` |
| `.in-page-sidebar__item-header` | `.in-page-sidebar__list .in-page-sidebar__item-header` |
| `.in-page-sidebar__link` | `.in-page-sidebar__item-header > ...` / `.in-page-sidebar__title ...` |
| `.sidebar-settings` | `.sidebar--collapsed .sidebar-settings` |
| `.sidebar-filter` | `.sidebar--collapsed .sidebar-filter` |
| `.mermaid-*-button` の共通スタイルと位置指定 | `.mermaid-diagram:hover > ...` / `.mermaid-diagram ...:hover` |

### `base` レイヤーへ移行（違反ではないが移行に必要）

| ルール | 理由 |
|---|---|
| `a` / `a:hover` | `.in-page-sidebar__link` 等（`<a>` 要素）を `components` に入れると、非レイヤーの `a { color: #000 }` が詳細度に関係なく勝ってしまうため、さらに下のレイヤーに置く |

### 抑制コメントを維持（10件）

別の DOM subtree 同士の偶然の詳細度一致であり、上書き関係がないため `@layer` の対象外（ガイドライン4）。

- `.mermaid svg a:hover`（vs ヘッダーの言語切替リンク）
- `.metric-description h3`（vs glossaryカード見出し）
- `.in-page-sidebar__item-header > ...`（vs ヘッダーの言語切替リンク）
- `.sidebar-settings > summary` 3件（vs 用語カードのsummary）
- `.package-heading h2`（vs ヘルプパネル）
- `.controller-group-header > td`（vs entrypointサマリ表）
- `.controller-group-header__inner > a` / 同 `:hover`（vs グローバルヘッダー）

## トレードオフ (Trade-Offs)

- メリット
  - 意図的な上書きが詳細度でなくレイヤーで表現され、抑制コメントが不要になる
  - ページCSSや上書き側のセレクタを、詳細度を稼ぐためだけに長くする必要がなくなる
- デメリット
  - 「レイヤー内は非レイヤーに常に負ける」というカスケードの理解が読み手に必要になる
  - レイヤー化したルールに後から競合プロパティを持つ非レイヤーのルールを足すと、ソース順や詳細度に反して非レイヤー側が勝つ（ガイドライン3の考慮が継続的に必要）
