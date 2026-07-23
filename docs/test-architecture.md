# JIG テストアーキテクチャ

## 目的

JIG の品質保証を、実装クラス単位のテストの寄せ集めから、利用者が観測する契約を中心に組み立て直す。対象は `jig-core`、CLI、Gradle プラグイン、生成されるドキュメント、およびブラウザ上の資産である。

この文書は目標アーキテクチャと移行順序を定める設計書であり、既存テストの移動・削除・追加やビルド設定の変更は含まない。

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
| Contract | JIG が生成・消費する公開形式 | バージョン管理された fixture のみ | JSON/データ JS/HTML のスキーマ・必須要素・互換性 | 全 PR |
| E2E | CLI、Gradle プラグイン、生成済みサイト | 実プロセスと隔離した代表プロジェクト | コマンド実行、成果物、主要画面操作 | 全 PR は最小セット、完全版は nightly/release |
| Compatibility | サポート対象の組合せ | 実 JDK/OS/Gradle/ブラウザ | 同一の公開シナリオ | main、nightly、release |
| Non-functional | 性能、メモリ、決定性、セキュリティ | 固定したベンチマーク入力 | 所要時間、メモリ上限、同一入力の同一出力 | nightly/release |

`learning` は実行対象のテスト階層に含めない。調査コードは `src/test` から分離し、CI で品質保証として実行されない場所へ置く。将来の仕様を固定したくなった時点で、目的に合う階層へ昇格する。

## モジュールとソースセット

テストコードを本番パッケージの鏡写しにするのではなく、対象契約ごとに配置する。共通の fixture とテスト支援コードは、プロダクトのソースセットにも各モジュールの private なテストコードにも置かない。

```text
jig-test-fixtures/                         # テスト専用。公開成果物には含めない
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
  src/test/java/                            # 拡張設定・タスク配線の Unit
  src/functionalTest/java/                  # Gradle TestKit による公開シナリオ

jig-web/ または jig-core の web 専用テスト領域
  test/unit/                                # Node + jsdom の Unit
  test/contract/                            # 生成 HTML/データとの境界
  test/e2e/                                 # Playwright による実ブラウザ検証
```

`jig-test-fixtures` は Java の `java-test-fixtures` 機能または専用の非公開 Gradle モジュールで実現する。後者を採用する場合も、他モジュールが依存できるのは fixture 読み込み・正規化 API とリソースだけであり、本番コードへの依存は禁止する。

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
| `large-sample` | 性能の傾向とメモリ消費の監視 |

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
- `functionalTest` は Gradle TestKit を使い、サポート対象 Gradle バージョンごとに同一の公開シナリオを実行する。既存の Gradle バージョンサポート方針に従う。
- 最小セットは「Java プラグインなしの明確な失敗」「クリーン状態からのコンパイル依存」「単一・複数ソースセット」「マルチプロジェクト」「設定オプション」「成果物生成」とする。
- TestKit のプロジェクト作成は fixture を参照し、各テストがビルドスクリプト文字列を個別に組み立てない。

### Web 資産

- JavaScript の Unit は DOM を最小化し、表示規則・データ変換・イベント配線を分けて確認する。jsdom は DOM 依存の Component に限定する。
- Contract は JIG が生成する HTML とデータ JS を読み込み、必須の data 属性、ID、JSON 形式、エスケープ規則を確認する。
- Playwright E2E は Chromium のみを PR の最小対象とし、画面ごとに「ページが読み込める」「主要データが描画される」「主要導線が操作できる」を一シナリオに絞る。Firefox/WebKit をサポート対象にする決定がある場合だけ Compatibility に加える。
- Mermaid や CDN のような外部依存は、通常の CI でネットワークに依存しないよう固定・代替する。実 CDN との結線は release 前の限定チェックに分離する。

## ビルドと実行インターフェース

開発者と CI が同じ入口を使えるよう、Gradle を Java テストの唯一の入口、npm を Web テストの唯一の入口にする。各タスクは名前から隔離度と所要時間を判断できるようにする。

| 入口 | 内容 | 期待時間 | 用途 |
| --- | --- | --- | --- |
| `./gradlew test` | 全モジュールの Unit/Component | 短い | 開発中・PR |
| `./gradlew contractTest` | 公開形式の Contract | 短い | PR |
| `./gradlew functionalTest` | Gradle TestKit | 中程度 | PR |
| `./gradlew e2eTest` | CLI と最小の生成シナリオ | 中程度 | PR |
| `npm run test:unit` | Web Unit | 短い | 開発中・PR |
| `npm run test:contract` | Web と生成物の Contract | 短い | PR |
| `npm run test:e2e` | Playwright の最小 E2E | 中程度 | PR |
| `./gradlew compatibilityTest` と `npm run test:compatibility` | 全サポート組合せ | 長い | main/nightly/release |
| `./gradlew qualityCheck` と `npm run quality-check` | 上記 PR 対象を束ねる | 中程度 | PR の唯一の必須入口 |

実装時には Gradle の `check` を `qualityCheck` に依存させる。`build` だけがどのテストを含むかを暗黙に決める状態をなくし、重い検証は明示タスクへ分離する。

## CI の設計

PR では速いフィードバックと失敗原因の分離を優先する。main と release では互換性を優先する。

| パイプライン | 実行内容 | 成功条件 |
| --- | --- | --- |
| PR gate | Java Unit/Component、Contract、Gradle functional の最小セット、Web Unit/Contract/E2E、CSS lint、アーキテクチャ規則 | すべて成功。生成物・スナップショットに未承認の差分がない |
| main upkeep | PR gate に加え、Ubuntu と Windows での Java 実行、サポートする Gradle の全組合せ | すべて成功。OS 依存の差分がない |
| nightly | 性能、決定性、全ブラウザ互換性、外部ライブラリとの結線 | 基準値を超えない。失敗は issue 化する |
| release candidate | 配布物から CLI・プラグインを利用する E2E、公開サイトのスモークテスト、脆弱性確認 | リリース対象の成果物で成功 |

失敗時には JUnit XML、HTML レポート、失敗した fixture の入力と正規化後の期待・実際の差分、Playwright の screenshot/video/trace を保持する。成功時に巨大な成果物を保存しない。

## アーキテクチャ規則と品質指標

テスト自体が保守不能にならないよう、次を自動検査する。

- 本番依存は `adapter → application/domain`、`infrastructure → domain/application` の向きだけにする。逆向きの依存とテスト専用 API の本番公開を禁止する。
- `jig-test-fixtures` は本番成果物の依存グラフに入らない。
- Unit はネットワークと実プロセスを起動しない。E2E だけがそれらを許可される。
- テスト名は入力、操作、観測可能な結果を表す。実装メソッド名や issue 番号は主語にしない。
- カバレッジは品質の代理指標として収集するが、全体行カバレッジを単独の合否基準にしない。代わりに、変更行カバレッジ、公開シナリオの網羅、失敗の再現性、テスト時間を継続監視する。
- 解析規則・変換規則のような分岐密度が高い箇所は、代表的な対象に限って mutation testing を nightly で行い、テストの検出力を確認する。

## 導入順序

既存テストを一括で置換しない。各段階で同じ保証を保ったまま移し、旧テストは置換後に削除する。

1. この設計を ADR として承認し、サポート対象の JDK、Gradle、ブラウザ、性能基準を明文化する。
2. テストタスク、`jig-test-fixtures`、fixture の命名・正規化・スナップショット更新手順だけを導入する。この時点では既存テストを移動しない。
3. 最重要の公開シナリオ（CLI の最小生成、Gradle のクリーン実行、主要ページの描画）を E2E/Contract として先に固定する。
4. `jig-core` の既存テストを Unit と Component に分類して移す。スタブが仕様 fixture であるものは `jig-test-fixtures` へ、調査コードは CI 対象外へ移す。
5. 生成物の文字列比較を構造比較と限定スナップショットへ置き換え、出力契約を安定化する。
6. Gradle の全サポートバージョン、OS、ブラウザ、性能を main/nightly/release の各パイプラインへ段階的に追加する。
7. 旧ディレクトリ、重複シナリオ、手動 Playwright 手順を削除し、各入口を `qualityCheck` と CI に統合する。

各段階の完了条件は「新しい層に同等以上の公開契約テストがあり、CI で安定していること」とする。カバレッジ値だけを完了条件にしない。

## 採用しない方針

- 単体テストだけで出力・起動の品質を保証すること。
- すべての組合せを PR ごとに実行すること。遅い互換性検証は main/nightly/release に分離する。
- スナップショットだけで生成物を検証すること。構造・意味の検証を優先する。
- テストの都合で本番コードの可視性を上げること。必要な観測点は公開契約または専用の設計された境界に置く。
- 実ネットワーク、現在時刻、共有ディレクトリ、テスト実行順序に依存すること。

## 成果

この設計により、変更時には速い層で原因を特定し、遅い層で実際の利用経路を確認できる。fixture と公開契約が共通資産になるため、JIG の解析対象・出力形式・対応環境を追加するときも、どこにどの粒度のテストを置くかが一意に決まる。
