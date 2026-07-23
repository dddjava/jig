# テストアーキテクチャ移行計画

## この文書の位置づけ

テストの設計判断は `docs/adr/test_architecture_policy.md` にある。この文書は**そこへ到達するまでの残作業だけ**を管理する。移行が完了したら不要になる。

完了した段階は実装が唯一の情報源になるため、ここから除く。

| 段階 | 内容 | 状態 |
| --- | --- | --- |
| 1 | ADR の書き起こし | 完了 |
| 2 | `contractTest` / `e2eTest` / `qualityCheck` と JaCoCo | 完了 |
| 3 | `jig-test-fixtures` と解析対象バージョンの契約検証 | 完了 |
| 4 | 最重要の公開シナリオを E2E/Contract で固定する | 完了 |
| 5 | 生成物の検証を構造比較へ置き換える | 完了 |
| 6 | `jig-core` の既存テストを分類して移す | — |
| 7 | Playwright E2E の導入可否を判断する | — |

必須段階（1〜5）は完了した。残る 6・7 は必須段階が安定してから着手する。

各段階の完了条件は「新しい層に同等以上の公開契約テストがあり、CI で安定していること」とする。カバレッジ値だけを完了条件にしない。既存テストは一括で置換せず、同じ保証を保ったまま移してから旧テストを削除する。

## 残っている課題

- `jig-core` の `src/test/java` にある fixture 相当が約 170。内訳は `stub` 配下に約 110、それ以外に約 60（`sample/data` の Spring Data サンプル一式、`infrastructure/asm/ut` と `.../javaparser/ut` の解析対象、`org/springframework` 配下の模造アノテーション）。
- `testing.TestSupport` が `DefaultPackageClass.class` の位置からテスト出力ディレクトリを逆算している。`JigFixtures` へ置き換えて消す。
- 調査用の `learning` が `src/test/java/learning` と `src/test/java/org/dddjava/jig/infrastructure/javaparser/learning` の二箇所にあり、CI で実行されている。
- 実ブラウザ検証は手動手順（`jig-core/src/test/playwright/README.md`）に依存している。

## 用意する代表プロジェクト

`minimal-java` `showcase` `bytecode-compat` は作成済み。残りは段階 6 で既存テストを移すときに追加する。追加手順は `jig-test-fixtures/README.md` を参照。

| fixture | 固定する契約 |
| --- | --- |
| `spring-data` | Spring Data JDBC の認識規則 |
| `mybatis` | Mapper と SQL の認識規則 |
| `bytecode-only` | ソースなしクラスファイルの解析 |

マルチモジュール構成と不正入力には専用の fixture を作らない。前者は既存の代表プロジェクトを並べて展開すれば足り、後者は壊れたクラスファイルをテスト内で書き出すほうがリポジトリに壊れたバイナリを置くより扱いやすいためである。

## スナップショットを導入していない理由

生成物の検証は DOM の必須構造・データ JS の形式・二度の実行による決定性で組み立てており、期待値をコミットするスナップショットは置いていない。`showcase` の成果物は 15,000 行を超え、全体を固定すると差分レビューが読めなくなるためである。

図や大きな文章について「構造では表せないが固定したい」対象が現れた時点で、その対象に限って導入する。そのときは更新を専用タスクで明示的に行い、通常のテスト実行中には更新しない。

## Playwright E2E を導入するかの判断（段階 7）

現在 `package.json` に `playwright` を入れていないのは「JS テストは jsdom で完結する」という判断による。これを覆すのは、Mermaid の実描画・CSS レイアウト・スクロール挙動など **jsdom が原理的に検証できない対象に限る**。それ以外は jsdom の Contract で足りる。

導入する場合の前提として次の二点を見込む。

1. CI で `~/.cache/ms-playwright` をキャッシュする。
2. E2E の入力は `jig-cli:bootJar` と `showcase` fixture の解析で作るため、ビルド時間が上乗せされる。

所要時間が PR gate に見合わないと判明した場合は release candidate へ移す。自動検証の対象は Chromium のみとし、画面ごとに「ページが読み込める」「主要データが描画される」「主要導線が操作できる」を一シナリオに絞る（`docs/adr/browser_support_policy.md`）。`file://` で開けることは生成物の要件なので、E2E は HTTP サーバー経由と `file://` の両方を対象にする。

Mermaid や CDN のような外部依存は、通常の CI がネットワークに依存しないよう固定・代替する。実 CDN との結線は release candidate に分離する。

## CI の残作業

PR gate と main upkeep は現在の構成のままでよい。未実装なのは release candidate の検証である。GitHub Actions のパイプラインは既存の三本を維持し、新設しない。

| パイプライン | 追加する検証 |
| --- | --- |
| release candidate | 配布物から CLI・プラグインを利用する E2E、外部ライブラリ・実 CDN との結線、脆弱性確認 |

これはタグ push で動く `release.yml` のリリースビルドに追加する。CircleCI を変更しない方針のため、次の非対称を持つ。

- 検証が失敗すると Draft リリースが作られず、GitHub Releases 経由の `jig-cli.jar` 配布は止まる。
- Maven Central と Gradle Plugin Portal への公開は CircleCI の `publish` ジョブが担っており、`release.yml` の失敗では止まらない。

CircleCI 廃止までの既知の制約として受け入れる。配布物からプラグインを利用する E2E は、Gradle Plugin Portal への公開を待たず `publishToMavenLocal` した成果物を TestKit から解決して行う。公開前に検証できる形を維持するためである。

失敗時には JUnit XML、HTML レポート、失敗した fixture の入力と正規化後の期待・実際の差分、Playwright の screenshot/video/trace を保持する。成功時に巨大な成果物を保存しない。

性能とメモリ消費に自動の合否判定は置かない。必要になった時点で自己解析を入力とした計測を手動で行い、退行が疑われる変更のレビューで参照する。

### CircleCI 廃止作業の完了条件

CircleCI は廃止する方向だが、廃止までは Maven Central と Gradle Plugin Portal への公開を担う。廃止は別スケジュールで扱い、この移行では CircleCI の設定を変更しない。廃止作業には次を含める。

- release candidate の検証を公開タスクの前に置き、公開のゲートとして機能させる。
- `jig-test-fixtures/build.gradle.kts` の `CIRCLECI` 判定（最新 LTS 向けクラスファイルの生成を外している）を削除する。
- `JigPluginFunctionalTest` の `@DisabledIfEnvironmentVariable`（メモリ上限による無効化）を削除する。
