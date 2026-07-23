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
| 6 | `jig-core` の既存テストを分類して移す | 完了 |
| 7 | Playwright E2E の導入可否を判断する | 完了（見送り） |

必須段階（1〜5）は完了した。段階 6 は `learning` の分離、`TestSupport` のパス逆算解消に加え、`stub` を共有コーパスとして使っていた8個のテストすべてを、意図ごとの小さな fixture を使う形へ再実装した（詳細は後述）。段階 7 は必須段階が安定した後に判断し、現時点では自動導入を見送ると結論した（詳細は後述）。

各段階の完了条件は「新しい層に同等以上の公開契約テストがあり、CI で安定していること」とする。カバレッジ値だけを完了条件にしない。既存テストは一括で置換せず、同じ保証を保ったまま移してから旧テストを削除する。

## 残っている課題

- 実ブラウザ検証は手動手順（`jig-core/src/test/playwright/README.md`）に依存している。段階 7 で自動導入を見送ると判断した結果であり、意図的な運用である（トリガーは後述）。

## `stub` の共有コーパス依存を解消した経緯

`stub` はもともと、8個の `@JigTest` テスト（`JigExecutorTest` `JigDocumentGeneratorAssetsTest` `JigTypesTest` `JigServiceTest` `SpringDataJdbcStatementReaderTest` `MyBatisStatementReaderTest` `OutboundAdapterExecutionTest` `MethodSmellsTest`）が「1つの共有コーパス」として使っていた。`testing.JigTest`（JUnit拡張）が `stub` 全体を一括解析して `JigRepository` を注入する仕組みのため、`stub` は「1プロジェクト1意図」という代表プロジェクトの原則（`docs/adr/test_architecture_policy.md`）に反する寄せ集めになっていた。

既存の `stub` を `jig-test-fixtures` へ分解・移設するのではなく、**各テストを意図ごとの小さな fixture でゼロから再実装し、共有コーパスへの依存を断つ**方針で解消した。

- 実は解析結果を必要としていなかったテスト（`JigDocumentGeneratorAssetsTest`）は `@JigTest` を外して直接組み立てる形にした。
- 実質的な検証がなかったテスト（`JigServiceTest`）や、他の Contract テストと重複していたケース（`JigExecutorTest` の一部）は退役させた。
- 可視性判定・package-info の扱い（`JigTypesTest`）、メソッドスメル検出（`MethodSmellsTest`）は、`TestSupport.buildJigType(s)`（ASM で個別クラスを読む）を使い、テスト直下の小さなクラスに置き換えた。`stub` はもちろん `jig-test-fixtures` も不要だった。
- Spring Data JDBC の認識規則（`SpringDataJdbcStatementReaderTest` と `OutboundAdapterExecutionTest` の一部）は、`SpringDataJdbcStatementsReader.readFrom(JigTypes)` が**純粋関数**（ファイルシステム・クラスパス不要）であることを確認し、`org.dddjava.jig.domain.model.information.outbound.springdata.ut` 配下の小さな fixture クラス群を `TestSupport.buildJigTypes(...)` で読んで直接呼び出す形にした。
- MyBatis の認識規則（`MyBatisStatementReaderTest` と `OutboundAdapterExecutionTest` の残り）は、`MyBatisStatementsReader` が `Resources.getResourceAsStream`（MyBatis の内部API）で XML マッパーを読む際に**親クラスローダーへの委譲**で解決されることを確認し、`jig-core` 自身の `src/test/java` / `src/test/resources` に fixture を同居させた（`org.dddjava.jig.infrastructure.mybatis.ut`、`org.dddjava.jig.domain.model.information.outbound.mybatis.ut`）。jig-test-fixtures のような別モジュールに置くと、jig-core のテスト実行時クラスパスに別モジュールの成果物を混在させる形になるため避けた。

結果、`@JigTest` の共有コーパス経由でしか動かないテストはなくなった。`stub/domain/model/**` の一部は今も残っているが、これは ASM/JavaParser 層の Component テストが `TestSupport.buildJigType(Class)` で**個別クラスを対象に**読む、正しい規模の Component-test-local fixture であり、問題ではない。`sample/data`（Spring Data サンプル一式、約30）を専用入力にしていた `SampleDataWriterTest` は、アサーションのないコード生成タスクだったため `sample/data` ごと廃止した（issue #1126）。表示確認用サンプルデータの生成先だった `templates/data/` も不要になったため削除した。

## 用意する代表プロジェクト

`minimal-java` `showcase` `bytecode-compat` は作成済み。`spring-data` と `mybatis` は、上記のとおり `jig-test-fixtures` を使わずに解消したため作らない（前者は純粋関数なので不要、後者はクラスパス経由の読み込みのため jig-core ローカルに留めた）。`bytecode-only`（ソースなしクラスファイルの解析）だけが未着手で残る。

マルチモジュール構成と不正入力には専用の fixture を作らない。前者は既存の代表プロジェクトを並べて展開すれば足り、後者は壊れたクラスファイルをテスト内で書き出すほうがリポジトリに壊れたバイナリを置くより扱いやすいためである。

## スナップショットを導入していない理由

生成物の検証は DOM の必須構造・データ JS の形式・二度の実行による決定性で組み立てており、期待値をコミットするスナップショットは置いていない。`showcase` の成果物は 15,000 行を超え、全体を固定すると差分レビューが読めなくなるためである。

図や大きな文章について「構造では表せないが固定したい」対象が現れた時点で、その対象に限って導入する。そのときは更新を専用タスクで明示的に行い、通常のテスト実行中には更新しない。

## Playwright E2E を自動導入しないと判断した理由（段階 7）

Playwright は一度導入していた（2026-05、`98dc558af9`）。しかし OS 別 PNG スクリーンショットのスナップショット比較に依存する構成で、生成物の検証は構造・意味を優先するという方針（`docs/adr/test_architecture_policy.md` 6章）と相容れず、段階 6 の再編と同時期に撤去した（`4f0c36316e`）。

現在 `package.json` に `playwright` を入れていないのは「JS テストは jsdom で完結する」という判断による。これを覆すのは、Mermaid の実描画・CSS レイアウト・スクロール挙動など **jsdom が原理的に検証できない対象に限る**。それ以外は jsdom の Contract で足りる。撤去後は `jig-core/src/test/playwright/README.md` の手動手順で代替しており、この手順は実際に不具合を捕捉した実績がある（`bcdf45871`: Mermaid の securityLevel を strict にした修正）。自動化しなければ見逃す具体的な漏れは、現時点では確認できていない。

再導入する場合の前提として次の二点を見込む。

1. CI で `~/.cache/ms-playwright` をキャッシュする。
2. E2E の入力は `jig-cli:bootJar` と `showcase` fixture の解析で作るため、ビルド時間が上乗せされる。

所要時間が PR gate に見合わないと判明した場合は release candidate へ移す。自動検証の対象は Chromium のみとし、画面ごとに「ページが読み込める」「主要データが描画される」「主要導線が操作できる」を一シナリオに絞る（`docs/adr/browser_support_policy.md`）。`file://` で開けることは生成物の要件なので、E2E は HTTP サーバー経由と `file://` の両方を対象にする。Mermaid や CDN のような外部依存は、通常の CI がネットワークに依存しないよう固定・代替する。実 CDN との結線は release candidate に分離する。

再検討のトリガーは、jsdom が原理的に検証できない不具合（実描画・レイアウト・スクロール）が手動確認で繰り返し見つかる、または見落としによる実際の不具合流出が発生した時点とする。それまでは手動手順と jsdom ベースの Contract テストで運用する。

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
