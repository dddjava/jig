# JIG テストアーキテクチャ

## 目的

JIG の品質保証を、実装クラス単位のテストの寄せ集めから、利用者が観測する契約を中心に組み立て直す。対象は `jig-core`、CLI、Gradle プラグイン、生成されるドキュメント、およびブラウザ上の資産である。

この文書は目標アーキテクチャと移行順序を定める設計書であり、既存テストの移動・削除・追加やビルド設定の変更は含まない。

## 現状

移行の起点となる現在の構成は次のとおり。

- モジュールは `jig-core` `jig-cli` `jig-gradle-plugin` の三つ。各モジュールのテストは `src/test` 単一のソースセット。
- `jig-core` の `src/test/java` にある約 225 の Java ファイルのうち、テストクラスは約 50。残りは解析対象として使う fixture であり、`stub` 配下に約 110、それ以外に約 60 ある（`sample/data` の Spring Data サンプル一式、`org/dddjava/jig/infrastructure/asm/ut` `.../javaparser/ut` の解析対象、`org/springframework` 配下に置いた模造アノテーションなど）。
- fixture のクラスファイルは `jig-core` のテストコンパイル成果物として得ている。`testing.TestSupport` が `DefaultPackageClass.class` の位置から出力ディレクトリを逆算し、`build/classes/java/test/stub` と `src/test/java/stub` の対を解析対象として組み立てる。したがって fixture は toolchain 21 でコンパイルされたクラスファイルしか存在しない。
- 実ライブラリを読み取る契約の一部は、既に模した fixture で検証している。`org.springframework.stereotype.Controller` や `org.springframework.data.repository.CrudRepository` などを `src/test/java` に最小定義しており、実ライブラリをテスト依存に加えていない。
- 調査用の `learning` は `src/test/java/learning` と `src/test/java/org/dddjava/jig/infrastructure/javaparser/learning` の二箇所にある。
- JS テストは `jig-core/src/test/js` に約 20 ファイル。Node.js 組み込み test runner と jsdom で完結しており、`package.json` に `playwright` は含めていない。
- GitHub Actions のパイプラインは PR gate・main upkeep・release の三本。Java は toolchain 21 固定で、CI の JDK も 21 単一。main upkeep のみ Ubuntu と Windows の両方で実行する。
- 上記とは別に CircleCI があり、main の push でビルドを、タグ push で Maven Central と Gradle Plugin Portal への公開を行う。CircleCI は廃止する方向だが、廃止までは実際の公開を担い続ける。廃止は別スケジュールで扱い、**この設計では CircleCI の設定を変更しない**。そのため CircleCI 側は、fixture のコンパイルに必要な JDK 25 を Gradle の toolchain 自動プロビジョニングで取得する（`settings.gradle.kts` の foojay-resolver による）。
- Gradle プラグインの TestKit テスト（`JigPluginFunctionalTest`）は `SupportGradleVersion` の全バージョンを **PR 時点から**実行している。ただし CircleCI 環境ではメモリ上限のため無効化されている。

この保証水準を下げないことを移行の前提条件とする。

## 品質モデル

JIG の利用者が信頼するのは、内部オブジェクトの生成手順ではなく、入力したプロジェクトから正しいドキュメントを再現可能に出力できることである。テストは次の四つの契約を守るために置く。

| 契約 | 失敗時に利用者へ起きること | 主な検証手段 |
| --- | --- | --- |
| 解析契約 | Java・クラス・設定ファイルからモデルを誤認識する | 単体、統合、代表プロジェクトのゴールデンテスト |
| 出力契約 | HTML・データ JS・JSON・アセットが壊れる、または内容が変わる | スナップショット、構造検証、ブラウザ E2E |
| 起動契約 | CLI または Gradle タスクから実行できない | ブラックボックス E2E、Gradle TestKit |
| 互換性契約 | サポートする JDK、OS、Gradle、ブラウザで動かない。サポートする Java バージョンでコンパイルされたクラスファイルを解析できない | 互換性マトリクス、複数バージョンでコンパイルした fixture、リリース候補検証 |

互換性契約には二つの軸がある。JIG 自体を動かす環境（JDK 21、OS、Gradle、ブラウザ）と、JIG が読み取る対象（Java 8 以上でコンパイルされたクラスファイル）である。後者は README に明記した約束でありながら、現在は fixture が toolchain 21 でしか作られていないため未検証である。サポート範囲は次の ADR に従う。

| 軸 | ADR |
| --- | --- |
| 実行 JDK・解析対象の Java バージョン | `docs/adr/java_version_support_policy.md` |
| Gradle | `docs/adr/gradle_version_support_policy.md` |
| ブラウザ | `docs/adr/browser_support_policy.md` |

内部実装の都合だけを確認するテストは、上記のいずれかの契約に結び付ける。結び付けられないテストは原則として作らない。

## テスト階層

テストを実行技術ではなく、隔離の度合いと保証する契約で分類する。同じ振る舞いを複数階層で重複させない。下位層は原因を特定し、上位層は結線と利用者体験を保証する。

| 階層 | 対象 | 許可する依存・I/O | 代表的な検証 | 実行頻度 |
| --- | --- | --- | --- | --- |
| Unit | 値オブジェクト、判定、変換、ドメイン規則、画面資産の純粋関数 | 実ファイル、ネットワーク、Gradle、ブラウザを使わない | 境界値、同値類、不変条件、例外契約 | 全 PR |
| Component | 一つのアダプターまたはアプリケーション・ユースケース | テンポラリファイル、埋め込みパーサー、テスト用実装まで | 読み込みからモデル化、モデルから一種類の出力まで | 全 PR |
| Contract | JIG が生成・消費する公開形式 | バージョン管理された fixture のみ | JSON/データ JS/HTML のスキーマ・必須要素・互換性、同一入力に対する同一出力 | 全 PR |
| E2E | CLI、Gradle プラグイン、生成済みサイト | 実プロセスと隔離した代表プロジェクト | コマンド実行、成果物、主要画面操作 | 全 PR |
| Compatibility | サポート対象の組合せ | 実 JDK/OS/Gradle/ブラウザ、複数バージョンでコンパイルした fixture | 同一の公開シナリオ、解析対象クラスファイルのバージョン差 | Gradle と解析対象バージョンは PR、OS は main、その他は release |

解析対象クラスファイルのバージョン差は、実行環境の組合せと違い CI のマトリクスを増やさずに検証できる。fixture のコンパイル時に `--release` を切り替えるだけで済むため、Compatibility に属しながら PR で実行する。

決定性（同一入力から同一出力が得られること）は独立した階層を設けず、Contract の検証項目として扱う。性能とメモリ消費は自動化した合否判定を置かず、必要時に計測する（後述）。

`learning` は実行対象のテスト階層に含めない。前述の二箇所の調査コードを `src/test` から分離し、CI で品質保証として実行されない場所へ置く。将来の仕様を固定したくなった時点で、目的に合う階層へ昇格する。

## モジュールとソースセット

テストコードを本番パッケージの鏡写しにするのではなく、対象契約ごとに配置する。共通の fixture とテスト支援コードは、プロダクトのソースセットにも各モジュールの private なテストコードにも置かない。

```text
jig-test-fixtures/                          # テスト専用の独立モジュール。公開成果物には含めない
  src/main/java/.../fixtures/               # fixture の配置・展開・正規化だけを担う API
  src/main/resources/contracts/             # 入力と期待する公開成果物
  projects/<name>/                          # 小さな代表プロジェクト（Java ソース、設定ファイル、README）

jig-core/
  src/test/java/                            # Unit と Component
  src/contractTest/java/                    # 出力形式・解析形式の Contract
  src/contractTest/resources/

jig-cli/
  src/test/java/                            # 引数・設定の Unit/Component
  src/e2eTest/java/                         # CLI プロセスを起動する公開シナリオ

jig-gradle-plugin/
  src/test/java/                            # 拡張設定・タスク配線の Unit と TestKit シナリオ

jig-core/src/test/js/                       # 既存の Node + jsdom テスト（Unit/Component）
jig-core/src/test/js/contract/              # 生成 HTML/データとの境界
jig-core/src/test/playwright/               # Playwright による実ブラウザ検証
```

`jig-test-fixtures` は Gradle の `java-test-fixtures` 機能ではなく、`settings.gradle.kts` に追加する独立モジュールとして実現する。fixture は `jig-core` `jig-cli` `jig-gradle-plugin` の三モジュールから参照するため一箇所に集約する必要がある。公開する `jig-core` のライフサイクルと依存関係から fixture を完全に分離し、fixture 用の依存を公開成果物へ波及させないためである。独立モジュールは公開対象から外す。他モジュールが依存できるのは fixture の展開・正規化 API とリソースだけであり、本番コードへの依存は禁止する。

Gradle プラグインの TestKit テストは `src/functionalTest` へ分離せず、既存の `src/test` に置いたままにする。分離しても実行タイミングは変わらず（下記のとおり PR で全サポートバージョンを実行する）、ソースセットを増やす便益がないためである。階層としては E2E に属するが、モジュール全体が小さいため物理的な分離は行わない。

### 代表プロジェクトのコンパイル

JIG の入力はクラスファイルである。代表プロジェクトの Java ソースをリソースとして置くだけでは解析対象にならないため、**代表プロジェクトごとに専用のソースセットを定義してコンパイルする**。

- `jig-test-fixtures` のビルドスクリプトが `projects/<name>` を走査し、代表プロジェクトごとにソースセットとコンパイルタスクを登録する。出力は `build/fixtures/<name>/classes` に分離する。
- fixture 配置 API は、代表プロジェクトごとに「クラス出力ディレクトリ」と「ソースディレクトリ」の対を返す。利用側はパスを組み立てず、この API から `SourceBasePaths` 相当を得る。現在 `testing.TestSupport` がクラスファイルの位置から出力ディレクトリを逆算している処理は、この API に置き換えて消す。
- 代表プロジェクトのソースセットは互いに独立させ、`jig-core` の本番・テストコードへ依存させない。あるプロジェクトの解析結果が他のプロジェクトの内容に影響されないようにするためである。
- 解析対象バージョンの検証には、同一ソースを `--release` で切り替えてコンパイルした三つの出力を作る。全 fixture を三重にはせず、この目的の fixture のみに適用する。
- TestKit は別プロセスで Gradle を起動するため、テンポラリディレクトリへ代表プロジェクトを展開する必要がある。展開も fixture API の責務に含める。各テストがビルドスクリプト文字列を個別に組み立てない。

検証点（下限・toolchain と同じバージョン・最新 LTS）とサポート終了の条件は `docs/adr/java_version_support_policy.md` に従う。事前コンパイル済みのクラスファイルを fixture に持ってサポートを延命しないため、fixture は常にソースから再現できる。

## 代表プロジェクトと fixture の方針

代表プロジェクトは「大量のスタブ」ではなく、利用者の入力を最小サイズで表す仕様資産とする。各プロジェクトは一つの意図を持ち、README に対象契約と必要な出力を記す。

| fixture | 固定する契約 |
| --- | --- |
| `minimal-java` | 最小の Java プロジェクトを解析し、サイトを生成できる |
| `multi-module-gradle` | 複数モジュール、クラスパス、ソースセットの収集 |
| `spring-data` | Spring Data JDBC の認識規則 |
| `mybatis` | Mapper と SQL の認識規則 |
| `bytecode-only` | ソースなしクラスファイルの解析 |
| `bytecode-compat` | Java 8 / 21 / 25 でコンパイルしたクラスファイルを同じように解析できる |
| `invalid-input` | 不正・欠損・読めない入力の失敗または継続方針 |
| `showcase` | 主要画面が意味のある内容で描画される（Web の Contract と E2E の入力） |

`showcase` は、各ドキュメントが空表示にならない程度の型・関連・Javadoc・パッケージ階層を持つ典型プロジェクトとする。画面検証の入力を JIG 自身の解析結果に頼らないために用意する。自己解析の結果は対象クラス数が少なく、「1 クラスだけのパッケージ」のような特定条件を満たすデータが存在しないことが既に分かっており、また JIG 自身のコード変更で入力が動くため画面の期待値が安定しない（`jig-core/src/test/playwright/README.md`）。

自己解析は、大きな入力に対する挙動の確認にだけ使う（JIG は自分自身を解析でき、既に自己解析でサンプルドキュメントを生成している）。保守すべき fixture を増やさずに済み、入力が実際のコードベースと共に育つ利点がある。期待値を固定する検証には使わない。

fixture は次の規則に従う。

- 時刻、絶対パス、ランダム値、OS の区切り文字を含めない。避けられない値は成果物比較の前に正規化する。
- 期待結果は全ファイルの無差別な文字列比較にしない。JSON は構造比較、HTML は DOM の必須構造・リンク・アクセシビリティ属性を比較し、図や大きな文章だけを必要に応じてスナップショットにする。
- スナップショットの更新は専用タスクで明示的に行い、通常のテスト実行中には更新しない。レビューでは差分を人間が読める形式で確認する。
- 実際のライブラリを読み取る契約は、ライブラリの最小公開 API を模した fixture を優先する。現在 `src/test/java/org/springframework` 配下で行っている方式を代表プロジェクトへ引き継ぐ。実ライブラリとの結線確認だけは Compatibility または E2E に一件置く。

## 各プロダクトの責務

### jig-core

- ドメイン規則、識別子、関係導出、フィルタリング、設定解釈は Unit で網羅する。テストは公開 API または同一パッケージの明確な規則を対象とし、private 実装の構造を検証しない。
- ASM、JavaParser、MyBatis、Spring Data、Git、ファイルシステムのアダプターは Component で検証する。各アダプターに一つ以上の成功・空入力・不正入力・境界入力を持たせる。
- ドキュメント生成は「入力モデル → 公開成果物」の Contract を中心にする。HTML、データ JS、JSON の生成順序や内部 DTO はテストしない。
- `JigExecutor` を通す代表シナリオを少数の E2E として置き、各アダプターの全分岐をそこへ持ち込まない。

### CLI

- 引数の解釈、デフォルト、エラー表示は Unit/Component で検証する。
- E2E は配布可能な CLI を別プロセスで起動し、標準出力・終了コード・生成物を公開契約として確認する。Spring の内部 Bean 構成や private メソッドは確認しない。
- 実行ディレクトリ、相対パス、空プロジェクト、不正設定を必須シナリオに含める。

### Gradle プラグイン

- 拡張 DSL の値からタスク設定への変換は Unit で確認する。
- TestKit によるシナリオは `SupportGradleVersion` のサポート対象バージョンごとに実行する。現状どおり **PR で全バージョンを実行**し、main 以降へ後ろ倒ししない。`docs/adr/gradle_version_support_policy.md` に従う。
- 最小セットは「Java プラグインなしの明確な失敗」「クリーン状態からのコンパイル依存」「単一・複数ソースセット」「マルチプロジェクト」「設定オプション」「成果物生成」とする。
- TestKit のプロジェクト作成は fixture の展開 API を使う。
- CircleCI での無効化（メモリ上限による `@DisabledIfEnvironmentVariable`）は現状のまま維持する。実行を担保するのは GitHub Actions 側である。

### Web 資産

- JavaScript の Unit は DOM を最小化し、表示規則・データ変換・イベント配線を分けて確認する。jsdom は DOM 依存の Component に限定する。既存の `jig-core/src/test/js` はこの位置づけを継続する。
- Contract は JIG が生成する HTML とデータ JS を読み込み、必須の data 属性、ID、JSON 形式、エスケープ規則を確認する。入力は `showcase` fixture を解析した生成物とする。
- Playwright E2E を導入する場合、`package.json` に `playwright` を依存として追加する判断が必要になる。現在は「JS テストは jsdom で完結する」という理由で意図的に依存へ入れていない（`jig-core/src/test/playwright/README.md`）。この判断を覆すのは、Mermaid の実描画・CSS レイアウト・スクロール挙動など jsdom が原理的に検証できない対象に限る。それ以外は jsdom の Contract で足りる。
- 覆す場合の前提として、(1) CI で `~/.cache/ms-playwright` をキャッシュする、(2) E2E の入力は `jig-cli:bootJar` と `showcase` fixture の解析で作るためビルド時間が上乗せされる、この二点を見込む。所要時間が PR gate に見合わないと判明した場合は release candidate へ移す。
- 自動検証の対象は Chromium のみとし、画面ごとに「ページが読み込める」「主要データが描画される」「主要導線が操作できる」を一シナリオに絞る（`docs/adr/browser_support_policy.md`）。
- `file://` で開けることは生成物の要件である。E2E は HTTP サーバー経由と `file://` の両方を対象にする。
- Mermaid や CDN のような外部依存は、通常の CI でネットワークに依存しないよう固定・代替する。実 CDN との結線は release candidate に分離する。

## ビルドと実行インターフェース

開発者と CI が同じ品質検証を実行できるよう、Gradle を Java テストの唯一の入口、npm を Web テストの唯一の入口にする。各タスクは名前から隔離度と所要時間を判断できるようにする。

開発中の入口と PR 相当の入口は分ける。`e2eTest` は `jig-cli:bootJar` と代表プロジェクトの解析を入力とするため、これを含む集約を変更のたびに実行するのは開発サイクルに合わない。一方で Contract は軽く、出力形式の破壊を最も早く捉える層なので、開発中の入口に含める。

| 入口 | 内容 | 期待時間 | 用途 |
| --- | --- | --- | --- |
| `./gradlew test` | 全モジュールの Unit/Component と Gradle TestKit | 短い〜中程度 | 開発中・PR |
| `./gradlew contractTest` | 公開形式の Contract | 短い | 開発中・PR |
| `./gradlew e2eTest` | CLI と最小の生成シナリオ | 中程度 | PR |
| `./gradlew qualityCheck` | `check`・E2E・カバレッジレポートを束ねる | 中程度 | PR の Java 側品質入口 |
| `npm run test` | Web の Unit/Component（現状どおり） | 短い | 開発中・PR |
| `npm run test:contract` | Web と生成物の Contract | 短い | 開発中・PR |
| `npm run test:e2e` | Playwright の最小 E2E（導入する場合） | 中程度 | PR |
| `npm run test:full` | Web の Unit/Component と Contract、Gradle の `clean test contractTest` | 中程度 | 開発中の標準入口 |
| `npm run test:pr` | CSS lint、導入済みの Web 全テスト、`clean build qualityCheck` | 長い | コミット前・PR 前 |

`npm run test:full` は現状「Web テストと Gradle テストの通し実行」であり、Contract を新設する際に両方の Contract を含める形へ広げる。開発中に出力契約の破壊へ気づけないまま PR で初めて落ちる状態を作らないためである。E2E は含めない。

`check` を `qualityCheck` に依存させてはならない。`check` は `build` から呼ばれるため、そうすると e2eTest まで `build` に載り、「重い検証は明示タスクへ分離する」という方針と矛盾する。依存の向きは逆にする。

- `check` — Unit/Component/Contract まで。`build` が重くならない範囲に留める。
- `qualityCheck` — `check`・`e2eTest`・`jacocoTestReport` を束ねる集約タスク。PR CI はこれを呼ぶ。

PR CI は `build` を省略せず、`./gradlew build qualityCheck` を実行する。`build` は公開・配布する JAR、sources JAR、Javadoc JAR を含む成果物の組み立てを検証し、`qualityCheck` は Contract/E2E を明示的に追加する。`npm run test:pr` も同じ組合せを実行する。これにより、成果物の検証を維持したまま、`build` だけでは分からない重い検証を明示する。

npm scripts に `test:contract` / `test:e2e` / `test:pr` を追加し `test:full` を広げる際は、CLAUDE.md の「テスト実行ポリシー」節も同時に更新し、入口を段階で使い分ける形にする。`*.js` のみの変更は `npm run test`、通常の変更は `npm run test:full`、コミット前・PR 前は `npm run test:pr` とする。

## CI の設計

PR では速いフィードバックと失敗原因の分離を優先する。main と release では互換性を優先する。GitHub Actions のパイプラインは既存の三本を維持し、新設しない。CircleCI は廃止する方向であり、この設計では変更しない。

| パイプライン | 実行内容 | 成功条件 |
| --- | --- | --- |
| PR gate | `./gradlew build qualityCheck`、Web Unit/Contract（および導入時は E2E）、CSS lint | Gradle TestKit の**全サポートバージョン**と解析対象バージョン（Java 8/21/25）を含め、すべて成功。公開・配布成果物と生成物・スナップショットに未承認の差分がない |
| main upkeep | PR gate に加え、Ubuntu と Windows での Java 実行 | すべて成功。OS 依存の差分がない |
| release candidate | 配布物から CLI・プラグインを利用する E2E、外部ライブラリ・実 CDN との結線、脆弱性確認 | リリース対象の成果物で成功 |

release candidate の検証は、タグ push で動く `release.yml` のリリースビルドに追加する。CircleCI を変更しない方針のため、この検証は次の非対称を持つ。

- 検証が失敗すると Draft リリースが作られず、GitHub Releases 経由の `jig-cli.jar` 配布は止まる。
- Maven Central と Gradle Plugin Portal への公開は CircleCI の `publish` ジョブが担っており、`release.yml` の失敗では止まらない。

これは CircleCI 廃止までの既知の制約として受け入れる。CircleCI の公開が `release.yml` 側へ移った時点で、release candidate の検証を公開タスクより前に置き、公開のゲートとして機能させる。廃止作業の完了条件にこの配置を含める。

配布物からプラグインを利用する E2E は、Gradle Plugin Portal への公開を待たず、`publishToMavenLocal` した成果物を TestKit から解決して行う。公開前に検証できる形を維持するためである。

性能とメモリ消費は自動の合否判定を CI に置かない。基準値の維持コストとノイズに見合わないためである。必要になった時点で自己解析を入力とした計測を手動で行い、退行が疑われる変更のレビューで参照する。

失敗時には JUnit XML、HTML レポート、失敗した fixture の入力と正規化後の期待・実際の差分、Playwright の screenshot/video/trace を保持する。成功時に巨大な成果物を保存しない。

## テストコードの規律と品質指標

テスト自体が保守不能にならないよう、次を守る。

- `jig-test-fixtures` は本番成果物の依存グラフに入らない。Gradle の依存宣言で構造的に担保し、`jig-core` の publication に含めない。
- テスト専用 API を本番コードに公開しない。必要な観測点は公開契約または専用の設計された境界に置く。
- Unit はネットワークと実プロセスを起動しない。E2E だけがそれらを許可される。
- テスト名は入力、操作、観測可能な結果を表す。実装メソッド名や issue 番号は主語にしない。
- カバレッジは JaCoCo で収集する。レポートは PR gate で生成して保存し、全体行カバレッジのしきい値による合否判定（`jacocoTestCoverageVerification` の違反ルール）は設定しない。代わりに、変更行カバレッジ、公開シナリオの網羅、失敗の再現性、テスト時間を継続監視する。

本番コードのパッケージ間依存については、自動検査を導入しない。依存方向の規則を機械的に定義する便益より、規則と実装の乖離を保守する手間が上回るためである。

## 導入順序

既存テストを一括で置換しない。各段階で同じ保証を保ったまま移し、旧テストは置換後に削除する。段階 1〜5 を必須とし、6・7 は必須段階が安定してから判断する。

### 必須

1. この設計をもとに `docs/adr/` の既存 ADR と同じ形式で ADR を書き起こして承認する。次の三本を作成済みで、承認をもって完了とする。
   - `docs/adr/test_architecture_policy.md` — 契約中心のテスト構成、fixture の集約、タスク階層の決定
   - `docs/adr/java_version_support_policy.md` — 実行 JDK と解析対象 Java バージョン、下限を切る条件
   - `docs/adr/browser_support_policy.md` — 対象ブラウザ、`file://` 要件、CDN 依存の扱い

   Gradle のサポート範囲は既存の `docs/adr/gradle_version_support_policy.md` に従い、変更しない。
2. テストタスクと JaCoCo を導入する。導入済みで、内訳は次のとおり。
   - `jig-core` に `contractTest`、`jig-cli` に `e2eTest` を `JvmTestSuite` で定義する。`contractTest` は `check` に載せ、`e2eTest` は載せない。buildSrc の `jig.contract-test-conventions` / `jig.e2e-test-conventions` で適用する。
   - ルートの `qualityCheck` が各モジュールの `check`・`jig-cli:e2eTest`・`jacocoTestReport` を束ねる。
   - JaCoCo は `jig.java-conventions` で全モジュールに適用し、しきい値による判定（`jacocoTestCoverageVerification` の違反ルール）は設定しない。
   - PR CI は `./gradlew build qualityCheck` を実行し、カバレッジレポートを保存する。
   - `npm run test:full` は `clean test contractTest`、`npm run test:pr` は `clean build qualityCheck` を実行する。

   Web 側の `test:contract` / `test:e2e` は検証対象が生まれる段階（4・7）で追加する。既存テストはこの段階では移動しない。
3. `jig-test-fixtures` モジュールを追加する。導入済みで、内訳は次のとおり。
   - `projects/<name>` ごとに `JavaCompile` タスクを登録し、`build/fixtures/<name>/classes-<release>` へ出力する。ソースは同じ階層の `sources` へ配置する。
   - `minimal-java`（21）と `bytecode-compat`（8・21・25）を置く。25 のクラスファイルを作るため toolchain 25 を使う。GitHub Actions は JDK 25 を追加でセットアップする。
   - `JigFixtures.project(name)` が配置先を解決する。ビルドがシステムプロパティ `jig.fixtures.root` で渡し、利用側はパスを組み立てない。
   - `jig-core` の `contractTest` に解析対象バージョンの契約テストを置き、`check` から実行する。

   代表プロジェクトのテンポラリへの展開は TestKit から使う段階で、正規化とスナップショット更新手順は比較対象の成果物が生まれる段階 4 で追加する。
4. 最重要の公開シナリオ（CLI の最小生成、Gradle のクリーン実行、主要ページの描画）を E2E/Contract として先に固定する。画面検証の入力となる `showcase` fixture もここで用意する。
5. 生成物の文字列比較を構造比較と限定スナップショットへ置き換え、出力契約を安定化する。

### 必須段階の完了後に判断

6. `jig-core` の既存テストを Unit と Component に分類して移す。`stub` 配下の約 110 と、`sample/data` や `*/ut` などの約 60 のうち仕様 fixture であるものを `jig-test-fixtures` の代表プロジェクトへ、`learning` の二箇所は CI 対象外へ移す。`testing.TestSupport` のパス逆算は fixture API へ置き換えて削除する。
7. Playwright E2E の導入可否を、Web 資産の節に記した基準（jsdom で検証できない対象があるか、PR gate の所要時間に見合うか）で判断する。導入するなら旧ディレクトリと手動 Playwright 手順を整理して入口に統合する。

各段階の完了条件は「新しい層に同等以上の公開契約テストがあり、CI で安定していること」とする。カバレッジ値だけを完了条件にしない。

## 採用しない方針

- 単体テストだけで出力・起動の品質を保証すること。
- Gradle の全サポートバージョン検証を PR から後ろ倒しすること。現状の保証水準を下げない。
- パイプラインを増やして検証を分散させること。運用コストに見合わない。性能・変異解析のような重い自動検証は導入しない。
- 本番コードの依存方向を ArchUnit 等で自動検査すること。
- カバレッジのしきい値を CI の合否基準にすること。
- スナップショットだけで生成物を検証すること。構造・意味の検証を優先する。
- 期待値を固定する検証の入力に自己解析の結果を使うこと。入力が製品コードと共に動くため、無関係な変更で期待値が壊れる。
- 事前コンパイル済みのクラスファイルを fixture に持ち、ツールチェーンで再現できなくなった解析対象バージョンのサポートを続けること。
- テストの都合で本番コードの可視性を上げること。必要な観測点は公開契約または専用の設計された境界に置く。
- 実ネットワーク、現在時刻、共有ディレクトリ、テスト実行順序に依存すること。

## 成果

この設計により、変更時には速い層で原因を特定し、遅い層で実際の利用経路を確認できる。fixture と公開契約が共通資産になるため、JIG の解析対象・出力形式・対応環境を追加するときも、どこにどの粒度のテストを置くかが一意に決まる。
