# テストアーキテクチャ移行 残作業

## この文書の位置づけ

テストの設計判断は `docs/adr/test_architecture_policy.md`（テスト全般）と `docs/adr/browser_support_policy.md`（実ブラウザ検証・Playwright）にある。この文書は**残っている作業だけ**を管理する。残作業がなくなったら不要になる。

移行の段階1〜7（ADR の書き起こし、契約テスト整備と JaCoCo、`jig-test-fixtures` と互換性契約、公開シナリオの固定、生成物検証の構造比較化、`jig-core` 既存テストの分類、Playwright 自動導入可否の判断）はすべて完了した。完了した内容は実装と上記 ADR が唯一の情報源であり、ここには残さない。

## 残っている作業

- `bytecode-only`（ソースなしクラスファイルの解析）代表プロジェクトが未着手。

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
