# JIG テストアーキテクチャ

## 目的

JIG の品質保証を、実装クラス単位のテストの寄せ集めから、利用者が観測する契約を中心に組み立て直す。対象は `jig-core`、CLI、Gradle プラグイン、生成されるドキュメント、およびブラウザ上の資産である。

この文書は目標アーキテクチャと移行順序を定める設計書であり、既存テストの移動・削除・追加やビルド設定の変更は含まない。

## 現状

移行の起点となる現在の構成は次のとおり。

- モジュールは `jig-core` `jig-cli` `jig-gradle-plugin` の三つ。各モジュールのテストは `src/test` 単一のソースセット。
- `jig-core` に Java テストクラスが約 50、`src/test/java/stub` 配下に約 110 のスタブクラスがある。調査用の `learning` は `src/test/java/learning` と `src/test/java/org/dddjava/jig/infrastructure/javaparser/learning` の二箇所にある。
- JS テストは `jig-core/src/test/js` に約 20 ファイル。Node.js 組み込み test runner と jsdom で完結しており、`package.json` に `playwright` は含めていない。
- CI は PR gate・main upkeep・release の三本。Java は toolchain 21 固定で、CI の JDK も 21 単一。main upkeep のみ Ubuntu と Windows の両方で実行する。
- Gradle プラグインの TestKit テスト（`JigPluginFunctionalTest`）は `SupportGradleVersion` の全バージョンを **PR 時点から**実行している。

この保証水準を下げないことを移行の前提条件とする。

## 品質モデル

JIG の利用者が信頼するのは、内部オブジェクトの生成手順ではなく、入力したプロジェクトから正しいドキュメントを再現可能に出力できることである。テストは次の四つの契約を守るために置く。

| 契約 | 失敗時に利用者へ起きること | 主な検証手段 |
| --- | --- | --- |
| 解析契約 | Java・クラス・設定ファイルからモデルを誤認識する | 単体、統合、代表プロジェクトのゴールデンテスト |
| 出力契約 | HTML・データ JS・JSON・アセットが壊れる、または内容が変わる | スナップショット、構造検証、ブラウザ E2E |
| 起動契約 | CLI または Gradle タスクから実行できない | ブラックボックス E2E、Gradle TestKit |
| 互換性契約 | サポートする JDK、OS、Gradle、ブラウザで動かない | 互換性マトリクス、リリース候補検証 |

内部実装の都合だけを確認するテストは、上記のいずれかの契約に結び付ける。結び付けられないテストは原則として作らない。

## テスト階層

テストを実行技術ではなく、隔離の度合いと保証する契約で分類する。同じ振る舞いを複数階層で重複させない。下位層は原因を特定し、上位層は結線と利用者体験を保証する。

| 階層 | 対象 | 許可する依存・I/O | 代表的な検証 | 実行頻度 |
| --- | --- | --- | --- | --- |
| Unit | 値オブジェクト、判定、変換、ドメイン規則、画面資産の純粋関数 | 実ファイル、ネットワーク、Gradle、ブラウザを使わない | 境界値、同値類、不変条件、例外契約 | 全 PR |
| Component | 一つのアダプターまたはアプリケーション・ユースケース | テンポラリファイル、埋め込みパーサー、テスト用実装まで | 読み込みからモデル化、モデルから一種類の出力まで | 全 PR |
| Contract | JIG が生成・消費する公開形式 | バージョン管理された fixture のみ | JSON/データ JS/HTML のスキーマ・必須要素・互換性、同一入力に対する同一出力 | 全 PR |
| E2E | CLI、Gradle プラグイン、生成済みサイト | 実プロセスと隔離した代表プロジェクト | コマンド実行、成果物、主要画面操作 | 全 PR |
| Compatibility | サポート対象の組合せ | 実 JDK/OS/Gradle/ブラウザ | 同一の公開シナリオ | Gradle は PR、OS は main、その他は release |

決定性（同一入力から同一出力が得られること）は独立した階層を設けず、Contract の検証項目として扱う。性能とメモリ消費は自動化した合否判定を置かず、必要時に計測する（後述）。

`learning` は実行対象のテスト階層に含めない。前述の二箇所の調査コードを `src/test` から分離し、CI で品質保証として実行されない場所へ置く。将来の仕様を固定したくなった時点で、目的に合う階層へ昇格する。

## モジュールとソースセット

テストコードを本番パッケージの鏡写しにするのではなく、対象契約ごとに配置する。共通の fixture とテスト支援コードは、プロダクトのソースセットにも各モジュールの private なテストコードにも置かない。

```text
jig-test-fixtures/                          # テスト専用の独立モジュール。公開成果物には含めない
  src/main/resources/projects/              # 小さな代表プロジェクト
  src/main/resources/contracts/             # 入力と期待する公開成果物
  src/main/java/.../fixtures/               # fixture の配置・正規化だけを担う API

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

`jig-test-fixtures` は Gradle の `java-test-fixtures` 機能ではなく、`settings.gradle.kts` に追加する独立モジュールとして実現する。fixture は `jig-core` `jig-cli` `jig-gradle-plugin` の三モジュールから参照するため一箇所に集約する必要がある。公開する `jig-core` のライフサイクルと依存関係から fixture を完全に分離し、fixture 用の依存を公開成果物へ波及させないためである。独立モジュールは公開対象から外す。他モジュールが依存できるのは fixture 読み込み・正規化 API とリソースだけであり、本番コードへの依存は禁止する。

Gradle プラグインの TestKit テストは `src/functionalTest` へ分離せず、既存の `src/test` に置いたままにする。分離しても実行タイミングは変わらず（下記のとおり PR で全サポートバージョンを実行する）、ソースセットを増やす便益がないためである。階層としては E2E に属するが、モジュール全体が小さいため物理的な分離は行わない。

## 代表プロジェクトと fixture の方針

代表プロジェクトは「大量のスタブ」ではなく、利用者の入力を最小サイズで表す仕様資産とする。各プロジェクトは一つの意図を持ち、README に対象契約と必要な出力を記す。

| fixture | 固定する契約 |
| --- | --- |
| `minimal-java` | 最小の Java プロジェクトを解析し、サイトを生成できる |
| `multi-module-gradle` | 複数モジュール、クラスパス、ソースセットの収集 |
| `spring-data` | Spring Data JDBC の認識規則 |
| `mybatis` | Mapper と SQL の認識規則 |
| `bytecode-only` | ソースなしクラスファイルの解析 |
| `invalid-input` | 不正・欠損・読めない入力の失敗または継続方針 |

大きな入力に対する挙動の確認には専用 fixture を作らず、JIG 自身のリポジトリを解析対象にする（JIG は自分自身を解析でき、既に自己解析でサンプルドキュメントを生成している）。保守すべき fixture を増やさずに済み、入力が実際のコードベースと共に育つ利点がある。

fixture は次の規則に従う。

- 時刻、絶対パス、ランダム値、OS の区切り文字を含めない。避けられない値は成果物比較の前に正規化する。
- 期待結果は全ファイルの無差別な文字列比較にしない。JSON は構造比較、HTML は DOM の必須構造・リンク・アクセシビリティ属性を比較し、図や大きな文章だけを必要に応じてスナップショットにする。
- スナップショットの更新は専用タスクで明示的に行い、通常のテスト実行中には更新しない。レビューでは差分を人間が読める形式で確認する。
- 実際のライブラリを読み取る契約は、ライブラリの最小公開 API を模した fixture を優先する。実ライブラリとの結線確認だけは Compatibility または E2E に一件置く。

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
- TestKit のプロジェクト作成は fixture を参照し、各テストがビルドスクリプト文字列を個別に組み立てない。

### Web 資産

- JavaScript の Unit は DOM を最小化し、表示規則・データ変換・イベント配線を分けて確認する。jsdom は DOM 依存の Component に限定する。既存の `jig-core/src/test/js` はこの位置づけを継続する。
- Contract は JIG が生成する HTML とデータ JS を読み込み、必須の data 属性、ID、JSON 形式、エスケープ規則を確認する。
- Playwright E2E を導入する場合、`package.json` に `playwright` を依存として追加する判断が必要になる。現在は「JS テストは jsdom で完結する」という理由で意図的に依存へ入れていない（`jig-core/src/test/playwright/README.md`）。この判断を覆すのは、Mermaid の実描画・CSS レイアウト・スクロール挙動など jsdom が原理的に検証できない対象に限る。それ以外は jsdom の Contract で足りる。
- 覆す場合の前提として、(1) CI で `~/.cache/ms-playwright` をキャッシュする、(2) E2E の入力となる生成物は `jig-cli:bootJar` と自己解析で作るためビルド時間が上乗せされる、この二点を見込む。所要時間が PR gate に見合わないと判明した場合は release candidate へ移す。
- Chromium のみを PR の対象とし、画面ごとに「ページが読み込める」「主要データが描画される」「主要導線が操作できる」を一シナリオに絞る。Firefox/WebKit をサポート対象にする決定がある場合だけ Compatibility に加える。
- Mermaid や CDN のような外部依存は、通常の CI でネットワークに依存しないよう固定・代替する。実 CDN との結線は release candidate に分離する。

## ビルドと実行インターフェース

開発者と CI が同じ品質検証を実行できるよう、Gradle を Java テストの唯一の入口、npm を Web テストの唯一の入口にする。各タスクは名前から隔離度と所要時間を判断できるようにする。

開発中の入口と PR 相当の入口は分ける。`e2eTest` は `jig-cli:bootJar` と自己解析を入力とするため、これを含む集約を変更のたびに実行するのは開発サイクルに合わない。`npm run test:full` は現状の意味（Web テストと Gradle テストの通し実行）を保って開発中の標準入口とし、PR 相当の完全実行は `npm run test:pr` として別に定義する。

| 入口 | 内容 | 期待時間 | 用途 |
| --- | --- | --- | --- |
| `./gradlew test` | 全モジュールの Unit/Component と Gradle TestKit | 短い〜中程度 | 開発中・PR |
| `./gradlew contractTest` | 公開形式の Contract | 短い | PR |
| `./gradlew e2eTest` | CLI と最小の生成シナリオ | 中程度 | PR |
| `./gradlew qualityCheck` | `check` と E2E を束ねる | 中程度 | PR の Java 側品質入口 |
| `npm run test` | Web の Unit/Component（現状どおり） | 短い | 開発中・PR |
| `npm run test:contract` | Web と生成物の Contract | 短い | PR |
| `npm run test:e2e` | Playwright の最小 E2E（導入する場合） | 中程度 | PR |
| `npm run test:full` | Web テストと Gradle テストの通し実行（現状どおり） | 中程度 | 開発中の標準入口 |
| `npm run test:pr` | CSS lint、導入済みの Web 全テスト、`clean build qualityCheck` | 長い | コミット前・PR 前 |

`check` を `qualityCheck` に依存させてはならない。`check` は `build` から呼ばれるため、そうすると e2eTest まで `build` に載り、「重い検証は明示タスクへ分離する」という方針と矛盾する。依存の向きは逆にする。

- `check` — Unit/Component/Contract まで。`build` が重くならない範囲に留める。
- `qualityCheck` — `check` と `e2eTest` を束ねる集約タスク。PR CI はこれを呼ぶ。

PR CI は `build` を省略せず、`./gradlew build qualityCheck` を実行する。`build` は公開・配布する JAR、sources JAR、Javadoc JAR を含む成果物の組み立てを検証し、`qualityCheck` は Contract/E2E を明示的に追加する。`npm run test:pr` も同じ組合せを実行する。これにより、成果物の検証を維持したまま、`build` だけでは分からない重い検証を明示する。

npm scripts に `test:contract` / `test:e2e` / `test:pr` を追加する際は、CLAUDE.md の「テスト実行ポリシー」節も同時に更新し、入口を段階で使い分ける形にする。`*.js` のみの変更は `npm run test`、通常の変更は `npm run test:full`、コミット前・PR 前は `npm run test:pr` とする。

## CI の設計

PR では速いフィードバックと失敗原因の分離を優先する。main と release では互換性を優先する。パイプラインは既存の三本を維持し、新設しない。

| パイプライン | 実行内容 | 成功条件 |
| --- | --- | --- |
| PR gate | `./gradlew build qualityCheck`、Web Unit/Contract（および導入時は E2E）、CSS lint、アーキテクチャ規則 | Gradle TestKit の**全サポートバージョン**を含め、すべて成功。公開・配布成果物と生成物・スナップショットに未承認の差分がない |
| main upkeep | PR gate に加え、Ubuntu と Windows での Java 実行 | すべて成功。OS 依存の差分がない |
| release candidate | 配布物から CLI・プラグインを利用する E2E、公開サイトのスモークテスト、外部ライブラリ・実 CDN との結線、脆弱性確認 | リリース対象の成果物で成功 |

release candidate の検証は、Maven Central と Gradle Plugin Portal へ実際に公開する CircleCI の `publish` ジョブに置く。タグ push で並行起動する `release.yml` にだけ検証を足しても、CircleCI の公開を止められないためである。

`publish` ジョブは既に単一の Gradle 呼び出しで `build` を publish タスクより先に列挙しており、公開前のゲートとして機能している。したがってジョブの分割は不要で、このタスク列に `qualityCheck` と release candidate の検証を加えることで実現する。`release.yml` は Draft リリースの作成に責務を限定し、公開可否の判定は持たない。新しいパイプラインは作らない。

`publish` ジョブ末尾の Dogfooding（生成した CLI で自己解析し成果物を保存する）は検証ではなく、そのバージョンの出力を見せるデモである。公開後の現在の位置のままとし、release candidate の検証には流用しない。

なお `release.yml` に記載のとおり CircleCI から GitHub Actions への移行が進行中である。移行が完了して公開が `release.yml` 側へ移った時点で、ここに置いた検証も同じ「公開の直前」という位置関係を保ったまま移設する。

性能とメモリ消費は自動の合否判定を CI に置かない。基準値の維持コストとノイズに見合わないためである。必要になった時点で自己解析を入力とした計測を手動で行い、退行が疑われる変更のレビューで参照する。

失敗時には JUnit XML、HTML レポート、失敗した fixture の入力と正規化後の期待・実際の差分、Playwright の screenshot/video/trace を保持する。成功時に巨大な成果物を保存しない。

## アーキテクチャ規則と品質指標

テスト自体が保守不能にならないよう、次を自動検査する。

- 本番依存は `adapter → application/domain`、`infrastructure → domain/application` の向きだけにする。逆向きの依存とテスト専用 API の本番公開を禁止する。
- `jig-test-fixtures` は本番成果物の依存グラフに入らない。
- Unit はネットワークと実プロセスを起動しない。E2E だけがそれらを許可される。
- テスト名は入力、操作、観測可能な結果を表す。実装メソッド名や issue 番号は主語にしない。
- カバレッジは品質の代理指標として収集するが、全体行カバレッジを単独の合否基準にしない。代わりに、変更行カバレッジ、公開シナリオの網羅、失敗の再現性、テスト時間を継続監視する。

## 導入順序

既存テストを一括で置換しない。各段階で同じ保証を保ったまま移し、旧テストは置換後に削除する。段階 1〜4 を必須とし、5・6 は必須段階が安定してから判断する。

### 必須

1. この設計をもとに `docs/adr/` の既存 ADR と同じ形式（状況／決定／トレードオフ／採用ガイドライン／結論）で ADR を書き起こして承認し、サポート対象の JDK、Gradle、ブラウザを明文化する。現状 JDK は 21 単一である事実を出発点として記す。
2. テストタスク（`contractTest` `e2eTest` `qualityCheck`）、`jig-test-fixtures` モジュール、fixture の命名・正規化・スナップショット更新手順を導入する。PR CI は `build` を維持したまま `qualityCheck` を追加し、`npm run test:pr` の新設と CLAUDE.md のテスト実行ポリシーの段階分けを行う。この時点では既存テストを移動しない。
3. 最重要の公開シナリオ（CLI の最小生成、Gradle のクリーン実行、主要ページの描画）を E2E/Contract として先に固定する。
4. 生成物の文字列比較を構造比較と限定スナップショットへ置き換え、出力契約を安定化する。

### 必須段階の完了後に判断

5. `jig-core` の既存テストを Unit と Component に分類して移す。スタブが仕様 fixture であるものは `jig-test-fixtures` へ、`learning` の二箇所は CI 対象外へ移す。
6. Playwright E2E の導入可否を、Web 資産の節に記した基準（jsdom で検証できない対象があるか、PR gate の所要時間に見合うか）で判断する。導入するなら旧ディレクトリと手動 Playwright 手順を整理して入口に統合する。

各段階の完了条件は「新しい層に同等以上の公開契約テストがあり、CI で安定していること」とする。カバレッジ値だけを完了条件にしない。

## 採用しない方針

- 単体テストだけで出力・起動の品質を保証すること。
- Gradle の全サポートバージョン検証を PR から後ろ倒しすること。現状の保証水準を下げない。
- パイプラインを増やして検証を分散させること。運用コストに見合わない。性能・変異解析のような重い自動検証は導入しない。
- スナップショットだけで生成物を検証すること。構造・意味の検証を優先する。
- テストの都合で本番コードの可視性を上げること。必要な観測点は公開契約または専用の設計された境界に置く。
- 実ネットワーク、現在時刻、共有ディレクトリ、テスト実行順序に依存すること。

## 成果

この設計により、変更時には速い層で原因を特定し、遅い層で実際の利用経路を確認できる。fixture と公開契約が共通資産になるため、JIG の解析対象・出力形式・対応環境を追加するときも、どこにどの粒度のテストを置くかが一意に決まる。
